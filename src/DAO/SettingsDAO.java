package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Handles settings-related database actions (ex: low stock threshold overrides).
 */
public class SettingsDAO
{

    /**
     * Sets a low stock threshold override for a specific restaurant + item.
     * If the inventory row does not exist yet, it creates it automatically.
     */
    public boolean setLowStockThresholdOverride(int restaurantId, String itemName, int newThreshold) {

        // Validate inputs
        if (restaurantId <= 0) return false;
        if (itemName == null || itemName.trim().isEmpty()) return false;
        if (newThreshold <= 0) return false;

        String findItemSql =
                "SELECT item_id FROM item WHERE item_name = ? AND is_active = TRUE LIMIT 1";

        String updateInventorySql =
                "UPDATE inventory " +
                        "SET min_quantity_override = ? " +
                        "WHERE restaurant_id = ? AND item_id = ?";

        String insertInventorySql =
                "INSERT INTO inventory (restaurant_id, item_id, quantity_on_hand, quantity_reserved, min_quantity_override) " +
                        "VALUES (?, ?, 0, 0, ?)";

        try (Connection conn = DatabaseConnection.getConnection())
        {

            // 1) Find the item_id from the item name
            Integer itemId = null;
            try (PreparedStatement findStmt = conn.prepareStatement(findItemSql))
            {
                findStmt.setString(1, itemName.trim());

                try (ResultSet rs = findStmt.executeQuery()) {
                    if (rs.next()) itemId = rs.getInt("item_id");
                }
            }

            // If item doesn't exist, stop
            if (itemId == null) return false;

            // 2) Try to update the inventory row (if it exists)
            int updatedRows;
            try (PreparedStatement updateStmt = conn.prepareStatement(updateInventorySql))
            {
                updateStmt.setInt(1, newThreshold);
                updateStmt.setInt(2, restaurantId);
                updateStmt.setInt(3, itemId);
                updatedRows = updateStmt.executeUpdate();
            }

            // 3) If no row was updated, it means inventory row doesn't exist -> insert it
            if (updatedRows == 0) {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertInventorySql))
                {
                    insertStmt.setInt(1, restaurantId);
                    insertStmt.setInt(2, itemId);
                    insertStmt.setInt(3, newThreshold);
                    insertStmt.executeUpdate();
                }
            }

            return true;

        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
}