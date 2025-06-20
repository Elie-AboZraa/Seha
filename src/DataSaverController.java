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
    
    @FXML
    public void initialize() {
        setupButtonColumns();
        loadDataFromDatabase();
    }
    
    @SuppressWarnings("unchecked")
    private void setupButtonColumns() {
        setupButtonColumn(4, "اجازة", this::handleLeave);
        setupButtonColumn(5, "تقرير طبي", this::handleMedical);
        setupButtonColumn(6, "مرافق المريض", this::handleCompanion);
        setupButtonColumn(7, "تعديل", this::handleEdit);
        setupButtonColumn(8, "حذف", this::handleDelete);
    }

    private void setupButtonColumn(int columnIndex, String buttonText, Consumer<DataModel> action) {
        // Suppress warnings for this cast
        @SuppressWarnings("unchecked")
        TableColumn<DataModel, Void> col = (TableColumn<DataModel, Void>) dataTableView.getColumns().get(columnIndex);
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
            showAlert("Database Error", "Failed to load data: " + e.getMessage());
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
            showAlert("Search Error", "Search failed: " + e.getMessage());
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
                showAlert("Success", "Record deleted successfully");
                loadDataFromDatabase();
            }
        } catch (SQLException e) {
            showAlert("Error", "Delete failed: " + e.getMessage());
        }
    }
    
    private void handleEdit(DataModel data) {
        System.out.println("Edit: " + data.getReportId());
    }
    
    private void handleCompanion(DataModel data) {
        System.out.println("Companion: " + data.getReportId());
    }
    
    private void handleMedical(DataModel data) {
        System.out.println("Medical: " + data.getReportId());
    }
    
    private void handleLeave(DataModel data) {
        System.out.println("Leave: " + data.getReportId());
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
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).showAndWait();
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