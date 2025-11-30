import java.util.*;

class Product {
    String id;
    String name;
    int stock;
    double price;

    public Product(String id, String name, int stock, double price) {
        this.id = id;
        this.name = name;
        this.stock = stock;
        this.price = price;
    }

    public String toString() {
        return "Product ID: " + id + " | Name: " + name + 
               " | Stock: " + stock + " | Price: " + price;
    }
}

class Supplier {
    String id;
    String name;
    String phone;

    public Supplier(String id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public String toString() {
        return "Supplier ID: " + id + " | Name: " + name + " | Phone: " + phone;
    }
}

class Order {
    String orderId;
    String productId;
    String supplierId;
    int quantity;
    Date date;

    public Order(String orderId, String productId, String supplierId, int quantity) {
        this.orderId = orderId;
        this.productId = productId;
        this.supplierId = supplierId;
        this.quantity = quantity;
        this.date = new Date();
    }

    public String toString() {
        return "Order ID: " + orderId + " | Product: " + productId +
               " | Supplier: " + supplierId + " | Qty: " + quantity +
               " | Date: " + date;
    }
}

public class InventoryManagementSystem {

    static Scanner sc = new Scanner(System.in);

    static Map<String, Product> products = new HashMap<>();
    static Map<String, Supplier> suppliers = new HashMap<>();
    static List<Order> orders = new ArrayList<>();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n==== INVENTORY MANAGEMENT SYSTEM ====");
            System.out.println("1. Add Product");
            System.out.println("2. Update Product Stock");
            System.out.println("3. Delete Product");
            System.out.println("4. View All Products");
            System.out.println("5. Add Supplier");
            System.out.println("6. View Suppliers");
            System.out.println("7. Create Order");
            System.out.println("8. View Orders");
            System.out.println("9. Exit");
            System.out.print("Choose: ");

            int ch = sc.nextInt();
            sc.nextLine();

            switch (ch) {
                case 1: addProduct(); break;
                case 2: updateStock(); break;
                case 3: deleteProduct(); break;
                case 4: viewProducts(); break;
                case 5: addSupplier(); break;
                case 6: viewSuppliers(); break;
                case 7: createOrder(); break;
                case 8: viewOrders(); break;
                case 9: System.out.println("Exiting..."); return;
                default: System.out.println("Invalid choice!");
            }
        }
    }

    // -------------------------------------------------------
    // PRODUCT METHODS
    // -------------------------------------------------------

    static void addProduct() {
        System.out.print("Product ID: ");
        String id = sc.nextLine();

        if (products.containsKey(id)) {
            System.out.println("Product already exists!");
            return;
        }

        System.out.print("Name: ");
        String name = sc.nextLine();
        System.out.print("Stock: ");
        int stock = sc.nextInt();
        System.out.print("Price: ");
        double price = sc.nextDouble();
        sc.nextLine();

        products.put(id, new Product(id, name, stock, price));
        System.out.println("✔ Product Added Successfully!");
    }

    static void updateStock() {
        System.out.print("Enter Product ID: ");
        String id = sc.nextLine();

        Product p = products.get(id);
        if (p == null) {
            System.out.println("❌ Product not found!");
            return;
        }

        System.out.print("Enter new stock amount: ");
        int stock = sc.nextInt();
        sc.nextLine();

        p.stock = stock;
        System.out.println("✔ Stock Updated Successfully!");
    }

    static void deleteProduct() {
        System.out.print("Enter Product ID to Delete: ");
        String id = sc.nextLine();

        if (products.remove(id) != null) {
            System.out.println("✔ Product Deleted Successfully!");
        } else {
            System.out.println("❌ Product not found!");
        }
    }

    static void viewProducts() {
        if (products.isEmpty()) {
            System.out.println("No products available!");
            return;
        }

        System.out.println("\n---- PRODUCT LIST ----");
        products.values().forEach(System.out::println);
    }

    // -------------------------------------------------------
    // SUPPLIER METHODS
    // -------------------------------------------------------

    static void addSupplier() {
        System.out.print("Supplier ID: ");
        String id = sc.nextLine();

        if (suppliers.containsKey(id)) {
            System.out.println("Supplier already exists!");
            return;
        }

        System.out.print("Name: ");
        String name = sc.nextLine();
        System.out.print("Phone: ");
        String phone = sc.nextLine();

        suppliers.put(id, new Supplier(id, name, phone));
        System.out.println("✔ Supplier Added Successfully!");
    }

    static void viewSuppliers() {
        if (suppliers.isEmpty()) {
            System.out.println("No suppliers available!");
            return;
        }

        System.out.println("\n---- SUPPLIER LIST ----");
        suppliers.values().forEach(System.out::println);
    }

    // -------------------------------------------------------
    // ORDER METHODS
    // -------------------------------------------------------

    static void createOrder() {
        System.out.print("Order ID: ");
        String oid = sc.nextLine();

        System.out.print("Product ID: ");
        String pid = sc.nextLine();

        if (!products.containsKey(pid)) {
            System.out.println("❌ Product not found!");
            return;
        }

        System.out.print("Supplier ID: ");
        String sid = sc.nextLine();

        if (!suppliers.containsKey(sid)) {
            System.out.println("❌ Supplier not found!");
            return;
        }

        System.out.print("Quantity: ");
        int qty = sc.nextInt();
        sc.nextLine();

        // Update stock
        products.get(pid).stock += qty;

        Order order = new Order(oid, pid, sid, qty);
        orders.add(order);

        System.out.println("✔ Order Created and Stock Updated!");
    }

    static void viewOrders() {
        if (orders.isEmpty()) {
            System.out.println("No orders available!");
            return;
        }

        System.out.println("\n---- ORDER LIST ----");
        orders.forEach(System.out::println);
    }
}
