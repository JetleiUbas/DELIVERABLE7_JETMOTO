package ui;

import dao.UserDao;
import model.User;

import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

/** Owns registration, login, and role-based navigation for the CLI. */
public final class Authentication {
    private final Scanner scanner;
    private final UserDao userDao;

    public Authentication(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\\n=== JETMOTOHUB MAIN MENU ===");
            System.out.println("1. Register Account");
            System.out.println("2. Login");
            System.out.println("3. Search Parts Catalog");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> register();
                case "2" -> login();
                case "3" -> new SearchPartsScreen(scanner).show();
                case "4" -> {
                    System.out.println("Thank you for using JetMotoHub. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void register() {
        System.out.println("\\n--- REGISTER ---");
        System.out.print("Full name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }
        if (userDao.usernameExists(username)) {
            System.out.println("Username already taken.");
            return;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine();
        if (password.isBlank()) {
            System.out.println("Password cannot be empty.");
            return;
        }

        String role = chooseRole();
        if (role == null) return;

        if (userDao.register(name, username, password, role)) {
            System.out.println("Registered as " + role + ". Your status is PENDING.");
        } else {
            System.out.println("Registration failed. Please try again.");
        }
    }

    private String chooseRole() {
        while (true) {
            System.out.println("Select role: 1. ADMIN  2. STAFF  3. CUSTOMER");
            System.out.print("Choose: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> { return "ADMIN"; }
                case "2" -> { return "STAFF"; }
                case "3" -> { return "CUSTOMER"; }
                case "0" -> { return null; }
                default -> System.out.println("Invalid role. Choose 1, 2, 3, or 0 to cancel.");
            }
        }
    }

    private void login() {
        System.out.println("\\n--- LOGIN ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        Optional<User> result = userDao.login(username, password);
        if (result.isEmpty()) {
            System.out.println("Invalid username or password.");
            return;
        }

        User user = result.get();
        String role = user.roles() == null ? "" : user.roles().toUpperCase(Locale.ROOT);
        switch (role) {
            case "ADMIN" -> new AdminMenu(scanner, userDao, user).show();
            case "STAFF" -> new DashboardMenu(scanner, user, "STAFF").show();
            case "CUSTOMER" -> new DashboardMenu(scanner, user, "CUSTOMER").show();
            default -> System.out.println("This account has an unsupported role. Contact the administrator.");
        }
    }
}
