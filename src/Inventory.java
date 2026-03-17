// Inventory.java
// Bound to Inventory.form (IntelliJ GUI Designer).
// Provides access to the inventory table, search box, filter button,
// and exit button. Also contains the methods that load data into the table.

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class Inventory {

    // Root panel for this screen
    private JPanel MainInvPanel;

    // Search box where the user types an item name to filter the table
    private JTextField textField1;

    // Button that triggers the search query
    private JButton filterButton;

    // Table that shows all inventory rows
    private JTable table1;

    // Returns to the main menu
    private JButton exitButton;

    // Returns the root panel so CardLayout can add and switch to this screen
    public JPanel getRootPanel() { return MainInvPanel; }

    // Returns the search text field
    public JTextField getSearchField() { return textField1; }

    // Returns the filter button
    public JButton getFilterButton() { return filterButton; }

    // Returns the inventory table so Main.groovy can attach a mouse listener
    // and read the selected row
    public JTable getTable() { return table1; }

    // Returns the exit button
    public JButton getExitButton() { return exitButton; }

    // Populates the table with a fresh set of rows from the database.
    // Called each time the user opens the Inventory screen.
    public void loadTable(List<Object[]> rows) {
        refreshTable(rows);
    }

    // Replaces the table contents with a new set of rows.
    // Called after a search filter is applied or after an item is edited.
    public void refreshTable(List<Object[]> rows) {
        // Column headers matching the six fields returned by InventoryDAO
        String[] columns = {"Item", "Category", "Quantity", "Unit", "Cost", "Min Threshold"};

        // isCellEditable returns false so users cannot type directly into cells.
        // Editing is done by double-clicking a row to open a dialog.
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Add each row of data to the table model
        for (Object[] row : rows) {
            model.addRow(row);
        }

        table1.setModel(model);

        // Force the scroll pane that wraps the table to redraw.
        // Without this, the table display can stay stale after setModel() is called.
        table1.revalidate();
        table1.repaint();
    }
}
