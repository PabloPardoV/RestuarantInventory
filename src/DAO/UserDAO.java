package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Handles database operations related to users (authentication).

public class UserDAO
{
    /**
     * Checks whether the given username/password match an active user in the database.
     *
     * @param username The username entered by the user.
     * @param password The password entered by the user.
     * @return true if the credentials are valid and the user is active; otherwise false.
     */
    public boolean authenticate(String username, String password)
    {

        // SQL query:
        // - Uses placeholders (?) so we can safely insert values using PreparedStatement
        // - Filters only active users
        // - LIMIT 1 because we only care if a match exists
        String sql =
                "SELECT 1 " +
                        "FROM app_user " +
                        "WHERE username = ? " +
                        "  AND password_hash = ? " +
                        "  AND is_active = TRUE " +
                        "LIMIT 1";

        // try-with-resources automatically closes Connection / PreparedStatement / ResultSet
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {

            // Fill the placeholders with the values provided by the user
            stmt.setString(1, username);
            stmt.setString(2, password);

            // Execute the query and check if at least one row was found
            try (ResultSet rs = stmt.executeQuery())
            {
                return rs.next();
            }

        } catch (Exception e)
        {
            // Print stack trace so you can see the exact reason if something fails
            e.printStackTrace();
            return false;
        }
    }
}
