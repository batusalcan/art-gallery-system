package app.ui.pages;

import app.models.CurrentUser;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public class HomePage extends JFrame {

    public HomePage() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.out.println("flatlaf-demo-3.6.jar must add as library to run");
            e.printStackTrace();
        }

        setTitle("Art Gallery");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.white);

        // Navbar (üst panel)
        JPanel navBarPanel = new JPanel(new BorderLayout());
        navBarPanel.setBackground(new Color(72, 144, 239));
        navBarPanel.setPreferredSize(new Dimension(900, 50));

        JLabel artGalleryLabel = new JLabel("  Art Gallery");
        artGalleryLabel.setFont(new Font("Serif", Font.BOLD, 20));
        artGalleryLabel.setForeground(Color.WHITE);
        navBarPanel.add(artGalleryLabel, BorderLayout.WEST);

        JPanel navButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navButtonPanel.setOpaque(false);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        JButton logoutButton = new JButton("Logout");
        JButton favoritesButton = new JButton("Favorites");
        JButton manageArtworksButton = new JButton("Manage My Artworks");
        JButton myProfileButton = new JButton("My Profile");
        JButton myOrdersButton = new JButton("My Orders");

        myProfileButton.setBackground(new Color(186, 166, 11));
        myProfileButton.setForeground(Color.WHITE);
        myProfileButton.setPreferredSize(new Dimension(120, 30));
        myProfileButton.setFocusPainted(false);
        myProfileButton.addActionListener(e -> openArtistProfilePage());

        myOrdersButton.setBackground(new Color(255, 140, 0));
        myOrdersButton.setForeground(Color.WHITE);
        myOrdersButton.setPreferredSize(new Dimension(120, 30));
        myOrdersButton.setFocusPainted(false);
        myOrdersButton.addActionListener(e -> openOrdersPage());

        favoritesButton.setBackground(new Color(40, 167, 69));
        favoritesButton.setForeground(Color.WHITE);
        favoritesButton.setPreferredSize(new Dimension(100, 30));
        favoritesButton.setFocusPainted(false);
        favoritesButton.addActionListener(e -> openFavoritesPage());

        manageArtworksButton.setBackground(new Color(113, 186, 28));
        manageArtworksButton.setForeground(Color.WHITE);
        manageArtworksButton.setPreferredSize(new Dimension(170, 30));
        manageArtworksButton.setFocusPainted(false);
        manageArtworksButton.addActionListener(e -> openManageArtworksPage());

        if (CurrentUser.currentUser != null || CurrentUser.currentArtist != null) {
            loginButton.setVisible(false);
            registerButton.setVisible(false);

            logoutButton.setBackground(new Color(220, 53, 69));
            logoutButton.setForeground(Color.WHITE);
            logoutButton.setFont(new Font("Arial", Font.BOLD, 12));
            logoutButton.setPreferredSize(new Dimension(90, 30));
            logoutButton.addActionListener(e -> logoutUser());

            if (CurrentUser.currentUser != null) {
                navButtonPanel.add(favoritesButton);
                navButtonPanel.add(myOrdersButton);
            }
            if (CurrentUser.currentArtist != null) {
                navButtonPanel.add(manageArtworksButton);
                navButtonPanel.add(myProfileButton);
            }

            navButtonPanel.add(logoutButton);

        } else {
            loginButton.setBackground(new Color(72, 144, 239));
            loginButton.setForeground(Color.WHITE);
            loginButton.setPreferredSize(new Dimension(90, 30));
            loginButton.addActionListener(e -> openLoginPage());

            registerButton.setBackground(new Color(33, 150, 243));
            registerButton.setForeground(Color.WHITE);
            registerButton.setPreferredSize(new Dimension(90, 30));
            registerButton.addActionListener(e -> openRegisterPage());

            navButtonPanel.add(loginButton);
            navButtonPanel.add(registerButton);
        }

        navBarPanel.add(navButtonPanel, BorderLayout.EAST);

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.white);

        JLabel artGalleryText = new JLabel("Art Gallery");
        artGalleryText.setFont(new Font("Serif", Font.BOLD, 72));
        artGalleryText.setForeground(new Color(16, 14, 14));
        artGalleryText.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(Box.createVerticalStrut(50));
        headerPanel.add(artGalleryText);

        JLabel sloganLabel = new JLabel("Discover the Beauty of Art");
        sloganLabel.setFont(new Font("Serif", Font.BOLD, 36));
        sloganLabel.setForeground(Color.BLACK);
        sloganLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(sloganLabel);

        JButton seeAllArtworksButton = new JButton("See All Artworks");
        seeAllArtworksButton.setFont(new Font("Arial", Font.BOLD, 16));
        seeAllArtworksButton.setBackground(new Color(72, 208, 239));
        seeAllArtworksButton.setForeground(Color.WHITE);
        seeAllArtworksButton.setFocusPainted(false);
        seeAllArtworksButton.setPreferredSize(new Dimension(400, 80));
        seeAllArtworksButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        seeAllArtworksButton.addActionListener(e -> openArtworksPage());

        headerPanel.add(Box.createVerticalStrut(20));
        headerPanel.add(seeAllArtworksButton);

        JLabel projectInfoContent = new JLabel("<html><div style='text-align: center;'>"
                + "<br><b>About the Project<b><br><br>"
                + "Art Gallery is a platform for discovering, rating, and bidding on artworks.<br><br>"
                + "Built by software engineering students at Yaşar University for Se-2230 Project.<br><br>"
                + "Technologies used include <b>Java Swing, MySQL</b><br><br>"
                + "All photographs used were generated by AI.<br><br>"
                + "</div></html>");
        projectInfoContent.setFont(new Font("Arial", Font.PLAIN, 16));
        projectInfoContent.setForeground(Color.DARK_GRAY);
        projectInfoContent.setHorizontalAlignment(SwingConstants.CENTER);
        projectInfoContent.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(Box.createVerticalStrut(40));
        headerPanel.add(projectInfoContent);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(new Color(0, 123, 255));
        footerPanel.setPreferredSize(new Dimension(900, 40));

        JLabel footerLabel = new JLabel(" 2025 Art Gallery | Department of Software Engineering, Yaşar University ");
        footerLabel.setForeground(Color.WHITE);

        footerPanel.add(footerLabel);

        mainPanel.add(navBarPanel, BorderLayout.NORTH);
        mainPanel.add(headerPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void openArtistProfilePage() {
        ArtistProfilePage profilePage = new ArtistProfilePage(CurrentUser.currentArtist);
        profilePage.setVisible(true);
        this.setVisible(false);
    }

    private void openLoginPage() {
        LoginPage loginPage = new LoginPage();
        loginPage.setVisible(true);
        this.setVisible(false);
    }

    private void openRegisterPage() {
        RegisterPage registerPage = new RegisterPage();
        registerPage.setVisible(true);
        this.setVisible(false);
    }

    private void logoutUser() {
        CurrentUser.currentUser = null;
        CurrentUser.currentArtist = null;
        JOptionPane.showMessageDialog(this, "Logged out successfully!");
        dispose();
        HomePage homePage = new HomePage();
        homePage.setVisible(true);
    }

    private void openArtworksPage() {
        ArtworksPage artworksPage = new ArtworksPage();
        artworksPage.setVisible(true);
        this.setVisible(false);
    }

    private void openFavoritesPage() {
        FavoritesPage favoritesPage = new FavoritesPage();
        favoritesPage.setVisible(true);
        this.setVisible(false);
    }

    private void openManageArtworksPage() {
        ManageArtworksPage managePage = new ManageArtworksPage();
        managePage.setVisible(true);
        this.setVisible(false);
    }

    private void openOrdersPage() {
        CustomerOrdersPage ordersPage = new CustomerOrdersPage();
        ordersPage.setVisible(true);
        this.setVisible(false);
    }

}