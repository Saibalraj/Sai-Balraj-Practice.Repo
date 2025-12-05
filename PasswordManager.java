import java.io.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class PasswordManager {

    // ---------------- Encryption utilities ----------------

    private static final String FILE_NAME = "passwords.dat";
    private static final SecureRandom random = new SecureRandom();
    private static String masterPassword;
    private static long lastActivity = System.currentTimeMillis();

    private static ScheduledExecutorService autoLockService;

    private static class Entry {
        String site;
        String username;
        String password;
        String notes;

        Entry(String s, String u, String p, String n) {
            site = s;
            username = u;
            password = p;
            notes = n;
        }

        public String toString() {
            return "Site: " + site + "\nUser: " + username + "\nPass: " + password + "\nNotes: " + notes;
        }
    }

    private static List<Entry> entries = new ArrayList<>();

    // ----------- Encryption methods -----------

    private static byte[] encrypt(String plain, String pass) throws Exception {
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        SecretKeySpec key = deriveKey(pass, salt);

        byte[] iv = new byte[16];
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

        byte[] enc = cipher.doFinal(plain.getBytes("UTF-8"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(salt);
        out.write(iv);
        out.write(enc);
        return out.toByteArray();
    }

    private static String decrypt(byte[] data, String pass) throws Exception {
        byte[] salt = Arrays.copyOfRange(data, 0, 16);
        byte[] iv = Arrays.copyOfRange(data, 16, 32);
        byte[] enc = Arrays.copyOfRange(data, 32, data.length);

        SecretKeySpec key = deriveKey(pass, salt);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

        byte[] dec = cipher.doFinal(enc);
        return new String(dec, "UTF-8");
    }

    private static SecretKeySpec deriveKey(String pass, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(pass.toCharArray(), salt, 65536, 256);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return new SecretKeySpec(skf.generateSecret(spec).getEncoded(), "AES");
    }

    // ---------------- Storage ----------------

    private static void saveData() {
        try (FileOutputStream fos = new FileOutputStream(FILE_NAME)) {
            StringBuilder sb = new StringBuilder();
            for (Entry e : entries) {
                sb.append(e.site).append("||")
                  .append(e.username).append("||")
                  .append(e.password).append("||")
                  .append(e.notes).append("\n");
            }
            byte[] enc = encrypt(sb.toString(), masterPassword);
            fos.write(enc);
        } catch (Exception e) {
            System.out.println("Error saving: " + e.getMessage());
        }
    }

    private static void loadData() {
        File f = new File(FILE_NAME);
        if (!f.exists()) return;

        try {
            byte[] fileData = new byte[(int) f.length()];
            FileInputStream fis = new FileInputStream(f);
            fis.read(fileData);
            fis.close();

            String dec = decrypt(fileData, masterPassword);

            entries.clear();
            for (String line : dec.split("\n")) {
                if (!line.trim().isEmpty()) {
                    String[] p = line.split("\\|\\|");
                    entries.add(new Entry(p[0], p[1], p[2], p.length > 3 ? p[3] : ""));
                }
            }
        } catch (Exception e) {
            System.out.println("Wrong master password!");
            System.exit(0);
        }
    }

    // ---------------- CSV Export/Import ----------------

    private static void exportCSV() {
        try (PrintWriter pw = new PrintWriter("passwords.csv")) {
            pw.println("Site,Username,Password,Notes");
            for (Entry e : entries) {
                pw.printf("%s,%s,%s,%s\n", e.site, e.username, e.password, e.notes);
            }
            System.out.println("Exported to passwords.csv");
        } catch (Exception e) {
            System.out.println("Export error: " + e.getMessage());
        }
    }

    private static void importCSV() {
        try (BufferedReader br = new BufferedReader(new FileReader("passwords.csv"))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 3)
                    entries.add(new Entry(p[0], p[1], p[2], p.length >= 4 ? p[3] : ""));
            }
            saveData();
            System.out.println("Import completed.");
        } catch (Exception e) {
            System.out.println("Import error: " + e.getMessage());
        }
    }

    // ---------------- Auto-lock ----------------

    private static void startAutoLock() {
        autoLockService = Executors.newSingleThreadScheduledExecutor();
        autoLockService.scheduleAtFixedRate(() -> {
            if (System.currentTimeMillis() - lastActivity > 60000) {
                System.out.println("\n⚠ Auto-locked due to inactivity!");
                System.exit(0);
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    private static void activity() {
        lastActivity = System.currentTimeMillis();
    }

    // ---------------- Menu functions ----------------

    private static void addEntry(Scanner sc) {
        System.out.print("Site: ");
        String site = sc.nextLine();
        System.out.print("Username: ");
        String user = sc.nextLine();
        System.out.print("Password: ");
        String pass = sc.nextLine();
        System.out.print("Notes: ");
        String notes = sc.nextLine();

        entries.add(new Entry(site, user, pass, notes));
        saveData();
        System.out.println("Added!");
    }

    private static void viewAll() {
        for (int i = 0; i < entries.size(); i++) {
            System.out.println("\nID: " + i);
            System.out.println(entries.get(i));
        }
    }

    private static void search(Scanner sc) {
        System.out.print("Search keyword: ");
        String k = sc.nextLine().toLowerCase();

        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            if (e.site.toLowerCase().contains(k) ||
                e.username.toLowerCase().contains(k) ||
                e.notes.toLowerCase().contains(k)) {

                System.out.println("\nID: " + i);
                System.out.println(e);
            }
        }
    }

    private static void deleteEntry(Scanner sc) {
        System.out.print("Enter ID to delete: ");
        int id = Integer.parseInt(sc.nextLine());
        if (id >= 0 && id < entries.size()) {
            entries.remove(id);
            saveData();
            System.out.println("Deleted!");
        }
    }

    private static void updateEntry(Scanner sc) {
        System.out.print("ID to update: ");
        int id = Integer.parseInt(sc.nextLine());
        if (id < 0 || id >= entries.size()) return;

        Entry e = entries.get(id);

        System.out.print("New site (" + e.site + "): ");
        String s = sc.nextLine();
        if (!s.isEmpty()) e.site = s;

        System.out.print("New username (" + e.username + "): ");
        s = sc.nextLine();
        if (!s.isEmpty()) e.username = s;

        System.out.print("New password (" + e.password + "): ");
        s = sc.nextLine();
        if (!s.isEmpty()) e.password = s;

        System.out.print("New notes (" + e.notes + "): ");
        s = sc.nextLine();
        if (!s.isEmpty()) e.notes = s;

        saveData();
        System.out.println("Updated!");
    }

    // ---------------- Main Program ----------------

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter master password: ");
        masterPassword = sc.nextLine();

        loadData();
        startAutoLock();

        while (true) {
            activity();
            System.out.println("\n------ PASSWORD MANAGER ------");
            System.out.println("1. Add Entry");
            System.out.println("2. View All");
            System.out.println("3. Search");
            System.out.println("4. Update Entry");
            System.out.println("5. Delete Entry");
            System.out.println("6. Export CSV");
            System.out.println("7. Import CSV");
            System.out.println("8. Exit");
            System.out.print("Choose: ");

            String op = sc.nextLine();
            activity();

            switch (op) {
                case "1": addEntry(sc); break;
                case "2": viewAll(); break;
                case "3": search(sc); break;
                case "4": updateEntry(sc); break;
                case "5": deleteEntry(sc); break;
                case "6": exportCSV(); break;
                case "7": importCSV(); break;
                case "8": System.exit(0);
                default: System.out.println("Invalid.");
            }
        }
    }
}
