import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * OnlineQuizApp.java
 *
 * Terminal-based online quiz application.
 * - Admin mode: add / list / delete MCQs
 * - User mode: take quiz with timer, scoring
 * - Stores data in SQLite (if jdbc driver present) or falls back to CSV
 *
 * To enable SQLite DB persistence, put sqlite-jdbc jar on classpath (optional).
 *
 * Example compile / run (with sqlite jar):
 * javac -cp ".:sqlite-jdbc-3.40.1.0.jar" OnlineQuizApp.java
 * java -cp ".:sqlite-jdbc-3.40.1.0.jar" OnlineQuizApp
 */
public class OnlineQuizApp {
    private static final Scanner SC = new Scanner(System.in);
    private static final Path QUESTIONS_CSV = Paths.get("questions.csv");
    private static final Path RESULTS_CSV = Paths.get("results.csv");
    private static final String DB_FILE = "quiz.db";
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Storage mode flags
    private final boolean dbAvailable;
    private Connection conn = null;

    public OnlineQuizApp() {
        boolean dbok = false;
        try {
            // attempt to load SQLite driver
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DB_FILE;
            conn = DriverManager.getConnection(url);
            conn.setAutoCommit(true);
            ensureDbTables();
            dbok = true;
            System.out.println("[INFO] SQLite available — using DB file: " + DB_FILE);
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("[WARN] SQLite JDBC not available or DB error. Falling back to CSV files.");
            // e.printStackTrace();
            dbok = false;
            closeQuietly(conn);
            conn = null;
        }
        dbAvailable = dbok;
    }

    // === MAIN MENU ===
    public void run() {
        while (true) {
            println("\n=== Online Quiz Application ===");
            println("1) Admin");
            println("2) User (Take Quiz)");
            println("3) Exit");
            print("Choose option: ");
            String opt = SC.nextLine().trim();
            switch (opt) {
                case "1" -> adminMenu();
                case "2" -> userMenu();
                case "3" -> {
                    println("Exiting. Goodbye!");
                    closeQuietly(conn);
                    return;
                }
                default -> println("Invalid selection.");
            }
        }
    }

    // === ADMIN MENU ===
    private void adminMenu() {
        while (true) {
            println("\n--- ADMIN ---");
            println("1) Add question");
            println("2) List questions");
            println("3) Delete question by ID");
            println("4) Export questions to CSV");
            println("5) Back");
            print("Choose option: ");
            String opt = SC.nextLine().trim();
            switch (opt) {
                case "1" -> addQuestionInteractive();
                case "2" -> listQuestions();
                case "3" -> deleteQuestionInteractive();
                case "4" -> exportQuestionsCsv();
                case "5" -> { return; }
                default -> println("Invalid selection.");
            }
        }
    }

    // === USER MENU ===
    private void userMenu() {
        List<Question> questions = loadAllQuestions();
        if (questions.isEmpty()) {
            println("No questions available. Ask Admin to add some first.");
            return;
        }
        print("Enter your name: ");
        String username = SC.nextLine().trim();
        if (username.isEmpty()) username = "Anonymous";
        int totalQuestions = Math.min(questions.size(), askInt("How many questions for the quiz? (max " + questions.size() + "): ", 1, questions.size()));
        int timeLimitSec = askInt("Total time for quiz in seconds (e.g. 60): ", 10, 3600);

        // pick random subset
        Collections.shuffle(questions);
        List<Question> quiz = questions.subList(0, totalQuestions);

        // run timed quiz
        QuizResult result = runTimedQuiz(username, quiz, timeLimitSec);

        // store result
        boolean stored = storeResult(result);
        println("Result " + (stored ? "saved." : "not saved (CSV fallback may have been used)."));
        println("Score: " + result.score + " / " + result.totalQuestions + " (" + result.getPercent() + "%)");
    }

    // === Add question interactively ===
    private void addQuestionInteractive() {
        println("\nAdd new question (MCQ, 4 options).");
        print("Enter question text: ");
        String qText = SC.nextLine().trim();
        if (qText.isEmpty()) { println("Question text cannot be empty."); return; }

        String[] opts = new String[4];
        for (int i = 0; i < 4; ++i) {
            print("Option " + (i + 1) + ": ");
            opts[i] = SC.nextLine().trim();
        }
        int correctIndex = askInt("Enter correct option number (1-4): ", 1, 4) - 1;
        Question q = new Question(-1, qText, opts[0], opts[1], opts[2], opts[3], correctIndex);
        boolean ok = saveQuestion(q);
        println(ok ? "Question saved." : "Failed to save question.");
    }

    private void listQuestions() {
        List<Question> questions = loadAllQuestions();
        if (questions.isEmpty()) { println("No questions present."); return; }
        println("\nQuestions:");
        for (Question q : questions) {
            println(q.toDisplayString());
        }
    }

    private void deleteQuestionInteractive() {
        listQuestions();
        int id = askInt("Enter question ID to delete (or 0 to cancel): ", 0, Integer.MAX_VALUE);
        if (id == 0) { println("Cancelled."); return; }
        boolean ok = deleteQuestionById(id);
        println(ok ? "Deleted." : "Not found / failed.");
    }

    // Export questions to CSV file explicitly
    private void exportQuestionsCsv() {
        List<Question> questions = loadAllQuestions();
        if (questions.isEmpty()) { println("No questions to export."); return; }
        Path out = QUESTIONS_CSV; // same file used in fallback
        try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            w.write("id,question,opt1,opt2,opt3,opt4,answerIndex");
            w.newLine();
            for (Question q : questions) {
                w.write(q.toCsvLine());
                w.newLine();
            }
            println("Exported to " + out.toAbsolutePath());
        } catch (IOException e) {
            println("Export failed: " + e.getMessage());
        }
    }

    // === Timed quiz runner ===
    private QuizResult runTimedQuiz(String username, List<Question> quiz, int timeLimitSec) {
        println("\nStarting quiz for " + username + ". You have " + timeLimitSec + " seconds total. Press Enter to begin.");
        SC.nextLine();

        final ExecutorService ex = Executors.newSingleThreadExecutor();
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        final long startTime = System.currentTimeMillis();
        final boolean[] timeUp = {false};
        final Future<QuizResult> future = ex.submit(() -> {
            int score = 0;
            int qnum = 0;
            for (Question q : quiz) {
                qnum++;
                long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                if (elapsed >= timeLimitSec) { timeUp[0] = true; break; }
                println("\nQuestion " + qnum + " of " + quiz.size() + "  (elapsed " + elapsed + "s)");
                println(q.question);
                println("1) " + q.opt1);
                println("2) " + q.opt2);
                println("3) " + q.opt3);
                println("4) " + q.opt4);
                print("Your answer (1-4) or 's' to skip: ");
                String ans = SC.nextLine().trim();
                if (ans.equalsIgnoreCase("s")) continue;
                try {
                    int a = Integer.parseInt(ans);
                    if (a >= 1 && a <= 4) {
                        if (a - 1 == q.correctIndex) score++;
                    }
                } catch (NumberFormatException ignore) { /* invalid -> treat as skip */ }
            }
            return new QuizResult(username, score, quiz.size(), LocalDateTime.now());
        });

        // schedule forcing finish
        scheduler.schedule(() -> {
            if (!future.isDone()) {
                println("\n*** Time is up! Finishing quiz... ***");
                // We can't cancel blocking input easily; marking timeUp and future will check elapsed between questions.
                // To be safe, attempt to cancel.
                future.cancel(true);
            }
        }, timeLimitSec, TimeUnit.SECONDS);

        QuizResult result;
        try {
            // Wait for completion; if cancelled due to timeout, gather partial info if possible (future cancelled)
            result = future.get(timeLimitSec + 2L, TimeUnit.SECONDS); // small buffer
        } catch (CancellationException | InterruptedException | ExecutionException | TimeoutException e) {
            // Timeout or cancel — partial progress may have been lost; we'll compute 0 or ask user to press Enter to continue.
            println("[INFO] Quiz interrupted by timeout. We'll compute results collected so far if any.");
            // For robustness, we will ask user how many correct they remember (best effort).
            int rememberedCorrect = askInt("Enter number of questions you answered correctly before timeout (or 0 if unknown): ", 0, quiz.size());
            result = new QuizResult(username, rememberedCorrect, quiz.size(), LocalDateTime.now());
        } finally {
            scheduler.shutdownNow();
            ex.shutdownNow();
        }
        println("\n--- Quiz finished ---");
        return result;
    }

    // === Persistence methods ===

    // Ensure DB tables exist if using DB
    private void ensureDbTables() throws SQLException {
        if (conn == null) return;
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS questions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "question TEXT NOT NULL," +
                    "opt1 TEXT, opt2 TEXT, opt3 TEXT, opt4 TEXT," +
                    "answer_index INTEGER NOT NULL" +
                    ");");
            st.execute("CREATE TABLE IF NOT EXISTS results (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT NOT NULL," +
                    "score INTEGER NOT NULL," +
                    "total INTEGER NOT NULL," +
                    "datetime TEXT NOT NULL" +
                    ");");
        }
    }

    // Save question to DB or CSV
    private boolean saveQuestion(Question q) {
        if (dbAvailable && conn != null) {
            String sql = "INSERT INTO questions (question,opt1,opt2,opt3,opt4,answer_index) VALUES (?,?,?,?,?,?);";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, q.question);
                ps.setString(2, q.opt1);
                ps.setString(3, q.opt2);
                ps.setString(4, q.opt3);
                ps.setString(5, q.opt4);
                ps.setInt(6, q.correctIndex);
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                println("DB save question failed: " + e.getMessage());
                return false;
            }
        } else {
            // append to QUESTIONS_CSV
            try {
                if (!Files.exists(QUESTIONS_CSV)) {
                    try (BufferedWriter w = Files.newBufferedWriter(QUESTIONS_CSV, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
                        w.write("id,question,opt1,opt2,opt3,opt4,answerIndex");
                        w.newLine();
                    }
                }
                List<Question> list = loadAllQuestions(); // to compute next id
                int nextId = list.stream().mapToInt(qx -> qx.id).max().orElse(0) + 1;
                String line = nextId + "," + q.toCsvLineNoId();
                Files.write(QUESTIONS_CSV, Collections.singletonList(line), StandardCharsets.UTF_8, Files.exists(QUESTIONS_CSV) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
                return true;
            } catch (IOException e) {
                println("CSV save question failed: " + e.getMessage());
                return false;
            }
        }
    }

    // Load all questions
    private List<Question> loadAllQuestions() {
        if (dbAvailable && conn != null) {
            List<Question> out = new ArrayList<>();
            String sql = "SELECT id,question,opt1,opt2,opt3,opt4,answer_index FROM questions ORDER BY id ASC;";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String question = rs.getString(2);
                    String opt1 = rs.getString(3);
                    String opt2 = rs.getString(4);
                    String opt3 = rs.getString(5);
                    String opt4 = rs.getString(6);
                    int idx = rs.getInt(7);
                    out.add(new Question(id, question, opt1, opt2, opt3, opt4, idx));
                }
            } catch (SQLException e) {
                println("DB load questions failed: " + e.getMessage());
            }
            return out;
        } else {
            // read CSV
            List<Question> out = new ArrayList<>();
            if (!Files.exists(QUESTIONS_CSV)) return out;
            try (BufferedReader r = Files.newBufferedReader(QUESTIONS_CSV, StandardCharsets.UTF_8)) {
                String header = r.readLine(); // skip header
                String ln;
                while ((ln = r.readLine()) != null) {
                    if (ln.trim().isEmpty()) continue;
                    // naive CSV parsing (we escape quotes when writing)
                    String[] parts = splitCsvLine(ln, 7);
                    if (parts.length >= 7) {
                        int id = Integer.parseInt(parts[0]);
                        String question = parts[1];
                        String opt1 = parts[2], opt2 = parts[3], opt3 = parts[4], opt4 = parts[5];
                        int idx = Integer.parseInt(parts[6]);
                        out.add(new Question(id, question, opt1, opt2, opt3, opt4, idx));
                    }
                }
            } catch (IOException e) {
                println("CSV read failed: " + e.getMessage());
            }
            return out;
        }
    }

    // Delete a question by id
    private boolean deleteQuestionById(int id) {
        if (dbAvailable && conn != null) {
            String sql = "DELETE FROM questions WHERE id = ?;";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                int affected = ps.executeUpdate();
                return affected > 0;
            } catch (SQLException e) {
                println("DB delete failed: " + e.getMessage());
                return false;
            }
        } else {
            // rewrite CSV without the id
            List<Question> list = loadAllQuestions();
            boolean found = list.removeIf(q -> q.id == id);
            if (!found) return false;
            try (BufferedWriter w = Files.newBufferedWriter(QUESTIONS_CSV, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                w.write("id,question,opt1,opt2,opt3,opt4,answerIndex");
                w.newLine();
                for (Question q : list) {
                    w.write(q.toCsvLine());
                    w.newLine();
                }
                return true;
            } catch (IOException e) {
                println("CSV rewrite failed: " + e.getMessage());
                return false;
            }
        }
    }

    // Store quiz result
    private boolean storeResult(QuizResult result) {
        if (dbAvailable && conn != null) {
            String sql = "INSERT INTO results (username,score,total,datetime) VALUES (?,?,?,?);";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, result.username);
                ps.setInt(2, result.score);
                ps.setInt(3, result.totalQuestions);
                ps.setString(4, result.dateTime.format(DT_FMT));
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                println("DB save result failed: " + e.getMessage());
                return false;
            }
        } else {
            // append to CSV
            try {
                if (!Files.exists(RESULTS_CSV)) {
                    try (BufferedWriter w = Files.newBufferedWriter(RESULTS_CSV, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
                        w.write("username,score,total,datetime");
                        w.newLine();
                    }
                }
                String line = escapeCsv(result.username) + "," + result.score + "," + result.totalQuestions + "," + result.dateTime.format(DT_FMT);
                Files.write(RESULTS_CSV, Collections.singletonList(line), StandardCharsets.UTF_8, Files.exists(RESULTS_CSV) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
                return true;
            } catch (IOException e) {
                println("CSV save result failed: " + e.getMessage());
                return false;
            }
        }
    }

    // === Utilities ===

    private static void closeQuietly(AutoCloseable ac) {
        if (ac == null) return;
        try { ac.close(); } catch (Exception ignored) {}
    }

    private static void println(String s) { System.out.println(s); }
    private static void print(String s) { System.out.print(s); }

    private int askInt(String prompt, int min, int max) {
        while (true) {
            print(prompt);
            String in = SC.nextLine().trim();
            try {
                int v = Integer.parseInt(in);
                if (v < min || v > max) {
                    println("Enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                println("Invalid number.");
            }
        }
    }

    // naive CSV splitting that handles quoted fields (simple)
    private static String[] splitCsvLine(String line, int expectedColumns) {
        List<String> cols = new ArrayList<>(expectedColumns);
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); ++i) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"'); // escaped quote
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                cols.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        cols.add(cur.toString());
        return cols.toArray(new String[0]);
    }

    private static String escapeCsv(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    // === Data classes ===
    private static class Question {
        final int id;
        final String question;
        final String opt1, opt2, opt3, opt4;
        final int correctIndex;

        Question(int id, String question, String opt1, String opt2, String opt3, String opt4, int correctIndex) {
            this.id = id;
            this.question = question;
            this.opt1 = opt1;
            this.opt2 = opt2;
            this.opt3 = opt3;
            this.opt4 = opt4;
            this.correctIndex = correctIndex;
        }

        String toDisplayString() {
            return String.format("[%d] %s\n 1) %s\n 2) %s\n 3) %s\n 4) %s\n (answer: %d)",
                    id, question, opt1, opt2, opt3, opt4, correctIndex + 1);
        }

        String toCsvLine() {
            return id + "," + toCsvLineNoId();
        }

        String toCsvLineNoId() {
            return escapeCsv(question) + "," + escapeCsv(opt1) + "," + escapeCsv(opt2) + "," + escapeCsv(opt3) + "," + escapeCsv(opt4) + "," + correctIndex;
        }
    }

    private static class QuizResult {
        final String username;
        final int score;
        final int totalQuestions;
        final LocalDateTime dateTime;

        QuizResult(String username, int score, int totalQuestions, LocalDateTime dateTime) {
            this.username = username;
            this.score = score;
            this.totalQuestions = totalQuestions;
            this.dateTime = dateTime;
        }

        double getPercent() {
            if (totalQuestions == 0) return 0;
            return Math.round((score * 10000.0 / totalQuestions)) / 100.0;
        }
    }

    // === MAIN ===
    public static void main(String[] args) {
        OnlineQuizApp app = new OnlineQuizApp();
        app.run();
    }
}
