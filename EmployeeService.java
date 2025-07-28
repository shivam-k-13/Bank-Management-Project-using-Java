import java.util.List;

public class EmployeeService {
    private static final String EMPLOYEE_FILE = "employees.dat";
    private final List<Employee> employees;

    public EmployeeService() {
        this.employees = FileUtil.readFromFile(EMPLOYEE_FILE);
        
        // If the file was empty or didn't exist, create default employees
        if (this.employees.isEmpty()) {
            System.out.println("No employees found. Creating default employee data.");
            
            // Add default employees
            this.employees.add(new Employee("E101", "John Teller", "Bank Teller", "employee", "password123"));
            this.employees.add(new Employee("M505", "Jane Manager", "Branch Manager", "manager", "password456"));
            
            // Save the new default employees to the file for future use
            FileUtil.writeToFile(EMPLOYEE_FILE, this.employees);
        }
    }

    public List<Employee> getAllEmployees() { return employees; }

    public Employee getEmployeeByUsername(String username) {
        for (Employee employee : employees) {
            if (employee.getUsername().equals(username)) {
                return employee;
            }
        }
        return null;
    }
}