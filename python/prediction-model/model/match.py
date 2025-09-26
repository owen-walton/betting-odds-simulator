from dataclasses import dataclass
from enum import Enum
from typing import Dict, Optional
from datetime import date

class DataSource(Enum):
    CRICSHEET = "CRICSHEET"
    CRICAPI = "CRICAPI"

class MarginType(Enum):
    RUNS = "Runs"
    WICKETS = "Wickets"
    ONE_INNINGS_AND_RUNS = "One Innings And Runs"
    UNKNOWN = "Unknown"

@dataclass
class Match:
    match_id: str
    data_source: DataSource
    date: date
    teams: Dict[int, bool]  # team_id -> is_home
    winning_team_id: Optional[int] = None
    toss_winner_id: Optional[int] = None
    margin_size: Optional[int] = None
    margin_type: MarginType = MarginType.UNKNOWN
