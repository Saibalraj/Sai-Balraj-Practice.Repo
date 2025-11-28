//This is PRE-DEFIEND

// public class StudentGrade {
//     public static void main(String[] args) {

//         double mark1 = 85;
//         double mark2 = 92;
//         double mark3 = 78;
//         double mark4 = 88;

//         double average = (mark1 + mark2 + mark3 + mark4) / 4;

//         System.out.println("Subject 1: " + mark1);
//         System.out.println("Subject 2: " + mark2);
//         System.out.println("Subject 3: " + mark3);
//         System.out.println("Subject 4: " + mark4);

//         System.out.println("\nAverage Mark: " + average);

//         char grade;
//         if (average >= 90) {
//             grade = 'A';
//         } else if (average >= 80) {
//             grade = 'B';
//         } else if (average >= 70) {
//             grade = 'C';
//         } else if (average >= 60) {
//             grade = 'D';
//         } else {
//             grade = 'F';
//         }

//         System.out.println("Grade: " + grade);
//     }
// }

//This is USER-DEFIEND
import java.util.Scanner;

public class StudentGrade {
    public static void main(String[] args) {
       
        Scanner scanner = new Scanner(System.in);

        double mark1, mark2, mark3, mark4, average;

       
        System.out.print("Enter the mark for Subject 1: ");
        mark1 = scanner.nextDouble();

        System.out.print("Enter the mark for Subject 2: ");
        mark2 = scanner.nextDouble();

        System.out.print("Enter the mark for Subject 3: ");
        mark3 = scanner.nextDouble();

        System.out.print("Enter the mark for Subject 4: ");
        mark4 = scanner.nextDouble();

        
        average = (mark1 + mark2 + mark3 + mark4) / 4;

        System.out.println("\nAverage Mark: " + average);

      
        char grade;
        if (average >= 90) {
            grade = 'A';
        } else if (average >= 80) {
            grade = 'B';
        } else if (average >= 70) {
            grade = 'C';
        } else if (average >= 60) {
            grade = 'D';
        } else {
            grade = 'F';
        }

        System.out.println("Grade: " + grade);
        
        scanner.close();
    }
}