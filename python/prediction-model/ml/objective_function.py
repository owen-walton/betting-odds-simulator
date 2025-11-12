import math
from model.match import Match, MarginType
from model.params import TuningParams
from typing import List, Dict, Tuple

def evaluate(params: TuningParams, matches: List[Match]) -> Tuple[float, Dict[int, float]]:
    team_ratings = {}  # team_id -> current ELO
    negative_log_loss = 0.0
    num_matches = 0

    for match in sorted(matches, key=lambda m: m.date):
        team_list = list(match.teams.keys())

        if len(team_list) != 2:
            continue

        # In cricket draw chance is a complex trend and unpredictable based on the data available
        # so in practice the betting shop will never allow bets on a draw,
        # meaning any draw is profitable to the company so no loss is added to the Negative Log Loss
        if match.winning_team_id is None:
            num_matches += 1
            continue

        team1_id, team2_id = team_list
        is_home1 = match.teams.get(team1_id, False)
        is_home2 = match.teams.get(team2_id, False)

        for team_id in (team1_id, team2_id):
            if team_id not in team_ratings:
                team_ratings[team_id] = 1500.0

        team1rating = team_ratings[team1_id]
        team2rating = team_ratings[team2_id]


        # Calculate expected win prob for team1
        win_prob = predict(team1rating, team2rating, params, is_home1=is_home1, is_home2=is_home2,
                           toss_winner_id=match.toss_winner_id, team1_id=team1_id, team2_id=team2_id)

        # Calculate log loss against actual result
        negative_log_loss += match_log_loss(match, team1_id, team2_id, win_prob)
        num_matches += 1

        # gain is added to team1 (if team2 has won the gain for team1 will be a negative value)
        if match.winning_team_id == team1_id:
            res = 1
        else:
            res = 0

        if match.margin_size is None or match.margin_size == 0:
            # match.margin_size of 0 is invalid so don't account for it
            gain = params.k_factor * (res - win_prob)
        elif match.margin_type == MarginType.RUNS:
            gain = params.runs_win_margin * match.margin_size * params.k_factor * (res - win_prob)
        elif match.margin_type == MarginType.WICKETS:
            gain = params.wickets_win_margin * match.margin_size * params.k_factor * (res - win_prob)
        elif match.margin_type == MarginType.ONE_INNINGS_AND_RUNS:
            gain = params.one_innings_margin_bonus + params.runs_win_margin * match.margin_size * params.k_factor * (res - win_prob)
        else:
            # if margin type is unknown then don't include it in calculation
            gain = params.k_factor * (res - win_prob)

        team_ratings[team1_id] = team_ratings[team1_id] + gain
        team_ratings[team2_id] = team_ratings[team2_id] - gain

    return negative_log_loss / max(1, num_matches), team_ratings

def predict(
    team1_elo: float,
    team2_elo: float,
    params: TuningParams,
    is_home1: bool = False,
    is_home2: bool = False,
    toss_winner_id: int = None,
    team1_id: int = None,
    team2_id: int = None,
) -> float:
    """
    Predict win probability for team1 given both teams' ELOs and match conditions.
    Returns a probability (0.0–1.0) that team1 wins.
    """

    # Apply home advantage
    if is_home1:
        team1_elo += params.home_adv
    if is_home2:
        team2_elo += params.home_adv

    # Apply toss advantage
    if toss_winner_id is not None and team1_id is not None and team2_id is not None:
        if toss_winner_id == team1_id:
            team1_elo += params.toss_adv
        elif toss_winner_id == team2_id:
            team2_elo += params.toss_adv

    # Compute expected probability
    return calculate_win_prob(team1_elo, team2_elo, params)

def calculate_win_prob(team_elo: float, opposition_elo: float, params: TuningParams) -> float:
    """
    Calculate probability team referenced by team_elo (not opposition_elo) wins the game.
    Doesn't account for draws
    based on ELO ratings and TuningParams.
    ELOs must be pre tuned before this function is called.
    """
    diff = team_elo - opposition_elo
    exponent = -diff / params.e_value

    # Clamp exponent to avoid overflow - this was encountered when the bayesian tested an extreme
    exponent = max(min(exponent, 50), -50)

    return 1 / (1 + 10 ** exponent)

def match_log_loss(match: Match, team1_id: int, team2_id: int, win_prob: float) -> float:
    """
    Calculates log loss for a single match based on win probability and the outcome of match.
    """
    epsilon = 1e-8


    if match.winning_team_id == team1_id:
        actual_prob = win_prob
    elif match.winning_team_id == team2_id:
        actual_prob = 1 - win_prob
    else:
        # unexpected team ID, treat as unknown; assign log loss 0
        return 0.0

    # log loss = -log(predicted probability of actual outcome)
    log_loss = -math.log(max(actual_prob, epsilon))
    return log_loss

def evaluate_rand_log_loss(matches: List[Match]) -> float:
    """
    Baseline evaluation: calculates the average log loss assuming
    a random 50/50 prediction between the two teams (no draw bets).

    Draws are treated as profitable for the house (no loss added, but still counted).
    """
    negative_log_loss = 0.0
    num_matches = 0
    random_prob = 0.5  # constant 50/50 probability

    for match in sorted(matches, key=lambda m: m.date):
        team_list = list(match.teams.keys())

        if len(team_list) != 2:
            continue

        # Treat draws as house wins — no loss added, still counted
        if match.winning_team_id is None:
            num_matches += 1
            continue

        team1_id, team2_id = team_list

        # Use the existing log loss helper for consistency
        negative_log_loss += match_log_loss(match, team1_id, team2_id, random_prob)
        num_matches += 1

    avg_log_loss = negative_log_loss / max(1, num_matches)
    return avg_log_loss