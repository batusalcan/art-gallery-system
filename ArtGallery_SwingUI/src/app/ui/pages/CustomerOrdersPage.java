package app.ui.pages;

import app.data.DBConnector;
import app.models.CurrentUser;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerOrdersPage extends JFrame {

    public CustomerOrdersPage() {
        setTitle("My Orders");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // NavBar
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(new Color(72, 144, 239));
        navBar.setPreferredSize(new Dimension(1000, 50));

        JLabel titleLabel = new JLabel("  My Orders");
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

        JPanel ordersPanel = new JPanel();
        ordersPanel.setLayout(new BoxLayout(ordersPanel, BoxLayout.Y_AXIS));
        ordersPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(ordersPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        boolean hasOrders = false;

        try (Connection conn = DBConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT SaleId, SoldAt, Amount, Title, ShipmentStatus, DeliveredAt " +
                             "FROM CustomerPurchaseHistoryView " +
                             "WHERE CustomerId = ? ORDER BY SoldAt DESC")) {

            stmt.setString(1, CurrentUser.currentUser);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                hasOrders = true;

                String artworkTitle = rs.getString("Title");
                String saleId = rs.getString("SaleId");
                double amount = rs.getDouble("Amount");
                String soldAt = rs.getString("SoldAt");
                String status = rs.getString("ShipmentStatus") != null ? rs.getString("ShipmentStatus") : "Pending";
                String deliveredAt = rs.getString("DeliveredAt") != null ? rs.getString("DeliveredAt") : "Not Delivered Yet";

                JPanel card = new JPanel(new GridLayout(3, 2, 10, 5));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200)),
                        BorderFactory.createEmptyBorder(10, 15, 10, 15)));
                card.setBackground(new Color(250, 250, 250));
                card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

                card.add(new JLabel("Artwork Title: " + artworkTitle));
                card.add(new JLabel("Sale ID: " + saleId));
                card.add(new JLabel("Sold At: " + soldAt));
                card.add(new JLabel("Amount Paid: ₺" + amount));
                card.add(new JLabel("Shipment Status: " + status));
                card.add(new JLabel("Delivered At: " + deliveredAt));

                ordersPanel.add(card);
                ordersPanel.add(Box.createVerticalStrut(10));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading orders.");
        }


        if (!hasOrders) {
            JLabel noOrdersLabel = new JLabel("You have no orders.");
            noOrdersLabel.setFont(new Font("Arial", Font.BOLD, 18));
            noOrdersLabel.setForeground(Color.GRAY);
            noOrdersLabel.setHorizontalAlignment(SwingConstants.CENTER);
            ordersPanel.add(Box.createVerticalStrut(50));
            ordersPanel.add(noOrdersLabel);
        }

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);
    }

}