package ui;

import model.User;
import java.util.Scanner;

/** Role-specific landing menu after successful authentication. */
public class DashboardMenu {
    private final Scanner scanner;
    private final User user;
    private final String role;

    public DashboardMenu(Scanner scanner, User user) {
        this(scanner, user, user.roles());
    }

    public DashboardMenu(Scanner scanner, User user, String role) {
        this.scanner = scanner;
        this.user = user;
        this.role = role == null ? "USER" : role;
    }

    public void show() {
        System.out.println("\\nWelcome to JetMotoHub, " + user.name() + "!");
        System.out.println("Role: " + role + " | Status: " + user.status());
        if ("PENDING".equalsIgnoreCase(user.status())) {
            System.out.println("Your account is still pending approval.");
        }

        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\\n===== " + role + " DASHBOARD =====");
            switch (role) {
                case "ADMIN" -> System.out.println("Administrator menu: manage the shop and oversee accounts.");
                case "STAFF" -> System.out.println("Staff menu: assist customers and check the parts catalog.");
                case "CUSTOMER" -> System.out.println("Customer menu: browse the motorcycle parts catalog.");
                default -> System.out.println("User menu");
            }
            System.out.println("1. Search Parts Catalog");
            System.out.println("2. Logout");
            System.out.print("Choose: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> new SearchPartsScreen(scanner).show();
                case "2" -> {
                    System.out.println("Logged out.");
                    loggedIn = false;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }
}
