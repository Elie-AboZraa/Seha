import java.time.LocalDate;

public class ReportData {
    private final String reportID;
    private final String nameArabic;
    private final String nameEnglish;
    private final String doctorArabic;
    private final String doctorEnglish;
    private final String specialtyArabic;
    private final String specialtyEnglish;
    private final String hospitalArabic;
    private final String hospitalEnglish;
    private final String nationalityArabic;
    private final String nationalityEnglish;
    private final LocalDate reportDateGregorian;
    private final LocalDate endDateGregorian;
    private final int daysCount;
    private final String dateHijri;
    private final String startHijri;
    private final String endHijri;
    private final String beginAd;
    private final String dateString;
    private final String timeString;
    private final String idNumber;
    private final String employerArabic;
    private final String licenseNumber;

    public ReportData(
        String reportID,
        String nameArabic,
        String nameEnglish,
        String doctorArabic,
        String doctorEnglish,
        String specialtyArabic,
        String specialtyEnglish,
        String hospitalArabic,
        String hospitalEnglish,
        String nationalityArabic,
        String nationalityEnglish,
        LocalDate reportDateGregorian,
        LocalDate endDateGregorian,
        int daysCount,
        String dateHijri,
        String startHijri,
        String endHijri,
        String beginAd,
        String dateString,
        String timeString,
        String idNumber,
        String employerArabic,
        String licenseNumber
    ) {
        this.reportID = reportID;
        this.nameArabic = nameArabic;
        this.nameEnglish = nameEnglish;
        this.doctorArabic = doctorArabic;
        this.doctorEnglish = doctorEnglish;
        this.specialtyArabic = specialtyArabic;
        this.specialtyEnglish = specialtyEnglish;
        this.hospitalArabic = hospitalArabic;
        this.hospitalEnglish = hospitalEnglish;
        this.nationalityArabic = nationalityArabic;
        this.nationalityEnglish = nationalityEnglish;
        this.reportDateGregorian = reportDateGregorian;
        this.endDateGregorian = endDateGregorian;
        this.daysCount = daysCount;
        this.dateHijri = dateHijri;
        this.startHijri = startHijri;
        this.endHijri = endHijri;
        this.beginAd = beginAd;
        this.dateString = dateString;
        this.timeString = timeString;
        this.idNumber = idNumber;
        this.employerArabic = employerArabic;
        this.licenseNumber = licenseNumber;
    }

    // Getters
    public String reportID() { return reportID; }
    public String nameArabic() { return nameArabic; }
    public String nameEnglish() { return nameEnglish; }
    public String doctorArabic() { return doctorArabic; }
    public String doctorEnglish() { return doctorEnglish; }
    public String specialtyArabic() { return specialtyArabic; }
    public String specialtyEnglish() { return specialtyEnglish; }
    public String hospitalArabic() { return hospitalArabic; }
    public String hospitalEnglish() { return hospitalEnglish; }
    public String nationalityArabic() { return nationalityArabic; }
    public String nationalityEnglish() { return nationalityEnglish; }
    public LocalDate reportDateGregorian() { return reportDateGregorian; }
    public LocalDate endDateGregorian() { return endDateGregorian; }
    public int daysCount() { return daysCount; }
    public String dateHijri() { return dateHijri; }
    public String startHijri() { return startHijri; }
    public String endHijri() { return endHijri; }
    public String beginAd() { return beginAd; }
    public String dateString() { return dateString; }
    public String timeString() { return timeString; }
    public String idNumber() { return idNumber; }
    public String employerArabic() { return employerArabic; }
    public String licenseNumber() { return licenseNumber; }
}