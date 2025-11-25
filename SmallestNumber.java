//This is PRE-DEFINED

// public class SmallestNumber {
//     public static void main(String[] args) {

//         int num1 = 47;
//         int num2 = 54;
//         int num3 = 35;

//         int smallest;

//         if (num1 <= num2 && num1 <= num3) {
//             smallest = num1;
//         } else if (num2 <= num1 && num2 <= num3) {
//             smallest = num2;
//         } else {
//             smallest = num3;
//         }

//                System.out.println("The smallest number among " + num1 + ", " + num2 + ", and " + num3 + " is: " + smallest);
//     }
// }

//This is USER-DEFINED

import java.util.Scanner;

public class SmallestNumber {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        // Taking user-defined input
        System.out.print("Enter first number: ");
        int num1 = sc.nextInt();

        System.out.print("Enter second number: ");
        int num2 = sc.nextInt();

        System.out.print("Enter third number: ");
        int num3 = sc.nextInt();

        // Find the smallest number
        int smallest;

        if (num1 <= num2 && num1 <= num3) {
            smallest = num1;
        } else if (num2 <= num1 && num2 <= num3) {
            smallest = num2;
        } else {
            smallest = num3;
        }

        // Output the smallest number
        System.out.println("The smallest number among " + num1 + ", " + num2 + ", and " + num3 + " is: " + smallest);

        sc.close();
    }
}
