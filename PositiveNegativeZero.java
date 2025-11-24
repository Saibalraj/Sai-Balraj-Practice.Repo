//This is PRE-DEFINED

// public class PositiveNegativeZero {
//     public static void main(String[] args) {

//         double number = -12.5;  

//         if (number > 0) {
//             System.out.println("The number " + number + " is positive.");
//         } else if (number < 0) {
//             System.out.println("The number " + number + " is negative.");
//         } else {
//             System.out.println("The number is zero.");
//         }
//     }
// }


//This is  USER-DEFINED

import java.util.Scanner;

public class PositiveNegativeZero {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter a number: ");
        double number = scanner.nextDouble();

        if (number > 0) {
            System.out.println("The number " + number + " is positive.");
        } else if (number < 0) {
            System.out.println("The number " + number + " is negative.");
        } else {
            System.out.println("The number is zero.");
        }

        scanner.close();
    }
}