
// This is the completed code for AlphabetCheck in PRE-DEFINED


// public class AlphabetCheck {
//     public static void main(String[] args) {
//         // Assign an English alphabet character (you can change this value to test with others)
//         char alphabet = 'E';  // Example default value
        
//         // Check if the character is an alphabet
//         if (Character.isLetter(alphabet)) {
//             // Convert the character to lowercase to simplify checking
//             alphabet = Character.toLowerCase(alphabet);

//             // Check if the character is a vowel
//             if (alphabet == 'a' || alphabet == 'e' || alphabet == 'i' || alphabet == 'o' || alphabet == 'u') {
//                 System.out.println(alphabet + " is a vowel.");
//             } else {
//                 // If not a vowel, then it's a consonant
//                 System.out.println(alphabet + " is a consonant.");
//             }
//         } else {
//             // If the character is not a letter, print a default message
//             System.out.println("The character is not a valid English alphabet.");
//         }
//     }
// }

// This is the completed code for AlphabetCheck in USER-DEFINED

// import java.util.Scanner;
// public class AlphabetCheck {
//     public static void main(String[] args) {

//         Scanner s = new Scanner(System.in);

//         // Take input from user
//         System.out.print("Enter a character: ");
//         char alphabet = s.next().charAt(0);

//         // Check if the character is an alphabet
//         if (Character.isLetter(alphabet)) {
//             alphabet = Character.toLowerCase(alphabet);

//             // Check for vowel
//             if (alphabet == 'a' || alphabet == 'e' || alphabet == 'i' || alphabet == 'o' || alphabet == 'u') {
//                 System.out.println(alphabet + " is a vowel.");
//             } else {
//                 System.out.println(alphabet + " is a consonant.");
//             }
//         } else {
//             System.out.println("The character is not a valid English alphabet.");
//         }

//         s.close();
//     }
// }
