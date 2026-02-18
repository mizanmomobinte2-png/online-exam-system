# Online Exam System - JavaFX

A desktop application for conducting online exams built with JavaFX, Maven, and MySQL.

## Prerequisites

- **JDK 17** or higher
- **Maven 3.6+**
- **MySQL 8.0+** (or MariaDB)

## Setup

### 1. Database Setup

1. Start MySQL server
2. Run the schema script to create the database and tables:

```bash
mysql -u root -p < src/main/resources/database/schema.sql
```

Or open MySQL Workbench/command line and execute the contents of `src/main/resources/database/schema.sql`

### 2. Configure Database Connection

Edit `src/main/java/com/example/onlineexam/util/DatabaseUtil.java` and update:

- `DB_USER` - your MySQL username (default: root)
- `DB_PASSWORD` - your MySQL password

### 3. Build & Run

**Using Maven (VS Code / Terminal):**

```bash
cd online-exam-system
mvn clean compile
mvn javafx:run
```

Or use the Maven extension in VS Code:
- Run `mvn javafx:run` from the Maven panel
- Or use the play button next to the javafx:run goal

## Features

- **Welcome/Landing Page** - Login and Register options
- **Registration** - Student and Teacher registration with validation
- **Login** - Username or email login with role-based redirection
- **Student Module**
  - Dashboard
  - Available Exams - List and start exams
  - Exam Instructions - Rules before starting
  - Take Exam - Timer, question palette, save & next, auto-submit
  - My Results - View scores and history
- **Teacher Module**
  - Dashboard
  - Create Exam - Manual questions + Excel import
  - My Exams - View, Activate/Deactivate exams
  - Results & Reports - View results, export to Excel

## Excel Import

Teachers can import questions from Excel. Use the "Download Template" button to get the template. Columns:

| Question | Option_A | Option_B | Option_C | Option_D | Correct_Answer | Marks |

## Quick Test

1. Run the app
2. Register as a **Teacher**
3. Login → Create Exam → Add questions → Publish Exam
4. Activate the exam from "My Exams"
5. Register as a **Student**
6. Login → Available Exams → Start Exam

## Project Structure

```
online-exam-system/
├── pom.xml
├── src/main/
│   ├── java/com/example/onlineexam/
│   │   ├── MainApp.java
│   │   ├── controller/     # FXML controllers
│   │   ├── dao/            # Database access
│   │   ├── model/          # Data models
│   │   ├── service/        # Business logic
│   │   └── util/           # DatabaseUtil, ExcelUtil, SceneUtil
│   └── resources/
│       ├── database/schema.sql
│       ├── fxml/           # FXML views
│       └── styles/style.css
```

## Troubleshooting

- **MySQL connection failed**: Ensure MySQL is running and credentials in `DatabaseUtil.java` are correct
- **JavaFX not found**: Use `mvn javafx:run` (not `mvn exec:java`) - the javafx-maven-plugin handles the module path
- **Blank screen**: Check that FXML paths use `/fxml/...` (leading slash for classpath root)
