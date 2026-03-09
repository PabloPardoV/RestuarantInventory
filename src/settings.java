// settings.java
// Bound to settings.form (IntelliJ GUI Designer).
// Provides access to all the buttons on the settings screen.

import javax.swing.*;

public class settings {

    // Root panel for this screen
    private JPanel MainSettingPanel;

    // Buttons for managing items
    private JButton addItemButton;
    private JButton removeItemButton;

    // Buttons for managing categories
    private JButton addCategoryButton;
    private JButton removeCategoryButton;

    // Button to change the low-stock threshold for a specific item and restaurant
    private JButton changeLowStockThresholdButton;

    // Returns to the main menu
    private JButton backToMenuButton;

    // Returns the root panel so CardLayout can add and switch to this screen
    public JPanel getRootPanel() { return MainSettingPanel; }

    // Returns the Back to Menu button
    public JButton getBackToMenuButton() { return backToMenuButton; }

    // Returns the Add Item button
    public JButton getAddItemButton() { return addItemButton; }

    // Returns the Remove Item button
    public JButton getRemoveItemButton() { return removeItemButton; }

    // Returns the Add Category button
    public JButton getAddCategoryButton() { return addCategoryButton; }

    // Returns the Remove Category button
    public JButton getRemoveCategoryButton() { return removeCategoryButton; }

    // Returns the Change Low-Stock Threshold button
    public JButton getChangeLowStockThresholdButton() { return changeLowStockThresholdButton; }
}
