import javax.swing.*;

public class settings
{
    private JPanel MainSettingPanel;
    private JButton removeCategoryButton;
    private JButton removeItemButton;
    private JButton addItemButton;
    private JButton backToMenuButton;
    private JButton changeLowStockThresholdButton;
    private JButton addCategoryButton;

    public JPanel getRootPanel() { return MainSettingPanel; }

    // Returns the "Back to menu" button so Main.groovy can attach logic
    public JButton getBackToMenuButton()
    {
        return backToMenuButton;
    }
    // Returns the "Change low stock threshold" button
    public JButton getChangeLowStockThresholdButton()
    {
        return changeLowStockThresholdButton;
    }
    // Returns the "Add item" button
    public JButton getAddItemButton()
    {
        return addItemButton;
    }
    // Returns the "Remove item" button
    public JButton getRemoveItemButton()
    {
        return removeItemButton;
    }
    // Returns the "Add category" button
    public JButton getAddCategoryButton()
    {
        return addCategoryButton;
    }
    // Returns the "Remove category" button
    public JButton getRemoveCategoryButton()
    {
        return removeCategoryButton;
    }

}