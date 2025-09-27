from typing import Dict
from db.connection import get_conn

def update_elos(format_name: str, id_to_elo: Dict[int, float]) -> None:
    """
    Insert or update the TeamFormatELO table with the given ELOs for one format.

    format_name- e.g. "Test", "ODI", "T20"
    id_to_elo- mapping of TeamID -> ELO rating
    """
    conn = get_conn()
    cursor = conn.cursor()

    sql = """
        INSERT INTO CricketMatchData.TeamFormatELO (TeamID, FormatName, ELO)
        VALUES (%s, %s, %s)
        ON DUPLICATE KEY UPDATE ELO = VALUES(ELO)
    """

    for team_id, elo in id_to_elo.items():
        cursor.execute(sql, (team_id, format_name, elo))

    conn.commit()
    cursor.close()
