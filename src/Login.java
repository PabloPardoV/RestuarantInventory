// Login.java
// Bound to Login.form (IntelliJ GUI Designer).
// Provides access to all components on the login screen
// so that Main.groovy can read inputs and attach button logic.

import javax.swing.*;

public class Login {

    // Root panel created in Login.form; CardLayout uses this to show or hide the screen
    private JPanel MainLogPanel;

    // Buttons for selecting a restaurant after login
    private JButton resturantAButton;
    private JButton resturantBButton;

    // Closes the application
    private JButton exitButton;

    // Input fields for the username and password
    private JTextField    usernameField;
    private JPasswordField passwordField;

    // Submits the login attempt
    private JButton loginButton;

    // Labels shown next to the input fields
    private JLabel usernameLabel;
    private JLabel passwordLabel;

    // Returns the root panel so CardLayout can add and switch to this screen
    public JPanel getRootPanel() { return MainLogPanel; }

    // Returns the Restaurant A button
    public JButton getResturantAButton() { return resturantAButton; }

    // Returns the Restaurant B button
    public JButton getResturantBButton() { return resturantBButton; }

    // Returns the Exit button
    public JButton getExitButton() { return exitButton; }

    // Returns the username text field
    public JTextField getUsernameField() { return usernameField; }

    // Returns the password field
    public JPasswordField getPasswordField() { return passwordField; }

    // Returns the Login button
    public JButton getLoginButton() { return loginButton; }

    // Enables or disables the Restaurant A and B buttons.
    // They are disabled at startup and only enabled after a successful login.
    public void setRestaurantButtonsEnabled(boolean enabled) {
        resturantAButton.setEnabled(enabled);
        resturantBButton.setEnabled(enabled);
    }
}
