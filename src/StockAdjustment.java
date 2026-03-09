// StockAdjustment.java
// Bound to StockAdjustment.form (IntelliJ GUI Designer).
// Provides access to the Add, Remove, and Exit buttons
// on the stock adjustment screen.

import javax.swing.*;

public class StockAdjustment {

    // Root panel for this screen
    private JPanel MainStkPanel;

    // Button to increase the stock of an item
    private JButton addButton;

    // Button to decrease the stock of an item
    private JButton removeButton;

    // Returns to the main menu
    private JButton exitButton;

    // Returns the root panel so CardLayout can add and switch to this screen
    public JPanel getRootPanel() { return MainStkPanel; }

    // Returns the Add Stock button
    public JButton getAddButton() { return addButton; }

    // Returns the Remove Stock button
    public JButton getRemoveButton() { return removeButton; }

    // Returns the Exit button
    public JButton getExitButton() { return exitButton; }
}
