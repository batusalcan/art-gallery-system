package app.ui.pages;

import app.data.DBConnector;
import app.models.CurrentUser;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginPage extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerRedirectButton;
    private JButton goToHomePageButton;

    public LoginPage() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        setTitle("Login");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.setBackground(new Color(255, 255, 255));


        JLabel titleLabel = new JLabel("Login Page");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 123, 255));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        gbc.gridwidth = 1;

        // Form fields
        addFormField(panel, gbc, "Email:", emailField = new JTextField());
        addFormField(panel, gbc, "Password:", passwordField = new JPasswordField());

        // Buttons
        loginButton = new JButton("Login");
        loginButton.setBackground(new Color(0, 123, 255));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setPreferredSize(new Dimension(200, 50));

        registerRedirectButton = new JButton("Don't have an account? Register");
        registerRedirectButton.setBackground(new Color(255, 255, 255));
        registerRedirectButton.setForeground(new Color(0, 123, 255));
        registerRedirectButton.setFont(new Font("Arial", Font.PLAIN, 14));
        registerRedirectButton.setFocusPainted(false);
        registerRedirectButton.setBorderPainted(false);
        registerRedirectButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        goToHomePageButton = new JButton("Go to HomePage");
        goToHomePageButton.setBackground(new Color(255, 255, 255));
        goToHomePageButton.setForeground(new Color(0, 123, 255));
        goToHomePageButton.setFont(new Font("Arial", Font.PLAIN, 14));
        goToHomePageButton.setFocusPainted(false);
        goToHomePageButton.setBorderPainted(false);
        goToHomePageButton.setAlignmentX(Component.CENTER_ALIGNMENT);


        loginButton.addActionListener(e -> handleLoginAction());
        registerRedirectButton.addActionListener(e -> openRegisterPage());
        goToHomePageButton.addActionListener(e -> openHomePage());


        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(loginButton, gbc);

        gbc.gridy++;
        panel.add(registerRedirectButton, gbc);

        gbc.gridy++;
        panel.add(goToHomePageButton, gbc);

        add(panel);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String label, JTextField field) {
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        jLabel.setForeground(new Color(80, 80, 80));

        field.setPreferredSize(new Dimension(350, 40));
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBackground(new Color(240, 240, 240));
        field.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(jLabel, gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void handleLoginAction() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        if (!validateLogin(email, password)) {
            JOptionPane.showMessageDialog(this, "Please enter email and password.");
            return;
        }

        // Login call (customer / artist)
        String sql = "{CALL LoginUser(?, ?, ?, ?)}";

        try (Connection conn = DBConnector.connect();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.registerOutParameter(3, Types.VARCHAR); // UserType
            stmt.registerOutParameter(4, Types.VARCHAR); // UserId

            stmt.execute();

            String userType = stmt.getString(3);
            String userId = stmt.getString(4);

            if (userType == null || userId == null) {
                JOptionPane.showMessageDialog(this, "Invalid email or password.");
            } else {
                if (userType.equals("Customer")) {
                    CurrentUser.currentUser = userId;
                    JOptionPane.showMessageDialog(this, "Welcome, Customer!");
                } else {
                    CurrentUser.currentArtist = userId;
                    JOptionPane.showMessageDialog(this, "Welcome, Artist!");
                }
                openHomePage();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Login failed due to a database error.");
        }
    }

    private boolean validateLogin(String email, String password) {
        return email != null && password != null && !email.isEmpty() && !password.isEmpty();
    }

    private void openRegisterPage() {
        RegisterPage registerPage = new RegisterPage();
        registerPage.setVisible(true);
        this.setVisible(false);
    }

    private void openHomePage() {
        HomePage homePage = new HomePage();
        homePage.setVisible(true);
        this.setVisible(false);
    }

}
