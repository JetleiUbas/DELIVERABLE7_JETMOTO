package ui;

import dao.UserDao;

import java.util.Scanner;

public class RegisterScreen {

    private final Scanner scanner;
    private final UserDao userDao;

    public RegisterScreen(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    public void show() {
        System.out.println("\n--- REGISTER ---");

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
        if (password.isEmpty()) {
            System.out.println("Password cannot be empty.");
            return;
        }

        String role = null;
        while (role == null) {
            System.out.println("Role: 1. ADMIN  2. STAFF  3. CUSTOMER");
            System.out.print("Choose: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> role = "ADMIN";
                case "2" -> role = "STAFF";
                case "3" -> role = "CUSTOMER";
                default -> System.out.println("Invalid role. Try again.");
            }
        }

        if (userDao.register(name, username, password, role)) {
            System.out.println("Registered as " + role + ". Your status is PENDING.");
        } else {
            System.out.println("Registration failed.");
        }
    }
}
