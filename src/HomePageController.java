import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HomePageController {

    @FXML
    void CreateLeave(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Scene/CreateLeavePage.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(getClass().getResource("/Scene/SceneStyle/CreateLeavePage.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void MedicalReport(ActionEvent event) {
        // Placeholder for Medical Report functionality
    }

    @FXML
    void PatientEscort(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Scene/PatientCompanion.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(getClass().getResource("/Scene/SceneStyle/CreateLeavePage.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void ReviewScene(ActionEvent event) {
        // Placeholder for Review Scene functionality
    }

    @FXML
    void handleCreateUser(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("إنشاء مستخدم جديد");
        dialog.setHeaderText("أدخل بيانات المستخدم الجديد");
        dialog.setContentText("اسم المستخدم:");

        Optional<String> usernameResult = dialog.showAndWait();
        if (usernameResult.isPresent() && !usernameResult.get().isEmpty()) {
            String username = usernameResult.get();
            
            // Show password dialog
            PasswordInputDialog passwordDialog = new PasswordInputDialog();
            passwordDialog.setTitle("كلمة المرور");
            passwordDialog.setContentText("أدخل كلمة المرور:");
            
            Optional<String> passwordResult = passwordDialog.showAndWait();
            if (passwordResult.isPresent() && !passwordResult.get().isEmpty()) {
                createNewUser(username, passwordResult.get());
            }
        }
    }

    @FXML
    void handleDeleteUser(ActionEvent event) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>();
        dialog.setTitle("حذف مستخدم");
        dialog.setHeaderText("اختر المستخدم لحذفه");
        
        // Populate with existing users
        List<String> users = getExistingUsers();
        if (users.isEmpty()) {
            showAlert("لا يوجد مستخدمين", "لا يوجد مستخدمين متاحين للحذف");
            return;
        }
        
        dialog.getItems().addAll(users);
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::deleteUser);
    }

    private void createNewUser(String username, String password) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO users (username, password) VALUES (?, ?)")) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                showAlert("نجاح", "تم إنشاء المستخدم بنجاح");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("خطأ", "فشل في إنشاء المستخدم: " + e.getMessage());
        }
    }

    private void deleteUser(String username) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM users WHERE username = ?")) {
            
            stmt.setString(1, username);
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                showAlert("نجاح", "تم حذف المستخدم بنجاح");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("خطأ", "فشل في حذف المستخدم: " + e.getMessage());
        }
    }

    private List<String> getExistingUsers() {
        List<String> users = new ArrayList<>();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT username FROM users")) {
            
            while (rs.next()) {
                users.add(rs.getString("username"));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return users;
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
    
    // Helper class for password input dialog
    public static class PasswordInputDialog extends TextInputDialog {
        public PasswordInputDialog() {
            // Create a password field
            PasswordField passwordField = new PasswordField();
            passwordField.setPromptText("كلمة المرور");
            
            // Replace the default text field with password field
            this.setGraphic(null);
            this.setHeaderText(null);
            this.getDialogPane().setContent(passwordField);
            
            // Set result converter to get password text
            this.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return passwordField.getText();
                }
                return null;
            });
        }
    }

    @FXML
    void initialize() {
        // Initialization code if needed
    }
}