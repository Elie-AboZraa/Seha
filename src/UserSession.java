public class UserSession {
    private static UserSession instance;
    private int userId;
    private String username;
    private int searchIdStart;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    private UserSession(int userId, String username, int searchIdStart, 
                       String dbUrl, String dbUser, String dbPassword) {
        this.userId = userId;
        this.username = username;
        this.searchIdStart = searchIdStart;
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    public static void setCurrentUser(int userId, String username, int searchIdStart, 
                                     String dbUrl, String dbUser, String dbPassword) {
        instance = new UserSession(userId, username, searchIdStart, dbUrl, dbUser, dbPassword);
    }

    public static UserSession getInstance() {
        return instance;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public int getSearchIdStart() {
        return searchIdStart;
    }
    
    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }
    
    public static void clearSession() {
        instance = null;
    }
}