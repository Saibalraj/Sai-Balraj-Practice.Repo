import java.util.Scanner;

public class ChatBot {

    // Solve simple math using JavaScript engine
    public static String solveMath(String expr) {
        try {
            javax.script.ScriptEngine engine = 
                new javax.script.ScriptEngineManager().getEngineByName("JavaScript");
            Object result = engine.eval(expr);
            return "Answer: " + result.toString();
        } catch (Exception e) {
            return "Sorry, I couldn't solve that.";
        }
    }

    // Chatbot logic
    public static String chatbotResponse(String user) {
        String u = user.toLowerCase();

        // Greetings
        if (u.contains("hello") || u.contains("hi")) {
            return "Hello! I'm Sai Balraj. How can I help you?";
        }
        if (u.contains("your name")) {
            return "My name is Sai Balraj, your chatbot assistant!";
        }

        // Math detection (only numbers and operators)
        if (u.matches("[0-9+\\-*/(). ]+")) {
            return solveMath(user);
        }

        // Default
        return "I'm not sure about that, but I'm learning!";
    }

    // Main program
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Sai Balraj Chatbot Ready! Type 'exit' to quit.\n");

        while (true) {
            System.out.print("You: ");
            String msg = sc.nextLine();

            if (msg.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }

            System.out.println("Bot: " + chatbotResponse(msg));
        }

        sc.close();
    }
}
