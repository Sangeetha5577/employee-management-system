import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Employee Management System - STQA Academic Project
 * Main Class
 * Initializes CSV storage, configures HTTP server on port 8080,
 * and routes all frontend/backend interactions.
 */
public class Main {

    private static final int PORT = 8080;
    private static final EmployeeService employeeService = new EmployeeService();
    private static final LoginService loginService = new LoginService();

    public static void main(String[] args) {
        try {
            // 1. Ensure CSV storage exists
            EmployeeFile.initialize();

            // 2. Start Java built-in HTTP server on port 8080
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            // 3. Register HTTP Route Handlers
            server.createContext("/", new RootHandler());
            server.createContext("/login", new LoginHandler());
            server.createContext("/logout", new LogoutHandler());
            server.createContext("/dashboard", new DashboardHandler());
            server.createContext("/employees", new EmployeeListHandler());
            server.createContext("/add-employee", new AddEmployeeHandler());
            server.createContext("/edit-employee", new EditEmployeeHandler());
            server.createContext("/update-employee", new UpdateEmployeeHandler());
            server.createContext("/delete-employee", new DeleteEmployeeHandler());
            server.createContext("/employee-profile", new ProfileHandler());
            server.createContext("/view-employee", new ProfileHandler()); // alias
            server.createContext("/search", new SearchHandler());
            server.createContext("/style.css", new CssHandler());

            server.setExecutor(null); // default executor
            server.start();

            System.out.println("====================================================");
            System.out.println("  Employee Management System (EMS) Started!         ");
            System.out.println("  Server Port : " + PORT);
            System.out.println("  URL         : http://localhost:" + PORT);
            System.out.println("  Credentials : sanman / sanman123                  ");
            System.out.println("====================================================");

        } catch (IOException e) {
            System.err.println("[FATAL] Failed to start HTTP server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================================================================
    // Authentication & Session Guard
    // =========================================================================

    /**
     * Checks if current request carries a valid admin session cookie.
     */
    private static boolean isAuthenticated(HttpExchange exchange) {
        List<String> cookies = exchange.getRequestHeaders().get("Cookie");
        if (cookies != null) {
            for (String cookieHeader : cookies) {
                String token = loginService.extractSessionToken(cookieHeader);
                if (token != null && loginService.isValidSession(token)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Redirects unauthenticated requests to /login.
     */
    private static void redirectToLogin(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Location", "/login");
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    /**
     * Redirects to a specified URL.
     */
    private static void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    /**
     * Sends an HTML response with UTF-8 encoding.
     */
    private static void sendHtml(HttpExchange exchange, int statusCode, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Parses application/x-www-form-urlencoded body from POST request.
     */
    private static Map<String, String> parseFormData(HttpExchange exchange) throws IOException {
        Map<String, String> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String body = sb.toString();
            if (!body.isEmpty()) {
                String[] pairs = body.split("&");
                for (String pair : pairs) {
                    String[] kv = pair.split("=", 2);
                    String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                    String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
                    map.put(key, value);
                }
            }
        }
        return map;
    }

    /**
     * Parses query string parameters from URL (e.g. ?id=EMP101&msg=hello).
     */
    private static Map<String, String> parseQueryParams(HttpExchange exchange) {
        Map<String, String> map = new HashMap<>();
        String query = exchange.getRequestURI().getQuery();
        if (query != null && !query.trim().isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                try {
                    String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                    String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
                    map.put(key, value);
                } catch (Exception ignored) {
                }
            }
        }
        return map;
    }

    // =========================================================================
    // HTTP Handlers
    // =========================================================================

    /**
     * Root Handler: Redirects to /dashboard if logged in, else to /login.
     */
    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (isAuthenticated(exchange)) {
                redirect(exchange, "/dashboard");
            } else {
                redirect(exchange, "/login");
            }
        }
    }

    /**
     * Login Handler: Displays login form (GET) and authenticates administrator (POST).
     */
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            if ("GET".equalsIgnoreCase(method)) {
                if (isAuthenticated(exchange)) {
                    redirect(exchange, "/dashboard");
                    return;
                }
                String html = WebPage.renderLogin("");
                sendHtml(exchange, 200, html);

            } else if ("POST".equalsIgnoreCase(method)) {
                Map<String, String> form = parseFormData(exchange);
                String username = form.get("username");
                String password = form.get("password");

                if (loginService.authenticate(username, password)) {
                    String token = loginService.createSession();
                    exchange.getResponseHeaders().add("Set-Cookie", "EMS_AUTH_TOKEN=" + token + "; Path=/; HttpOnly");
                    redirect(exchange, "/dashboard");
                } else {
                    String html = WebPage.renderLogin("Invalid username or password. Please try again.");
                    sendHtml(exchange, 200, html);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    /**
     * Logout Handler: Invalidates session and clears cookie.
     */
    static class LogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<String> cookies = exchange.getRequestHeaders().get("Cookie");
            if (cookies != null) {
                for (String c : cookies) {
                    String token = loginService.extractSessionToken(c);
                    if (token != null) {
                        loginService.invalidateSession(token);
                    }
                }
            }
            exchange.getResponseHeaders().add("Set-Cookie", "EMS_AUTH_TOKEN=; Path=/; Max-Age=0; HttpOnly");
            redirect(exchange, "/login");
        }
    }

    /**
     * Dashboard Handler: Calculates stats and displays workforce overview.
     */
    static class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!isAuthenticated(exchange)) {
                redirectToLogin(exchange);
                return;
            }

            Map<String, String> params = parseQueryParams(exchange);
            String msg = params.get("msg");
            String type = params.getOrDefault("type", "info");

            EmployeeService.DashboardStats stats = employeeService.calculateStatistics();
            String html = WebPage.renderDashboard(stats, msg, type);
            sendHtml(exchange, 200, html);
        }
    }

    /**
     * Employee List Handler: Displays table of all employees.
     */
    static class EmployeeListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!isAuthenticated(exchange)) {
                redirectToLogin(exchange);
                return;
            }

            Map<String, String> params = parseQueryParams(exchange);
            String msg = params.get("msg");
            String type = params.getOrDefault("type", "info");

            List<Employee> list = employeeService.getAllEmployees();
            String html = WebPage.renderEmployeeList(list, msg, type);
            sendHtml(exchange, 200, html);
        }
    }

    /**
     * Add Employee Handler: Registration form (GET) & record persistence (POST).
     */
    static class AddEmployeeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!isAuthenticated(exchange)) {
                redirectToLogin(exchange);
                return;
            }

            String method = exchange.getRequestMethod();

            if ("GET".equalsIgnoreCase(method)) {
                String html = WebPage.renderAddEmployee(null, "", "");
                sendHtml(exchange, 200, html);

            } else if ("POST".equalsIgnoreCase(method)) {
                Map<String, String> form = parseFormData(exchange);
                String id = form.get("id");
                String name = form.get("name");
                String email = form.get("email");
                String phone = form.get("phone");
                String gender = form.get("gender");
                String department = form.get("department");
                String role = form.get("role");
                double salary = 0.0;
                try {
                    salary = Double.parseDouble(form.get("salary"));
                } catch (Exception ignored) {
                }
                String joiningDate = form.get("joiningDate");
                String status = form.get("status");

                Employee newEmp = new Employee(id, name, email, phone, gender, department, role, salary, joiningDate, status);
                EmployeeService.ValidationResult validation = employeeService.validateEmployee(newEmp, true);

                if (!validation.isValid()) {
                    // Re-render form with entered values and error message
                    String html = WebPage.renderAddEmployee(newEmp, validation.getMessage(), "danger");
                    sendHtml(exchange, 200, html);
                } else {
                    boolean saved = employeeService.addEmployee(newEmp);
                    if (saved) {
                        // Success message
                        String html = WebPage.renderAddEmployee(null, "Employee '" + newEmp.getName() + "' (ID: " + newEmp.getId() + ") registered successfully!", "success");
                        sendHtml(exchange, 200, html);
                    } else {
                        String html = WebPage.renderAddEmployee(newEmp, "Failed to save employee to CSV storage. Please check disk permissions.", "danger");
                        sendHtml(exchange, 200, html);
                    }
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    /**
     * Edit Employee Handler: Opens existing details in form for editing.
     */
    static class EditEmployeeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!isAuthenticated(exchange)) {
                redirectToLogin(exchange);
                return;
            }

            Map<String, String> params = parseQueryParams(exchange);
            String id = params.get("id");
            Employee emp = employeeService.getEmployeeById(id);

            if (emp == null) {
                redirect(exchange, "/employees?msg=Employee+not+found.&type=danger");
                return;
            }

            String html = WebPage.renderUpdateEmployee(emp, "", "");
            sendHtml(exchange, 200, html);
        }
    }

    /**
     * Update Employee Handler: Persists modifications to CSV while ensuring Employee ID is unchanged.
     */
    static class UpdateEmployeeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!isAuthenticated(exchange)) {
                redirectToLogin(exchange);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            Map<String, String> form = parseFormData(exchange);
            String id = form.get("id");
            String name = form.get("name");
            String email = form.get("email");
            String phone = form.get("phone");
            String gender = form.get("gender");
            String department = form.get("department");
            String role = form.get("role");
            double salary = 0.0;
            try {
                salary = Double.parseDouble(form.get("salary"));
            } catch (Exception ignored) {
            }
            String joiningDate = form.get("joiningDate");
            String status = form.get("status");

            Employee updatedEmp = new Employee(id, name, email, phone, gender, department, role, salary, joiningDate, status);
            EmployeeService.ValidationResult validation = employeeService.validateEmployee(updatedEmp, false);

            if (!validation.isValid()) {
                String html = WebPage.renderUpdateEmployee(updatedEmp, validation.getMessage(), "danger");
                sendHtml(exchange, 200, html);
            } else {
                boolean success = employeeService.updateEmployee(updatedEmp);
                if (success) {
                    redirect(exchange, "/employees?msg=Employee+" + id + "+updated+successfully.&type=success");
                } else {
                    String html = WebPage.renderUpdateEmployee(updatedEmp, "Failed to update employee in CSV storage.", "danger");
                    sendHtml(exchange, 200, html);
                }
            }
        }
    }

    /**
     * Delete Employee Handler: Deletes employee record after confirmation.
     */
    static class DeleteEmployeeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!isAuthenticated(exchange)) {
                redirectToLogin(exchange);
                return;
            }

            Map<String, String> params = parseQueryParams(exchange);
            String id = params.get("id");

            if (id == null || id.trim().isEmpty()) {
                redirect(exchange, "/employees?msg=Invalid+employee+ID.&type=danger");
                return;
            }

            boolean deleted = employeeService.deleteEmployee(id);
            if (deleted) {
                redirect(exchange, "/employees?msg=Employee+" + id + "+deleted+successfully.&type=success");
            } else {
                redirect(exchange, "/employees?msg=Could+not+delete+employee+" + id + "+(Record+not+found).&type=danger");
            }
        }
    }

    /**
     * Employee Profile Handler: Shows detailed record for an employee.
     */
    static class ProfileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!isAuthenticated(exchange)) {
                redirectToLogin(exchange);
                return;
            }

            Map<String, String> params = parseQueryParams(exchange);
            String id = params.get("id");
            Employee emp = employeeService.getEmployeeById(id);

            String html = WebPage.renderEmployeeProfile(emp, "", "");
            sendHtml(exchange, 200, html);
        }
    }

    /**
     * Search Handler: Queries employees by ID or partial Name.
     */
    static class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!isAuthenticated(exchange)) {
                redirectToLogin(exchange);
                return;
            }

            String query = null;
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> form = parseFormData(exchange);
                query = form.get("query");
            } else {
                Map<String, String> params = parseQueryParams(exchange);
                query = params.get("query");
            }

            List<Employee> results = null;
            if (query != null && !query.trim().isEmpty()) {
                results = employeeService.searchEmployees(query);
            }

            String html = WebPage.renderSearchEmployee(query, results, "", "");
            sendHtml(exchange, 200, html);
        }
    }

    /**
     * CSS Handler: Serves style.css.
     */
    static class CssHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String css = WebPage.getCssContent();
            byte[] bytes = css.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/css; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
