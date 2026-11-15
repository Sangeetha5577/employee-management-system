# Filename is ReadMe

# EMPLOYEE MANAGEMENT SYSTEM (EMS)
### Third-Year Engineering Software Testing and Quality Assurance (STQA) Academic Project

---

## 1. PROJECT OVERVIEW

The **Employee Management System** is a complete, lightweight, professional workforce management web application developed using **Java 17's built-in HTTP server (`com.sun.net.httpserver.HttpServer`)**, **HTML5**, **CSS3**, and **CSV data persistence**.

The application requires **zero external frameworks** (no Spring Boot, no Node.js, no external database like MySQL/MongoDB). It runs out-of-the-box on standard Java 17.

* **Default Server Port:** `8080`
* **Local Web Address:** `http://localhost:8080`
* **Administrator Username:** `sanman`
* **Administrator Password:** `sanman123`

---

## 2. TECHNOLOGIES USED

* **Backend:** Java 17 (Built-in `com.sun.net.httpserver.HttpServer`, standard `java.io`, `java.util`)
* **Frontend:** HTML5 (Semantic forms & inputs), CSS3 (Responsive corporate dashboard theme)
* **Data Storage:** Comma-Separated Values (`employees.csv`) with RFC 4180 quote escaping
* **Security:** Cookie-based session tracking (`EMS_AUTH_TOKEN`), HTML sanitization against XSS
* **Architecture:** 3-Tier Model-View-Controller (MVC)

---

## 3. PROJECT DIRECTORY STRUCTURE

```
employee management system/
│
├── ReadMe.md                   # Project documentation & execution guide (Filename: ReadMe)
├── employees.csv               # Persistent CSV data storage (auto-created if missing)
├── style.css                   # Professional corporate dashboard stylesheet
│
├── login.html                  # Administrator login page
├── dashboard.html              # Admin dashboard with 4 live stat cards & recent employees
├── add-employee.html           # 10-field employee registration form with validation
├── employee-list.html          # Full employee directory with View, Edit, Delete actions
├── search-employee.html        # Search console supporting ID or partial name lookup
├── update-employee.html        # Edit employee record (Employee ID locked as readonly)
├── employee-profile.html       # Individual employee profile view
│
└── src/
    └── main/
        └── java/
            ├── Main.java           # Server entry point on port 8080 & HTTP route controllers
            ├── Employee.java       # Model class with 10 fields and getters/setters
            ├── EmployeeFile.java   # CSV parser, RFC 4180 quote escaping, file I/O
            ├── EmployeeService.java# Validation rules, CRUD logic, search, dynamic statistics
            ├── LoginService.java   # Auth verification (sanman / sanman123) & session tokens
            └── WebPage.java        # Template loader, XSS sanitization, dynamic HTML builder
```

---

## 4. HOW TO RUN THE APPLICATION

### Option A: In Visual Studio Code (Fastest & Easiest)
1. Open the project folder in VS Code:
   `c:\Users\Sangeetha\OneDrive\Documents\employee management system`
2. In the left file explorer, open **`src/main/java/` $\rightarrow$ `Main.java`**.
3. Click the **`Run`** button that appears directly above:
   ```java
   public static void main(String[] args)
   ```
4. Look at the terminal at the bottom. You will see:
   ```
   ====================================================
     Employee Management System (EMS) Started!         
     Server Port : 8080
     URL         : http://localhost:8080
     Credentials : sanman / sanman123                  
   ====================================================
   ```

---

### Option B: Using Terminal / PowerShell / Command Prompt
Open your terminal in the project root directory and run:

```powershell
# 1. Compile all Java source files
javac src/main/java/*.java

# 2. Start the server
java -cp src/main/java Main
```

---

### Option C: In Eclipse IDE
1. Open Eclipse and choose **File $\rightarrow$ Open Projects from File System...**.
2. Select the `employee management system` directory.
3. Right-click **`Main.java`** $\rightarrow$ **Run As $\rightarrow$ Java Application**.

---

## 5. ACCESSING THE WEB APPLICATION

1. Open **Google Chrome** (or Edge / Firefox).
2. Go to: **`http://localhost:8080`**  
   *(Or `http://localhost:8080/logout` to force a fresh login screen)*
3. Enter administrator credentials:
   * **Username:** `sanman`
   * **Password:** `sanman123`
4. Click **"Sign In to Dashboard →"**.

---

## 6. APPLICATION FEATURES & DEMONSTRATION WORKFLOW

### 1. Administrator Login (`login.html`)
* Secure credentials verification (`sanman` / `sanman123`).
* Invalid attempts display a red alert banner: *"Invalid username or password. Please try again."*
* Session cookie (`EMS_AUTH_TOKEN`) is issued on successful login.
* Direct URL access to internal pages without logging in is intercepted and redirected to `/login`.

### 2. Admin Dashboard (`dashboard.html`)
* **4 Dynamic Stat Cards:**
  * **Total Employees:** Live count calculated directly from `employees.csv`.
  * **Active Employees:** Tally of active workforce members.
  * **Inactive Employees:** Tally of inactive workforce members.
  * **Total Departments:** Count of distinct departments (IT, HR, Finance, etc.).
* **Recently Added Employees:** Table showing newest additions in reverse chronological order with working action buttons.

### 3. Add Employee (`add-employee.html`)
* Comprehensive registration form capturing all 10 required fields:
  1. `Employee ID` (e.g. `EMP106`)
  2. `Employee Full Name`
  3. `Email Address` (validated with RFC standard email regex)
  4. `Phone Number` (validated strictly for 10 numeric digits)
  5. `Gender` (Male / Female / Other)
  6. `Department` (IT, HR, Finance, Marketing, Operations, Quality Assurance)
  7. `Job Role`
  8. `Salary` (validated strictly for values $> 0.00$)
  9. `Joining Date` (validated for `YYYY-MM-DD` ISO format)
  10. `Employee Status` (Active / Inactive)
* **Duplicate ID Prevention:** Blocks registration if an Employee ID already exists and displays a clear red error message while keeping entered values in the form.
* Saves directly to `employees.csv` and updates dashboard stats automatically.

### 4. Employee Directory (`employee-list.html`)
* Renders a responsive table displaying all 10 columns.
* Includes working action buttons on every row:
  * **View:** Opens the dedicated employee profile page.
  * **Edit:** Opens the update form with pre-populated data.
  * **Delete:** Triggers a safe browser confirmation dialog before deletion.

### 5. View Employee Profile (`employee-profile.html`)
* Displays an individual card view for the selected employee.
* Shows profile initial avatar, designation header, and all 10 formatted attributes.
* Provides **"Edit Profile"** and **"Back to Employee List"** buttons.

### 6. Edit Employee Record (`update-employee.html`)
* Pre-fills all current employee information.
* **Primary Key Immutability:** The **Employee ID** input field is strictly `readonly` and cannot be altered by the user or backend.
* Saves modifications to `employees.csv` and displays a green success alert.

### 7. Safe Deletion Flow
* Clicking **Delete** prompts: *"Are you sure you want to delete employee EMP...?"*.
* Clicking **Cancel** cancels the operation with zero changes.
* Clicking **OK** removes the record from `employees.csv`, refreshes the table, and updates dashboard counters.

### 8. Directory Search (`search-employee.html`)
* Search input accepts both **Employee ID** and **Employee Name**.
* Supports **case-insensitive** and **partial substring** matching (e.g. typing `john` matches `John Smith` and `Sarah Johnson`).
* If no record matches, displays: **`"Employee not found."`**.

### 9. Persistent CSV Storage (`employees.csv`)
* Automatically creates `employees.csv` on first startup if missing, populated with 5 realistic sample records.
* Uses an RFC 4180-compliant state-machine parser in `EmployeeFile.java` that handles cells containing commas and quotation marks without splitting columns.
* Data remains permanently saved across application restarts.

---

## 7. CODE & CLASS DESCRIPTIONS

| Class / File | Responsibility |
| :--- | :--- |
| **`Main.java`** | Starts Java's built-in `HttpServer` on port 8080, registers 12 route contexts, decodes URL query strings and POST bodies with `URLDecoder`, and enforces session cookies. |
| **`Employee.java`** | Model class encapsulating all 10 employee fields with constructors, getters, setters, and `getFormattedSalary()`. |
| **`EmployeeFile.java`** | Handles low-level reading and writing of `employees.csv`. Implements quote escaping and state-machine CSV line parsing. |
| **`EmployeeService.java`** | Contains core business logic: duplicate ID checks, 10-digit phone regex, email regex, positive salary verification, CRUD methods, search algorithm, and live statistics calculation. |
| **`LoginService.java`** | Manages credentials (`ADMIN_USERNAME = "sanman"`, `ADMIN_PASSWORD = "sanman123"`), in-memory UUID session tokens (`EMS_SESSION_...`), and cookie authentication. |
| **`WebPage.java`** | Locates HTML/CSS files on disk, sanitizes user content via `escapeHtml()` to prevent XSS attacks, and dynamically renders table rows, badges, and alerts. |

---

## 8. ACADEMIC EVALUATION & VIVA CHEAT SHEET

### Q1: Why did you not use Spring Boot or MySQL?
> **Answer:**  
> *"Java 17 provides a production-grade HTTP server in `com.sun.net.httpserver.HttpServer`. Using pure Java demonstrates fundamental computer science principles: socket listening, request dispatching, session cookies, input validation, and file I/O concurrency without third-party framework abstraction."*

### Q2: How is data preserved when commas are inside names or job roles?
> **Answer:**  
> *"In `EmployeeFile.java`, we implemented RFC 4180-compliant quote escaping. Any field containing commas or quotes is wrapped in double quotes (`\"`), and internal quotes are escaped as `\"\"`. The parser tracks open and closed quote states so commas inside quotes are not mistaken for column delimiters."*

### Q3: How do you protect against Cross-Site Scripting (XSS)?
> **Answer:**  
> *"In `WebPage.java`, all user inputs pass through `escapeHtml()` before rendering into HTML templates. Characters like `<`, `>`, `&`, and `\"` are converted into HTML entities (`&lt;`, `&gt;`, `&amp;`, `&quot;`), preventing script injection in the browser."*

---

*STQA Academic Project © 2026 | Employee Management System (EMS)*
