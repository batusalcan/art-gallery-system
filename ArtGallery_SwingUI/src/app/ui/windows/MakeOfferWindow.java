package app.ui.windows;

import app.data.DBConnector;
import app.models.CurrentUser;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class MakeOfferWindow {

    public static void showOfferPopup(String artworkId) {
        if (CurrentUser.currentUser == null) {
            JOptionPane.showMessageDialog(null, "You must be logged in as a customer to make an offer.");
            return;
        }

        double highestOffer = getHighestOffer(artworkId);
        if (highestOffer <= 0) {
            JOptionPane.showMessageDialog(null, "Invalid artwork or price.");
            return;
        }

        double minIncrease = highestOffer * 0.05;
        double minOffer = highestOffer + minIncrease;

        JTextField offerField = new JTextField();
        JLabel infoLabel = new JLabel("Minimum offer: ₺" + String.format("%.2f", minOffer));

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(infoLabel);
        panel.add(new JLabel("Your Offer:"));
        panel.add(offerField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Make an Offer",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                double enteredAmount = Double.parseDouble(offerField.getText());
                if (enteredAmount < minOffer) {
                    JOptionPane.showMessageDialog(null, "Offer must be at least ₺" + String.format("%.2f", minOffer));
                    return;
                }

                insertOffer(artworkId, enteredAmount, minIncrease);
                JOptionPane.showMessageDialog(null, "Your offer was submitted successfully.");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Please enter a valid number.");
            }
        }
    }

    private static double getHighestOffer(String artworkId) {
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

    private static double getBasePrice(String artworkId) {
        String sql = "SELECT BasePrice FROM Artwork WHERE ArtworkId = ?";
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
        return 0;
    }

    private static void insertOffer(String artworkId, double amount, double minIncrease) {
        String sql = "INSERT INTO Offer (OfferId, CustomerId, ArtworkId, Amount, OfferStatus, OfferTime, minIncrease) " +
                "VALUES (?, ?, ?, ?, 'Pending', ?, ?)";

        try (Connection conn = DBConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, CurrentUser.currentUser);
            stmt.setString(3, artworkId);
            stmt.setDouble(4, amount);
            stmt.setString(5, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            stmt.setDouble(6, minIncrease);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to submit offer: " + e.getMessage());
        }
    }
}
