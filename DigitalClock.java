import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DigitalClock {

    public static void main(String[] args) throws InterruptedException {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH : mm : ss");

        while (true) {
            LocalTime now = LocalTime.now();
            String time = now.format(formatter);

            clearScreen();

            System.out.println("=====================================");
            System.out.println("         DIGITAL CLOCK (CLI)         ");
            System.out.println("=====================================");
            System.out.println();
            System.out.println("             " + time);
            System.out.println();
            System.out.println("=====================================");

            Thread.sleep(1000); // update every second
        }
    }

    // Terminal clear method
    public static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // If clear fails, just print new lines
            for (int i = 0; i < 50; i++) System.out.println();
        }
    }
}
