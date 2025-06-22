import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ResourceBundle;

public class ReportFormController implements Initializable {

    @FXML private TextField ReportID;
    @FXML private TextField NameInArabic;
    @FXML private TextField NameInEnglish;
    @FXML private TextField DoctorNameInArabic;
    @FXML private TextField DoctorNameInEnglish;
    @FXML private TextField DoctorSpecialtyInArabic;
    @FXML private TextField DoctorSpecialtyInEnglish;
    @FXML private TextField HospitalNameInArabic;
    @FXML private TextField HospitalNameInEnglish;
    @FXML private TextField NationalityInArabic;
    @FXML private TextField NationalityInEnglish;
    @FXML private DatePicker ReportDateInGregorian;
    @FXML private DatePicker EndOFReportInGregorian;
    @FXML private TextField NumberOfDays;
    @FXML private TextField ReportDateHijri;
    @FXML private TextField StartOfTheHijriReport;
    @FXML private TextField EndOfHijriReport;
    @FXML private TextField BeginningOfTheADReport;
    @FXML private TextField Date;
    @FXML private TextField Time;
    @FXML private TextField IdNumber;
    @FXML private TextField EmployerArabic;
    @FXML private TextField LicenseNumber;
    @FXML private Button selectImageButton;
    @FXML private ImageView imagePreview;
    @FXML private TextField kinshipArabic;
    @FXML private TextField kinshipEnglish;
    @FXML private Button savePdfButton;
    
    private File selectedImageFile;
    private String mode;
    private Map<String, Object> reportData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        savePdfButton.setOnAction(e -> savePdf());
        selectImageButton.setOnAction(e -> selectImage());
    }
    
    public void setMode(String mode) {
        this.mode = mode;
        // Hide kinship fields for leave reports
        if ("leave".equals(mode)) {
            kinshipArabic.setVisible(false);
            kinshipEnglish.setVisible(false);
        }
    }
    
    public void populateForm(Map<String, Object> reportData) {
        this.reportData = reportData;
        
        // Populate all fields from report data
        ReportID.setText(getString(reportData, "report_id"));
        NameInArabic.setText(getString(reportData, "name_arabic"));
        NameInEnglish.setText(getString(reportData, "name_english"));
        IdNumber.setText(getString(reportData, "id_number"));
        DoctorNameInArabic.setText(getString(reportData, "doctor_arabic"));
        DoctorNameInEnglish.setText(getString(reportData, "doctor_english"));
        DoctorSpecialtyInArabic.setText(getString(reportData, "specialty_arabic"));
        DoctorSpecialtyInEnglish.setText(getString(reportData, "specialty_english"));
        HospitalNameInArabic.setText(getString(reportData, "hospital_arabic"));
        HospitalNameInEnglish.setText(getString(reportData, "hospital_english"));
        EmployerArabic.setText(getString(reportData, "employer_arabic"));
        LicenseNumber.setText(getString(reportData, "license_number"));
        NationalityInArabic.setText(getString(reportData, "nationality_arabic"));
        NationalityInEnglish.setText(getString(reportData, "nationality_english"));
        kinshipArabic.setText(getString(reportData, "kinship_arabic"));
        kinshipEnglish.setText(getString(reportData, "kinship_english"));
        
        // Handle dates
        Date dateGregorian = (Date) reportData.get("date_gregorian");
        if (dateGregorian != null) {
            ReportDateInGregorian.setValue(dateGregorian.toLocalDate());
        }
        
        Date endDateGregorian = (Date) reportData.get("end_date_gregorian");
        if (endDateGregorian != null) {
            EndOFReportInGregorian.setValue(endDateGregorian.toLocalDate());
        }
        
        // Set calculated fields
        NumberOfDays.setText(getString(reportData, "days_count"));
        ReportDateHijri.setText(getString(reportData, "date_hijri"));
        StartOfTheHijriReport.setText(getString(reportData, "start_hijri"));
        EndOfHijriReport.setText(getString(reportData, "end_hijri"));
        BeginningOfTheADReport.setText(getString(reportData, "begin_ad"));
        Date.setText(getString(reportData, "date_string"));
        Time.setText(getString(reportData, "time_string"));
    }
    
    private String getString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : "";
    }
    
    private void selectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("اختر صورة للتوقيع");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("ملفات الصور", "*.png", "*.jpg", "*.jpeg")
        );
        selectedImageFile = fileChooser.showOpenDialog(selectImageButton.getScene().getWindow());
        if (selectedImageFile != null) {
            imagePreview.setImage(new Image(selectedImageFile.toURI().toString()));
        }
    }
     @FXML
    void theDataPage(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Scene/dataSaver.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 1400, 900);  // Add dimensions
        stage.setScene(scene);
        stage.show();
    }
    
    private void savePdf() {
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
        String dateInGregorianString = "";
        String dateEndGregorianString = "";
        
        LocalDate reportDate = ReportDateInGregorian.getValue();
        if (reportDate != null) {
            dateInGregorianString = reportDate.format(DateTimeFormatter.ofPattern("dd-MM-yy"));
        }
        
        LocalDate endDate = EndOFReportInGregorian.getValue();
        if (endDate != null) {
            dateEndGregorianString = endDate.format(DateTimeFormatter.ofPattern("dd-MM-yy"));
        }
        
        String customImagePath = selectedImageFile != null ? selectedImageFile.getAbsolutePath() : null;
        
        if ("companion".equals(mode)) {
            CompanionPdfMaker pdfMaker = new CompanionPdfMaker();
            pdfMaker.CompanionReport(
                ReportID.getText(),
                IdNumber.getText(),
                NameInArabic.getText(),
                NameInEnglish.getText(),
                DoctorNameInArabic.getText(),
                DoctorNameInEnglish.getText(),
                DoctorSpecialtyInArabic.getText(),
                DoctorSpecialtyInEnglish.getText(),
                HospitalNameInArabic.getText(),
                HospitalNameInEnglish.getText(),
                EmployerArabic.getText(),
                dateInGregorianString,
                dateEndGregorianString,
                NumberOfDays.getText(),
                LicenseNumber.getText(),
                ReportDateHijri.getText(),
                StartOfTheHijriReport.getText(),
                EndOfHijriReport.getText(),
                BeginningOfTheADReport.getText(),
                NationalityInArabic.getText(),
                NationalityInEnglish.getText(),
                currentTime,
                Date.getText(),
                kinshipArabic.getText(),
                kinshipEnglish.getText(),
                customImagePath
            );
        } else {
            PdfMaker pdfMaker = new PdfMaker();
            pdfMaker.SickLeaveF(
                ReportID.getText(),
                IdNumber.getText(),
                NameInArabic.getText(),
                NameInEnglish.getText(),
                DoctorNameInArabic.getText(),
                DoctorNameInEnglish.getText(),
                DoctorSpecialtyInArabic.getText(),
                DoctorSpecialtyInEnglish.getText(),
                HospitalNameInArabic.getText(),
                HospitalNameInEnglish.getText(),
                EmployerArabic.getText(),
                dateInGregorianString,
                dateEndGregorianString,
                NumberOfDays.getText(),
                LicenseNumber.getText(),
                ReportDateHijri.getText(),
                StartOfTheHijriReport.getText(),
                EndOfHijriReport.getText(),
                BeginningOfTheADReport.getText(),
                NationalityInArabic.getText(),
                NationalityInEnglish.getText(),
                currentTime,
                Date.getText(),
                customImagePath
            );
        }
        
        // Show success message
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("نجاح");
        alert.setHeaderText(null);
        alert.setContentText("تم إنشاء ملف PDF بنجاح!");
        alert.showAndWait();
    }
}