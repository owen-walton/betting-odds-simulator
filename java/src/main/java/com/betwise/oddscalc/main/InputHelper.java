/**
 * @author Owen Walton
 * @aim a helper class for Main.java that separates the validation and Scanner logic from prompt/response
 */

package com.betwise.oddscalc.main;

import java.util.Scanner;

public class InputHelper {
    /**
     * Allows user to input a string prompt that will have an A,B,C,... menu, and the corresponding number of options
     * The input can be either upper or lower case but must be a valid letter (2 options means only A and B accepted)
     * Null safe
     * @return The upper case version of the letter they entered
      */
    public String inputLetterMultipleChoice(int numOfOptions, String prompt)
    {
        Scanner sc = new Scanner(System.in);
        String szOut;

        while(true)
        {
            System.out.print(prompt);
            szOut = sc.nextLine();

            if (szOut.length() == 1)
            {
                for (int i = 0; i < numOfOptions; i++)
                {
                    if(szOut.toLowerCase().charAt(0) == (char)(i + 97))
                    {
                        return szOut.toUpperCase();
                    }
                }
            }

            System.out.println("Please enter a valid letter.");
        }

    }

    /**
     * Ask prompt and take in a string, this string can be empty,
     * ";" is never necessary for input, so as a basic SQL precaution anything with ";" is not accepted (asked again)
     * @return The string response
     */
    public String inputStringOrNothing(String prompt)
    {
        Scanner sc = new Scanner(System.in);
        String input;

        while (true)
        {
            System.out.print(prompt);

            input = sc.nextLine();

            // check that there is no semicolon for sql injection precaution
            if (input != null && !input.contains(";"))
            {
                return input; // Return the valid input
            }
            else
            {
                System.out.println("Please enter a valid string.");
            }
        }
    }
}
