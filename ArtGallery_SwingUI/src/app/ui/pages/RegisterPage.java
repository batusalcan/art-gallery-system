package app.ui.pages;

import app.data.DBConnector;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.UUID;

public class RegisterPage extends JFrame {

    private JTextField fullNameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField addressField;
    private JTextField bioField;
    private JTextField profileImageField;
    private JRadioButton customerRadioButton;
    private JRadioButton artistRadioButton;
    private JButton registerButton;
    private JButton loginRedirectButton;

    public RegisterPage() {
        setTitle("Register");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.setBackground(new Color(255, 255, 255));

        // Title
        JLabel titleLabel = new JLabel("Register Page");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 123, 255));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        gbc.gridwidth = 1;

        // Radio buttons
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        customerRadioButton = new JRadioButton("Register as Customer");
        artistRadioButton = new JRadioButton("Register as Artist");
        ButtonGroup group = new ButtonGroup();
        group.add(customerRadioButton);
        group.add(artistRadioButton);
        radioPanel.add(customerRadioButton);
        radioPanel.add(artistRadioButton);
        gbc.gridy++;
        panel.add(radioPanel, gbc);

        // Add form fields
        addFormField(panel, gbc, "Full Name:", fullNameField = new JTextField());
        addFormField(panel, gbc, "Email:", emailField = new JTextField());
        addFormField(panel, gbc, "Password:", passwordField = new JPasswordField());
        addFormField(panel, gbc, "Address:", addressField = new JTextField());

        // Artist-specific fields
        JLabel artistNote = new JLabel("Biography & Profile Image URL are required for artists only!");
        artistNote.setFont(new Font("Arial", Font.CENTER_BASELINE, 14));
        artistNote.setForeground(new Color(255, 0, 0));
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(artistNote, gbc);

        addFormField(panel, gbc, "Biography:", bioField = new JTextField());
        addFormField(panel, gbc, "Profile Image URL:", profileImageField = new JTextField());

        // Register button
        registerButton = new JButton("Register");
        registerButton.setBackground(new Color(0, 123, 255));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("Arial", Font.BOLD, 16));
        registerButton.setPreferredSize(new Dimension(200, 50));
        registerButton.setFocusPainted(false);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        panel.add(registerButton, gbc);

        // Login redirect button
        loginRedirectButton = new JButton("Already have an account? Login");
        loginRedirectButton.setBackground(new Color(255, 255, 255));
        loginRedirectButton.setForeground(new Color(0, 123, 255));
        loginRedirectButton.setFont(new Font("Arial", Font.PLAIN, 14));
        loginRedirectButton.setFocusPainted(false);
        loginRedirectButton.setBorderPainted(false);
        loginRedirectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        gbc.gridy++;
        panel.add(loginRedirectButton, gbc);

        // Home button
        JButton homeButton = new JButton("Back to Home");
        homeButton.setBackground(new Color(255, 255, 255));
        homeButton.setForeground(new Color(40, 167, 69));
        homeButton.setFont(new Font("Arial", Font.PLAIN, 14));
        homeButton.setFocusPainted(false);
        homeButton.setBorderPainted(false);
        homeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        gbc.gridy++;
        panel.add(homeButton, gbc);

        // Actions
        registerButton.addActionListener(e -> handleRegisterAction());
        loginRedirectButton.addActionListener(e -> openLoginPage());
        homeButton.addActionListener(e -> openHomePage());

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

    private void handleRegisterAction() {
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String address = addressField.getText();
        String bio = bioField.getText();
        String profileImage = profileImageField.getText();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.");
            return;
        }

        if (customerRadioButton.isSelected()) {
            registerCustomer(fullName, email, password, address);
        } else if (artistRadioButton.isSelected()) {
            if (bio.isEmpty() || profileImage.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields for Artist.");
                return;
            }
            registerArtist(fullName, email, password, bio, profileImage);
        } else {
            JOptionPane.showMessageDialog(this, "Please select register type!");
        }
    }

    private void registerCustomer(String fullName, String email, String password, String address) {
        String sql = "{CALL RegisterCustomer(?, ?, ?, ?, ?)}";

        try (Connection conn = DBConnector.connect();
             CallableStatement stmt = conn.prepareCall(sql)) {

            String customerId = UUID.randomUUID().toString();

            stmt.setString(1, customerId);
            stmt.setString(2, fullName);
            stmt.setString(3, email);
            stmt.setString(4, password);
            stmt.setString(5, address);

            stmt.execute();
            JOptionPane.showMessageDialog(this, "Customer registered successfully!");
            openLoginPage(); // Auto-redirect to login

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: Could not register customer.");
        }
    }

    private void registerArtist(String fullName, String email, String password, String bio, String profileImage) {
        String sql = "{CALL RegisterArtist(?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DBConnector.connect();
             CallableStatement stmt = conn.prepareCall(sql)) {

            String artistId = UUID.randomUUID().toString();

            stmt.setString(1, artistId);
            stmt.setString(2, fullName);
            stmt.setString(3, email);
            stmt.setString(4, password);
            stmt.setString(5, bio);
            stmt.setString(6, profileImage);
            stmt.setBigDecimal(7, new java.math.BigDecimal("0"));

            stmt.execute();
            JOptionPane.showMessageDialog(this, "Artist registered successfully!");
            openLoginPage();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: Could not register artist.");
        }
    }

    private void openLoginPage() {
        LoginPage loginPage = new LoginPage();
        loginPage.setVisible(true);
        this.setVisible(false);
    }

    private void openHomePage() {
        HomePage homePage = new HomePage();
        homePage.setVisible(true);
        this.setVisible(false);
    }

}
