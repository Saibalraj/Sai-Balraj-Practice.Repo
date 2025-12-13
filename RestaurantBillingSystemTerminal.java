import java.io.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;

public class RestaurantBillingSystemTerminal {

    /* ================= MODELS ================= */
    static class MenuItem {
        String name;
        double price;
        int stock;

        MenuItem(String n, double p, int s) {
            name = n;
            price = p;
            stock = s;
        }
    }

    static class OrderItem {
        MenuItem item;
        int qty;

        OrderItem(MenuItem i, int q) {
            item = i;
            qty = q;
        }

        double total() {
            return item.price * qty;
        }
    }

    /* ================= DATA ================= */
    private static final List<MenuItem> menu = new ArrayList<>();
    private static final List<OrderItem> order = new ArrayList<>();

    private static final double GST = 0.05;
    private static final double SERVICE = 0.10;

    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin123";
    private static final String STAFF_USER = "staff";
    private static final String STAFF_PASS = "staff123";

    private static boolean isAdmin;
    private static int invoiceNo = 1001;

    private static final File MENU_FILE = new File("menu.csv");
    private static final Scanner sc = new Scanner(System.in);
    private static final DecimalFormat df = new DecimalFormat("0.00");

    /* ================= MAIN ================= */
    public static void main(String[] args) {
        loadMenu();
        login();
        mainMenu();
    }

    /* ================= LOGIN ================= */
    private static void login() {
        System.out.print("Username: ");
        String u = sc.nextLine();
        System.out.print("Password: ");
        String p = sc.nextLine();

        if (u.equals(ADMIN_USER) && p.equals(ADMIN_PASS)) isAdmin = true;
        else if (u.equals(STAFF_USER) && p.equals(STAFF_PASS)) isAdmin = false;
        else {
            System.out.println("❌ Invalid Login");
            System.exit(0);
        }
        System.out.println("✅ Login Successful\n");
    }

    /* ================= MAIN MENU ================= */
    private static void mainMenu() {
        while (true) {
            System.out.println("\n===== RESTAURANT SYSTEM =====");
            System.out.println("1. View Menu");
            System.out.println("2. Add to Bill");
            System.out.println("3. Remove from Bill");
            System.out.println("4. Generate Invoice");
            if (isAdmin) {
                System.out.println("5. Add Menu Item");
                System.out.println("6. Edit Menu Item");
                System.out.println("7. Delete Menu Item");
            }
            System.out.println("0. Exit");
            System.out.print("Choose: ");

            int ch = Integer.parseInt(sc.nextLine());

            switch (ch) {
                case 1 -> showMenu();
                case 2 -> addOrder();
                case 3 -> removeOrder();
                case 4 -> generateInvoice();
                case 5 -> { if (isAdmin) addMenuItem(); }
                case 6 -> { if (isAdmin) editMenuItem(); }
                case 7 -> { if (isAdmin) deleteMenuItem(); }
                case 0 -> System.exit(0);
                default -> System.out.println("Invalid option");
            }
        }
    }

    /* ================= MENU ================= */
    private static void showMenu() {
        System.out.println("\n--- MENU ---");
        for (int i = 0; i < menu.size(); i++) {
            MenuItem m = menu.get(i);
            System.out.printf("%d. %s | ₹%.2f | Stock: %d%n",
                    i + 1, m.name, m.price, m.stock);
        }
    }

    private static void addMenuItem() {
        System.out.print("Item Name: ");
        String name = sc.nextLine();
        System.out.print("Price: ");
        double price = Double.parseDouble(sc.nextLine());
        System.out.print("Stock: ");
        int stock = Integer.parseInt(sc.nextLine());

        menu.add(new MenuItem(name, price, stock));
        saveMenu();
        System.out.println("✅ Item added");
    }

    private static void editMenuItem() {
        showMenu();
        System.out.print("Select item number: ");
        int i = Integer.parseInt(sc.nextLine()) - 1;
        if (i < 0 || i >= menu.size()) return;

        MenuItem m = menu.get(i);
        System.out.print("New Name: ");
        m.name = sc.nextLine();
        System.out.print("New Price: ");
        m.price = Double.parseDouble(sc.nextLine());
        System.out.print("New Stock: ");
        m.stock = Integer.parseInt(sc.nextLine());

        saveMenu();
        System.out.println("✅ Item updated");
    }

    private static void deleteMenuItem() {
        showMenu();
        System.out.print("Select item number: ");
        int i = Integer.parseInt(sc.nextLine()) - 1;
        if (i < 0 || i >= menu.size()) return;

        menu.remove(i);
        saveMenu();
        System.out.println("✅ Item deleted");
    }

    /* ================= ORDER ================= */
    private static void addOrder() {
        showMenu();
        System.out.print("Select item number: ");
        int i = Integer.parseInt(sc.nextLine()) - 1;
        if (i < 0 || i >= menu.size()) return;

        MenuItem m = menu.get(i);
        System.out.print("Quantity: ");
        int q = Integer.parseInt(sc.nextLine());

        if (m.stock < q) {
            System.out.println("❌ Not enough stock");
            return;
        }

        m.stock -= q;
        order.add(new OrderItem(m, q));
        saveMenu();
        System.out.println("✅ Added to bill");
    }

    private static void removeOrder() {
        if (order.isEmpty()) return;

        for (int i = 0; i < order.size(); i++) {
            OrderItem o = order.get(i);
            System.out.printf("%d. %s x%d%n", i + 1, o.item.name, o.qty);
        }

        System.out.print("Select to remove: ");
        int i = Integer.parseInt(sc.nextLine()) - 1;
        if (i < 0 || i >= order.size()) return;

        OrderItem o = order.remove(i);
        o.item.stock += o.qty;
        saveMenu();
        System.out.println("✅ Removed");
    }

    /* ================= INVOICE ================= */
    private static void generateInvoice() {
        if (order.isEmpty()) return;

        System.out.print("Discount %: ");
        double discountP = Double.parseDouble(sc.nextLine());

        double sub = 0;
        for (OrderItem o : order) sub += o.total();

        double discount = sub * discountP / 100;
        double gst = (sub - discount) * GST;
        double service = (sub - discount) * SERVICE;
        double total = sub - discount + gst + service;

        try (PrintWriter pw = new PrintWriter("Invoice_" + invoiceNo++ + ".txt")) {
            pw.println("RESTAURANT BILL");
            pw.println("Date: " + LocalDate.now());
            pw.println("--------------------------------");
            for (OrderItem o : order)
                pw.printf("%s x%d = %.2f%n", o.item.name, o.qty, o.total());
            pw.println("--------------------------------");
            pw.printf("TOTAL: %.2f%n", total);
        } catch (Exception e) {
            e.printStackTrace();
        }

        order.clear();
        System.out.println("🧾 Invoice Generated");
    }

    /* ================= FILE ================= */
    private static void saveMenu() {
        try (PrintWriter pw = new PrintWriter(MENU_FILE)) {
            for (MenuItem m : menu)
                pw.println(m.name + "," + m.price + "," + m.stock);
        } catch (Exception ignored) {}
    }

    private static void loadMenu() {
        if (!MENU_FILE.exists()) {
            menu.add(new MenuItem("Burger", 120, 50));
            menu.add(new MenuItem("Pizza", 250, 40));
            menu.add(new MenuItem("Coffee", 60, 100));
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(MENU_FILE))) {
            String l;
            while ((l = br.readLine()) != null) {
                String[] p = l.split(",");
                menu.add(new MenuItem(p[0],
                        Double.parseDouble(p[1]),
                        Integer.parseInt(p[2])));
            }
        } catch (Exception ignored) {}
    }
}
