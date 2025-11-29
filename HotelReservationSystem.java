import java.util.*;

class Room {
    int roomNumber;
    String type;
    double price;
    boolean isBooked;

    Room(int roomNumber, String type, double price) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.price = price;
        this.isBooked = false;
    }

    @Override
    public String toString() {
        return "Room " + roomNumber + " | " + type + " | ₹" + price + " | " +
                (isBooked ? "Booked" : "Available");
    }
}

class Booking {
    int roomNumber;
    String customerName;
    String date;

    Booking(int roomNumber, String customerName, String date) {
        this.roomNumber = roomNumber;
        this.customerName = customerName;
        this.date = date;
    }

    @Override
    public String toString() {
        return "Room " + roomNumber + " booked by " + customerName + " on " + date;
    }
}

public class HotelReservationSystem {
    private static Scanner sc = new Scanner(System.in);
    private static List<Room> rooms = new ArrayList<>();
    private static List<Booking> bookings = new ArrayList<>();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n=== HOTEL RESERVATION SYSTEM ===");
            System.out.println("1. Add Room");
            System.out.println("2. View Rooms");
            System.out.println("3. Book Room");
            System.out.println("4. View Bookings");
            System.out.println("5. Cancel Booking");
            System.out.println("6. Search Room by Type");
            System.out.println("7. Exit");
            System.out.print("Choose: ");
            int ch = sc.nextInt();
            sc.nextLine();

            switch (ch) {
                case 1: addRoom(); break;
                case 2: viewRooms(); break;
                case 3: bookRoom(); break;
                case 4: viewBookings(); break;
                case 5: cancelBooking(); break;
                case 6: searchRoom(); break;
                case 7: System.out.println("Goodbye!"); return;
                default: System.out.println("Invalid choice!");
            }
        }
    }

    private static void addRoom() {
        System.out.print("Room Number: ");
        int num = sc.nextInt();
        sc.nextLine();

        System.out.print("Room Type (Single/Double/Deluxe): ");
        String type = sc.nextLine();

        System.out.print("Price: ");
        double price = sc.nextDouble();

        rooms.add(new Room(num, type, price));
        System.out.println("Room added successfully!");
    }

    private static void viewRooms() {
        if (rooms.isEmpty()) {
            System.out.println("No rooms available.");
            return;
        }
        System.out.println("\n--- ROOM LIST ---");
        rooms.forEach(System.out::println);
    }

    private static void bookRoom() {
        System.out.print("Enter Room Number: ");
        int num = sc.nextInt();
        sc.nextLine();

        Room room = findRoom(num);
        if (room == null) {
            System.out.println("Room not found!");
            return;
        }
        if (room.isBooked) {
            System.out.println("Room already booked!");
            return;
        }

        System.out.print("Enter Customer Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Booking Date (DD/MM/YYYY): ");
        String date = sc.nextLine();

        room.isBooked = true;
        bookings.add(new Booking(num, name, date));

        System.out.println("Room booked successfully!");
    }

    private static void viewBookings() {
        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }
        System.out.println("\n--- BOOKING LIST ---");
        bookings.forEach(System.out::println);
    }

    private static void cancelBooking() {
        System.out.print("Enter Room Number to cancel booking: ");
        int num = sc.nextInt();
        sc.nextLine();

        Booking b = null;
        for (Booking bk : bookings) {
            if (bk.roomNumber == num) {
                b = bk;
                break;
            }
        }

        if (b == null) {
            System.out.println("Booking not found!");
            return;
        }

        bookings.remove(b);
        findRoom(num).isBooked = false;
        System.out.println("Booking cancelled.");
    }

    private static void searchRoom() {
        System.out.print("Enter room type (Single/Double/Deluxe): ");
        String type = sc.nextLine();

        boolean found = false;
        for (Room room : rooms) {
            if (room.type.equalsIgnoreCase(type) && !room.isBooked) {
                System.out.println(room);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No available rooms of this type.");
        }
    }

    private static Room findRoom(int num) {
        for (Room r : rooms) {
            if (r.roomNumber == num) return r;
        }
        return null;
    }
}
