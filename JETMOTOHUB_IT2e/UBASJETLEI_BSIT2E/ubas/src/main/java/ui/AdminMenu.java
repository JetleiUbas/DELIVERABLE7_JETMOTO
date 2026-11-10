package ui;

import dao.ActivityLogDao;
import dao.UserDao;
import model.ActivityLog;
import model.User;

import java.util.List;
import java.util.Scanner;

/** Administration screens for reviewing users and managing account access. */
public final class AdminMenu {
    private final Scanner scanner;
    private final UserDao userDao;
    private final User currentAdmin;
    private final ActivityLogDao logDao = new ActivityLogDao();

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
            System.out.println("6. View System Audit Logs");
            System.out.println("7. Logout");
            int choice = readIntInput("Choose an option (0 to cancel): ");

            switch (choice) {
                case 0 -> System.out.println("Cancelled.");
                case 1 -> displayUsers(userDao.getAllUsers());
                case 2 -> displayUsers(userDao.getPendingUsers());
                case 3 -> approveAccount();
                case 4 -> updateUser();
                case 5 -> new SearchPartsScreen(scanner, currentAdmin.id()).show();
                case 6 -> auditLogMenu();
                case 7 -> {
                    System.out.println("Logged out.");
                    logDao.logAction(currentAdmin.id(), "USER_LOGOUT",
                            "Admin [" + currentAdmin.id() + "] logged out");
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
            logDao.logAction(currentAdmin.id(), "USER_UPDATED",
                    "Admin [" + currentAdmin.id() + "] changed User " + targetUserId
                            + " to role " + newRole + ", status " + newStatus);
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
            logDao.logAction(currentAdmin.id(), "USER_APPROVED",
                    "Admin [" + currentAdmin.id() + "] approved account for User ID " + userId);
        } else {
            System.out.println("Approval failed. The account may already have been updated.");
        }
    }

    /** Sub-menu for the activity_logs audit trail. */
    private void auditLogMenu() {
        boolean inAudit = true;
        while (inAudit) {
            System.out.println("\n===== SYSTEM AUDIT LOGS =====");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear All Logs");
            System.out.println("0. Back");
            int choice = readIntInput("Choose an option: ");

            switch (choice) {
                case 1 -> displayLogs(logDao.getAllLogs());
                case 2 -> {
                    int userId = readIntInput("Enter the User ID to filter (0 to cancel): ");
                    if (userId == 0) {
                        System.out.println("Filter cancelled.");
                    } else {
                        displayLogs(logDao.getLogsByUserId(userId));
                    }
                }
                case 3 -> clearLogs();
                case 0 -> inAudit = false;
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    /** Asks for typed confirmation, wipes the history, then records who did it. */
    private void clearLogs() {
        System.out.println("WARNING: This permanently deletes ALL activity log history.");
        System.out.print("Type YES to confirm (anything else cancels): ");
        if (!"YES".equals(scanner.nextLine().trim())) {
            System.out.println("Clear logs cancelled.");
            return;
        }
        int removed = logDao.clearAllLogs();
        if (removed < 0) {
            System.out.println("Failed to clear logs.");
            return;
        }
        System.out.println(removed + " log entr" + (removed == 1 ? "y" : "ies") + " deleted.");
        // Leave a trace of the clear itself so the trail is never silently empty.
        logDao.logAction(currentAdmin.id(), "LOGS_CLEARED",
                "Admin [" + currentAdmin.id() + "] cleared " + removed + " log entries");
    }

    /** Renders log entries in the required aligned console table. */
    private void displayLogs(List<ActivityLog> logs) {
        if (logs.isEmpty()) {
            System.out.println("No activity logs found.");
            return;
        }

        String format = "%-5s | %-8s | %-18s | %-35s | %-20s%n";
        String line = "-".repeat(5 + 3 + 8 + 3 + 18 + 3 + 35 + 3 + 20);
        System.out.println(line);
        System.out.printf(format, "ID", "USER ID", "ACTION TYPE", "DESCRIPTION", "TIMESTAMP");
        System.out.println(line);
        for (ActivityLog log : logs) {
            System.out.printf(format,
                    log.logId(), log.userId(),
                    fit(log.actionType(), 18), fit(log.description(), 35), fit(log.timestamp(), 20));
        }
        System.out.println(line);
        System.out.println(logs.size() + " log entr" + (logs.size() == 1 ? "y" : "ies") + " shown.");
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
        if (value.length() <= width) return value;
        return width > 3 ? value.substring(0, width - 3) + "..." : value.substring(0, width);
    }
}
