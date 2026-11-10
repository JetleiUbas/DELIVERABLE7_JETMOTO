package ui;

import dao.ActivityLogDao;
import dao.PartDao;
import model.Part;

import java.util.List;
import java.util.Scanner;

public class SearchPartsScreen {

    private final Scanner scanner;
    private final PartDao partDao = new PartDao();
    private final ActivityLogDao logDao = new ActivityLogDao();
    private final int userId; // 0 = guest (not logged in), guests are not logged

    /** Guest search from the main menu. */
    public SearchPartsScreen(Scanner scanner) {
        this(scanner, 0);
    }

    /** Search by a logged-in user; the search is recorded in the audit trail. */
    public SearchPartsScreen(Scanner scanner, int userId) {
        this.scanner = scanner;
        this.userId = userId;
    }

    public void show() {
        System.out.println("\n--- SEARCH PARTS CATALOG ---");
        System.out.print("Enter part name, category, or brand (leave blank to list all): ");
        String keyword = scanner.nextLine().trim();

        List<Part> results = partDao.search(keyword);
        if (userId > 0) {
            String term = keyword.isEmpty() ? "all parts" : "'" + keyword + "'";
            logDao.logAction(userId, "PART_SEARCHED",
                    "User [" + userId + "] searched parts for " + term + " (" + results.size() + " found)");
        }
        if (results.isEmpty()) {
            System.out.println("No parts found.");
            return;
        }

        System.out.printf("%n%-4s %-18s %-12s %-10s %10s %6s%n",
                "ID", "Part Name", "Category", "Brand", "Price", "Stock");
        System.out.println("-".repeat(65));
        for (Part p : results) {
            String stockText = p.stock() > 0 ? String.valueOf(p.stock()) : "Out";
            System.out.printf("%-4d %-18s %-12s %-10s %10.2f %6s%n",
                    p.partId(), p.partName(), p.category(), p.brand(), p.price(), stockText);
        }
        System.out.println(results.size() + " part(s) found.");
    }
}
