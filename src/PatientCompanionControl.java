import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.chrono.HijrahChronology;
import java.time.chrono.HijrahDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ResourceBundle;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Locale;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Controller class for the Patient Companion Page in a medical application.
 */
public class PatientCompanionControl {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private Stage stage;
    private Scene scene;

    // UI components
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
    
    private File selectedImageFile;

    // Date formatters
    private final DateTimeFormatter hijriFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd").withChronology(HijrahChronology.INSTANCE);
    private final DateTimeFormatter longDateFormatter = DateTimeFormatter.ofPattern("EEEE, d, MMMM yyyy").withLocale(Locale.ENGLISH);
    private final DateTimeFormatter standardDateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd").withLocale(Locale.ENGLISH);

    /**
     * Translates Arabic text to English using MyMemory Translation API
     */
    @FXML
    private void translateArabicToEnglish(String arabicText, TextField targetField) {
        if (arabicText == null || arabicText.trim().isEmpty()) {
            Platform.runLater(() -> targetField.setText(""));
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        try {
            URI uri = new URI("https", "api.mymemory.translated.net", "/get", "q=" + arabicText + "&langpair=ar|en", null);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(responseBody -> {
                        try {
                            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                            JsonObject responseData = jsonResponse.getAsJsonObject("responseData");

                            if (responseData != null && responseData.has("translatedText")) {
                                String translatedText = responseData.get("translatedText").getAsString();
                                String upperCaseText = translatedText.toUpperCase();
                                Platform.runLater(() -> targetField.setText(upperCaseText));
                            }
                        } catch (Exception e) {
                            System.err.println("Error parsing translation response: " + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            System.err.println("HTTP Error: " + e.getMessage());
        }
    }

    /**
     * Updates Hijri date fields based on Gregorian date selections
     */
    private void updateHijriDates() {
        if (ReportDateInGregorian.getValue() != null) {
            HijrahDate hijriDate = HijrahDate.from(ReportDateInGregorian.getValue());
            String formattedHijri = hijriDate.format(hijriFormatter);
            ReportDateHijri.setText(formattedHijri);
            StartOfTheHijriReport.setText(formattedHijri);
        }
        
        if (EndOFReportInGregorian.getValue() != null) {
            HijrahDate hijriEndDate = HijrahDate.from(EndOFReportInGregorian.getValue());
            EndOfHijriReport.setText(hijriEndDate.format(hijriFormatter));
        }
    }

    /**
     * Updates various date format fields based on the selected Gregorian date
     */
    private void updateDateFields() {
        LocalDate gregorianDate = ReportDateInGregorian.getValue();
        
        if (gregorianDate != null) {
            BeginningOfTheADReport.setText(gregorianDate.format(standardDateFormatter));
            Date.setText(gregorianDate.format(longDateFormatter));
        } else {
            BeginningOfTheADReport.setText("");
            Date.setText("");
        }
    }
    
    @FXML
    private void selectImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("اختر صورة للتوقيع");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("ملفات الصور", "*.png", "*.jpg", "*.jpeg")
        );
        selectedImageFile = fileChooser.showOpenDialog(stage);
        if (selectedImageFile != null) {
            imagePreview.setImage(new Image(selectedImageFile.toURI().toString()));
        }
    }

    /**
     * Calculates the duration between start and end dates
     */
    private String calculateDuration() {
        LocalDate start = ReportDateInGregorian.getValue();
        LocalDate end = EndOFReportInGregorian.getValue();
        
        if (start != null && end != null) {
            if (end.isBefore(start)) {
                return "Invalid (End date before start)";
            }
            
            long daysBetween = ChronoUnit.DAYS.between(start, end) + 1;
            return daysBetween + " days (" + formatDate(start) + " to " + formatDate(end) + ")";
        }
        return "";
    }

    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    /**
     * Navigates back to the home page
     */
    @FXML
    void BackToHomePAge(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Scene/HomePage.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
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

    /**
     * Generates a random report ID
     */
    @FXML
    void ReportID(ActionEvent event) {
        SecureRandom random = new SecureRandom();
        long number = Math.abs(random.nextLong()) % 1_000_000_00000L;
        String formatted = String.format("%011d", number);
        ReportID.setText("GSL" + formatted);
        System.out.println("ReportID is: " + ReportID.getText());
    }

    /**
     * Creates a new companion form
     */
    @FXML
    void NewCreateLeave(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Scene/CreateLeavePage.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(getClass().getResource("/Scene/SceneStyle/CreateLeavePage.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Saves the companion form data as a PDF document
     */
    @FXML
    void saveTheData(ActionEvent event) {
        // Get current time
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
        Time.setText(currentTime);
        
        new Thread(() -> {
            try {
                int daysCount = Integer.parseInt(NumberOfDays.getText());
                
                // Create final variables for PDF generation
                final String dateInGregorianString = ReportDateInGregorian.getValue() != null ?
                    ReportDateInGregorian.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yy")) : "";
                
                final String dateEndGregorianString = EndOFReportInGregorian.getValue() != null ?
                    EndOFReportInGregorian.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yy")) : "";
                
                // Get the path of the selected image if any
                String customImagePath = selectedImageFile != null ? selectedImageFile.getAbsolutePath() : null;
                
                CompanionReportData data = new CompanionReportData(
                    ReportID.getText(),
                    NameInArabic.getText(),
                    NameInEnglish.getText(),
                    DoctorNameInArabic.getText(),
                    DoctorNameInEnglish.getText(),
                    DoctorSpecialtyInArabic.getText(),
                    DoctorSpecialtyInEnglish.getText(),
                    HospitalNameInArabic.getText(),
                    HospitalNameInEnglish.getText(),
                    NationalityInArabic.getText(),
                    NationalityInEnglish.getText(),
                    ReportDateInGregorian.getValue(),
                    EndOFReportInGregorian.getValue(),
                    daysCount,
                    ReportDateHijri.getText(),
                    StartOfTheHijriReport.getText(),
                    EndOfHijriReport.getText(),
                    BeginningOfTheADReport.getText(),
                    Date.getText(),
                    currentTime,
                    IdNumber.getText(),
                    EmployerArabic.getText(),
                    LicenseNumber.getText(),
                    kinshipArabic.getText(),
                    kinshipEnglish.getText()
                );
                
                // Save to database
                boolean saveSuccess = CompanionDatabaseManager.saveReport(data);
                
                // Generate PDF if save successful
                if (saveSuccess) {
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
                    
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("نجاح");
                        alert.setHeaderText(null);
                        alert.setContentText("✅ تم حفظ البيانات وإنشاء ملف PDF بنجاح!");
                        alert.showAndWait();
                    });
                } else {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("خطأ في قاعدة البيانات");
                        alert.setHeaderText(null);
                        alert.setContentText("❌ فشل في حفظ البيانات في قاعدة البيانات!");
                        alert.showAndWait();
                    });
                }
                
            } catch (NumberFormatException e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("خطأ في الإدخال");
                    alert.setHeaderText(null);
                    alert.setContentText("تنسيق غير صحيح لعدد الأيام: " + e.getMessage());
                    alert.showAndWait();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("خطأ");
                    alert.setHeaderText(null);
                    alert.setContentText("حدث خطأ: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    @FXML
    void theDataPage(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Scene/dataSaver.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 1400, 900);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Initializes the controller class
     */
    @FXML
    void initialize() {
        String randomNumber = String.format("%011d", (long)(Math.random() * 1_000_000_00000L));
        String finalID = "GSL" + randomNumber;

        if (ReportID != null) {
            ReportID.setText(finalID);
        }

        // Setup translation listeners
        setupTranslationListener(NameInArabic, NameInEnglish);
        setupTranslationListener(DoctorNameInArabic, DoctorNameInEnglish);
        setupTranslationListener(DoctorSpecialtyInArabic, DoctorSpecialtyInEnglish);
        setupTranslationListener(HospitalNameInArabic, HospitalNameInEnglish);
        setupTranslationListener(NationalityInArabic, NationalityInEnglish);
        setupTranslationListener(kinshipArabic, kinshipEnglish); // New kinship translation

        // Date listeners
        ReportDateInGregorian.valueProperty().addListener((obs, oldDate, newDate) -> {
            calculateDays();
            updateHijriDates();
            updateDateFields();
        });
        
        EndOFReportInGregorian.valueProperty().addListener((obs, oldDate, newDate) -> {
            calculateDays();
            updateHijriDates();
        });
    }

    /**
     * Sets up a listener to automatically translate text from Arabic to English
     */
    private void setupTranslationListener(TextField sourceField, TextField targetField) {
        if (sourceField != null && targetField != null) {
            sourceField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.trim().isEmpty()) {
                    translateArabicToEnglish(newVal, targetField);
                }
            });
        }
    }

    /**
     * Calculates the number of days between start and end dates
     */
    private void calculateDays() {
        LocalDate startDate = ReportDateInGregorian.getValue();
        LocalDate endDate = EndOFReportInGregorian.getValue();
        
        if (startDate != null && endDate != null) {
            if (endDate.isBefore(startDate)) {
                NumberOfDays.setText("Invalid (End date before start)");
                return;
            }
            
            long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1; 
            NumberOfDays.setText(String.valueOf(daysBetween));
        }
    }
}