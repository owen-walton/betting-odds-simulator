/**
 * @author Owen Walton
 * @aim Entry point for the program, handles the menu and input
 * - uses InputHelper.java to handle the validation and Scanner logic
 * - Stores no back-end logic:
 *    - only input and output to determine what branch of the program to tell Controller to run
 */

package com.betwise.oddscalc.main;

import com.betwise.oddscalc.controller.Controller;

import java.io.IOException;

public class MainApp {

    public static void main(String[] args) throws IOException {
        Controller controller = new Controller();
        InputHelper inputHelper = new InputHelper();

        String input = inputHelper.inputLetterMultipleChoice(3, """
                ===== MAIN MENU =====
                What would you like to do?
                A) Initialise program
                B) Update match dataset
                C) Quit
                """);
        switch (input) {
            case "A":
                String check = inputHelper.inputStringOrNothing("""
                        \n4 Warning(s):
                        - This command will reset the system
                        - This command should ONLY be ran with an up to date cricsheet.zip file in resources.
                        - Please ensure the MySQL system has an existing database named CricketMatchData (The program can only overwrite- not create a DB)
                        - Please ensure the fields in DBConfig.properties are correct
                        
                        To proceed, please type \"INITIALISE\" in upper case. Any other input will terminate the program.""");
                if (check.equals("INITIALISE")) {
                    controller.init();
                    System.out.println("\n-------Initialisation complete-------");
                }
                break;
            case "B":
                controller.maintain();
                System.out.println("\n-------Match dataset has been updated-------");
                break;
            case "C":
                System.out.println("\n-------Program terminated-------");
                break;
            default:
                System.err.println("Unexpected input");
                break;
        }
    }
}
