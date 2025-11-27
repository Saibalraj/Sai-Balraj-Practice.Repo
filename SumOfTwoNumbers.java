//This is PRE-DEFINEND

// public class SumOfTwoNumbers{
//     public static void main(String[] args) {
//         int num1 = 10;   
//         int num2 = 20;   

//         int sum = num1 + num2;

//         System.out.println("The sum of " + num1 + " and " + num2 + " is: " + sum);
//     }
// }


//This is USER-DEFIEND

import java.util.Scanner;

public class SumOfTwoNumbers {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter first number: ");
        int num1 = sc.nextInt();

        System.out.print("Enter second number: ");
        int num2 = sc.nextInt();

        int sum = num1 + num2;

        System.out.println("The sum is: " + sum);
        sc.close();
    }
}
