// AddItem.java
// Bound to AddItem.form (IntelliJ GUI Designer).
// This screen is not currently used in the main navigation flow.
// Adding items is handled through dialog prompts on the Settings screen.
// The class is kept here for potential future use as a dedicated add-item screen.

import javax.swing.*;

public class AddItem {

    // Root panel bound to AddItem.form
    private JPanel MainAddItemPanel;

    // Input field for the name of the item to add
    private JTextField whichItemWouldYouTextField;

    // Input field for the quantity to add
    private JTextField howMuchWouldYouTextField;

    // Returns the root panel so CardLayout can add this screen if needed
    public JPanel getRootPanel() { return MainAddItemPanel; }

    // Returns the item name input field
    public JTextField getItemNameField() { return whichItemWouldYouTextField; }

    // Returns the quantity input field
    public JTextField getQuantityField() { return howMuchWouldYouTextField; }
}
