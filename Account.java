import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Account implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String accountNumber;
    private final String accountType;
    private double balance;
    private final List<String> transactions;
    private static final double INTEREST_RATE = 0.02;

    // IMPORTANT: The constructor now takes an account number.
    public Account(String accountNumber, String accountType) {
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = 0.0;
        this.transactions = new ArrayList<>();
    }

    // Getters
    public String getAccountNumber() { return accountNumber; }
    public String getAccountType() { return accountType; }
    public double getBalance() { return balance; }
    public List<String> getTransactions() { return transactions; }

    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
            this.transactions.add("Deposit: +" + new DecimalFormat("#,##0.00").format(amount));
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && this.balance >= amount) {
            this.balance -= amount;
            this.transactions.add("Withdrawal: -" + new DecimalFormat("#,##0.00").format(amount));
            return true;
        }
        return false;
    }

    public void addInterest() {
        double monthlyInterest = (this.balance * INTEREST_RATE) / 12;
        this.balance += monthlyInterest;
        this.transactions.add("Interest Added: +" + new DecimalFormat("#,##0.00").format(monthlyInterest));
    }
}