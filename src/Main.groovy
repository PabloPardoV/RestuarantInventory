import javax.swing.*
import java.awt.CardLayout

class Main {
    static void main(String[] args) {
        SwingUtilities.invokeLater {

            // Frame
            def frame = new JFrame("Restaurant Inventory")
            frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            frame.setSize(900, 600)
            frame.setLocationRelativeTo(null)

            // Card system
            def cards = new CardLayout()
            def host = new JPanel(cards)
            frame.setContentPane(host)

            // GUIs
            def login        = new Login()
            def menu         = new Menu()
            def inventory    = new Inventory()
            def stock        = new StockAdjustment()
            def settingsView = new settings()

            // Add cards
            host.add(login.getRootPanel(),        "login")
            host.add(menu.getRootPanel(),         "menu")
            host.add(inventory.getRootPanel(),    "inventory")
            host.add(stock.getRootPanel(),        "stock")
            host.add(settingsView.getRootPanel(), "settings")

            // ----- SIMPLE BUTTON WIRING -----

            // LOGIN
            login.getResturantAButton().addActionListener { cards.show(host, "menu") }
            login.getResturantBButton().addActionListener { cards.show(host, "menu") }
            login.getExitButton().addActionListener       { System.exit(0) }

            // MENU
            menu.getInventoryButton().addActionListener       { cards.show(host, "inventory") }
            menu.getStockAdjustmentButton().addActionListener { cards.show(host, "stock") }
            menu.getSettingsButton().addActionListener        { cards.show(host, "settings") }
            menu.getExitButton().addActionListener            { cards.show(host, "login") }

            // SETTINGS
            settingsView.getBackToMenuButton().addActionListener { cards.show(host, "menu") }

            // INVENTORY
            inventory.getExitButton().addActionListener { cards.show(host, "menu") }

            // STOCK ADJUSTMENT
            stock.getExitButton().addActionListener { cards.show(host, "menu") }

            // Show Login
            frame.setVisible(true)
            cards.show(host, "login")
        }
    }
}
