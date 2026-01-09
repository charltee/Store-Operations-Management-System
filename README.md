# Store Operations Management System

A Java-based application designed to streamline and manage the daily operations of a retail store chain. This project was developed as an assignment for the **WIX1002 Fundamentals of Programming** course (Session 2025/2026) at Universiti Malaya.

## 📖 Overview

This system provides a console-based interface for administrators and managers to handle essential store functions, ranging from employee management to sales tracking. It utilizes CSV files for persistent data storage, ensuring that records are saved and retrieved efficiently between sessions.

## ✨ Key Features

* **Employee Management**: Add, update, and remove employee records.
* **Attendance Tracking**: Log employee clock-in and clock-out times to monitor attendance.
* **Sales Management**: Record sales transactions and generate basic sales reports.
* **Inventory / Model Management**: Manage product models and stock information.
* **Outlet Management**: Handle details for multiple store branches/outlets.
* **Data Persistence**: All data is stored locally in CSV files (`employee.csv`, `sales.csv`, `attendance.csv`, etc.), making the system lightweight and portable.

## 📂 Project Structure

```text
Store-Operations-Management-System/
├── src/            # Java source code files
├── bin/            # Compiled Java bytecode
├── .vscode/        # VS Code configuration settings
├── employee.csv    # Database for employee records
├── attendance.csv  # Database for attendance logs
├── sales.csv       # Database for sales transactions
├── model.csv       # Database for product/inventory models
├── outlet.csv      # Database for store outlet locations
└── README.md       # Project documentation
