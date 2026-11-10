package ui;

import dao.UserDao;
import model.User;

import java.util.List;
import java.util.Scanner;

/** Administration screens for reviewing users and managing account access. */
public final class AdminMenu {
    private final Scanner scanner;
    private final UserDao userDao;
    private final User currentAdmin;

    private static final String[] VALID_ROLES = {"ADMIN", "STAFF", "CUSTOMER"};
    private static final String[] VALID_STATUSES = {"APPROVED", "PENDING"};

    public AdminMenu(Scanner scanner, UserDao userDao, User currentAdmin) {
        this.scanner = scanner;
        this.userDao = userDao;
        this.currentAdmin = currentAdmin;
    }

    public void show() {
        boolean running = true;
        while (running) {
            System.out.println("\\n===== ADMIN MENU =====");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User");
            System.out.println("5. Search Parts Catalog");
            System.out.println("6. Logout");
            int choice = readIntInput("Choose an option (0 to cancel): ");

            switch (choice) {
                case 0 -> System.out.println("Cancelled.");
                case 1 -> displayUsers(userDao.getAllUsers());
                case 2 -> displayUsers(userDao.getPendingUsers());
                case 3 -> approveAccount();
                case 4 -> updateUser();
                case 5 -> new SearchPartsScreen(scanner).show();
                case 6 -> {
                    System.out.println("Logged out.");
                    running = false;
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void updateUser() {
        List<User> users = userDao.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        System.out.println("\\n--- UPDATE USER ---");
        displayUsers(users);
        int targetUserId = readIntInput("Enter the target User ID (0 to cancel): ");
        if (targetUserId == 0) {
            System.out.println("Update cancelled.");
            return;
        }

        User target = users.stream()
                .filter(user -> user.id() == targetUserId)
                .findFirst().orElse(null);
        if (target == null) {
            System.out.println("User ID not found. Update cancelled.");
            return;
        }

        if (targetUserId == currentAdmin.id()) {
            System.out.println("WARNING: Modifying the currently active admin account is restricted.");
            return;
        }

        String newRole = readValidRole();
        if (newRole == null) {
            System.out.println("Update cancelled.");
            return;
        }

        String newStatus = readValidStatus();
        if (newStatus == null) {
            System.out.println("Update cancelled.");
            return;
        }

        if (userDao.updateUserRoleAndStatus(targetUserId, newRole, newStatus)) {
            System.out.println("User ID " + targetUserId + " updated successfully. Role: "
                    + newRole + " | Status: " + newStatus);
        } else {
            System.out.println("User update failed.");
        }
    }

    /** Reads an integer safely; invalid input is re-prompted instead of crashing. */
    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a numeric value (0 to cancel).");
            }
        }
    }

    /** Returns a valid project role, or null when the user enters 0. */
    private String readValidRole() {
        while (true) {
            System.out.println("Valid roles: ADMIN, STAFF, CUSTOMER");
            System.out.print("Enter new role (0 to cancel): ");
            String role = scanner.nextLine().trim().toUpperCase(java.util.Locale.ROOT);
            if ("0".equals(role)) return null;
            for (String validRole : VALID_ROLES) {
                if (validRole.equals(role)) return role;
            }
            System.out.println("Invalid role. Please enter ADMIN, STAFF, CUSTOMER, or 0 to cancel.");
        }
    }

    /** Returns a valid account status, or null when the user enters 0. */
    private String readValidStatus() {
        while (true) {
            System.out.println("Valid statuses: APPROVED, PENDING");
            System.out.print("Enter new status (0 to cancel): ");
            String status = scanner.nextLine().trim().toUpperCase(java.util.Locale.ROOT);
            if ("0".equals(status)) return null;
            for (String validStatus : VALID_STATUSES) {
                if (validStatus.equals(status)) return status;
            }
            System.out.println("Invalid status. Please enter APPROVED, PENDING, or 0 to cancel.");
        }
    }

    private void approveAccount() {
        List<User> pendingUsers = userDao.getPendingUsers();
        if (pendingUsers.isEmpty()) {
            System.out.println("There are no pending registrations.");
            return;
        }

        System.out.println("\\n--- PENDING REGISTRATIONS ---");
        displayUsers(pendingUsers);
        int userId = readIntInput("Enter the User ID to approve (0 to cancel): ");
        if (userId == 0) {
            System.out.println("Approval cancelled.");
            return;
        }

        boolean isPendingUser = pendingUsers.stream().anyMatch(user -> user.id() == userId);
        if (!isPendingUser) {
            System.out.println("That ID is not in the pending registrations list.");
            return;
        }

        if (userDao.approveUser(userId)) {
            System.out.println("User ID " + userId + " has been approved.");
        } else {
            System.out.println("Approval failed. The account may already have been updated.");
        }
    }

    /** Renders any user list using fixed-width columns for aligned console output. */
    private void displayUsers(List<User> users) {
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        String border = "+-------+-----------------+--------------+------------+";
        System.out.println(border);
        System.out.printf("| %-5s | %-15s | %-12s | %-10s |%n",
                "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println(border);
        for (User user : users) {
            System.out.printf("| %-5s | %-15s | %-12s | %-10s |%n",
                    user.id(), fit(user.username(), 15), fit(user.roles(), 12), fit(user.status(), 10));
        }
        System.out.println(border);
    }

    private String fit(String value, int width) {
        if (value == null) return "";
        return value.length() <= width ? value : value.substring(0, width);
    }
}
