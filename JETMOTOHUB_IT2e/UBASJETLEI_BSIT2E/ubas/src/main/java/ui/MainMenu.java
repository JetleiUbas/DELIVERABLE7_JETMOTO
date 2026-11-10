package ui;

import dao.UserDao;

import java.util.Scanner;

public class MainMenu {

    private final Scanner scanner = new Scanner(System.in);
    private final UserDao userDao = new UserDao();

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== JETMOTOHUB MAIN MENU ===");
            System.out.println("1. Register Account");
            System.out.println("2. Login");
            System.out.println("3. Search Parts Catalog");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> new RegisterScreen(scanner, userDao).show();
                case "2" -> new LoginScreen(scanner, userDao).show();
                case "3" -> new SearchPartsScreen(scanner).show();
                case "4" -> {
                    System.out.println("Thank you for using JetMotoHub. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }
}
