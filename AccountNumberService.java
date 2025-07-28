import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AccountNumberService {

    private static final String COUNTER_FILE = "account_counter.dat";
    private static long currentAccountNumber;

    // This block initializes the counter when the server starts.
    static {
        try {
            File file = new File(COUNTER_FILE);
            if (file.exists()) {
                String content = new String(Files.readAllBytes(Paths.get(COUNTER_FILE)));
                currentAccountNumber = Long.parseLong(content.trim());
            } else {
                currentAccountNumber = 10000000L; // Starting number for accounts
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error initializing account number: " + e.getMessage());
            currentAccountNumber = 10000000L;
        }
    }

    /**
     * This synchronized method guarantees that every call gets a unique number.
     */
    public static synchronized String getNextAccountNumber() {
        currentAccountNumber++;
        try (PrintWriter out = new PrintWriter(COUNTER_FILE)) {
            out.println(currentAccountNumber);
        } catch (IOException e) {
            System.err.println("Error writing account number to file: " + e.getMessage());
        }
        return String.valueOf(currentAccountNumber);
    }
}