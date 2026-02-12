package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Handles inventory-related database actions such as adding/removing stock.
 */
public class InventoryDAO
{

    /**
     * Adds stock for a given item name in the selected restaurant.
     *
     * @param restaurantId The restaurant currently selected (1 or 2).
     * @param itemName     The item name the user typed (must exist in item table).
     * @param amount       How much to add (must be > 0).
     * @return true if the update succeeded; false otherwise.
     */
    public boolean addStock(int restaurantId, String itemName, int amount)
    {
        // Validate amount
        if (amount <= 0) return false;

        // Steps:
        // 1) Find item_id by item_name
        // 2) Update inventory.quantity_on_hand += amount
        // 3) Insert a stock_transaction row
        String findItemSql = "SELECT item_id FROM item WHERE item_name = ? AND is_active = TRUE LIMIT 1";
        String updateSql = "UPDATE inventory SET quantity_on_hand = quantity_on_hand + ? WHERE restaurant_id = ? AND item_id = ?";
        String insertTxSql = "INSERT INTO stock_transaction (restaurant_id, item_id, change_amount, transaction_type, note) VALUES (?, ?, ?, 'ADD', ?)";

        try (Connection conn = DatabaseConnection.getConnection())
        {

            // 1) Find item_id
            Integer itemId = null;
            try (PreparedStatement findStmt = conn.prepareStatement(findItemSql))
            {
                findStmt.setString(1, itemName);

                try (ResultSet rs = findStmt.executeQuery())
                {
                    if (rs.next()) itemId = rs.getInt("item_id");
                }
            }

            // If item doesn't exist, stop
            if (itemId == null) return false;

            // 2) Update inventory
            int rowsUpdated;
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql))
            {
                updateStmt.setInt(1, amount);
                updateStmt.setInt(2, restaurantId);
                updateStmt.setInt(3, itemId);
                rowsUpdated = updateStmt.executeUpdate();
            }

            // If inventory row doesn't exist, stop
            if (rowsUpdated == 0) return false;

            // 3) Log transaction (optional but recommended)
            try (PreparedStatement txStmt = conn.prepareStatement(insertTxSql))
            {
                txStmt.setInt(1, restaurantId);
                txStmt.setInt(2, itemId);
                txStmt.setInt(3, amount);
                txStmt.setString(4, "Added via Stock Adjustment screen");
                txStmt.executeUpdate();
            }

            return true;

        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Removes stock for a given item name in the selected restaurant.
     * Prevents stock going below 0.
     */
    public boolean removeStock(int restaurantId, String itemName, int amount)
    {
        // Validate amount
        if (amount <= 0) return false;

        String findItemSql = "SELECT item_id FROM item WHERE item_name = ? AND is_active = TRUE LIMIT 1";
        String checkSql = "SELECT quantity_on_hand FROM inventory WHERE restaurant_id = ? AND item_id = ? LIMIT 1";
        String updateSql = "UPDATE inventory SET quantity_on_hand = quantity_on_hand - ? WHERE restaurant_id = ? AND item_id = ?";
        String insertTxSql = "INSERT INTO stock_transaction (restaurant_id, item_id, change_amount, transaction_type, note) VALUES (?, ?, ?, 'REMOVE', ?)";

        try (Connection conn = DatabaseConnection.getConnection())
        {

            // 1) Find item_id
            Integer itemId = null;
            try (PreparedStatement findStmt = conn.prepareStatement(findItemSql))
            {
                findStmt.setString(1, itemName);

                try (ResultSet rs = findStmt.executeQuery())
                {
                    if (rs.next()) itemId = rs.getInt("item_id");
                }
            }

            if (itemId == null) return false;

            // 2) Check current stock (avoid negative)
            int currentQty;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql))
            {
                checkStmt.setInt(1, restaurantId);
                checkStmt.setInt(2, itemId);

                try (ResultSet rs = checkStmt.executeQuery())
                {
                    if (!rs.next()) return false; // no inventory row
                    currentQty = rs.getInt("quantity_on_hand");
                }
            }

            // If removal would make negative, not allow
            if (currentQty - amount < 0) return false;

            // 3) Update inventory
            int rowsUpdated;
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql))
            {
                updateStmt.setInt(1, amount);
                updateStmt.setInt(2, restaurantId);
                updateStmt.setInt(3, itemId);
                rowsUpdated = updateStmt.executeUpdate();
            }

            if (rowsUpdated == 0) return false;

            // 4) Log transaction (store negative change for clarity in reports)
            try (PreparedStatement txStmt = conn.prepareStatement(insertTxSql))
            {
                txStmt.setInt(1, restaurantId);
                txStmt.setInt(2, itemId);
                txStmt.setInt(3, -amount);
                txStmt.setString(4, "Removed via Stock Adjustment screen");
                txStmt.executeUpdate();
            }

            return true;

        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
}