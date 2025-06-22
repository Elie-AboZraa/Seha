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

public class CompanionPdfMaker {
    private static final String ARABIC_FONT_PATH = "Resorses/NotoIKEAArabic-Bold.ttf";
    private static final String IMAGE_TEMPLATE_PATH = "Resorses/sickLeaveForPartner.jpg";
    private static final Color DARK_BLUE = new Color(0, 0, 128);
    private static final Color WHITE = new Color(255, 255, 255);
    private static final Color BLACK = new Color(0, 0, 0);

    public void CompanionReport(String ReportID, String IdNumber, String NameInArabic, String NameInEnglish,
                   String DoctorNameInArabic, String DoctorNameInEnglish, String DoctorSpecialtyInArabic,
                   String DoctorSpecialtyInEnglish, String HospitalNameInArabic, String HospitalNameInEnglish,
                   String EmployerArabic, String DateInGregorianString, String DateEndGregorianString,
                   String NumberOfDays, String LicenseNumber, String ReportDateHijri,
                   String StartOfTheHijriReport, String EndOfHijriReport, String BeginningOfTheADReport,
                   String NationalityInArabic, String NationalityInEnglish, String Time, String Date,
                   String kinshipArabic, String kinshipEnglish, String customImagePath) {
        
        try (PDDocument document = new PDDocument()) {
            createPDF(document, ReportID, IdNumber, NameInArabic, NameInEnglish, DoctorNameInArabic,
                    DoctorNameInEnglish, DoctorSpecialtyInArabic, DoctorSpecialtyInEnglish,
                    HospitalNameInArabic, HospitalNameInEnglish, EmployerArabic,
                    DateInGregorianString, DateEndGregorianString, NumberOfDays, LicenseNumber,
                    ReportDateHijri, StartOfTheHijriReport, EndOfHijriReport, BeginningOfTheADReport,
                    NationalityInArabic, NationalityInEnglish, Time, Date, 
                    kinshipArabic, kinshipEnglish, customImagePath);
            
            String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
            File outputFile = new File(desktopPath, "Patient Companion "+ReportID+".pdf");
            document.save(outputFile);
            
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("PDF Created");
                alert.setHeaderText(null);
                alert.setContentText("Companion report saved to:\n" + outputFile.getAbsolutePath());
                alert.showAndWait();
            });
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("PDF Creation Failed");
                alert.setHeaderText("Error Details");
                alert.setContentText("Failed to create PDF: " + e.getMessage());
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
                          String NationalityInEnglish, String Time, String Date, 
                          String kinshipArabic, String kinshipEnglish, String customImagePath) throws Exception {
        
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDRectangle mediaBox = page.getMediaBox();
        float pageWidth = mediaBox.getWidth();
        float pageHeight = mediaBox.getHeight();

        try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
            Path imagePath = Paths.get(IMAGE_TEMPLATE_PATH);
            if (!Files.exists(imagePath)) {
                throw new IOException("Background image not found: " + imagePath.toAbsolutePath());
            }
            PDImageXObject image = PDImageXObject.createFromFile(IMAGE_TEMPLATE_PATH, document);
            cs.drawImage(image, 0, 0, pageWidth, pageHeight);

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

            Path fontPath = Paths.get(ARABIC_FONT_PATH);
            if (!Files.exists(fontPath)) {
                throw new IOException("Font file not found: " + fontPath.toAbsolutePath());
            }
            PDFont font = PDType0Font.load(document, new File(ARABIC_FONT_PATH));
            
            float mainFontSize = 9.5f;
            float footerFontSize = 10.5f;

            List<TextPosition> positions = new ArrayList<>();
            
            positions.add(new TextPosition(260, 655, ReportID, false));
            positions.add(new TextPosition(180, 595, BeginningOfTheADReport, false));
            positions.add(new TextPosition(180, 565, DateEndGregorianString, false));
            positions.add(new TextPosition(280, 533, BeginningOfTheADReport, false));
            positions.add(new TextPosition(180, 508, NameInEnglish, false));
            positions.add(new TextPosition(300, 477, IdNumber, false));
            positions.add(new TextPosition(180, 440, NationalityInEnglish, false));
            positions.add(new TextPosition(180, 408, kinshipEnglish, false));
            positions.add(new TextPosition(180, 370, DoctorNameInEnglish, false));
            positions.add(new TextPosition(180, 330, DoctorSpecialtyInEnglish, false));
            
            positions.add(new TextPosition(400, 595, processArabicText(ReportDateHijri), true));
            positions.add(new TextPosition(400, 565, processArabicText(EndOfHijriReport), true));
            positions.add(new TextPosition(400, 508, processArabicText(NameInArabic), true));
            positions.add(new TextPosition(400, 440, processArabicText(NationalityInArabic), true));
            positions.add(new TextPosition(400, 408, processArabicText(kinshipArabic), true));
            positions.add(new TextPosition(400, 370, processArabicText(DoctorNameInArabic), true));
            positions.add(new TextPosition(400, 330, processArabicText(DoctorSpecialtyInArabic), true));

            cs.setNonStrokingColor(WHITE);
            renderText(cs, font, mainFontSize, new TextPosition(180, 611, "", false));
            renderText(cs, font, mainFontSize, new TextPosition(400, 611, "", true));
            
            String durationEnglish = NumberOfDays + " day (" + DateInGregorianString + " to " + DateEndGregorianString + ")";
            renderText(cs, font, 8f, new TextPosition(158, 628, durationEnglish, false));

            String durationArabic = NumberOfDays + " يوم (" + StartOfTheHijriReport + " إلى " + EndOfHijriReport + ")";
            renderText(cs, font, 8f, new TextPosition(445, 628, processArabicText(durationArabic), true));

            cs.setNonStrokingColor(DARK_BLUE);
            for (TextPosition pos : positions) {
                renderText(cs, font, mainFontSize, pos);
            }

            cs.setNonStrokingColor(BLACK);
            TextPosition timePos = new TextPosition(50, 115, Time, false); 
            TextPosition datePos = new TextPosition(50, 100, Date, false); 
            renderText(cs, font, footerFontSize, timePos);
            renderText(cs, font, footerFontSize, datePos);
        }
    }

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