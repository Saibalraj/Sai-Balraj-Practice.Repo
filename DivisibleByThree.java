//This is PRE-DEFINED

// public class DivisibleByThree {
//     public static void main(String[] args) {
//         int number = 5;  
//         while (number % 3 != 0) {
//             System.out.println("SAI");
//             number++;  
//         }
//         System.out.println("The number " + number + " is divisible by 3.");
//     }
// }

//This is USER-DEFINED

import java.util.Scanner;

public class DivisibleByThree {
    public static void main(String[] args) {
        
        Scanner sc = new Scanner(System.in);
        
        System.out.print("Enter any starting number: ");
        int number = sc.nextInt();   // user-defined value
        
        while (number % 3 != 0) {
            System.out.println("SAI");
            number++;
        }
        
        System.out.println("The number " + number + " is divisible by 3.");
        
        sc.close();
    }
}
