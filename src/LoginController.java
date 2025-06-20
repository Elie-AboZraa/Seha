import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.*;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> dbServerComboBox;
    @FXML private TextField dbNameField;
    @FXML private TextField dbUserField;
    @FXML private PasswordField dbPassField;
    
    @FXML
    public void initialize() {
        // Populate database server options
        dbServerComboBox.getItems().addAll(
            "jdbc:mysql://127.0.0.1:3306/",
            "jdbc:mysql://localhost:3306/",
            "jdbc:mariadb://localhost:3306/"
        );
        dbServerComboBox.setValue("jdbc:mysql://127.0.0.1:3306/");
        dbNameField.setText("medical_reports_db");
        dbUserField.setText("root");
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String dbUrl = dbServerComboBox.getValue() + dbNameField.getText().trim();
        String dbUser = dbUserField.getText().trim();
        String dbPass = dbPassField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("خطأ في الإدخال", "يرجى إدخال اسم المستخدم وكلمة المرور");
            return;
        }
        
        try {
            // Test database connection
            testDatabaseConnection(dbUrl, dbUser, dbPass);
            
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT user_id, search_id_start FROM users WHERE username = ? AND password = ?")) {
                
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    int searchIdStart = rs.getInt("search_id_start");
                    
                    // Set user session with DB info
                    UserSession.setCurrentUser(userId, username, searchIdStart, dbUrl, dbUser, dbPass);
                    
                    // Open homepage
                    openHomepage();
                } else {
                    showAlert("خطأ في تسجيل الدخول", "اسم المستخدم أو كلمة المرور غير صحيحة");
                }
                
            } catch (SQLException | IOException e) {
                e.printStackTrace();
                showAlert("خطأ", "فشل في عملية الدخول: " + e.getMessage());
            }
        } catch (SQLException e) {
            showAlert("خطأ في قاعدة البيانات", "تعذر الاتصال بقاعدة البيانات:\n" + e.getMessage());
        }
    }
    
    @FXML
    private void handleCreateAccount() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String dbUrl = dbServerComboBox.getValue() + dbNameField.getText().trim();
        String dbUser = dbUserField.getText().trim();
        String dbPass = dbPassField.getText().trim();
        
        // Validate inputs
        if (username.isEmpty()) {
            showAlert("خطأ في الإدخال", "يرجى إدخال اسم المستخدم");
            return;
        }
        if (password.isEmpty()) {
            showAlert("خطأ في الإدخال", "يرجى إدخال كلمة المرور");
            return;
        }
        
        try {
            // Test database connection
            testDatabaseConnection(dbUrl, dbUser, dbPass);
            
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                 PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO users (username, password) VALUES (?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
                
                stmt.setString(1, username);
                stmt.setString(2, password);
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int userId = generatedKeys.getInt(1);
                            UserSession.setCurrentUser(userId, username, 1, dbUrl, dbUser, dbPass);
                            openHomepage();
                        }
                    }
                }
                
            } catch (SQLException | IOException e) {
                e.printStackTrace();
                showAlert("خطأ في قاعدة البيانات", "فشل في إنشاء الحساب: " + e.getMessage());
            }
        } catch (SQLException e) {
            showAlert("خطأ في قاعدة البيانات", "تعذر الاتصال بقاعدة البيانات:\n" + e.getMessage());
        }
    }
    
    private void testDatabaseConnection(String dbUrl, String dbUser, String dbPass) throws SQLException {
        try (Connection testConn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
            if (!testConn.isValid(2)) {
                throw new SQLException("فشل في التحقق من صحة الاتصال");
            }
        }
    }
    
    private void openHomepage() throws IOException {
        // Close login window
        Stage loginStage = (Stage) usernameField.getScene().getWindow();
        loginStage.close();
        
        // Open homepage
        Stage homeStage = new Stage();
        URL fxmlUrl = getClass().getResource("/Scene/HomePage.fxml");
        if (fxmlUrl == null) {
            throw new RuntimeException("HomePage.fxml not found");
        }
        Parent root = FXMLLoader.load(fxmlUrl);
        Scene scene = new Scene(root, 1400, 900);
        
        // Apply CSS for home page
        URL cssUrl = getClass().getResource("/Scene/SceneStyle/HomePage.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("HomePage.css not found");
        }
        
        homeStage.setTitle("نظام التقارير الطبية");
        homeStage.setScene(scene);
        homeStage.show();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}