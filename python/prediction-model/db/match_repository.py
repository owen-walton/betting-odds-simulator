from datetime import date
from typing import List, Dict
import mysql.connector
from db.connection import get_conn
from model.match import Match, DataSource, MarginType

def get_matches(min_date: date, max_date: date, match_format: str) -> List[Match]:
    conn = get_conn()
    cursor = conn.cursor(dictionary=True)

    sql = """
        SELECT cm.MatchID AS match_id,
               cm.DataSource AS data_source,
               cm.StartDate AS start_date,
               mr.WinningTeamID AS winning_team_id,
               mr.TossWinningTeamID AS toss_winner_id,
               mr.MarginSize AS margin_size,
               mr.MarginType AS margin_type,
               mt.TeamID AS team_id,
               CASE WHEN thv.TeamID IS NOT NULL THEN 1 ELSE 0 END AS is_home
        FROM CricketMatchData.CricketMatch cm
        JOIN CricketMatchData.MatchResult mr
            ON cm.MatchID = mr.MatchID AND cm.DataSource = mr.DataSource
        JOIN CricketMatchData.MatchTeam mt
            ON cm.MatchID = mt.MatchID AND cm.DataSource = mt.DataSource
        LEFT JOIN CricketMatchData.TeamHomeVenue thv
            ON thv.TeamID = mt.TeamID AND thv.VenueID = cm.VenueID
        WHERE cm.FormatName = %s AND cm.StartDate BETWEEN %s AND %s
        ORDER BY cm.MatchID, cm.DataSource;
    """

    cursor.execute(sql, (match_format, min_date, max_date))
    rows = cursor.fetchall()
    cursor.close()
    conn.close()

    matches: List[Match] = []
    current_match_id = None
    current_data_source = None
    current_match: Match = None

    for row in rows:
        # if new match, create a new Match object
        if row["match_id"] != current_match_id or row["data_source"] != current_data_source:
            current_match_id = row["match_id"]
            current_data_source = row["data_source"]
            current_match = Match(
                match_id=row["match_id"],
                data_source=DataSource[row["data_source"]],
                date=row["start_date"],
                teams={},
                winning_team_id=row["winning_team_id"],
                toss_winner_id=row["toss_winner_id"],
                margin_size=row["margin_size"],
                # check against value of enum not name (no capitalisation error)
                margin_type=MarginType(row["margin_type"]) if row["margin_type"] else MarginType.UNKNOWN
            )
            matches.append(current_match)

        # add teams
        current_match.teams[row["team_id"]] = bool(row["is_home"])

    return matches
