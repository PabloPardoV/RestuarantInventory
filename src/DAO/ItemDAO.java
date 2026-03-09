package DAO;

// ItemDAO.java
// Handles inserting new items into the database.
// Accepts human-readable names so the user never has to deal with raw IDs.

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ItemDAO {

    // Adds a new item to the item table.
    // Looks up category_id and unit_id from the names provided,
    // so the user only needs to type a category name and a unit abbreviation.
    //
    // itemName          - display name of the new item, e.g. "Chicken Thigh"
    // categoryName      - name of the category it belongs to, e.g. "Meat"
    // unitAbbreviation  - unit short form, e.g. "kg" or "pcs"
    // lowStockThreshold - minimum quantity before a low-stock warning is triggered
    //
    // Returns true if the item was inserted, false if any input is invalid
    // or the category or unit could not be found.
    public boolean addItem(String itemName, String categoryName,
                           String unitAbbreviation, int lowStockThreshold) {

        // Validate all inputs before touching the database
        if (itemName == null || itemName.trim().isEmpty())                 return false;
        if (categoryName == null || categoryName.trim().isEmpty())         return false;
        if (unitAbbreviation == null || unitAbbreviation.trim().isEmpty()) return false;
        if (lowStockThreshold <= 0)                                        return false;

        String findCategorySql =
                "SELECT category_id FROM category WHERE category_name = ? LIMIT 1";
        String findUnitSql =
                "SELECT unit_id FROM unit WHERE unit_abbreviation = ? LIMIT 1";
        String insertItemSql =
                "INSERT INTO item " +
                        "(item_name, category_id, unit_id, is_active, create_date, low_stock_threshold) " +
                        "VALUES (?, ?, ?, TRUE, NOW(), ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Step 1: Resolve the category name to a category_id
            Integer categoryId = null;
            try (PreparedStatement catStmt = conn.prepareStatement(findCategorySql)) {
                catStmt.setString(1, categoryName.trim());
                try (ResultSet rs = catStmt.executeQuery()) {
                    if (rs.next()) categoryId = rs.getInt("category_id");
                }
            }
            if (categoryId == null) return false;   // Category does not exist

            // Step 2: Resolve the unit abbreviation to a unit_id
            Integer unitId = null;
            try (PreparedStatement unitStmt = conn.prepareStatement(findUnitSql)) {
                unitStmt.setString(1, unitAbbreviation.trim());
                try (ResultSet rs = unitStmt.executeQuery()) {
                    if (rs.next()) unitId = rs.getInt("unit_id");
                }
            }
            if (unitId == null) return false;   // Unit does not exist

            // Step 3: Insert the item row; MySQL auto-generates the item_id
            try (PreparedStatement insertStmt = conn.prepareStatement(insertItemSql)) {
                insertStmt.setString(1, itemName.trim());
                insertStmt.setInt(2, categoryId);
                insertStmt.setInt(3, unitId);
                insertStmt.setInt(4, lowStockThreshold);
                insertStmt.executeUpdate();
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
