import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

public class CurrencyConverterApp {

    private static final Scanner sc = new Scanner(System.in);

    // In-memory user DB (simple file persistence)
    private static final Map<String, String> users = new HashMap<>();

    // Offline fallback rates keyed "FROM_TO"
    private static final Map<String, Double> offlineRates = new HashMap<>();

    // Conversion history lines
    private static final List<String> conversionHistory = new ArrayList<>();

    private static String currentUser = null;

    public static void main(String[] args) {
        initializeOfflineRates();
        loadUsers();

        while (true) {
            clear();
            System.out.println("===========================================");
            System.out.println("        CURRENCY CONVERTER SYSTEM          ");
            System.out.println("===========================================\n");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("\nEnter choice: ");

            int ch = getInt();
            if (ch == 1) login();
            else if (ch == 2) register();
            else if (ch == 3) {
                System.out.println("Goodbye!");
                return;
            } else {
                System.out.println("Invalid choice. Press Enter.");
                pause();
            }
        }
    }

    /* ---------------- User management ---------------- */

    private static void register() {
        clear();
        System.out.println("=== Register ===");
        System.out.print("New username: ");
        String user = sc.nextLine().trim();
        if (user.isEmpty()) { System.out.println("Username required."); pause(); return; }

        System.out.print("New password: ");
        String pass = sc.nextLine();
        if (users.containsKey(user)) {
            System.out.println("Username already exists.");
        } else {
            users.put(user, pass);
            saveUsers();
            System.out.println("Registered successfully.");
        }
        pause();
    }

    private static void login() {
        clear();
        System.out.println("=== Login ===");
        System.out.print("Username: ");
        String user = sc.nextLine().trim();
        System.out.print("Password: ");
        String pass = sc.nextLine();

        if (users.containsKey(user) && users.get(user).equals(pass)) {
            currentUser = user;
            System.out.println("Login successful. Welcome " + user + "!");
            pause();
            mainMenu();
        } else {
            // default admin fallback
            if ("admin".equals(user) && "admin123".equals(pass)) {
                users.put("admin", "admin123");
                saveUsers();
                currentUser = "admin";
                System.out.println("Admin login successful.");
                pause();
                mainMenu();
            } else {
                System.out.println("Invalid credentials.");
                pause();
            }
        }
    }

    /* ---------------- Main menu ---------------- */

    private static void mainMenu() {
        while (currentUser != null) {
            clear();
            System.out.println("===========================================");
            System.out.println("                 MAIN MENU                 ");
            System.out.println("User: " + currentUser);
            System.out.println("===========================================\n");
            System.out.println("1. Convert Currency (Live API)");
            System.out.println("2. Convert Currency (Offline Rates)");
            System.out.println("3. Historical Conversion (date)");
            System.out.println("4. Import rates from CSV");
            System.out.println("5. Export conversion history (CSV)");
            System.out.println("6. Export conversion history (HTML)");
            System.out.println("7. Show conversion history");
            System.out.println("8. Logout");
            System.out.print("\nEnter choice: ");

            int ch = getInt();
            switch (ch) {
                case 1 -> liveConvert();
                case 2 -> offlineConvert();
                case 3 -> historicalConvert();
                case 4 -> importCSV();
                case 5 -> exportCSV();
                case 6 -> exportHTML();
                case 7 -> showHistory();
                case 8 -> { currentUser = null; System.out.println("Logged out."); pause(); }
                default -> { System.out.println("Invalid choice."); pause(); }
            }
        }
    }

    /* ---------------- Conversion: Live / Offline / Historical ---------------- */

    private static void liveConvert() {
        clear();
        System.out.println("=== Live Conversion ===");
        System.out.print("Base currency (e.g. USD): ");
        String from = sc.nextLine().trim().toUpperCase();
        System.out.print("Target currency (e.g. INR): ");
        String to = sc.nextLine().trim().toUpperCase();
        System.out.print("Amount: ");
        double amount = getDouble();

        if (amount < 0) { System.out.println("Invalid amount."); pause(); return; }

        System.out.println("Fetching latest rate...");
        String url = "https://api.exchangerate.host/latest?base=" + urlEncode(from) + "&symbols=" + urlEncode(to);
        String json = fetchJSON(url);
        if (json == null) {
            System.out.println("API failed or no network. Try offline conversion.");
            pause();
            return;
        }

        Double rate = extractRateFromJson(json, to);
        if (rate == null) {
            System.out.println("Failed to parse rate from API response.");
            pause();
            return;
        }

        double result = amount * rate;
        System.out.printf("1 %s = %.6f %s\n", from, rate, to);
        System.out.printf("Converted: %.4f %s = %.4f %s\n", amount, from, result, to);

        addHistory(from, to, amount, rate, result, "LIVE");
        pause();
    }

    private static void offlineConvert() {
        clear();
        System.out.println("=== Offline Conversion ===");
        System.out.print("Base currency: ");
        String from = sc.nextLine().trim().toUpperCase();
        System.out.print("Target currency: ");
        String to = sc.nextLine().trim().toUpperCase();
        System.out.print("Amount: ");
        double amount = getDouble();
        if (amount < 0) { System.out.println("Invalid amount."); pause(); return; }

        String key = from + "_" + to;
        if (!offlineRates.containsKey(key)) {
            System.out.println("Offline rate not found for " + key);
            pause();
            return;
        }
        double rate = offlineRates.get(key);
        double result = amount * rate;
        System.out.printf("Offline: 1 %s = %.6f %s\n", from, rate, to);
        System.out.printf("Converted: %.4f %s = %.4f %s\n", amount, from, result, to);
        addHistory(from, to, amount, rate, result, "OFFLINE");
        pause();
    }

    private static void historicalConvert() {
        clear();
        System.out.println("=== Historical Conversion ===");
        System.out.print("Date (YYYY-MM-DD): ");
        String date = sc.nextLine().trim();
        if (!isValidDate(date)) { System.out.println("Invalid date format."); pause(); return; }

        System.out.print("Base currency: ");
        String from = sc.nextLine().trim().toUpperCase();
        System.out.print("Target currency: ");
        String to = sc.nextLine().trim().toUpperCase();
        System.out.print("Amount: ");
        double amount = getDouble();
        if (amount < 0) { System.out.println("Invalid amount."); pause(); return; }

        System.out.println("Fetching historical rate...");
        String url = "https://api.exchangerate.host/" + urlEncode(date) + "?base=" + urlEncode(from) + "&symbols=" + urlEncode(to);
        String json = fetchJSON(url);
        if (json == null) {
            System.out.println("API failed.");
            pause();
            return;
        }
        Double rate = extractRateFromJson(json, to);
        if (rate == null) {
            System.out.println("Failed to parse rate.");
            pause();
            return;
        }
        double result = amount * rate;
        System.out.printf("On %s: 1 %s = %.6f %s\n", date, from, rate, to);
        System.out.printf("Converted: %.4f %s = %.4f %s\n", amount, from, result, to);
        addHistory(from, to, amount, rate, result, "HIST " + date);
        pause();
    }

    /* ---------------- Import / Export / History ---------------- */

    private static void importCSV() {
        clear();
        System.out.println("=== Import rates from CSV ===");
        System.out.println("CSV format: FROM,TO,RATE (one per line)");
        System.out.print("Enter path to CSV: ");
        String path = sc.nextLine().trim();
        try {
            List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
            int count = 0;
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(",");
                if (p.length < 3) continue;
                String from = p[0].trim().toUpperCase();
                String to = p[1].trim().toUpperCase();
                double rate = Double.parseDouble(p[2].trim());
                offlineRates.put(from + "_" + to, rate);
                count++;
            }
            System.out.println("Imported " + count + " rates.");
        } catch (Exception e) {
            System.out.println("Import failed: " + e.getMessage());
        }
        pause();
    }

    private static void exportCSV() {
        clear();
        System.out.println("=== Export conversion history (CSV) ===");
        if (conversionHistory.isEmpty()) {
            System.out.println("No history to export.");
            pause();
            return;
        }
        String file = "conversion_history_" + timestamp() + ".csv";
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(Paths.get(file), StandardCharsets.UTF_8))) {
            pw.println("timestamp,type,base,target,amount,rate,result");
            for (String line : conversionHistory) {
                // internal format: timestamp | TYPE | amount BASE -> result TARGET (rate RATE)
                // we'll store as the raw line for simplicity but also include commas replaced
                pw.println(escapeCsv(line));
            }
            System.out.println("Exported to " + file);
        } catch (Exception e) {
            System.out.println("Export failed: " + e.getMessage());
        }
        pause();
    }

    private static void exportHTML() {
        clear();
        System.out.println("=== Export conversion history (HTML) ===");
        if (conversionHistory.isEmpty()) {
            System.out.println("No history to export.");
            pause();
            return;
        }
        String file = "conversion_history_" + timestamp() + ".html";
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(file), StandardCharsets.UTF_8)) {
            bw.write("<!doctype html>\n<html><head><meta charset='utf-8'><title>Conversion History</title></head><body>");
            bw.write("<h2>Conversion History</h2>\n<ul>\n");
            for (String line : conversionHistory) {
                bw.write("<li>" + htmlEscape(line) + "</li>\n");
            }
            bw.write("</ul>\n</body></html>");
            System.out.println("Exported to " + file);
        } catch (Exception e) {
            System.out.println("Export failed: " + e.getMessage());
        }
        pause();
    }

    private static void showHistory() {
        clear();
        System.out.println("=== Conversion History ===");
        if (conversionHistory.isEmpty()) {
            System.out.println("No entries yet.");
        } else {
            for (String line : conversionHistory) System.out.println(line);
        }
        pause();
    }

    /* ---------------- Helpers / Persistence ---------------- */

    private static void addHistory(String from, String to, double amount, double rate, double result, String type) {
        String ts = timestamp();
        String entry = String.format("%s | %s | %.4f %s -> %.4f %s | rate=%.6f", ts, type, amount, from, result, to, rate);
        conversionHistory.add(entry);
    }

    private static void loadUsers() {
        Path p = Paths.get("users.db");
        if (!Files.exists(p)) return;
        try {
            List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
            for (String ln : lines) {
                if (ln.trim().isEmpty()) continue;
                String[] parts = ln.split(":", 2);
                if (parts.length == 2) users.put(parts[0], parts[1]);
            }
        } catch (Exception ignored) {}
    }

    private static void saveUsers() {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get("users.db"), StandardCharsets.UTF_8)) {
            for (Map.Entry<String, String> e : users.entrySet()) {
                bw.write(e.getKey() + ":" + e.getValue());
                bw.newLine();
            }
        } catch (Exception ignored) {}
    }

    private static void initializeOfflineRates() {
        offlineRates.put("USD_INR", 83.20);
        offlineRates.put("EUR_USD", 1.09);
        offlineRates.put("GBP_USD", 1.27);
        offlineRates.put("USD_EUR", 0.92);
        offlineRates.put("INR_USD", 1.0/83.20);
    }

    /* ---------------- Networking & JSON minimal parsing ---------------- */

    private static String fetchJSON(String urlStr) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(8000);
            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            if (is == null) return null;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                return sb.toString();
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /**
     * Minimal rate extractor from API JSON response.
     * Looks for: "rates":{... "TO":value ...}
     * returns Double or null on failure.
     */
    private static Double extractRateFromJson(String json, String toCurrency) {
        if (json == null || toCurrency == null) return null;
        // Regex: "TO"\s*:\s*([0-9]+(\.[0-9]+)?)
        String patternStr = "\"" + Pattern.quote(toCurrency) + "\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)";
        Pattern p = Pattern.compile(patternStr);
        Matcher m = p.matcher(json);
        if (m.find()) {
            try { return Double.parseDouble(m.group(1)); }
            catch (NumberFormatException ex) { return null; }
        }
        // fallback: some APIs might return lowercase keys or without quotes - attempt loose parse
        patternStr = toCurrency + "\"?\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)";
        p = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
        m = p.matcher(json);
        if (m.find()) {
            try { return Double.parseDouble(m.group(1)); }
            catch (NumberFormatException ex) { return null; }
        }
        return null;
    }

    /* ---------------- Utility small helpers ---------------- */

    private static int getInt() {
        try { return Integer.parseInt(sc.nextLine().trim()); }
        catch (Exception e) { return -1; }
    }

    private static double getDouble() {
        try { return Double.parseDouble(sc.nextLine().trim()); }
        catch (Exception e) { return -1.0; }
    }

    private static void pause() {
        System.out.println("\n(Press Enter to continue)");
        sc.nextLine();
    }

    private static void clear() {
        // attempt ANSI clear; if not supported just print some newlines
        try { final String os = System.getProperty("os.name"); if (os.contains("Windows")) { new ProcessBuilder("cmd","/c","cls").inheritIO().start().waitFor(); } else { System.out.print("\033[H\033[2J"); System.out.flush(); } }
        catch (Exception ignored) { for (int i=0;i<30;i++) System.out.println(); }
    }

    private static String timestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

    private static boolean isValidDate(String s) {
        if (s == null) return false;
        try {
            new SimpleDateFormat("yyyy-MM-dd").parse(s);
            return true;
        } catch (Exception e) { return false; }
    }

    private static String urlEncode(String s) {
        try { return URLEncoder.encode(s, StandardCharsets.UTF_8.toString()); } catch (Exception e) { return s; }
    }

    private static String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) return "\"" + s.replace("\"","\"\"") + "\"";
        return s;
    }

    private static String htmlEscape(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
    }
}
