from dataclasses import dataclass
from typing import Optional


@dataclass
class TuningParams:
    """
    Stores naive values that will be used for optimizing against
    https://www.reddit.com/r/Cricket/comments/1hddsht/an_updated_elo_rating_system_for_test_cricket/
    Link above suggests 25 is a strong naive value for K; will require tuning based on starting ELO etc.
    """
    e_value: float
    k_factor: float
    starting_elo: float
    home_adv: float
    toss_adv: float
    max_draw_chance: float
    runs_win_margin: float
    wickets_win_margin: float
    one_innings_margin_bonus: float

    # default constructor with naive starting values
    def __init__(self,
                 e_value: float = 400.0,
                 k_factor: float = 25.0,
                 starting_elo: float = 1500.0,
                 home_adv: float = 10.0,
                 toss_adv: float = 5.0,
                 max_draw_chance: float = 0.4,
                 runs_win_margin: float = 1.0,
                 wickets_win_margin: float = 1.0,
                 one_innings_margin_bonus = 1.0
                 ):
        self.e_value = e_value
        self.k_factor = k_factor
        self.starting_elo = starting_elo
        self.home_adv = home_adv
        self.toss_adv = toss_adv
        self.max_draw_chance = max_draw_chance
        self.runs_win_margin = runs_win_margin
        self.wickets_win_margin = wickets_win_margin
        self.one_innings_margin_bonus = one_innings_margin_bonus
