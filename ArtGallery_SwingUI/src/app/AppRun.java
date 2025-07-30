package app;
import app.models.CurrentUser;
import app.ui.pages.HomePage;
import javax.swing.*;

public class AppRun {
/*
    Note:
    These test data will be automatically created after running the ArtGallerySystem_FullSchema SQL script.

    Test Artwork Data 1:
        Title: Digital Fracture
        Artist Name: Jasper Nguyen
        Base Price: 930 Tl
        Current Price: 950 Tl (updated because of Yağmur's offer — from 930 Tl to 950 Tl)
        Minimum New Offer: 5% -> means the next offer must be at least 5% higher than the current highest offer
        Countdown: Ends within 5 minutes after running the SQL to create the schema
        Offer History: Yağmur Pazı offered 950 Tl for this artwork

    Test Customer Data 1:
        Email: yagmur.pazi@example.com
        Password: yagmur123
            -> Yağmur has 4 favorite artworks
            -> Yağmur has 1 offer on the "Digital Fracture" artwork
            -> Yağmur rated at least one artwork
            -> When the countdown ends, sale and shipment records are created for the highest offer on the artwork

    Test Artist Data 1:
        Email: batu.salcan@artgallery.com
        Password: batu123
            -> Batu has 2 artworks: "Dog Days" and "Loyal Gaze"
            -> Batu’s default artist rating is 4.70 (rating is dynamic and can change as new artworks are rated)

     ---------------------------------------------------------------------------------------------------------------
       To test the offer → sale → shipment flow, update the countdown for a specific artwork like this:

        Use the following SQL to set a countdown that ends in 5 minutes:
            UPDATE Countdown
            SET EndTime = NOW() + INTERVAL 5 MINUTE
            WHERE ArtworkId = ARTWORK_ID_OR_A_SUBQUERY_THAT_RETURNS_ARTWORK_ID;

        Note:
        After the countdown ends, you must reload the Artworks page to trigger the flow automatically.
*/

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HomePage frame = new HomePage();
            frame.setVisible(true);
        });
    }


}

