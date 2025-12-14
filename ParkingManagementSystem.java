import java.util.*;
import java.text.SimpleDateFormat;

class Vehicle {
    String vehicleNumber;
    String vehicleType;
    int slotNumber;
    long entryTime;

    Vehicle(String vehicleNumber, String vehicleType, int slotNumber) {
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.slotNumber = slotNumber;
        this.entryTime = System.currentTimeMillis();
    }
}

public class ParkingManagementSystem {

    static Scanner sc = new Scanner(System.in);
    static Map<Integer, Vehicle> parkingSlots = new HashMap<>();
    static int maxSlots = 20;

    public static void main(String[] args) {

        while (true) {
            System.out.println("\n====== PARKING MANAGEMENT SYSTEM ======");
            System.out.println("1. Park Vehicle");
            System.out.println("2. Exit Vehicle");
            System.out.println("3. View Parked Vehicles");
            System.out.println("4. Search Vehicle");
            System.out.println("5. Exit System");
            System.out.print("Choose option: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> parkVehicle();
                case 2 -> exitVehicle();
                case 3 -> viewVehicles();
                case 4 -> searchVehicle();
                case 5 -> {
                    System.out.println("System Closed.");
                    System.exit(0);
                }
                default -> System.out.println("Invalid option!");
            }
        }
    }

    // Park vehicle
    static void parkVehicle() {
        if (parkingSlots.size() >= maxSlots) {
            System.out.println("❌ Parking Full!");
            return;
        }

        System.out.print("Enter Vehicle Number: ");
        String number = sc.nextLine();

        System.out.print("Enter Vehicle Type (Bike/Car/Truck): ");
        String type = sc.nextLine();

        int slot = getFreeSlot();
        Vehicle v = new Vehicle(number, type, slot);
        parkingSlots.put(slot, v);

        System.out.println("✅ Vehicle Parked Successfully!");
        System.out.println("Slot Number: " + slot);
    }

    // Exit vehicle
    static void exitVehicle() {
        System.out.print("Enter Slot Number: ");
        int slot = sc.nextInt();

        if (!parkingSlots.containsKey(slot)) {
            System.out.println("❌ Invalid Slot!");
            return;
        }

        Vehicle v = parkingSlots.remove(slot);
        long exitTime = System.currentTimeMillis();
        long hours = Math.max(1, (exitTime - v.entryTime) / (1000 * 60 * 60));

        int rate = switch (v.vehicleType.toLowerCase()) {
            case "bike" -> 20;
            case "car" -> 50;
            case "truck" -> 100;
            default -> 50;
        };

        long fee = hours * rate;

        System.out.println("\n====== PARKING RECEIPT ======");
        System.out.println("Vehicle Number : " + v.vehicleNumber);
        System.out.println("Vehicle Type   : " + v.vehicleType);
        System.out.println("Slot Number    : " + slot);
        System.out.println("Hours Parked   : " + hours);
        System.out.println("Total Fee      : ₹" + fee);
        System.out.println("============================");
    }

    // View parked vehicles
    static void viewVehicles() {
        if (parkingSlots.isEmpty()) {
            System.out.println("No vehicles parked.");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        System.out.println("\nSlot | Vehicle No | Type | Entry Time");
        System.out.println("------------------------------------");

        for (Vehicle v : parkingSlots.values()) {
            System.out.printf("%4d | %-10s | %-6s | %s%n",
                    v.slotNumber,
                    v.vehicleNumber,
                    v.vehicleType,
                    sdf.format(new Date(v.entryTime)));
        }
    }

    // Search vehicle
    static void searchVehicle() {
        System.out.print("Enter Vehicle Number: ");
        String search = sc.nextLine();

        for (Vehicle v : parkingSlots.values()) {
            if (v.vehicleNumber.equalsIgnoreCase(search)) {
                System.out.println("✅ Vehicle Found!");
                System.out.println("Slot Number: " + v.slotNumber);
                System.out.println("Vehicle Type: " + v.vehicleType);
                return;
            }
        }
        System.out.println("❌ Vehicle Not Found!");
    }

    // Find free slot
    static int getFreeSlot() {
        for (int i = 1; i <= maxSlots; i++) {
            if (!parkingSlots.containsKey(i)) {
                return i;
            }
        }
        return -1;
    }
}
