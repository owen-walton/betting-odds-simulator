from typing    import Tuple, List, Optional
from bayes_opt  import BayesianOptimization

from model.params import TuningParams
from model.match  import Match
from ml.objective_function import evaluate


def optimiseParams(
    search_bounds: Tuple[TuningParams, TuningParams],
    matches: List[Match],
    init_points: int = 20,
    n_iter:      int = 200,
    random_state: Optional[int] = None
) -> TuningParams:
    """
    TODO May need batching in future
    searchBounds: (minParams, maxParams)
    matches: matches data
    init_points: number of random explorations before bayesia opt begins
    n_iter: number of Bayesian–Optimization iterations
    random_state: seed for random reproducibility

    Returns TuningParams instance with best factors found
    """
    min_params, max_params = search_bounds

    # build pbounds
    pbounds = {
        "e_value":         (min_params.e_value,        max_params.e_value),
        "k_factor":        (min_params.k_factor,       max_params.k_factor),
        "starting_elo":    (min_params.starting_elo,   max_params.starting_elo),
        "home_adv":        (min_params.home_adv,       max_params.home_adv),
        "toss_adv":        (min_params.toss_adv,       max_params.toss_adv),
        "max_draw_chance": (min_params.max_draw_chance,max_params.max_draw_chance),
    }

    # wrap evaluate() so that a higher score is better (as required by bayes opt library)
    def bayes_evaluate(**kwargs) -> float:
        tp = TuningParams(**kwargs)
        return -evaluate(tp, matches)

    # create the optimiser
    optimizer = BayesianOptimization(
        f=bayes_evaluate,
        pbounds=pbounds,
        verbose=2,
        random_state=random_state
    )

    # run optimisation
    optimizer.maximize(init_points=init_points, n_iter=n_iter)

    # return best parameters
    best_params_dict = optimizer.max["params"]
    return TuningParams(**best_params_dict)
