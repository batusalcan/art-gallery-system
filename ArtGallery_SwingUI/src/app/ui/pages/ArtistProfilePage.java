package app.ui.pages;

import app.data.DBConnector;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.sql.*;

public class ArtistProfilePage extends JFrame {

    private JPanel artworkPanel;
    private final String artistId;

    public ArtistProfilePage(String artistId) {
        this.artistId = artistId;

        setTitle("Artist Profile");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);


        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(new Color(72, 144, 239));
        navBar.setPreferredSize(new Dimension(1000, 50));

        JLabel titleLabel = new JLabel("  Artist Profile");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        navBar.add(titleLabel, BorderLayout.WEST);

        JButton homeBtn = new JButton("Return to Home Page");
        homeBtn.setFont(new Font("Arial", Font.BOLD, 14));
        homeBtn.setBackground(new Color(113, 186, 28));
        homeBtn.setForeground(Color.WHITE);
        homeBtn.setFocusPainted(false);
        homeBtn.setPreferredSize(new Dimension(200, 40));
        homeBtn.addActionListener(e -> {
            dispose();
            new HomePage().setVisible(true);
        });
        navBar.add(homeBtn, BorderLayout.EAST);
        mainPanel.add(navBar, BorderLayout.NORTH);

        // Artist Info
        JPanel artistInfoContainer = new JPanel();
        artistInfoContainer.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        artistInfoContainer.setBackground(new Color(245, 245, 245));
        artistInfoContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 40, 20));

        JLabel profilePic = new JLabel();
        profilePic.setPreferredSize(new Dimension(160, 90));
        profilePic.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(245, 245, 245));

        JLabel nameLabel = new JLabel();
        nameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        JLabel emailLabel = new JLabel();
        JLabel bioLabel = new JLabel();
        JLabel rateLabel = new JLabel();
        JLabel mentorLabel = new JLabel();
        mentorLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        mentorLabel.setForeground(new Color(33, 150, 243));
        mentorLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        try (Connection conn = DBConnector.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Artist WHERE ArtistId = ?")) {

            stmt.setString(1, artistId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nameLabel.setText("Name: " + rs.getString("FullName"));
                emailLabel.setText("Email: " + rs.getString("Email"));
                bioLabel.setText("Bio: " + rs.getString("Bio"));
                rateLabel.setText("Rating: ★ " + rs.getDouble("ArtistRate"));

                String imgUrl = rs.getString("ProfileImgUrl");
                if (imgUrl != null && !imgUrl.isEmpty()) {
                    URL imgURL = getClass().getClassLoader().getResource(imgUrl);
                    if (imgURL != null) {
                        ImageIcon icon = new ImageIcon(imgURL);
                        Image img = icon.getImage().getScaledInstance(160, 90, Image.SCALE_SMOOTH);
                        profilePic.setIcon(new ImageIcon(img));
                    } else {
                        profilePic.setText("Image not found");
                    }
                } else {
                    profilePic.setText("No Image");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // mentor info
        boolean hasMentor = false;
        try (Connection conn = DBConnector.connect();
             PreparedStatement stmt = conn.prepareStatement("CALL GetMentorInfoByArtistId(?)")) {

            stmt.setString(1, artistId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String mentorName = rs.getString("FullName");
                String mentorId = rs.getString("MentorId");
                mentorLabel.setText("Mentor: " + mentorName);
                mentorLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        new ArtistProfilePage(mentorId).setVisible(true);
                    }
                });
                hasMentor = true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (!hasMentor) {
            mentorLabel.setText("Mentor: No mentor assigned");
        }


        infoPanel.add(nameLabel);
        infoPanel.add(emailLabel);
        infoPanel.add(bioLabel);
        infoPanel.add(rateLabel);
        infoPanel.add(mentorLabel);

        artistInfoContainer.add(profilePic);
        artistInfoContainer.add(infoPanel);

        // Artwork list
        artworkPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        artworkPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        artworkPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(artworkPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel centerContent = new JPanel(new BorderLayout());
        centerContent.setBackground(Color.WHITE);
        centerContent.add(artistInfoContainer, BorderLayout.NORTH);
        centerContent.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(centerContent, BorderLayout.CENTER);
        add(mainPanel);

        loadArtworks();
    }

    private void loadArtworks() {
        artworkPanel.removeAll();
        String sql = "SELECT * FROM Artwork WHERE ArtistId = ?";

        try (Connection conn = DBConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, artistId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String title = rs.getString("Title");
                double price = rs.getDouble("BasePrice");
                String category = rs.getString("Category");

                JPanel card = new JPanel();
                card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                card.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                card.setBackground(Color.WHITE);
                card.setPreferredSize(new Dimension(200, 120));
                card.setMaximumSize(new Dimension(200, 120));
                card.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel titleLabel = new JLabel("Title: " + title);
                titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                JLabel priceLabel = new JLabel("Base Price: ₺" + price);
                priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                JLabel categoryLabel = new JLabel("Category: " + category);
                categoryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                card.add(Box.createVerticalGlue());
                card.add(titleLabel);
                card.add(priceLabel);
                card.add(categoryLabel);
                card.add(Box.createVerticalGlue());

                artworkPanel.add(card);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


        artworkPanel.revalidate();
        artworkPanel.repaint();
    }

}
