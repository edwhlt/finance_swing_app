package fr.hedwin;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;

public class Test {

    public static void main(String[] args) {
        List<String> fruits = new ArrayList<>();
        fruits.add("Apple");
        fruits.add("Banana");
        fruits.add("Orange");

        ListIterator<String> iterator = fruits.listIterator();

        Scanner scanner = new Scanner(System.in);
        String input;

        int current = iterator.nextIndex();
        while (true) {
            System.out.println("Current element: "+fruits.get(current)+" - "+current);

            System.out.print("Enter 'n' for next, 'p' for previous, or 'q' to quit: ");
            input = scanner.nextLine();

            if (input.equalsIgnoreCase("n")) {
                if (iterator.hasNext()) {
                    System.out.println("Before next: "+current);
                    int n = iterator.nextIndex();
                    current = n;
                    System.out.println("After next: "+n);
                } else {
                    System.out.println("End of the list reached.");
                }
            } else if (input.equalsIgnoreCase("p")) {
                if (iterator.hasPrevious()) {
                    System.out.println("Before previous: "+current);
                    int p = iterator.previousIndex();
                    current = p;
                    System.out.println("After previous: "+p);
                } else {
                    System.out.println("Beginning of the list reached.");
                }
            } else if (input.equalsIgnoreCase("q")) {
                break;
            } else {
                System.out.println("Invalid command.");
            }
        }

        System.out.println("Program terminated.");
    }

}
