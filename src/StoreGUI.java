import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class StoreGUI extends Application {

    private Stage window;
    
    // --- Session Data ---
    private String currentUserID = "";
    private String currentUserName = "";
    private String currentUserRole = "";
    private String currentOutletCode = "";
    private String currentOutletName = "";
    private LocalTime timeIn;
    
    // --- Email Configuration ---
    private final String STUDENT_EMAIL = "ccharltondunstan@gmail.com"; 
    private final String GMAIL_USERNAME = "ccharltondunstan@gmail.com";  
    private final String GMAIL_APP_PASSWORD = "ytuq ntfw ghfb rhud"; 

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("Store Operations Management System");
        createDummyFilesIfMissing(); 
        showLoginScreen();
        window.show();
    }

    // --- Login Screen ---
    private void showLoginScreen() {
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        Label title = new Label("System Login");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField userField = new TextField(); userField.setPromptText("User ID");
        PasswordField passField = new PasswordField(); passField.setPromptText("Password");
        
        Button loginBtn = new Button("Login");
        Label statusLabel = new Label();

        loginBtn.setOnAction(e -> {
            String id = userField.getText().trim();
            String pass = passField.getText().trim();
            
            if (performLogin(id, pass)) {
                currentUserID = id;
                timeIn = LocalTime.now().withNano(0);
                if (id.length() >= 3) currentOutletCode = id.substring(0, 3);
                currentOutletName = getOutletNameFromFile(currentOutletCode);
                showMainMenu();
            } else {
                statusLabel.setText("Invalid User ID or Password");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        });

        layout.getChildren().addAll(title, userField, passField, loginBtn, statusLabel);
        window.setScene(new Scene(layout, 400, 350));
    }

    // --- Main Menu ---
    private void showMainMenu() {
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Label welcome = new Label("Welcome, " + currentUserName);
        welcome.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label roleLbl = new Label("Role: " + currentUserRole);
        Label info = new Label("Outlet: " + currentOutletName + " (" + currentOutletCode + ")");

        Button btn1 = new Button("1. Stock Management");
        Button btn2 = new Button("2. Sales System");
        Button btn3 = new Button("3. Sales History (Filter/Sort)"); 
        Button btn4 = new Button("4. Edit Information");
        Button btn5 = new Button("5. Reports & Analytics");
        Button btn6 = new Button("6. Clock-out");

        setBtnWidth(btn1, btn2, btn3, btn4, btn5, btn6);

        btn1.setOnAction(e -> showStockMenu());
        btn2.setOnAction(e -> showSalesScreen());
        btn3.setOnAction(e -> showHistoryFilterScreen()); 
        btn4.setOnAction(e -> showEditMenu());
        btn5.setOnAction(e -> showAnalyticsMenu());
        btn6.setOnAction(e -> performClockOut());

        layout.getChildren().addAll(welcome, roleLbl, info, new Separator(), btn1, btn2, btn3, btn4, btn5);

        // Manager Options
        if (currentUserRole.equalsIgnoreCase("Manager")) {
            Button btnReg = new Button("★ Register New Employee");
            btnReg.setMinWidth(250);
            btnReg.setAlignment(Pos.BASELINE_LEFT);
            btnReg.setStyle("-fx-text-fill: blue;");
            btnReg.setOnAction(e -> showRegisterEmployeeScreen());
            layout.getChildren().add(btnReg);
        }

        layout.getChildren().add(btn6);
        window.setScene(new Scene(layout, 400, 600));
    }

    // --- Register Employee Screen ---
    private void showRegisterEmployeeScreen() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(20));
        grid.setVgap(10); grid.setHgap(10);

        Label title = new Label("Register New Employee");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextField newId = new TextField(); newId.setPromptText("Employee ID");
        TextField newName = new TextField(); newName.setPromptText("Full Name");
        TextField newRole = new TextField(); newRole.setPromptText("Role (e.g. Staff, Manager)");
        TextField newPass = new TextField(); newPass.setPromptText("Password");
        
        Button saveBtn = new Button("Register");
        Button backBtn = new Button("Back");
        Label status = new Label();

        saveBtn.setOnAction(e -> {
            if(newId.getText().isEmpty() || newName.getText().isEmpty() || newPass.getText().isEmpty()) {
                status.setText("All fields required."); return;
            }
            try (BufferedWriter w = new BufferedWriter(new FileWriter("employee.csv", true))) {
                w.write(newId.getText() + "," + newName.getText() + "," + newRole.getText() + "," + newPass.getText());
                w.newLine();
                status.setText("Employee Registered!");
                status.setStyle("-fx-text-fill: green;");
                newId.clear(); newName.clear(); newRole.clear(); newPass.clear();
            } catch (Exception ex) { status.setText("Error writing file."); }
        });

        backBtn.setOnAction(e -> showMainMenu());

        grid.add(title, 0, 0, 2, 1);
        grid.add(new Label("ID:"), 0, 1); grid.add(newId, 1, 1);
        grid.add(new Label("Name:"), 0, 2); grid.add(newName, 1, 2);
        grid.add(new Label("Role:"), 0, 3); grid.add(newRole, 1, 3);
        grid.add(new Label("Pass:"), 0, 4); grid.add(newPass, 1, 4);
        grid.add(saveBtn, 1, 5);
        grid.add(backBtn, 1, 6);
        grid.add(status, 0, 7, 2, 1);

        window.setScene(new Scene(grid, 400, 400));
    }

    // --- Stock Management Menu ---
    private void showStockMenu() {
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Label title = new Label("Stock Management");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Button btnViewAll = new Button("View All Inventory"); 
        Button btnCount = new Button("Morning/Night Stock Count");
        Button btnMove = new Button("Stock Movement (In/Out)");
        Button btnBack = new Button("Back");
        setBtnWidth(btnViewAll, btnCount, btnMove, btnBack);

        btnViewAll.setOnAction(e -> showFullInventoryScreen());
        btnCount.setOnAction(e -> showStockCountScreen());
        btnMove.setOnAction(e -> showStockMovementScreen());
        btnBack.setOnAction(e -> showMainMenu());

        layout.getChildren().addAll(title, btnViewAll, btnCount, btnMove, new Separator(), btnBack);
        window.setScene(new Scene(layout, 400, 400));
    }
    
    // --- Full Inventory Screen ---
    private void showFullInventoryScreen() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        
        Label title = new Label("Current Inventory (Read-Only)");
        title.setStyle("-fx-font-weight: bold;");
        
        TableView<ObservableList<String>> table = new TableView<>();
        
        try (Scanner sc = new Scanner(new File("model.csv"))) {
            if (sc.hasNextLine()) {
                String headerLine = sc.nextLine();
                String[] headers = headerLine.split(",");
                
                // Create Columns Dynamically
                for (int i = 0; i < headers.length; i++) {
                    final int colIndex = i;
                    TableColumn<ObservableList<String>, String> col = new TableColumn<>(headers[i].trim());
                    col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(colIndex)));
                    table.getColumns().add(col);
                }
                
                // Load Data
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    if(!line.trim().isEmpty()){
                        ObservableList<String> row = FXCollections.observableArrayList(line.split(","));
                        table.getItems().add(row);
                    }
                }
            }
        } catch (Exception e) { 
            layout.getChildren().add(new Label("Error loading model.csv - File may not exist yet.")); 
        }

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> showStockMenu());
        
        layout.getChildren().addAll(title, table, backBtn);
        window.setScene(new Scene(layout, 600, 400));
    }

    // --- Stock Count Screen ---
    private void showStockCountScreen() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        Label instr = new Label("Enter Model to check count vs system:");
        TextField modelInput = new TextField(); modelInput.setPromptText("Model Name");
        TextField physInput = new TextField(); physInput.setPromptText("Physical Count");
        Button checkBtn = new Button("Compare");
        TextArea resultArea = new TextArea(); resultArea.setEditable(false);
        Button backBtn = new Button("Back");

        checkBtn.setOnAction(e -> {
            String m = modelInput.getText();
            String pStr = physInput.getText();
            if(m.isEmpty() || pStr.isEmpty()) { resultArea.setText("Please fill fields."); return; }
            int sysQty = getStockQty(m, currentOutletCode);
            try {
                int physQty = Integer.parseInt(pStr);
                String res = "Model: " + m + "\nSystem Record: " + sysQty + "\nPhysical Count: " + physQty;
                if(sysQty == physQty) res += "\nResult: Tally Correct.";
                else res += "\nResult: MISMATCH (" + Math.abs(sysQty - physQty) + ")";
                resultArea.setText(res);
            } catch (Exception ex) { resultArea.setText("Invalid number."); }
        });

        backBtn.setOnAction(e -> showStockMenu());
        layout.getChildren().addAll(instr, modelInput, physInput, checkBtn, resultArea, backBtn);
        window.setScene(new Scene(layout, 400, 400));
    }

    // --- Stock Movement Screen ---
    private void showStockMovementScreen() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10); grid.setHgap(10);
        grid.setAlignment(Pos.CENTER);
        Label title = new Label("Stock Movement"); title.setStyle("-fx-font-weight: bold");
        ComboBox<String> typeBox = new ComboBox<>(); typeBox.getItems().addAll("Stock In", "Stock Out"); typeBox.setValue("Stock In");
        TextField fromToField = new TextField(); fromToField.setPromptText("Source/Destination");
        TextField modelField = new TextField(); modelField.setPromptText("Model Name");
        TextField qtyField = new TextField(); qtyField.setPromptText("Quantity");
        Button processBtn = new Button("Process & Receipt"); Button backBtn = new Button("Back"); Label status = new Label();

        processBtn.setOnAction(e -> {
            String type = typeBox.getValue();
            String location = fromToField.getText(); 
            String model = modelField.getText();
            String qtyStr = qtyField.getText();
            
            if(model.isEmpty() || qtyStr.isEmpty() || location.isEmpty()) { 
                status.setText("Fill all fields."); return; 
            }
            
            try {
                int qty = Integer.parseInt(qtyStr);
                // For Stock In, we pass negative qty to 'qtySold' param so it adds instead of subtracts
                int adjustQty = type.equals("Stock In") ? -qty : qty;
                
                boolean stockUpdated = updateStockInCSV(model, adjustQty, currentOutletCode);
                
                if (stockUpdated) {
                    try(BufferedWriter w = new BufferedWriter(new FileWriter("receipts_" + LocalDate.now() + ".txt", true))) {
                        String from = (type.equals("Stock In")) ? location : currentOutletCode;
                        String to = (type.equals("Stock In")) ? currentOutletCode : location;
                        w.write("\n=== "+type+" ===\nFrom: "+from+"\nTo: "+to+"\n" + model + " (Qty: " + qtyStr + ")\nUser: " + currentUserName + "\n----------------\n");
                        status.setText("Success! Stock Updated & Receipt generated."); 
                        status.setStyle("-fx-text-fill: green;");
                    }
                } else {
                    status.setText("Error: Model not found or Insufficient Stock.");
                    status.setStyle("-fx-text-fill: red;");
                }
            } catch (Exception ex) { status.setText("Error: Invalid Quantity or File Access."); }
        });
        
        backBtn.setOnAction(e -> showStockMenu());
        grid.add(title, 0, 0, 2, 1); grid.add(new Label("Type:"), 0, 1); grid.add(typeBox, 1, 1);
        grid.add(new Label("From/To:"), 0, 2); grid.add(fromToField, 1, 2);
        grid.add(new Label("Model:"), 0, 3); grid.add(modelField, 1, 3);
        grid.add(new Label("Qty:"), 0, 4); grid.add(qtyField, 1, 4);
        grid.add(processBtn, 1, 5); grid.add(backBtn, 1, 6); grid.add(status, 0, 7, 2, 1);
        window.setScene(new Scene(grid, 400, 400));
    }

    // --- Sales System Screen ---
    private void showSalesScreen() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(20));
        grid.setVgap(10); grid.setHgap(10);
        Label title = new Label("Record New Sale"); title.setStyle("-fx-font-weight: bold;");
        TextField custField = new TextField(); custField.setPromptText("Customer Name");
        TextField modelField = new TextField(); modelField.setPromptText("Model Name");
        TextField qtyField = new TextField(); qtyField.setPromptText("Quantity");
        TextField priceField = new TextField(); priceField.setPromptText("Unit Price");
        
        ComboBox<String> methodBox = new ComboBox<>();
        methodBox.getItems().addAll("Cash", "Card", "QR");
        methodBox.setValue("Cash");
        methodBox.setPromptText("Select Method");

        Button sellBtn = new Button("Confirm Sale"); Button backBtn = new Button("Back"); Label status = new Label();

        sellBtn.setOnAction(e -> {
            try {
                String cust = custField.getText(); String mod = modelField.getText();
                int q = Integer.parseInt(qtyField.getText()); double p = Double.parseDouble(priceField.getText());
                String meth = methodBox.getValue();
                
                if(cust.isEmpty() || mod.isEmpty() || meth == null) { status.setText("Missing fields."); return; }
                if(updateStockInCSV(mod, q, currentOutletCode)) {
                    writeSalesRecord(cust, mod, q, p, meth);
                    status.setText("Sale Recorded!"); status.setStyle("-fx-text-fill: green;");
                    custField.clear(); modelField.clear(); qtyField.clear(); priceField.clear();
                    methodBox.setValue("Cash");
                } else {
                    status.setText("Error: Model not found or Low Stock."); status.setStyle("-fx-text-fill: red;");
                }
            } catch (Exception ex) { status.setText("Invalid Number Format"); status.setStyle("-fx-text-fill: red;"); }
        });
        backBtn.setOnAction(e -> showMainMenu());
        grid.add(title, 0, 0, 2, 1); grid.add(new Label("Customer:"), 0, 1); grid.add(custField, 1, 1);
        grid.add(new Label("Model:"), 0, 2); grid.add(modelField, 1, 2);
        grid.add(new Label("Qty:"), 0, 3); grid.add(qtyField, 1, 3);
        grid.add(new Label("Price:"), 0, 4); grid.add(priceField, 1, 4);
        grid.add(new Label("Method:"), 0, 5); grid.add(methodBox, 1, 5);
        grid.add(sellBtn, 1, 6); grid.add(backBtn, 1, 7); grid.add(status, 0, 8, 2, 1);
        window.setScene(new Scene(grid, 400, 450));
    }

    // --- Sales History & Filter Screen ---
    public static class SalesRecord {
        private final SimpleStringProperty date, time, customer, total, items;
        private final double amountVal;
        private final LocalDate dateVal;

        public SalesRecord(String d, String t, String c, String tot, String i) {
            this.date = new SimpleStringProperty(d);
            this.time = new SimpleStringProperty(t);
            this.customer = new SimpleStringProperty(c);
            this.total = new SimpleStringProperty(tot);
            this.items = new SimpleStringProperty(i);
            this.amountVal = Double.parseDouble(tot);
            this.dateVal = LocalDate.parse(d);
        }
        public String getDate() { return date.get(); }
        public String getTime() { return time.get(); }
        public String getCustomer() { return customer.get(); }
        public String getTotal() { return total.get(); }
        public String getItems() { return items.get(); }
        public double getAmountVal() { return amountVal; }
        public LocalDate getDateVal() { return dateVal; }
    }

    private void showHistoryFilterScreen() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        
        Label title = new Label("Sales History (Filter & Sort)");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Filter Controls
        HBox filters = new HBox(10);
        DatePicker startDate = new DatePicker(); startDate.setPromptText("Start Date");
        DatePicker endDate = new DatePicker(); endDate.setPromptText("End Date");
        Button filterBtn = new Button("Apply Date Filter");

        HBox sorts = new HBox(10);
        ComboBox<String> sortBox = new ComboBox<>();
        sortBox.getItems().addAll("Date (Newest First)", "Date (Oldest First)", "Amount (High-Low)", "Amount (Low-High)", "Customer (A-Z)");
        sortBox.setValue("Date (Newest First)");
        Button sortBtn = new Button("Apply Sort");

        filters.getChildren().addAll(new Label("From:"), startDate, new Label("To:"), endDate, filterBtn);
        sorts.getChildren().addAll(new Label("Sort By:"), sortBox, sortBtn);

        // Table Setup
        TableView<SalesRecord> table = new TableView<>();
        TableColumn<SalesRecord, String> colDate = new TableColumn<>("Date"); colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<SalesRecord, String> colTime = new TableColumn<>("Time"); colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        TableColumn<SalesRecord, String> colCust = new TableColumn<>("Customer"); colCust.setCellValueFactory(new PropertyValueFactory<>("customer"));
        TableColumn<SalesRecord, String> colTot = new TableColumn<>("Total (RM)"); colTot.setCellValueFactory(new PropertyValueFactory<>("total"));
        TableColumn<SalesRecord, String> colItem = new TableColumn<>("Items"); colItem.setCellValueFactory(new PropertyValueFactory<>("items"));
        colItem.setMinWidth(200);
        
        table.getColumns().addAll(colDate, colTime, colCust, colTot, colItem);

        // Data Logic
        List<SalesRecord> allRecords = loadSalesRecords();
        table.setItems(FXCollections.observableArrayList(allRecords)); 

        // Filter Action
        filterBtn.setOnAction(e -> {
            LocalDate start = startDate.getValue();
            LocalDate end = endDate.getValue();
            if (start == null || end == null) return;
            
            List<SalesRecord> filtered = allRecords.stream()
                .filter(r -> (r.getDateVal().isEqual(start) || r.getDateVal().isAfter(start)) && 
                             (r.getDateVal().isEqual(end) || r.getDateVal().isBefore(end)))
                .collect(Collectors.toList());
            table.setItems(FXCollections.observableArrayList(filtered));
            
            double sum = filtered.stream().mapToDouble(SalesRecord::getAmountVal).sum();
            new Alert(Alert.AlertType.INFORMATION, "Total Sales in Range: RM " + String.format("%.2f", sum)).show();
        });

        // Sort Action
        sortBtn.setOnAction(e -> {
            ObservableList<SalesRecord> current = table.getItems();
            String mode = sortBox.getValue();
            if(mode.contains("Newest")) current.sort(Comparator.comparing(SalesRecord::getDateVal).reversed());
            else if(mode.contains("Oldest")) current.sort(Comparator.comparing(SalesRecord::getDateVal));
            else if(mode.contains("High-Low")) current.sort(Comparator.comparing(SalesRecord::getAmountVal).reversed());
            else if(mode.contains("Low-High")) current.sort(Comparator.comparing(SalesRecord::getAmountVal));
            else if(mode.contains("Customer")) current.sort(Comparator.comparing(SalesRecord::getCustomer));
        });

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> showMainMenu());

        layout.getChildren().addAll(title, filters, sorts, table, backBtn);
        window.setScene(new Scene(layout, 700, 500));
    }

    private List<SalesRecord> loadSalesRecords() {
        List<SalesRecord> list = new ArrayList<>();
        try (Scanner sc = new Scanner(new File("sales.csv"))) {
            if(sc.hasNextLine()) sc.nextLine(); 
            while(sc.hasNextLine()) {
                String[] p = sc.nextLine().split(",");
                if(p.length >= 7) {
                    list.add(new SalesRecord(p[0], p[1], p[4], p[6], (p.length>7?p[7]:"")));
                }
            }
        } catch(Exception e) {}
        return list;
    }

    // --- Reports & Analytics Menu ---
    private void showAnalyticsMenu() {
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Label title = new Label("Reports & Analytics");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        TextArea reportArea = new TextArea();
        reportArea.setEditable(false);
        reportArea.setPrefHeight(250);

        Button btnDataAnalytics = new Button("Data Analytics (Revenue)");
        Button btnEmpMetrics = new Button("Employee Performance Metrics (Manager Only)"); 
        Button btnAutoEmail = new Button("Send Daily Report to HQ (Email)");
        Button btnBack = new Button("Back");

        setBtnWidth(btnDataAnalytics, btnEmpMetrics, btnAutoEmail, btnBack);

        // Revenue Logic
        btnDataAnalytics.setOnAction(e -> {
            double totalRevenue = 0;
            int totalCount = 0;
            try (Scanner sc = new Scanner(new File("sales.csv"))) {
                if(sc.hasNextLine()) sc.nextLine();
                while(sc.hasNextLine()) {
                    String[] p = sc.nextLine().split(",");
                    if (p.length >= 7) {
                        try { totalRevenue += Double.parseDouble(p[6]); totalCount++; } catch(Exception ex){}
                    }
                }
                reportArea.setText("=== DATA ANALYTICS ===\n\n");
                reportArea.appendText("Total Sales Count: " + totalCount + "\n");
                reportArea.appendText(String.format("Total Revenue: RM%.2f\n", totalRevenue));
                reportArea.appendText("Average Order Value: RM" + (totalCount > 0 ? String.format("%.2f", totalRevenue/totalCount) : "0.00"));
            } catch(Exception ex) { reportArea.setText("Error reading sales data."); }
        });

        // Employee Metrics Logic
        btnEmpMetrics.setOnAction(e -> {
            if(!currentUserRole.equalsIgnoreCase("Manager")) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Access Denied. Manager Role Required.");
                alert.show();
                return;
            }
            
            // Load Employee Names
            Map<String, String> empNames = new HashMap<>();
            try (Scanner sc = new Scanner(new File("employee.csv"))) {
                while(sc.hasNextLine()) {
                    String[] p = sc.nextLine().split(",");
                    if(p.length >= 2) empNames.put(p[0].trim(), p[1].trim());
                }
            } catch(Exception ex) {}

            Map<String, Double> salesMap = new HashMap<>();
            Map<String, Integer> countMap = new HashMap<>(); 
            
            try (Scanner sc = new Scanner(new File("sales.csv"))) {
                if(sc.hasNextLine()) sc.nextLine();
                while(sc.hasNextLine()) {
                    String[] p = sc.nextLine().split(",");
                    if (p.length >= 7) {
                        String empID = p[3]; 
                        double amt = Double.parseDouble(p[6]);
                        salesMap.put(empID, salesMap.getOrDefault(empID, 0.0) + amt);
                        countMap.put(empID, countMap.getOrDefault(empID, 0) + 1);
                    }
                }
                List<Map.Entry<String, Double>> sorted = new ArrayList<>(salesMap.entrySet());
                sorted.sort((a,b) -> b.getValue().compareTo(a.getValue()));

                StringBuilder sb = new StringBuilder("=== EMPLOYEE PERFORMANCE ===\n(Ranked by Revenue)\n\n");
                for(Map.Entry<String, Double> entry : sorted) {
                    String id = entry.getKey();
                    String name = empNames.getOrDefault(id, "Unknown");
                    sb.append("Emp: ").append(name).append(" (ID: ").append(id).append(")")
                      .append(" | Transactions: ").append(countMap.get(id))
                      .append(" | Total: RM").append(String.format("%.2f", entry.getValue())).append("\n");
                }
                reportArea.setText(sb.toString());
            } catch(Exception ex) { reportArea.setText("Error reading sales data."); }
        });

        // Auto Email Logic
        btnAutoEmail.setOnAction(e -> {
            generateDailyReportFile(); 
            sendRealEmail();          
        });

        btnBack.setOnAction(e -> showMainMenu());

        layout.getChildren().addAll(title, btnDataAnalytics, btnEmpMetrics, btnAutoEmail, reportArea, btnBack);
        window.setScene(new Scene(layout, 500, 500));
    }

    // --- Email Helper Methods ---
    
    // Generate Attachment
    private void generateDailyReportFile() {
        String fileName = "DailySales_" + LocalDate.now() + ".txt";
        double total = 0;
        try (BufferedWriter w = new BufferedWriter(new FileWriter(fileName));
             Scanner sc = new Scanner(new File("sales.csv"))) {
             
            w.write("DAILY SALES REPORT - " + LocalDate.now() + "\n");
            w.write("Outlet: " + currentOutletName + "\n--------------------------------\n");
            
            if(sc.hasNextLine()) sc.nextLine();
            while(sc.hasNextLine()) {
                String line = sc.nextLine();
                if(line.startsWith(LocalDate.now().toString())) { 
                    w.write(line + "\n");
                    String[] p = line.split(",");
                    if(p.length >= 7) total += Double.parseDouble(p[6]);
                }
            }
            w.write("\n--------------------------------\nTOTAL REVENUE: RM" + String.format("%.2f", total));
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "Failed to generate report file.").show();
        }
    }

    // Send Email Implementation
    private void sendRealEmail() {
        // Calculate total for email body
        double bodyTotal = 0.0;
        try(Scanner sc = new Scanner(new File("sales.csv"))) {
            if(sc.hasNextLine()) sc.nextLine();
            while(sc.hasNextLine()) {
                String line = sc.nextLine();
                if(line.startsWith(LocalDate.now().toString())) {
                     String[] p = line.split(",");
                     if(p.length >= 7) bodyTotal += Double.parseDouble(p[6]);
                }
            }
        } catch (Exception e) {}
        
        // Properties
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        // Session
        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(GMAIL_USERNAME, GMAIL_APP_PASSWORD);
            }
        });

        try {
            // Message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(GMAIL_USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(STUDENT_EMAIL));
            message.setSubject("Daily Sales Summary - " + LocalDate.now());

            // Multipart
            Multipart multipart = new MimeMultipart();

            // Body
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("Attached is the daily sales report.\n\nDate: " + LocalDate.now() + 
                                  "\nTotal Revenue: RM" + String.format("%.2f", bodyTotal));
            multipart.addBodyPart(messageBodyPart);

            // Attachment
            messageBodyPart = new MimeBodyPart();
            String fileName = "DailySales_" + LocalDate.now() + ".txt";
            FileDataSource source = new FileDataSource(fileName);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(fileName);
            multipart.addBodyPart(messageBodyPart);

            message.setContent(multipart);
            Transport.send(message);

            new Alert(Alert.AlertType.INFORMATION, "Real Email Sent Successfully!").showAndWait();

        } catch (MessagingException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Email Failed: " + e.getMessage()).show();
        }
    }

    // --- Edit Information Menu ---
    private void showEditMenu() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); 

        // Tab 1: Edit Stock
        Tab stockTab = new Tab("Edit Stock");
        VBox stockLayout = new VBox(15); 
        stockLayout.setAlignment(Pos.CENTER);
        stockLayout.setPadding(new Insets(20));
        
        TextField sModel = new TextField(); sModel.setPromptText("Model Name"); sModel.setMaxWidth(250);
        TextField sOutlet = new TextField(); sOutlet.setPromptText("Outlet Code (e.g. JB1)"); sOutlet.setMaxWidth(250);
        TextField sQty = new TextField(); sQty.setPromptText("New Quantity"); sQty.setMaxWidth(250);
        Button sUpdate = new Button("Update Stock");
        Label sStatus = new Label();

        sUpdate.setOnAction(e -> {
            if(sModel.getText().isEmpty() || sOutlet.getText().isEmpty() || sQty.getText().isEmpty()) {
                sStatus.setText("Fill all fields."); return;
            }
            boolean done = updateExactStock(sModel.getText(), sOutlet.getText(), sQty.getText());
            if(done) {
                sStatus.setText("Stock Updated Successfully.");
                sStatus.setStyle("-fx-text-fill: green;");
            } else {
                sStatus.setText("Failed. Check Model Name or Outlet Code.");
                sStatus.setStyle("-fx-text-fill: red;");
            }
        });
        stockLayout.getChildren().addAll(new Label("Modify Stock Quantity"), sModel, sOutlet, sQty, sUpdate, sStatus);
        stockTab.setContent(stockLayout);

        // Tab 2: Edit Sales
        Tab salesTab = new Tab("Edit Sales");
        VBox salesLayout = new VBox(15); 
        salesLayout.setAlignment(Pos.CENTER);
        salesLayout.setPadding(new Insets(20));

        Label instr = new Label("1. Find Record by Customer Name");
        TextField salesCust = new TextField(); salesCust.setPromptText("Enter Customer Name"); salesCust.setMaxWidth(250);
        Button salesFind = new Button("Find Record");
        
        Label previewLbl = new Label("Record Preview:");
        TextArea previewArea = new TextArea();
        previewArea.setEditable(false);
        previewArea.setMaxHeight(80);
        previewArea.setMaxWidth(400);

        Label instr2 = new Label("2. Edit Raw Data (Format: Date,Time,Outlet,User,Cust,Method,Total,Items)");
        TextField salesEditLine = new TextField(); salesEditLine.setPromptText("Record will appear here"); salesEditLine.setMaxWidth(400);
        
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER);
        Button salesSave = new Button("Save Changes");
        Button salesDel = new Button("Delete Record");
        salesDel.setStyle("-fx-background-color: #ffcccc;");
        actionBox.getChildren().addAll(salesSave, salesDel);
        
        Label salesMsg = new Label();
        
        final int[] foundLineIndex = {-1}; 
        final List<String> cacheLines = new ArrayList<>();

        salesFind.setOnAction(e -> {
            cacheLines.clear();
            foundLineIndex[0] = -1;
            salesEditLine.clear();
            previewArea.clear();
            try (Scanner sc = new Scanner(new File("sales.csv"))) {
                while(sc.hasNextLine()) cacheLines.add(sc.nextLine());
                boolean found = false;
                for(int i=0; i<cacheLines.size(); i++) {
                    if(cacheLines.get(i).toLowerCase().contains(salesCust.getText().toLowerCase()) && !salesCust.getText().isEmpty()) {
                        salesEditLine.setText(cacheLines.get(i));
                        
                        // Parse for preview
                        String[] p = cacheLines.get(i).split(",");
                        if(p.length >= 7) {
                            String pretty = "Date: " + p[0] + " | Cust: " + p[4] + " | Total: RM" + p[6] + "\n" +
                                            "Items: " + (p.length > 7 ? p[7] : "N/A");
                            previewArea.setText(pretty);
                        }

                        foundLineIndex[0] = i;
                        found = true;
                        salesMsg.setText("Record found!");
                        salesMsg.setStyle("-fx-text-fill: green;");
                        break; 
                    }
                }
                if(!found) {
                    salesMsg.setText("No record found for that name.");
                    salesMsg.setStyle("-fx-text-fill: red;");
                }
            } catch(Exception ex) { salesMsg.setText("Error reading file."); }
        });

        salesSave.setOnAction(e -> {
            if(foundLineIndex[0] != -1) {
                cacheLines.set(foundLineIndex[0], salesEditLine.getText());
                rewriteSalesFile(cacheLines);
                salesMsg.setText("Record updated successfully.");
            } else {
                salesMsg.setText("Find a record first.");
            }
        });

        salesDel.setOnAction(e -> {
            if(foundLineIndex[0] != -1) {
                cacheLines.remove(foundLineIndex[0]);
                rewriteSalesFile(cacheLines);
                salesMsg.setText("Record Deleted.");
                salesEditLine.clear();
                previewArea.clear();
                foundLineIndex[0] = -1;
            } else {
                salesMsg.setText("Find a record first.");
            }
        });

        salesLayout.getChildren().addAll(instr, salesCust, salesFind, new Separator(), previewLbl, previewArea, instr2, salesEditLine, actionBox, salesMsg);
        salesTab.setContent(salesLayout);

        // Main Edit Layout
        tabPane.getTabs().addAll(stockTab, salesTab);
        VBox mainBox = new VBox(10);
        Button back = new Button("Back to Main Menu");
        back.setOnAction(e -> showMainMenu());
        mainBox.getChildren().addAll(tabPane, back);
        mainBox.setAlignment(Pos.CENTER);
        
        window.setScene(new Scene(mainBox, 500, 550));
    }

    // --- Clock Out ---
    private void performClockOut() {
        LocalTime timeOut = LocalTime.now().withNano(0);
        long mins = Duration.between(timeIn, timeOut).toMinutes();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Clock-out");
        alert.setHeaderText("Goodbye, " + currentUserName);
        alert.setContentText("Shift Duration: " + mins + " minutes.\nAttendance logged.");
        alert.showAndWait();

        try (BufferedWriter att = new BufferedWriter(new FileWriter("attendance.csv", true))) {
            att.write(currentUserID + "," + currentUserName + "," + currentOutletCode + "," + 
                      currentOutletName + "," + LocalDate.now() + "," + timeIn + "," + timeOut);
            att.newLine();
        } catch (Exception e) { System.out.println("Error writing attendance."); }

        showLoginScreen();
    }

    // --- Helper Methods (File I/O) ---
    private void createDummyFilesIfMissing() {
        try {
            File emp = new File("employee.csv");
            if(emp.createNewFile()) {
                BufferedWriter w = new BufferedWriter(new FileWriter(emp));
                w.write("ADM01,Admin User,Manager,admin123"); w.newLine();
                w.write("JB101,John Doe,Staff,1234"); w.newLine();
                w.close();
            }
            File mod = new File("model.csv");
            if(mod.createNewFile()) {
                BufferedWriter w = new BufferedWriter(new FileWriter(mod));
                w.write("Model,JB1,KL1,Price"); w.newLine();
                w.write("iPhone 13,10,5,3000.00"); w.newLine();
                w.write("Samsung S22,8,8,2800.00"); w.newLine();
                w.close();
            }
            File out = new File("outlet.csv");
            if(out.createNewFile()) {
                BufferedWriter w = new BufferedWriter(new FileWriter(out));
                w.write("JB1,Johor Branch"); w.newLine();
                w.write("KL1,KL Branch"); w.newLine();
                w.close();
            }
            File sales = new File("sales.csv");
            if(sales.createNewFile()) {
                BufferedWriter w = new BufferedWriter(new FileWriter(sales));
                w.write("Date,Time,Outlet,User,Customer,Method,Total,Items"); w.newLine();
                w.close();
            }
        } catch(IOException e) {}
    }

    private boolean performLogin(String id, String pass) {
        try (Scanner sc = new Scanner(new File("employee.csv"))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if(line.trim().isEmpty()) continue;
                String[] p = line.split(",");
                if (p.length >= 4) {
                    if (p[0].trim().equals(id) && p[3].trim().equals(pass)) {
                        currentUserName = p[1].trim(); 
                        currentUserRole = p[2].trim();
                        return true;
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    private String getOutletNameFromFile(String code) {
        try (Scanner sc = new Scanner(new File("outlet.csv"))) {
            while (sc.hasNextLine()) {
                String[] p = sc.nextLine().split(",");
                if (p.length >= 2 && p[0].trim().equalsIgnoreCase(code)) {
                    return p[1].trim();
                }
            }
        } catch (Exception e) { return "Unknown Outlet"; }
        return "Unknown Outlet";
    }

    private int getStockQty(String model, String outletCode) {
        try (Scanner sc = new Scanner(new File("model.csv"))) {
            int colIndex = -1;
            if(sc.hasNextLine()) {
                String[] headers = sc.nextLine().split(",");
                for(int i=0; i<headers.length; i++) {
                    if(headers[i].trim().equalsIgnoreCase(outletCode)) colIndex = i;
                }
            }
            if(colIndex == -1) return 0;
            while(sc.hasNextLine()) {
                String[] p = sc.nextLine().split(",");
                if(p[0].equalsIgnoreCase(model)) {
                    return Integer.parseInt(p[colIndex].trim());
                }
            }
        } catch(Exception e) { return 0; }
        return 0;
    }

    private boolean updateStockInCSV(String model, int qtySold, String outletCode) {
        List<String> lines = new ArrayList<>();
        boolean found = false;
        try (Scanner sc = new Scanner(new File("model.csv"))) {
            int colIndex = -1;
            if (sc.hasNextLine()) {
                String h = sc.nextLine();
                lines.add(h);
                String[] headers = h.split(",");
                for(int i=0; i<headers.length; i++) {
                    if(headers[i].trim().equalsIgnoreCase(outletCode)) colIndex = i;
                }
            }
            while(sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] p = line.split(",");
                if(colIndex != -1 && p[0].equalsIgnoreCase(model)) {
                    try {
                        int cur = Integer.parseInt(p[colIndex].trim());
                        // If qtySold is positive, we are selling (subtracting). Check if stock >= qty.
                        // If qtySold is negative, we are restocking (adding). cur >= negative is always true.
                        if(cur >= qtySold) {
                            p[colIndex] = String.valueOf(cur - qtySold);
                            line = String.join(",", p);
                            found = true;
                        }
                    } catch(Exception e) {}
                }
                lines.add(line);
            }
        } catch(Exception e) { return false; }

        if(found) {
            try(BufferedWriter w = new BufferedWriter(new FileWriter("model.csv"))) {
                for(String l : lines) { w.write(l); w.newLine(); }
            } catch(Exception e) { return false; }
        }
        return found;
    }

    private void writeSalesRecord(String cust, String mod, int q, double p, String meth) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter("sales.csv", true))) {
            w.write(LocalDate.now() + "," + LocalTime.now().withNano(0) + "," + 
                    currentOutletCode + "," + currentUserID + "," + cust + "," + 
                    meth + "," + (q*p) + "," + mod + ":" + q + ";");
            w.newLine();
        } catch(Exception e) {}
    }

    private boolean updateExactStock(String model, String outlet, String newQty) {
        List<String> lines = new ArrayList<>();
        boolean updated = false;
        try (Scanner sc = new Scanner(new File("model.csv"))) {
            int col = -1;
            if(sc.hasNextLine()) {
                String h = sc.nextLine();
                lines.add(h);
                String[] p = h.split(",");
                for(int i=0; i<p.length; i++) if(p[i].trim().equalsIgnoreCase(outlet)) col = i;
            }
            while(sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] p = line.split(",");
                if(col != -1 && p[0].equalsIgnoreCase(model)) {
                    p[col] = newQty;
                    line = String.join(",", p);
                    updated = true;
                }
                lines.add(line);
            }
        } catch(Exception e) { return false; }
        
        if(updated) {
            try(BufferedWriter w = new BufferedWriter(new FileWriter("model.csv"))) {
                for(String l : lines) { w.write(l); w.newLine(); }
            } catch(Exception e) { return false; }
        }
        return updated;
    }

    private void rewriteSalesFile(List<String> lines) {
        try(BufferedWriter w = new BufferedWriter(new FileWriter("sales.csv"))) {
            for(String l : lines) { w.write(l); w.newLine(); }
        } catch(Exception e) {}
    }

    private void setBtnWidth(Button... btns) {
        for(Button b : btns) { b.setMinWidth(250); b.setAlignment(Pos.BASELINE_LEFT); }
    }
}