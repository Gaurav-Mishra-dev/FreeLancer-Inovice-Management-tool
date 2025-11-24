class DBConnection {
    // TODO: CHANGE THESE VALUES FOR YOUR MYSQL
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/freelancer_db";
    private static final String DB_USER = "root";       // your username
    private static final String DB_PASS = "123456";       // your password

    private static Connection connection;

    // synchronized ensures thread-safe (used for multithreading mark)
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        }
        return connection;
    }
}
