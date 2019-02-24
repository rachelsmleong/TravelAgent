/**
 * Execution: Front-end class
 */

import java.io.IOException;
import java.util.Scanner;

public class StartHere {
    private static String API_KEY = ""; // insert API_KEY here

    private static databaseManager database;
    private static int databaseSize;
    private static LinkedList<Point> databaseTable;
    private static LinkedList<userSelected> userOutput;
    private static String databaseLocation = "";

    public static void main(String args[]) throws IOException {
        /* read file  */
        if (args.length != 1) {
            System.out.println("No file attached, please start program with a database file.");
        } else {
            databaseLocation = args[0];
            database = new databaseManager(databaseLocation, API_KEY);

            databaseTable = database.getFromFile();
            databaseSize = databaseTable.size();
            userOutput = new LinkedList<>();

            System.out.println("File populated and proceed to start program...  \n");
            terminal();

        }
    }

    /**
     * This is the beginning of the program
     * <p>
     * When user enters 1, starts the customer terminal
     * WHen user enters 2, it starts the staff terminal
     *
     * @throws IOException
     */
    private static void terminal() throws IOException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("How can I help you today? \n [1] - Plan an itinerary  \n [2] - I'm a staff \n [3] - Enter '3' at any point to exit \n Please enter a choice: ");
            int choice = scanner.nextInt();

            if (choice == 1) {
                clearScreen();
                customerTerminal();
                break;

            } else if (choice == 2) {
                clearScreen();
                staffTerminal();
                break;
            } else if (choice == 3) {
                System.exit(0);
            } else {
                clearScreen();
                System.out.println("Please input a valid number");
                terminal();
            }

        }

    }

    /**
     * Staff terminal access
     *
     * @throws IOException
     */
    private static void staffTerminal() throws IOException {
        boolean editedDatabase = false;
        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("What do you want to do? " +
                    "\n [1] - Add Locations  " +
                    "\n [2] - Edit Locations" +
                    "\n [3] - Delete Locations" +
                    "\n [4] - Exit staff " +
                    "\nPlease enter a choice: ");
            int choice = scanner.nextInt();
            if (choice == 1) {
                addLocation();
                editedDatabase = true;
            } else if (choice == 2) {
                editLocation();
                editedDatabase = true;
            } else if (choice == 3) {
                deleteLocation();
                editedDatabase = true;
            } else if (choice == 4) {

                if (editedDatabase) {
                    database.updateDatabase(databaseLocation);
                    database = new databaseManager(databaseLocation, API_KEY);
                    databaseTable = database.getFromFile();
                }
                clearScreen();
                terminal();
                break;
            } else {
                clearScreen();
                System.out.println("Please Enter a valid number");
                staffTerminal();
            }
        }

    }


    public static void addLocation() throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Name of Location: ");
        String name = scanner.nextLine();
        System.out.println("Checking location validity with Google servers....");
        fetchGPS fetch = new fetchGPS(name, API_KEY);
        System.out.println("Time to spend there: ");
        double time = dayToSec(scanner.nextDouble());
        System.out.println("Budget customer would need to visit there: ");
        double budget = scanner.nextDouble();
        Point newPoint = new Point(fetch.getLat(), fetch.getLng(), name, time, budget);
        databaseTable.add(1, newPoint);
        System.out.println("\n Location Added \n \n");
    }


    private static void editLocation() {
        Scanner scanner = new Scanner(System.in);
        for (int i = 0; i < databaseTable.size() - 1; i++)
            System.out.println("[" + i + "] - " + databaseTable.get(i).getName());
        System.out.print(" \n Please enter index of location to edit: ");

        int choice = scanner.nextInt();


        if (choice < databaseSize && choice >= 0) {
            Point edit = databaseTable.get(choice);
            System.out.println("Location selected: " + edit.getName());
            System.out.println("Current time in database: " 
                                   + secToDay(edit.getTimeToSpend()) + " day(s)");
            System.out.println("Edit time to spend there: ");
            double time = dayToSec(scanner.nextDouble());
            System.out.println("Current budget in database: " + edit.getBudget() 
                                   + " USD");
            System.out.println("Edit budget customer would need to visit there: ");
            double budget = scanner.nextDouble();
            edit.setBudget(budget);
            edit.setTimeToSpend(time);
            databaseTable.set(choice, edit);
            System.out.println("\n Location Edited \n \n");
        } else {
            System.out.println("Please enter a valid location");
        }

    }

    private static void deleteLocation() {
        Scanner scanner = new Scanner(System.in);
        if (databaseTable.size() == 1) {
            System.out.println("\n \n ERROR ! DATABASE IS EMPTY\n \n");
        } else {
            for (int i = 0; i < databaseTable.size() - 1; i++)
                System.out.println("[" + i + "] - " + databaseTable.get(i).getName());
            System.out.print(" \n Please enter index of location to delete: ");

            int choice = scanner.nextInt();
            if (choice < databaseSize && choice >= 0) {
                databaseTable.remove(choice);
                System.out.println("\n Location Deleted \n \n");
            } else {
                System.out.println("Please enter a valid location");
            }
        }
    }

    /**
     * Customer terminal access
     */
    private static void customerTerminal() {
        double timeToSpend;
        double budget;
        int startingLocation = 0;
        boolean definedOrigin = false;
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the trip planner! \n We hope to help you plan for your holiday! We will start by asking you a few questions and the system will plan the best itinerary for you ! ");

        while (true) {
            System.out.print("Do you have an idea on where do you want to start your trip? \n [1] - Yes!  \n [2] - No \n Please enter a choice: ");
            int choice = scanner.nextInt();
            if (choice == 1) {
                definedOrigin = true;
                break;
            } else if (choice == 2) {
                clearScreen();
                break;
            } else if (choice == 3) {
                System.exit(0);
            }
            System.out.println("Please input a valid number");
        }


        while (definedOrigin) {
            System.out.print("Where do you want to start your trip? \n \n Please enter a choice: ");
            for (int i = 0; i < databaseSize - 1; i++)
                System.out.print("\n[" + i + "] - " + databaseTable.get(i).getName());
            System.out.print(" \n Please enter index of location: ");

            int choice = scanner.nextInt();
            if (choice < databaseSize -1 && choice >= 0) {
                startingLocation = choice;
                break;
            }
            System.out.println("Please input a valid number");
        }


        while (true) {
            if (!definedOrigin) startingLocation = (int) (Math.random() * 
                                                          databaseTable.size() - 1);
            System.out.print("\nHow much time do you have?\n Number of days:  ");
            double time = scanner.nextDouble();
            if (time > 0) {
                timeToSpend = dayToSec(time);
                break;
            }
            System.out.println("Please input a valid number");
        }
        while (true) {
            System.out.print("\nHow much budget do you have?\n In USD ");
            int choice = scanner.nextInt();
            if (choice > 0) {
                budget = choice;
                break;
            }
            System.out.println("Please input a valid number");
        }

        System.out.print("\nWait while the system calculates the best route for you... \n");


        runCalculations(startingLocation, budget, timeToSpend);
        finalOutput();
    }

    /**
     * Runs calculations on the best route given the starting location, budget and timeToSpend
     */
    private static void runCalculations(int startingLocation, double budget, double timeToSpend) {
        double budgetCounter = 0;
        double daysCounter = 0;
        int startIndex = startingLocation;

        while (budgetCounter <= budget && daysCounter <= timeToSpend) {
            if (startIndex == databaseTable.size() - 1) startIndex = 0;

            userOutput.add(new userSelected(
                    databaseTable.get(startIndex).getName(),
                    databaseTable.get(startIndex).getTimeToSpend(),
                    databaseTable.get(startIndex).getBudget(),
                    databaseTable.get(startIndex).getDistanceToNext(),
                    databaseTable.get(startIndex).getDurationToNext()));


            budgetCounter = budgetCounter + databaseTable.get(startIndex).getBudget();
            daysCounter = daysCounter + databaseTable.get(startIndex).getDurationToNext() 
                + databaseTable.get(startIndex).getTimeToSpend();
            startIndex++;

        }
        userOutput.remove(userOutput.size() - 1);

    }

    /**
     * Prints the determined route customer is travelling.
     */
    private static void finalOutput() {
        double totalDuration = 0;
        double totalBudget = 0;
        double distanceTravelled = 0;

        System.out.println("Here is your itinerary: \n ");
        for (int i = 0; i < userOutput.size(); i++) {

            userSelected out = userOutput.get(i);
            System.out.println(out.getName() + "   <Time to spend here: " +
                    secToDay(out.getDuration()) + " day(s)  Budget for this location: " +
                    out.getBudget() + ">");

            totalDuration = totalDuration + out.getDuration() + out.getTimeToNext();
            totalBudget = totalBudget + out.getBudget();
            distanceTravelled = distanceTravelled + out.getDistanceToNext();
        }
        System.out.println("\n \nTotal budget: " + totalBudget + " USD \nTotal Duration: " 
                               + secToDay(totalDuration) + " day(s) \nDistance travelled by driving: " 
                               + meterToKm(distanceTravelled) + " KM");
    }

    /**
     * Generates empty lines to give an illusion of a "new screen". 
     * Could use clear for mac/linux or cls on windows
     */
    private static void clearScreen() {
        for (int i = 0; i < 50; i++) System.out.println();
    }

    private static double secToDay(double sec) {
        return sec / (24 * 3600);
    }

    private static double dayToSec(double day) {
        return day * 24 * 3600;
    }

    private static double meterToKm(double meter) {
        return meter / 1000;
    }

}