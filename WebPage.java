import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Employee Management System - STQA Academic Project
 * WebPage Class
 * Responsible for loading HTML/CSS files, injecting dynamic employee data,
 * formatting table rows, generating alerts, and performing safe HTML escaping.
 */
public class WebPage {

    /**
     * Resolves and locates a file on disk across all execution directories.
     */
    public static File resolveFile(String fileName) {
        String[] candidatePaths = {
            fileName,
            "employee management system/" + fileName,
            "../" + fileName,
            "../../" + fileName,
            "c:/Users/Sangeetha/OneDrive/Documents/employee management system/" + fileName
        };

        for (String p : candidatePaths) {
            File f = new File(p);
            if (f.exists()) {
                return f;
            }
        }

        try {
            File codeSource = new File(WebPage.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File dir = codeSource.isDirectory() ? codeSource : codeSource.getParentFile();
            while (dir != null) {
                File check = new File(dir, fileName);
                if (check.exists()) {
                    return check;
                }
                dir = dir.getParentFile();
            }
        } catch (Exception ignored) {
        }

        return new File(fileName);
    }

    /**
     * Resolves and reads a file content as a String.
     */
    public static String loadTemplate(String fileName) {
        File file = resolveFile(fileName);

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            } catch (IOException e) {
                System.err.println("[ERROR] Failed to read template " + fileName + ": " + e.getMessage());
            }
        }

        // Fallback: try loading from ClassLoader resource
        try (InputStream is = WebPage.class.getClassLoader().getResourceAsStream(fileName)) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    return sb.toString();
                }
            }
        } catch (Exception ignored) {
        }

        System.err.println("[WARN] Template not found on disk: " + fileName);
        return "";
    }

    /**
     * Escape special HTML characters to prevent XSS.
     */
    public static String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }

    /**
     * Helper to render an alert banner.
     */
    public static String renderAlert(String message, String type) {
        if (message == null || message.trim().isEmpty()) {
            return "";
        }
        String cssClass = "alert-info";
        String icon = "&#8505;&#65039;";
        if ("success".equalsIgnoreCase(type)) {
            cssClass = "alert-success";
            icon = "&#9989;";
        } else if ("danger".equalsIgnoreCase(type) || "error".equalsIgnoreCase(type)) {
            cssClass = "alert-danger";
            icon = "&#9888;&#65039;";
        }
        return "<div class=\"alert " + cssClass + "\"><span>" + icon + "</span> " + escapeHtml(message) + "</div>";
    }

    /**
     * Helper to render a status badge.
     */
    public static String renderStatusBadge(String status) {
        if (status == null) status = "Active";
        if ("Active".equalsIgnoreCase(status)) {
            return "<span class=\"badge badge-active\">Active</span>";
        } else {
            return "<span class=\"badge badge-inactive\">Inactive</span>";
        }
    }

    /**
     * Renders table rows for the employee list.
     */
    public static String renderEmployeeRows(List<Employee> employees) {
        if (employees == null || employees.isEmpty()) {
            return "<tr><td colspan=\"10\" style=\"text-align: center; padding: 30px; color: #718096;\">" +
                   "No employees found. Click 'Add New Employee' to register one.</td></tr>";
        }

        StringBuilder sb = new StringBuilder();
        for (Employee emp : employees) {
            String safeId = escapeHtml(emp.getId());
            String safeName = escapeHtml(emp.getName());
            String safeEmail = escapeHtml(emp.getEmail());
            String safePhone = escapeHtml(emp.getPhone());
            String safeDept = escapeHtml(emp.getDepartment());
            String safeRole = escapeHtml(emp.getRole());
            String safeSalary = escapeHtml(emp.getFormattedSalary());
            String safeDate = escapeHtml(emp.getJoiningDate());
            String statusBadge = renderStatusBadge(emp.getStatus());

            sb.append("<tr>");
            sb.append("<td><strong><code>").append(safeId).append("</code></strong></td>");
            sb.append("<td>").append(safeName).append("</td>");
            sb.append("<td><a href=\"mailto:").append(safeEmail).append("\">").append(safeEmail).append("</a></td>");
            sb.append("<td>").append(safePhone).append("</td>");
            sb.append("<td><span class=\"badge badge-dept\">").append(safeDept).append("</span></td>");
            sb.append("<td>").append(safeRole).append("</td>");
            sb.append("<td>").append(safeSalary).append("</td>");
            sb.append("<td>").append(safeDate).append("</td>");
            sb.append("<td>").append(statusBadge).append("</td>");
            sb.append("<td style=\"text-align: right;\">");
            sb.append("<div class=\"action-buttons\" style=\"justify-content: flex-end;\">");
            sb.append("<a href=\"/employee-profile?id=").append(safeId).append("\" class=\"btn-action btn-view\">&#128065; View</a>");
            sb.append("<a href=\"/edit-employee?id=").append(safeId).append("\" class=\"btn-action btn-edit\">&#9998; Edit</a>");
            sb.append("<a href=\"/delete-employee?id=").append(safeId)
              .append("\" class=\"btn-action btn-delete\" onclick=\"return confirm('Are you sure you want to delete employee ")
              .append(safeId).append(" (").append(safeName).append(")?');\">&#128465; Delete</a>");
            sb.append("</div>");
            sb.append("</td>");
            sb.append("</tr>\n");
        }
        return sb.toString();
    }

    /**
     * Renders recent employee rows on Dashboard.
     */
    public static String renderRecentEmployeeRows(List<Employee> employees) {
        if (employees == null || employees.isEmpty()) {
            return "<tr><td colspan=\"7\" style=\"text-align: center; padding: 25px; color: #718096;\">" +
                   "No recently added employees to display.</td></tr>";
        }

        StringBuilder sb = new StringBuilder();
        for (Employee emp : employees) {
            String safeId = escapeHtml(emp.getId());
            String safeName = escapeHtml(emp.getName());
            String safeDept = escapeHtml(emp.getDepartment());
            String safeRole = escapeHtml(emp.getRole());
            String safeDate = escapeHtml(emp.getJoiningDate());
            String statusBadge = renderStatusBadge(emp.getStatus());

            sb.append("<tr>");
            sb.append("<td><code>").append(safeId).append("</code></td>");
            sb.append("<td><strong>").append(safeName).append("</strong></td>");
            sb.append("<td><span class=\"badge badge-dept\">").append(safeDept).append("</span></td>");
            sb.append("<td>").append(safeRole).append("</td>");
            sb.append("<td>").append(safeDate).append("</td>");
            sb.append("<td>").append(statusBadge).append("</td>");
            sb.append("<td style=\"text-align: right;\">");
            sb.append("<div class=\"action-buttons\" style=\"justify-content: flex-end;\">");
            sb.append("<a href=\"/employee-profile?id=").append(safeId).append("\" class=\"btn-action btn-view\">&#128065; View</a>");
            sb.append("<a href=\"/edit-employee?id=").append(safeId).append("\" class=\"btn-action btn-edit\">&#9998; Edit</a>");
            sb.append("<a href=\"/delete-employee?id=").append(safeId)
              .append("\" class=\"btn-action btn-delete\" onclick=\"return confirm('Are you sure you want to delete employee ")
              .append(safeId).append("?');\">&#128465; Delete</a>");
            sb.append("</div>");
            sb.append("</td>");
            sb.append("</tr>\n");
        }
        return sb.toString();
    }

    /**
     * Renders Login Page.
     */
    public static String renderLogin(String errorMessage) {
        String template = loadTemplate("login.html");
        String alert = "";
        if (errorMessage != null && !errorMessage.trim().isEmpty()) {
            alert = renderAlert(errorMessage, "danger");
        }
        if (template.isEmpty()) {
            template = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Employee Management System - Administrator Login</title>\n" +
                "    <link rel=\"stylesheet\" href=\"/style.css\">\n" +
                "</head>\n" +
                "<body class=\"login-body\" style=\"min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #0f2744 0%, #1a365d 100%); padding: 20px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\">\n" +
                "    <div class=\"login-card\" style=\"background: #ffffff; width: 100%; max-width: 440px; border-radius: 12px; box-shadow: 0 12px 30px rgba(0, 0, 0, 0.25); overflow: hidden;\">\n" +
                "        <div class=\"login-header\" style=\"background: #0a1b30; color: #ffffff; padding: 32px 24px; text-align: center;\">\n" +
                "            <div class=\"login-logo\" style=\"width: 58px; height: 58px; background: #2b6cb0; border-radius: 14px; display: inline-flex; align-items: center; justify-content: center; font-size: 30px; margin-bottom: 12px;\">&#128188;</div>\n" +
                "            <h1 style=\"font-size: 21px; font-weight: 700; color: #ffffff;\">Employee Management System</h1>\n" +
                "            <p style=\"font-size: 13px; color: #a0aec0; margin-top: 4px; text-transform: uppercase;\">Administrator Portal</p>\n" +
                "        </div>\n" +
                "        <div class=\"login-form-container\" style=\"padding: 32px 28px; background-color: #ffffff;\">\n" +
                "            {{ALERT_MESSAGE}}\n" +
                "            <form action=\"/login\" method=\"POST\">\n" +
                "                <div class=\"form-group\" style=\"margin-bottom: 18px; display: flex; flex-direction: column;\">\n" +
                "                    <label for=\"username\" style=\"font-size: 13px; font-weight: 600; color: #4a5568; margin-bottom: 6px;\">Username <span style=\"color: #e53e3e;\">*</span></label>\n" +
                "                    <input type=\"text\" id=\"username\" name=\"username\" class=\"form-control\" style=\"width: 100%; padding: 12px 14px; border: 1px solid #cbd5e0; border-radius: 6px; font-size: 14px;\" placeholder=\"Enter administrator username\" required autofocus autocomplete=\"username\">\n" +
                "                </div>\n" +
                "                <div class=\"form-group\" style=\"margin-bottom: 24px; display: flex; flex-direction: column;\">\n" +
                "                    <label for=\"password\" style=\"font-size: 13px; font-weight: 600; color: #4a5568; margin-bottom: 6px;\">Password <span style=\"color: #e53e3e;\">*</span></label>\n" +
                "                    <input type=\"password\" id=\"password\" name=\"password\" class=\"form-control\" style=\"width: 100%; padding: 12px 14px; border: 1px solid #cbd5e0; border-radius: 6px; font-size: 14px;\" placeholder=\"Enter administrator password\" required autocomplete=\"current-password\">\n" +
                "                </div>\n" +
                "                <button type=\"submit\" class=\"btn btn-primary\" style=\"display: block; width: 100%; padding: 13px; border-radius: 6px; font-size: 15px; font-weight: 600; text-align: center; border: none; cursor: pointer; background-color: #1a365d; color: #ffffff;\">\n" +
                "                    Sign In to Dashboard &rarr;\n" +
                "                </button>\n" +
                "            </form>\n" +
                "            <div class=\"login-hint\" style=\"background: #ebf8ff; border: 1px solid #bee3f8; color: #2b6cb0; padding: 12px 14px; border-radius: 8px; font-size: 13px; margin-top: 20px; text-align: center;\">\n" +
                "                <strong>Administrator Credentials:</strong><br>\n" +
                "                Username: <code style=\"background: rgba(43, 108, 176, 0.15); padding: 2px 6px; border-radius: 4px; font-weight: bold;\">sanman</code> &nbsp;|&nbsp; Password: <code style=\"background: rgba(43, 108, 176, 0.15); padding: 2px 6px; border-radius: 4px; font-weight: bold;\">sanman123</code>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
        }
        return template.replace("{{ALERT_MESSAGE}}", alert);
    }

    /**
     * Renders Dashboard Page.
     */
    public static String renderDashboard(EmployeeService.DashboardStats stats, String alertMessage, String alertType) {
        String template = loadTemplate("dashboard.html");
        String alert = renderAlert(alertMessage, alertType);

        if (template.isEmpty()) {
            return "<!DOCTYPE html><html><head><title>Dashboard</title><link rel=\"stylesheet\" href=\"/style.css\"></head><body>" +
                   "<h1>Administrator Dashboard</h1><p>Total Employees: " + stats.totalEmployees + "</p>" +
                   "<p><a href=\"/employees\">View Employees</a> | <a href=\"/add-employee\">Add Employee</a></p></body></html>";
        }

        return template
                .replace("{{ALERT_MESSAGE}}", alert)
                .replace("{{TOTAL_EMPLOYEES}}", String.valueOf(stats.totalEmployees))
                .replace("{{ACTIVE_EMPLOYEES}}", String.valueOf(stats.activeEmployees))
                .replace("{{INACTIVE_EMPLOYEES}}", String.valueOf(stats.inactiveEmployees))
                .replace("{{TOTAL_DEPARTMENTS}}", String.valueOf(stats.totalDepartments))
                .replace("{{RECENT_EMPLOYEE_ROWS}}", renderRecentEmployeeRows(stats.recentEmployees));
    }

    /**
     * Renders Employee Directory List Page.
     */
    public static String renderEmployeeList(List<Employee> employees, String alertMessage, String alertType) {
        String template = loadTemplate("employee-list.html");
        String alert = renderAlert(alertMessage, alertType);

        if (template.isEmpty()) {
            return "<!DOCTYPE html><html><head><title>Employee Directory</title><link rel=\"stylesheet\" href=\"/style.css\"></head><body>" +
                   "<h1>Employee Directory</h1>" + alert + "<table border='1'>" + renderEmployeeRows(employees) + "</table></body></html>";
        }

        return template
                .replace("{{ALERT_MESSAGE}}", alert)
                .replace("{{TOTAL_COUNT}}", String.valueOf(employees != null ? employees.size() : 0))
                .replace("{{EMPLOYEE_ROWS}}", renderEmployeeRows(employees));
    }

    /**
     * Renders Add Employee Page.
     */
    public static String renderAddEmployee(Employee formValues, String alertMessage, String alertType) {
        String template = loadTemplate("add-employee.html");
        String alert = renderAlert(alertMessage, alertType);

        String id = formValues != null ? escapeHtml(formValues.getId()) : "";
        String name = formValues != null ? escapeHtml(formValues.getName()) : "";
        String email = formValues != null ? escapeHtml(formValues.getEmail()) : "";
        String phone = formValues != null ? escapeHtml(formValues.getPhone()) : "";
        String gender = formValues != null ? formValues.getGender() : "";
        String dept = formValues != null ? formValues.getDepartment() : "";
        String role = formValues != null ? escapeHtml(formValues.getRole()) : "";
        String salary = (formValues != null && formValues.getSalary() > 0) ? formValues.getFormattedSalary() : "";
        String date = formValues != null ? escapeHtml(formValues.getJoiningDate()) : "";
        String status = formValues != null ? formValues.getStatus() : "Active";

        return template
                .replace("{{ALERT_MESSAGE}}", alert)
                .replace("{{VAL_ID}}", id)
                .replace("{{VAL_NAME}}", name)
                .replace("{{VAL_EMAIL}}", email)
                .replace("{{VAL_PHONE}}", phone)
                .replace("{{SEL_GENDER_MALE}}", "Male".equalsIgnoreCase(gender) ? "selected" : "")
                .replace("{{SEL_GENDER_FEMALE}}", "Female".equalsIgnoreCase(gender) ? "selected" : "")
                .replace("{{SEL_GENDER_OTHER}}", "Other".equalsIgnoreCase(gender) ? "selected" : "")
                .replace("{{SEL_DEPT_IT}}", "IT".equalsIgnoreCase(dept) ? "selected" : "")
                .replace("{{SEL_DEPT_HR}}", "HR".equalsIgnoreCase(dept) ? "selected" : "")
                .replace("{{SEL_DEPT_FIN}}", "Finance".equalsIgnoreCase(dept) ? "selected" : "")
                .replace("{{SEL_DEPT_MKT}}", "Marketing".equalsIgnoreCase(dept) ? "selected" : "")
                .replace("{{SEL_DEPT_OPS}}", "Operations".equalsIgnoreCase(dept) ? "selected" : "")
                .replace("{{SEL_DEPT_QA}}", "Quality Assurance".equalsIgnoreCase(dept) ? "selected" : "")
                .replace("{{VAL_ROLE}}", role)
                .replace("{{VAL_SALARY}}", salary)
                .replace("{{VAL_DATE}}", date)
                .replace("{{SEL_STATUS_ACTIVE}}", "Active".equalsIgnoreCase(status) ? "selected" : "")
                .replace("{{SEL_STATUS_INACTIVE}}", "Inactive".equalsIgnoreCase(status) ? "selected" : "");
    }

    /**
     * Renders Update / Edit Employee Page.
     */
    public static String renderUpdateEmployee(Employee emp, String alertMessage, String alertType) {
        String template = loadTemplate("update-employee.html");
        String alert = renderAlert(alertMessage, alertType);

        if (emp == null) {
            return template
                    .replace("{{ALERT_MESSAGE}}", renderAlert("Employee record not found.", "danger"))
                    .replace("{{VAL_ID}}", "")
                    .replace("{{VAL_NAME}}", "")
                    .replace("{{VAL_EMAIL}}", "")
                    .replace("{{VAL_PHONE}}", "")
                    .replace("{{SEL_GENDER_MALE}}", "")
                    .replace("{{SEL_GENDER_FEMALE}}", "")
                    .replace("{{SEL_GENDER_OTHER}}", "")
                    .replace("{{SEL_DEPT_IT}}", "")
                    .replace("{{SEL_DEPT_HR}}", "")
                    .replace("{{SEL_DEPT_FIN}}", "")
                    .replace("{{SEL_DEPT_MKT}}", "")
                    .replace("{{SEL_DEPT_OPS}}", "")
                    .replace("{{SEL_DEPT_QA}}", "")
                    .replace("{{VAL_ROLE}}", "")
                    .replace("{{VAL_SALARY}}", "")
                    .replace("{{VAL_DATE}}", "")
                    .replace("{{SEL_STATUS_ACTIVE}}", "")
                    .replace("{{SEL_STATUS_INACTIVE}}", "");
        }

        return template
                .replace("{{ALERT_MESSAGE}}", alert)
                .replace("{{VAL_ID}}", escapeHtml(emp.getId()))
                .replace("{{VAL_NAME}}", escapeHtml(emp.getName()))
                .replace("{{VAL_EMAIL}}", escapeHtml(emp.getEmail()))
                .replace("{{VAL_PHONE}}", escapeHtml(emp.getPhone()))
                .replace("{{SEL_GENDER_MALE}}", "Male".equalsIgnoreCase(emp.getGender()) ? "selected" : "")
                .replace("{{SEL_GENDER_FEMALE}}", "Female".equalsIgnoreCase(emp.getGender()) ? "selected" : "")
                .replace("{{SEL_GENDER_OTHER}}", "Other".equalsIgnoreCase(emp.getGender()) ? "selected" : "")
                .replace("{{SEL_DEPT_IT}}", "IT".equalsIgnoreCase(emp.getDepartment()) ? "selected" : "")
                .replace("{{SEL_DEPT_HR}}", "HR".equalsIgnoreCase(emp.getDepartment()) ? "selected" : "")
                .replace("{{SEL_DEPT_FIN}}", "Finance".equalsIgnoreCase(emp.getDepartment()) ? "selected" : "")
                .replace("{{SEL_DEPT_MKT}}", "Marketing".equalsIgnoreCase(emp.getDepartment()) ? "selected" : "")
                .replace("{{SEL_DEPT_OPS}}", "Operations".equalsIgnoreCase(emp.getDepartment()) ? "selected" : "")
                .replace("{{SEL_DEPT_QA}}", "Quality Assurance".equalsIgnoreCase(emp.getDepartment()) ? "selected" : "")
                .replace("{{VAL_ROLE}}", escapeHtml(emp.getRole()))
                .replace("{{VAL_SALARY}}", emp.getFormattedSalary())
                .replace("{{VAL_DATE}}", escapeHtml(emp.getJoiningDate()))
                .replace("{{SEL_STATUS_ACTIVE}}", "Active".equalsIgnoreCase(emp.getStatus()) ? "selected" : "")
                .replace("{{SEL_STATUS_INACTIVE}}", "Inactive".equalsIgnoreCase(emp.getStatus()) ? "selected" : "");
    }

    /**
     * Renders Employee Profile Page.
     */
    public static String renderEmployeeProfile(Employee emp, String alertMessage, String alertType) {
        String template = loadTemplate("employee-profile.html");
        String alert = renderAlert(alertMessage, alertType);

        if (emp == null) {
            return template
                    .replace("{{ALERT_MESSAGE}}", renderAlert("Employee record not found.", "danger"))
                    .replace("{{EMP_INITIAL}}", "?")
                    .replace("{{EMP_NAME}}", "Not Found")
                    .replace("{{EMP_ROLE}}", "-")
                    .replace("{{EMP_DEPT}}", "-")
                    .replace("{{EMP_ID}}", "-")
                    .replace("{{EMP_EMAIL}}", "-")
                    .replace("{{EMP_PHONE}}", "-")
                    .replace("{{EMP_GENDER}}", "-")
                    .replace("{{EMP_SALARY}}", "-")
                    .replace("{{EMP_JOIN_DATE}}", "-")
                    .replace("{{EMP_STATUS_BADGE}}", "-");
        }

        String initial = emp.getName().length() > 0 ? String.valueOf(emp.getName().toUpperCase().charAt(0)) : "E";

        return template
                .replace("{{ALERT_MESSAGE}}", alert)
                .replace("{{EMP_INITIAL}}", initial)
                .replace("{{EMP_NAME}}", escapeHtml(emp.getName()))
                .replace("{{EMP_ROLE}}", escapeHtml(emp.getRole()))
                .replace("{{EMP_DEPT}}", escapeHtml(emp.getDepartment()))
                .replace("{{EMP_ID}}", escapeHtml(emp.getId()))
                .replace("{{EMP_EMAIL}}", escapeHtml(emp.getEmail()))
                .replace("{{EMP_PHONE}}", escapeHtml(emp.getPhone()))
                .replace("{{EMP_GENDER}}", escapeHtml(emp.getGender()))
                .replace("{{EMP_SALARY}}", "₹ " + escapeHtml(emp.getFormattedSalary()))
                .replace("{{EMP_JOIN_DATE}}", escapeHtml(emp.getJoiningDate()))
                .replace("{{EMP_STATUS_BADGE}}", renderStatusBadge(emp.getStatus()));
    }

    /**
     * Renders Search Employee Page.
     */
    public static String renderSearchEmployee(String query, List<Employee> results, String alertMessage, String alertType) {
        String template = loadTemplate("search-employee.html");
        String alert = renderAlert(alertMessage, alertType);

        String resultTitle;
        String rowsHtml;

        if (query == null || query.trim().isEmpty()) {
            resultTitle = "Search Results";
            rowsHtml = "<tr><td colspan=\"10\" style=\"text-align: center; padding: 25px; color: #718096;\">" +
                       "Enter an Employee ID or Name above and click Search.</td></tr>";
        } else if (results == null || results.isEmpty()) {
            resultTitle = "Search Results for \"" + escapeHtml(query) + "\" (0 found)";
            rowsHtml = "<tr><td colspan=\"10\" style=\"text-align: center; padding: 30px; color: #e53e3e; font-weight: bold;\">" +
                       "Employee not found.</td></tr>";
        } else {
            resultTitle = "Search Results for \"" + escapeHtml(query) + "\" (" + results.size() + " found)";
            rowsHtml = renderEmployeeRows(results);
        }

        return template
                .replace("{{ALERT_MESSAGE}}", alert)
                .replace("{{SEARCH_QUERY}}", query != null ? escapeHtml(query) : "")
                .replace("{{SEARCH_RESULT_TITLE}}", resultTitle)
                .replace("{{SEARCH_RESULT_ROWS}}", rowsHtml);
    }

    /**
     * Serves style.css content.
     */
    public static String getCssContent() {
        return loadTemplate("style.css");
    }
}
