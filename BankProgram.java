// //This is Bank Account Checker PRE-DEFINED

// // import java.util.Date;

// // abstract class BankAccount {
// //     protected String accountNumber;
// //     protected String accountHolderName;
// //     protected double balance;
// //     protected String password;
// //     protected Date lastTransactionDate;

// //     public BankAccount(String accountNumber, String accountHolderName, double initialBalance, String password) {
// //         this.accountNumber = accountNumber;
// //         this.accountHolderName = accountHolderName;
// //         this.balance = initialBalance;
// //         this.password = password;
// //         this.lastTransactionDate = new Date();
// //     }

// //     public abstract void withdraw(double amount, String password) throws Exception;

// //     public void deposit(double amount) {
// //         balance += amount;
// //         lastTransactionDate = new Date();
// //     }

// //     public double getBalance() {
// //         return balance;
// //     }

// //     public String getAccountNumber() {
// //         return accountNumber;
// //     }

// //     public String getAccountHolderName() {
// //         return accountHolderName;
// //     }

// //     public Date getLastTransactionDate() {
// //         return lastTransactionDate;
// //     }
// // }

// // // Subclass: SavingsAccount

// // class SavingsAccount extends BankAccount {

// //     public SavingsAccount(String accountNumber, String accountHolderName, double balance, String password) {
// //         super(accountNumber, accountHolderName, balance, password);
// //     }

// //     @Override
// //     public void withdraw(double amount, String password) throws Exception {
// //         if (!this.password.equals(password)) {
// //             throw new Exception("Incorrect password!");
// //         }
// //         if (amount > balance) {
// //             throw new Exception("Insufficient balance!");
// //         }

// //         balance -= amount;
// //         lastTransactionDate = new Date();
// //     }
// // }

// // // MAIN CLASS

// // public class BankProgram {
// //     public static void main(String[] args) {
// //         try {
// //             SavingsAccount acc = new SavingsAccount("7775", "Sai Balraj", 5000, "Sai@123");

// //             acc.deposit(100000);               // deposit
// //             acc.withdraw(3000, "Sai@123");        // withdraw

// //             System.out.println("Account Holder: " + acc.getAccountHolderName());
// //             System.out.println("Account Number: " + acc.getAccountNumber());
// //             System.out.println("Balance: " + acc.getBalance());
// //             System.out.println("Last Transaction: " + acc.getLastTransactionDate());

// //         } catch (Exception e) {
// //             System.out.println("Error: " + e.getMessage());
// //         }
// //     }
// // }

// //This is Bank Account Checker USER-DEFINED

// import java.util.Date;
// import java.util.Scanner;

// // USER-DEFINED CLASS
// abstract class BankAccount {
//     protected String accountNumber;
//     protected String accountHolderName;
//     protected double balance;
//     protected String password;
//     protected Date lastTransactionDate;

//     public BankAccount(String accountNumber, String accountHolderName, double initialBalance, String password) {
//         this.accountNumber = accountNumber;
//         this.accountHolderName = accountHolderName;
//         this.balance = initialBalance;
//         this.password = password;
//         this.lastTransactionDate = new Date();
//     }

//     public abstract void withdraw(double amount, String password) throws Exception;

//     public void deposit(double amount) {
//         balance += amount;
//         lastTransactionDate = new Date();
//     }

//     public double getBalance() {
//         return balance;
//     }

//     public String getAccountNumber() {
//         return accountNumber;
//     }

//     public String getAccountHolderName() {
//         return accountHolderName;
//     }

//     public Date getLastTransactionDate() {
//         return lastTransactionDate;
//     }
// }

// // SUBCLASS
// class SavingsAccount extends BankAccount {

//     public SavingsAccount(String accountNumber, String accountHolderName, double balance, String password) {
//         super(accountNumber, accountHolderName, balance, password);
//     }

//     @Override
//     public void withdraw(double amount, String password) throws Exception {
//         if (!this.password.equals(password)) {
//             throw new Exception("Incorrect password!");
//         }
//         if (amount > balance) {
//             throw new Exception("Insufficient balance!");
//         }

//         balance -= amount;
//         lastTransactionDate = new Date();
//     }
// }

// // DRIVER CLASS
// public class BankProgram {
//     public static void main(String[] args) {

//         Scanner sc = new Scanner(System.in);

//         // Input account data
//         System.out.print("Enter Account Number: ");
//         String accNo = sc.nextLine();

//         System.out.print("Enter Account Holder Name: ");
//         String name = sc.nextLine();

//         System.out.print("Enter Initial Balance: ");
//         double bal = sc.nextDouble();
//         sc.nextLine();

//         System.out.print("Set Password: ");
//         String pwd = sc.nextLine();

//         // Create account object
//         SavingsAccount acc = new SavingsAccount(accNo, name, bal, pwd);

//         // Deposit
//         System.out.print("Enter deposit amount: ");
//         double dep = sc.nextDouble();
//         acc.deposit(dep);

//         // Withdraw
//         System.out.print("Enter withdrawal amount: ");
//         double wAmt = sc.nextDouble();
//         sc.nextLine();

//         System.out.print("Enter password for withdrawal: ");
//         String wPwd = sc.nextLine();

//         try {
//             acc.withdraw(wAmt, wPwd);
//         } catch (Exception e) {
//             System.out.println("Error: " + e.getMessage());
//         }

//         // Final Output
//         System.out.println("\n----- ACCOUNT DETAILS -----");
//         System.out.println("Account Holder: " + acc.getAccountHolderName());
//         System.out.println("Account Number: " + acc.getAccountNumber());
//         System.out.println("Current Balance: " + acc.getBalance());
//         System.out.println("Last Transaction: " + acc.getLastTransactionDate());
//     }
// }
