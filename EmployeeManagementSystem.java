import java.io.*;
import java.util.*;

abstract class Employee {
    protected String employeeId, name, department;
    
    public Employee(String employeeId, String name, String department) {
        this.employeeId = employeeId;
        setName(name);
        setDepartment(department);
    }
    
    public String getEmployeeId() { return employeeId; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    
    public void setName(String name) {
        if (name.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Name cannot contain numbers");
        }
        this.name = name;
    }
    
    public void setDepartment(String department) {
        if (department.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Department cannot contain numbers");
        }
        this.department = department;
    }
    
    public abstract double calculateSalary();
    
    @Override
    public String toString() {
        return String.format("ID: %s, Name: %s, Dept: %s", employeeId, name, department);
    }
    
    public abstract Map<String, Object> toMap();
    
    public static Employee fromMap(Map<String, Object> data) {
        String type = (String) data.get("type");
        String empId = (String) data.get("employee_id");
        String name = (String) data.get("name");
        String dept = (String) data.get("department");
        
        switch (type) {
            case "fulltimeemployee":
                return new FullTimeEmployee(empId, name, dept, Double.parseDouble(data.get("monthly_salary").toString()));
            case "parttimeemployee":
                return new PartTimeEmployee(empId, name, dept, 
                    Double.parseDouble(data.get("hourly_rate").toString()),
                    Double.parseDouble(data.get("hours_worked").toString()));
            case "manager":
                return new Manager(empId, name, dept, 
                    Double.parseDouble(data.get("monthly_salary").toString()),
                    Double.parseDouble(data.get("bonus").toString()));
            default: throw new IllegalArgumentException("Unknown employee type: " + type);
        }
    }
}

class FullTimeEmployee extends Employee {
    private double monthlySalary;
    
    public FullTimeEmployee(String employeeId, String name, String department, double monthlySalary) {
        super(employeeId, name, department);
        setMonthlySalary(monthlySalary);
    }
    
    public double getMonthlySalary() { return monthlySalary; }
    public void setMonthlySalary(double monthlySalary) {
        if (monthlySalary < 0) throw new IllegalArgumentException("Salary cannot be negative");
        this.monthlySalary = monthlySalary;
    }
    
    @Override
    public double calculateSalary() { return monthlySalary; }
    
    @Override
    public String toString() {
        return String.format("%s, Monthly Salary: ₹%,.2f", super.toString(), monthlySalary);
    }
    
    @Override
    public Map<String, Object> toMap() {
        return Map.of(
            "type", "fulltimeemployee",
            "employee_id", employeeId,
            "name", name,
            "department", department,
            "monthly_salary", monthlySalary
        );
    }
}

class PartTimeEmployee extends Employee {
    private double hourlyRate, hoursWorked;
    
    public PartTimeEmployee(String employeeId, String name, String department, 
                           double hourlyRate, double hoursWorked) {
        super(employeeId, name, department);
        setHourlyRate(hourlyRate);
        setHoursWorked(hoursWorked);
    }
    
    public double getHourlyRate() { return hourlyRate; }
    public double getHoursWorked() { return hoursWorked; }
    
    public void setHourlyRate(double hourlyRate) {
        if (hourlyRate < 0) throw new IllegalArgumentException("Hourly rate cannot be negative");
        this.hourlyRate = hourlyRate;
    }
    
    public void setHoursWorked(double hoursWorked) {
        if (hoursWorked < 0) throw new IllegalArgumentException("Hours worked cannot be negative");
        this.hoursWorked = hoursWorked;
    }
    
    @Override
    public double calculateSalary() { return hourlyRate * hoursWorked; }
    
    @Override
    public String toString() {
        return String.format("%s, Hourly Rate: ₹%.2f, Hours Worked: %.2f, Monthly Pay: ₹%,.2f",
                super.toString(), hourlyRate, hoursWorked, calculateSalary());
    }
    
    @Override
    public Map<String, Object> toMap() {
        return Map.of(
            "type", "parttimeemployee",
            "employee_id", employeeId,
            "name", name,
            "department", department,
            "hourly_rate", hourlyRate,
            "hours_worked", hoursWorked
        );
    }
}

class Manager extends FullTimeEmployee {
    private double bonus;
    
    public Manager(String employeeId, String name, String department, 
                  double monthlySalary, double bonus) {
        super(employeeId, name, department, monthlySalary);
        setBonus(bonus);
    }
    
    public double getBonus() { return bonus; }
    public void setBonus(double bonus) {
        if (bonus < 0) throw new IllegalArgumentException("Bonus cannot be negative");
        this.bonus = bonus;
    }
    
    @Override
    public double calculateSalary() { return super.calculateSalary() + bonus; }
    
    @Override
    public String toString() {
        return String.format("%s, Bonus: ₹%,.2f, Total Salary: ₹%,.2f",
                super.toString(), bonus, calculateSalary());
    }
    
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>(super.toMap());
        data.put("type", "manager");
        data.put("bonus", bonus);
        return data;
    }
}

class Company {
    private Map<String, Employee> employees = new HashMap<>();
    private String dataFile;
    
    public Company() { this("employees.dat"); }
    public Company(String dataFile) { 
        this.dataFile = dataFile;
        loadData();
    }
    
    @SuppressWarnings("unchecked")
    private void loadData() {
        File file = new File(dataFile);
        if (!file.exists()) return;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try {
                    Map<String, Object> data = parseMapString(line.trim());
                    Employee employee = Employee.fromMap(data);
                    employees.put(employee.getEmployeeId(), employee);
                } catch (Exception e) {
                    System.out.println("Error loading employee data: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading data file: " + e.getMessage());
        }
    }
    
    private Map<String, Object> parseMapString(String line) {
        Map<String, Object> map = new HashMap<>();
        String content = line.substring(1, line.length() - 1);
        String[] pairs = content.split(", ");
        
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                map.put(key, value.matches("-?\\d+(\\.\\d+)?") ? Double.parseDouble(value) : value);
            }
        }
        return map;
    }
    
    private void saveData() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(dataFile))) {
            employees.values().forEach(emp -> writer.println(emp.toMap().toString()));
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }
    
    public boolean addEmployee(Employee employee) {
        if (employees.containsKey(employee.getEmployeeId())) return false;
        employees.put(employee.getEmployeeId(), employee);
        saveData();
        return true;
    }
    
    public boolean removeEmployee(String employeeId) {
        if (!employees.containsKey(employeeId)) return false;
        employees.remove(employeeId);
        saveData();
        return true;
    }
    
    public Employee findEmployee(String employeeId) { return employees.get(employeeId); }
    
    public List<Employee> findEmployeesByName(String name) {
        List<Employee> result = new ArrayList<>();
        String searchName = name.toLowerCase();
        employees.values().forEach(emp -> {
            if (emp.getName().toLowerCase().contains(searchName)) result.add(emp);
        });
        return result;
    }
    
    public double calculateTotalPayroll() {
        return employees.values().stream().mapToDouble(Employee::calculateSalary).sum();
    }
    
    public void displayAllEmployees() {
        if (employees.isEmpty()) System.out.println("No employees in the system.");
        else employees.values().forEach(System.out::println);
    }
    
    public void generatePayrollReport() {
        if (employees.isEmpty()) {
            System.out.println("No employees in the system.");
            return;
        }
        
        System.out.println("\nPayroll Report:");
        System.out.println("=".repeat(60));
        System.out.printf("%-8s %-20s %-15s %15s%n", "ID", "Name", "Type", "Salary");
        System.out.println("-".repeat(60));
        
        employees.values().forEach(emp -> 
            System.out.printf("%-8s %-20s %-15s ₹%12.2f%n",
                emp.getEmployeeId(), emp.getName(), 
                emp.getClass().getSimpleName(), emp.calculateSalary())
        );
        
        System.out.println("=".repeat(60));
        System.out.printf("Total Payroll: ₹%395.2f%n%n", calculateTotalPayroll());
    }
}

public class EmployeeManagementSystem {
    private static Scanner scanner = new Scanner(System.in);
    
    private static String getInput(String prompt, boolean required) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (required && value.isEmpty()) System.out.println("This field is required.");
            else return value;
        }
    }
    
    private static String getStringInput(String prompt, boolean required, String fieldName) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (required && value.isEmpty()) {
                System.out.println("This field is required.");
                continue;
            }
            if (value.matches(".*\\d.*")) {
                System.out.println(fieldName + " cannot contain numbers.");
                continue;
            }
            return value;
        }
    }
    
    private static double getDoubleInput(String prompt, double minVal) {
        while (true) {
            try {
                System.out.print(prompt);
                double value = Double.parseDouble(scanner.nextLine());
                if (value < minVal) System.out.printf("Value must be at least %.2f.%n", minVal);
                else return value;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
    
    public static void main(String[] args) {
        Company company = new Company();
        
        while (true) {
            System.out.println("\nEmployee Management System");
            System.out.println("1. Add Employee");
            System.out.println("2. Remove Employee");
            System.out.println("3. Find Employee by ID");
            System.out.println("4. Find Employees by Name");
            System.out.println("5. View All Employees");
            System.out.println("6. Generate Payroll Report");
            System.out.println("7. Exit");
            
            String choice = getInput("Enter your choice (1-7): ", true);
            
            switch (choice) {
                case "1": addEmployee(company); break;
                case "2": removeEmployee(company); break;
                case "3": findEmployeeById(company); break;
                case "4": findEmployeesByName(company); break;
                case "5": company.displayAllEmployees(); break;
                case "6": company.generatePayrollReport(); break;
                case "7": 
                    System.out.println("Exiting the system. Goodbye!");
                    scanner.close();
                    return;
                default: System.out.println("Invalid choice. Please enter a number between 1 and 7.");
            }
        }
    }
    
    private static void addEmployee(Company company) {
        System.out.println("\nEmployee Types:");
        System.out.println("1. Full-time");
        System.out.println("2. Part-time");
        System.out.println("3. Manager");
        String empType = getInput("Select employee type (1-3): ", true);
        
        if (!Arrays.asList("1", "2", "3").contains(empType)) {
            System.out.println("Invalid choice. Please try again.");
            return;
        }
        
        String empId = getInput("Enter employee ID: ", true);
        if (company.findEmployee(empId) != null) {
            System.out.println("Employee ID already exists.");
            return;
        }
        
        String name = getStringInput("Enter employee name: ", true, "Name");
        String dept = getStringInput("Enter department: ", true, "Department");
        
        try {
            Employee emp;
            if (empType.equals("1")) {
                double salary = getDoubleInput("Enter monthly salary: ", 0);
                emp = new FullTimeEmployee(empId, name, dept, salary);
            } else if (empType.equals("2")) {
                double rate = getDoubleInput("Enter hourly rate: ", 0);
                double hours = getDoubleInput("Enter hours worked per month: ", 0);
                emp = new PartTimeEmployee(empId, name, dept, rate, hours);
            } else {
                double salary = getDoubleInput("Enter base salary: ", 0);
                double bonus = getDoubleInput("Enter bonus amount: ", 0);
                emp = new Manager(empId, name, dept, salary, bonus);
            }
            
            if (company.addEmployee(emp)) System.out.println("Employee " + name + " added successfully!");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static void removeEmployee(Company company) {
        String empId = getInput("Enter employee ID to remove: ", true);
        System.out.println(company.removeEmployee(empId) ? "Employee removed successfully." : "Employee not found.");
    }
    
    private static void findEmployeeById(Company company) {
        String empId = getInput("Enter employee ID to search: ", true);
        Employee emp = company.findEmployee(empId);
        if (emp != null) {
            System.out.println("\nEmployee Found:");
            System.out.println(emp);
        } else {
            System.out.println("Employee not found.");
        }
    }
    
    private static void findEmployeesByName(Company company) {
        String name = getStringInput("Enter name to search: ", false, "Name");
        List<Employee> employees = company.findEmployeesByName(name);
        if (!employees.isEmpty()) {
            System.out.println("\nMatching Employees:");
            employees.forEach(System.out::println);
        } else {
            System.out.println("No employees found with that name.");
        }
    }
}package javaproj;

import java.io.*;
import java.util.*;

abstract class Employee {
    protected String employeeId, name, department;
    
    public Employee(String employeeId, String name, String department) {
        this.employeeId = employeeId;
        setName(name);
        setDepartment(department);
    }
    
    public String getEmployeeId() { return employeeId; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    
    public void setName(String name) {
        if (name.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Name cannot contain numbers");
        }
        this.name = name;
    }
    
    public void setDepartment(String department) {
        if (department.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Department cannot contain numbers");
        }
        this.department = department;
    }
    
    public abstract double calculateSalary();
    
    @Override
    public String toString() {
        return String.format("ID: %s, Name: %s, Dept: %s", employeeId, name, department);
    }
    
    public abstract Map<String, Object> toMap();
    
    public static Employee fromMap(Map<String, Object> data) {
        String type = (String) data.get("type");
        String empId = (String) data.get("employee_id");
        String name = (String) data.get("name");
        String dept = (String) data.get("department");
        
        switch (type) {
            case "fulltimeemployee":
                return new FullTimeEmployee(empId, name, dept, Double.parseDouble(data.get("monthly_salary").toString()));
            case "parttimeemployee":
                return new PartTimeEmployee(empId, name, dept, 
                    Double.parseDouble(data.get("hourly_rate").toString()),
                    Double.parseDouble(data.get("hours_worked").toString()));
            case "manager":
                return new Manager(empId, name, dept, 
                    Double.parseDouble(data.get("monthly_salary").toString()),
                    Double.parseDouble(data.get("bonus").toString()));
            default: throw new IllegalArgumentException("Unknown employee type: " + type);
        }
    }
}

class FullTimeEmployee extends Employee {
    private double monthlySalary;
    
    public FullTimeEmployee(String employeeId, String name, String department, double monthlySalary) {
        super(employeeId, name, department);
        setMonthlySalary(monthlySalary);
    }
    
    public double getMonthlySalary() { return monthlySalary; }
    public void setMonthlySalary(double monthlySalary) {
        if (monthlySalary < 0) throw new IllegalArgumentException("Salary cannot be negative");
        this.monthlySalary = monthlySalary;
    }
    
    @Override
    public double calculateSalary() { return monthlySalary; }
    
    @Override
    public String toString() {
        return String.format("%s, Monthly Salary: ₹%,.2f", super.toString(), monthlySalary);
    }
    
    @Override
    public Map<String, Object> toMap() {
        return Map.of(
            "type", "fulltimeemployee",
            "employee_id", employeeId,
            "name", name,
            "department", department,
            "monthly_salary", monthlySalary
        );
    }
}

class PartTimeEmployee extends Employee {
    private double hourlyRate, hoursWorked;
    
    public PartTimeEmployee(String employeeId, String name, String department, 
                           double hourlyRate, double hoursWorked) {
        super(employeeId, name, department);
        setHourlyRate(hourlyRate);
        setHoursWorked(hoursWorked);
    }
    
    public double getHourlyRate() { return hourlyRate; }
    public double getHoursWorked() { return hoursWorked; }
    
    public void setHourlyRate(double hourlyRate) {
        if (hourlyRate < 0) throw new IllegalArgumentException("Hourly rate cannot be negative");
        this.hourlyRate = hourlyRate;
    }
    
    public void setHoursWorked(double hoursWorked) {
        if (hoursWorked < 0) throw new IllegalArgumentException("Hours worked cannot be negative");
        this.hoursWorked = hoursWorked;
    }
    
    @Override
    public double calculateSalary() { return hourlyRate * hoursWorked; }
    
    @Override
    public String toString() {
        return String.format("%s, Hourly Rate: ₹%.2f, Hours Worked: %.2f, Monthly Pay: ₹%,.2f",
                super.toString(), hourlyRate, hoursWorked, calculateSalary());
    }
    
    @Override
    public Map<String, Object> toMap() {
        return Map.of(
            "type", "parttimeemployee",
            "employee_id", employeeId,
            "name", name,
            "department", department,
            "hourly_rate", hourlyRate,
            "hours_worked", hoursWorked
        );
    }
}

class Manager extends FullTimeEmployee {
    private double bonus;
    
    public Manager(String employeeId, String name, String department, 
                  double monthlySalary, double bonus) {
        super(employeeId, name, department, monthlySalary);
        setBonus(bonus);
    }
    
    public double getBonus() { return bonus; }
    public void setBonus(double bonus) {
        if (bonus < 0) throw new IllegalArgumentException("Bonus cannot be negative");
        this.bonus = bonus;
    }
    
    @Override
    public double calculateSalary() { return super.calculateSalary() + bonus; }
    
    @Override
    public String toString() {
        return String.format("%s, Bonus: ₹%,.2f, Total Salary: ₹%,.2f",
                super.toString(), bonus, calculateSalary());
    }
    
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>(super.toMap());
        data.put("type", "manager");
        data.put("bonus", bonus);
        return data;
    }
}

class Company {
    private Map<String, Employee> employees = new HashMap<>();
    private String dataFile;
    
    public Company() { this("employees.dat"); }
    public Company(String dataFile) { 
        this.dataFile = dataFile;
        loadData();
    }
    
    @SuppressWarnings("unchecked")
    private void loadData() {
        File file = new File(dataFile);
        if (!file.exists()) return;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try {
                    Map<String, Object> data = parseMapString(line.trim());
                    Employee employee = Employee.fromMap(data);
                    employees.put(employee.getEmployeeId(), employee);
                } catch (Exception e) {
                    System.out.println("Error loading employee data: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading data file: " + e.getMessage());
        }
    }
    
    private Map<String, Object> parseMapString(String line) {
        Map<String, Object> map = new HashMap<>();
        String content = line.substring(1, line.length() - 1);
        String[] pairs = content.split(", ");
        
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                map.put(key, value.matches("-?\\d+(\\.\\d+)?") ? Double.parseDouble(value) : value);
            }
        }
        return map;
    }
    
    private void saveData() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(dataFile))) {
            employees.values().forEach(emp -> writer.println(emp.toMap().toString()));
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }
    
    public boolean addEmployee(Employee employee) {
        if (employees.containsKey(employee.getEmployeeId())) return false;
        employees.put(employee.getEmployeeId(), employee);
        saveData();
        return true;
    }
    
    public boolean removeEmployee(String employeeId) {
        if (!employees.containsKey(employeeId)) return false;
        employees.remove(employeeId);
        saveData();
        return true;
    }
    
    public Employee findEmployee(String employeeId) { return employees.get(employeeId); }
    
    public List<Employee> findEmployeesByName(String name) {
        List<Employee> result = new ArrayList<>();
        String searchName = name.toLowerCase();
        employees.values().forEach(emp -> {
            if (emp.getName().toLowerCase().contains(searchName)) result.add(emp);
        });
        return result;
    }
    
    public double calculateTotalPayroll() {
        return employees.values().stream().mapToDouble(Employee::calculateSalary).sum();
    }
    
    public void displayAllEmployees() {
        if (employees.isEmpty()) System.out.println("No employees in the system.");
        else employees.values().forEach(System.out::println);
    }
    
    public void generatePayrollReport() {
        if (employees.isEmpty()) {
            System.out.println("No employees in the system.");
            return;
        }
        
        System.out.println("\nPayroll Report:");
        System.out.println("=".repeat(60));
        System.out.printf("%-8s %-20s %-15s %15s%n", "ID", "Name", "Type", "Salary");
        System.out.println("-".repeat(60));
        
        employees.values().forEach(emp -> 
            System.out.printf("%-8s %-20s %-15s ₹%12.2f%n",
                emp.getEmployeeId(), emp.getName(), 
                emp.getClass().getSimpleName(), emp.calculateSalary())
        );
        
        System.out.println("=".repeat(60));
        System.out.printf("Total Payroll: ₹%395.2f%n%n", calculateTotalPayroll());
    }
}

public class EmployeeManagementSystem {
    private static Scanner scanner = new Scanner(System.in);
    
    private static String getInput(String prompt, boolean required) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (required && value.isEmpty()) System.out.println("This field is required.");
            else return value;
        }
    }
    
    private static String getStringInput(String prompt, boolean required, String fieldName) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (required && value.isEmpty()) {
                System.out.println("This field is required.");
                continue;
            }
            if (value.matches(".*\\d.*")) {
                System.out.println(fieldName + " cannot contain numbers.");
                continue;
            }
            return value;
        }
    }
    
    private static double getDoubleInput(String prompt, double minVal) {
        while (true) {
            try {
                System.out.print(prompt);
                double value = Double.parseDouble(scanner.nextLine());
                if (value < minVal) System.out.printf("Value must be at least %.2f.%n", minVal);
                else return value;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
    
    public static void main(String[] args) {
        Company company = new Company();
        
        while (true) {
            System.out.println("\nEmployee Management System");
            System.out.println("1. Add Employee");
            System.out.println("2. Remove Employee");
            System.out.println("3. Find Employee by ID");
            System.out.println("4. Find Employees by Name");
            System.out.println("5. View All Employees");
            System.out.println("6. Generate Payroll Report");
            System.out.println("7. Exit");
            
            String choice = getInput("Enter your choice (1-7): ", true);
            
            switch (choice) {
                case "1": addEmployee(company); break;
                case "2": removeEmployee(company); break;
                case "3": findEmployeeById(company); break;
                case "4": findEmployeesByName(company); break;
                case "5": company.displayAllEmployees(); break;
                case "6": company.generatePayrollReport(); break;
                case "7": 
                    System.out.println("Exiting the system. Goodbye!");
                    scanner.close();
                    return;
                default: System.out.println("Invalid choice. Please enter a number between 1 and 7.");
            }
        }
    }
    
    private static void addEmployee(Company company) {
        System.out.println("\nEmployee Types:");
        System.out.println("1. Full-time");
        System.out.println("2. Part-time");
        System.out.println("3. Manager");
        String empType = getInput("Select employee type (1-3): ", true);
        
        if (!Arrays.asList("1", "2", "3").contains(empType)) {
            System.out.println("Invalid choice. Please try again.");
            return;
        }
        
        String empId = getInput("Enter employee ID: ", true);
        if (company.findEmployee(empId) != null) {
            System.out.println("Employee ID already exists.");
            return;
        }
        
        String name = getStringInput("Enter employee name: ", true, "Name");
        String dept = getStringInput("Enter department: ", true, "Department");
        
        try {
            Employee emp;
            if (empType.equals("1")) {
                double salary = getDoubleInput("Enter monthly salary: ", 0);
                emp = new FullTimeEmployee(empId, name, dept, salary);
            } else if (empType.equals("2")) {
                double rate = getDoubleInput("Enter hourly rate: ", 0);
                double hours = getDoubleInput("Enter hours worked per month: ", 0);
                emp = new PartTimeEmployee(empId, name, dept, rate, hours);
            } else {
                double salary = getDoubleInput("Enter base salary: ", 0);
                double bonus = getDoubleInput("Enter bonus amount: ", 0);
                emp = new Manager(empId, name, dept, salary, bonus);
            }
            
            if (company.addEmployee(emp)) System.out.println("Employee " + name + " added successfully!");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static void removeEmployee(Company company) {
        String empId = getInput("Enter employee ID to remove: ", true);
        System.out.println(company.removeEmployee(empId) ? "Employee removed successfully." : "Employee not found.");
    }
    
    private static void findEmployeeById(Company company) {
        String empId = getInput("Enter employee ID to search: ", true);
        Employee emp = company.findEmployee(empId);
        if (emp != null) {
            System.out.println("\nEmployee Found:");
            System.out.println(emp);
        } else {
            System.out.println("Employee not found.");
        }
    }
    
    private static void findEmployeesByName(Company company) {
        String name = getStringInput("Enter name to search: ", false, "Name");
        List<Employee> employees = company.findEmployeesByName(name);
        if (!employees.isEmpty()) {
            System.out.println("\nMatching Employees:");
            employees.forEach(System.out::println);
        } else {
            System.out.println("No employees found with that name.");
        }
    }
}
