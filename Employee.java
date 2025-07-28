import java.io.Serializable;

public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String employeeId;
    private final String name;
    private final String position;
    private final String username;
    private final String password;

    public Employee(String employeeId, String name, String position, String username, String password) {
        this.employeeId = employeeId;
        this.name = name;
        this.position = position;
        this.username = username;
        this.password = password;
    }

    // Getters
    public String getEmployeeId() { return employeeId; }
    public String getName() { return name; }
    public String getPosition() { return position; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}