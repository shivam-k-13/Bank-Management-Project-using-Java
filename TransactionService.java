public class TransactionService {

    private static final double MAX_DEPOSIT = 10000.0;

    public boolean deposit(Customer customer, String accountType, double amount) {
        if (amount > MAX_DEPOSIT) {
            return false;
        }
        Account account = customer.getAccount(accountType);
        if (account != null) {
            account.deposit(amount);
            new CustomerService().updateCustomer(customer);
            return true;
        }
        return false;
    }

    public boolean withdraw(Customer customer, String accountType, double amount) {
        Account account = customer.getAccount(accountType);
        if (account != null) {
            boolean success = account.withdraw(amount);
            if (success) {
                new CustomerService().updateCustomer(customer);
            }
            return success;
        }
        return false;
    }
}