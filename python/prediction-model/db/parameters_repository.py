from datetime import datetime, timezone
from typing import Dict
from model.params import TuningParams
from db.connection import get_conn

def insert_tuning_params(params_by_format: Dict[str, TuningParams]):
    """
    Each key in params_by_format is a format name ("Test", "ODI", "T20") mapping
    to a TuningParams instance.

    All formats have the same CreatedAt timestamp
    TuningID is manually assigned as 1 + MAX(TuningID) in the table.
    """
    conn = get_conn()
    cursor = conn.cursor()

    # get next TuningID
    cursor.execute("SELECT COALESCE(MAX(TuningID), 0) + 1 FROM CricketMatchData.TunedParameters;")
    next_id = cursor.fetchone()[0]

    # current timestamp is same for all formats being uploaded
    created_at = datetime.now(timezone.utc)

    # set all old models to inactive
    cursor.execute("UPDATE CricketMatchData.TunedParameters SET IsActive = FALSE;")

    sql = """
        INSERT INTO CricketMatchData.TunedParameters
        (TuningID, FormatName, e_value, k_factor, home_adv, toss_adv, runs_win_margin, 
         wickets_win_margin, one_innings_margin_bonus, CreatedAt, IsActive)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
    """

    for format_name, tp in params_by_format.items():
        data = (
            next_id,
            format_name,
            tp.e_value,
            tp.k_factor,
            tp.home_adv,
            tp.toss_adv,
            tp.runs_win_margin,
            tp.wickets_win_margin,
            tp.one_innings_margin_bonus,
            created_at,
            True
        )
        cursor.execute(sql, data)

    conn.commit()
    cursor.close()
