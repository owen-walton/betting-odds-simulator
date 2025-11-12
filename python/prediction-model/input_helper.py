from datetime import date

class InputHelper:
    def input_letter_multiple_choice(self, num_of_options: int, prompt: str) -> str:
        """
        Prompts the user for a single-letter multiple-choice answer.
        Accepts letters 'A'...'Z' based on num_of_options.
        """
        while True:
            sz_out = input(prompt).strip()

            if len(sz_out) == 1:
                letter = sz_out.lower()
                valid_letters = [chr(i + 97) for i in range(num_of_options)]  # ['a', 'b', 'c', ...]

                if letter in valid_letters:
                    return letter.upper()

            print("Please enter a valid letter.")

    def input_string_or_nothing(self, prompt: str) -> str:
        """
        Prompts the user for a string (can be empty) but disallows semicolons
        to help prevent SQL injection risks.
        """
        while True:
            user_input = input(prompt)

            if ";" not in user_input:
                return user_input
            else:
                print("Please enter a valid string (no semicolons).")

    def input_date(self, lower_bound_incl: date, upper_bound_incl: date, prompt: str) -> date:
        """Prompts for a date and ensures it's within inclusive bounds."""
        while True:
            print(prompt)
            print("Enter in the form: DD/MM/YYYY")

            day = self.input_integer(1, 31, "Enter DD: ")
            month = self.input_integer(1, 12, "Enter MM: ")
            year = self.input_integer(1, 4000, "Enter YYYY: ")

            try:
                input_date = date(year, month, day)

                if input_date < lower_bound_incl or input_date > upper_bound_incl:
                    print(f"The date must be between {lower_bound_incl} and {upper_bound_incl}")
                else:
                    return input_date

            except ValueError:
                print("The date you entered is invalid.")