import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalTime;

/*
  Store Operations Management System
*/
public class Main {
    
    // --- Data Models ---
    static class Employee {
        String id, name, role, password;
        public Employee(String id, String name, String role, String password) {
            this.id = id; this.name = name; this.role = role; this.password = password;
        }
    }

    static class Model {
        String name;
        List<Integer> quantities;
        public Model(String name, List<Integer> quantities) {
            this.name = name; this.quantities = quantities;
        }
    }

    static List<Employee> employeeList = new ArrayList<>();
    static List<Model> modelListState = new ArrayList<>();

    @SuppressWarnings("resource")
    public static void main(String[] args) throws Exception {
        Scanner scan = new Scanner(System.in);

        // --- Load Data State ---
        loadDataState(); 

        // --- Employee Login ---
        System.out.println("=== Employee Login ===");
        System.out.print("Enter User ID: ");
        String userID = scan.nextLine();

        System.out.print("Enter Password: ");
        String password = scan.nextLine();

        boolean isFound = false;
        String userName = "";
        String userRole = "";
        String outletName = "Unknown"; 

        try (Scanner fileScanner = new Scanner(new File("employee.csv"))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String fileUser = parts[0].trim();
                    String fileName = parts[1].trim();
                    String fileRole = parts[2].trim();
                    String filePass = parts[3].trim();

                    if (fileUser.equalsIgnoreCase("EmployeeID")) continue;

                    if (fileUser.equals(userID) && filePass.equals(password)) {
                        isFound = true;
                        userName = fileName;
                        userRole = fileRole;
                        break;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Employee file not found.");
        }

        if (isFound) {
            System.out.println("\nLogin Successful!");
            System.out.println("Welcome, " + userName + " (" + userID + ")");

            // --- Manager Registration Logic ---
            if (userRole.equalsIgnoreCase("Manager")) {
                String answer = "";
                do {
                    System.out.print("\nDo you want to register a new employee? (yes/no): ");
                    answer = scan.nextLine();
                    if (answer.equalsIgnoreCase("yes")) {
                        BufferedWriter writer = new BufferedWriter(new FileWriter("employee.csv", true));
                        System.out.print("\nEnter New Employee ID: ");
                        String newEmpID = scan.nextLine();
                        System.out.print("Enter New Employee Name: ");
                        String newEmpName = scan.nextLine();
                        System.out.print("Enter New Employee Role: ");
                        String newEmpRole = scan.nextLine();
                        System.out.print("Enter New Employee Password: ");
                        String newEmpPass = scan.nextLine();
                        writer.write(newEmpID + "," + newEmpName + "," + newEmpRole + "," + newEmpPass);
                        writer.newLine();
                        writer.close();
                        System.out.println("\nEmployee successfully registered! ");
                    } else if (!answer.equalsIgnoreCase("no")) {
                        System.out.println("Invalid input.");
                    }
                } while (!answer.equalsIgnoreCase("no"));
            }
        } else {
            System.out.println("\nLogin Failed: Invalid User ID or Password.");
            return; 
        }

        // --- Resolve Outlet Name ---
        String outletCode_Prefix = "";
        if (userID != null && userID.length() >= 3) {
            outletCode_Prefix = userID.substring(0, 3);
        } else {
            System.out.print("Enter Outlet Code: ");
            outletCode_Prefix = scan.nextLine().trim();
        }

        try (Scanner outletScanner = new Scanner(new File("outlet.csv"))) {
            while (outletScanner.hasNextLine()) {
                String line = outletScanner.nextLine().trim();
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    if (parts[0].trim().equalsIgnoreCase(outletCode_Prefix)) {
                        outletName = parts[1].trim();
                        break;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Outlet file not found.");
        }

        // --- Attendance Clock-In ---
        System.out.println("\n=== Attendance Clock - In ===");
        LocalDate date = LocalDate.now();
        LocalTime timeIn = LocalTime.now().withNano(0);
        System.out.println("Employee: " + userName + " (" + userID + ")");
        System.out.println("Outlet: " + outletCode_Prefix + " ("+ outletName + ")");
        System.out.println("Clock-In: " + date + " " + timeIn);

        // --- Main Menu Loop ---
        boolean sessionActive = true;

        while (sessionActive) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("1. Stock Management");
            System.out.println("2. Sales System");
            System.out.println("3. Search Information");
            System.out.println("4. Edit Information");
            System.out.println("5. Clock-out");

            System.out.print("Select task: ");
            String mainChoice = scan.nextLine();

            switch (mainChoice) {
                case "1":
                    stockManagementMenu(scan, outletCode_Prefix, outletName, userName);
                    break;
                case "2":
                    salesSystemMenu(scan, outletCode_Prefix, outletName, userID, userName);
                    break;
                case "3":
                    searchInformationMenu(scan);
                    break;
                case "4":
                    editInformationMenu(scan);
                    break;
                case "5":
                    sessionActive = false;
                    break;
                default:
                    System.out.println("Invalid selection.");
            }
        }

        // --- Attendance Clock-Out ---
        System.out.println("\n=== Attendance Clock - Out ===");
        System.out.println("Employee ID: " + userID);
        System.out.println("Clock-Out Successful!");
        LocalTime timeOut = LocalTime.now().withNano(0);
        long hoursWorked = java.time.Duration.between(timeIn, timeOut).toHours();
        long minutesWorked = java.time.Duration.between(timeIn, timeOut).toMinutes();
        
        System.out.println("Date: " + date);
        System.out.println("Time: " + timeOut);
        System.out.println("Total Time: " + hoursWorked + " hours " + (minutesWorked % 60) + " mins");

        try (BufferedWriter att = new BufferedWriter(new FileWriter("attendance.csv", true))) {
            att.write(userID + "," + userName + "," + outletCode_Prefix + "," + outletName + "," + date + "," + timeIn + "," + timeOut);
            att.newLine();
        } catch (Exception e) {
            System.out.println("Error writing attendance file.");
        }

        scan.close();
    }

    // --- Stock Management Module ---
    private static void stockManagementMenu(Scanner scan, String outletCode, String outletName, String userName) {
        boolean inStockMenu = true;
        while(inStockMenu) {
            System.out.println("\n=== Stock Management ===");
            System.out.println("1. Morning/Night Stock Count");
            System.out.println("2. Stock Movement (In/Out)");
            System.out.println("3. Back");
            System.out.print("Select activity: ");
            String choice = scan.nextLine();

            if (choice.equals("1")) {
                System.out.println("\n--- Stock Count Mode ---");
                int targetColumnIndex = -1;
                ArrayList<String> modelList = new ArrayList<>();
                ArrayList<Integer> recordedQtyList = new ArrayList<>();

                try (Scanner modelFile = new Scanner(new File("model.csv"))) {
                    if (modelFile.hasNextLine()) {
                        String headerLine = modelFile.nextLine().trim();
                        String[] headers = headerLine.split(",");
                        for (int i = 0; i < headers.length; i++) {
                            if (headers[i].trim().equalsIgnoreCase(outletCode)) {
                                targetColumnIndex = i; break;
                            }
                        }
                    }
                    if (targetColumnIndex != -1) {
                        while (modelFile.hasNextLine()) {
                            String line = modelFile.nextLine().trim();
                            if (line.isEmpty()) continue;
                            String[] parts = line.split(",");
                            if (parts.length > targetColumnIndex) {
                                modelList.add(parts[0].trim());
                                try {
                                    recordedQtyList.add(Integer.parseInt(parts[targetColumnIndex].trim()));
                                } catch (Exception e) { recordedQtyList.add(0); }
                            }
                        }
                        
                        int correct = 0, mismatch = 0;
                        for(int i=0; i<modelList.size(); i++) {
                            System.out.println("Model: " + modelList.get(i));
                            System.out.print("Enter Physical Count: ");
                            int userCount = 0;
                            try { userCount = Integer.parseInt(scan.nextLine()); } catch(Exception e){}
                            
                            int sysQty = recordedQtyList.get(i);
                            System.out.println("System Record: " + sysQty);
                            if(userCount == sysQty) { System.out.println("Tally Correct."); correct++; }
                            else { System.out.println("! Mismatch (" + Math.abs(userCount-sysQty) + ")"); mismatch++; }
                        }
                        System.out.println("Done. Correct: " + correct + ", Mismatches: " + mismatch);
                    } else {
                        System.out.println("Outlet code not found in model.csv");
                    }
                } catch (Exception e) { System.out.println("Error reading model.csv"); }

            } else if (choice.equals("2")) {
                System.out.println("\n--- Stock Movement ---");
                System.out.println("1. Stock In (Received)\n2. Stock Out (Transfer)");
                System.out.print("Select: ");
                String type = scan.nextLine().equals("1") ? "Stock In" : "Stock Out";
                
                String from = (type.equals("Stock In")) ? "HQ" : outletCode;
                String to = (type.equals("Stock In")) ? outletCode : "Other Outlet";
                if(type.equals("Stock In")) { System.out.print("From: "); from = scan.nextLine(); }
                else { System.out.print("To: "); to = scan.nextLine(); }

                StringBuilder body = new StringBuilder();
                while(true) {
                    System.out.print("Model Name: "); String m = scan.nextLine();
                    System.out.print("Quantity: "); String q = scan.nextLine();
                    body.append(m).append(" (Qty: ").append(q).append(")\n");
                    System.out.print("More? (y/n): ");
                    if(scan.nextLine().equalsIgnoreCase("n")) break;
                }
                
                try(BufferedWriter w = new BufferedWriter(new FileWriter("receipts_" + LocalDate.now() + ".txt", true))) {
                    w.write("\n=== "+type+" ===\nFrom: "+from+"\nTo: "+to+"\n"+body.toString()+"User: "+userName+"\n----------------\n");
                    System.out.println("Receipt generated.");
                } catch(Exception e) { System.out.println("Error saving receipt."); }

            } else if (choice.equals("3")) {
                inStockMenu = false;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    // --- Sales System Module ---
    private static void salesSystemMenu(Scanner scan, String outletCode, String outletName, String userID, String userName) {
        boolean inSalesMenu = true;
        while(inSalesMenu) {
            System.out.println("\n=== Sales System ===");
            System.out.println("1. Record New Sale");
            System.out.println("2. Back");
            System.out.print("Select option: ");
            String choice = scan.nextLine();

            if (choice.equals("1")) {
                System.out.print("Customer Name: "); String cust = scan.nextLine();
                ArrayList<String> items = new ArrayList<>();
                ArrayList<Integer> qtys = new ArrayList<>();
                ArrayList<Double> prices = new ArrayList<>();
                double subtotal = 0;

                while(true) {
                    System.out.print("Model: "); items.add(scan.nextLine());
                    System.out.print("Qty: "); 
                    int q = 1; try { q = Integer.parseInt(scan.nextLine()); } catch(Exception e){}
                    qtys.add(q);
                    System.out.print("Unit Price: "); 
                    double p = 0; try { p = Double.parseDouble(scan.nextLine()); } catch(Exception e){}
                    prices.add(p);
                    subtotal += (q*p);
                    System.out.print("More items? (y/n): ");
                    if(scan.nextLine().equalsIgnoreCase("n")) break;
                }
                System.out.print("Method: "); String method = scan.nextLine();
                
                updateStockCSV(items, qtys, outletCode);

                try (BufferedWriter sw = new BufferedWriter(new FileWriter("sales.csv", true))) {
                    StringBuilder itemStr = new StringBuilder();
                    for(int i=0; i<items.size(); i++) itemStr.append(items.get(i)).append(":").append(qtys.get(i)).append(";");
                    sw.write(LocalDate.now()+","+LocalTime.now().withNano(0)+","+outletCode+","+userID+","+cust+","+method+","+subtotal+","+itemStr);
                    sw.newLine();
                    System.out.println("Sale Recorded.");
                } catch(Exception e) { System.out.println("Error writing sales.csv"); }

            } else if (choice.equals("2")) {
                inSalesMenu = false;
            }
        }
    }

    // --- Search Module ---
    private static void searchInformationMenu(Scanner scan) {
        boolean inSearch = true;
        while(inSearch) {
            System.out.println("\n=== Search Information ===");
            System.out.println("1. Search Stock (by Model)");
            System.out.println("2. Search Sales (by Keyword)");
            System.out.println("3. Back");
            System.out.print("Select: ");
            String choice = scan.nextLine();

            if (choice.equals("1")) {
                System.out.print("Enter Model Name: ");
                String searchModel = scan.nextLine().toLowerCase();
                try (Scanner f = new Scanner(new File("model.csv"))) {
                    String header = f.nextLine(); 
                    String[] outlets = header.split(",");
                    boolean found = false;
                    while(f.hasNextLine()) {
                        String line = f.nextLine();
                        String[] parts = line.split(",");
                        if(parts[0].toLowerCase().contains(searchModel)) {
                            System.out.println("\nModel: " + parts[0]);
                            System.out.println("Stock Availability:");
                            for(int i=1; i<parts.length; i++) {
                                if(i < outlets.length)
                                    System.out.println(" - " + outlets[i].trim() + ": " + parts[i]);
                            }
                            found = true;
                        }
                    }
                    if(!found) System.out.println("Model not found.");
                } catch(Exception e) { System.out.println("Error reading model.csv"); }

            } else if (choice.equals("2")) {
                System.out.print("Enter Keyword (Name/Date/Model): ");
                String key = scan.nextLine().toLowerCase();
                try (Scanner f = new Scanner(new File("sales.csv"))) {
                    if(f.hasNextLine()) f.nextLine(); 
                    boolean found = false;
                    while(f.hasNextLine()) {
                        String line = f.nextLine();
                        if(line.toLowerCase().contains(key)) {
                            System.out.println("Found: " + line);
                            found = true;
                        }
                    }
                    if(!found) System.out.println("No records found.");
                } catch(Exception e) { System.out.println("Error reading sales.csv"); }

            } else if (choice.equals("3")) {
                inSearch = false;
            }
        }
    }

    // --- Edit Information Module ---
    private static void editInformationMenu(Scanner scan) {
        boolean inEdit = true;
        while(inEdit) {
            System.out.println("\n=== Edit Information ===");
            System.out.println("1. Edit Stock Quantity");
            System.out.println("2. Edit Sales Record");
            System.out.println("3. Back");
            System.out.print("Select: ");
            String choice = scan.nextLine();

            if (choice.equals("1")) {
                System.out.print("Enter Model Name to Edit: ");
                String modelTarget = scan.nextLine();
                System.out.print("Enter Outlet Code (column header): ");
                String outletTarget = scan.nextLine();
                
                List<String> lines = new ArrayList<>();
                int targetCol = -1;
                
                try (Scanner f = new Scanner(new File("model.csv"))) {
                    if(f.hasNextLine()) {
                        String header = f.nextLine();
                        lines.add(header);
                        String[] hParts = header.split(",");
                        for(int i=0; i<hParts.length; i++) {
                            if(hParts[i].trim().equalsIgnoreCase(outletTarget)) targetCol = i;
                        }
                    }
                    while(f.hasNextLine()) lines.add(f.nextLine());
                } catch(Exception e) { System.out.println("Error reading file."); continue; }

                if(targetCol == -1) {
                    System.out.println("Outlet column not found.");
                    continue;
                }

                boolean updated = false;
                for(int i=1; i<lines.size(); i++) {
                    String[] parts = lines.get(i).split(",");
                    if(parts[0].equalsIgnoreCase(modelTarget) && parts.length > targetCol) {
                        System.out.println("Current Stock: " + parts[targetCol]);
                        System.out.print("Enter New Stock Value: ");
                        String newVal = scan.nextLine();
                        parts[targetCol] = newVal;
                        lines.set(i, String.join(",", parts));
                        updated = true;
                        break;
                    }
                }

                if(updated) {
                    try (BufferedWriter w = new BufferedWriter(new FileWriter("model.csv"))) {
                        for(String l : lines) { w.write(l); w.newLine(); }
                        System.out.println("Stock updated successfully.");
                    } catch(Exception e) { System.out.println("Error writing file."); }
                } else {
                    System.out.println("Model not found.");
                }

            } else if (choice.equals("2")) {
                System.out.print("Enter Customer Name in record to edit: ");
                String searchCust = scan.nextLine();
                
                List<String> lines = new ArrayList<>();
                try(Scanner f = new Scanner(new File("sales.csv"))) {
                    while(f.hasNextLine()) lines.add(f.nextLine());
                } catch(Exception e) { System.out.println("No sales file."); continue; }

                int foundIndex = -1;
                for(int i=0; i<lines.size(); i++) {
                    if(lines.get(i).contains(searchCust)) {
                        System.out.println("Record Found: " + lines.get(i));
                        System.out.print("Is this the record? (y/n): ");
                        if(scan.nextLine().equalsIgnoreCase("y")) {
                            foundIndex = i;
                            break;
                        }
                    }
                }

                if(foundIndex != -1) {
                    System.out.println("Enter new full record details (Format: Date,Time,Outlet,User,Cust,Method,Total,Items):");
                    System.out.println("Or type 'DELETE' to remove.");
                    String input = scan.nextLine();
                    if(input.equalsIgnoreCase("DELETE")) {
                        lines.remove(foundIndex);
                        System.out.println("Record deleted.");
                    } else {
                        lines.set(foundIndex, input);
                        System.out.println("Record updated.");
                    }
                    try (BufferedWriter w = new BufferedWriter(new FileWriter("sales.csv"))) {
                        for(String l : lines) { w.write(l); w.newLine(); }
                    } catch(Exception e) { System.out.println("Error writing file."); }
                } else {
                    System.out.println("Record not found.");
                }

            } else if (choice.equals("3")) {
                inEdit = false;
            }
        }
    }

    // --- Helper Methods ---
    private static void updateStockCSV(ArrayList<String> soldModels, ArrayList<Integer> soldQtys, String outletCode) {
        try {
            List<String> lines = new ArrayList<>();
            int colIndex = -1;
            Scanner f = new Scanner(new File("model.csv"));
            if(f.hasNextLine()) {
                String h = f.nextLine();
                lines.add(h);
                String[] cols = h.split(",");
                for(int i=0; i<cols.length; i++) if(cols[i].trim().equalsIgnoreCase(outletCode)) colIndex = i;
            }
            while(f.hasNextLine()) lines.add(f.nextLine());
            f.close();

            if(colIndex != -1) {
                for(int i=1; i<lines.size(); i++) {
                    String[] parts = lines.get(i).split(",");
                    String mName = parts[0];
                    for(int j=0; j<soldModels.size(); j++) {
                        if(mName.equalsIgnoreCase(soldModels.get(j))) {
                            int cur = Integer.parseInt(parts[colIndex].trim());
                            int newQ = Math.max(0, cur - soldQtys.get(j));
                            parts[colIndex] = String.valueOf(newQ);
                        }
                    }
                    lines.set(i, String.join(",", parts));
                }
                BufferedWriter w = new BufferedWriter(new FileWriter("model.csv"));
                for(String l : lines) { w.write(l); w.newLine(); }
                w.close();
            }
        } catch(Exception e) { System.out.println("Error updating stock file."); }
    }

    private static void loadDataState() {
        try (Scanner sc = new Scanner(new File("employee.csv"))) {
            if(sc.hasNextLine()) sc.nextLine();
            while(sc.hasNextLine()) {
                String[] p = sc.nextLine().split(",");
                if(p.length >= 4) employeeList.add(new Employee(p[0], p[1], p[2], p[3]));
            }
        } catch(Exception e) { }
        
        try (Scanner sc = new Scanner(new File("model.csv"))) {
            if(sc.hasNextLine()) sc.nextLine(); 
            while(sc.hasNextLine()) {
                String[] p = sc.nextLine().split(",");
                List<Integer> qtys = new ArrayList<>();
                for(int i=1; i<p.length; i++) {
                    try { qtys.add(Integer.parseInt(p[i].trim())); } catch(Exception e) { qtys.add(0); }
                }
                modelListState.add(new Model(p[0], qtys));
            }
        } catch(Exception e) { }
    }
}