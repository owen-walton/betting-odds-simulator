from ml.bayesian_opt import *
from ml.objective_function import *
from db.parameters_repository import *
from db.match_repository import *
from db.team_repository import *
from typing import Tuple, Dict
from model.params import TuningParams
from datetime import date

def tune():

    # lower bound values
    min_params = TuningParams(
        e_value=100.0,
        k_factor=5.0,
        home_adv=25.0,
        toss_adv=2.0,
        runs_win_margin=0.0,
        wickets_win_margin=0.0,
        one_innings_margin_bonus=15.0
    )

    # upper bound values
    max_params = TuningParams(
        e_value=550.0,
        k_factor=70.0,
        home_adv=100.0,
        toss_adv=50.0,
        runs_win_margin=5,
        wickets_win_margin=1,
        one_innings_margin_bonus=60.0
    )

    # create the tuple
    search_bounds: Tuple[TuningParams, TuningParams] = (min_params, max_params)

    params_by_format: Dict[str, TuningParams] = {}
    formats = ["T20", "ODI", "Test"]
    for match_format in formats:
        matches = get_matches(date.min, date.max, match_format)
        """evaluate(TuningParams(
            starting_elo=1500.0,
            k_factor=20.0,
            home_adv=0.0,
            toss_adv=0.0,
            e_value=400.0,
            one_innings_margin_bonus=0.0,
            runs_win_margin=0.0,
            wickets_win_margin=0.0
        ), matches)"""

        params_by_format[match_format] = optimise_params(search_bounds, matches)
        _, final_elo_dict = evaluate(params_by_format[match_format], matches)
        update_elos(match_format, final_elo_dict)

    insert_tuning_params(params_by_format)