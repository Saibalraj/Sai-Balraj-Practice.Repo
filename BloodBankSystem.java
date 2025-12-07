import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class BloodBankSystem {

    // Files (created in working directory)
    private static final Path DONORS_CSV = Paths.get("donors.csv");
    private static final Path INVENTORY_CSV = Paths.get("inventory.csv");
    private static final Path REQUESTS_CSV = Paths.get("requests.csv");

    // Date format
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Blood types
    private static final String[] BLOOD_TYPES = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

    // In-memory data
    private final List<Donor> donors = new ArrayList<>();
    private final Map<String, Integer> inventory = new TreeMap<>(); // blood type -> units
    private final List<Request> requests = new ArrayList<>();

    private final Scanner scanner = new Scanner(System.in);

    // Models
    static class Donor {
        String id;
        String name;
        String bloodType;
        int age;
        String contact;
        LocalDate lastDonation;

        Donor(String id, String name, String bloodType, int age, String contact, LocalDate lastDonation) {
            this.id = id; this.name = name; this.bloodType = bloodType; this.age = age; this.contact = contact; this.lastDonation = lastDonation;
        }
    }

    static class Request {
        String id;
        String requester;
        String bloodType;
        int units;
        String status; // Pending, Fulfilled, Cancelled

        Request(String id, String requester, String bloodType, int units, String status) {
            this.id = id; this.requester = requester; this.bloodType = bloodType; this.units = units; this.status = status;
        }
    }

    // Entry
    public static void main(String[] args) {
        new BloodBankSystem().start();
    }

    // Start app
    private void start() {
        initInventoryDefault();
        loadAllData();
        mainLoop();
    }

    // Initialize inventory map with zeros
    private void initInventoryDefault() {
        for (String bt : BLOOD_TYPES) inventory.put(bt, 0);
    }

    // Load all CSV files
    private void loadAllData() {
        loadDonors();
        loadInventory();
        loadRequests();
    }

    // Save all CSV files
    private void saveAllData() {
        saveDonors();
        saveInventory();
        saveRequests();
    }

    // ---------- Persistence ----------
    private void loadDonors() {
        donors.clear();
        if (!Files.exists(DONORS_CSV)) return;
        try {
            List<String> lines = Files.readAllLines(DONORS_CSV, StandardCharsets.UTF_8);
            for (String ln : lines) {
                if (ln.trim().isEmpty()) continue;
                // id,name,blood,age,contact,lastDonation
                String[] p = splitCsv(ln, 6);
                if (p.length < 6) continue;
                donors.add(new Donor(p[0], p[1], p[2], Integer.parseInt(p[3]), p[4], LocalDate.parse(p[5], DF)));
            }
        } catch (Exception e) {
            printlnErr("Failed to load donors: " + e.getMessage());
        }
    }

    private void saveDonors() {
        try {
            List<String> lines = donors.stream()
                    .map(d -> String.join(",", d.id, escape(d.name), d.bloodType, String.valueOf(d.age), escape(d.contact), d.lastDonation.format(DF)))
                    .collect(Collectors.toList());
            Files.write(DONORS_CSV, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            printlnErr("Failed to save donors: " + e.getMessage());
        }
    }

    private void loadInventory() {
        initInventoryDefault();
        if (!Files.exists(INVENTORY_CSV)) return;
        try {
            List<String> lines = Files.readAllLines(INVENTORY_CSV, StandardCharsets.UTF_8);
            for (String ln : lines) {
                if (ln.trim().isEmpty()) continue;
                String[] p = splitCsv(ln, 2);
                if (p.length < 2) continue;
                String type = p[0];
                int units = Integer.parseInt(p[1]);
                inventory.put(type, units);
            }
        } catch (Exception e) {
            printlnErr("Failed to load inventory: " + e.getMessage());
        }
    }

    private void saveInventory() {
        try {
            List<String> lines = inventory.entrySet().stream()
                    .map(e -> e.getKey() + "," + e.getValue())
                    .collect(Collectors.toList());
            Files.write(INVENTORY_CSV, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            printlnErr("Failed to save inventory: " + e.getMessage());
        }
    }

    private void loadRequests() {
        requests.clear();
        if (!Files.exists(REQUESTS_CSV)) return;
        try {
            List<String> lines = Files.readAllLines(REQUESTS_CSV, StandardCharsets.UTF_8);
            for (String ln : lines) {
                if (ln.trim().isEmpty()) continue;
                String[] p = splitCsv(ln, 5);
                if (p.length < 5) continue;
                requests.add(new Request(p[0], p[1], p[2], Integer.parseInt(p[3]), p[4]));
            }
        } catch (Exception e) {
            printlnErr("Failed to load requests: " + e.getMessage());
        }
    }

    private void saveRequests() {
        try {
            List<String> lines = requests.stream()
                    .map(r -> String.join(",", r.id, escape(r.requester), r.bloodType, String.valueOf(r.units), r.status))
                    .collect(Collectors.toList());
            Files.write(REQUESTS_CSV, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            printlnErr("Failed to save requests: " + e.getMessage());
        }
    }

    // Simple CSV split that handles quoted values (basic)
    private static String[] splitCsv(String line, int expected) {
        List<String> out = new ArrayList<>();
        boolean inQuote = false;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuote = !inQuote;
                continue;
            }
            if (c == ',' && !inQuote) {
                out.add(cur.toString());
                cur = new StringBuilder();
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    private static String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    // ---------- CLI ----------
    private void mainLoop() {
        boolean running = true;
        while (running) {
            println("");
            println("=== Blood Bank Management (CLI) ===");
            println("1) Donor Management");
            println("2) Inventory Management");
            println("3) Requests Management");
            println("4) Reports");
            println("5) Save Data");
            println("0) Exit");
            print("Choose > ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": donorMenu(); break;
                case "2": inventoryMenu(); break;
                case "3": requestsMenu(); break;
                case "4": reportsMenu(); break;
                case "5": saveAllData(); println("Data saved."); break;
                case "0": saveAllData(); println("Exiting. Data saved."); running = false; break;
                default: println("Invalid choice."); break;
            }
        }
    }

    // ---------- Donor Menu ----------
    private void donorMenu() {
        boolean back = false;
        while (!back) {
            println("");
            println("--- Donor Management ---");
            println("1) List Donors");
            println("2) Add Donor");
            println("3) Edit Donor");
            println("4) Delete Donor");
            println("5) Search Donors");
            println("0) Back");
            print("Choose > ");
            String ch = scanner.nextLine().trim();
            switch (ch) {
                case "1": listDonors(); break;
                case "2": addDonor(); break;
                case "3": editDonor(); break;
                case "4": deleteDonor(); break;
                case "5": searchDonors(); break;
                case "0": back = true; break;
                default: println("Invalid choice."); break;
            }
        }
    }

    private void listDonors() {
        println("");
        println("ID\tName\tBlood\tAge\tContact\tLastDonation");
        for (Donor d : donors) {
            println(String.join("\t", d.id, d.name, d.bloodType, String.valueOf(d.age), d.contact, d.lastDonation.format(DF)));
        }
        if (donors.isEmpty()) println("(no donors)");
    }

    private void addDonor() {
        println("");
        print("Name: "); String name = scanner.nextLine().trim();
        String blood = chooseBloodType();
        if (blood == null) return;
        print("Age: "); String ageStr = scanner.nextLine().trim();
        int age = parseInt(ageStr, -1);
        if (age <= 0) { println("Invalid age."); return; }
        print("Contact: "); String contact = scanner.nextLine().trim();
        print("Last donation date (yyyy-MM-dd) or blank for today: "); String dateStr = scanner.nextLine().trim();
        LocalDate ld = dateStr.isEmpty() ? LocalDate.now() : parseDate(dateStr);
        if (ld == null) { println("Invalid date."); return; }
        String id = "D" + (donors.size() + 1) + "_" + (System.currentTimeMillis() % 10000);
        donors.add(new Donor(id, name, blood, age, contact, ld));
        saveDonors();
        println("Donor added: " + id);
    }

    private void editDonor() {
        print("Enter Donor ID to edit: "); String id = scanner.nextLine().trim();
        Optional<Donor> od = donors.stream().filter(d -> d.id.equals(id)).findFirst();
        if (!od.isPresent()) { println("Donor not found."); return; }
        Donor d = od.get();
        println("Leave blank to keep current value.");
        print("Name (" + d.name + "): "); String name = scanner.nextLine().trim(); if (!name.isEmpty()) d.name = name;
        println("Blood Type (" + d.bloodType + "): ");
        String b = chooseBloodTypeAllowBlank();
        if (b != null) d.bloodType = b;
        print("Age (" + d.age + "): "); String ageStr = scanner.nextLine().trim(); if (!ageStr.isEmpty()) { int age = parseInt(ageStr, -1); if (age>0) d.age = age; else println("Invalid age ignored."); }
        print("Contact (" + d.contact + "): "); String contact = scanner.nextLine().trim(); if (!contact.isEmpty()) d.contact = contact;
        print("Last donation (" + d.lastDonation.format(DF) + "): "); String dateStr = scanner.nextLine().trim(); if (!dateStr.isEmpty()) { LocalDate ld = parseDate(dateStr); if (ld!=null) d.lastDonation = ld; else println("Invalid date ignored."); }
        saveDonors();
        println("Donor updated.");
    }

    private void deleteDonor() {
        print("Enter Donor ID to delete: "); String id = scanner.nextLine().trim();
        Optional<Donor> od = donors.stream().filter(d -> d.id.equals(id)).findFirst();
        if (!od.isPresent()) { println("Donor not found."); return; }
        print("Confirm delete donor " + od.get().name + " (y/n): "); String conf = scanner.nextLine().trim().toLowerCase();
        if ("y".equals(conf)) { donors.remove(od.get()); saveDonors(); println("Deleted."); } else println("Cancelled.");
    }

    private void searchDonors() {
        print("Search term (name or blood): "); String q = scanner.nextLine().trim().toLowerCase();
        List<Donor> res = donors.stream().filter(d -> d.name.toLowerCase().contains(q) || d.bloodType.toLowerCase().contains(q)).collect(Collectors.toList());
        if (res.isEmpty()) { println("No results."); return; }
        println("ID\tName\tBlood\tAge\tContact\tLastDonation");
        for (Donor d : res) println(String.join("\t", d.id, d.name, d.bloodType, String.valueOf(d.age), d.contact, d.lastDonation.format(DF)));
    }

    // ---------- Inventory Menu ----------
    private void inventoryMenu() {
        boolean back = false;
        while (!back) {
            println("");
            println("--- Inventory Management ---");
            println("1) View Inventory");
            println("2) Add Units");
            println("3) Remove Units");
            println("0) Back");
            print("Choose > ");
            String ch = scanner.nextLine().trim();
            switch (ch) {
                case "1": viewInventory(); break;
                case "2": modifyInventory(true); break;
                case "3": modifyInventory(false); break;
                case "0": back = true; break;
                default: println("Invalid choice."); break;
            }
        }
    }

    private void viewInventory() {
        println("");
        println("BloodType\tUnits");
        inventory.forEach((k,v) -> println(k + "\t" + v));
    }

    private void modifyInventory(boolean add) {
        String type = chooseBloodType();
        if (type == null) return;
        print("Units to " + (add ? "add" : "remove") + ": ");
        String unitsStr = scanner.nextLine().trim();
        int units = parseInt(unitsStr, -1);
        if (units <= 0) { println("Invalid units."); return; }
        int current = inventory.getOrDefault(type, 0);
        int updated = add ? current + units : current - units;
        if (updated < 0) { println("Insufficient units. Current: " + current); return; }
        inventory.put(type, updated);
        saveInventory();
        println("Inventory updated: " + type + " = " + updated);
    }

    // ---------- Requests Menu ----------
    private void requestsMenu() {
        boolean back = false;
        while (!back) {
            println("");
            println("--- Requests Management ---");
            println("1) List Requests");
            println("2) Create Request");
            println("3) Fulfill Request");
            println("4) Cancel Request");
            println("0) Back");
            print("Choose > ");
            String ch = scanner.nextLine().trim();
            switch (ch) {
                case "1": listRequests(); break;
                case "2": createRequest(); break;
                case "3": fulfillRequest(); break;
                case "4": cancelRequest(); break;
                case "0": back = true; break;
                default: println("Invalid choice."); break;
            }
        }
    }

    private void listRequests() {
        println("");
        println("ID\tRequester\tBlood\tUnits\tStatus");
        for (Request r : requests) println(String.join("\t", r.id, r.requester, r.bloodType, String.valueOf(r.units), r.status));
        if (requests.isEmpty()) println("(no requests)");
    }

    private void createRequest() {
        print("Requester name: "); String requester = scanner.nextLine().trim();
        if (requester.isEmpty()) { println("Requester required."); return; }
        String blood = chooseBloodType();
        if (blood == null) return;
        print("Units required: "); String unitsStr = scanner.nextLine().trim();
        int units = parseInt(unitsStr, -1);
        if (units <= 0) { println("Invalid units."); return; }
        String id = "R" + (requests.size() + 1) + "_" + (System.currentTimeMillis() % 10000);
        requests.add(new Request(id, requester, blood, units, "Pending"));
        saveRequests();
        println("Request created: " + id);
    }

    private void fulfillRequest() {
        print("Enter Request ID to fulfill: "); String id = scanner.nextLine().trim();
        Optional<Request> or = requests.stream().filter(r -> r.id.equals(id)).findFirst();
        if (!or.isPresent()) { println("Request not found."); return; }
        Request req = or.get();
        if (!"Pending".equals(req.status)) { println("Request is not pending (status=" + req.status + ")"); return; }
        int available = inventory.getOrDefault(req.bloodType, 0);
        if (available < req.units) { println("Insufficient inventory ("+available+" units)."); return; }
        inventory.put(req.bloodType, available - req.units);
        req.status = "Fulfilled";
        saveInventory();
        saveRequests();
        println("Request fulfilled and inventory updated.");
    }

    private void cancelRequest() {
        print("Enter Request ID to cancel: "); String id = scanner.nextLine().trim();
        Optional<Request> or = requests.stream().filter(r -> r.id.equals(id)).findFirst();
        if (!or.isPresent()) { println("Request not found."); return; }
        Request req = or.get();
        if ("Fulfilled".equals(req.status)) { println("Fulfilled request cannot be cancelled."); return; }
        req.status = "Cancelled";
        saveRequests();
        println("Request cancelled.");
    }

    // ---------- Reports ----------
    private void reportsMenu() {
        boolean back = false;
        while (!back) {
            println("");
            println("--- Reports ---");
            println("1) Quick Inventory Report (console)");
            println("2) Export donors CSV");
            println("3) Export requests CSV");
            println("0) Back");
            print("Choose > ");
            String ch = scanner.nextLine().trim();
            switch (ch) {
                case "1": showQuickReport(); break;
                case "2": exportDonorsCsv(); break;
                case "3": exportRequestsCsv(); break;
                case "0": back = true; break;
                default: println("Invalid choice."); break;
            }
        }
    }

    private void showQuickReport() {
        println("");
        println("=== Quick Report ===");
        println("-- Inventory --");
        inventory.forEach((k,v) -> println(String.format("%s : %d", k, v)));
        println("");
        println("-- Donors (" + donors.size() + ") --");
        donors.stream().limit(20).forEach(d -> println(String.format("%s | %s | %s | %d | %s", d.id, d.name, d.bloodType, d.age, d.lastDonation.format(DF))));
        println("");
        println("-- Requests (" + requests.size() + ") --");
        requests.stream().limit(20).forEach(r -> println(String.format("%s | %s | %s | %d | %s", r.id, r.requester, r.bloodType, r.units, r.status)));
    }

    private void exportDonorsCsv() {
        print("Enter filename to export donors (e.g. donors_export.csv): ");
        String file = scanner.nextLine().trim();
        if (file.isEmpty()) { println("Cancelled."); return; }
        Path out = Paths.get(file);
        try {
            List<String> lines = donors.stream()
                    .map(d -> String.join(",", d.id, escape(d.name), d.bloodType, String.valueOf(d.age), d.contact, d.lastDonation.format(DF)))
                    .collect(Collectors.toList());
            Files.write(out, lines, StandardCharsets.UTF_8);
            println("Exported donors to " + out.toAbsolutePath());
        } catch (Exception e) { printlnErr("Export failed: " + e.getMessage()); }
    }

    private void exportRequestsCsv() {
        print("Enter filename to export requests (e.g. requests_export.csv): ");
        String file = scanner.nextLine().trim();
        if (file.isEmpty()) { println("Cancelled."); return; }
        Path out = Paths.get(file);
        try {
            List<String> lines = requests.stream()
                    .map(r -> String.join(",", r.id, escape(r.requester), r.bloodType, String.valueOf(r.units), r.status))
                    .collect(Collectors.toList());
            Files.write(out, lines, StandardCharsets.UTF_8);
            println("Exported requests to " + out.toAbsolutePath());
        } catch (Exception e) { printlnErr("Export failed: " + e.getMessage()); }
    }

    // ---------- Helpers ----------
    private String chooseBloodType() {
        println("Choose blood type:");
        for (int i = 0; i < BLOOD_TYPES.length; i++) println((i+1) + ") " + BLOOD_TYPES[i]);
        print("Select number (or 0 to cancel): ");
        String sel = scanner.nextLine().trim();
        int idx = parseInt(sel, -1);
        if (idx <= 0 || idx > BLOOD_TYPES.length) { println("Cancelled."); return null; }
        return BLOOD_TYPES[idx-1];
    }

    private String chooseBloodTypeAllowBlank() {
        println("Choose blood type (or blank to keep current):");
        for (int i = 0; i < BLOOD_TYPES.length; i++) println((i+1) + ") " + BLOOD_TYPES[i]);
        print("Select number or press Enter: ");
        String sel = scanner.nextLine().trim();
        if (sel.isEmpty()) return null;
        int idx = parseInt(sel, -1);
        if (idx <= 0 || idx > BLOOD_TYPES.length) { println("Invalid selection - ignored."); return null; }
        return BLOOD_TYPES[idx-1];
    }

    private int parseInt(String s, int fallback) {
        try { return Integer.parseInt(s); } catch (Exception e) { return fallback; }
    }

    private LocalDate parseDate(String s) {
        try { return LocalDate.parse(s, DF); } catch (Exception e) { return null; }
    }

    private void println(String s) { System.out.println(s); }
    private void print(String s) { System.out.print(s); }
    private void printlnErr(String s) { System.err.println("[ERROR] " + s); }

}
