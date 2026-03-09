package DAO;

// UserDAO.java
// Handles database operations related to user accounts.
// Currently used only for login authentication.

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    // Checks whether the given username and password match an active account
    // in the app_user table.
    //
    // A PreparedStatement is used with placeholders (?) instead of putting
    // values directly into the SQL string. This prevents SQL injection attacks.
    //
    // Returns true if a matching active user is found, false otherwise.
    public boolean authenticate(String username, String password) {

        // SELECT 1 just checks if a matching row exists; we do not need to read any columns.
        // is_active = TRUE means deactivated accounts cannot log in.
        // LIMIT 1 stops scanning after the first match is found.
        String sql =
                "SELECT 1 " +
                        "FROM app_user " +
                        "WHERE username = ? " +
                        "  AND password_hash = ? " +
                        "  AND is_active = TRUE " +
                        "LIMIT 1";

        // try-with-resources automatically closes the Connection,
        // PreparedStatement, and ResultSet when the block finishes
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            // Bind the values typed by the user to the query placeholders
            stmt.setString(1, username);
            stmt.setString(2, password);

            // rs.next() returns true if at least one matching row was found
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            // Print the error so it is visible in the console if something goes wrong
            e.printStackTrace();
            return false;
        }
    }
}
