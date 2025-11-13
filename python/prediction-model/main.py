from controller import *
from input_helper import *

"""
main.py is an entry point for the program and holds no logic
Each method supplied by controller.py is a different run case:
- tune(): updates the tuning model in the database
"""
if __name__ == "__main__":
    controller = Controller()
    input_helper = InputHelper()

    # --- Role Selection ---
    role = input_helper.input_letter_multiple_choice(2, """
    ===== LOGIN =====
    Are you:
    A) Owner
    B) Customer Assistant
    """)

    # --- Owner Menu ---
    if role == "A":
        owner_choice = input_helper.input_letter_multiple_choice(3, """
    ===== OWNER MENU =====
    What would you like to do?
    A) Update prediction model
    B) Run test
    C) Quit
    """)

        if owner_choice == "A":
            print("\nUpdating prediction model...")
            controller.official_tune()
            print("\n-------Prediction model updated-------")

        elif owner_choice == "B":
            print("Running test against ELO formula (control test)")
            elo_loss = controller.test_elo_avg_loss()
            print("Running test with random guessing (control test)")
            rand_loss = controller.test_rand_avg_loss()
            print("Running test against the Main Prediction Model")
            model_loss = controller.test_tune(2)

            print("\n================= MODEL PERFORMANCE SUMMARY =================")
            print("------- Evaluation metric: Log loss (lower is better) -------\n")
            print(f"{'Format':<10} | {'ELO Baseline':>15} | {'Random 50/50':>15} | {'Tuned Model':>15}")
            print("-" * 65)

            # all_formats = sorted(set(elo_loss.keys()) | set(rand_loss.keys()) | set(model_loss.keys()))
            all_formats = ["T20", "ODI", "Test"]
            for fmt in all_formats:
                elo = elo_loss.get(fmt, float('nan'))
                rand = rand_loss.get(fmt, float('nan'))
                model = model_loss.get(fmt, float('nan'))
                print(f"{fmt:<10} | {elo:>15.6f} | {rand:>15.6f} | {model:>15.6f}")

            print("-" * 65)

            avg_elo = sum(elo_loss.values()) / len(elo_loss)
            avg_rand = sum(rand_loss.values()) / len(rand_loss)
            avg_model = sum(model_loss.values()) / len(model_loss)
            print(f"{'AVERAGE':<10} | {avg_elo:>15.6f} | {avg_rand:>15.6f} | {avg_model:>15.6f}")
            print("==============================================================\n")


        elif owner_choice == "C":
            print("\n-------Program terminated-------")

    # --- Customer Assistant Menu ---
    elif role == "B":
        print("\n\t===== MATCH PREDICTION =====")

        # Ask for format
        format_choice = input_helper.input_letter_multiple_choice(3, """
    Which match format is this?
    A) Test
    B) ODI
    C) T20
    """)

        if format_choice == "A":
            format_name = "Test"
        elif format_choice == "B":
            format_name = "ODI"
        elif format_choice == "C":
            format_name = "T20"
        else:
            print("Invalid format choice.")
            exit(1)

        # --- Team 1 ---
        while True:
            team1_name = input_helper.input_string_or_nothing("\nEnter the team you want to bet on's name (Team 1): ").strip()
            confirm = input_helper.input_letter_multiple_choice(2,
                                                                f"Confirm Team 1 is '{team1_name}'?\nA) Yes\nB) No\n")
            if confirm == "A":
                break

        # --- Team 2 ---
        while True:
            team2_name = input_helper.input_string_or_nothing("\nEnter Team 2 name: ").strip()
            confirm = input_helper.input_letter_multiple_choice(2,
                                                                f"Confirm Team 2 is '{team2_name}'?\nA) Yes\nB) No\n")
            if confirm == "A":
                break

        # --- Home status ---
        home1_choice = input_helper.input_letter_multiple_choice(2,
                                                                 f"Is {team1_name} playing at home?\nA) Yes\nB) No\n")
        is_team1_home = home1_choice == "A"

        home2_choice = input_helper.input_letter_multiple_choice(2,
                                                                 f"Is {team2_name} playing at home?\nA) Yes\nB) No\n")
        is_team2_home = home2_choice == "A"

        # --- Predict ---
        print("\nCalculating odds...")

        try:
            odds_str = controller.predict_match(format_name, team1_name, team2_name, is_team1_home, is_team2_home)
            print(f"\nPredicted odds for {team1_name}: {odds_str}")
        except Exception as e:
            print(f"\nAn error occurred while predicting: {e}")

        print("\n-------Prediction complete-------")
    else: # will not get reached
        print("\nUnexpected input.")