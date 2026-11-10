package ui;

import dao.UserDao;
import model.User;

import java.util.Optional;
import java.util.Scanner;

public class LoginScreen {

    private final Scanner scanner;
    private final UserDao userDao;

    public LoginScreen(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    public void show() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        Optional<User> result = userDao.login(username, password);
        if (result.isPresent()) {
            new DashboardMenu(scanner, result.get()).show();
        } else {
            System.out.println("Invalid username or password.");
        }
    }
}
