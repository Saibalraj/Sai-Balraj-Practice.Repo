import java.io.*;
import java.util.*;

class Employee implements Serializable {
    private static final long serialVersionUID = 1L;

    int id;
    String name;
    double basicSalary;
    double allowance;
    double deduction;

    public Employee(int id, String name, double basicSalary, double allowance, double deduction) {
        this.id = id;
        this.name = name;
        this.basicSalary = basicSalary;
        this.allowance = allowance;
        this.deduction = deduction;
    }

    public double getNetSalary() {
        return basicSalary + allowance - deduction;
    }

    @Override
    public String toString() {
        return "---------------------------------------\n" +
               "ID           : " + id + "\n" +
               "Name         : " + name + "\n" +
               "Basic Salary : " + basicSalary + "\n" +
               "Allowance    : " + allowance + "\n" +
               "Deduction    : " + deduction + "\n" +
               "Net Salary   : " + getNetSalary() + "\n" +
               "---------------------------------------";
    }
}

public class EmployeePayrollSystem {

    private static final String FILE_NAME = "employees.db";
    private static ArrayList<Employee> employees = new ArrayList<>();
    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        loadEmployees();
        while (true) {
            System.out.println("\n=======================================");
            System.out.println("          EMPLOYEE PAYROLL SYSTEM      ");
            System.out.println("=======================================");
            System.out.println("1. Add Employee");
            System.out.println("2. View All Employees");
            System.out.println("3. Search Employee");
            System.out.println("4. Update Employee");
            System.out.println("5. Delete Employee");
            System.out.println("6. Save & Exit");
            System.out.print("Enter choice: ");

            int choice = safeInt();
            switch (choice) {
                case 1 -> addEmployee();
                case 2 -> viewEmployees();
                case 3 -> searchEmployee();
                case 4 -> updateEmployee();
                case 5 -> deleteEmployee();
                case 6 -> {
                    saveEmployees();
                    System.out.println("\n✔ Data saved successfully! Exiting...");
                    return;
                }
                default -> System.out.println("❌ Invalid choice! Try again.");
            }
        }
    }

    // Input safely
    private static int safeInt() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.print("Enter a valid number: ");
            }
        }
    }

    private static double safeDouble() {
        while (true) {
            try {
                return Double.parseDouble(sc.nextLine());
            } catch (Exception e) {
                System.out.print("Enter a valid amount: ");
            }
        }
    }

    private static void addEmployee() {
        System.out.print("Enter ID: ");
        int id = safeInt();

        System.out.print("Enter Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Basic Salary: ");
        double basic = safeDouble();

        System.out.print("Enter Allowance: ");
        double allowance = safeDouble();

        System.out.print("Enter Deduction: ");
        double deduction = safeDouble();

        employees.add(new Employee(id, name, basic, allowance, deduction));
        System.out.println("\n✔ Employee added successfully!");
    }

    private static void viewEmployees() {
        if (employees.isEmpty()) {
            System.out.println("\n❌ No employees found!");
            return;
        }

        System.out.println("\n======= ALL EMPLOYEES =======");
        for (Employee e : employees) {
            System.out.println(e);
        }
    }

    private static void searchEmployee() {
        System.out.print("Enter Employee ID or Name to search: ");
        String keyword = sc.nextLine().toLowerCase();

        boolean found = false;

        for (Employee e : employees) {
            if (String.valueOf(e.id).equals(keyword) || e.name.toLowerCase().contains(keyword)) {
                System.out.println(e);
                found = true;
            }
        }

        if (!found) System.out.println("\n❌ Employee not found!");
    }

    private static void updateEmployee() {
        System.out.print("Enter Employee ID to update: ");
        int id = safeInt();

        for (Employee e : employees) {
            if (e.id == id) {
                System.out.println("Editing Employee: " + e.name);

                System.out.print("New Name (" + e.name + "): ");
                e.name = sc.nextLine();

                System.out.print("New Basic Salary (" + e.basicSalary + "): ");
                e.basicSalary = safeDouble();

                System.out.print("New Allowance (" + e.allowance + "): ");
                e.allowance = safeDouble();

                System.out.print("New Deduction (" + e.deduction + "): ");
                e.deduction = safeDouble();

                System.out.println("\n✔ Employee updated successfully!");
                return;
            }
        }
        System.out.println("\n❌ Employee not found!");
    }

    private static void deleteEmployee() {
        System.out.print("Enter Employee ID to delete: ");
        int id = safeInt();

        Iterator<Employee> it = employees.iterator();
        while (it.hasNext()) {
            if (it.next().id == id) {
                it.remove();
                System.out.println("\n✔ Employee deleted successfully!");
                return;
            }
        }
        System.out.println("\n❌ Employee not found!");
    }

    private static void saveEmployees() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(employees);
        } catch (Exception e) {
            System.out.println("❌ Error saving data: " + e.getMessage());
        }
    }

    private static void loadEmployees() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            employees = (ArrayList<Employee>) ois.readObject();
            System.out.println("✔ Loaded " + employees.size() + " employees.");
        } catch (Exception e) {
            System.out.println("✔ No previous data found. Starting fresh.");
        }
    }
}
