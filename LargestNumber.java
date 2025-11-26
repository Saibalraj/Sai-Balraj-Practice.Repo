//This is PRE-DEFINED 

// public class LargestNumber {
//     public static void main(String[] args) {

//         int num1 = 47;
//         int num2 = 54;
//         int num3 = 35;

//         int largest;

//         if (num1 >= num2 && num1 >= num3) {
//             largest = num1;
//         } else if (num2 >= num1 && num2 >= num3) {
//             largest = num2;
//         } else {
//             largest = num3;
//         }

//         System.out.println("The largest number among " + num1 + ", " + num2 + ", and " + num3 + " is: " + largest);
//     }
// }

//This is USER-DEFINED

import java.util.Scanner;

public class LargestNumber {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter first number: ");
        int num1 = sc.nextInt();

        System.out.print("Enter second number: ");
        int num2 = sc.nextInt();

        System.out.print("Enter third number: ");
        int num3 = sc.nextInt();

        int largest;

        if (num1 >= num2 && num1 >= num3) {
            largest = num1;
        } else if (num2 >= num1 && num2 >= num3) {
            largest = num2;
        } else {
            largest = num3;
        }

        System.out.println("The largest number among " + num1 + ", " + num2 + ", and " + num3 + " is: " + largest);

        sc.close();
    }
}