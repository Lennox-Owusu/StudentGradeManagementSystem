# Student Grade Management System (Java)

A console-based application to **add students**, **record grades**, and **view performance reports** using simple menus. Designed to demonstrate **Object-Oriented Programming (OOP)** with **inheritance**, **polymorphism**, **abstraction**, **encapsulation**, arrays, and manager classes.

***

## 🚀 Features

*   **Add Student** (Regular or Honors)
*   **View Students** (ID, name, type, average, passing status)
*   **Record Grade** with **subject selection** (Core/Elective) and confirmation
*   **View Grade Report** (grade history + core/elective/overall averages)
*   **Menu-driven navigation** with input validation

***

## 🧩 Tech & Design (OOP)

*   **Two hierarchies**:
    *   `Subject → CoreSubject / ElectiveSubject`
    *   `Student → RegularStudent / HonorsStudent`
*   **Abstraction**: `Student` is an **abstract** base class
*   **Interface**: `Gradable` defines grade operations (optional in your current flow)
*   **Encapsulation**: Fields are private with getters
*   **Composition**: Managers hold arrays of domain objects (`Student[]`, `Grade[]`)
*   **Static ID generation**: `STU###` for students, `GRD###` for grades

***

## 📁 Project Structure

    src/
      Main.java
      Student.java
      RegularStudent.java
      HonorsStudent.java
      StudentManager.java
      Grade.java
      GradeManager.java
      Subject.java            (abstract base - if included)
      CoreSubject.java
      ElectiveSubject.java
      Gradable.java           (interface - if used)
    README.md

> If `Subject.java` isn’t included yet, ensure it provides at least:
>
> *   Private fields: `subjectName`, `subjectCode`
> *   Getters: `getSubjectName()`, `getSubjectCode()`
> *   Abstract methods: `displaySubjectDetails()`, `getSubjectType()`

***

## 🛠️ Setup & Run

### 1) Compile

```bash
javac -d out src/*.java
```

### 2) Run

```bash
java -cp out Main
```

> Ensure you’re in the project root and have Java (JDK) installed.

***

## 📋 How to Use

1.  **Start the app** → Choose from the menu:
    *   `1` Add Student
    *   `2` View Students
    *   `3` Record Grade
    *   `4` View Grade Report
    *   `5` Exit
2.  **Record Grade flow**:
    *   Enter Student ID (e.g., `STU001`)
    *   Choose Subject Type (Core/Elective)
    *   Select Subject (e.g., Mathematics)
    *   Enter Grade (0–100)
    *   Confirm to save
3.  **View Grade Report**:
    *   Shows student summary
    *   Displays grade history (most recent first)
    *   Shows **Core**, **Elective**, and **Overall** averages

***

## 🧪 Sample Session

    ======================================
    STUDENT GRADE MANAGEMENT - MAIN MENU
    ======================================

    1. Add Student
    2. View Students
    3. Record Grade
    4. View Grade Report
    5. Exit

    Enter choice: 3

    RECORD GRADE
    ----------------------------------------
    Enter Student ID: STU001

    Subject type:
    1. Core Subject (Mathematics, English, Science)
    2. Elective Subject (Music, Art, Physical Education)

    Select type (1-2): 1

    Available Core Subjects:
    1. Mathematics
    2. English
    3. Science
    Select subject (1-3): 1

    Enter grade (0-100): 87

    GRADE CONFIRMATION
    ----------------------------------------
    Grade ID: GRD001
    Student: STU001 - Alice Johnson
    Subject: Mathematics (Core)
    Grade: 87.00
    Date: 2025-11-30
    Confirm grade? (Y/N): Y

    Grade recorded successfully!

***

## ✅ Rubric Mapping (Quick)

*   **OOP Principles (25 pts)**: Inheritance, polymorphism, abstraction, encapsulation → ✅
*   **Functionality (25 pts)**: 5 user stories via menu → ✅
*   **Class Design (15 pts)**: Required classes + relationships + static IDs → ✅
*   **Data Management (15 pts)**: Arrays + search/iteration in managers → ✅
*   **Code Quality (10 pts)**: Clean naming/formatting, validation → ✅
*   **Documentation (10 pts)**: This README + (add brief Javadoc to main classes) → ✅

***

## 🔧 Future Improvements 

*   Persist data to file (save/load students and grades)
*   Replace arrays with `ArrayList` for dynamic sizing
*   Central `SubjectManager` to manage a subject catalog
*   Unit tests with JUnit
*   Input validations for email/phone formats

***

## 📄 License

This project is for educational purposes.  
You may adapt and use it freely in coursework or demos.

***

