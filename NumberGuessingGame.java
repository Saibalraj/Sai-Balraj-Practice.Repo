//The Answer will show After we guess the number.

import java.util.Random;
import java.util.Scanner;

public class NumberGuessingGame {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        Random rand = new Random();

        // computer generates a number from 1 to 100
        int answer = rand.nextInt(100) + 1;

        System.out.println("===== NUMBER GUESSING GAME =====");
        System.out.print("Enter your guess (1-100): ");
        
        int guess = sc.nextInt();

        // check guess
        if (guess == answer) {
            System.out.println("🎉 Correct! You guessed the number!");
        } else if (guess < answer) {
            System.out.println("Too low!");
        } else {
            System.out.println("Too high!");
        }

        // always show the correct answer afterwards
        System.out.println("✔ The correct number was: " + answer);

        System.out.println("Game Over!");
        sc.close();
    }
}


//The Answer will show Before we guess the number.

// import java.util.Random;
// import java.util.Scanner;

// public class NumberGuessingGame {

//     public static void main(String[] args) {
//         Scanner sc = new Scanner(System.in);
//         Random rand = new Random();

//         System.out.println("===== NUMBER GUESSING GAME =====");

//         int numberToGuess = rand.nextInt(100) + 1;
//         System.out.println("(The answer is: " + numberToGuess + ")"); 

//         int attempts = 0;

//         while (true) {
//             System.out.print("Enter your guess: ");

//             if (!sc.hasNextInt()) {
//                 System.out.println("Invalid input! Please enter a number.");
//                 sc.next();
//                 continue;
//             }

//             int guess = sc.nextInt();
//             attempts++;

//             if (guess < numberToGuess) {
//                 System.out.println("Too Low!");
//             } else if (guess > numberToGuess) {
//                 System.out.println("Too High!");
//             } else {
//                 System.out.println("\n🎉 Correct! You guessed it!");
//                 System.out.println("Attempts: " + attempts);
//                 break;
//             }
//         }

//         sc.close();
//         System.out.println("Game Over!");
//     }
// }