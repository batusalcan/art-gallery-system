# Se2230- Project: Art Gallery System

### Group Members
- Demir Demirdöğen - 23070006036
- Batuhan Salcan - 22070006040
- Beril Filibelioğlu - 22070006042
- Yağmur Pazı - 23070006066
---

> **Note:** The detailed database schema is provided below.
---
> **Note:** Test data is provided in `src/app/AppRun.java` as command-line.
---
### Technologies Used: Java Swing + MySQL

**This project is SQL-focused**. Most of the logic—such as artwork management, bidding, rating, and offer validation—is executed directly from the UI using **structured SQL operations**. These include:

- **Stored Procedures** for complex business rules
- **User-defined Functions** for reusable SQL logic
- **Views** to simplify query layers
- **Triggers** to automate tasks like countdown creation

---

### Our Development Environment
- **Java JDK 17+**
- **IntelliJ IDEA Ultimate**
- **MySQL 8.0+**
- **MySQL Workbench**

---

###  External JARs (Already Added in `/lib`)
Make sure to include the following libraries:

| JAR File                      | Purpose                                   |
|-------------------------------|-------------------------------------------|
| `mysql-connector-j-9.3.0.jar` | MySQL JDBC driver                         |
| `flatlaf-demo-3.6.jar`        | FlatLaf UI theme for modern styling       |

### How to Add as Library in IntelliJ:
1. Right-click the `lib` folder → **"Add as Library..."**
2. Ensure **Project Level** is selected.
3. Click OK.

---

### ️ Database Configuration

- Create a database named: `artgallerysystem`
- Import the provided SQL schema (includes tables like `Artwork`, `Customer`, `Offer`, `Rate`, `Countdown`, `Sales`, etc.)

Modify the `DBConnector.java` class with your database credentials:
```java
String url = "jdbc:mysql://localhost:3306/artgallerysystem";
String username = "root";
String password = "your_password";
```
---

## Project Structure


### `src/app/data/`

| Class              | Purpose                                                                 |
|--------------------|-------------------------------------------------------------------------|
| `DBConnector.java` | Manages MySQL connection using JDBC. Used globally for DB access.       |

---

###  `src/app/models/`

| Class               | Purpose                                                                 |
|---------------------|-------------------------------------------------------------------------|
| `Artwork.java`      | Models an artwork: title, artist ID, category, price, status, etc.      |
| `CurrentUser.java`  | Holds session info for logged-in user (either Customer or Artist).      |

---

###  `src/app/ui/pages/` – Main Application Screens

| Class                      | Purpose                                                                 |
|----------------------------|-------------------------------------------------------------------------|
| `HomePage.java`            | Welcome page + navigation to other features.                           |
| `LoginPage.java`           | Handles user login (Customer or Artist) using credentials.              |
| `RegisterPage.java`        | Creates new users (inserts into Customer/Artist tables).                |
| `ArtworksPage.java`        | Displays all available artworks with filter/search and countdowns.      |
| `FavoritesPage.java`       | Lists artworks favorited by the current customer.                       |
| `CustomerOrdersPage.java`  | Shows artworks purchased by the customer after bidding ends.            |
| `ManageArtworksPage.java`  | Allows artists to manage their own artworks (view, edit, delete).       |
| `ArtistProfilePage.java`   | Shows artist bio and profile when clicking on an artwork.               |

---

###  `src/app/ui/windows/` – Dialog/Popup Windows

| Class                     | Purpose                                                                 |
|---------------------------|-------------------------------------------------------------------------|
| `MakeOfferWindow.java`    | Opens a modal for placing a bid (INSERT into `Offer`).                 |
| `RateArtworkWindow.java`  | Popup window for submitting a rating (1–5 stars) for an artwork.        |
| `OfferHistoryWindow.java` | Displays the history of bids for a selected artwork.                   |

---

###  `resources/images/`

- Stores artwork and artist images used in the UI display.

---

###  `AppRun.java`

- Application's main entry point. Starts the Swing UI by launching the login screen.


---

## Page Responsibilities & SQL Operations

| UI Page / Window            | Description / Actions                                                                 | Example SQL Operations Used                                                       |
|----------------------------|----------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------|
| **LoginPage**              | Customer/Artist login                                                                 | `CALL LoginUser(?, ?, ?, ?)`                                                      |
| **RegisterPage**           | Register new users (Customer/Artist)                                                  | `CALL RegisterCustomer(?, ?, ?, ?, ?)` & `CALL RegisterArtist(?, ?, ?, ?, ?, ?, ?)` |
| **HomePage**               | Welcome page + navigation                                                              | –                                                                                 |
| **ArtworksPage**           | Lists all available artworks, search, filter, view countdown                          | `CALL SearchArtworksWithCategory(?, ?)`                                           |
| **FavoritesPage**          | Lists artworks added to favorites by the user                                         | `CALL GetFavoritesByCustomer(?)`                                                  |
| **ManageArtworksPage**     | Artists view & manage their own artworks                                              | `DELETE FROM Artwork WHERE ArtworkId = ?`                                         |
| **CustomerOrdersPage**     | Customers view artworks they won after bidding                                        | `SELECT * FROM CustomerOrderView`                                                 |
| **ArtistProfilePage**      | Show artist details when an artwork is clicked                                        | `CALL GetMentorInfoByArtistId(?)`                                          |
| **MakeOfferWindow**        | Allows customers to place bids during countdown                                       | `INSERT INTO Offer (...) VALUES (...)`                                            |
| **RateArtworkWindow**      | Submit artwork ratings                                                                | `CALL SaveOrUpdateRating(?, ?, ?)`                   |
| **OfferHistoryWindow**     | Show past offers for a selected artwork                                               | `CALL GetOfferHistoryForArtwork(?)`                 |

 > Note:Each row above includes only one sample SQL query for the corresponding UI component. In reality, many of these classes involve multiple SQL operations such as `UPDATE`, `DELETE`, nested `SELECT`, and complex JOIN queries depending on the actions performed by the user.</sub>

---

## Database Schema

### Customer
| Column     | Type         | Constraints               |
|------------|--------------|---------------------------|
| CustomerId | CHAR(36)     | PRIMARY KEY, NOT NULL     |
| FullName   | VARCHAR(200) | NOT NULL                  |
| Email      | VARCHAR(255) | NOT NULL, UNIQUE          |
| Password   | VARCHAR(255) | NOT NULL                  |
| Address    | VARCHAR(500) |                           |
| CreatedAt  | DATETIME     | NOT NULL                  |

### Artist
| Column        | Type          | Constraints               |
|---------------|---------------|---------------------------|
| ArtistId      | CHAR(36)      | PRIMARY KEY, NOT NULL     |
| FullName      | VARCHAR(200)  | NOT NULL                  |
| Email         | VARCHAR(255)  | NOT NULL, UNIQUE          |
| Password      | VARCHAR(255)  | NOT NULL                  |
| Bio           | TEXT          |                           |
| ProfileImgUrl | VARCHAR(500)  |                           |
| ArtistRate    | DECIMAL(3,2)  |                           |
| CreatedAt     | DATETIME      | NOT NULL                  |

### Artwork
| Column       | Type           | Constraints                                              |
|--------------|----------------|----------------------------------------------------------|
| ArtworkId    | CHAR(36)       | PRIMARY KEY, NOT NULL                                    |
| ArtistId     | CHAR(36)       | NOT NULL, FOREIGN KEY → Artist(ArtistId)                 |
| Title        | VARCHAR(200)   | NOT NULL                                                 |
| Descp        | TEXT           |                                                          |
| BasePrice    | DECIMAL(18,2)  | NOT NULL                                                 |
| Category     | VARCHAR(100)   |                                                          |
| Status       | VARCHAR(50)    | NOT NULL                                                 |
| IsOpenToSale | TINYINT(1)     | NOT NULL                                                 |
| CreatedAt    | DATETIME       | NOT NULL                                                 |

### Countdown
| Column     | Type       | Constraints                                         |
|------------|------------|-----------------------------------------------------|
| ArtworkId  | CHAR(36)   | PRIMARY KEY, NOT NULL, FOREIGN KEY → Artwork(ArtworkId) |
| EndTime    | DATETIME   | NOT NULL                                            |
| IsRunning  | TINYINT(1) | NOT NULL                                            |

### ArtworkImages
| Column     | Type          | Constraints                                         |
|------------|---------------|-----------------------------------------------------|
| ImageId    | CHAR(36)      | PRIMARY KEY, NOT NULL                               |
| ArtworkId  | CHAR(36)      | NOT NULL, FOREIGN KEY → Artwork(ArtworkId)          |
| ImageUrl   | VARCHAR(500)  | NOT NULL                                            |

### Favorites
| Column      | Type       | Constraints                                                       |
|-------------|------------|-------------------------------------------------------------------|
| CustomerId  | CHAR(36)   | PRIMARY KEY (with ArtworkId), NOT NULL, FOREIGN KEY → Customer(CustomerId) |
| ArtworkId   | CHAR(36)   | PRIMARY KEY (with CustomerId), NOT NULL, FOREIGN KEY → Artwork(ArtworkId)   |
| FavoritedAt | DATETIME   | NOT NULL                                                          |

### Rate
| Column      | Type     | Constraints                                                       |
|-------------|----------|-------------------------------------------------------------------|
| CustomerId  | CHAR(36) | PRIMARY KEY (with ArtworkId), NOT NULL, FOREIGN KEY → Customer(CustomerId) |
| ArtworkId   | CHAR(36) | PRIMARY KEY (with CustomerId), NOT NULL, FOREIGN KEY → Artwork(ArtworkId)   |
| RatingValue | TINYINT  | NOT NULL                                                          |
| RatedAt     | DATETIME | NOT NULL                                                          |

### Offer
| Column      | Type           | Constraints                                                       |
|-------------|----------------|-------------------------------------------------------------------|
| OfferId     | CHAR(36)       | PRIMARY KEY, NOT NULL                                             |
| CustomerId  | CHAR(36)       | NOT NULL, FOREIGN KEY → Customer(CustomerId)                      |
| ArtworkId   | CHAR(36)       | NOT NULL, FOREIGN KEY → Artwork(ArtworkId)                        |
| Amount      | DECIMAL(18,2)  | NOT NULL                                                          |
| OfferStatus | VARCHAR(50)    | NOT NULL                                                          |
| OfferTime   | DATETIME       | NOT NULL                                                          |
| minIncrease | DECIMAL(18,2)  | NOT NULL                                                          |

### Sales
| Column  | Type     | Constraints                                              |
|---------|----------|----------------------------------------------------------|
| SaleId  | CHAR(36) | PRIMARY KEY, NOT NULL                                    |
| OfferId | CHAR(36) | NOT NULL, FOREIGN KEY → Offer(OfferId)                   |
| SoldAt  | DATETIME | NOT NULL                                                 |

### Shipment
| Column      | Type        | Constraints                                              |
|-------------|-------------|----------------------------------------------------------|
| TrackId     | CHAR(36)    | PRIMARY KEY, NOT NULL                                    |
| SaleId      | CHAR(36)    | NOT NULL, FOREIGN KEY → Sales(SaleId)                    |
| Status      | VARCHAR(50) | NOT NULL                                                 |
| DeliveredAt | DATETIME    |                                                          |

### Mentors
| Column    | Type     | Constraints                                                      |
|-----------|----------|------------------------------------------------------------------|
| ArtistId  | CHAR(36) | PRIMARY KEY (with MentorId), NOT NULL, FOREIGN KEY → Artist(ArtistId) |
| MentorId  | CHAR(36) | PRIMARY KEY (with ArtistId), NOT NULL, FOREIGN KEY → Artist(ArtistId) |

---



