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


def get_elos(format_name: str) -> Dict[int, float]:
    """
    Fetch all team ELOs for the given format name from the TeamFormatELO table.

    format_name - e.g. "Test", "ODI", "T20"
    Returns: dict mapping TeamID -> ELO
    """
    conn = get_conn()
    cursor = conn.cursor(dictionary=True)

    sql = """
        SELECT TeamID, ELO
        FROM CricketMatchData.TeamFormatELO
        WHERE FormatName = %s
    """

    cursor.execute(sql, (format_name,))
    rows = cursor.fetchall()

    # Build mapping TeamID -> ELO
    id_to_elo = {row["TeamID"]: float(row["ELO"]) for row in rows}

    cursor.close()
    conn.close()
    return id_to_elo

def get_team_id_by_name(team_name: str) -> int:
    """
    Fetch the TeamID for a given team name from the Teams table.

    team_name - full or exact team name (e.g. "India", "Australia")
    Returns: integer TeamID if found, otherwise raises ValueError
    """
    conn = get_conn()
    cursor = conn.cursor(dictionary=True)

    sql = """
        SELECT TeamID
        FROM CricketMatchData.Team
        WHERE Name = %s
        LIMIT 1
    """

    cursor.execute(sql, (team_name,))
    row = cursor.fetchone()

    cursor.close()
    conn.close()

    if row is None:
        raise ValueError(f"Team name '{team_name}' not found in database.")

    return row["TeamID"]