import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {
    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "MySQL JDBC Driver not found!", e);
        }
    }

    public static boolean saveReport(ReportData data) {
        // Get database credentials from user session
        UserSession session = UserSession.getInstance();
        String URL = session.getDbUrl();
        String USER = session.getDbUser();
        String PASSWORD = session.getDbPassword();
        
        String sql = "INSERT INTO reports (" +
            "report_id, name_arabic, name_english, doctor_arabic, doctor_english, " +
            "specialty_arabic, specialty_english, hospital_arabic, hospital_english, " +
            "nationality_arabic, nationality_english, date_gregorian, end_date_gregorian, " +
            "days_count, date_hijri, start_hijri, end_hijri, begin_ad, date_string, " +
            "time_string, id_number, employer_arabic, license_number, user_id" +
            ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, data.reportID());
            stmt.setString(2, data.nameArabic());
            stmt.setString(3, data.nameEnglish());
            stmt.setString(4, data.doctorArabic());
            stmt.setString(5, data.doctorEnglish());
            stmt.setString(6, data.specialtyArabic());
            stmt.setString(7, data.specialtyEnglish());
            stmt.setString(8, data.hospitalArabic());
            stmt.setString(9, data.hospitalEnglish());
            stmt.setString(10, data.nationalityArabic());
            stmt.setString(11, data.nationalityEnglish());
            
            if (data.reportDateGregorian() != null) {
                stmt.setDate(12, Date.valueOf(data.reportDateGregorian()));
            } else {
                stmt.setNull(12, Types.DATE);
            }
            
            if (data.endDateGregorian() != null) {
                stmt.setDate(13, Date.valueOf(data.endDateGregorian()));
            } else {
                stmt.setNull(13, Types.DATE);
            }

            stmt.setInt(14, data.daysCount());
            stmt.setString(15, data.dateHijri());
            stmt.setString(16, data.startHijri());
            stmt.setString(17, data.endHijri());
            stmt.setString(18, data.beginAd());
            stmt.setString(19, data.dateString());
            stmt.setString(20, data.timeString());
            stmt.setString(21, data.idNumber());
            stmt.setString(22, data.employerArabic());
            stmt.setString(23, data.licenseNumber());
            stmt.setInt(24, UserSession.getInstance().getUserId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error during saveReport", e);
            return false;
        }
    }
}