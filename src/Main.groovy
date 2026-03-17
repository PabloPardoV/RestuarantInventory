// Main.groovy
// This is the entry point of the application.
// It creates the main window, sets up screen switching, and attaches
// all button click logic to the GUI components.

import javax.swing.*
import java.awt.CardLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

// DAO classes handle all communication with the database
import DAO.UserDAO
import DAO.InventoryDAO
import DAO.SettingsDAO

class Main {

    static void main(String[] args) {

        // Swing UI code must run on the Event Dispatch Thread (EDT)
        // to avoid threading issues with the GUI
        SwingUtilities.invokeLater {

            // Create the main application window
            def frame = new JFrame("チキン・ブラザーズ – Inventory Manager")
            frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            frame.setSize(960, 640)
            frame.setLocationRelativeTo(null)   // Centre the window on screen

            // Add a Reports menu bar at the top of the window.
            // This avoids needing to add new buttons to any .form file.
            def menuBar        = new JMenuBar()
            def reportsMenu    = new JMenu("Reports")
            def fullReportItem = new JMenuItem("Full Stock Report")
            def lowStockItem   = new JMenuItem("Low-Stock Report")
            reportsMenu.add(fullReportItem)
            reportsMenu.add(lowStockItem)
            menuBar.add(reportsMenu)
            frame.setJMenuBar(menuBar)

            // CardLayout lets us swap between screens inside one window
            def cards = new CardLayout()
            def host  = new JPanel(cards)
            frame.setContentPane(host)

            // Create one instance of each screen
            def login        = new Login()
            def menu         = new Menu()
            def inventory    = new Inventory()
            def stock        = new StockAdjustment()
            def settingsView = new settings()

            // Register each screen with the CardLayout using a string key.
            // We use these keys later to switch to a specific screen.
            host.add(login.getRootPanel(),        "login")
            host.add(menu.getRootPanel(),         "menu")
            host.add(inventory.getRootPanel(),    "inventory")
            host.add(stock.getRootPanel(),        "stock")
            host.add(settingsView.getRootPanel(), "settings")

            // Create DAO instances, one per area of the database
            def userDAO      = new UserDAO()
            def inventoryDAO = new InventoryDAO()
            def settingsDAO  = new SettingsDAO()

            // LOGIN SCREEN

            // The restaurant selection buttons are disabled at startup.
            // They only become enabled once the user logs in successfully.
            login.setRestaurantButtonsEnabled(false)

            // Login button: reads the username and password fields,
            // then checks them against the database.
            login.getLoginButton().addActionListener {

                String username = login.getUsernameField().getText().trim()
                String password = new String(login.getPasswordField().getPassword())

                // Both fields must be filled in before we attempt authentication
                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(frame,
                            "Please enter both a username and a password.",
                            "Login Error", JOptionPane.ERROR_MESSAGE)
                    return
                }

                // Check the credentials against the app_user table
                boolean ok = userDAO.authenticate(username, password)

                if (ok) {
                    // Show the restaurant selection buttons on successful login
                    login.setRestaurantButtonsEnabled(true)
                    JOptionPane.showMessageDialog(frame,
                            "Login successful. Please select a restaurant.",
                            "Welcome", JOptionPane.INFORMATION_MESSAGE)
                } else {
                    login.setRestaurantButtonsEnabled(false)
                    JOptionPane.showMessageDialog(frame,
                            "Invalid username or password.",
                            "Login Error", JOptionPane.ERROR_MESSAGE)
                }
            }

            // Restaurant A button: saves restaurant ID 1 in AppSession, then opens the menu
            login.getResturantAButton().addActionListener {
                AppSession.setCurrentRestaurantId(1)
                cards.show(host, "menu")
            }

            // Restaurant B button: saves restaurant ID 2 in AppSession, then opens the menu
            login.getResturantBButton().addActionListener {
                AppSession.setCurrentRestaurantId(2)
                cards.show(host, "menu")
            }

            // Exit button on the login screen closes the application
            login.getExitButton().addActionListener {
                System.exit(0)
            }

            // MAIN MENU

            // Inventory button: fetches the latest data from the database,
            // loads it into the table, then switches to the inventory screen
            menu.getInventoryButton().addActionListener {
                int restId = AppSession.getCurrentRestaurantId()
                List<Object[]> rows = inventoryDAO.getInventory(restId, "", "All")
                inventory.loadTable(rows)
                cards.show(host, "inventory")
            }

            // Navigate to the stock adjustment screen
            menu.getStockAdjustmentButton().addActionListener {
                cards.show(host, "stock")
            }

            // Navigate to the settings screen
            menu.getSettingsButton().addActionListener {
                cards.show(host, "settings")
            }

            // Log out button: clears all session data and returns to the login screen
            menu.getExitButton().addActionListener {
                AppSession.setCurrentRestaurantId(-1)        // Clear the restaurant selection
                login.setRestaurantButtonsEnabled(false)     // Hide the restaurant buttons again
                login.getUsernameField().setText("")         // Clear the username field
                login.getPasswordField().setText("")         // Clear the password field
                cards.show(host, "login")
            }

            // INVENTORY SCREEN

            // Filter button: re-runs the inventory query using whatever the user typed
            // in the search box, filtering results by item name
            inventory.getFilterButton().addActionListener {
                int    restId = AppSession.getCurrentRestaurantId()
                String search = inventory.getSearchField().getText().trim()
                // Passing "All" for category means no category filter is applied
                List<Object[]> rows = inventoryDAO.getInventory(restId, search, "All")
                inventory.refreshTable(rows)
            }

            // Also trigger the filter when the user presses Enter inside the search box,
            // so they do not have to move to the button after typing
            if (inventory.getSearchField() != null) {
                inventory.getSearchField().addActionListener {
                    int    restId = AppSession.getCurrentRestaurantId()
                    String search = inventory.getSearchField().getText().trim()
                    List<Object[]> rows = inventoryDAO.getInventory(restId, search, "All")
                    inventory.refreshTable(rows)
                }
            }

            // Double-clicking a row in the inventory table opens an edit dialog.
            // This lets the user update an item's cost or low-stock threshold
            // without needing an extra button in the form file.
            inventory.getTable().addMouseListener(new MouseAdapter() {
                @Override
                void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {

                        int selectedRow = inventory.getTable().getSelectedRow()
                        if (selectedRow == -1) return

                        // The item name is always stored in column 0
                        String itemName = inventory.getTable().getValueAt(selectedRow, 0).toString()

                        // Ask for a new cost value; leaving it blank keeps the current one
                        String costStr = JOptionPane.showInputDialog(frame,
                                "New cost for \"${itemName}\"  (leave blank to keep current):")
                        if (costStr == null) return   // User pressed Cancel

                        // Ask for a new low-stock threshold; leaving it blank keeps the current one
                        String thrStr = JOptionPane.showInputDialog(frame,
                                "New low-stock threshold for \"${itemName}\"  (leave blank to keep current):")
                        if (thrStr == null) return

                        // Only parse the value if the user actually typed something
                        Double  newCost      = costStr.trim().isEmpty() ? null : Double.parseDouble(costStr.trim())
                        Integer newThreshold = thrStr.trim().isEmpty()  ? null : Integer.parseInt(thrStr.trim())

                        // Validate the inputs before sending them to the database
                        if (newCost != null && newCost < 0) {
                            JOptionPane.showMessageDialog(frame, "Cost cannot be negative.",
                                    "Validation Error", JOptionPane.ERROR_MESSAGE)
                            return
                        }
                        if (newThreshold != null && newThreshold <= 0) {
                            JOptionPane.showMessageDialog(frame,
                                    "Low-stock threshold must be greater than zero.",
                                    "Validation Error", JOptionPane.ERROR_MESSAGE)
                            return
                        }

                        boolean ok = inventoryDAO.editItemDetails(itemName, newCost, newThreshold)

                        JOptionPane.showMessageDialog(frame,
                                ok ? "Item updated successfully." : "Failed to update item.",
                                ok ? "Success" : "Error",
                                ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE)

                        // Reload the table so the new values are visible straight away
                        if (ok) {
                            int restId = AppSession.getCurrentRestaurantId()
                            inventory.refreshTable(inventoryDAO.getInventory(restId, "", "All"))
                        }
                    }
                }
            })

            // Back to menu from the inventory screen
            inventory.getExitButton().addActionListener {
                cards.show(host, "menu")
            }

            // STOCK ADJUSTMENT SCREEN

            // Add Stock button: asks for an item name and an amount,
            // then increases the stock in the database
            stock.getAddButton().addActionListener {

                int restId = AppSession.getCurrentRestaurantId()
                if (restId <= 0) {
                    JOptionPane.showMessageDialog(frame, "Please select a restaurant first.",
                            "Error", JOptionPane.ERROR_MESSAGE)
                    return
                }

                String itemName = JOptionPane.showInputDialog(frame,
                        "Enter item name to add stock for:")
                if (itemName == null || itemName.trim().isEmpty()) return

                String amountStr = JOptionPane.showInputDialog(frame, "Enter amount to add:")
                if (amountStr == null || amountStr.trim().isEmpty()) return

                int amount
                try {
                    amount = Integer.parseInt(amountStr.trim())
                    if (amount <= 0) throw new NumberFormatException()
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame,
                            "Amount must be a positive whole number.",
                            "Validation Error", JOptionPane.ERROR_MESSAGE)
                    return
                }

                // Add the amount to the inventory row in the database
                boolean ok = inventoryDAO.addStock(restId, itemName.trim(), amount)

                JOptionPane.showMessageDialog(frame,
                        ok ? "Stock added successfully."
                                : "Failed to add stock. Check that the item name is correct.",
                        ok ? "Success" : "Error",
                        ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE)
            }

            // Remove Stock button: asks for an item name and an amount,
            // then decreases the stock in the database
            stock.getRemoveButton().addActionListener {

                int restId = AppSession.getCurrentRestaurantId()
                if (restId <= 0) {
                    JOptionPane.showMessageDialog(frame, "Please select a restaurant first.",
                            "Error", JOptionPane.ERROR_MESSAGE)
                    return
                }

                String itemName = JOptionPane.showInputDialog(frame,
                        "Enter item name to remove stock from:")
                if (itemName == null || itemName.trim().isEmpty()) return

                String amountStr = JOptionPane.showInputDialog(frame, "Enter amount to remove:")
                if (amountStr == null || amountStr.trim().isEmpty()) return

                int amount
                try {
                    amount = Integer.parseInt(amountStr.trim())
                    if (amount <= 0) throw new NumberFormatException()
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame,
                            "Amount must be a positive whole number.",
                            "Validation Error", JOptionPane.ERROR_MESSAGE)
                    return
                }

                // Subtract from the inventory row; the DAO prevents stock going below zero
                boolean ok = inventoryDAO.removeStock(restId, itemName.trim(), amount)

                if (ok) {
                    JOptionPane.showMessageDialog(frame, "Stock removed successfully.",
                            "Success", JOptionPane.INFORMATION_MESSAGE)

                    // After removing stock, check if the item has dropped below the minimum level
                    if (inventoryDAO.isLowStock(restId, itemName.trim())) {
                        JOptionPane.showMessageDialog(frame,
                                "WARNING: \"${itemName}\" is now at or below its low-stock threshold!",
                                "Low Stock Alert", JOptionPane.WARNING_MESSAGE)
                    }
                } else {
                    JOptionPane.showMessageDialog(frame,
                            "Failed to remove stock. Check the item name or available quantity.",
                            "Error", JOptionPane.ERROR_MESSAGE)
                }
            }

            // Back to menu from the stock adjustment screen
            stock.getExitButton().addActionListener {
                cards.show(host, "menu")
            }

            // SETTINGS SCREEN

            // Back to menu from the settings screen
            settingsView.getBackToMenuButton().addActionListener {
                cards.show(host, "menu")
            }

            // Add Item button: collects item details from the user via dialogs
            // and inserts a new item into the database.
            // Uses names and abbreviations so the user never needs to know database IDs.
            settingsView.getAddItemButton().addActionListener {

                String itemName = JOptionPane.showInputDialog(frame, "Enter new item name:")
                if (itemName == null || itemName.trim().isEmpty()) return

                String categoryName = JOptionPane.showInputDialog(frame,
                        "Enter category name (e.g. Meat, Vegetables, Dry Goods):")
                if (categoryName == null || categoryName.trim().isEmpty()) return

                String unitAbbr = JOptionPane.showInputDialog(frame,
                        "Enter unit abbreviation (e.g. kg, g, L, pcs):")
                if (unitAbbr == null || unitAbbr.trim().isEmpty()) return

                String thrStr = JOptionPane.showInputDialog(frame,
                        "Enter low-stock threshold (minimum quantity before an alert is shown):")
                if (thrStr == null || thrStr.trim().isEmpty()) return

                int threshold
                try {
                    threshold = Integer.parseInt(thrStr.trim())
                    if (threshold <= 0) throw new NumberFormatException()
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame,
                            "Threshold must be a positive whole number.",
                            "Validation Error", JOptionPane.ERROR_MESSAGE)
                    return
                }

                boolean ok = settingsDAO.addItem(
                        itemName.trim(), categoryName.trim(), unitAbbr.trim(), threshold)

                JOptionPane.showMessageDialog(frame,
                        ok ? "Item \"${itemName}\" added successfully."
                                : "Failed to add item. Check that the category and unit exist.",
                        ok ? "Success" : "Error",
                        ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE)
            }

            // Remove Item button: soft-deletes the item by setting is_active = FALSE.
            // The item disappears from all screens but its history stays in the database.
            settingsView.getRemoveItemButton().addActionListener {

                String itemName = JOptionPane.showInputDialog(frame,
                        "Enter item name to deactivate:")
                if (itemName == null || itemName.trim().isEmpty()) return

                boolean ok = settingsDAO.deactivateItem(itemName.trim())

                JOptionPane.showMessageDialog(frame,
                        ok ? "Item \"${itemName}\" deactivated successfully."
                                : "Failed to deactivate item. Check that the name is correct.",
                        ok ? "Success" : "Error",
                        ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE)
            }

            // Add Category button: inserts a new category into the database
            settingsView.getAddCategoryButton().addActionListener {

                String categoryName = JOptionPane.showInputDialog(frame,
                        "Enter new category name:")
                if (categoryName == null || categoryName.trim().isEmpty()) return

                boolean ok = settingsDAO.addCategory(categoryName.trim())

                JOptionPane.showMessageDialog(frame,
                        ok ? "Category \"${categoryName}\" added successfully."
                                : "Failed to add category.",
                        ok ? "Success" : "Error",
                        ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE)
            }

            // Remove Category button: deletes the category from the database.
            // This will fail if any items still belong to this category.
            settingsView.getRemoveCategoryButton().addActionListener {

                String categoryName = JOptionPane.showInputDialog(frame,
                        "Enter category name to remove:")
                if (categoryName == null || categoryName.trim().isEmpty()) return

                boolean ok = settingsDAO.removeCategory(categoryName.trim())

                JOptionPane.showMessageDialog(frame,
                        ok ? "Category \"${categoryName}\" removed successfully."
                                : "Failed to remove category.",
                        ok ? "Success" : "Error",
                        ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE)
            }

            // Change Low-Stock Threshold button: sets a per-restaurant minimum
            // quantity override for a specific item
            settingsView.getChangeLowStockThresholdButton().addActionListener {

                int restId = AppSession.getCurrentRestaurantId()
                if (restId <= 0) {
                    JOptionPane.showMessageDialog(frame,
                            "Please select a restaurant first.",
                            "Error", JOptionPane.ERROR_MESSAGE)
                    return
                }

                String itemName = JOptionPane.showInputDialog(frame,
                        "Enter item name to set a custom low-stock threshold for:")
                if (itemName == null || itemName.trim().isEmpty()) return

                String thrStr = JOptionPane.showInputDialog(frame,
                        "Enter new low-stock threshold for \"${itemName}\":")
                if (thrStr == null || thrStr.trim().isEmpty()) return

                int threshold
                try {
                    threshold = Integer.parseInt(thrStr.trim())
                    if (threshold <= 0) throw new NumberFormatException()
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame,
                            "Threshold must be a positive whole number.",
                            "Validation Error", JOptionPane.ERROR_MESSAGE)
                    return
                }

                boolean ok = settingsDAO.setLowStockThresholdOverride(
                        restId, itemName.trim(), threshold)

                JOptionPane.showMessageDialog(frame,
                        ok ? "Threshold updated successfully."
                                : "Failed to update threshold. Check that the item name is correct.",
                        ok ? "Success" : "Error",
                        ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE)
            }

            // REPORTS MENU BAR
            // Both report options live in the menu bar at the top of the window,
            // so they are accessible from any screen.

            // Full Stock Report: displays every item and its current quantity
            fullReportItem.addActionListener {
                int restId = AppSession.getCurrentRestaurantId()
                if (restId <= 0) {
                    JOptionPane.showMessageDialog(frame,
                            "Please select a restaurant first.", "Error", JOptionPane.ERROR_MESSAGE)
                    return
                }
                List<Object[]> rows = inventoryDAO.getInventory(restId, "", "All")
                showReportDialog(frame, "Full Stock Report", buildFullStockReport(rows))
            }

            // Low-Stock Report: displays only items that are at or below their minimum threshold
            lowStockItem.addActionListener {
                int restId = AppSession.getCurrentRestaurantId()
                if (restId <= 0) {
                    JOptionPane.showMessageDialog(frame,
                            "Please select a restaurant first.", "Error", JOptionPane.ERROR_MESSAGE)
                    return
                }
                List<Object[]> rows = inventoryDAO.getLowStockItems(restId)
                showReportDialog(frame, "Low-Stock Report", buildLowStockReport(rows))
            }

            // Make the window visible and start on the login screen
            frame.setVisible(true)
            cards.show(host, "login")
        }
    }

    // Builds a formatted plain-text Full Stock Report.
    // Each row shows the item name, category, quantity, unit and cost.
    static String buildFullStockReport(List<Object[]> rows) {
        StringBuilder sb = new StringBuilder()
        sb.append("=== FULL STOCK REPORT ===\n\n")
        sb.append(String.format("%-22s %-16s %-10s %-6s %-8s%n",
                "Item", "Category", "Quantity", "Unit", "Cost"))
        sb.append("-".repeat(66)).append("\n")
        for (Object[] row : rows) {
            // row order: [name, category, quantity, unit, cost, threshold]
            sb.append(String.format("%-22s %-16s %-10s %-6s %-8s%n",
                    row[0], row[1], row[2], row[3], row[4]))
        }
        return sb.toString()
    }

    // Builds a formatted plain-text Low-Stock Report.
    // Only includes items where the current quantity is at or below the threshold.
    static String buildLowStockReport(List<Object[]> rows) {
        StringBuilder sb = new StringBuilder()
        sb.append("=== LOW-STOCK REPORT ===\n\n")
        sb.append(String.format("%-22s %-10s %-12s %-6s%n",
                "Item", "Quantity", "Min Needed", "Unit"))
        sb.append("-".repeat(54)).append("\n")
        if (rows.isEmpty()) {
            sb.append("No items are currently below their low-stock threshold.\n")
        } else {
            for (Object[] row : rows) {
                // row[2] = quantity, row[5] = threshold
                sb.append(String.format("%-22s %-10s %-12s %-6s%n",
                        row[0], row[2], row[5], row[3]))
            }
        }
        return sb.toString()
    }

    // Opens a scrollable, read-only dialog to display a report
    static void showReportDialog(JFrame parent, String title, String reportText) {
        JTextArea textArea = new JTextArea(reportText)
        textArea.setEditable(false)
        // Monospaced font keeps all the columns lined up properly
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 13))

        JScrollPane scrollPane = new JScrollPane(textArea)
        scrollPane.setPreferredSize(new java.awt.Dimension(620, 420))

        JOptionPane.showMessageDialog(parent, scrollPane, title,
                JOptionPane.PLAIN_MESSAGE)
    }
}
