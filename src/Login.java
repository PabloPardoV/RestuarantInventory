import javax.swing.*;

public class Login
{

    // Root panel (already exists)
    private JPanel MainLogPanel;

    private JButton resturantBButton;
    private JButton resturantAButton;
    private JButton exitButton;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel usernameLabel;
    private JLabel passwordLabel;

    // Gives Main access to the root panel for CardLayout
    public JPanel getRootPanel() { return MainLogPanel; }

    // Gives Main access to Restaurant buttons
    public JButton getResturantAButton() { return resturantAButton; }
    public JButton getResturantBButton() { return resturantBButton; }
    public JButton getExitButton() { return exitButton; }

    // Gives Main access to username/password/login controls
    public JTextField getUsernameField() { return usernameField; }
    public JPasswordField getPasswordField() { return passwordField; }
    public JButton getLoginButton() { return loginButton; }

    // Enables/disables Restaurant A/B buttons depending on authentication result
    public void setRestaurantButtonsEnabled(boolean enabled)
    {
        resturantAButton.setEnabled(enabled);
        resturantBButton.setEnabled(enabled);
    }
}