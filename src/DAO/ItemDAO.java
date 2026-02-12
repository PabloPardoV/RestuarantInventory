package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Handles item-related database actions (adding items, looking up IDs, etc.)
 */
public class ItemDAO
{

    /**
     * Adds a new item by using names/abbreviations instead of asking the user for IDs.
     *
     * @param itemName          The item name (example: "Tomato")
     * @param categoryName      The category name (example: "Vegetables")
     * @param unitAbbreviation  The unit abbreviation (example: "kg", "g", "ml", "pcs")
     * @param lowStockThreshold Default low stock threshold for this item
     * @return true if insert succeeded, false otherwise
     */
    public boolean addItem(String itemName, String categoryName, String unitAbbreviation, int lowStockThreshold)
    {

        // Basic validation
        if (itemName == null || itemName.trim().isEmpty()) return false;
        if (categoryName == null || categoryName.trim().isEmpty()) return false;
        if (unitAbbreviation == null || unitAbbreviation.trim().isEmpty()) return false;
        if (lowStockThreshold <= 0) return false;

        String findCategorySql = "SELECT category_id FROM category WHERE category_name = ? LIMIT 1";
        String findUnitSql = "SELECT unit_id FROM unit WHERE unit_abbreviation = ? LIMIT 1";

        String insertItemSql =
                "INSERT INTO item (item_name, category_id, unit_id, is_active, create_date, low_stock_threshold) " +
                        "VALUES (?, ?, ?, TRUE, NOW(), ?)";

        try (Connection conn = DatabaseConnection.getConnection())
        {

            // 1) Find category_id using category_name
            Integer categoryId = null;
            try (PreparedStatement catStmt = conn.prepareStatement(findCategorySql))
            {
                catStmt.setString(1, categoryName.trim());
                try (ResultSet rs = catStmt.executeQuery())
                {
                    if (rs.next()) categoryId = rs.getInt("category_id");
                }
            }
            if (categoryId == null) return false; // Category not found

            // 2) Find unit_id using unit_abbreviation
            Integer unitId = null;
            try (PreparedStatement unitStmt = conn.prepareStatement(findUnitSql))
            {
                unitStmt.setString(1, unitAbbreviation.trim());
                try (ResultSet rs = unitStmt.executeQuery())
                {
                    if (rs.next()) unitId = rs.getInt("unit_id");
                }
            }
            if (unitId == null) return false; // Unit not found

            // 3) Insert the item (MySQL will auto-generate item_id)
            try (PreparedStatement insertStmt = conn.prepareStatement(insertItemSql))
            {
                insertStmt.setString(1, itemName.trim());
                insertStmt.setInt(2, categoryId);
                insertStmt.setInt(3, unitId);
                insertStmt.setInt(4, lowStockThreshold);
                insertStmt.executeUpdate();
            }

            return true;

        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
}