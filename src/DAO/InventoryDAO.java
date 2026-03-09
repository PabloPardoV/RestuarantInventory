package DAO;

// InventoryDAO.java
// Handles all inventory-related database operations including
// reading stock levels, adding and removing stock, editing item
// details, and checking for low-stock items.

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

    // Fetches inventory rows for the selected restaurant.
    // Supports optional filtering by item name (partial match) and category.
    //
    // Pass an empty string for search to get all items with no name filter.
    // Pass "All" for category to skip the category filter.
    //
    // Returns a list of Object arrays, one per row.
    // Each array contains: [itemName, category, quantity, unit, cost, threshold]
    public List<Object[]> getInventory(int restaurantId, String search, String category) {

        List<Object[]> results = new ArrayList<>();

        // This query joins four tables to get all the human-readable values at once.
        // COALESCE checks for a per-restaurant threshold override first, then falls
        // back to the global threshold stored on the item itself.
        // The LIKE clause on item_name lets us do a partial name search.
        String sql =
                "SELECT i.item_name, " +
                        "       c.category_name, " +
                        "       inv.quantity_on_hand, " +
                        "       u.unit_abbreviation, " +
                        "       i.cost, " +
                        "       COALESCE(inv.min_quantity_override, i.low_stock_threshold) AS threshold " +
                        "FROM inventory inv " +
                        "JOIN item     i ON inv.item_id     = i.item_id " +
                        "JOIN category c ON i.category_id   = c.category_id " +
                        "JOIN unit     u ON i.unit_id        = u.unit_id " +
                        "WHERE inv.restaurant_id = ? " +
                        "  AND i.is_active = TRUE " +
                        // LOWER() on both sides makes the name search case-insensitive
                        "  AND (? = '' OR LOWER(i.item_name) LIKE LOWER(CONCAT('%', ?, '%'))) " +
                        // Skip the category filter if "All" is passed
                        "  AND (? = 'All' OR c.category_name = ?) " +
                        "ORDER BY c.category_name, i.item_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, restaurantId);
            stmt.setString(2, search);       // used in the empty string check
            stmt.setString(3, search);       // used in the LIKE pattern
            stmt.setString(4, category);     // used in the "All" check
            stmt.setString(5, category);     // used in the exact category match

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Pack all six columns into one array per row
                    results.add(new Object[]{
                            rs.getString("item_name"),
                            rs.getString("category_name"),
                            rs.getInt("quantity_on_hand"),
                            rs.getString("unit_abbreviation"),
                            rs.getDouble("cost"),
                            rs.getInt("threshold")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    // Returns only the inventory rows where the current quantity is at or below
    // the effective minimum threshold for the given restaurant.
    // Used to build the Low-Stock Report.
    public List<Object[]> getLowStockItems(int restaurantId) {

        List<Object[]> results = new ArrayList<>();

        String sql =
                "SELECT i.item_name, " +
                        "       c.category_name, " +
                        "       inv.quantity_on_hand, " +
                        "       u.unit_abbreviation, " +
                        "       i.cost, " +
                        "       COALESCE(inv.min_quantity_override, i.low_stock_threshold) AS threshold " +
                        "FROM inventory inv " +
                        "JOIN item     i ON inv.item_id   = i.item_id " +
                        "JOIN category c ON i.category_id = c.category_id " +
                        "JOIN unit     u ON i.unit_id     = u.unit_id " +
                        "WHERE inv.restaurant_id = ? " +
                        "  AND i.is_active = TRUE " +
                        // Only include rows where quantity is at or below the threshold
                        "  AND inv.quantity_on_hand <= " +
                        "      COALESCE(inv.min_quantity_override, i.low_stock_threshold) " +
                        "ORDER BY inv.quantity_on_hand ASC";   // Show the lowest stock items first

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, restaurantId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new Object[]{
                            rs.getString("item_name"),
                            rs.getString("category_name"),
                            rs.getInt("quantity_on_hand"),
                            rs.getString("unit_abbreviation"),
                            rs.getDouble("cost"),
                            rs.getInt("threshold")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    // Checks whether a single item in the given restaurant is currently
    // at or below its minimum threshold.
    // Called right after removeStock() to decide whether to show a warning.
    // Returns true if the item is low on stock, false if it is fine or not found.
    public boolean isLowStock(int restaurantId, String itemName) {

        String sql =
                "SELECT inv.quantity_on_hand, " +
                        "       COALESCE(inv.min_quantity_override, i.low_stock_threshold) AS threshold " +
                        "FROM inventory inv " +
                        "JOIN item i ON inv.item_id = i.item_id " +
                        "WHERE inv.restaurant_id = ? " +
                        "  AND i.item_name = ? " +
                        "  AND i.is_active = TRUE " +
                        "LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, restaurantId);
            stmt.setString(2, itemName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int qty       = rs.getInt("quantity_on_hand");
                    int threshold = rs.getInt("threshold");
                    return qty <= threshold;   // true means the item is low
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;   // Default to false so the app does not show false alarms
    }

    // Increases the quantity_on_hand for the given item in the given restaurant.
    // Also inserts a record into stock_transaction to keep a history of changes.
    // The amount must be greater than zero.
    // Returns true on success, false if the item is not found or the update fails.
    public boolean addStock(int restaurantId, String itemName, int amount) {

        // Reject zero or negative amounts before touching the database
        if (amount <= 0) return false;

        String findItemSql =
                "SELECT item_id FROM item " +
                        "WHERE item_name = ? AND is_active = TRUE LIMIT 1";

        String updateSql =
                "UPDATE inventory " +
                        "SET quantity_on_hand = quantity_on_hand + ? " +
                        "WHERE restaurant_id = ? AND item_id = ?";

        String insertTxSql =
                "INSERT INTO stock_transaction " +
                        "(restaurant_id, item_id, change_amount, transaction_type, note) " +
                        "VALUES (?, ?, ?, 'ADD', ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Step 1: Get the item_id from the item name
            Integer itemId = null;
            try (PreparedStatement findStmt = conn.prepareStatement(findItemSql)) {
                findStmt.setString(1, itemName);
                try (ResultSet rs = findStmt.executeQuery()) {
                    if (rs.next()) itemId = rs.getInt("item_id");
                }
            }
            if (itemId == null) return false;   // Item not found or has been deactivated

            // Step 2: Add the amount to the current quantity
            int rowsUpdated;
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, amount);
                updateStmt.setInt(2, restaurantId);
                updateStmt.setInt(3, itemId);
                rowsUpdated = updateStmt.executeUpdate();
            }
            if (rowsUpdated == 0) return false;   // No inventory row found for this restaurant

            // Step 3: Log the transaction so every change has a history record
            try (PreparedStatement txStmt = conn.prepareStatement(insertTxSql)) {
                txStmt.setInt(1, restaurantId);
                txStmt.setInt(2, itemId);
                txStmt.setInt(3, amount);
                txStmt.setString(4, "Added via Stock Adjustment screen");
                txStmt.executeUpdate();
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Decreases the quantity_on_hand for the given item in the given restaurant.
    // Refuses the operation if it would make the stock go below zero.
    // Also inserts a record into stock_transaction to keep a history of changes.
    // Returns true on success, false if there is not enough stock or an error occurs.
    public boolean removeStock(int restaurantId, String itemName, int amount) {

        // Reject zero or negative amounts
        if (amount <= 0) return false;

        String findItemSql =
                "SELECT item_id FROM item " +
                        "WHERE item_name = ? AND is_active = TRUE LIMIT 1";

        String checkSql =
                "SELECT quantity_on_hand FROM inventory " +
                        "WHERE restaurant_id = ? AND item_id = ? LIMIT 1";

        String updateSql =
                "UPDATE inventory " +
                        "SET quantity_on_hand = quantity_on_hand - ? " +
                        "WHERE restaurant_id = ? AND item_id = ?";

        String insertTxSql =
                "INSERT INTO stock_transaction " +
                        "(restaurant_id, item_id, change_amount, transaction_type, note) " +
                        "VALUES (?, ?, ?, 'REMOVE', ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Step 1: Get the item_id from the item name
            Integer itemId = null;
            try (PreparedStatement findStmt = conn.prepareStatement(findItemSql)) {
                findStmt.setString(1, itemName);
                try (ResultSet rs = findStmt.executeQuery()) {
                    if (rs.next()) itemId = rs.getInt("item_id");
                }
            }
            if (itemId == null) return false;

            // Step 2: Read the current stock level to make sure we will not go below zero
            int currentQty;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, restaurantId);
                checkStmt.setInt(2, itemId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) return false;   // No inventory row exists for this restaurant
                    currentQty = rs.getInt("quantity_on_hand");
                }
            }

            // Block the removal if it would result in a negative stock value
            if (currentQty - amount < 0) return false;

            // Step 3: Subtract the amount from the current quantity
            int rowsUpdated;
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, amount);
                updateStmt.setInt(2, restaurantId);
                updateStmt.setInt(3, itemId);
                rowsUpdated = updateStmt.executeUpdate();
            }
            if (rowsUpdated == 0) return false;

            // Step 4: Log the transaction; the amount is stored as negative to indicate a removal
            try (PreparedStatement txStmt = conn.prepareStatement(insertTxSql)) {
                txStmt.setInt(1, restaurantId);
                txStmt.setInt(2, itemId);
                txStmt.setInt(3, -amount);
                txStmt.setString(4, "Removed via Stock Adjustment screen");
                txStmt.executeUpdate();
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Updates the cost and/or low-stock threshold for an item.
    // Either newCost or newThreshold can be null, which means "leave that field alone".
    // Returns true if at least one field was updated successfully.
    public boolean editItemDetails(String itemName, Double newCost, Integer newThreshold) {

        // Nothing to do if the user did not change anything
        if (newCost == null && newThreshold == null) return false;

        // Build the UPDATE statement dynamically depending on which fields were provided.
        // This avoids accidentally overwriting a field the user did not intend to change.
        StringBuilder sql = new StringBuilder("UPDATE item SET ");
        List<Object> params = new ArrayList<>();

        if (newCost != null) {
            sql.append("cost = ?");
            params.add(newCost);
        }
        if (newThreshold != null) {
            if (!params.isEmpty()) sql.append(", ");
            sql.append("low_stock_threshold = ?");
            params.add(newThreshold);
        }

        sql.append(" WHERE item_name = ? AND is_active = TRUE");
        params.add(itemName);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString()))
        {
            // Bind each parameter in the order it was added to the list
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            int rows = stmt.executeUpdate();
            return rows > 0;   // true means the item was found and updated

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
