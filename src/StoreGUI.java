import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class StoreGUI extends Application {

    private Stage window;
    
    // --- Session Data ---
    private String currentUserID = "";
    private String currentUserName = "";
    private String currentUserRole = "";
    private String currentOutletCode = "";
    private String currentOutletName = "";
    private LocalTime timeIn;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("Store Operations Management System");
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
        Button btn3 = new Button("3. Search Information");
        Button btn4 = new Button("4. Edit Information");
        Button btn5 = new Button("5. Reports & Analytics");
        Button btn6 = new Button("6. Clock-out");

        setBtnWidth(btn1, btn2, btn3, btn4, btn5, btn6);

        btn1.setOnAction(e -> showStockMenu());
        btn2.setOnAction(e -> showSalesScreen());
        btn3.setOnAction(e -> showSearchMenu());
        btn4.setOnAction(e -> showEditMenu());
        btn5.setOnAction(e -> showAnalyticsMenu());
        btn6.setOnAction(e -> performClockOut());

        layout.getChildren().addAll(welcome, roleLbl, info, new Separator(), btn1, btn2, btn3, btn4, btn5);

        // Manager Option
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

    // --- Register Employee (Manager) ---
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

        Button btnCount = new Button("Morning/Night Stock Count");
        Button btnMove = new Button("Stock Movement (In/Out)");
        Button btnBack = new Button("Back");
        setBtnWidth(btnCount, btnMove, btnBack);

        btnCount.setOnAction(e -> showStockCountScreen());
        btnMove.setOnAction(e -> showStockMovementScreen());
        btnBack.setOnAction(e -> showMainMenu());

        layout.getChildren().addAll(title, btnCount, btnMove, new Separator(), btnBack);
        window.setScene(new Scene(layout, 400, 350));
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
            if(model.isEmpty() || qtyStr.isEmpty() || location.isEmpty()) { status.setText("Fill all fields."); return; }
            try {
                try(BufferedWriter w = new BufferedWriter(new FileWriter("receipts_" + LocalDate.now() + ".txt", true))) {
                    String from = (type.equals("Stock In")) ? location : currentOutletCode;
                    String to = (type.equals("Stock In")) ? currentOutletCode : location;
                    w.write("\n=== "+type+" ===\nFrom: "+from+"\nTo: "+to+"\n" + model + " (Qty: " + qtyStr + ")\nUser: " + currentUserName + "\n----------------\n");
                    status.setText("Receipt generated!"); status.setStyle("-fx-text-fill: green;");
                }
            } catch (Exception ex) { status.setText("Error writing receipt."); }
        });
        backBtn.setOnAction(e -> showStockMenu());
        grid.add(title, 0, 0, 2, 1); grid.add(new Label("Type:"), 0, 1); grid.add(typeBox, 1, 1);
        grid.add(new Label("From/To:"), 0, 2); grid.add(fromToField, 1, 2);
        grid.add(new Label("Model:"), 0, 3); grid.add(modelField, 1, 3);
        grid.add(new Label("Qty:"), 0, 4); grid.add(qtyField, 1, 4);
        grid.add(processBtn, 1, 5); grid.add(backBtn, 1, 6); grid.add(status, 0, 7, 2, 1);
        window.setScene(new Scene(grid, 400, 400));
    }

    // --- Sales System ---
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
        
        // MODIFICATION 1: Changed method textfield to ComboBox
        ComboBox<String> methodBox = new ComboBox<>();
        methodBox.getItems().addAll("Cash", "Card", "QR");
        methodBox.setValue("Cash");
        methodBox.setPromptText("Select Method");

        Button sellBtn = new Button("Confirm Sale"); Button backBtn = new Button("Back"); Label status = new Label();

        sellBtn.setOnAction(e -> {
            try {
                String cust = custField.getText(); String mod = modelField.getText();
                int q = Integer.parseInt(qtyField.getText()); double p = Double.parseDouble(priceField.getText());
                String meth = methodBox.getValue(); // Get value from box
                
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

    // --- Search Information ---
    private void showSearchMenu() {
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        
        Label title = new Label("Search Information");
        title.setStyle("-fx-font-weight: bold;");

        TextField searchField = new TextField();
        searchField.setPromptText("Enter Model or Keyword");

        ComboBox<String> sortBox = new ComboBox<>();
        sortBox.getItems().addAll("Sort by Date (Newest)", "Sort by Amount (High-Low)");
        sortBox.setValue("Sort by Date (Newest)");
        
        Button btnSearchStock = new Button("Search Stock (Model)");
        Button btnSearchSales = new Button("Search Sales (Keyword)");
        Button btnBack = new Button("Back");
        
        TextArea output = new TextArea();
        output.setEditable(false);
        output.setPrefHeight(300);

        btnSearchStock.setOnAction(e -> output.setText(searchStockLogic(searchField.getText())));
        
        btnSearchSales.setOnAction(e -> {
            String res = searchSalesLogic(searchField.getText(), sortBox.getValue());
            output.setText(res);
        });

        btnBack.setOnAction(e -> showMainMenu());

        setBtnWidth(btnSearchStock, btnSearchSales, btnBack);

        layout.getChildren().addAll(title, searchField, new Label("Sales Sorting:"), sortBox, 
                                    btnSearchStock, btnSearchSales, output, btnBack);
        window.setScene(new Scene(layout, 500, 550));
    }

    // --- Reports & Analytics ---
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
        Button btnEmpMetrics = new Button("Employee Performance Metrics");
        Button btnAutoEmail = new Button("Auto Email to HQ");
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
                // MODIFICATION 4: Changed currency to RM
                reportArea.appendText(String.format("Total Revenue: RM%.2f\n", totalRevenue));
                reportArea.appendText("Average Order Value: RM" + (totalCount > 0 ? String.format("%.2f", totalRevenue/totalCount) : "0.00"));
            } catch(Exception ex) { reportArea.setText("Error reading sales data."); }
        });

        // Performance Logic
        btnEmpMetrics.setOnAction(e -> {
            Map<String, Double> salesMap = new HashMap<>();
            try (Scanner sc = new Scanner(new File("sales.csv"))) {
                if(sc.hasNextLine()) sc.nextLine();
                while(sc.hasNextLine()) {
                    String[] p = sc.nextLine().split(",");
                    if (p.length >= 7) {
                        String empID = p[3]; 
                        double amt = Double.parseDouble(p[6]);
                        salesMap.put(empID, salesMap.getOrDefault(empID, 0.0) + amt);
                    }
                }
                StringBuilder sb = new StringBuilder("=== EMPLOYEE PERFORMANCE ===\n(Total Revenue Generated)\n\n");
                // MODIFICATION 4: Changed currency to RM
                salesMap.forEach((k, v) -> sb.append("Employee ").append(k).append(": RM").append(String.format("%.2f", v)).append("\n"));
                reportArea.setText(sb.toString());
            } catch(Exception ex) { reportArea.setText("Error reading sales data."); }
        });

        // Email Logic
        btnAutoEmail.setOnAction(e -> {
            try (BufferedWriter log = new BufferedWriter(new FileWriter("email_log.txt", true))) {
                log.write("REPORT SENT: " + LocalDate.now() + " " + LocalTime.now() + " | Outlet: " + currentOutletCode + "\n");
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Auto Email");
                alert.setHeaderText("Report Sent Successfully");
                alert.setContentText("Daily sales report has been emailed to hq@store.com.");
                alert.showAndWait();
            } catch(Exception ex) { 
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Failed to send email.");
                alert.show();
            }
        });

        btnBack.setOnAction(e -> showMainMenu());

        layout.getChildren().addAll(title, btnDataAnalytics, btnEmpMetrics, btnAutoEmail, reportArea, btnBack);
        window.setScene(new Scene(layout, 500, 500));
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
        
        // MODIFICATION 3: Better formatting (added preview area)
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
                    // MODIFICATION 3: Case insensitive search
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

    private String searchStockLogic(String key) {
        StringBuilder sb = new StringBuilder();
        try (Scanner sc = new Scanner(new File("model.csv"))) {
            String header = sc.nextLine(); 
            String[] headers = header.split(","); 
            
            while(sc.hasNextLine()) {
                String line = sc.nextLine();
                if(line.toLowerCase().contains(key.toLowerCase())) {
                    String[] p = line.split(",");
                    sb.append("Model: ").append(p[0]).append("\n");
                    for(int i=1; i<p.length; i++) {
                        if(i < headers.length) {
                             // MODIFICATION 2: Handle "Price" header and Unknown Outlets
                             String hName = headers[i].trim();
                             if(hName.equalsIgnoreCase("Price")) {
                                 sb.append(" - Price: RM").append(p[i]).append("\n");
                             } else {
                                 String outName = getOutletNameFromFile(hName);
                                 if(!outName.contains("Unknown")) {
                                     sb.append(" - ").append(outName).append(" (").append(hName).append("): ").append(p[i]).append("\n");
                                 } else {
                                     // If really unknown, just show code or skip
                                     sb.append(" - ").append(hName).append(": ").append(p[i]).append("\n");
                                 }
                             }
                        }
                    }
                    sb.append("--------------------\n");
                }
            }
        } catch(Exception e) { return "Error reading model.csv"; }
        return sb.length() > 0 ? sb.toString() : "No results.";
    }

    private String searchSalesLogic(String key, String sortBy) {
        List<String[]> records = new ArrayList<>();
        try (Scanner sc = new Scanner(new File("sales.csv"))) {
            if(sc.hasNextLine()) sc.nextLine(); 
            while(sc.hasNextLine()) {
                String line = sc.nextLine();
                if(line.toLowerCase().contains(key.toLowerCase())) {
                    records.add(line.split(","));
                }
            }
        } catch(Exception e) { return "Error reading sales.csv"; }

        if(sortBy.contains("Amount")) {
            records.sort((a, b) -> {
                try {
                    double v1 = Double.parseDouble(a[6]);
                    double v2 = Double.parseDouble(b[6]);
                    return Double.compare(v2, v1);
                } catch(Exception e) { return 0; }
            });
        } else {
            Collections.reverse(records);
        }

        StringBuilder sb = new StringBuilder();
        for(String[] p : records) {
            if (p.length >= 7) {
                sb.append("Date: ").append(p[0]).append(" | Time: ").append(p[1]).append("\n");
                sb.append("Customer: ").append(p[4]).append("\n");
                // MODIFICATION 2.1: Changed to RM
                sb.append("Total: RM").append(p[6]).append("\n");
                
                // MODIFICATION 2.1: Parse Items and Quantity
                String rawItems = p.length > 7 ? p[7] : "";
                if(rawItems.contains(":")) {
                     String[] parts = rawItems.split(":");
                     if(parts.length >= 2) {
                         // Removes semicolon if present
                         String qty = parts[1].replace(";", "");
                         sb.append("Items: ").append(parts[0])
                           .append(", Quantity: ").append(qty).append("\n");
                     } else {
                         sb.append("Items: ").append(rawItems).append("\n");
                     }
                } else {
                     sb.append("Items: ").append(rawItems).append("\n");
                }
                sb.append("--------------------\n");
            }
        }
        return sb.length() > 0 ? sb.toString() : "No records found.";
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