"""
Container class that mirrors TunedParameters database table
Stores a combination of parameters potentially used as the prediction model
"""

from dataclasses import dataclass
from typing import Optional


@dataclass
class TuningParams:
    e_value: float
    k_factor: float
    home_adv: float
    toss_adv: float
    runs_win_margin: float
    wickets_win_margin: float
    one_innings_margin_bonus: float

    # default constructor with naive starting values
    def __init__(self,
                 e_value: float = 400.0,
                 k_factor: float = 25.0,
                 home_adv: float = 10.0,
                 toss_adv: float = 5.0,
                 runs_win_margin: float = 1.0,
                 wickets_win_margin: float = 1.0,
                 one_innings_margin_bonus = 1.0
                 ):
        self.e_value = e_value
        self.k_factor = k_factor
        self.home_adv = home_adv
        self.toss_adv = toss_adv
        self.runs_win_margin = runs_win_margin
        self.wickets_win_margin = wickets_win_margin
        self.one_innings_margin_bonus = one_innings_margin_bonus
