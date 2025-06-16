import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfMaker {
    // Paths on the filesystem (relative to working directory)
    private static final String ARABIC_FONT_PATH    = "Resorses/NotoIKEAArabic-Bold.ttf";
    private static final String IMAGE_TEMPLATE_PATH = "Resorses/sickLeave.jpg";

    public void SickLeaveF(String ReportID, String IdNumber, String NameInArabic, String NameInEnglish,
                   String DoctorNameInArabic, String DoctorNameInEnglish, String DoctorSpecialtyInArabic,
                   String DoctorSpecialtyInEnglish, String HospitalNameInArabic, String HospitalNameInEnglish,
                   String EmployerArabic, String DateInGregorianString, String DateEndGregorianString,
                   String NumberOfDays, String LicenseNumber, String ReportDateHijri,
                   String StartOfTheHijriReport, String EndOfHijriReport, String BeginningOfTheADReport,
                   String NationalityInArabic, String NationalityInEnglish, String Time, String Date) {
        
        try (PDDocument document = new PDDocument()) {
            createPDF(document, ReportID, IdNumber, NameInArabic, NameInEnglish, DoctorNameInArabic,
                    DoctorNameInEnglish, DoctorSpecialtyInArabic, DoctorSpecialtyInEnglish,
                    HospitalNameInArabic, HospitalNameInEnglish, EmployerArabic,
                    DateInGregorianString, DateEndGregorianString, NumberOfDays, LicenseNumber,
                    ReportDateHijri, StartOfTheHijriReport, EndOfHijriReport, BeginningOfTheADReport,
                    NationalityInArabic, NationalityInEnglish, Time, Date);
            
            // Save to desktop
            String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
            File outputFile = new File(desktopPath, "Sick Leave "+ReportID+".pdf");
            document.save(outputFile);
            
            showSuccessAlert(outputFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("PDF Creation Failed: " + e.getMessage() + 
                          "\n\nMake sure these files exist:\n" +
                          "  • " + new File(IMAGE_TEMPLATE_PATH).getAbsolutePath() + "\n" +
                          "  • " + new File(ARABIC_FONT_PATH).getAbsolutePath());
        }
    }

    private void showSuccessAlert(String filePath) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("PDF Created Successfully");
            alert.setHeaderText(null);
            alert.setContentText("Sick leave report saved to:\n" + filePath);
            alert.showAndWait();
        });
    }

    private void showErrorAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("PDF Creation Failed");
            alert.setHeaderText("Resource Loading Error");
            alert.setContentText(message);
            alert.setResizable(true);
            alert.getDialogPane().setPrefSize(600, 300);
            alert.showAndWait();
        });
    }

    private void createPDF(PDDocument document, String ReportID, String IdNumber, String NameInArabic,
                          String NameInEnglish, String DoctorNameInArabic, String DoctorNameInEnglish,
                          String DoctorSpecialtyInArabic, String DoctorSpecialtyInEnglish,
                          String HospitalNameInArabic, String HospitalNameInEnglish, String EmployerArabic,
                          String DateInGregorianString, String DateEndGregorianString, String NumberOfDays,
                          String LicenseNumber, String ReportDateHijri, String StartOfTheHijriReport,
                          String EndOfHijriReport, String BeginningOfTheADReport, String NationalityInArabic,
                          String NationalityInEnglish, String Time, String Date) throws Exception {
        
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDRectangle mediaBox = page.getMediaBox();
        float pageWidth = mediaBox.getWidth();
        float pageHeight = mediaBox.getHeight();

        try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
            // 1) Background image directly from file
            PDImageXObject image = PDImageXObject.createFromFile(IMAGE_TEMPLATE_PATH, document);
            cs.drawImage(image, 0, 0, pageWidth, pageHeight);

            // 2) Arabic font from file
            PDFont font = PDType0Font.load(document, new FileInputStream(ARABIC_FONT_PATH));
            float fontSize = 10.5f;
            cs.setFont(font, fontSize);
            cs.setNonStrokingColor(Color.BLACK);

            // build and render your TextPosition list (same as before)…
            List<TextPosition> positions = new ArrayList<>();
            float baseY = 655;
            float[] rowYs = {
                baseY, baseY - 22, baseY - 44, baseY - 66,
                baseY - 88, baseY - 110, baseY - 132, baseY - 154,
                baseY - 176, baseY - 198, baseY - 220
            };
            String durationEnglish = NumberOfDays + " day (" + DateInGregorianString + " to " + DateEndGregorianString + ")";
            String durationArabic = NumberOfDays + " يوم (" + StartOfTheHijriReport + " إلى " + EndOfHijriReport + ")";

            // English column
            positions.add(new TextPosition(55, rowYs[0], ReportID));
            positions.add(new TextPosition(55, rowYs[1], durationEnglish));
            positions.add(new TextPosition(55, rowYs[2], DateInGregorianString));
            positions.add(new TextPosition(55, rowYs[3], DateEndGregorianString));
            positions.add(new TextPosition(55, rowYs[4], BeginningOfTheADReport));
            positions.add(new TextPosition(55, rowYs[5], NameInEnglish));
            positions.add(new TextPosition(55, rowYs[6], IdNumber));
            positions.add(new TextPosition(55, rowYs[7], NationalityInEnglish));
            positions.add(new TextPosition(55, rowYs[8], ""));
            positions.add(new TextPosition(55, rowYs[9], DoctorNameInEnglish));
            positions.add(new TextPosition(55, rowYs[10], DoctorSpecialtyInEnglish));

            // Arabic column (right‑aligned)
            positions.add(new TextPosition(345, rowYs[0], ReportID, true));
            positions.add(new TextPosition(345, rowYs[1], durationArabic, true));
            positions.add(new TextPosition(345, rowYs[2], StartOfTheHijriReport, true));
            positions.add(new TextPosition(345, rowYs[3], EndOfHijriReport, true));
            positions.add(new TextPosition(345, rowYs[4], ReportDateHijri, true));
            positions.add(new TextPosition(345, rowYs[5], NameInArabic, true));
            positions.add(new TextPosition(345, rowYs[6], IdNumber, true));
            positions.add(new TextPosition(345, rowYs[7], NationalityInArabic, true));
            positions.add(new TextPosition(345, rowYs[8], EmployerArabic, true));
            positions.add(new TextPosition(345, rowYs[9], DoctorNameInArabic, true));
            positions.add(new TextPosition(345, rowYs[10], DoctorSpecialtyInArabic, true));

            // Footer
            positions.add(new TextPosition(55, 135, "To check the report please visit Seha's official website"));
            positions.add(new TextPosition(55, 120, "www.seha.sa/#/inquiries/slengujry"));
            positions.add(new TextPosition(400, 80, Time, true));
            positions.add(new TextPosition(400, 65, Date, true));

            for (TextPosition pos : positions) {
                renderText(cs, font, fontSize, pos);
            }
        }
    }

    private void renderText(PDPageContentStream cs, PDFont font, float fontSize, TextPosition pos) 
            throws IOException {
        cs.beginText();
        float textWidth = font.getStringWidth(pos.text) * fontSize / 1000;
        float x = pos.rightAlign ? pos.x - textWidth : pos.x;
        cs.newLineAtOffset(x, pos.y);
        cs.showText(pos.text);
        cs.endText();
    }

    // Helper for positioning
    private static class TextPosition {
        float x, y; String text; boolean rightAlign;
        TextPosition(float x, float y, String text) { this(x,y,text,false); }
        TextPosition(float x, float y, String text, boolean rightAlign) {
            this.x=x; this.y=y; this.text=text; this.rightAlign=rightAlign;
        }
    }
}
