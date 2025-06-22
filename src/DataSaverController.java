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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class DataSaverController {

    @FXML private TableView<DataModel> dataTableView;
    @FXML private TextField searchField;
    
    @FXML
    public void initialize() {
        setupTableColumns();
        loadDataFromDatabase();
    }
    
    private void setupTableColumns() {
        // ID Column
        TableColumn<DataModel, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("searchId"));
        idCol.setPrefWidth(100);
        
        // Name Column
        TableColumn<DataModel, String> nameCol = new TableColumn<>("الاسم");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("nameArabic"));
        nameCol.setPrefWidth(300);
        
        // Report ID Column
        TableColumn<DataModel, String> reportIdCol = new TableColumn<>("رمز الاجازة");
        reportIdCol.setCellValueFactory(new PropertyValueFactory<>("reportId"));
        reportIdCol.setPrefWidth(200);
        
        // ID Number Column
        TableColumn<DataModel, String> idNumberCol = new TableColumn<>("الرقم الوطني");
        idNumberCol.setCellValueFactory(new PropertyValueFactory<>("idNumber"));
        idNumberCol.setPrefWidth(200);
        
        // Action Buttons
        TableColumn<DataModel, Void> leaveCol = new TableColumn<>("اجازة");
        TableColumn<DataModel, Void> medicalCol = new TableColumn<>("تقرير طبي");
        TableColumn<DataModel, Void> companionCol = new TableColumn<>("مرافق المريض");
        TableColumn<DataModel, Void> editCol = new TableColumn<>("تعديل");
        TableColumn<DataModel, Void> deleteCol = new TableColumn<>("حذف");
        
        setupButtonColumn(leaveCol, "اجازة", this::handleLeave);
        setupButtonColumn(medicalCol, "تقرير طبي", this::handleMedical);
        setupButtonColumn(companionCol, "مرافق المريض", this::handleCompanion);
        setupButtonColumn(editCol, "تعديل", this::handleEdit);
        setupButtonColumn(deleteCol, "حذف", this::handleDelete);
        
        // Add all columns to table
        dataTableView.getColumns().addAll(
            idCol, nameCol, reportIdCol, idNumberCol, 
            leaveCol, medicalCol, companionCol, editCol, deleteCol
        );
    }

    private void setupButtonColumn(TableColumn<DataModel, Void> col, String buttonText, 
                                  Consumer<DataModel> action) {
        col.setCellFactory(param -> new TableCell<DataModel, Void>() {
            private final Button button = new Button(buttonText);
            {
                button.getStyleClass().add("action-button");
                button.setOnAction(event -> {
                    DataModel data = getTableRow().getItem();
                    if (data != null) action.accept(data);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic((empty || getTableRow() == null || getTableRow().getItem() == null) ? 
                    null : button);
            }
        });
    }

    private void loadDataFromDatabase() {
        int currentUserId = UserSession.getInstance().getUserId();
        ObservableList<DataModel> data = FXCollections.observableArrayList();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT id AS search_id, report_id, name_arabic, id_number " +
                 "FROM reports WHERE user_id = ? ORDER BY id DESC")) {
            
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                data.add(new DataModel(
                    rs.getInt("search_id"),
                    rs.getString("report_id"),
                    rs.getString("name_arabic"),
                    rs.getString("id_number")
                ));
            }
            dataTableView.setItems(data);
            
        } catch (SQLException e) {
            showAlert("خطأ في قاعدة البيانات", "فشل في تحميل البيانات: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        int currentUserId = UserSession.getInstance().getUserId();
        String keyword = searchField.getText().trim();
        
        if (keyword.isEmpty()) {
            loadDataFromDatabase();
            return;
        }
        
        ObservableList<DataModel> filteredData = FXCollections.observableArrayList();
        String searchTerm = "%" + keyword + "%";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT id AS search_id, report_id, name_arabic, id_number " +
                 "FROM reports WHERE user_id = ? AND (name_arabic LIKE ? OR CAST(id AS CHAR) LIKE ?) " +
                 "ORDER BY id DESC")) {
            
            stmt.setInt(1, currentUserId);
            stmt.setString(2, searchTerm);
            stmt.setString(3, searchTerm);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                filteredData.add(new DataModel(
                    rs.getInt("search_id"),
                    rs.getString("report_id"),
                    rs.getString("name_arabic"),
                    rs.getString("id_number")
                ));
            }
            dataTableView.setItems(filteredData);
            
        } catch (SQLException e) {
            showAlert("خطأ في البحث", "فشل في البحث: " + e.getMessage());
        }
    }
    
    private void handleDelete(DataModel data) {
        int currentUserId = UserSession.getInstance().getUserId();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM reports WHERE id = ? AND user_id = ?")) {
            
            stmt.setInt(1, data.getSearchId());
            stmt.setInt(2, currentUserId);
            
            if (stmt.executeUpdate() > 0) {
                showAlert("نجاح", "تم حذف السجل بنجاح");
                loadDataFromDatabase();
            }
        } catch (SQLException e) {
            showAlert("خطأ", "فشل في الحذف: " + e.getMessage());
        }
    }
    
    private void handleEdit(DataModel data) {
        try {
            // Load the edit form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scene/EditReportForm.fxml"));
            Parent root = loader.load();
            
            // Pass the search ID to the edit controller
            EditReportFormController controller = loader.getController();
            controller.setSearchId(data.getSearchId());
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("تعديل التقرير");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("خطأ", "فشل في فتح نموذج التعديل: " + e.getMessage());
        }
    }
    
    private void handleLeave(DataModel data) {
        try {
            Map<String, Object> reportData = loadFullReport(data.getSearchId());
            openReportForm(reportData, "leave");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert("خطأ", "فشل في تحميل نموذج الإجازة: " + e.getMessage());
        }
    }
    
    private void handleCompanion(DataModel data) {
        try {
            Map<String, Object> reportData = loadFullReport(data.getSearchId());
            openReportForm(reportData, "companion");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert("خطأ", "فشل في تحميل نموذج المرافق: " + e.getMessage());
        }
    }
    
    private void handleMedical(DataModel data) {
        showAlert("تقرير طبي", "تقرير طبي لـ: " + data.getNameArabic());
    }
    
    private void openReportForm(Map<String, Object> reportData, String mode) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Scene/ReportForm.fxml"));
        Parent root = loader.load();
        
        ReportFormController controller = loader.getController();
        controller.setMode(mode);
        controller.populateForm(reportData);
        
        Stage stage = (Stage) dataTableView.getScene().getWindow();
        stage.setScene(new Scene(root, 1400, 900));
        stage.show();
    }
    
    private Map<String, Object> loadFullReport(int searchId) throws SQLException {
        Map<String, Object> reportData = new HashMap<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM reports WHERE id = ?")) {
            
            stmt.setInt(1, searchId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    
                    // Special handling for date fields
                    if (columnName.equals("date_gregorian") || 
                        columnName.equals("end_date_gregorian")) {
                        java.sql.Date dateValue = rs.getDate(i);
                        reportData.put(columnName, dateValue);
                    } else {
                        reportData.put(columnName, rs.getObject(i));
                    }
                }
            }
        }
        return reportData;
    }

    private Connection getConnection() throws SQLException {
        UserSession session = UserSession.getInstance();
        return DriverManager.getConnection(
            session.getDbUrl(), 
            session.getDbUser(), 
            session.getDbPassword()
        );
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
        Scene scene = new Scene(root, 1400, 900);
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
    
    @FXML
    void PatientCompanion(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Scene/PatientCompanion.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(getClass().getResource("/Scene/SceneStyle/CreateLeavePage.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
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
        
        public int getSearchId() { return searchId; }
        public String getReportId() { return reportId; }
        public String getNameArabic() { return nameArabic; }
        public String getIdNumber() { return idNumber; }
    }
}