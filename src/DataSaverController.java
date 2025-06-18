
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import java.sql.*;
import java.util.function.Consumer;

public class DataSaverController {

    @FXML private TableView<DataModel> dataTableView;
    @FXML private TextField searchField;
    
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/medical_reports_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "@5688120@";
    private int totalRecords = 0;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadTotalRecordCount();
        loadDataFromDatabase();
    }

    // First get total number of records for countdown
    private void loadTotalRecordCount() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM reports")) {
            
            if (rs.next()) {
                totalRecords = rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to get record count: " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        // Use lambdas instead of method references to avoid compatibility issues
        createButtonColumn(0, "حذف", data -> handleDelete(data));
        createButtonColumn(1, "تعديل", data -> handleEdit(data));
        createButtonColumn(2, "مرافق المريض", data -> handleCompanion(data));
        createButtonColumn(3, "تقرير طبي", data -> handleMedical(data));
        createButtonColumn(4, "اجازة", data -> handleLeave(data));
    }
    
    @SuppressWarnings("unchecked") // Suppress the unchecked cast warning
    private void createButtonColumn(int columnIndex, String buttonText, Consumer<DataModel> action) {
        TableColumn<DataModel, Void> col = (TableColumn<DataModel, Void>) dataTableView.getColumns().get(columnIndex);
        col.setCellFactory(new Callback<TableColumn<DataModel, Void>, TableCell<DataModel, Void>>() {
            @Override
            public TableCell<DataModel, Void> call(final TableColumn<DataModel, Void> param) {
                return new TableCell<DataModel, Void>() {
                    private final Button button = new Button(buttonText);
                    
                    {
                        button.getStyleClass().add("action-button");
                        button.setOnAction(event -> {
                            if (getTableRow() != null && getTableRow().getItem() != null) {
                                DataModel data = getTableRow().getItem();
                                action.accept(data);
                            }
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : button);
                    }
                };
            }
        });
    }

    private void loadDataFromDatabase() {
        ObservableList<DataModel> data = FXCollections.observableArrayList();
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT report_id, name_arabic, id_number FROM reports ORDER BY report_id DESC")) {
            
            int currentCount = totalRecords;
            while (rs.next()) {
                data.add(new DataModel(
                    currentCount--,  // Countdown ID
                    rs.getString("report_id"),
                    rs.getString("name_arabic"),
                    rs.getString("id_number")
                ));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load data: " + e.getMessage());
        }
        
        dataTableView.setItems(data);
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadDataFromDatabase();
            return;
        }
        
        ObservableList<DataModel> filteredData = FXCollections.observableArrayList();
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT report_id, name_arabic, id_number FROM reports " +
                 "WHERE LOWER(name_arabic) LIKE ? ORDER BY report_id DESC")) {
            
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            
            // Get count for search results
            int searchCount = 0;
            try (Statement countStmt = conn.createStatement();
                 ResultSet countRs = countStmt.executeQuery(
                     "SELECT COUNT(*) AS total FROM reports WHERE LOWER(name_arabic) LIKE '%" + keyword + "%'")) {
                if (countRs.next()) searchCount = countRs.getInt("total");
            }
            
            int currentCount = searchCount;
            while (rs.next()) {
                filteredData.add(new DataModel(
                    currentCount--,  // Countdown ID for search results
                    rs.getString("report_id"),
                    rs.getString("name_arabic"),
                    rs.getString("id_number")
                ));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Search failed: " + e.getMessage());
        }
        
        dataTableView.setItems(filteredData);
    }
    
    private void handleDelete(DataModel data) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM reports WHERE report_id = ?")) {
            
            stmt.setString(1, data.getReportId());
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                showAlert("نجاح", "تم حذف السجل بنجاح");
                // Refresh counts and data
                loadTotalRecordCount();
                loadDataFromDatabase();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("خطأ", "فشل في حذف السجل: " + e.getMessage());
        }
    }
    
    private void handleEdit(DataModel data) {
        // Implement edit functionality
        System.out.println("Edit: " + data.getReportId());
    }
    
    private void handleCompanion(DataModel data) {
        // Implement companion functionality
        System.out.println("Companion: " + data.getReportId());
    }
    
    private void handleMedical(DataModel data) {
        // Implement medical report functionality
        System.out.println("Medical: " + data.getReportId());
    }
    
    private void handleLeave(DataModel data) {
        // Implement leave functionality
        System.out.println("Leave: " + data.getReportId());
    }
    
    // ADDED THE MISSING showAlert METHOD
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Data Model Class for the table
    public static class DataModel {
        private final int countdownId;
        private final String reportId;
        private final String nameArabic;
        private final String idNumber;
        
        public DataModel(int countdownId, String reportId, String nameArabic, String idNumber) {
            this.countdownId = countdownId;
            this.reportId = reportId;
            this.nameArabic = nameArabic;
            this.idNumber = idNumber;
        }
        
        // Getters
        public int getCountdownId() { return countdownId; }
        public String getReportId() { return reportId; }
        public String getNameArabic() { return nameArabic; }
        public String getIdNumber() { return idNumber; }
    }
}