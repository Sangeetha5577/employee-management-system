/**
 * Employee Management System - STQA Academic Project
 * Employee Model Class
 * Represents an employee with all required 10 attributes.
 */
public class Employee {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String gender;
    private String department;
    private String role;
    private double salary;
    private String joiningDate;
    private String status;

    // Default constructor
    public Employee() {
    }

    // Parameterized constructor with all 10 fields
    public Employee(String id, String name, String email, String phone, String gender,
                    String department, String role, double salary, String joiningDate, String status) {
        this.id = id != null ? id.trim() : "";
        this.name = name != null ? name.trim() : "";
        this.email = email != null ? email.trim() : "";
        this.phone = phone != null ? phone.trim() : "";
        this.gender = gender != null ? gender.trim() : "";
        this.department = department != null ? department.trim() : "";
        this.role = role != null ? role.trim() : "";
        this.salary = salary;
        this.joiningDate = joiningDate != null ? joiningDate.trim() : "";
        this.status = status != null ? status.trim() : "Active";
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id != null ? id.trim() : "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : "";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim() : "";
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone.trim() : "";
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender != null ? gender.trim() : "";
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department != null ? department.trim() : "";
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role != null ? role.trim() : "";
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(String joiningDate) {
        this.joiningDate = joiningDate != null ? joiningDate.trim() : "";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status != null ? status.trim() : "Active";
    }

    public String getFormattedSalary() {
        return String.format("%.2f", salary);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", gender='" + gender + '\'' +
                ", department='" + department + '\'' +
                ", role='" + role + '\'' +
                ", salary=" + salary +
                ", joiningDate='" + joiningDate + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
