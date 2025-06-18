import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {
    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());
    
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/medical_reports_db";
    private static final String USER = "root";
    private static final String PASSWORD = "@5688120@";

    static {
        try {
            // ensure MySQL driver is loaded
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "MySQL JDBC Driver not found!", e);
        }
    }

    public static boolean saveReport(ReportData data) {
        // list every column explicitly
        String sql =
        "INSERT INTO reports (" +
          "report_id, " +
          "name_arabic, " +
          "name_english, " +
          "doctor_arabic, " +
          "doctor_english, " +
          "specialty_arabic, " +
          "specialty_english, " +
          "hospital_arabic, " +
          "hospital_english, " +
          "nationality_arabic, " +
          "nationality_english, " +
          "date_gregorian, " +
          "end_date_gregorian, " +
          "days_count, " +
          "date_hijri, " +
          "start_hijri, " +
          "end_hijri, " +
          "begin_ad, " +
          "date_string, " +
          "time_string, " +
          "id_number, " +
          "employer_arabic, " +
          "license_number" +
        ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (!conn.isValid(2)) {
                logger.severe("❌ Database connection failed");
                return false;
            }
            logger.info("✅ Database connection established");

            stmt.setString(1,  data.reportID());
            stmt.setString(2,  data.nameArabic());
            stmt.setString(3,  data.nameEnglish());
            stmt.setString(4,  data.doctorArabic());
            stmt.setString(5,  data.doctorEnglish());
            stmt.setString(6,  data.specialtyArabic());
            stmt.setString(7,  data.specialtyEnglish());
            stmt.setString(8,  data.hospitalArabic());
            stmt.setString(9,  data.hospitalEnglish());
            stmt.setString(10, data.nationalityArabic());
            stmt.setString(11, data.nationalityEnglish());
            
            // handle possible null dates properly
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

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("✅ Inserted " + rowsAffected + " row(s) into database");
                return true;
            } else {
                logger.severe("❌ No rows inserted");
                return false;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "❌ Database error during saveReport", e);
            return false;
        }
    }
}
