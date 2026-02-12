import javax.swing.*;

/**
 * StockAdjustment screen class bound to StockAdjustment.form.
 * This class provides access to the UI components created in the IntelliJ
 * GUI Designer so they can be controlled from Main.groovy.
 */

public class StockAdjustment
{

    // Root panel defined in the .form file (must match the exact field name)
    private JPanel MainStkPanel;

    // Buttons defined in the .form file
    private JButton addButton;
    private JButton removeButton;
    private JButton exitButton;

    //Constructor
    public StockAdjustment()
    {
        $$$setupUI$$$();
    }

    public JPanel getRootPanel()
    {
        return MainStkPanel;
    }

    public JButton getAddButton()
    {
        return addButton;
    }

    public JButton getRemoveButton()
    {
        return removeButton;
    }

    public JButton getExitButton()
    {
        return exitButton;
    }


    private void $$$setupUI$$$()
    {

    }
}