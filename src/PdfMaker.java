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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PdfMaker {
    // Use relative paths since Resorses is at project root
    private static final String ARABIC_FONT_PATH = "Resorses/NotoIKEAArabic-Bold.ttf";
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
                   String NationalityInArabic, String NationalityInEnglish, String Time, String Date,
                   String customImagePath) {
        
        try (PDDocument document = new PDDocument()) {
            createPDF(document, ReportID, IdNumber, NameInArabic, NameInEnglish, DoctorNameInArabic,
                    DoctorNameInEnglish, DoctorSpecialtyInArabic, DoctorSpecialtyInEnglish,
                    HospitalNameInArabic, HospitalNameInEnglish, EmployerArabic,
                    DateInGregorianString, DateEndGregorianString, NumberOfDays, LicenseNumber,
                    ReportDateHijri, StartOfTheHijriReport, EndOfHijriReport, BeginningOfTheADReport,
                    NationalityInArabic, NationalityInEnglish, Time, Date, customImagePath);
            
            // Save to desktop
            String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
            File outputFile = new File(desktopPath, "Sick Leave "+ReportID+".pdf");
            document.save(outputFile);
            
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("تم إنشاء PDF بنجاح");
                alert.setHeaderText(null);
                alert.setContentText("تم حفظ تقرير الإجازة المرضية في:\n" + outputFile.getAbsolutePath());
                alert.showAndWait();
            });
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("فشل في إنشاء PDF");
                alert.setHeaderText("خطأ في إنشاء الملف");
                alert.setContentText("فشل في إنشاء ملف PDF: " + e.getMessage() + 
                                  "\n\nتأكد من وجود الملفات:\n" +
                                  "  • " + new File(IMAGE_TEMPLATE_PATH).getAbsolutePath() + "\n" +
                                  "  • " + new File(ARABIC_FONT_PATH).getAbsolutePath());
                alert.setResizable(true);
                alert.getDialogPane().setPrefSize(600, 300);
                alert.showAndWait();
            });
        }
    }

    private void createPDF(PDDocument document, String ReportID, String IdNumber, String NameInArabic,
                          String NameInEnglish, String DoctorNameInArabic, String DoctorNameInEnglish,
                          String DoctorSpecialtyInArabic, String DoctorSpecialtyInEnglish,
                          String HospitalNameInArabic, String HospitalNameInEnglish, String EmployerArabic,
                          String DateInGregorianString, String DateEndGregorianString, String NumberOfDays,
                          String LicenseNumber, String ReportDateHijri, String StartOfTheHijriReport,
                          String EndOfHijriReport, String BeginningOfTheADReport, String NationalityInArabic,
                          String NationalityInEnglish, String Time, String Date, String customImagePath) throws Exception {
        
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDRectangle mediaBox = page.getMediaBox();
        float pageWidth = mediaBox.getWidth();
        float pageHeight = mediaBox.getHeight();

        try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
            // 1) Load background image from file system
            Path imagePath = Paths.get(IMAGE_TEMPLATE_PATH);
            if (!Files.exists(imagePath)) {
                throw new IOException("Background image not found: " + imagePath.toAbsolutePath());
            }
            PDImageXObject image = PDImageXObject.createFromFile(IMAGE_TEMPLATE_PATH, document);
            cs.drawImage(image, 0, 0, pageWidth, pageHeight);

            // 2) Add custom image if provided
            if (customImagePath != null && !customImagePath.isEmpty()) {
                try {
                    PDImageXObject customImage = PDImageXObject.createFromFile(customImagePath, document);
                    float imageSize = 85;
                    float x = pageWidth - imageSize - 105;
                    float y = 220;
                    cs.drawImage(customImage, x, y, imageSize, imageSize);
                } catch (IOException e) {
                    System.err.println("Error loading custom image: " + e.getMessage());
                }
            }

            // 3) Load Arabic font from file system
            Path fontPath = Paths.get(ARABIC_FONT_PATH);
            if (!Files.exists(fontPath)) {
                throw new IOException("Font file not found: " + fontPath.toAbsolutePath());
            }
            PDFont font = PDType0Font.load(document, new File(ARABIC_FONT_PATH));
            
            float mainFontSize = 9.5f;
            float footerFontSize = 10.5f;

            // Build and render TextPosition list
            List<TextPosition> positions = new ArrayList<>();
            
            // INDIVIDUAL POSITIONING FOR EACH FIELD
            // ===== English Column =====
            positions.add(new TextPosition(260, 655, ReportID, false));
            
            // Admission Date - REMOVED FROM LIST (will be drawn separately)
            positions.add(new TextPosition(180, 590, BeginningOfTheADReport, false));//588
            positions.add(new TextPosition(180, 565, DateEndGregorianString, false));//565
            positions.add(new TextPosition(280, 533, BeginningOfTheADReport, false));
            positions.add(new TextPosition(180, 508, NameInEnglish, false));
            positions.add(new TextPosition(300, 477, IdNumber, false));
            positions.add(new TextPosition(180, 440, NationalityInEnglish, false));
            positions.add(new TextPosition(180, 408, "", false));
            positions.add(new TextPosition(180, 370, DoctorNameInEnglish, false));
            positions.add(new TextPosition(180, 330, DoctorSpecialtyInEnglish, false));
            
            // ===== Arabic Column =====
            // Admission Date - REMOVED FROM LIST (will be drawn separately)
            positions.add(new TextPosition(400, 590, processArabicText(ReportDateHijri), true));//588
            positions.add(new TextPosition(400, 565, processArabicText(EndOfHijriReport), true));//565
            positions.add(new TextPosition(400, 508, processArabicText(NameInArabic), true));
            //positions.add(new TextPosition(400, 523, IdNumber, true));
            positions.add(new TextPosition(400, 440, processArabicText(NationalityInArabic), true));
            positions.add(new TextPosition(400, 408, processArabicText(EmployerArabic), true));
            positions.add(new TextPosition(400, 370, processArabicText(DoctorNameInArabic), true));
            positions.add(new TextPosition(400, 330, processArabicText(DoctorSpecialtyInArabic), true));

            // ===== WHITE TEXT FIELDS =====
            cs.setNonStrokingColor(WHITE);
            // Render Admission Dates
            renderText(cs, font, mainFontSize, new TextPosition(180, 611, DateInGregorianString, false));
            renderText(cs, font, mainFontSize, new TextPosition(400, 611, processArabicText(StartOfTheHijriReport), true));
            
            // Render Duration Strings in WHITE
            String durationEnglish = NumberOfDays + " day (" + DateInGregorianString + " to " + DateEndGregorianString + ")";
            renderText(cs, font, mainFontSize, new TextPosition(158, 633, durationEnglish, false));
            
            String durationArabic = NumberOfDays + " يوم (" + StartOfTheHijriReport + " إلى " + EndOfHijriReport + ")";
            renderText(cs, font, mainFontSize, new TextPosition(430, 633, processArabicText(durationArabic), true));

            // ===== DARK BLUE TEXT FIELDS =====
            cs.setNonStrokingColor(DARK_BLUE);
            for (TextPosition pos : positions) {
                renderText(cs, font, mainFontSize, pos);
            }

            // === FOOTER: Time and date in black at left corner ===
            cs.setNonStrokingColor(BLACK);
            TextPosition timePos = new TextPosition(50, 115, Time, false); 
            TextPosition datePos = new TextPosition(50, 100, Date, false); 
            renderText(cs, font, footerFontSize, timePos);
            renderText(cs, font, footerFontSize, datePos);
        }
    }

    // Process Arabic text using ICU4J for proper shaping and direction
    private String processArabicText(String text) {
        try {
            ArabicShaping shaper = new ArabicShaping(ArabicShaping.LETTERS_SHAPE);
            String shapedText = shaper.shape(text);
            Bidi bidi = new Bidi(shapedText, Bidi.DIRECTION_RIGHT_TO_LEFT);
            bidi.setReorderingMode(Bidi.REORDER_DEFAULT);
            return bidi.writeReordered(Bidi.DO_MIRRORING);
        } catch (ArabicShapingException e) {
            e.printStackTrace();
            return text;
        }
    }

    private void renderText(PDPageContentStream cs, PDFont font, float fontSize, TextPosition pos) 
            throws IOException {
        cs.beginText();
        float textWidth = font.getStringWidth(pos.text) * fontSize / 1000;
        float x = pos.rightAlign ? pos.x - textWidth : pos.x;
        cs.setFont(font, fontSize);
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