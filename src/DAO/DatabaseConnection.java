package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection
{

    // JDBC connection URL pointing to the MySQL database
    private static final String URL =
            "jdbc:mysql://localhost:3306/RESTAURANT_INVENTORY?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    // Database credentials (replace with your real username and password)
    private static final String USER = "root";
    private static final String PASSWORD = "rootroot";

    // Creates and returns a connection to the MySQL database, throws SQLException if the connection fails.
    public static Connection getConnection() throws SQLException
    {
        try
        {
            // Ensures the MySQL JDBC driver is loaded
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e)
        {
            throw new SQLException("MySQL JDBC Driver not found.", e);
        }

        // Opens and returns the database connection
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Tests whether a database connection can be successfully established. Returns true if the connection opens correctly, otherwise false.
    public static boolean testConnection()
    {
        try (Connection conn = getConnection())
        {
            return conn != null && !conn.isClosed();
        } catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }
}

