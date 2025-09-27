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

        team1_id, team2_id = team_list
        is_home1 = match.teams.get(team1_id, False)
        is_home2 = match.teams.get(team2_id, False)

        for team_id in (team1_id, team2_id):
            if team_id not in team_ratings:
                team_ratings[team_id] = params.starting_elo

        team1rating = team_ratings[team1_id]
        team2rating = team_ratings[team2_id]

        # Apply home advantage (additive)
        if is_home1:
            team1rating += params.home_adv
        if is_home2:
            team2rating += params.home_adv

        # Apply toss advantage (additive)
        if match.toss_winner_id is not None:
            if match.toss_winner_id == team1_id:
                team1rating += params.toss_adv
            elif match.toss_winner_id == team2_id:
                team2rating += params.toss_adv

        # Calculate expected probability for each result type w/d/l
        win_prob, draw_prob, loss_prob = calculate_result_prob(team1rating, team2rating, params)

        # Calculate log loss against actual result
        negative_log_loss += match_log_loss(match, team1_id, team2_id, (win_prob, draw_prob, loss_prob))
        num_matches += 1

        # gain is added to team1 (if team2 wins the gain will be a negative value)
        if match.winning_team_id is not None:
            if match.winning_team_id == team1_id:
                res = 1
            else:
                res = 0
        else:
            res = 0.5

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

        gain = params.k_factor * (res - win_prob)
        team_ratings[team1_id] = team_ratings[team1_id] + gain
        team_ratings[team2_id] = team_ratings[team2_id] - gain

    print(negative_log_loss / max(1, num_matches))
    return negative_log_loss / max(1, num_matches), team_ratings

def calculate_result_prob(team_elo: float, opposition_elo: float, params: TuningParams) -> Tuple[float, float, float]:
    """
    Calculate probabilities for:
    - team win
    - draw
    - opposition win
    based on ELO ratings and TuningParams.
    ELOs must be pre tuned before this function is called.
    """
    diff = team_elo - opposition_elo

    # expected win probability without draw
    e_win = 1 / (1 + 10 ** (-diff / params.e_value))

    # draw probability increases when ratings are close
    e_draw = params.max_draw_chance * max(0.0, 1 - abs(diff) / params.e_value)
    e_draw = min(e_draw, params.max_draw_chance)

    # adjust win/loss probabilities to account for draw chance so sum = 1
    e_win_adj = e_win * (1 - e_draw)
    e_loss_adj = 1 - e_win_adj - e_draw

    # ensure sum = 1 (avoids rounding issues)
    total = e_win_adj + e_draw + e_loss_adj
    e_win_adj, e_draw_adj, e_loss_adj = (e_win_adj / total, e_draw / total, e_loss_adj / total)

    return e_win_adj, e_draw_adj, e_loss_adj


import math


def match_log_loss(match: Match, team1_id: int, team2_id: int, result: tuple) -> float:
    """
    Calculates log loss for a single match based on predicted probabilities
    and actual outcome stored in Match.
    """
    epsilon = 1e-8
    win_prob, draw_prob, loss_prob = result

    if match.winning_team_id is None:
        actual_prob = draw_prob
    elif match.winning_team_id == team1_id:
        actual_prob = win_prob
    elif match.winning_team_id == team2_id:
        actual_prob = loss_prob
    else:
        # unexpected team ID, treat as unknown; assign log loss 0
        return 0.0

    # log loss = -log(predicted probability of actual outcome)
    log_loss = -math.log(max(actual_prob, epsilon))
    return log_loss
