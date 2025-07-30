package app.ui.pages;

import app.data.DBConnector;
import app.models.CurrentUser;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoritesPage extends JFrame {

    private JPanel cardsPanel;
    private JPanel container;

    public FavoritesPage() {
        setTitle("My Favorite Artworks");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        container = new JPanel(new BorderLayout());

        JLabel title = new JLabel("My Favorite Artworks", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        container.add(title, BorderLayout.NORTH);

        cardsPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        cardsPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        container.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomNav = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomNav.setPreferredSize(new Dimension(1200, 60));
        bottomNav.setBackground(Color.WHITE);

        JButton homeButton = new JButton("Return to Home Page");
        homeButton.setFont(new Font("Arial", Font.BOLD, 16));
        homeButton.setBackground(new Color(113, 186, 28));
        homeButton.setForeground(Color.WHITE);
        homeButton.setFocusPainted(false);
        homeButton.setPreferredSize(new Dimension(200, 40));
        bottomNav.add(homeButton);

        homeButton.addActionListener(e -> {
            HomePage home = new HomePage();
            home.setVisible(true);
            this.dispose();
        });

        container.add(bottomNav, BorderLayout.SOUTH);

        add(container);
        loadFavorites();
    }

    private void loadFavorites() {
        String sql = "CALL GetFavoritesByCustomer(?)";

        try (Connection conn = DBConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, CurrentUser.currentUser);
            ResultSet rs = stmt.executeQuery();

            boolean hasFavorites = false;

            while (rs.next()) {
                hasFavorites = true;
                List<String> imageList = getImageUrlsForArtwork(rs.getString("ArtworkId"));
                JPanel card = createArtworkCard(
                        rs.getString("ArtworkId"),
                        rs.getString("Title"),
                        imageList,
                        rs.getString("ArtistName"),
                        rs.getString("Category")
                );
                cardsPanel.add(card);
            }

            if (!hasFavorites) {
                showEmptyMessage();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showEmptyMessage() {
        cardsPanel.setLayout(new GridBagLayout());

        JPanel emptyPanel = new JPanel();
        emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
        emptyPanel.setBackground(Color.WHITE);

        JLabel emptyLabel = new JLabel("You don't have any favorite artworks yet.");
        emptyLabel.setFont(new Font("Arial", Font.BOLD, 20));
        emptyLabel.setForeground(Color.GRAY);
        emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton seeArtworksButton = new JButton("See Artworks");
        seeArtworksButton.setFont(new Font("Arial", Font.BOLD, 16));
        seeArtworksButton.setBackground(new Color(72, 144, 239));
        seeArtworksButton.setForeground(Color.WHITE);
        seeArtworksButton.setPreferredSize(new Dimension(180, 40));
        seeArtworksButton.setFocusPainted(false);
        seeArtworksButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        seeArtworksButton.addActionListener(e -> {
            ArtworksPage artworksPage = new ArtworksPage();
            artworksPage.setVisible(true);
            this.dispose();
        });

        emptyPanel.add(Box.createVerticalGlue());
        emptyPanel.add(emptyLabel);
        emptyPanel.add(Box.createVerticalStrut(20));
        emptyPanel.add(seeArtworksButton);
        emptyPanel.add(Box.createVerticalGlue());

        cardsPanel.add(emptyPanel);
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private JPanel createArtworkCard(String artworkId, String title, List<String> imageUrls, String artist, String category) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(300, 240));
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        card.setBackground(Color.WHITE);

        card.add(createImageSlider(imageUrls));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel artistLabel = new JLabel("By " + artist);
        artistLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        artistLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel categoryLabel = new JLabel("Category: " + category);
        categoryLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        categoryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton removeBtn = new JButton("Remove from Favorites");
        removeBtn.setBackground(Color.RED);
        removeBtn.setForeground(Color.WHITE);
        removeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeBtn.setMaximumSize(new Dimension(180, 30));
        removeBtn.addActionListener(e -> {
            removeFavorite(CurrentUser.currentUser, artworkId);
            cardsPanel.remove(card);
            cardsPanel.revalidate();
            cardsPanel.repaint();
        });

        card.add(Box.createVerticalStrut(10));
        card.add(titleLabel);
        card.add(artistLabel);
        card.add(categoryLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(removeBtn);

        return card;
    }

    private JPanel createImageSlider(List<String> imagePaths) {
        JPanel sliderPanel = new JPanel(new BorderLayout());
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);

        sliderPanel.setPreferredSize(new Dimension(260, 170));

        if (imagePaths == null || imagePaths.isEmpty()) {
            imageLabel.setText("No Image Available");
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            sliderPanel.add(imageLabel, BorderLayout.CENTER);
            return sliderPanel;
        }

        int[] currentIndex = {0};
        updateImage(imageLabel, imagePaths.get(currentIndex[0]));

        JButton prevButton = new JButton("<");
        JButton nextButton = new JButton(">");

        prevButton.addActionListener(e -> {
            currentIndex[0] = (currentIndex[0] - 1 + imagePaths.size()) % imagePaths.size();
            updateImage(imageLabel, imagePaths.get(currentIndex[0]));
        });

        nextButton.addActionListener(e -> {
            currentIndex[0] = (currentIndex[0] + 1) % imagePaths.size();
            updateImage(imageLabel, imagePaths.get(currentIndex[0]));
        });

        sliderPanel.add(prevButton, BorderLayout.WEST);
        sliderPanel.add(imageLabel, BorderLayout.CENTER);
        sliderPanel.add(nextButton, BorderLayout.EAST);

        return sliderPanel;
    }

    private void updateImage(JLabel imageLabel, String imagePath) {
        try {
            URL imgURL = getClass().getClassLoader().getResource(imagePath);
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                Image img = icon.getImage().getScaledInstance(260, 170, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(img));
            } else {
                imageLabel.setText("Image not found");
            }
        } catch (Exception e) {
            imageLabel.setText("Image Load Error");
        }
    }

    private List<String> getImageUrlsForArtwork(String artworkId) {
        List<String> imageUrls = new ArrayList<>();
        String sql = "SELECT ImageUrl FROM ArtworkImages WHERE ArtworkId = ?";

        try (Connection conn = DBConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, artworkId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                imageUrls.add(rs.getString("ImageUrl"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return imageUrls;
    }

    private void removeFavorite(String customerId, String artworkId) {
        String sql = "CALL RemoveFavorite(?, ?)";
        try (Connection conn = DBConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customerId);
            stmt.setString(2, artworkId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}