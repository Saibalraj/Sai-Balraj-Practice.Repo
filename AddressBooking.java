import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class AddressBooking {

    private static final Path DATA_FILE = Paths.get("contacts.csv");
    private final List<Contact> contacts = new ArrayList<>();
    private final Scanner scanner = new Scanner(System.in);

    private static class Contact {
        String id;
        String name;
        String phone;
        String email;
        String address;

        Contact(String id, String name, String phone, String email, String address) {
            this.id = id;
            this.name = name;
            this.phone = phone;
            this.email = email;
            this.address = address;
        }
    }

    public AddressBooking() {
        loadFromFile();
        mainLoop();
    }

    private void mainLoop() {
        while (true) {
            System.out.println("\n=== Address Book Menu ===");
            System.out.println("1. List contacts");
            System.out.println("2. Add contact");
            System.out.println("3. Edit contact");
            System.out.println("4. Delete contact");
            System.out.println("5. Search contacts");
            System.out.println("6. Import CSV");
            System.out.println("7. Export CSV");
            System.out.println("0. Exit");
            System.out.print("Choose option: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": listContacts(); break;
                case "2": addContact(); break;
                case "3": editContact(); break;
                case "4": deleteContact(); break;
                case "5": searchContacts(); break;
                case "6": importCsv(); break;
                case "7": exportCsv(); break;
                case "0": System.out.println("Exiting..."); return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    // ========================
    // CRUD Operations
    // ========================

    private void listContacts() {
        if (contacts.isEmpty()) {
            System.out.println("No contacts found.");
            return;
        }
        System.out.println("\nID\tName\tPhone\tEmail\tAddress");
        for (Contact c : contacts) {
            System.out.printf("%s\t%s\t%s\t%s\t%s%n", c.id, c.name, c.phone, c.email, c.address);
        }
    }

    private void addContact() {
        System.out.print("Name: "); String name = scanner.nextLine().trim();
        System.out.print("Phone: "); String phone = scanner.nextLine().trim();
        System.out.print("Email: "); String email = scanner.nextLine().trim();
        System.out.print("Address: "); String address = scanner.nextLine().trim();

        if (name.isEmpty() && phone.isEmpty()) {
            System.out.println("Name or Phone is required.");
            return;
        }

        String id = generateId();
        contacts.add(new Contact(id, name, phone, email, address));
        saveToFile();
        System.out.println("Contact added.");
    }

    private void editContact() {
        System.out.print("Enter Contact ID to edit: ");
        String id = scanner.nextLine().trim();
        Contact c = findById(id);
        if (c == null) { System.out.println("Contact not found."); return; }

        System.out.print("New Name (" + c.name + "): "); String name = scanner.nextLine().trim();
        System.out.print("New Phone (" + c.phone + "): "); String phone = scanner.nextLine().trim();
        System.out.print("New Email (" + c.email + "): "); String email = scanner.nextLine().trim();
        System.out.print("New Address (" + c.address + "): "); String address = scanner.nextLine().trim();

        if (!name.isEmpty()) c.name = name;
        if (!phone.isEmpty()) c.phone = phone;
        if (!email.isEmpty()) c.email = email;
        if (!address.isEmpty()) c.address = address;

        saveToFile();
        System.out.println("Contact updated.");
    }

    private void deleteContact() {
        System.out.print("Enter Contact ID to delete: ");
        String id = scanner.nextLine().trim();
        Contact c = findById(id);
        if (c == null) { System.out.println("Contact not found."); return; }

        System.out.print("Are you sure you want to delete " + c.name + "? (y/n): ");
        String ans = scanner.nextLine().trim().toLowerCase();
        if (ans.equals("y")) {
            contacts.remove(c);
            saveToFile();
            System.out.println("Contact deleted.");
        }
    }

    private void searchContacts() {
        System.out.print("Enter search query: ");
        String q = scanner.nextLine().trim().toLowerCase();
        List<Contact> result = contacts.stream()
                .filter(c -> (c.name != null && c.name.toLowerCase().contains(q)) ||
                             (c.phone != null && c.phone.toLowerCase().contains(q)) ||
                             (c.email != null && c.email.toLowerCase().contains(q)) ||
                             (c.address != null && c.address.toLowerCase().contains(q)))
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            System.out.println("No contacts found.");
            return;
        }

        System.out.println("\nID\tName\tPhone\tEmail\tAddress");
        for (Contact c : result) {
            System.out.printf("%s\t%s\t%s\t%s\t%s%n", c.id, c.name, c.phone, c.email, c.address);
        }
    }

    // ========================
    // FILE OPERATIONS
    // ========================

    private void saveToFile() {
        try {
            List<String> lines = new ArrayList<>();
            lines.add("id,name,phone,email,address");
            for (Contact c : contacts) {
                lines.add(csvEscape(c.id) + "," + csvEscape(c.name) + "," +
                        csvEscape(c.phone) + "," + csvEscape(c.email) + "," + csvEscape(c.address));
            }
            Files.write(DATA_FILE, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            System.out.println("Failed to save: " + e.getMessage());
        }
    }

    private void loadFromFile() {
        contacts.clear();
        if (!Files.exists(DATA_FILE)) return;
        try {
            List<String> lines = Files.readAllLines(DATA_FILE, StandardCharsets.UTF_8);
            boolean first = true;
            for (String ln : lines) {
                if (first && ln.toLowerCase().startsWith("id,")) { first = false; continue; }
                first = false;
                String[] parts = csvSplit(ln, 5);
                if (parts.length < 5) continue;
                contacts.add(new Contact(parts[0], parts[1], parts[2], parts[3], parts[4]));
            }
        } catch (Exception e) {
            System.out.println("Failed to load contacts: " + e.getMessage());
        }
    }

    private void importCsv() {
        System.out.print("Enter CSV file path to import: ");
        String path = scanner.nextLine().trim();
        File f = new File(path);
        if (!f.exists()) { System.out.println("File not found."); return; }

        try {
            List<String> lines = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
            boolean first = true;
            int added = 0;

            for (String ln : lines) {
                if (first && ln.toLowerCase().startsWith("id,")) { first = false; continue; }
                first = false;

                String[] parts = csvSplit(ln, 5);
                if (parts.length < 5) continue;

                String id = parts[0].isEmpty() ? generateId() : parts[0];
                while (idExists(id)) id = generateId();

                contacts.add(new Contact(id, parts[1], parts[2], parts[3], parts[4]));
                added++;
            }

            if (added > 0) {
                saveToFile();
                System.out.println("Imported " + added + " contacts.");
            } else {
                System.out.println("No contacts found in file.");
            }
        } catch (Exception e) {
            System.out.println("Import failed: " + e.getMessage());
        }
    }

    private void exportCsv() {
        System.out.print("Enter CSV file path to export: ");
        String path = scanner.nextLine().trim();
        File f = new File(path);

        try {
            List<String> lines = new ArrayList<>();
            lines.add("id,name,phone,email,address");
            for (Contact c : contacts) {
                lines.add(csvEscape(c.id) + "," + csvEscape(c.name) + "," +
                        csvEscape(c.phone) + "," + csvEscape(c.email) + "," + csvEscape(c.address));
            }
            Files.write(f.toPath(), lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Exported to " + f.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }

    // ========================
    // UTILITIES
    // ========================

    private Contact findById(String id) {
        for (Contact c : contacts) if (c.id.equals(id)) return c;
        return null;
    }

    private boolean idExists(String id) {
        for (Contact c : contacts) if (c.id.equals(id)) return true;
        return false;
    }

    private String generateId() {
        return "C" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    private static String csvEscape(String s) {
        if (s == null) return "";
        String t = s.replace("\"", "\"\"");
        if (t.contains(",") || t.contains("\"") || t.contains("\n") || t.contains("\r")) {
            return "\"" + t + "\"";
        }
        return t;
    }

    private static String[] csvSplit(String line, int expectedColumns) {
        List<String> parts = new ArrayList<>(expectedColumns);
        if (line == null || line.isEmpty()) {
            for (int i = 0; i < expectedColumns; i++) parts.add("");
            return parts.toArray(new String[0]);
        }
        int len = line.length();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < len; i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < len && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++;
                } else inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                parts.add(cur.toString());
                cur.setLength(0);
            } else cur.append(ch);
        }
        parts.add(cur.toString());
        while (parts.size() < expectedColumns) parts.add("");
        return parts.toArray(new String[0]);
    }

    public static void main(String[] args) {
        new AddressBooking();
    }
}
