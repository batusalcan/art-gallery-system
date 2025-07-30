package app.ui.pages;

import app.models.CurrentUser;
import app.data.DBConnector;
import app.models.Artwork;
import app.ui.windows.MakeOfferWindow;
import app.ui.windows.OfferHistoryWindow;
import app.ui.windows.RateArtworkWindow;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.sql.Timestamp;

public class ArtworksPage extends JFrame {

    private JTextField searchField;
    private JComboBox<String> categoryComboBox;
    private JPanel artworkGridPanel;
    private Timer countdownTimer;

    public ArtworksPage() {
        setTitle("Artworks");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.white);

        JPanel navBarPanel = new JPanel(new BorderLayout());
        navBarPanel.setBackground(new Color(72, 144, 239));
        navBarPanel.setPreferredSize(new Dimension(1000, 50));

        JLabel artGalleryLabel = new JLabel("  Art Gallery");
        artGalleryLabel.setFont(new Font("Serif", Font.BOLD, 20));
        artGalleryLabel.setForeground(Color.WHITE);
        navBarPanel.add(artGalleryLabel, BorderLayout.WEST);

        JButton returnHomeButton = new JButton("Return to Home Page");
        returnHomeButton.setFont(new Font("Arial", Font.BOLD, 16));
        returnHomeButton.setBackground(new Color(113, 186, 28));
        returnHomeButton.setForeground(Color.WHITE);
        returnHomeButton.setFocusPainted(false);
        returnHomeButton.setPreferredSize(new Dimension(200, 50));
        returnHomeButton.addActionListener(e -> returnToHomePage());
        navBarPanel.add(returnHomeButton, BorderLayout.EAST);

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        headerPanel.setBackground(Color.white);

        JLabel searchLabel = new JLabel("Search Artworks:");
        headerPanel.add(searchLabel);

        searchField = new JTextField(20);
        headerPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> loadArtworks());
        headerPanel.add(searchButton);

        JLabel categoryLabel = new JLabel("Category:");
        headerPanel.add(categoryLabel);

        String[] categories = {"All", "Figure", "Portrait", "Genre", "Figurative","Landscape","Schematic"};
        categoryComboBox = new JComboBox<>(categories);
        headerPanel.add(categoryComboBox);

        artworkGridPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        artworkGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        artworkGridPanel.setBackground(Color.white);

        JScrollPane scrollPane = new JScrollPane(artworkGridPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(new Color(0, 123, 255));
        footerPanel.setPreferredSize(new Dimension(1000, 40));

        JLabel footerLabel = new JLabel("© 2025 Art Gallery | About Us");
        footerLabel.setForeground(Color.WHITE);
        footerPanel.add(footerLabel);

        mainPanel.add(navBarPanel, BorderLayout.PAGE_END);
        mainPanel.add(headerPanel, BorderLayout.PAGE_START);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
        loadArtworks();
    }

    private void loadArtworks() {
        artworkGridPanel.removeAll();
        checkAndFinalizeAuctions();
        String searchQuery = searchField.getText().trim();
        String category = (String) categoryComboBox.getSelectedItem();
        List<Artwork> artworks = getAllArtworks(searchQuery, category);

        for (Artwork artwork : artworks) {
            artworkGridPanel.add(createArtworkCard(artwork));
        }

        repaint();
        revalidate();
    }

    private List<Artwork> getAllArtworks(String searchQuery, String category) {
        List<Artwork> artworks = new ArrayList<>();
        String sql = "CALL SearchArtworksWithCategory(?, ?)";

        try (Connection conn = DBConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, searchQuery);
            stmt.setString(2, category);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String artworkId = rs.getString("ArtworkId");
                String title = rs.getString("Title");
                String artistName = getArtistNameById(rs.getString("ArtistId"));
                double currentPrice = getHighestOffer(artworkId);
                double rating = getRatingForArtwork(artworkId);
                List<String> imageUrls = getImageUrlsForArtwork(artworkId);
                String artworkCategory = rs.getString("Category");
                Timestamp endTime = getEndTimeForArtwork(artworkId);

                artworks.add(new Artwork(
                        artworkId, title, artistName, currentPrice, rating, currentPrice,
                        imageUrls, artworkCategory, endTime
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return artworks;
    }


    private double getHighestOffer(String artworkId) {
        String sql = "SELECT MAX(Amount) AS MaxOffer FROM Offer WHERE ArtworkId = ?";
        try (Connection conn = DBConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, artworkId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double offer = rs.getDouble("MaxOffer");
                return offer > 0 ? offer : getBasePrice(artworkId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return getBasePrice(artworkId);

    }

    private double getBasePrice(String artworkId) {
        String sql = "SELECT GetBasePrice(?) AS BasePrice";
        try (Connection conn = DBConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, artworkId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("BasePrice");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
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

    private String getArtistNameById(String artistId) {
        try (Connection conn = DBConnector.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT FullName FROM Artist WHERE ArtistId = ?")) {
            stmt.setString(1, artistId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("FullName");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    private double getRatingForArtwork(String artworkId) {
        String sql = "SELECT GetArtworkAverageRating(?) AS avgRating";

        try (Connection conn = DBConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, artworkId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double rating = rs.getDouble("avgRating");
                    return rs.wasNull() ? 0.0 : rating;   // NULL -> 0.0
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }


    private Timestamp getEndTimeForArtwork(String artworkId) {
        String sql = "SELECT GetEndTimeForArtwork(?) AS EndTime";

        try (Connection conn = DBConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, artworkId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp("EndTime");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void checkAndFinalizeAuctions() {
        String sql = "CALL GetEndedAuctions()";

        try (Connection conn = DBConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String artworkId = rs.getString("ArtworkId");

                String offerSql = "SELECT GetHighestOfferId(?) AS OfferId";
                try (PreparedStatement offerStmt = conn.prepareStatement(offerSql)) {
                    offerStmt.setString(1, artworkId);
                    ResultSet offerRs = offerStmt.executeQuery();

                    if (offerRs.next() && offerRs.getString("OfferId") != null) {
                        String offerId = offerRs.getString("OfferId");

                        // 1. Insert into Sales using procedure
                        String saleSql = "CALL CreateSale(?)";
                        try (PreparedStatement saleStmt = conn.prepareStatement(saleSql)) {
                            saleStmt.setString(1, offerId);
                            saleStmt.executeUpdate();
                        }

                        // 2. Retrieve SaleId based on OfferId
                        String getSaleIdSql = "SELECT SaleId FROM Sales WHERE OfferId = ? ORDER BY SoldAt DESC LIMIT 1";
                        String saleId = null;
                        try (PreparedStatement saleIdStmt = conn.prepareStatement(getSaleIdSql)) {
                            saleIdStmt.setString(1, offerId);
                            ResultSet saleIdRs = saleIdStmt.executeQuery();
                            if (saleIdRs.next()) {
                                saleId = saleIdRs.getString("SaleId");
                            }
                        }

                        // 3. Insert into Shipment using procedure
                        if (saleId != null) {
                            String shipmentSql = "CALL CreateShipmentForSaleId(?)";
                            try (PreparedStatement shipmentStmt = conn.prepareStatement(shipmentSql)) {
                                shipmentStmt.setString(1, saleId);
                                shipmentStmt.executeUpdate();
                            }
                        }

                        // 4. Update Artwork Status to 'sold'
                        try (PreparedStatement updateStmt = conn.prepareStatement("CALL UpdateArtworkStatus(?, ?)")) {
                            updateStmt.setString(1, artworkId);
                            updateStmt.setString(2, "sold");
                            updateStmt.executeUpdate();
                        }

                    } else {
                        // No offer: update artwork status to 'close_to_sale'
                        try (PreparedStatement updateStmt = conn.prepareStatement("CALL UpdateArtworkStatus(?, ?)")) {
                            updateStmt.setString(1, artworkId);
                            updateStmt.setString(2, "close_to_sale");
                            updateStmt.executeUpdate();
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }





    private JPanel createImageSlider(List<String> imagePaths) {
        JPanel sliderPanel = new JPanel(new BorderLayout());
        JLabel imageLabel = new JLabel("", JLabel.CENTER);
        sliderPanel.setPreferredSize(new Dimension(260, 180));

        if (imagePaths == null || imagePaths.isEmpty()) {
            imageLabel.setText("No Image Available");
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
        URL imgURL = getClass().getClassLoader().getResource(imagePath);
        if (imgURL != null) {
            ImageIcon newIcon = new ImageIcon(imgURL);
            Image newImage = newIcon.getImage().getScaledInstance(260, 180, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(newImage));
        } else {
            imageLabel.setText("Image not found");
        }
    }

    private JPanel createArtworkCard(Artwork artwork) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setPreferredSize(new Dimension(300, 450));

        card.add(createImageSlider(artwork.getImageURL()));
        card.add(Box.createVerticalStrut(10));

        JLabel titleLabel = new JLabel("<html><b>" + artwork.getTitle() + "</b></html>", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel artistLabel = new JLabel("Artist: " + artwork.getArtistName(), SwingConstants.CENTER);
        artistLabel.setForeground(new Color(33, 150, 243));
        artistLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        artistLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        artistLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        artistLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                try (Connection conn = DBConnector.connect();
                     PreparedStatement stmt = conn.prepareStatement("SELECT ArtistId FROM Artwork WHERE ArtworkId = ?")) {
                    stmt.setString(1, artwork.getArtworkId());
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        String artistId = rs.getString("ArtistId");
                        new ArtistProfilePage(artistId).setVisible(true);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Unable to open artist profile.");
                }
            }
        });

        JLabel priceLabel = new JLabel("Current Price: ₺" + artwork.getCurrentPrice());
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel ratingLabel = new JLabel("Rating: " + String.format("%.1f", artwork.getRating()) + "/5");
        ratingLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        ratingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel categoryLabel = new JLabel("Category: " + artwork.getCategory());
        categoryLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        categoryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel countdownLabel = new JLabel();
        countdownLabel.setForeground(Color.RED);
        countdownLabel.setFont(new Font("Arial", Font.BOLD, 14));
        countdownLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (artwork.getEndTime() != null) {
            countdownTimer = new Timer(1000, e -> {
                long remainingTime = artwork.getEndTime().getTime() - System.currentTimeMillis();
                long days = remainingTime / (1000 * 60 * 60 * 24);
                long hours = (remainingTime / (1000 * 60 * 60)) % 24;
                long minutes = (remainingTime / (1000 * 60)) % 60;
                long seconds = (remainingTime / 1000) % 60;

                if (remainingTime > 0) {
                    countdownLabel.setText("Time left: " + days + "d " + hours + "h " + minutes + "m " + seconds + "s");
                } else {
                    countdownLabel.setText("Auction Ended & Closed to Sale");
                    countdownTimer.stop();
                }
            });
            countdownTimer.start();
        } else {
            countdownLabel.setText("Auction Ended & Closed to Sale");
        }

        JButton favButton = new JButton("Add to Favorites");
        JButton offerButton = new JButton("Make an Offer");
        JButton rateButton = new JButton("Rate this Artwork");
        JButton historyButton = new JButton("View Bid History");

        for (JButton btn : new JButton[]{favButton, offerButton, rateButton, historyButton}) {
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(200, 30));
        }

        favButton.addActionListener(e -> {
            if (CurrentUser.currentUser == null) {
                JOptionPane.showMessageDialog(card, "You must be logged in as a customer to favorite artworks.");
                return;
            }
            String sql = "CALL AddToFavorite(?, ?)";
            try (Connection conn = DBConnector.connect();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, CurrentUser.currentUser);
                stmt.setString(2, artwork.getArtworkId());
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(card, "Artwork added to favorites.");
                } else {
                    JOptionPane.showMessageDialog(card, "This artwork is already in your favorites.");
                }
                loadArtworks();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(card, "An error occurred while adding to favorites.");
            }
        });

        historyButton.addActionListener(e -> OfferHistoryWindow.showBidHistoryPopup(artwork.getArtworkId(), artwork.getTitle()));

        offerButton.addActionListener(e -> {
            if (CurrentUser.currentUser == null) {
                JOptionPane.showMessageDialog(card, "You must be logged in as a customer to make an offer.");
                return;
            }
            MakeOfferWindow.showOfferPopup(artwork.getArtworkId());
            loadArtworks();
        });

        rateButton.addActionListener(e -> RateArtworkWindow.showRatingDialog(this, artwork.getArtworkId(), artwork.getTitle()));

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(artistLabel);
        card.add(priceLabel);
        card.add(ratingLabel);
        card.add(categoryLabel);
        card.add(countdownLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(favButton);
        card.add(offerButton);
        card.add(rateButton);
        card.add(historyButton);

        return card;
    }

    private void returnToHomePage() {
        HomePage homePage = new HomePage();
        homePage.setVisible(true);
        this.setVisible(false);
    }

}