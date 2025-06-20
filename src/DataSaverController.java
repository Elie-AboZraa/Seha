import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.function.Consumer;

public class DataSaverController {

    @FXML private TableView<DataModel> dataTableView;
    @FXML private TextField searchField;
    
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/medical_reports_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "@5688120@";

    @FXML
    public void initialize() {
        // Set up button columns
        setupButtonColumns();
        
        // Load initial data
        loadDataFromDatabase();
    }
     @FXML
    void BackCreateLeave(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Scene/CreateLeavePage.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(getClass().getResource("/Scene/SceneStyle/CreateLeavePage.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
      @FXML
    void theDataPage(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Scene/dataSaver.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 1400, 900);  // Add dimensions
        stage.setScene(scene);
        stage.show();
    }
    @FXML
    void BackToHomePAge(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Scene/HomePage.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(getClass().getResource("/Scene/SceneStyle/HomePage.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private void setupButtonColumns() {
        // Button columns indices (columns 4-8)
        setupButtonColumn(4, "اجازة", this::handleLeave);
        setupButtonColumn(5, "تقرير طبي", this::handleMedical);
        setupButtonColumn(6, "مرافق المريض", this::handleCompanion);
        setupButtonColumn(7, "تعديل", this::handleEdit);
        setupButtonColumn(8, "حذف", this::handleDelete);
    }

    private void setupButtonColumn(int columnIndex, String buttonText, Consumer<DataModel> action) {
        // Get the column
        TableColumn<DataModel, Void> col = (TableColumn<DataModel, Void>) dataTableView.getColumns().get(columnIndex);
        
        // Set cell factory
        col.setCellFactory(param -> new TableCell<DataModel, Void>() {
            private final Button button = new Button(buttonText);
            
            {
                button.getStyleClass().add("action-button");
                button.setOnAction(event -> {
                    DataModel data = getTableRow().getItem();
                    if (data != null) {
                        action.accept(data);
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(button);
                }
            }
        });
    }

    private void loadDataFromDatabase() {
        ObservableList<DataModel> data = FXCollections.observableArrayList();
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT search_id, report_id, name_arabic, id_number " +
                 "FROM reports ORDER BY search_id DESC")) {
            
            while (rs.next()) {
                data.add(new DataModel(
                    rs.getInt("search_id"),
                    rs.getString("report_id"),
                    rs.getString("name_arabic"),
                    rs.getString("id_number")
                ));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("خطأ في قاعدة البيانات", "فشل تحميل البيانات: " + e.getMessage());
        }
        
        dataTableView.setItems(data);
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadDataFromDatabase();
            return;
        }
        
        ObservableList<DataModel> filteredData = FXCollections.observableArrayList();
        String searchTerm = "%" + keyword + "%";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT search_id, report_id, name_arabic, id_number " +
                "FROM reports WHERE name_arabic LIKE ? OR CAST(search_id AS CHAR) LIKE ? " +
                "ORDER BY search_id DESC")) {
            
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                filteredData.add(new DataModel(
                    rs.getInt("search_id"),
                    rs.getString("report_id"),
                    rs.getString("name_arabic"),
                    rs.getString("id_number")
                ));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("خطأ في البحث", "فشل البحث: " + e.getMessage());
        }
        
        dataTableView.setItems(filteredData);
    }
    
    // Button handlers as requested
    private void handleDelete(DataModel data) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM reports WHERE report_id = ?")) {
            
            stmt.setString(1, data.getReportId());
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                showAlert("نجاح", "تم حذف السجل بنجاح");
                loadDataFromDatabase();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("خطأ", "فشل في حذف السجل: " + e.getMessage());
        }
    }
    
    private void handleEdit(DataModel data) {
        System.out.println("Edit: " + data.getReportId());
        // Implement actual edit functionality here
    }
    
    private void handleCompanion(DataModel data) {
        System.out.println("Companion: " + data.getReportId());
        // Implement actual companion functionality here
    }
    
    private void handleMedical(DataModel data) {
        System.out.println("Medical: " + data.getReportId());
        // Implement actual medical report functionality here
    }
    
    private void handleLeave(DataModel data) {
        System.out.println("Leave: " + data.getReportId());
        // Implement actual leave functionality here
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static class DataModel {
        private final int searchId;
        private final String reportId;
        private final String nameArabic;
        private final String idNumber;
        
        public DataModel(int searchId, String reportId, String nameArabic, String idNumber) {
            this.searchId = searchId;
            this.reportId = reportId;
            this.nameArabic = nameArabic;
            this.idNumber = idNumber;
        }
        
        // Getters (MUST match PropertyValueFactory names)
        public int getSearchId() { return searchId; }
        public String getReportId() { return reportId; }
        public String getNameArabic() { return nameArabic; }
        public String getIdNumber() { return idNumber; }
    }
    
}