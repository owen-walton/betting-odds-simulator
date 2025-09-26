from ml.bayesian_opt import *
from db.parameters_repository import *
from db.match_repository import *
from typing import Tuple
from model.params import TuningParams

def tune():

    # lower bound values
    min_params = TuningParams(
        e_value=200.0,
        k_factor=10.0,
        starting_elo=1000.0,
        home_adv=0.0,
        toss_adv=0.0,
        max_draw_chance=0.0
    )

    # upper bound values
    max_params = TuningParams(
        e_value=800.0,
        k_factor=50.0,
        starting_elo=2000.0,
        home_adv=40.0,
        toss_adv=10.0,
        max_draw_chance=1.0
    )

    # create the tuple
    search_bounds: Tuple[TuningParams, TuningParams] = (min_params, max_params)

    tunedParams = optimiseParams(search_bounds, get_matches(date.min, date.max))
    print(tunedParams)



if __name__ == "__main__":
    tune()