import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
public class EmployeeFile {
    private static final String CSV_HEADER = "id,name,email,phone,gender,department,role,salary,joiningDate,status";
    private static final String FILE_NAME = "employees.csv";
    private static File getFile() {
        String[] candidatePaths = { FILE_NAME, "employee management system/" + FILE_NAME, "../" + FILE_NAME, "../../" + FILE_NAME, "c:/Users/Sangeetha/OneDrive/Documents/employee management system/" + FILE_NAME };
        for (String p : candidatePaths) { File f = new File(p); if (f.exists()) { return f; } }
        try {
            File codeSource = new File(EmployeeFile.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File dir = codeSource.isDirectory() ? codeSource : codeSource.getParentFile();
            while (dir != null) { File check = new File(dir, FILE_NAME); if (check.exists()) { return check; } dir = dir.getParentFile(); }
        } catch (Exception ignored) {}
        return new File(FILE_NAME);
    }
    public static synchronized void initialize() {
        File file = getFile();
        if (!file.exists() || file.length() < 90) {
            try {
                if (file.getParentFile() != null) { file.getParentFile().mkdirs(); }
                try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                    writer.println(CSV_HEADER);
                    writer.println("EMP101,John Smith,john.smith@company.com,9876543210,Male,IT,Software Engineer,75000.00,2023-01-15,Active");
                    writer.println("EMP102,Sarah Johnson,sarah.j@company.com,9876543211,Female,HR,HR Manager,65000.00,2022-06-10,Active");
                    writer.println("EMP103,Michael Brown,michael.b@company.com,9876543212,Male,Finance,Financial Analyst,60000.00,2023-03-20,Active");
                    writer.println("EMP104,Emily Davis,emily.d@company.com,9876543213,Female,Marketing,Marketing Lead,58000.00,2021-11-05,Inactive");
                    writer.println("EMP105,Robert Wilson,robert.w@company.com,9876543214,Male,Operations,Operations Head,72000.00,2022-09-01,Active");
                }
                System.out.println("[INFO] Created initial " + file.getAbsolutePath());
            } catch (IOException e) { System.err.println("[ERROR] Failed to initialize CSV file: " + e.getMessage()); }
        }
    }
    public static synchronized List<Employee> readEmployees() {
        List<Employee> list = new ArrayList<>(); File file = getFile();
        if (!file.exists() || file.length() < 90) { initialize(); }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line; boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) { continue; }
                if (isFirstLine) { isFirstLine = false; if (line.toLowerCase().startsWith("id,")) { continue; } }
                Employee emp = parseCsvLine(line); if (emp != null) { list.add(emp); }
            }
        } catch (IOException e) { System.err.println("[ERROR] Error reading CSV file: " + e.getMessage()); }
        if (list.isEmpty()) {
            initialize();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line; boolean isFirstLine = true;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) { continue; }
                    if (isFirstLine) { isFirstLine = false; if (line.toLowerCase().startsWith("id,")) { continue; } }
                    Employee emp = parseCsvLine(line); if (emp != null) { list.add(emp); }
                }
            } catch (IOException ignored) {}
        }
        return list;
    }
    public static synchronized boolean writeEmployees(List<Employee> employees) {
        File file = getFile();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {
            writer.println(CSV_HEADER); for (Employee emp : employees) { writer.println(formatCsvLine(emp)); } return true;
        } catch (IOException e) { System.err.println("[ERROR] Error writing CSV file: " + e.getMessage()); return false; }
    }
    public static String formatCsvLine(Employee e) {
        return escapeField(e.getId()) + "," + escapeField(e.getName()) + "," + escapeField(e.getEmail()) + "," + escapeField(e.getPhone()) + "," + escapeField(e.getGender()) + "," + escapeField(e.getDepartment()) + "," + escapeField(e.getRole()) + "," + e.getFormattedSalary() + "," + escapeField(e.getJoiningDate()) + "," + escapeField(e.getStatus());
    }
    private static String escapeField(String val) {
        if (val == null) return ""; if (val.contains(",") || val.contains("\"") || val.contains("\n")) { return "\"" + val.replace("\"", "\"\"") + "\""; } return val;
    }
    public static Employee parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>(); StringBuilder sb = new StringBuilder(); boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') { if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') { sb.append('\"'); i++; } else { inQuotes = !inQuotes; } }
            else if (c == ',' && !inQuotes) { tokens.add(sb.toString().trim()); sb.setLength(0); } else { sb.append(c); }
        }
        tokens.add(sb.toString().trim());
        if (tokens.size() >= 10) {
            String id = tokens.get(0); String name = tokens.get(1); String email = tokens.get(2); String phone = tokens.get(3); String gender = tokens.get(4); String dept = tokens.get(5); String role = tokens.get(6); double salary = 0.0;
            try { salary = Double.parseDouble(tokens.get(7)); } catch (NumberFormatException ignored) {}
            String date = tokens.get(8); String status = tokens.get(9);
            return new Employee(id, name, email, phone, gender, dept, role, salary, date, status);
        }
        return null;
    }
}