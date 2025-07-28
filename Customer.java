import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date; 
import java.util.List;

public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String address;
    private final String email;
    private final String phoneNumber;
    private final String username;
    private final String password;
    private final List<Account> accounts;
    private final Date accountCreationDate; // NEW FIELD

    public Customer(String name, String address, String email, String phoneNumber, String username, String password) {
        this.name = name;
        this.address = address;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.username = username;
        this.password = password;
        this.accounts = new ArrayList<>();
        this.accountCreationDate = new Date(); // Set the date when the object is created
    }

    // Getters
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public List<Account> getAccounts() { return accounts; }
    public Date getAccountCreationDate() { return accountCreationDate; } // NEW GETTER

    public Account getAccount(String accountType) {
        for (Account account : accounts) {
            if (account.getAccountType().equalsIgnoreCase(accountType)) {
                return account;
            }
        }
        return null;
    }
}