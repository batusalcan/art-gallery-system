package app.ui.windows;

import app.data.DBConnector;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OfferHistoryWindow {

    public static void showBidHistoryPopup(String artworkId, String artworkTitle) {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        StringBuilder history = new StringBuilder();
        history.append("=== Offer History ===\n");
        history.append("Artwork: ").append(artworkTitle).append("\n");
        history.append("-----------------------------\n\n");

        String sql = "CALL GetOfferHistoryForArtwork(?)";

        try (Connection conn = DBConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, artworkId);
            ResultSet rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                double amount = rs.getDouble("Amount");
                String fullName = rs.getString("FullName");
                String time = rs.getString("OfferTime");

                history.append(String.format("%2d. ₺%-10.2f | %-20s | %s\n",
                        count, amount, fullName, time));
            }

            if (count == 0) {
                history.append("No offers yet.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            history.append("An error occurred while fetching offer history.");
        }

        textArea.setText(history.toString());

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 350));

        JOptionPane.showMessageDialog(null, scrollPane, "Offer History", JOptionPane.INFORMATION_MESSAGE);
    }
}
