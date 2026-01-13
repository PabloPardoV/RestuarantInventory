import javax.swing.*;

public class Menu {
    private JPanel MainManuPanel;
    private JButton inventoryButton;
    private JButton stockAdjustmentButton;
    private JButton exitButton;
    private JButton settingsButton;

    public JPanel getRootPanel() { return MainManuPanel; }

    public JButton getInventoryButton() { return inventoryButton; }
    public JButton getStockAdjustmentButton() { return stockAdjustmentButton; }
    public JButton getSettingsButton() { return settingsButton; }
    public JButton getExitButton() { return exitButton; }
}