import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

public class EnterpriseComplaintSystem {

    // ================= DATA =================
    static class Complaint {
        String id, customer, description, status, createdBy, date;

        Complaint(String id, String customer, String desc,
                  String status, String user, String date) {
            this.id = id;
            this.customer = customer;
            this.description = desc;
            this.status = status;
            this.createdBy = user;
            this.date = date;
        }
    }

    static final Path FILE = Paths.get("complaints.csv");
    static final Scanner sc = new Scanner(System.in);
    static final List<Complaint> complaints = new ArrayList<>();

    static int idCounter = 1;
    static String loggedUser;
    static String role;

    // ================= MAIN =================
    public static void main(String[] args) {
        login();
        loadData();
        menu();
    }

    // ================= LOGIN =================
    static void login() {
        System.out.println("=== ENTERPRISE COMPLAINT SYSTEM ===");

        while (true) {
            System.out.print("Username: ");
            String u = sc.nextLine();

            System.out.print("Password: ");
            String p = sc.nextLine();

            if (u.equals("admin") && p.equals("admin123")) {
                loggedUser = "Admin";
                role = "ADMIN";
                break;
            } else if (u.equals("staff") && p.equals("staff123")) {
                loggedUser = "Staff";
                role = "STAFF";
                break;
            } else {
                System.out.println("❌ Invalid login. Try again.\n");
            }
        }
        System.out.println("✅ Logged in as " + loggedUser + " (" + role + ")\n");
    }

    // ================= MENU =================
    static void menu() {
        while (true) {
            System.out.println("\n===== MENU =====");
            System.out.println("1. Add Complaint");
            System.out.println("2. View Complaints");
            System.out.println("3. Edit Complaint");

            if (role.equals("ADMIN"))
                System.out.println("4. Delete Complaint");

            System.out.println("5. Search by Status");
            System.out.println("6. Export CSV");
            System.out.println("0. Exit");

            System.out.print("Choose: ");
            String ch = sc.nextLine();

            switch (ch) {
                case "1": addComplaint(); break;
                case "2": viewComplaints(); break;
                case "3": editComplaint(); break;
                case "4":
                    if (role.equals("ADMIN")) deleteComplaint();
                    else System.out.println("❌ Access denied.");
                    break;
                case "5": filterByStatus(); break;
                case "6": exportCSV(); break;
                case "0":
                    saveData();
                    System.out.println("👋 Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // ================= CRUD =================
    static void addComplaint() {
        System.out.print("Customer Name: ");
        String customer = sc.nextLine();

        System.out.print("Description: ");
        String desc = sc.nextLine();

        System.out.print("Status (Open/In Progress/Resolved/Closed): ");
        String status = sc.nextLine();

        Complaint c = new Complaint(
                "CMP" + (idCounter++),
                customer,
                desc,
                status,
                loggedUser,
                LocalDate.now().toString()
        );
        complaints.add(c);
        saveData();
        System.out.println("✅ Complaint added.");
    }

    static void viewComplaints() {
        System.out.println("\n--- COMPLAINT LIST ---");
        for (Complaint c : complaints) {
            printComplaint(c);
        }
    }

    static void editComplaint() {
        System.out.print("Enter Complaint ID: ");
        String id = sc.nextLine();

        Complaint c = findById(id);
        if (c == null) {
            System.out.println("❌ Not found.");
            return;
        }

        System.out.print("New Customer (" + c.customer + "): ");
        String cust = sc.nextLine();
        if (!cust.isEmpty()) c.customer = cust;

        System.out.print("New Description (" + c.description + "): ");
        String desc = sc.nextLine();
        if (!desc.isEmpty()) c.description = desc;

        System.out.print("New Status (" + c.status + "): ");
        String status = sc.nextLine();
        if (!status.isEmpty()) c.status = status;

        saveData();
        System.out.println("✅ Complaint updated.");
    }

    static void deleteComplaint() {
        System.out.print("Enter Complaint ID to delete: ");
        String id = sc.nextLine();

        Complaint c = findById(id);
        if (c == null) {
            System.out.println("❌ Not found.");
            return;
        }

        complaints.remove(c);
        saveData();
        System.out.println("🗑 Complaint deleted.");
    }

    static void filterByStatus() {
        System.out.print("Enter status: ");
        String s = sc.nextLine();

        for (Complaint c : complaints) {
            if (c.status.equalsIgnoreCase(s))
                printComplaint(c);
        }
    }

    // ================= STORAGE =================
    static void saveData() {
        try {
            List<String> lines = new ArrayList<>();
            for (Complaint c : complaints) {
                lines.add(String.join(",",
                        c.id,
                        c.customer,
                        c.description.replace(",", " "),
                        c.status,
                        c.createdBy,
                        c.date));
            }
            Files.write(FILE, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            System.out.println("Error saving data.");
        }
    }

    static void loadData() {
        complaints.clear();
        if (!Files.exists(FILE)) return;

        try {
            for (String l : Files.readAllLines(FILE)) {
                String[] p = l.split(",", -1);
                complaints.add(new Complaint(p[0], p[1], p[2], p[3], p[4], p[5]));
                idCounter++;
            }
        } catch (Exception e) {
            System.out.println("Error loading data.");
        }
    }

    static void exportCSV() {
        try {
            Files.copy(FILE, Paths.get("complaints_export.csv"),
                    StandardCopyOption.REPLACE_EXISTING);
            System.out.println("📁 Exported to complaints_export.csv");
        } catch (Exception e) {
            System.out.println("Export failed.");
        }
    }

    // ================= UTIL =================
    static Complaint findById(String id) {
        for (Complaint c : complaints)
            if (c.id.equalsIgnoreCase(id))
                return c;
        return null;
    }

    static void printComplaint(Complaint c) {
        System.out.println("--------------------------------");
        System.out.println("ID: " + c.id);
        System.out.println("Customer: " + c.customer);
        System.out.println("Description: " + c.description);
        System.out.println("Status: " + c.status);
        System.out.println("Created By: " + c.createdBy);
        System.out.println("Date: " + c.date);
    }
}
