package com.cmrl.utils;


/**
 * Utility class for displaying console-based menus in a formatted way.
 * <p>
 * Provides a reusable method to print menus with titles and selectable options.
 * </p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>
 *     MenuPrinter.printMenu("Main Menu",
 *             "Login",
 *             "Signup",
 *             "Exit");
 * </pre>
 * <p>Will produce:</p>
 * <pre>
 * ===== Main Menu =====
 * 1. Login
 * 2. Signup
 * 3. Exit
 * -------------------------
 *
 * 🎯 Make your selection 👉 :
 * </pre>
 *
 * @author Ezhil
 */
public class MenuPrinter {

    private MenuPrinter(){}

    /**
     * Prints a formatted menu with the given title and options.
     * <p>
     * Each option is numbered automatically starting from 1.
     * </p>
     *
     * @param title   the title of the menu to display.
     * @param options the list of options to display as selectable items.
     */
    public static void printMenu(String title, String... options) {
        System.out.println("\n===== " + title + " =====");
        for (int i = 0; i < options.length; i++) {
            System.out.println((i + 1) + ". " + options[i]);
        }
        System.out.println("-------------------------");
        System.out.print("\n🎯 Make your selection 👉 : ");
    }
}
