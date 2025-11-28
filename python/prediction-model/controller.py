"""
Each function in controller class represents a branch of the program, menus in main determine which to run
"""

from ml.bayesian_opt import *
from ml.objective_function import *
from db.parameters_repository import *
from db.match_repository import *
from db.team_repository import *
from typing import Tuple, Dict
from model.params import TuningParams
from datetime import date
import math
from fractions import Fraction

class Controller:

    MARKET_OVERROUND = 0.12

    def tune(self, rand_state: Optional[int] = None, end_of_training_date: Optional[date] = None) -> Tuple[Dict[str, TuningParams], Dict[str, float]]:
        """
        Core tuning logic used by both official_tune() and test_tune().

        If end_of_training_date is None, run the tune on entire set,
        if it has a date, tune based on matches BEFORE that date and
        then run log loss calculations based off of matches AFTER that date

        Returns:
            (params_by_format, log_loss_by_format)
        """
        # search parameters
        min_params = TuningParams(
            e_value=200.0,
            k_factor=0.1,
            home_adv=10.0,
            toss_adv=0.0,
            runs_win_margin=0.0,
            wickets_win_margin=0.0,
            one_innings_margin_bonus=0.0
        )

        max_params = TuningParams(
            e_value=1200.0,
            k_factor=40.0,
            home_adv=150.0,
            toss_adv=60.0,
            runs_win_margin=3.0,
            wickets_win_margin=6.0,
            one_innings_margin_bonus=100.0
        )

        search_bounds: Tuple[TuningParams, TuningParams] = (min_params, max_params)

        formats = ["T20", "ODI", "Test"]
        params_by_format: Dict[str, TuningParams] = {}
        log_loss_by_format: Dict[str, float] = {}

        # Run tuning per format
        for match_format in formats:
            # if not required to train and test on separate matches
            if end_of_training_date is None:
                matches = get_matches(date.min, date.max, match_format)
                best_params, best_log_loss = optimise_params(
                    search_bounds=search_bounds,
                    matches=matches,
                    random_state=rand_state
                )
                params_by_format[match_format] = best_params
                log_loss_by_format[match_format] = best_log_loss

            else:
                # Training matches only
                train_matches = get_matches(date.min, end_of_training_date, match_format)

                best_params, _ = optimise_params(
                    search_bounds=search_bounds,
                    matches=train_matches,
                    random_state=rand_state
                )
                params_by_format[match_format] = best_params

                # Test matches (after end_of_training_date)
                test_matches = get_matches(end_of_training_date, date.max, match_format)

                if test_matches:
                    test_loss, _ = evaluate(best_params, test_matches)
                else:
                    test_loss = float("nan")

                log_loss_by_format[match_format] = test_loss

        return params_by_format, log_loss_by_format

    def official_tune(self):
        """
        Runs tuning, updates ELOs and tuning parameters in the DB.
        """
        params_by_format, log_loss_by_format = self.tune()

        # Update ELOs and store tuned params
        for match_format, params in params_by_format.items():
            matches = get_matches(date.min, date.max, match_format)
            _, final_elo_dict = evaluate(params, matches)
            update_elos(match_format, final_elo_dict)

        insert_tuning_params(params_by_format)

    def test_tune(self, rand_state) -> Dict[str, float]:
        """
        Runs tuning but does not write to DB.
        Returns a dictionary of log losses for each format.
        """
        _, log_loss_by_format = self.tune(rand_state=rand_state)
        return log_loss_by_format

    def test_elo_avg_loss(self) -> Dict[str, float]:
        """
        Evaluate average ELO log loss per match format using a fixed parameter baseline.
        Returns a dict of format -> average log loss.
        """
        formats = ["T20", "ODI", "Test"]
        results: Dict[str, float] = {}

        base_params = TuningParams(
            k_factor=20.0,
            home_adv=0.0,
            toss_adv=0.0,
            e_value=400.0,
            one_innings_margin_bonus=0.0,
            runs_win_margin=0.0,
            wickets_win_margin=0.0
        )

        for match_format in formats:
            matches = get_matches(date.min, date.max, match_format)
            avg_loss, _ = evaluate(base_params, matches)
            results[match_format] = avg_loss

        return results

    def test_rand_avg_loss(self) -> Dict[str, float]:
        """
        Evaluate random (50/50) average log loss per match format.
        Returns a dict of format -> average random baseline log loss.
        """
        formats = ["T20", "ODI", "Test"]
        results: Dict[str, float] = {}

        for match_format in formats:
            matches = get_matches(date.min, date.max, match_format)
            results[match_format] = evaluate_rand_log_loss(matches)

        return results

    # returns odds for team1
    def predict_match(self, format, team1, team2, is_team1_home, is_team2_home) -> str:

        # convert team names to IDs
        team1_id = get_team_id_by_name(team1)
        team2_id = get_team_id_by_name(team2)

        # get team ELOs for this format
        elos = get_elos(format)
        team1_elo = elos.get(team1_id, 1500.0)
        team2_elo = elos.get(team2_id, 1500.0)

        # get tuning parameters for this format
        params_by_format = get_latest_tuning_params()
        params = params_by_format.get(format)

        # calculate predicted probability for team1
        win_prob = predict(
            team1_elo=team1_elo,
            team2_elo=team2_elo,
            params=params,
            is_home1=is_team1_home,
            is_home2=is_team2_home,
            toss_winner_id=None,
            team1_id=team1_id,
            team2_id=team2_id
        )

        print(f"{team1}: {win_prob * 100:.1f}%")
        print(f"{team2}: {(1 - win_prob) * 100:.1f}%")
        return self.win_prob_to_fractional_odds(win_prob)


    def win_prob_to_fractional_odds(self, win_prob: float) -> str:
        """
        Converts a win probability (0–1) to fractional odds (e.g. '3/1'),
        applying a 12% market overround.
        """

        # Prevent division by zero
        win_prob = max(min(win_prob, 0.9999), 0.0001)

        # Apply overround — inflate probabilities by (1 + overround)
        adjusted_prob = win_prob * (1 + self.MARKET_OVERROUND)

        # Cap at 0.999 to avoid division errors
        adjusted_prob = min(adjusted_prob, 0.999)

        # Convert to decimal odds
        decimal_odds = 1 / adjusted_prob

        # Convert to fractional odds
        fractional_value = decimal_odds - 1

        # Simplify fraction — limit denominator for clean representation
        frac = Fraction(fractional_value).limit_denominator(20)

        return f"{frac.numerator}/{frac.denominator}"