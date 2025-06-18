import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.time.LocalDate;
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


/**
 * Controller class for the Create Leave Page in a medical leave application.
 * This class handles:
 * - Creating medical leave certificates in both English and Arabic
 * - Automatic translation between Arabic and English fields
 * - Date conversion between Gregorian and Hijri calendars
 * - PDF generation with bilingual support
 * - Navigation between application screens
 */
public class CreateLeavePageControl {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Stage stage;
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

    // Date formatters for different date formats
    private final DateTimeFormatter hijriFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd").withChronology(HijrahChronology.INSTANCE);
    private final DateTimeFormatter longDateFormatter = DateTimeFormatter.ofPattern("EEEE, d, MMMM yyyy").withLocale(Locale.ENGLISH);
    private final DateTimeFormatter standardDateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd").withLocale(Locale.ENGLISH);

    /**
     * Translates Arabic text to English using MyMemory Translation API
     * @param arabicText The Arabic text to translate
     * @param targetField The TextField where the translation should be displayed
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

    /**
     * Calculates the duration between start and end dates
     * @return Formatted string with duration and date range
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

    /**
     * Formats a date as dd-MM-yyyy
     * @param date The date to format
     * @return Formatted date string
     */
    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }


    /**
     * Navigates back to the home page
     * @param event The action event
     * @throws IOException If the FXML file cannot be loaded
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

    /**
     * Generates a random report ID
     * @param event The action event
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
     * Creates a new leave form
     * @param event The action event
     * @throws IOException If the FXML file cannot be loaded
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
     * Saves the form data as a PDF document
     * @param event The action event
     */
    @FXML
    void saveTheData(ActionEvent event) {
       PdfMaker savePDF =new PdfMaker();
       //EndOFReportInGregorian
       LocalDate DateInGregorian = ReportDateInGregorian.getValue();
       String DateInGregorianString = DateInGregorian.format(DateTimeFormatter.ofPattern("dd-MM-yy"));

       LocalDate EndReportInGregorian = EndOFReportInGregorian.getValue();
       String DateEndGregorianString = EndReportInGregorian.format(DateTimeFormatter.ofPattern("dd-MM-yy"));

       String reportIdString = ReportID.getText();
       String IdNumberString = IdNumber.getText();
       String NameInArabicString = NameInArabic.getText();
       String NameInEnglishString = NameInEnglish.getText();
       String DoctorNameInArabichString = DoctorNameInArabic.getText();
       String DoctorNameInEnglishString = DoctorNameInEnglish.getText();
       String DoctorSpecialtyInArabicString = DoctorSpecialtyInArabic.getText();
       String DoctorSpecialtyInEnglishString = DoctorSpecialtyInEnglish.getText();
       String HospitalNameInArabicString = HospitalNameInArabic.getText();
       String HospitalNameInEnglishString = HospitalNameInEnglish.getText();
       String EmployerArabicString = EmployerArabic.getText();
       String NumberOfDaysString = NumberOfDays.getText();
       String LicenseNumberString = LicenseNumber.getText();
       String ReportDateHijriString = ReportDateHijri.getText();
       String StartOfTheHijriReportString = StartOfTheHijriReport.getText();
       String EndOfHijriReportString = EndOfHijriReport.getText();
       String BeginningOfTheADReportString = BeginningOfTheADReport.getText();
       String NationalityInArabicString = NationalityInArabic.getText();
       String NationalityInEnglishString = NationalityInEnglish.getText();
       String TimeString = Time.getText();
       String DateString = Date.getText();
       new Thread(()->{
               savePDF.SickLeaveF(reportIdString,IdNumberString,NameInArabicString,NameInEnglishString,DoctorNameInArabichString,DoctorNameInEnglishString,DoctorSpecialtyInArabicString,DoctorSpecialtyInEnglishString,HospitalNameInArabicString,HospitalNameInEnglishString,EmployerArabicString,DateInGregorianString,DateEndGregorianString,NumberOfDaysString,LicenseNumberString,ReportDateHijriString,StartOfTheHijriReportString,EndOfHijriReportString,BeginningOfTheADReportString,NationalityInArabicString,NationalityInEnglishString,TimeString,DateString);
            }
       ).start();

       new Thread(() -> {
        try {
            int daysCount = Integer.parseInt(NumberOfDaysString);
            
            ReportData data = new ReportData(
                reportIdString,
                NameInArabicString,
                NameInEnglishString,
                DoctorNameInArabichString,
                DoctorNameInEnglishString,
                DoctorSpecialtyInArabicString,
                DoctorSpecialtyInEnglishString,
                HospitalNameInArabicString,
                HospitalNameInEnglishString,
                NationalityInArabicString,
                NationalityInEnglishString,
                DateInGregorian,
                EndReportInGregorian,
                daysCount,
                ReportDateHijriString,
                StartOfTheHijriReportString,
                EndOfHijriReportString,
                BeginningOfTheADReportString,
                DateString,
                TimeString,
                IdNumberString,
                EmployerArabicString,
                LicenseNumberString
            );
            
            boolean saveSuccess = DatabaseManager.saveReport(data);
            
            Platform.runLater(() -> {
                Alert alert = new Alert(saveSuccess ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
                alert.setTitle("Database Status");
                alert.setHeaderText(null);
                alert.setContentText(saveSuccess ? 
                    "✅ Report saved to database successfully!" : 
                    "❌ Failed to save report to database!");
                alert.showAndWait();
            });
            
        } catch (NumberFormatException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Input Error");
                alert.setHeaderText(null);
                alert.setContentText("Invalid number format for days: " + e.getMessage());
                alert.showAndWait();
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setHeaderText(null);
                alert.setContentText("Error saving to database: " + e.getMessage());
                alert.showAndWait();
            });
        }
    }).start();

    
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded.
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
     * @param sourceField The source field containing Arabic text
     * @param targetField The target field for English translation
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