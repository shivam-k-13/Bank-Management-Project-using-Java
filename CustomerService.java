import java.util.List;

public class CustomerService {
    private static final String CUSTOMER_FILE = "customers.dat";
    private final List<Customer> customers;

    public CustomerService() {
        this.customers = FileUtil.readFromFile(CUSTOMER_FILE);
    }

    public List<Customer> getAllCustomers() {
        return customers;
    }

    public Customer getCustomerByUsername(String username) {
        for (Customer customer : customers) {
            if (customer.getUsername().equals(username)) {
                return customer;
            }
        }
        return null;
    }

    public void registerCustomer(Customer customer) {
        // This logic now correctly uses the other classes.
        // 1. Get a new unique account number from our service.
        String newAccountNumber = AccountNumberService.getNextAccountNumber();

        // 2. Create a "Savings" account using the new constructor.
        Account savingsAccount = new Account(newAccountNumber, "Savings");

        // 3. Add this new account to the customer's list of accounts.
        customer.getAccounts().add(savingsAccount);

        // 4. Add the fully formed customer to our main list and save to file.
        customers.add(customer);
        FileUtil.writeToFile(CUSTOMER_FILE, customers);
    }

    public void updateCustomer(Customer updatedCustomer) {
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getUsername().equals(updatedCustomer.getUsername())) {
                customers.set(i, updatedCustomer);
                break;
            }
        }
        FileUtil.writeToFile(CUSTOMER_FILE, customers);
    }
}