import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.ResourceBundle;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.net.http.*;
import com.google.gson.*;



import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CreateLeavePageControl {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Stage stage;
    private Scene scene;

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

    // ترجمة نص عربي إلى إنجليزي باستخدام LibreTranslate
  @FXML
private void translateArabicToEnglish(String arabicText, TextField targetField) {
    if (arabicText == null || arabicText.trim().isEmpty()) {
        Platform.runLater(() -> targetField.setText(""));
        return;
    }

    HttpClient client = HttpClient.newHttpClient();
    try {
        // إزالة الترميز المسبق للنص
        URI uri = new URI("https", "api.mymemory.translated.net", "/get", "q=" + arabicText + "&langpair=ar|en", null);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> {
                    try {
                        // تحليل الاستجابة JSON
                        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                        JsonObject responseData = jsonResponse.getAsJsonObject("responseData");

                        if (responseData != null && responseData.has("translatedText")) {
                            String translatedText = responseData.get("translatedText").getAsString();

                            // تحويل النص إلى حروف كبيرة
                            String upperCaseText = translatedText.toUpperCase();

                            // تعيين الترجمة في الحقل
                            Platform.runLater(() -> targetField.setText(upperCaseText));
                        } else {
                            System.err.println("Unexpected JSON response: " + responseBody);
                            Platform.runLater(() -> targetField.setText("[Parse Error]"));
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing translation response: " + e.getMessage());
                        Platform.runLater(() -> targetField.setText("[Error]"));
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Async HTTP Error: " + e.getMessage());
                    Platform.runLater(() -> targetField.setText("[Network Error]"));
                    return null;
                });

    } catch (Exception e) {
        System.err.println("HTTP Error: " + e.getMessage());
        Platform.runLater(() -> targetField.setText("[Network Error]"));
    }
}





    

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
    void ReportID(ActionEvent event) {
        SecureRandom random = new SecureRandom();
        long number = Math.abs(random.nextLong()) % 1_000_000_00000L;
        String formatted = String.format("%011d", number);
        ReportID.setText("GSL" + formatted);
        System.out.println("ReportID is: " + ReportID);
    }

    @FXML
    void NewCreateLeave(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Scene/CreateLeavePage.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    @FXML
    void saveTheData(ActionEvent event) {
        // Add your save functionality here
        System.out.println("Saving data...");
        // Example: Print all field values
        System.out.println("Report ID: " + ReportID.getText());
        System.out.println("Name (Arabic): " + NameInArabic.getText());
        System.out.println("Name (English): " + NameInEnglish.getText());
        // Add more fields as needed
    }

    @FXML
    void initialize() {
        // إنشاء رقم عشوائي لرقم الإجازة
        String randomNumber = String.format("%011d", (long)(Math.random() * 1_000_000_00000L));
        String finalID = "GSL" + randomNumber;

        if (ReportID != null) {
            ReportID.setText(finalID);
        } else {
            System.out.println("ReportID TextField is null! Check fx:id.");
        }

        // إعداد الترجمة التلقائية عند إدخال نص عربي

        if (NameInArabic != null && NameInEnglish != null) {
            NameInArabic.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.trim().isEmpty()) {
                    translateArabicToEnglish(newVal, NameInEnglish);
                }
            });
        }

        if (DoctorNameInArabic != null && DoctorNameInEnglish != null) {
            DoctorNameInArabic.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.trim().isEmpty()) {
                    translateArabicToEnglish(newVal, DoctorNameInEnglish);
                }
            });
        }

        if (DoctorSpecialtyInArabic != null && DoctorSpecialtyInEnglish != null) {
            DoctorSpecialtyInArabic.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.trim().isEmpty()) {
                    translateArabicToEnglish(newVal, DoctorSpecialtyInEnglish);
                }
            });
        }

        if (HospitalNameInArabic != null && HospitalNameInEnglish != null) {
            HospitalNameInArabic.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.trim().isEmpty()) {
                    translateArabicToEnglish(newVal, HospitalNameInEnglish);
                }
            });
        }

        if (NationalityInArabic != null && NationalityInEnglish != null) {
            NationalityInArabic.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.trim().isEmpty()) {
                    translateArabicToEnglish(newVal, NationalityInEnglish);
                }
            });
        }
    }
}
