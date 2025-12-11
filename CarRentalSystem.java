import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class CarRentalSystem {

    // Data models
    static class Car {
        int id;
        String model;
        double pricePerDay;

        Car(int id, String model, double pricePerDay) {
            this.id = id;
            this.model = model;
            this.pricePerDay = pricePerDay;
        }
    }

    static class Booking {
        int id;
        int carId;
        String customerName;
        int days;

        Booking(int id, int carId, String customerName, int days) {
            this.id = id;
            this.carId = carId;
            this.customerName = customerName;
            this.days = days;
        }
    }

    // Data storage
    private static List<Car> cars = new ArrayList<>();
    private static List<Booking> bookings = new ArrayList<>();
    private static Scanner sc = new Scanner(System.in);

    // File paths
    private static final Path CARS_FILE = Paths.get("cars.csv");
    private static final Path BOOKINGS_FILE = Paths.get("bookings.csv");

    public static void main(String[] args) {
        loadCars();
        loadBookings();

        System.out.println("=== Welcome to Car Rental System ===");

        while (true) {
            System.out.println("\n1. Admin Login");
            System.out.println("2. Customer View");
            System.out.println("3. Exit");
            System.out.print("Select option: ");
            String option = sc.nextLine();

            switch (option) {
                case "1": adminLogin(); break;
                case "2": customerMenu(); break;
                case "3": saveData(); System.exit(0);
                default: System.out.println("Invalid option!");
            }
        }
    }

    // ------------------- ADMIN -------------------
    private static void adminLogin() {
        System.out.print("Username: ");
        String user = sc.nextLine();
        System.out.print("Password: ");
        String pass = sc.nextLine();

        if (user.equals("admin") && pass.equals("admin123")) {
            System.out.println("Login successful!");
            adminMenu();
        } else {
            System.out.println("Invalid credentials!");
        }
    }

    private static void adminMenu() {
        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Add Car");
            System.out.println("2. Delete Car");
            System.out.println("3. View Cars");
            System.out.println("4. View Bookings");
            System.out.println("5. Export Bookings CSV");
            System.out.println("6. Logout");
            System.out.print("Option: ");
            String opt = sc.nextLine();

            switch (opt) {
                case "1": addCar(); break;
                case "2": deleteCar(); break;
                case "3": viewCars(); break;
                case "4": viewBookings(); break;
                case "5": exportBookingsCsv(); break;
                case "6": return;
                default: System.out.println("Invalid option!");
            }
        }
    }

    private static void addCar() {
        try {
            System.out.print("Car ID: ");
            int id = Integer.parseInt(sc.nextLine());
            System.out.print("Model: ");
            String model = sc.nextLine();
            System.out.print("Price per day: ");
            double price = Double.parseDouble(sc.nextLine());

            for (Car c : cars) {
                if (c.id == id) { System.out.println("Car ID already exists!"); return; }
            }

            cars.add(new Car(id, model, price));
            saveCars();
            System.out.println("Car added successfully.");
        } catch (Exception e) {
            System.out.println("Invalid input!");
        }
    }

    private static void deleteCar() {
        System.out.print("Enter Car ID to delete: ");
        int id = Integer.parseInt(sc.nextLine());
        cars.removeIf(c -> c.id == id);
        bookings.removeIf(b -> b.carId == id);
        saveCars();
        saveBookings();
        System.out.println("Car and related bookings deleted if existed.");
    }

    private static void viewCars() {
        System.out.println("\n--- Car List ---");
        if (cars.isEmpty()) System.out.println("No cars available.");
        else {
            for (Car c : cars) {
                System.out.println("ID: " + c.id + ", Model: " + c.model + ", Price/day: " + c.pricePerDay);
            }
        }
    }

    private static void viewBookings() {
        System.out.println("\n--- Booking List ---");
        if (bookings.isEmpty()) System.out.println("No bookings yet.");
        else {
            for (Booking b : bookings) {
                Car c = cars.stream().filter(car -> car.id == b.carId).findFirst().orElse(null);
                String model = (c != null) ? c.model : "Deleted Car";
                System.out.println("Booking ID: " + b.id + ", Car: " + model + ", Customer: " + b.customerName + ", Days: " + b.days);
            }
        }
    }

    private static void exportBookingsCsv() {
        try {
            List<String> lines = new ArrayList<>();
            lines.add("BookingID,CarID,Customer,Days");
            for (Booking b : bookings) {
                lines.add(b.id + "," + b.carId + "," + b.customerName + "," + b.days);
            }
            Files.write(Paths.get("bookings_export.csv"), lines, StandardCharsets.UTF_8);
            System.out.println("Bookings exported to bookings_export.csv");
        } catch (Exception e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }

    // ------------------- CUSTOMER -------------------
    private static void customerMenu() {
        while (true) {
            System.out.println("\n--- Customer Menu ---");
            System.out.println("1. View Available Cars");
            System.out.println("2. Book Car");
            System.out.println("3. Cancel Booking");
            System.out.println("4. Back");
            System.out.print("Option: ");
            String opt = sc.nextLine();

            switch (opt) {
                case "1": viewCars(); break;
                case "2": bookCar(); break;
                case "3": cancelBooking(); break;
                case "4": return;
                default: System.out.println("Invalid option!");
            }
        }
    }

    private static void bookCar() {
        try {
            viewCars();
            System.out.print("Enter Car ID to book: ");
            int carId = Integer.parseInt(sc.nextLine());
            System.out.print("Customer Name: ");
            String name = sc.nextLine();
            System.out.print("Number of days: ");
            int days = Integer.parseInt(sc.nextLine());

            Optional<Car> car = cars.stream().filter(c -> c.id == carId).findFirst();
            if (!car.isPresent()) { System.out.println("Car ID not found."); return; }

            int bookingId = bookings.size() + 1;
            bookings.add(new Booking(bookingId, carId, name, days));
            saveBookings();
            System.out.println("Booking successful! Booking ID: " + bookingId);
        } catch (Exception e) {
            System.out.println("Invalid input!");
        }
    }

    private static void cancelBooking() {
        System.out.print("Enter Booking ID to cancel: ");
        int id = Integer.parseInt(sc.nextLine());
        boolean removed = bookings.removeIf(b -> b.id == id);
        if (removed) {
            saveBookings();
            System.out.println("Booking canceled.");
        } else {
            System.out.println("Booking ID not found.");
        }
    }

    // ------------------- FILE HANDLING -------------------
    private static void loadCars() {
        cars.clear();
        if (!Files.exists(CARS_FILE)) return;
        try {
            List<String> lines = Files.readAllLines(CARS_FILE, StandardCharsets.UTF_8);
            for (String ln : lines) {
                if (ln.trim().isEmpty()) continue;
                String[] parts = ln.split(",", -1);
                if (parts.length >= 3) {
                    int id = Integer.parseInt(parts[0].trim());
                    String model = parts[1].trim();
                    double price = Double.parseDouble(parts[2].trim());
                    cars.add(new Car(id, model, price));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void loadBookings() {
        bookings.clear();
        if (!Files.exists(BOOKINGS_FILE)) return;
        try {
            List<String> lines = Files.readAllLines(BOOKINGS_FILE, StandardCharsets.UTF_8);
            for (String ln : lines) {
                if (ln.trim().isEmpty()) continue;
                String[] parts = ln.split(",", -1);
                if (parts.length >= 4) {
                    int id = Integer.parseInt(parts[0].trim());
                    int carId = Integer.parseInt(parts[1].trim());
                    String customer = parts[2].trim();
                    int days = Integer.parseInt(parts[3].trim());
                    bookings.add(new Booking(id, carId, customer, days));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void saveCars() {
        try {
            List<String> lines = new ArrayList<>();
            for (Car c : cars) lines.add(c.id + "," + c.model + "," + c.pricePerDay);
            Files.write(CARS_FILE, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void saveBookings() {
        try {
            List<String> lines = new ArrayList<>();
            for (Booking b : bookings) lines.add(b.id + "," + b.carId + "," + b.customerName + "," + b.days);
            Files.write(BOOKINGS_FILE, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void saveData() {
        saveCars();
        saveBookings();
    }
}
