// Menu.java
// Bound to Menu.form (IntelliJ GUI Designer).
// Provides access to the navigation buttons on the main menu screen.

import javax.swing.*;

public class Menu {

    // Root panel for this screen
    private JPanel MainManuPanel;

    // Navigation buttons
    private JButton inventoryButton;
    private JButton stockAdjustmentButton;
    private JButton settingsButton;
    private JButton exitButton;   // Used as the Log Out button in Main.groovy

    // Returns the root panel so CardLayout can add and switch to this screen
    public JPanel getRootPanel() { return MainManuPanel; }

    // Returns the button that navigates to the Inventory screen
    public JButton getInventoryButton() { return inventoryButton; }

    // Returns the button that navigates to the Stock Adjustment screen
    public JButton getStockAdjustmentButton() { return stockAdjustmentButton; }

    // Returns the button that navigates to the Settings screen
    public JButton getSettingsButton() { return settingsButton; }

    // Returns the Log Out button that returns the user to the login screen
    public JButton getExitButton() { return exitButton; }
}
