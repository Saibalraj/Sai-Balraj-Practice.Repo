import java.io.*;
import java.util.*;

public class FileBasedLoginSystem {

    private static final Map<String, User> users = new HashMap<>();
    private static final File userFile = new File("users.txt");
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        loadUsers();
        while (true) {
            showMainMenu();
        }
    }
    // -------------------- MAIN MENU --------------------
    private static void showMainMenu() {
        System.out.println("\n====== FILE BASED LOGIN SYSTEM ======");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Forgot Password");
        System.out.println("4. Exit");
        System.out.print("Choose an option: ");

        int choice = safeIntInput();

        switch (choice) {
            case 1 -> login();
            case 2 -> registerUser();
            case 3 -> forgotPassword();
            case 4 -> {
                System.out.println("Exiting... Goodbye!");
                System.exit(0);
            }
            default -> System.out.println("Invalid choice!");
        }
    }
    // Safe integer input
    private static int safeIntInput() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.print("Enter a valid number: ");
            }
        }
    }
    // -------------------- LOGIN --------------------
    private static void login() {
        System.out.print("Enter username: ");
        String user = sc.nextLine();

        System.out.print("Enter password: ");
        String pass = sc.nextLine();

        User u = users.get(user);
        if (u != null && u.password.equals(pass)) {
            System.out.println("Login Successful! Welcome " + user + "!");
        } else {
            System.out.println("Invalid username or password!");
        }
    }
    // -------------------- REGISTER --------------------
    private static void registerUser() {
        System.out.print("Choose username: ");
        String user = sc.nextLine().trim();

        if (users.containsKey(user)) {
            System.out.println("Username already exists!");
            return;
        }

        System.out.print("Choose password: ");
        String pass = sc.nextLine();

        System.out.print("Enter security question: ");
        String ques = sc.nextLine();

        System.out.print("Enter answer: ");
        String ans = sc.nextLine();

        users.put(user, new User(user, pass, ques, ans));
        saveUsers();

        System.out.println("Registration Successful!");
    }
    // -------------------- FORGOT PASSWORD --------------------
    private static void forgotPassword() {
        System.out.print("Enter username: ");
        String user = sc.nextLine();

        if (!users.containsKey(user)) {
            System.out.println("User not found!");
            return;
        }

        User u = users.get(user);

        System.out.println("Security Question: " + u.securityQuestion);
        System.out.print("Your Answer: ");
        String ans = sc.nextLine();

        if (ans.equals(u.securityAnswer)) {
            System.out.print("Enter new password: ");
            String newPass = sc.nextLine();

            u.password = newPass;
            saveUsers();

            System.out.println("Password reset successful!");
        } else {
            System.out.println("Incorrect answer!");
        }
    }
    // -------------------- FILE SAVE/LOAD --------------------
    private static void loadUsers() {
        try {
            if (!userFile.exists()) return;

            BufferedReader br = new BufferedReader(new FileReader(userFile));
            String line;

            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length == 4) {
                    users.put(p[0], new User(p[0], p[1], p[2], p[3]));
                }
            }
            br.close();
        } catch (Exception e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }
    private static void saveUsers() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(userFile));
            for (User u : users.values()) {
                pw.println(u.username + "|" + u.password + "|" +
                           u.securityQuestion + "|" + u.securityAnswer);
            }
            pw.close();
        } catch (Exception e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }
    // -------------------- USER CLASS --------------------
    static class User {
        String username, password, securityQuestion, securityAnswer;

        User(String u, String p, String q, String a) {
            username = u;
            password = p;
            securityQuestion = q;
            securityAnswer = a;
        }
    }
}
