package app.ui.windows;

import app.data.DBConnector;
import app.models.CurrentUser;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RateArtworkWindow {

    public static void showRatingDialog(JFrame parentFrame, String artworkId, String artworkTitle) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(320, 180));
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Rate: " + artworkTitle);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel instructionLabel = new JLabel("Please select a rating between 1 and 5:");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        Integer[] ratings = {1, 2, 3, 4, 5};
        JComboBox<Integer> ratingCombo = new JComboBox<>(ratings);
        ratingCombo.setMaximumSize(new Dimension(60, 30));
        ratingCombo.setFont(new Font("Arial", Font.BOLD, 16));
        ratingCombo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton exitButton = new JButton("Cancel");
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setBackground(Color.LIGHT_GRAY);
        exitButton.setForeground(Color.DARK_GRAY);
        exitButton.setFocusPainted(false);
        exitButton.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(panel);
            if (window != null) window.dispose();
        });

        panel.add(Box.createVerticalStrut(10));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(instructionLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(ratingCombo);
        panel.add(Box.createVerticalStrut(20));

        int result = JOptionPane.showOptionDialog(
                parentFrame,
                panel,
                "Rate this Artwork",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[]{"Submit", "Exit"},
                "Submit"
        );

        if(CurrentUser.currentUser != null){
            if (result == JOptionPane.OK_OPTION) {
                int selectedRating = (Integer) ratingCombo.getSelectedItem();
                saveRating(artworkId, selectedRating);
            }
        }else{
            JOptionPane.showMessageDialog(
                    parentFrame,
                    "You must be logged in to rate artworks.",
                    "Login Required",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private static void saveRating(String artworkId, int ratingValue) {
        String sql = "CALL SaveOrUpdateRating(?, ?, ?)";

        try (Connection conn = DBConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, CurrentUser.currentUser);
            stmt.setString(2, artworkId);
            stmt.setInt(3, ratingValue);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(null, "Your rating has been submitted!", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred while submitting your rating.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}