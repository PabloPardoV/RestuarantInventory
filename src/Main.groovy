// -----------------------------
// Imports used by this file
import javax.swing.*              // JFrame, JOptionPane, SwingUtilities
import java.awt.CardLayout        // Screen switching (CardLayout)

// -----------------------------
// Import database access classes
import DAO.UserDAO
import DAO.InventoryDAO
import DAO.SettingsDAO

class Main
{

    static void main(String[] args)
    {

        // Run all Swing UI creation on the Swing event thread
        SwingUtilities.invokeLater
                {

                    // -----------------------------
                    // 1) Create the main application window (JFrame)
                    // -----------------------------
                    def frame = new JFrame("Restaurant Inventory")
                    frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
                    frame.setSize(900, 600)
                    frame.setLocationRelativeTo(null)

                    // -----------------------------
                    // 2) Set up CardLayout (screen switching)
                    // -----------------------------
                    def cards = new CardLayout()
                    def host = new JPanel(cards)
                    frame.setContentPane(host)

                    // -----------------------------
                    // 3) Create GUI screen instances
                    // -----------------------------
                    def login        = new Login()
                    def menu         = new Menu()
                    def inventory    = new Inventory()
                    def stock        = new StockAdjustment()
                    def settingsView = new settings()

                    // -----------------------------
                    // 4) Add screens to the CardLayout host panel
                    // -----------------------------
                    host.add(login.getRootPanel(),        "login")
                    host.add(menu.getRootPanel(),         "menu")
                    host.add(inventory.getRootPanel(),    "inventory")
                    host.add(stock.getRootPanel(),        "stock")
                    host.add(settingsView.getRootPanel(), "settings")

                    // -----------------------------
                    // 5) Create DAO objects (classes that talk to the database)
                    // -----------------------------
                    def userDAO = new UserDAO()
                    def inventoryDAO = new InventoryDAO()
                    def settingsDAO = new SettingsDAO()

                    // =========================================================
                    // LOGIN SCREEN LOGIC
                    // =========================================================

                    // Restaurant buttons start disabled until the user logs in successfully
                    login.setRestaurantButtonsEnabled(false)

                    // When the Login button is pressed, authenticate user against the DB
                    login.getLoginButton().addActionListener {

                        String username = login.getUsernameField().getText().trim()
                        String password = new String(login.getPasswordField().getPassword())

                        if (username.isEmpty() || password.isEmpty()) {
                            JOptionPane.showMessageDialog(
                                    frame,
                                    "Please enter a username and password.",
                                    "Login Error",
                                    JOptionPane.ERROR_MESSAGE
                            )
                            return
                        }

                        boolean ok = userDAO.authenticate(username, password)

                        if (ok) {
                            login.setRestaurantButtonsEnabled(true)
                            JOptionPane.showMessageDialog(
                                    frame,
                                    "Login successful. Select a restaurant.",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE
                            )
                        } else {
                            login.setRestaurantButtonsEnabled(false)
                            JOptionPane.showMessageDialog(
                                    frame,
                                    "Invalid username or password.",
                                    "Login Error",
                                    JOptionPane.ERROR_MESSAGE
                            )
                        }
                    }

                    login.getResturantAButton().addActionListener {
                        AppSession.setCurrentRestaurantId(1)
                        cards.show(host, "menu")
                    }

                    login.getResturantBButton().addActionListener {
                        AppSession.setCurrentRestaurantId(2)
                        cards.show(host, "menu")
                    }

                    login.getExitButton().addActionListener {
                        System.exit(0)
                    }

                    // =========================================================
                    // MENU NAVIGATION LOGIC
                    // =========================================================

                    menu.getInventoryButton().addActionListener {
                        cards.show(host, "inventory")
                    }

                    menu.getStockAdjustmentButton().addActionListener {
                        cards.show(host, "stock")
                    }

                    menu.getSettingsButton().addActionListener {
                        cards.show(host, "settings")
                    }

                    menu.getExitButton().addActionListener {
                        cards.show(host, "login")
                    }

                    // =========================================================
                    // SETTINGS NAVIGATION LOGIC
                    // =========================================================

                    settingsView.getBackToMenuButton().addActionListener {
                        cards.show(host, "menu")
                    }

                    // -----------------------------
                    // SETTINGS DATABASE BUTTONS
                    // -----------------------------

                    settingsView.getAddItemButton().addActionListener {

                        String itemName = JOptionPane.showInputDialog(frame, "Enter new item name:")
                        if (itemName == null || itemName.trim().isEmpty()) return

                        String categoryIdStr = JOptionPane.showInputDialog(frame, "Enter category ID for this item:")
                        if (categoryIdStr == null || categoryIdStr.trim().isEmpty()) return

                        String unitIdStr = JOptionPane.showInputDialog(frame, "Enter unit ID for this item:")
                        if (unitIdStr == null || unitIdStr.trim().isEmpty()) return

                        int categoryId
                        int unitId

                        try {
                            categoryId = Integer.parseInt(categoryIdStr.trim())
                            unitId = Integer.parseInt(unitIdStr.trim())
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(frame, "Category ID and Unit ID must be numbers.", "Error", JOptionPane.ERROR_MESSAGE)
                            return
                        }

                        boolean ok = settingsDAO.addItem(itemName.trim(), categoryId, unitId)

                        JOptionPane.showMessageDialog(
                                frame,
                                ok ? "Item added successfully." : "Failed to add item.",
                                ok ? "Success" : "Error",
                                ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
                        )
                    }

                    settingsView.getRemoveItemButton().addActionListener {

                        String itemName = JOptionPane.showInputDialog(frame, "Enter item name to remove (deactivate):")
                        if (itemName == null || itemName.trim().isEmpty()) return

                        boolean ok = settingsDAO.deactivateItem(itemName.trim())

                        JOptionPane.showMessageDialog(
                                frame,
                                ok ? "Item removed successfully." : "Failed to remove item.",
                                ok ? "Success" : "Error",
                                ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
                        )
                    }

                    settingsView.getAddCategoryButton().addActionListener {

                        String categoryName = JOptionPane.showInputDialog(frame, "Enter new category name:")
                        if (categoryName == null || categoryName.trim().isEmpty()) return

                        boolean ok = settingsDAO.addCategory(categoryName.trim())

                        JOptionPane.showMessageDialog(
                                frame,
                                ok ? "Category added successfully." : "Failed to add category.",
                                ok ? "Success" : "Error",
                                ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
                        )
                    }

                    settingsView.getRemoveCategoryButton().addActionListener {

                        String categoryName = JOptionPane.showInputDialog(frame, "Enter category name to remove:")
                        if (categoryName == null || categoryName.trim().isEmpty()) return

                        boolean ok = settingsDAO.removeCategory(categoryName.trim())

                        JOptionPane.showMessageDialog(
                                frame,
                                ok ? "Category removed successfully." : "Failed to remove category.",
                                ok ? "Success" : "Error",
                                ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
                        )
                    }

                    // =========================================================
                    // INVENTORY NAVIGATION LOGIC
                    // =========================================================
                    inventory.getExitButton().addActionListener {
                        cards.show(host, "menu")
                    }

                    // =========================================================
                    // STOCK ADJUSTMENT LOGIC (Add/Remove stock using the database)
                    // =========================================================

                    stock.getAddButton().addActionListener {

                        int restId = AppSession.getCurrentRestaurantId()

                        if (restId <= 0) {
                            JOptionPane.showMessageDialog(
                                    frame,
                                    "Please select a restaurant first.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            )
                            return
                        }

                        String itemName = JOptionPane.showInputDialog(frame, "Enter item name to add:")
                        if (itemName == null || itemName.trim().isEmpty()) return

                        String amountStr = JOptionPane.showInputDialog(frame, "Enter amount to add:")
                        if (amountStr == null || amountStr.trim().isEmpty()) return

                        int amount
                        try {
                            amount = Integer.parseInt(amountStr.trim())
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(
                                    frame,
                                    "Amount must be a whole number.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            )
                            return
                        }

                        boolean ok = inventoryDAO.addStock(restId, itemName.trim(), amount)

                        if (ok) {
                            JOptionPane.showMessageDialog(
                                    frame,
                                    "Stock added successfully.",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE
                            )
                        } else {
                            JOptionPane.showMessageDialog(
                                    frame,
                                    "Failed to add stock. Check the item name and amount.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            )
                        }
                    }

                    stock.getRemoveButton().addActionListener {

                        int restId = AppSession.getCurrentRestaurantId()

                        if (restId <= 0) {
                            JOptionPane.showMessageDialog(
                                    frame,
                                    "Please select a restaurant first.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            )
                            return
                        }

                        String itemName = JOptionPane.showInputDialog(frame, "Enter item name to remove:")
                        if (itemName == null || itemName.trim().isEmpty()) return

                        String amountStr = JOptionPane.showInputDialog(frame, "Enter amount to remove:")
                        if (amountStr == null || amountStr.trim().isEmpty()) return

                        int amount
                        try {
                            amount = Integer.parseInt(amountStr.trim())
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(
                                    frame,
                                    "Amount must be a whole number.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            )
                            return
                        }

                        boolean ok = inventoryDAO.removeStock(restId, itemName.trim(), amount)

                        if (ok)
                        {
                            JOptionPane.showMessageDialog(
                                    frame,
                                    "Stock removed successfully.",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE
                            )
                        } else
                        {
                            JOptionPane.showMessageDialog(
                                    frame,
                                    "Failed to remove stock. Check item name, amount, or available stock.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            )
                        }
                    }

                    stock.getExitButton().addActionListener {
                        cards.show(host, "menu")
                    }

                    // -----------------------------
                    // 6) Show the window and start on the login screen
                    // -----------------------------
                    frame.setVisible(true)
                    cards.show(host, "login")
                }
    }
}