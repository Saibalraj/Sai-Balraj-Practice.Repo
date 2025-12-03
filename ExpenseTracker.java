import java.io.*;
import java.text.*;
import java.util.*;

public class ExpenseTracker{

    static Scanner sc = new Scanner(System.in);
    static ArrayList<Expense> expenses = new ArrayList<>();
    static double budget = 0;

    public static void main(String[] args) {
        loadCSV(); // load existing data
        while (true) {
            System.out.println("\n=== Expense Tracker ===");
            System.out.println("1. Add Expense");
            System.out.println("2. Delete Expense");
            System.out.println("3. Show Expenses");
            System.out.println("4. Set Budget");
            System.out.println("5. Show Summary");
            System.out.println("6. Generate Recurring Expenses");
            System.out.println("7. Save to CSV");
            System.out.println("8. Load CSV");
            System.out.println("9. Exit");
            System.out.print("Choose option: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1": addExpense(); break;
                case "2": deleteExpense(); break;
                case "3": showExpenses(); break;
                case "4": setBudget(); break;
                case "5": showSummary(); break;
                case "6": generateRecurring(); break;
                case "7": saveCSV(); break;
                case "8": loadCSV(); break;
                case "9": System.exit(0); break;
                default: System.out.println("Invalid option!");
            }
        }
    }

    // ============================
    // ADD EXPENSE
    // ============================
    private static void addExpense() {
        try {
            System.out.print("Amount: ");
            double amount = Double.parseDouble(sc.nextLine());

            System.out.print("Category: ");
            String category = sc.nextLine();

            System.out.print("Date (YYYY-MM-DD): ");
            String date = sc.nextLine();
            if (date.isEmpty()) date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

            System.out.print("Description: ");
            String desc = sc.nextLine();

            System.out.print("Recurring? (yes/no): ");
            boolean recurring = sc.nextLine().equalsIgnoreCase("yes");

            Expense ex = new Expense(amount, category, date, desc, recurring);
            expenses.add(ex);

            System.out.println("Expense added!");
        } catch (Exception e) {
            System.out.println("Invalid input!");
        }
    }

    // ============================
    // DELETE EXPENSE
    // ============================
    private static void deleteExpense() {
        showExpenses();
        System.out.print("Enter expense number to delete: ");
        try {
            int idx = Integer.parseInt(sc.nextLine()) - 1;
            if (idx >= 0 && idx < expenses.size()) {
                expenses.remove(idx);
                System.out.println("Expense deleted.");
            } else {
                System.out.println("Invalid number!");
            }
        } catch (Exception e) {
            System.out.println("Invalid input!");
        }
    }

    // ============================
    // SHOW EXPENSES
    // ============================
    private static void showExpenses() {
        if (expenses.isEmpty()) {
            System.out.println("No expenses recorded.");
            return;
        }
        System.out.println("\nNo | Amount | Category | Date | Description | Recurring");
        for (int i = 0; i < expenses.size(); i++) {
            Expense ex = expenses.get(i);
            System.out.printf("%d | %.2f | %s | %s | %s | %s\n",
                    i+1, ex.amount, ex.category, ex.date, ex.description, ex.recurring ? "Yes" : "No");
        }
    }

    // ============================
    // SET BUDGET
    // ============================
    private static void setBudget() {
        System.out.print("Enter monthly budget: ");
        try {
            budget = Double.parseDouble(sc.nextLine());
            System.out.println("Budget set to: " + budget);
        } catch (Exception e) {
            System.out.println("Invalid input!");
        }
    }

    // ============================
    // SHOW SUMMARY
    // ============================
    private static void showSummary() {
        double sum = 0;
        String curMonth = new SimpleDateFormat("yyyy-MM").format(new Date());
        for (Expense ex : expenses) {
            if (ex.date.startsWith(curMonth)) sum += ex.amount;
        }
        System.out.println("Monthly Total: " + sum);
        if (budget > 0) {
            System.out.println("Budget: " + budget);
            if (sum > budget) System.out.println("⚠ Budget Exceeded!");
        }
    }

    // ============================
    // GENERATE RECURRING
    // ============================
    private static void generateRecurring() {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String month = today.substring(0, 7);

        int count = 0;
        for (Expense ex : new ArrayList<>(expenses)) {
            if (ex.recurring && !ex.date.startsWith(month)) {
                Expense newEx = new Expense(ex.amount, ex.category, month + "-01", ex.description, true);
                expenses.add(newEx);
                count++;
            }
        }
        System.out.println(count + " recurring expenses added.");
    }

    // ============================
    // SAVE CSV
    // ============================
    private static void saveCSV() {
        try (PrintWriter pw = new PrintWriter("expenses.csv")) {
            for (Expense ex : expenses) {
                pw.println(ex.amount + "," + ex.category + "," + ex.date + "," +
                        ex.description + "," + ex.recurring);
            }
            System.out.println("Saved to expenses.csv");
        } catch (Exception e) {
            System.out.println("Error saving!");
        }
    }

    // ============================
    // LOAD CSV
    // ============================
    private static void loadCSV() {
        try {
            expenses.clear();
            BufferedReader br = new BufferedReader(new FileReader("expenses.csv"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                Expense ex = new Expense(
                        Double.parseDouble(p[0]),
                        p[1], p[2], p[3],
                        Boolean.parseBoolean(p[4])
                );
                expenses.add(ex);
            }
            br.close();
            System.out.println("Loaded " + expenses.size() + " expenses.");
        } catch (FileNotFoundException e) {
            System.out.println("No CSV found, starting fresh.");
        } catch (Exception e) {
            System.out.println("Error loading CSV!");
        }
    }
}

// ============================
// EXPENSE CLASS
// ============================
class Expense {
    double amount;
    String category, date, description;
    boolean recurring;

    public Expense(double a, String c, String d, String desc, boolean r) {
        amount = a; category = c; date = d; description = desc; recurring = r;
    }
}
