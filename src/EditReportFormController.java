import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class EditReportFormController implements Initializable {

    // Form fields
    @FXML private TextField searchIdField;
    @FXML private TextField reportIdField;
    @FXML private TextField nameArabicField;
    @FXML private TextField nameEnglishField;
    @FXML private TextField doctorArabicField;
    @FXML private TextField doctorEnglishField;
    @FXML private TextField specialtyArabicField;
    @FXML private TextField specialtyEnglishField;
    @FXML private TextField hospitalArabicField;
    @FXML private TextField hospitalEnglishField;
    @FXML private TextField nationalityArabicField;
    @FXML private TextField nationalityEnglishField;
    @FXML private DatePicker reportDateGregorianField;
    @FXML private DatePicker endDateGregorianField;
    @FXML private TextField daysCountField;
    @FXML private TextField reportDateHijriField;
    @FXML private TextField startHijriField;
    @FXML private TextField endHijriField;
    @FXML private TextField beginAdField;
    @FXML private TextField dateStringField;
    @FXML private TextField timeStringField;
    @FXML private TextField idNumberField;
    @FXML private TextField employerArabicField;
    @FXML private TextField licenseNumberField;
    @FXML private TextField kinshipArabicField;
    @FXML private TextField kinshipEnglishField;
    
    @FXML private Button saveButton;
    
    private int searchId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        saveButton.setOnAction(e -> saveChanges());
    }
    
    public void setSearchId(int searchId) {
        this.searchId = searchId;
        searchIdField.setText(String.valueOf(searchId));
        loadReportData();
    }
    
    private void loadReportData() {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM reports WHERE id = ?")) {
            
            stmt.setInt(1, searchId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                // Populate all fields from database
                reportIdField.setText(rs.getString("report_id"));
                nameArabicField.setText(rs.getString("name_arabic"));
                nameEnglishField.setText(rs.getString("name_english"));
                doctorArabicField.setText(rs.getString("doctor_arabic"));
                doctorEnglishField.setText(rs.getString("doctor_english"));
                specialtyArabicField.setText(rs.getString("specialty_arabic"));
                specialtyEnglishField.setText(rs.getString("specialty_english"));
                hospitalArabicField.setText(rs.getString("hospital_arabic"));
                hospitalEnglishField.setText(rs.getString("hospital_english"));
                nationalityArabicField.setText(rs.getString("nationality_arabic"));
                nationalityEnglishField.setText(rs.getString("nationality_english"));
                
                Date dateGregorian = rs.getDate("date_gregorian");
                if (dateGregorian != null) {
                    reportDateGregorianField.setValue(dateGregorian.toLocalDate());
                }
                
                Date endDateGregorian = rs.getDate("end_date_gregorian");
                if (endDateGregorian != null) {
                    endDateGregorianField.setValue(endDateGregorian.toLocalDate());
                }
                
                daysCountField.setText(String.valueOf(rs.getInt("days_count")));
                reportDateHijriField.setText(rs.getString("date_hijri"));
                startHijriField.setText(rs.getString("start_hijri"));
                endHijriField.setText(rs.getString("end_hijri"));
                beginAdField.setText(rs.getString("begin_ad"));
                dateStringField.setText(rs.getString("date_string"));
                timeStringField.setText(rs.getString("time_string"));
                idNumberField.setText(rs.getString("id_number"));
                employerArabicField.setText(rs.getString("employer_arabic"));
                licenseNumberField.setText(rs.getString("license_number"));
                
                // Kinship fields (if they exist in the database)
                try {
                    kinshipArabicField.setText(rs.getString("kinship_arabic"));
                    kinshipEnglishField.setText(rs.getString("kinship_english"));
                } catch (SQLException e) {
                    // Kinship columns might not exist in all reports
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("خطأ في قاعدة البيانات", "فشل في تحميل بيانات التقرير: " + e.getMessage());
        }
    }
    
    private void saveChanges() {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE reports SET " +
                 "report_id = ?, name_arabic = ?, name_english = ?, " +
                 "doctor_arabic = ?, doctor_english = ?, " +
                 "specialty_arabic = ?, specialty_english = ?, " +
                 "hospital_arabic = ?, hospital_english = ?, " +
                 "nationality_arabic = ?, nationality_english = ?, " +
                 "date_gregorian = ?, end_date_gregorian = ?, " +
                 "days_count = ?, date_hijri = ?, start_hijri = ?, " +
                 "end_hijri = ?, begin_ad = ?, date_string = ?, " +
                 "time_string = ?, id_number = ?, employer_arabic = ?, " +
                 "license_number = ?, kinship_arabic = ?, kinship_english = ? " +
                 "WHERE id = ?")) {
            
            // Set all parameters from form fields
            int paramIndex = 1;
            stmt.setString(paramIndex++, reportIdField.getText());
            stmt.setString(paramIndex++, nameArabicField.getText());
            stmt.setString(paramIndex++, nameEnglishField.getText());
            stmt.setString(paramIndex++, doctorArabicField.getText());
            stmt.setString(paramIndex++, doctorEnglishField.getText());
            stmt.setString(paramIndex++, specialtyArabicField.getText());
            stmt.setString(paramIndex++, specialtyEnglishField.getText());
            stmt.setString(paramIndex++, hospitalArabicField.getText());
            stmt.setString(paramIndex++, hospitalEnglishField.getText());
            stmt.setString(paramIndex++, nationalityArabicField.getText());
            stmt.setString(paramIndex++, nationalityEnglishField.getText());
            
            // Handle dates
            LocalDate reportDate = reportDateGregorianField.getValue();
            if (reportDate != null) {
                stmt.setDate(paramIndex++, Date.valueOf(reportDate));
            } else {
                stmt.setNull(paramIndex++, Types.DATE);
            }
            
            LocalDate endDate = endDateGregorianField.getValue();
            if (endDate != null) {
                stmt.setDate(paramIndex++, Date.valueOf(endDate));
            } else {
                stmt.setNull(paramIndex++, Types.DATE);
            }
            
            // Set other fields
            stmt.setInt(paramIndex++, Integer.parseInt(daysCountField.getText()));
            stmt.setString(paramIndex++, reportDateHijriField.getText());
            stmt.setString(paramIndex++, startHijriField.getText());
            stmt.setString(paramIndex++, endHijriField.getText());
            stmt.setString(paramIndex++, beginAdField.getText());
            stmt.setString(paramIndex++, dateStringField.getText());
            stmt.setString(paramIndex++, timeStringField.getText());
            stmt.setString(paramIndex++, idNumberField.getText());
            stmt.setString(paramIndex++, employerArabicField.getText());
            stmt.setString(paramIndex++, licenseNumberField.getText());
            stmt.setString(paramIndex++, kinshipArabicField.getText());
            stmt.setString(paramIndex++, kinshipEnglishField.getText());
            
            // Where clause
            stmt.setInt(paramIndex, searchId);
            
            // Execute update
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showAlert("نجاح", "تم تحديث البيانات بنجاح");
                closeWindow();
            } else {
                showAlert("خطأ", "لم يتم تحديث أي سجلات");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("خطأ في قاعدة البيانات", "فشل في تحديث التقرير: " + e.getMessage());
        } catch (NumberFormatException e) {
            showAlert("خطأ في الإدخال", "عدد الأيام يجب أن يكون رقماً: " + e.getMessage());
        }
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
    
    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}