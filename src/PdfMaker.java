import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
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
    
    // Define colors
    private static final Color DARK_BLUE = new Color(0, 0, 128); // Dark blue color
    private static final Color WHITE = new Color(255, 255, 255); // White color
    private static final Color BLACK = new Color(0, 0, 0);       // Black color

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
            float mainFontSize = 9.5f;  // Reduced from 10.5f to 9.5f
            float footerFontSize = 10.5f;  // Keep footer size unchanged

            // Build and render TextPosition list
            List<TextPosition> positions = new ArrayList<>();
            float baseY = 655;
            float[] rowYs = {
                baseY, baseY - 22, baseY - 44, baseY - 66,
                baseY - 88, baseY - 110, baseY - 132, baseY - 154,
                baseY - 176, baseY - 198, baseY - 220
            };
            
            // Format duration strings
            String durationEnglish = NumberOfDays + " day (" + DateInGregorianString + " to " + DateEndGregorianString + ")";
            String durationArabic = NumberOfDays + " يوم (" + StartOfTheHijriReport + " إلى " + EndOfHijriReport + ")";

            // Set color to DARK_BLUE for all main content
            cs.setFont(font, mainFontSize);

            // === WHITE CONTENT: First date fields ===
            cs.setNonStrokingColor(WHITE);
            
            // English column (white)
            renderText(cs, font, mainFontSize, new TextPosition(180, rowYs[1], durationEnglish, false));
            renderText(cs, font, mainFontSize, new TextPosition(180, rowYs[2], DateInGregorianString, false));
            
            // Arabic column (white)
            renderText(cs, font, mainFontSize, new TextPosition(400, rowYs[1], processArabicText(durationArabic), true));
            renderText(cs, font, mainFontSize, new TextPosition(400, rowYs[2], processArabicText(StartOfTheHijriReport), true));
            
            // === DARK BLUE CONTENT: Rest of the fields ===
            cs.setNonStrokingColor(DARK_BLUE);
            
            // English column (dark blue)
            positions.add(new TextPosition(260, rowYs[0], ReportID, false));
            positions.add(new TextPosition(180, rowYs[3], DateEndGregorianString, false));
            positions.add(new TextPosition(180, rowYs[4], BeginningOfTheADReport, false));
            positions.add(new TextPosition(180, rowYs[5], NameInEnglish, false));
            positions.add(new TextPosition(180, rowYs[6], IdNumber, false));
            positions.add(new TextPosition(180, rowYs[7], NationalityInEnglish, false));
            positions.add(new TextPosition(180, rowYs[8], "", false));
            positions.add(new TextPosition(180, rowYs[9], DoctorNameInEnglish, false));
            positions.add(new TextPosition(180, rowYs[10], DoctorSpecialtyInEnglish, false));

            // Arabic column (dark blue)
            positions.add(new TextPosition(400, rowYs[3], processArabicText(EndOfHijriReport), true));
            positions.add(new TextPosition(400, rowYs[4], processArabicText(ReportDateHijri), true));
            positions.add(new TextPosition(400, rowYs[5], processArabicText(NameInArabic), true));
            positions.add(new TextPosition(400, rowYs[6], IdNumber, true)); // Numbers don't need processing
            positions.add(new TextPosition(400, rowYs[7], processArabicText(NationalityInArabic), true));
            positions.add(new TextPosition(400, rowYs[8], processArabicText(EmployerArabic), true));
            positions.add(new TextPosition(400, rowYs[9], processArabicText(DoctorNameInArabic), true));
            positions.add(new TextPosition(400, rowYs[10], processArabicText(DoctorSpecialtyInArabic), true));

            // Render main content in dark blue
            for (TextPosition pos : positions) {
                renderText(cs, font, mainFontSize, pos);
            }

            // === FOOTER: Time and date in black at left corner ===
            cs.setNonStrokingColor(BLACK);
            
            // Footer time/date (left-aligned)
            TextPosition timePos = new TextPosition(50, 80, Time, false);
            TextPosition datePos = new TextPosition(50, 65, Date, false);
            
            // Use larger font size for footer
            renderText(cs, font, footerFontSize, timePos);
            renderText(cs, font, footerFontSize, datePos);
        }
    }

    // Process Arabic text using ICU4J for proper shaping and direction
    private String processArabicText(String text) {
        try {
            // Step 1: Shape Arabic letters for proper connection
            ArabicShaping shaper = new ArabicShaping(ArabicShaping.LETTERS_SHAPE);
            String shapedText = shaper.shape(text);
            
            // Step 2: Handle bidirectional text for proper RTL rendering
            Bidi bidi = new Bidi(shapedText, Bidi.DIRECTION_RIGHT_TO_LEFT);
            bidi.setReorderingMode(Bidi.REORDER_DEFAULT);
            
            // Create visual ordered string
            return bidi.writeReordered(Bidi.DO_MIRRORING);
        } catch (ArabicShapingException e) {
            e.printStackTrace();
            return text; // Return original if shaping fails
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
        float x, y; 
        String text;
        boolean rightAlign;
        
        TextPosition(float x, float y, String text, boolean rightAlign) {
            this.x = x;
            this.y = y;
            this.text = text;
            this.rightAlign = rightAlign;
        }
    }
}