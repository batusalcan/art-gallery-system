package app.models;

import java.sql.Timestamp;
import java.util.List;

public class Artwork {

    private String artworkId;
    private String title;
    private String artistName;
    private List<String> imageURL;
    private String artworkCategory;
    private double currentPrice;
    private double rating;
    private double basePrice;
    private Timestamp endTime;

    public Artwork(String artworkId, String title, String artistName, double currentPrice,
                   double rating, double basePrice, List<String> imageURL, String artworkCategory, Timestamp endTime) {
        this.artworkId = artworkId;
        this.title = title;
        this.artistName = artistName;
        this.currentPrice = currentPrice;
        this.rating = rating;
        this.basePrice = basePrice;
        this.imageURL = imageURL;
        this.artworkCategory = artworkCategory;
        this.endTime = endTime;
    }

    // Getter and Setter methods
    public String getArtworkId() {
        return artworkId;
    }

    public void setArtworkId(String artworkId) {
        this.artworkId = artworkId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public List<String> getImageURL() {
        return imageURL;
    }

    public String getCategory() {
        return artworkCategory;
    }

    public Timestamp getEndTime() {
        return endTime;
    }
}
