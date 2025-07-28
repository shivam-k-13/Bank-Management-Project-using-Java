import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class InterestService {

    public void startMonthlyInterestCalculation() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                addInterestToAllAccounts();
            }
        }, 0, 1000L * 60 * 60 * 24 * 30); // Roughly every 30 days
    }

    private void addInterestToAllAccounts() {
        CustomerService customerService = new CustomerService();
        List<Customer> customers = customerService.getAllCustomers();
        for (Customer customer : customers) {
            for (Account account : customer.getAccounts()) {
                account.addInterest();
            }
            customerService.updateCustomer(customer);
        }
        System.out.println("Monthly interest has been added to all customer accounts.");
    }
}