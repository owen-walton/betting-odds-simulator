package com.betwise.oddscalc.main;

import java.util.Scanner;

public class InputHelper {
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
