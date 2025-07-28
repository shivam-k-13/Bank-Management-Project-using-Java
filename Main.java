import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Main {

    // A simple, thread-safe map to store session IDs and their corresponding usernames.
    private static final Map<String, String> sessions = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        
        // Static file handler serves all .html and .css files
        server.createContext("/", new StaticFileHandler());

        // Dynamic action handlers
        server.createContext("/login", new LoginHandler());
        server.createContext("/register", new RegisterHandler());
        server.createContext("/dashboard", new DashboardHandler());
        server.createContext("/transaction", new TransactionHandler());
        server.createContext("/logout", new LogoutHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8000");

        new InterestService().startMonthlyInterestCalculation();
    }

    // --- Dynamic Action Handlers ---

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!"POST".equals(t.getRequestMethod())) {
                sendResponse(t, 405, "Method Not Allowed");
                return;
            }

            Map<String, String> params = parseFormData(t);
            String userType = params.get("userType");
            String username = params.get("username");
            String password = params.get("password");

            boolean loggedIn = false;
            String successRedirectUrl = "/dashboard"; // Default success redirect
            String failureRedirectUrl = "/"; // Default failure redirect

            if (null != userType) switch (userType) {
                case "customer":
                    Customer customer = new CustomerService().getCustomerByUsername(username);
                    if (customer != null && customer.getPassword().equals(password)) {
                        String sessionId = UUID.randomUUID().toString();
                        sessions.put(sessionId, username);
                        t.getResponseHeaders().add("Set-Cookie", "sessionId=" + sessionId + "; Path=/");
                        loggedIn = true;
                    }   failureRedirectUrl = "/customer_portal.html"; // On failure, go back to sign-in page
                    break;
                case "employee":
                    Employee employee = new EmployeeService().getEmployeeByUsername(username);
                    if (employee != null && employee.getPassword().equals(password)) {
                        // Employee doesn't need a session for this example, just show the dashboard
                        sendResponse(t, 200, generateEmployeeDashboard(employee));
                        return;
                    }   failureRedirectUrl = "/employee_login.html";
                    break;
                case "admin":
                    Admin admin = new AdminService().getAdminByUsername(username);
                    if (admin != null && admin.getPassword().equals(password)) {
                        sendResponse(t, 200, generateAdminDashboard());
                        return;
                    }  failureRedirectUrl = "/admin_login.html";
                    break;
                default:
                    break;
            }
            
            if (loggedIn) {
                t.getResponseHeaders().add("Location", successRedirectUrl);
                t.sendResponseHeaders(302, -1);
            } else {
                // Redirect back to the appropriate login page on failure
                t.getResponseHeaders().add("Location", failureRedirectUrl + "?error=1");
                t.sendResponseHeaders(302, -1);
            }
        }
    }
    
    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!"POST".equals(t.getRequestMethod())) { sendResponse(t, 405, "Method Not Allowed"); return; }
            
            Map<String, String> params = parseFormData(t);
            new CustomerService().registerCustomer(new Customer(params.get("name"), params.get("address"), params.get("email"),
                    params.get("phoneNumber"), params.get("username"), params.get("password")));
            
            // On successful registration, redirect the user to the sign-in page to log in.
            t.getResponseHeaders().add("Location", "/customer_portal.html?success=1");
            t.sendResponseHeaders(302, -1);
        }
    }
    
    static class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String sessionId = getCookie(t, "sessionId");
            String username = sessions.get(sessionId);

            if (username == null) {
                // If user is not logged in, send them to the customer sign-in page.
                t.getResponseHeaders().add("Location", "/customer_portal.html");
                t.sendResponseHeaders(302, -1);
                return;
            }

            Customer customer = new CustomerService().getCustomerByUsername(username);
            String response = generateCustomerDashboard(customer);
            sendResponse(t, 200, response);
        }
    }

    static class TransactionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!"POST".equals(t.getRequestMethod())) { sendResponse(t, 405, "Method Not Allowed"); return; }

            String sessionId = getCookie(t, "sessionId");
            String username = sessions.get(sessionId);
            if (username == null) { sendResponse(t, 401, "Unauthorized"); return; }

            Map<String, String> params = parseFormData(t);
            double amount = Double.parseDouble(params.get("amount"));
            String action = params.get("action");
            
            CustomerService customerService = new CustomerService();
            Customer customer = customerService.getCustomerByUsername(username);
            TransactionService transactionService = new TransactionService();

            if ("deposit".equals(action)) {
                transactionService.deposit(customer, "Savings", amount);
            } else if ("withdraw".equals(action)) {
                transactionService.withdraw(customer, "Savings", amount);
            }
            
            // After transaction, refresh the dashboard.
            t.getResponseHeaders().add("Location", "/dashboard");
            t.sendResponseHeaders(302, -1);
        }
    }
    
    static class LogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String sessionId = getCookie(t, "sessionId");
            if (sessionId != null) {
                sessions.remove(sessionId);
            }
            // Expire the cookie in the browser.
            t.getResponseHeaders().add("Set-Cookie", "sessionId=; Path=/; Max-Age=0");
            
            // Redirect to the main index page (main menu) on logout.
            t.getResponseHeaders().add("Location", "/");
            t.sendResponseHeaders(302, -1);
        }
    }

    // --- Static File Handler ---

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String requestedFile = t.getRequestURI().getPath();
            if (requestedFile.equals("/") || requestedFile.equals("")) {
                requestedFile = "/index.html";
            }
            
            String filePath = System.getProperty("user.dir") + requestedFile;
            File file = new java.io.File(filePath);

            if (file.exists()) {
                byte[] response = Files.readAllBytes(file.toPath());
                if(requestedFile.endsWith(".css")) {
                    t.getResponseHeaders().set("Content-Type", "text/css");
                } else if(requestedFile.endsWith(".html")) {
                    t.getResponseHeaders().set("Content-Type", "text/html");
                }
                t.sendResponseHeaders(200, response.length);
                try (OutputStream os = t.getResponseBody()) {
                    os.write(response);
                }
            } else {
                sendResponse(t, 404, "404 Not Found");
            }
        }
    }

    // --- HTML Generation and Helper Methods ---

    private static String generateCustomerDashboard(Customer customer) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        Account savingsAccount = customer.getAccount("Savings");

        // Get the account number. Add a check to prevent errors if the account somehow doesn't exist.
        String accountNumber = (savingsAccount != null) ? savingsAccount.getAccountNumber() : "N/A";
        double balance = (savingsAccount != null) ? savingsAccount.getBalance() : 0.0;
        String transactionsList = (savingsAccount != null) ? 
            savingsAccount.getTransactions().stream().map(tx -> "<li>" + tx + "</li>").collect(Collectors.joining()) : 
            "<li>No transactions found.</li>";

        return "<html><head><title>Customer Dashboard</title><link rel='stylesheet' href='style.css'></head><body>" +
                "<div class='container'>" +
                "<h2>Welcome, " + customer.getName() + "</h2>" +
                
                // Display Account Number and Balance
                "<div class='dashboard-section'><h3>Savings Account: " + accountNumber + "</h3>" +
                "<h2>Balance: $" + df.format(balance) + "</h2></div>" +
                
                // Transaction Forms Section
                "<div class='dashboard-section'><h3>Perform a Transaction</h3>" +
                "<form action='/transaction' method='post' style='margin-bottom: 20px;'>" +
                "<div class='form-group'><label for='amount'>Amount:</label><input type='number' id='amount' name='amount' step='0.01' required></div>" +
                "<input type='hidden' name='action' value='deposit'><button type='submit' style='background-color: #28a745;'>Deposit</button></form>" +
                "<form action='/transaction' method='post'>" +
                "<div class='form-group'><label for='w_amount'>Amount:</label><input type='number' id='w_amount' name='amount' step='0.01' required></div>" +
                "<input type='hidden' name='action' value='withdraw'><button type='submit' style='background-color: #dc3545;'>Withdraw</button></form></div>" +

                // Transaction History Section
                "<div class='dashboard-section'><h3>Transaction History</h3><ul class='transactions'>" +
                transactionsList +
                "</ul></div>" +
                "<a href='/logout'>Logout</a></div></body></html>";
    }

    private static String generateEmployeeDashboard(Employee employee) {
        return "<html><head><title>Employee Dashboard</title><link rel='stylesheet' href='style.css'></head><body>" +
                "<div class='container'><h2>Welcome, " + employee.getName() + "</h2>" +
                "<div class='dashboard-section'><h3>Your Profile</h3>" +
                "<p><strong>Employee ID:</strong> " + employee.getEmployeeId() + "</p>" +
                "<p><strong>Position:</strong> " + employee.getPosition() + "</p>" +
                "</div><p><a href='/'>Back to Main Menu</a></p></div></body></html>";
    }

    private static String generateAdminDashboard() {
        List<Customer> customers = new CustomerService().getAllCustomers();
        List<Employee> employees = new EmployeeService().getAllEmployees();
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Admin Dashboard</title><link rel='stylesheet' href='style.css'></head><body>");
        sb.append("<div class='container'><h2>Admin Dashboard</h2>");
        sb.append("<div class='dashboard-section'><h3>Customers</h3><table>");
        sb.append("<tr><th>Name</th><th>Username</th><th>Email</th><th>Account No.</th></tr>");
        for (Customer c : customers) {
            String accNum = c.getAccount("Savings") != null ? c.getAccount("Savings").getAccountNumber() : "N/A";
            sb.append("<tr><td>").append(c.getName()).append("</td><td>").append(c.getUsername()).append("</td><td>").append(c.getEmail()).append("</td><td>").append(accNum).append("</td></tr>");
        }
        sb.append("</table></div>");
        sb.append("<div class='dashboard-section'><h3>Employees</h3><table>");
        sb.append("<tr><th>Name</th><th>Employee ID</th><th>Position</th></tr>");
        for (Employee e : employees) {
            sb.append("<tr><td>").append(e.getName()).append("</td><td>").append(e.getEmployeeId()).append("</td><td>").append(e.getPosition()).append("</td></tr>");
        }
        sb.append("</table></div><p><a href='/'>Back to Main Menu</a></p></div></body></html>");
        return sb.toString();
    }
    
    private static void sendResponse(HttpExchange t, int statusCode, String response) throws IOException {
        t.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = t.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
    
    private static String getCookie(HttpExchange t, String name) {
        List<String> cookies = t.getRequestHeaders().get("Cookie");
        if (cookies != null) {
            for (String cookieStr : cookies) {
                for (String cookiePair : cookieStr.split(";")) {
                    String[] pair = cookiePair.trim().split("=");
                    if (pair.length == 2 && name.equals(pair[0])) {
                        return pair[1];
                    }
                }
            }
        }
        return null;
    }

    private static Map<String, String> parseFormData(HttpExchange t) throws IOException {
        InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        String query = br.readLine();
        return java.util.Arrays.stream(query.split("&"))
                .map(s -> s.split("=", 2))
                .collect(Collectors.toMap(
                        a -> {
                            try {
                                return URLDecoder.decode(a[0], "UTF-8");
                            } catch (java.io.UnsupportedEncodingException | IllegalArgumentException e) {
                                return a[0];
                            }
                        },
                        a -> {
                            try {
                                return URLDecoder.decode(a.length > 1 ? a[1] : "", "UTF-8");
                            } catch (java.io.UnsupportedEncodingException | IllegalArgumentException e) {
                                return a.length > 1 ? a[1] : "";
                            }
                        }
                ));
    }
}