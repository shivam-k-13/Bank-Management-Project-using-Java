import java.io.Serializable;

public class Admin implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String username;
    private final String password;

    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}