import java.util.*;
import java.util.regex.Pattern;

/**
 * Employee Management System - STQA Academic Project
 * EmployeeService Class
 * Contains all business logic, validation, data operations, and statistics.
 */
public class EmployeeService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    /**
     * Retrieve all employees from CSV.
     */
    public List<Employee> getAllEmployees() {
        return EmployeeFile.readEmployees();
    }

    /**
     * Find employee by unique ID.
     */
    public Employee getEmployeeById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        for (Employee emp : getAllEmployees()) {
            if (emp.getId().equalsIgnoreCase(id.trim())) {
                return emp;
            }
        }
        return null;
    }

    /**
     * Validation Result Helper Class
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Validates employee information.
     * @param emp The employee object to validate
     * @param isNew true when adding new employee, false when updating
     */
    public ValidationResult validateEmployee(Employee emp, boolean isNew) {
        if (emp == null) {
            return new ValidationResult(false, "Employee information cannot be empty.");
        }

        // 1. Employee ID check
        if (emp.getId() == null || emp.getId().trim().isEmpty()) {
            return new ValidationResult(false, "Employee ID is required.");
        }

        if (isNew) {
            // Check uniqueness for new registration
            if (getEmployeeById(emp.getId()) != null) {
                return new ValidationResult(false, "Employee ID '" + emp.getId() + "' already exists! Please use a unique ID.");
            }
        }

        // 2. Name check
        if (emp.getName() == null || emp.getName().trim().isEmpty()) {
            return new ValidationResult(false, "Employee Name is required.");
        }

        // 3. Email validation
        if (emp.getEmail() == null || emp.getEmail().trim().isEmpty()) {
            return new ValidationResult(false, "Email address is required.");
        }
        if (!EMAIL_PATTERN.matcher(emp.getEmail().trim()).matches()) {
            return new ValidationResult(false, "Invalid email format! Example: user@domain.com");
        }

        // 4. Phone validation
        if (emp.getPhone() == null || emp.getPhone().trim().isEmpty()) {
            return new ValidationResult(false, "Phone number is required.");
        }
        if (!PHONE_PATTERN.matcher(emp.getPhone().trim()).matches()) {
            return new ValidationResult(false, "Phone number must be exactly 10 digits.");
        }

        // 5. Gender check
        if (emp.getGender() == null || emp.getGender().trim().isEmpty()) {
            return new ValidationResult(false, "Please select Gender.");
        }

        // 6. Department check
        if (emp.getDepartment() == null || emp.getDepartment().trim().isEmpty()) {
            return new ValidationResult(false, "Please select a Department.");
        }

        // 7. Role check
        if (emp.getRole() == null || emp.getRole().trim().isEmpty()) {
            return new ValidationResult(false, "Job Role is required.");
        }

        // 8. Salary check
        if (emp.getSalary() <= 0) {
            return new ValidationResult(false, "Salary must be greater than zero.");
        }

        // 9. Joining Date check
        if (emp.getJoiningDate() == null || emp.getJoiningDate().trim().isEmpty()) {
            return new ValidationResult(false, "Joining Date is required.");
        }
        if (!DATE_PATTERN.matcher(emp.getJoiningDate().trim()).matches()) {
            return new ValidationResult(false, "Invalid Joining Date format. Expected YYYY-MM-DD.");
        }

        // 10. Status check
        if (emp.getStatus() == null || emp.getStatus().trim().isEmpty()) {
            return new ValidationResult(false, "Please select Employee Status.");
        }

        return new ValidationResult(true, "Validation successful.");
    }

    /**
     * Adds a new employee to the CSV file after validation.
     */
    public boolean addEmployee(Employee emp) {
        List<Employee> list = getAllEmployees();
        list.add(emp);
        return EmployeeFile.writeEmployees(list);
    }

    /**
     * Updates an existing employee in the CSV file while keeping Employee ID unchanged.
     */
    public boolean updateEmployee(Employee updatedEmp) {
        if (updatedEmp == null || updatedEmp.getId() == null) {
            return false;
        }

        List<Employee> list = getAllEmployees();
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            Employee existing = list.get(i);
            if (existing.getId().equalsIgnoreCase(updatedEmp.getId())) {
                // Ensure ID remains unchanged
                updatedEmp.setId(existing.getId());
                list.set(i, updatedEmp);
                found = true;
                break;
            }
        }

        if (found) {
            return EmployeeFile.writeEmployees(list);
        }
        return false;
    }

    /**
     * Deletes an employee by ID from the CSV file.
     */
    public boolean deleteEmployee(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        List<Employee> list = getAllEmployees();
        boolean removed = list.removeIf(e -> e.getId().equalsIgnoreCase(id.trim()));
        if (removed) {
            return EmployeeFile.writeEmployees(list);
        }
        return false;
    }

    /**
     * Searches employees by ID or Name.
     * Supports case-insensitive and partial match searches.
     */
    public List<Employee> searchEmployees(String query) {
        List<Employee> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return results;
        }

        String lowerQuery = query.trim().toLowerCase();
        for (Employee emp : getAllEmployees()) {
            boolean matchId = emp.getId().toLowerCase().contains(lowerQuery);
            boolean matchName = emp.getName().toLowerCase().contains(lowerQuery);
            if (matchId || matchName) {
                results.add(emp);
            }
        }
        return results;
    }

    /**
     * Dashboard Statistics Model
     */
    public static class DashboardStats {
        public int totalEmployees;
        public int activeEmployees;
        public int inactiveEmployees;
        public int totalDepartments;
        public List<Employee> recentEmployees;

        public DashboardStats(int totalEmployees, int activeEmployees, int inactiveEmployees,
                              int totalDepartments, List<Employee> recentEmployees) {
            this.totalEmployees = totalEmployees;
            this.activeEmployees = activeEmployees;
            this.inactiveEmployees = inactiveEmployees;
            this.totalDepartments = totalDepartments;
            this.recentEmployees = recentEmployees;
        }
    }

    /**
     * Calculates all dashboard statistics dynamically from the actual CSV data.
     */
    public DashboardStats calculateStatistics() {
        List<Employee> list = getAllEmployees();
        int total = list.size();
        int active = 0;
        int inactive = 0;
        Set<String> departments = new HashSet<>();

        for (Employee emp : list) {
            if ("Active".equalsIgnoreCase(emp.getStatus())) {
                active++;
            } else {
                inactive++;
            }
            if (emp.getDepartment() != null && !emp.getDepartment().trim().isEmpty()) {
                departments.add(emp.getDepartment().trim().toLowerCase());
            }
        }

        // Get last 5 recently added employees (reversed so newest is on top)
        List<Employee> recent = new ArrayList<>(list);
        Collections.reverse(recent);
        if (recent.size() > 5) {
            recent = recent.subList(0, 5);
        }

        return new DashboardStats(total, active, inactive, departments.size(), recent);
    }
}
