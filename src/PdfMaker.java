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
            // 1) Background image directly from file
            PDImageXObject image = PDImageXObject.createFromFile(IMAGE_TEMPLATE_PATH, document);
            cs.drawImage(image, 0, 0, pageWidth, pageHeight);

            // 2) Add custom image if provided - positioned above "الملك سلمان"
            if (customImagePath != null && !customImagePath.isEmpty()) {
                try {
                    PDImageXObject customImage = PDImageXObject.createFromFile(customImagePath, document);
                    float imageSize = 100; // Same size as QR code
                    
                    // Final position: above "الملك سلمان" text
                    float x = pageWidth - imageSize - 120; // Move left 20 points more
                    float y = 170; // Move up 20 points more
                    
                    cs.drawImage(customImage, x, y, imageSize, imageSize);
                } catch (IOException e) {
                    System.err.println("Error loading custom image: " + e.getMessage());
                }
            }

            // 3) Arabic font from file
            PDFont font = PDType0Font.load(document, new FileInputStream(ARABIC_FONT_PATH));
            float mainFontSize = 9.5f;  // Reduced from 10.5f to 9.5f
            float footerFontSize = 10.5f;  // Keep footer size unchanged

            // Build and render TextPosition list
            List<TextPosition> positions = new ArrayList<>();
            
            // INDIVIDUAL POSITIONING FOR EACH FIELD
            // ===== English Column =====
            // Report ID
            positions.add(new TextPosition(260, 655, ReportID, false));
            
            // Duration
            String durationEnglish = NumberOfDays + " day (" + DateInGregorianString + " to " + DateEndGregorianString + ")";
            positions.add(new TextPosition(180, 633, durationEnglish, false));
            
            // Admission Date
            positions.add(new TextPosition(180, 611, DateInGregorianString, false));
            
            // Discharge Date
            positions.add(new TextPosition(180, 589, DateEndGregorianString, false));
            
            // Issue Date
            positions.add(new TextPosition(180, 567, BeginningOfTheADReport, false));
            
            // Name (English)
            positions.add(new TextPosition(180, 545, NameInEnglish, false));
            
            // ID Number
            positions.add(new TextPosition(180, 523, IdNumber, false));
            
            // Nationality (English)
            positions.add(new TextPosition(180, 501, NationalityInEnglish, false));
            
            // Employer (English) - empty in template
            positions.add(new TextPosition(180, 479, "", false));
            
            // Doctor Name (English)
            positions.add(new TextPosition(180, 457, DoctorNameInEnglish, false));
            
            // Doctor Specialty (English)
            positions.add(new TextPosition(180, 435, DoctorSpecialtyInEnglish, false));
            
            // ===== Arabic Column =====
            // Duration (Arabic)
            String durationArabic = NumberOfDays + " يوم (" + StartOfTheHijriReport + " إلى " + EndOfHijriReport + ")";
            positions.add(new TextPosition(400, 633, processArabicText(durationArabic), true));
            
            // Admission Date (Arabic)
            positions.add(new TextPosition(400, 611, processArabicText(StartOfTheHijriReport), true));
            
            // Discharge Date (Arabic)
            positions.add(new TextPosition(400, 589, processArabicText(EndOfHijriReport), true));
            
            // Issue Date (Arabic)
            positions.add(new TextPosition(400, 567, processArabicText(ReportDateHijri), true));
            
            // Name (Arabic)
            positions.add(new TextPosition(400, 545, processArabicText(NameInArabic), true));
            
            // ID Number
            positions.add(new TextPosition(400, 523, IdNumber, true));
            
            // Nationality (Arabic)
            positions.add(new TextPosition(400, 501, processArabicText(NationalityInArabic), true));
            
            // Employer (Arabic)
            positions.add(new TextPosition(400, 479, processArabicText(EmployerArabic), true));
            
            // Doctor Name (Arabic)
            positions.add(new TextPosition(400, 457, processArabicText(DoctorNameInArabic), true));
            
            // Doctor Specialty (Arabic)
            positions.add(new TextPosition(400, 435, processArabicText(DoctorSpecialtyInArabic), true));

            // ===== WHITE TEXT FIELDS =====
            cs.setNonStrokingColor(WHITE);
            renderText(cs, font, mainFontSize, new TextPosition(180, 611, DateInGregorianString, false));
            renderText(cs, font, mainFontSize, new TextPosition(400, 611, processArabicText(StartOfTheHijriReport), true));

            // ===== DARK BLUE TEXT FIELDS =====
            cs.setNonStrokingColor(DARK_BLUE);
            for (TextPosition pos : positions) {
                renderText(cs, font, mainFontSize, pos);
            }

            // === FOOTER: Time and date in black at left corner ===
            cs.setNonStrokingColor(BLACK);
            
            // Final position: very bottom of page
            TextPosition timePos = new TextPosition(50, 20, Time, false); 
            TextPosition datePos = new TextPosition(50, 5, Date, false); 
            
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