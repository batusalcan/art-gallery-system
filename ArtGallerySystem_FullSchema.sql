-- -------------------------------------------------------------
-- Se2230 Art Gallery System Database
-- Group Members:
-- 1. Demir Demirdöğen -23070006036 
-- 2. Batuhan Salcan - 22070006040 
-- 3. Beril Filibelioğlu - 22070006042 
-- 4. Yağmur Pazı - 23070006066 
-- -------------------------------------------------------------

CREATE DATABASE `artgallerysystem` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE artgallerysystem;

-- -------------------------------------------------------------
-- TABLES
-- -------------------------------------------------------------

-- 1. Customer
CREATE TABLE Customer (
  CustomerId CHAR(36)      NOT NULL,
  FullName   VARCHAR(200)  NOT NULL,
  Email      VARCHAR(255)  NOT NULL,
  Password   VARCHAR(255)  NOT NULL,
  Address    VARCHAR(500)  NULL,
  CreatedAt  DATETIME      NOT NULL,
  CONSTRAINT Customer_CustomerId_pk PRIMARY KEY (CustomerId),
  CONSTRAINT Customer_Email_uk      UNIQUE (Email)
);

-- 2. Artist
CREATE TABLE Artist (
  ArtistId      CHAR(36)     NOT NULL,
  FullName      VARCHAR(200) NOT NULL,
  Email         VARCHAR(255) NOT NULL,
  Password      VARCHAR(255) NOT NULL,
  Bio           TEXT         NULL,
  ProfileImgUrl VARCHAR(500) NULL,
  ArtistRate    DECIMAL(3,2) NULL,
  CreatedAt     DATETIME     NOT NULL,
  CONSTRAINT Artist_ArtistId_pk PRIMARY KEY (ArtistId),
  CONSTRAINT Artist_Email_uk    UNIQUE (Email)
);

-- 3. Artwork
CREATE TABLE Artwork (
  ArtworkId    CHAR(36)      NOT NULL,
  ArtistId     CHAR(36)      NOT NULL,
  Title        VARCHAR(200)  NOT NULL,
  Descp        TEXT          NULL,
  BasePrice    DECIMAL(18,2) NOT NULL,
  Category     VARCHAR(100)  NULL,
  Status       VARCHAR(50)   NOT NULL,
  IsOpenToSale TINYINT(1)    NOT NULL,
  CreatedAt    DATETIME      NOT NULL,
  CONSTRAINT Artwork_ArtworkId_pk PRIMARY KEY (ArtworkId),
  CONSTRAINT Artwork_ArtistId_fk  FOREIGN KEY (ArtistId)
    REFERENCES Artist (ArtistId)
);

-- 4. Countdown
CREATE TABLE Countdown (
  ArtworkId CHAR(36)   NOT NULL,
  EndTime   DATETIME   NOT NULL,
  IsRunning TINYINT(1) NOT NULL,
  CONSTRAINT Countdown_ArtworkId_pk PRIMARY KEY (ArtworkId),
  CONSTRAINT Countdown_ArtworkId_fk FOREIGN KEY (ArtworkId)
    REFERENCES Artwork (ArtworkId)
);

-- 5. ArtworkImages
CREATE TABLE ArtworkImages (
  ImageId   CHAR(36)     NOT NULL,
  ArtworkId CHAR(36)     NOT NULL,
  ImageUrl  VARCHAR(500) NOT NULL,
  CONSTRAINT ArtworkImages_ImageId_pk   PRIMARY KEY (ImageId),
  CONSTRAINT ArtworkImages_ArtworkId_fk FOREIGN KEY (ArtworkId)
    REFERENCES Artwork (ArtworkId)
);

-- 6. Favorites
CREATE TABLE Favorites (
  CustomerId  CHAR(36) NOT NULL,
  ArtworkId   CHAR(36) NOT NULL,
  FavoritedAt DATETIME NOT NULL,
  CONSTRAINT Favorites_pk             PRIMARY KEY (CustomerId, ArtworkId),
  CONSTRAINT Favorites_CustomerId_fk  FOREIGN KEY (CustomerId)
    REFERENCES Customer (CustomerId),
  CONSTRAINT Favorites_ArtworkId_fk   FOREIGN KEY (ArtworkId)
    REFERENCES Artwork (ArtworkId)
);

-- 7. Rate (Ratings)
CREATE TABLE Rate (
  CustomerId  CHAR(36) NOT NULL,
  ArtworkId   CHAR(36) NOT NULL,
  RatingValue TINYINT  NOT NULL,
  RatedAt     DATETIME NOT NULL,
  CONSTRAINT Rate_pk             PRIMARY KEY (CustomerId, ArtworkId),
  CONSTRAINT Rate_CustomerId_fk  FOREIGN KEY (CustomerId)
    REFERENCES Customer (CustomerId),
  CONSTRAINT Rate_ArtworkId_fk   FOREIGN KEY (ArtworkId)
    REFERENCES Artwork (ArtworkId)
);

-- 8. Offer
CREATE TABLE Offer (
  OfferId     CHAR(36)      NOT NULL,
  CustomerId  CHAR(36)      NOT NULL,
  ArtworkId   CHAR(36)      NOT NULL,
  Amount      DECIMAL(18,2) NOT NULL,
  OfferStatus VARCHAR(50)   NOT NULL,
  OfferTime   DATETIME      NOT NULL,
  minIncrease DECIMAL(18,2) NOT NULL,
  CONSTRAINT Offer_OfferId_pk     PRIMARY KEY (OfferId),
  CONSTRAINT Offer_CustomerId_fk  FOREIGN KEY (CustomerId)
    REFERENCES Customer (CustomerId),
  CONSTRAINT Offer_ArtworkId_fk   FOREIGN KEY (ArtworkId)
    REFERENCES Artwork (ArtworkId)
);

-- 9. Sales
CREATE TABLE Sales (
  SaleId  CHAR(36) NOT NULL,
  OfferId CHAR(36) NOT NULL,
  SoldAt  DATETIME NOT NULL,
  CONSTRAINT Sales_SaleId_pk   PRIMARY KEY (SaleId),
  CONSTRAINT Sales_OfferId_fk  FOREIGN KEY (OfferId)
    REFERENCES Offer (OfferId)
);

-- 10. Shipment
CREATE TABLE Shipment (
  TrackId     CHAR(36)    NOT NULL,
  SaleId      CHAR(36)    NOT NULL,
  Status      VARCHAR(50) NOT NULL,
  DeliveredAt DATETIME    NULL,
  CONSTRAINT Shipment_TrackId_pk PRIMARY KEY (TrackId),
  CONSTRAINT Shipment_SaleId_fk  FOREIGN KEY (SaleId)
    REFERENCES Sales (SaleId)
);

-- 11. Mentors
CREATE TABLE Mentors (
  ArtistId CHAR(36) NOT NULL,
  MentorId CHAR(36) NOT NULL,
  CONSTRAINT Mentors_pk             PRIMARY KEY (ArtistId, MentorId),
  CONSTRAINT Mentors_ArtistId_fk    FOREIGN KEY (ArtistId)
    REFERENCES Artist (ArtistId),
  CONSTRAINT Mentors_MentorId_fk    FOREIGN KEY (MentorId)
    REFERENCES Artist (ArtistId)
);

-- -------------------------------------------------------------
-- TRIGGERS
-- -------------------------------------------------------------

DELIMITER //

CREATE TRIGGER trg_cascade_delete_artwork
BEFORE DELETE ON Artwork
FOR EACH ROW
BEGIN
  -- 1. Countdown
  DELETE FROM Countdown WHERE ArtworkId = OLD.ArtworkId;

  -- 2. ArtworkImages
  DELETE FROM ArtworkImages WHERE ArtworkId = OLD.ArtworkId;

  -- 3. Favorites
  DELETE FROM Favorites WHERE ArtworkId = OLD.ArtworkId;

  -- 4. Rate
  DELETE FROM Rate WHERE ArtworkId = OLD.ArtworkId;

  -- 5. Offer
  DELETE FROM Offer WHERE ArtworkId = OLD.ArtworkId;

  DELETE FROM Sales
  WHERE OfferId IN (
    SELECT OfferId FROM Offer WHERE ArtworkId = OLD.ArtworkId
  );

  DELETE FROM Shipment
  WHERE SaleId IN (
    SELECT SaleId FROM Sales
    WHERE OfferId IN (
      SELECT OfferId FROM Offer WHERE ArtworkId = OLD.ArtworkId
    )
  );
END;
//

DELIMITER ;


-- Countdown Trigger 
DELIMITER //

CREATE TRIGGER trg_after_artwork_insert_countdown
AFTER INSERT ON Artwork
FOR EACH ROW
BEGIN
    DECLARE daysToAdd INT;

    -- Generate a random number between 8 and 20 (inclusive)
    SET daysToAdd = FLOOR(8 + (RAND() * 13));

    -- Insert into Countdown with EndTime as DATETIME
    INSERT INTO Countdown (ArtworkId, EndTime, IsRunning)
    VALUES (
        NEW.ArtworkId,
        DATE_ADD(NOW(), INTERVAL daysToAdd DAY),
        1
    );
END;
//

DELIMITER ;



-- -------------------------------------------------------------
-- PROCEDURES
-- -------------------------------------------------------------

-- SearchArtworksWithCategory
DELIMITER //
CREATE PROCEDURE SearchArtworksWithCategory (
    IN searchTerm VARCHAR(200),
    IN categoryFilter VARCHAR(100)
)
BEGIN
    IF categoryFilter = 'All' THEN
        SELECT * FROM Artwork
        WHERE Title LIKE CONCAT('%', searchTerm, '%')
           OR ArtistId IN (
               SELECT ArtistId FROM Artist WHERE FullName LIKE CONCAT('%', searchTerm, '%')
           );
    ELSE
        SELECT * FROM Artwork
        WHERE (Title LIKE CONCAT('%', searchTerm, '%')
            OR ArtistId IN (
               SELECT ArtistId FROM Artist WHERE FullName LIKE CONCAT('%', searchTerm, '%')
            ))
          AND Category = categoryFilter;
    END IF;
END;
//
DELIMITER ;



-- AddToFavorite
DELIMITER //
CREATE PROCEDURE AddToFavorite (
    IN inCustomerId CHAR(36),
    IN inArtworkId CHAR(36)
)
BEGIN
    INSERT IGNORE INTO Favorites (CustomerId, ArtworkId, FavoritedAt)
    VALUES (inCustomerId, inArtworkId, NOW());
END;
//

DELIMITER ;


-- GetEndedAuctions
DELIMITER //
CREATE PROCEDURE GetEndedAuctions()
BEGIN
    SELECT a.ArtworkId, c.EndTime, a.Status
    FROM Artwork a
    JOIN Countdown c ON a.ArtworkId = c.ArtworkId
    WHERE c.EndTime < NOW() AND a.Status = 'open_to_sale';
END;
//
DELIMITER ;


-- UpdateArtworkStatus
DELIMITER //
CREATE PROCEDURE UpdateArtworkStatus (
    IN inArtworkId CHAR(36),
    IN inStatus VARCHAR(50)
)
BEGIN
    UPDATE Artwork
    SET Status = inStatus
    WHERE ArtworkId = inArtworkId;
END; // 
DELIMITER ;



-- SaveOrUpdateRating
DELIMITER //
CREATE PROCEDURE SaveOrUpdateRating (
    IN inCustomerId CHAR(36),
    IN inArtworkId CHAR(36),
    IN inRatingValue TINYINT
)
BEGIN
    INSERT INTO Rate (CustomerId, ArtworkId, RatingValue, RatedAt)
    VALUES (inCustomerId, inArtworkId, inRatingValue, NOW())
    ON DUPLICATE KEY UPDATE
        RatingValue = VALUES(RatingValue),
        RatedAt = VALUES(RatedAt);
END;
//
DELIMITER ;


-- GetMentorInfoByArtistId
DELIMITER //
CREATE PROCEDURE GetMentorInfoByArtistId (
    IN inArtistId CHAR(36)
)
BEGIN
    SELECT m.MentorId, a.FullName
    FROM Mentors m
    JOIN Artist a ON m.MentorId = a.ArtistId
    WHERE m.ArtistId = inArtistId;
END;
//
DELIMITER ;

-- create sale 
DELIMITER //
CREATE PROCEDURE CreateSale (
    IN inOfferId CHAR(36)
)
BEGIN
    INSERT INTO Sales (SaleId, OfferId, SoldAt)
    VALUES (UUID(), inOfferId, NOW());
END;
//
DELIMITER ;


-- CreateShipmentForSaleId
DELIMITER //

CREATE PROCEDURE CreateShipmentForSaleId (
    IN inSaleId CHAR(36)
)
BEGIN
    INSERT INTO Shipment (TrackId, SaleId, Status, DeliveredAt)
    VALUES (UUID(), inSaleId, 'processing', NULL);
END; 
//
DELIMITER ;


-- GetFavoritesByCustomer 
 DELIMITER //
CREATE PROCEDURE GetFavoritesByCustomer (
    IN inCustomerId CHAR(36)
)
BEGIN
    SELECT ArtworkId, Title, BasePrice, Category, Status, ArtistName
    FROM FavoriteView
    WHERE CustomerId = inCustomerId
    ORDER BY FavoritedAt DESC;
END;
//
DELIMITER ;


-- RemoveFavorite
DELIMITER //
CREATE PROCEDURE RemoveFavorite (
    IN inCustomerId CHAR(36),
    IN inArtworkId CHAR(36)
)
BEGIN
    DELETE FROM Favorites
    WHERE CustomerId = inCustomerId AND ArtworkId = inArtworkId;
END;
//
DELIMITER ;


-- LoginUser -> Artist or customer 
DELIMITER $$
CREATE  PROCEDURE `LoginUser`(
    IN p_Email VARCHAR(255),
    IN p_Password VARCHAR(255),
    OUT p_UserType VARCHAR(10),
    OUT p_UserId CHAR(36)
)
BEGIN
    SET p_UserType = NULL;
    SET p_UserId = NULL;

    -- Check Customer
    SELECT CustomerId INTO p_UserId
    FROM Customer
    WHERE Email = p_Email AND Password = p_Password
    LIMIT 1;

    IF p_UserId IS NOT NULL THEN
        SET p_UserType = 'Customer';
    ELSE
        -- Check Artist if not Customer
        SELECT ArtistId INTO p_UserId
        FROM Artist
        WHERE Email = p_Email AND Password = p_Password
        LIMIT 1;

        IF p_UserId IS NOT NULL THEN
            SET p_UserType = 'Artist';
        END IF;
    END IF;
END$$
DELIMITER ;


-- RegisterArtist 
DELIMITER $$
CREATE  PROCEDURE `RegisterArtist`(
     p_ArtistId CHAR(36),
     p_FullName VARCHAR(255),
     p_Email VARCHAR(255),
     p_Password VARCHAR(255),
     p_Bio TEXT,
     p_ProfileImgUrl VARCHAR(255),
     p_ArtistRate DECIMAL(3,2)
)
BEGIN
    INSERT INTO Artist (
        ArtistId, FullName, Email, Password, Bio, ProfileImgUrl, ArtistRate, CreatedAt
    )
    VALUES (
        p_ArtistId, p_FullName, p_Email, p_Password, p_Bio, p_ProfileImgUrl, p_ArtistRate, NOW()
    );
END$$
DELIMITER ;


-- RegisterCustomer
DELIMITER $$
CREATE  PROCEDURE `RegisterCustomer`(
    IN p_CustomerId CHAR(36),
    IN p_FullName VARCHAR(255),
    IN p_Email VARCHAR(255),
    IN p_Password VARCHAR(255),
    IN p_Address VARCHAR(255)
)
BEGIN
    INSERT INTO Customer (CustomerId, FullName, Email, Password, Address, CreatedAt)
    VALUES (p_CustomerId, p_FullName, p_Email, p_Password, p_Address, NOW());
END$$
DELIMITER ;


-- GetOfferHistoryForArtwork
DELIMITER //

CREATE PROCEDURE GetOfferHistoryForArtwork (
    IN inArtworkId CHAR(36)
)
BEGIN
    SELECT Amount, FullName, OfferTime
    FROM OfferHistoryView
    WHERE ArtworkId = inArtworkId
    ORDER BY OfferTime DESC;
END;
//

DELIMITER ;


-- UpdateArtworkDetails
DELIMITER //
CREATE PROCEDURE UpdateArtworkDetails (
    IN inArtworkId CHAR(36),
    IN inTitle VARCHAR(200),
    IN inBasePrice DECIMAL(18,2),
    IN inCategory VARCHAR(100),
    IN inStatus VARCHAR(50)
)
BEGIN
    UPDATE Artwork
    SET Title = inTitle,
        BasePrice = inBasePrice,
        Category = inCategory,
        Status = inStatus
    WHERE ArtworkId = inArtworkId;
END;
//

DELIMITER ;

-- -------------------------------------------------------------
-- FUNCTIONS
-- -------------------------------------------------------------


-- GetEndTimeForArtwork
CREATE FUNCTION GetEndTimeForArtwork(p_artworkId CHAR(36))
RETURNS DATETIME
DETERMINISTIC
READS SQL DATA
RETURN (
    SELECT MAX(EndTime)          
    FROM   Countdown
    WHERE  ArtworkId = p_artworkId
);

-- GetArtworkAverageRating
CREATE FUNCTION GetArtworkAverageRating(p_artworkId CHAR(36))
RETURNS DECIMAL(3,2)
DETERMINISTIC
READS SQL DATA
RETURN (
    SELECT AVG(RatingValue)
    FROM   Rate
    WHERE  ArtworkId = p_artworkId
);


-- GetHighestOfferOrBase
DELIMITER //
CREATE FUNCTION GetHighestOfferOrBase (artworkId CHAR(36))
RETURNS DECIMAL(18,2)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE maxOffer DECIMAL(18,2);

    SELECT MAX(Amount) INTO maxOffer FROM Offer WHERE ArtworkId = artworkId;

    IF maxOffer IS NOT NULL THEN
        RETURN maxOffer;
    ELSE
        RETURN (
            SELECT BasePrice FROM Artwork WHERE ArtworkId = artworkId
        );
    END IF;
END // 
DELIMITER ;


-- GetBasePrice
CREATE FUNCTION GetBasePrice (p_artworkId CHAR(36))
RETURNS DECIMAL(18,2)
DETERMINISTIC
READS SQL DATA
RETURN (
    SELECT BasePrice
    FROM Artwork
    WHERE ArtworkId = p_artworkId
);

-- GetArtistNameById
CREATE FUNCTION GetArtistNameById (artistId CHAR(36))
RETURNS VARCHAR(200)
DETERMINISTIC
READS SQL DATA
RETURN (
    SELECT MAX(FullName)
    FROM Artist
    WHERE ArtistId = artistId
);	

-- GetHighestOfferId 
CREATE FUNCTION GetHighestOfferId (artworkId CHAR(36))
RETURNS CHAR(36)
DETERMINISTIC
READS SQL DATA
RETURN (
    SELECT MIN(OfferId)
    FROM Offer
    WHERE ArtworkId = artworkId
      AND Amount = (
          SELECT MAX(Amount)
          FROM Offer
          WHERE ArtworkId = artworkId
      )
);

-- -------------------------------------------------------------
-- VIEWS
-- -------------------------------------------------------------

-- Favorites View 
CREATE VIEW FavoriteView AS
SELECT 
    F.CustomerId,
    A.ArtworkId,
    A.Title,
    A.BasePrice,
    A.Category,
    A.Status,
    AR.FullName AS ArtistName,
    F.FavoritedAt
FROM Favorites F
JOIN Artwork A ON F.ArtworkId = A.ArtworkId
JOIN Artist AR ON A.ArtistId = AR.ArtistId;


-- OfferHistoryView
CREATE  VIEW OfferHistoryView AS
SELECT 
    o.ArtworkId,
    o.Amount,
    c.FullName,
    o.OfferTime
FROM Offer o
JOIN Customer c ON o.CustomerId = c.CustomerId;

-- Customer Purchase History View
CREATE  VIEW CustomerPurchaseHistoryView AS
SELECT 
    s.SaleId, 
    s.SoldAt, 
    o.Amount, 
    a.Title, 
    sh.Status AS ShipmentStatus, 
    sh.DeliveredAt, 
    o.CustomerId
FROM Sales s
JOIN Offer o ON s.OfferId = o.OfferId
JOIN Artwork a ON o.ArtworkId = a.ArtworkId
LEFT JOIN Shipment sh ON s.SaleId = sh.SaleId;

-- -------------------------------------------------------------
-- DATAS
-- -------------------------------------------------------------

-- Insert Artists
INSERT INTO Artist (ArtistId, FullName, Email, Password, Bio, ProfileImgUrl, ArtistRate, CreatedAt) VALUES
(UUID(), 'Elif Karaman', 'elif.karaman@artgallery.com', 'elif1234', 'Focuses on emotional storytelling through color.', 'resources/images/ArtistProfiles/Elif_Karaman.png', 4.60, NOW()),
(UUID(), 'Zeynep Durmaz', 'zeynep.durmaz@artgallery.com', 'zeynep99', 'Acrylic painter focusing on cultural themes.', 'resources/images/ArtistProfiles/Zeynep_Durmaz.png', 4.80, NOW()),
(UUID(), 'Mira Kowalska', 'mira.kowalska@artgallery.com', 'miraPolska!', 'Polish mixed media artist using recycled materials.', 'resources/images/ArtistProfiles/Mira_Kowalska.png', 4.41, NOW()),
(UUID(), 'Mert Yalçın', 'mert.yalcin@artgallery.com', 'merty123', 'An artist focuses on portraying isolation.', 'resources/images/artworks/Mert_Yalcin', 4.45, NOW()),
(UUID(), 'Batu Salcan', 'batu.salcan@artgallery.com', 'batu123', 'An artist focuses on his dog.', 'resources/images/ArtistProfiles/Batu_Salcan.png', 4.70, NOW()),
(UUID(), 'Kerem Öztürk', 'kerem.ozturk@artgallery.com', 'kerem2025', 'An artist combining realism with abstraction.', 'resources/images/ArtistProfiles/Kerem_Ozturk.png', 4.50, NOW()),
(UUID(), 'Bora Kılıç', 'bora.kilic@artgallery.com', 'boraK321', 'An artist focuses on landscapes.', 'resources/images/ArtistProfiles/Bora_Kilic.png', 4.35, NOW()),
(UUID(), 'Jasper Nguyen', 'jasper.nguyen@artgallery.com', 'jasperN9', 'Vietnamese-American artist using digital collage.', 'resources/images/ArtistProfiles/Jasper_Nyugen.png', 4.68, NOW());


-- Artworks
INSERT INTO Artwork (ArtworkId, ArtistId, Title, Descp, BasePrice, Category, Status, IsOpenToSale, CreatedAt) VALUES
-- Elif Karaman (1)
(UUID(), (SELECT ArtistId FROM Artist WHERE FullName = 'Elif Karaman'), 'Whispers', 'Soft-toned exploration of silence.', 800.00, 'Figure', 'open_to_sale', 1, NOW()),

-- Batu Salcan (2)
(UUID(), (SELECT ArtistId FROM Artist WHERE FullName = 'Batu Salcan'), 'Dog Days', 'Acrylic scenes of a dog’s daily life.', 600.00, 'Figure', 'open_to_sale', 1, NOW()),
(UUID(), (SELECT ArtistId FROM Artist WHERE FullName = 'Batu Salcan'), 'Loyal Gaze', 'Realistic portrait of his dog.', 1000.00, 'Figure', 'open_to_sale', 1, NOW()),

-- Mira Kowalska (1)
(UUID(), (SELECT ArtistId FROM Artist WHERE FullName = 'Mira Kowalska'), 'Glass Memory', 'Mixed media on recycled glass.', 1150.00, 'Portrait', 'open_to_sale', 1, NOW()),

-- Zeynep Durmaz (2)
(UUID(), (SELECT ArtistId FROM Artist WHERE FullName = 'Zeynep Durmaz'), 'Zeybek', 'Series of Ottoman-era inspired portraits.', 870.00, 'Genre', 'open_to_sale', 1, NOW()),
(UUID(), (SELECT ArtistId FROM Artist WHERE FullName = 'Zeynep Durmaz'), 'Hintli Aile', 'Culture through layered textures.', 950.00, 'Genre', 'open_to_sale', 1, NOW()),

-- Kerem Öztürk (1)
(UUID(), (SELECT ArtistId FROM Artist WHERE FullName = 'Kerem Öztürk'), 'Urban Static', 'Graffiti and realism fusion.', 780.00, 'Figurative', 'open_to_sale', 1, NOW()),

-- Bora Kılıç (2)
(UUID(), (SELECT ArtistId FROM Artist WHERE FullName = 'Bora Kılıç'), 'Horizon', 'Landscape with soft oil blending.', 690.00, 'Landscape', 'open_to_sale', 1, NOW()),
(UUID(), (SELECT ArtistId FROM Artist WHERE FullName = 'Bora Kılıç'), 'Calm Fields', 'Pastel colors over rolling hills.', 720.00, 'Landscape', 'open_to_sale', 1, NOW()),

-- Jasper Nguyen (1)
(UUID(), (SELECT ArtistId FROM Artist WHERE FullName = 'Jasper Nguyen'), 'Digital Fracture', 'Collage of modern tech decay.', 930.00, 'Schematic', 'open_to_sale', 1, NOW()),

-- Mert Yalçın (1)
(UUID(), (SELECT ArtistId FROM Artist WHERE FullName = 'Mert Yalçın'), 'Gray Isolation', 'Cold urban color palette.', 760.00, 'Figurative', 'open_to_sale', 1, NOW()),
(UUID(), (SELECT ArtistId FROM Artist WHERE FullName = 'Mert Yalçın'), 'Loneliness', 'Cold urban color palette.', 740.00, 'Figurative', 'open_to_sale', 1, NOW());

-- Insert Images

select a1.ArtworkId, ar1.FullName,a1.Title from artist ar1 join artwork a1 on a1.ArtistId = ar1.ArtistId;

select * from offer;

-- Doh Days - slide1
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Batu Salcan/BatuSalcan-Artwork1-Slide1.JPG'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Batu Salcan'
  AND A.Title = 'Dog Days'
LIMIT 1;


-- Dog Days - slide 2
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Batu Salcan/BatuSalcan-Artwork1-Slide2.png'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Batu Salcan'
  AND A.Title = 'Dog Days'
LIMIT 1;


-- Barkscape - slide-1
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Batu Salcan/BatuSalcan-Artwork2-Slide2.png'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Batu Salcan'
  AND A.Title = 'Barkscape'
LIMIT 1;

-- Barkscape - slide-2
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Batu Salcan/BatuSalcan-Artwork2-Slide2.png'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Batu Salcan'
  AND A.Title = 'Barkscape'
LIMIT 1;


-- Loyal Gaze - slide -1 
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Batu Salcan/BatuSalcan-Artwork3-Slide1.png'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Batu Salcan'
  AND A.Title = 'Loyal Gaze'
LIMIT 1;

-- Loyal Gaze - slide -2
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Batu Salcan/BatuSalcan-Artwork3-Slide2.png'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Batu Salcan'
  AND A.Title = 'Loyal Gaze'
LIMIT 1;


-- Horizon - slide-1
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Bora Kilic/BoraKilic-Artwork1-Slide1.png'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Bora Kılıç'
  AND A.Title = 'Horizon'
LIMIT 1;

-- Green Horizon - slide-2
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Bora Kilic/BoraKilic-Artwork1-Slide2.png'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Bora Kılıç'
  AND A.Title = 'Horizon'
LIMIT 1;


-- Calm Fields - slide-1
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Bora Kilic/BoraKilic-Artwork2-Slide1.png'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Bora Kılıç'
  AND A.Title = 'Calm Fields'
LIMIT 1;

-- Calm Fields - slide-2
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Bora Kilic/BoraKilic-Artwork2-Slide2.png'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Bora Kılıç'
  AND A.Title = 'Calm Fields'
LIMIT 1;

-- Whispers - slide-1
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Elif Karaman/ElifKaraman-Artwork1-Slide1.png'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Elif Karaman'
  AND A.Title = 'Whispers'
LIMIT 1;


-- Whispers - slide-2
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Elif Karaman/ElifKaraman-Artwork1-Slide2.png'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Elif Karaman'
  AND A.Title = 'Whispers'
LIMIT 1;

-- Inner Child - slide -1 
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Elif Karaman/ElifKaraman-Artwork2-Slide1.png'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Elif Karaman'
  AND A.Title = 'Inner Child'
LIMIT 1;

-- Inner Child - slide -2
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Elif Karaman/ElifKaraman-Artwork2-Slide2.png'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Elif Karaman'
  AND A.Title = 'Inner Child'
LIMIT 1;

-- Glass Memory - slide -1
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Mira Kowalska/MiraKowalska-Artwork1-Slide1.jpg'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Mira Kowalska'
  AND A.Title = 'Glass Memory'
LIMIT 1;

-- Glass Memory - slide -2
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Mira Kowalska/MiraKowalska-Artwork2-Slide1.jpg'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Mira Kowalska'
  AND A.Title = 'Glass Memory'
LIMIT 1;

-- Zeybek -slide -1
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Zeynep Durmaz/ZeynepDurmaz-Artwork1-Slide1.png'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Zeynep Durmaz'
  AND A.Title = 'Zeybek'
LIMIT 1;

-- Zeybek -slide -2
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Zeynep Durmaz/ZeynepDurmaz-Artwork1-Slide2.png'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Zeynep Durmaz'
  AND A.Title = 'Zeybek'
LIMIT 1;

-- Hintli Aile -slide -1
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Zeynep Durmaz/ZeynepDurmaz-Artwork2-Slide1.png'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Zeynep Durmaz'
  AND A.Title = 'Hintli Aile'
LIMIT 1;

-- Hintli Aile -slide -2
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Zeynep Durmaz/ZeynepDurmaz-Artwork2-Slide2.png'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Zeynep Durmaz'
  AND A.Title = 'Hintli Aile'
LIMIT 1;

-- Urban static -slide -1
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Kerem Ozturk/KeremOzturk-Artwork1-Slide2.png'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Kerem Öztürk'
  AND A.Title = 'Urban Static'
LIMIT 1;

-- Urban static -slide -2
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Kerem Ozturk/KeremOzturk-Artwork2-Slide2.png'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Kerem Öztürk'
  AND A.Title = 'Urban Static'
LIMIT 1;

-- Digital Fracture -slide -1
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Jasper Nyugen/JasperNguyen-Artwork1-Slide1.jpeg'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Jasper Nguyen'
  AND A.Title = 'Digital Fracture'
LIMIT 1;

-- Digital Fracture -slide -2
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Jasper Nyugen/JasperNguyen-Artwork1-Slide2.jpeg'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Jasper Nguyen'
  AND A.Title = 'Digital Fracture'
LIMIT 1;

-- Gray Isolation -slide -1
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Mert Yalcin/MertYalcin-Artwork1-Slide1.png'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Mert Yalçın'
  AND A.Title = 'Gray Isolation'
LIMIT 1;

-- Gray Isolation -slide -2
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Mert Yalcin/MertYalcin-Artwork1-Slide2.png'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Mert Yalçın'
  AND A.Title = 'Gray Isolation'
LIMIT 1;


-- Loneliness slide -1
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Mert Yalcin/MertYalcin-Artwork2-Slide1.png'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Mert Yalçın'
  AND A.Title = 'Loneliness'
LIMIT 1;

-- Loneliness slide -2
INSERT INTO ArtworkImages (ImageId, ArtworkId, ImageUrl)
SELECT UUID(), A.ArtworkId, 'resources/images/artworks/Mert Yalcin/MertYalcin-Artwork2-Slide2.png'
FROM Artwork A
JOIN Artist AR ON A.ArtistId = AR.ArtistId
WHERE AR.FullName = 'Mert Yalçın'
  AND A.Title = 'Loneliness'
LIMIT 1;




-- Mentors

INSERT INTO Mentors (ArtistId, MentorId)
VALUES (
    (SELECT ArtistId FROM Artist WHERE FullName = 'Batu Salcan'),
    (SELECT ArtistId FROM Artist WHERE FullName = 'Kerem Öztürk')
);

INSERT INTO Mentors (ArtistId, MentorId)
VALUES (
    (SELECT ArtistId FROM Artist WHERE FullName = 'Jasper Nguyen'),
    (SELECT ArtistId FROM Artist WHERE FullName = 'Kerem Öztürk')
);

INSERT INTO Mentors (ArtistId, MentorId)
VALUES (
    (SELECT ArtistId FROM Artist WHERE FullName = 'Elif Karaman'),
    (SELECT ArtistId FROM Artist WHERE FullName = 'Zeynep Durmaz')
);



select * from mentors;


-- add customers

INSERT INTO Customer (CustomerId, FullName, Email, Password, Address, CreatedAt)
VALUES 
(UUID(), 'Yağmur Pazi', 'yagmur.pazi@example.com', 'yagmur123', 'İzmir, Türkiye', NOW()),
(UUID(), 'Beril Filibelioğlu', 'beril.filibe@example.com', 'beril456', 'Ankara, Türkiye', NOW());


-- add favorites
INSERT INTO Favorites (CustomerId, ArtworkId, FavoritedAt)
VALUES 
(
  (SELECT CustomerId FROM Customer WHERE FullName = 'Yağmur Pazi'),
  (SELECT ArtworkId FROM Artwork WHERE Title = 'Whispers'),
  NOW()
),
(
  (SELECT CustomerId FROM Customer WHERE FullName = 'Yağmur Pazi'),
  (SELECT ArtworkId FROM Artwork WHERE Title = 'Gray Isolation'),
  NOW()
);

INSERT INTO Favorites (CustomerId, ArtworkId, FavoritedAt)
VALUES (
  (SELECT CustomerId FROM Customer WHERE FullName = 'Yağmur Pazi'),
  (SELECT ArtworkId FROM Artwork WHERE Title = 'Glass Memory'),
  NOW()
);

INSERT INTO Favorites (CustomerId, ArtworkId, FavoritedAt)
VALUES (
  (SELECT CustomerId FROM Customer WHERE FullName = 'Yağmur Pazi'),
  (SELECT ArtworkId FROM Artwork WHERE Title = 'Calm Fields'),
  NOW()
);


-- add offer
INSERT INTO Offer (OfferId, CustomerId, ArtworkId, Amount, OfferStatus, OfferTime, minIncrease)
VALUES (
  UUID(),
  (SELECT CustomerId FROM Customer WHERE FullName = 'Yağmur Pazi'),
  (SELECT ArtworkId FROM Artwork WHERE Title = 'Digital Fracture'),
  950.00,
  'pending',
  NOW(),
  10.00
);

-- 
UPDATE Countdown
SET EndTime = NOW() + INTERVAL 5 MINUTE
WHERE ArtworkId = (
    SELECT ArtworkId FROM Artwork WHERE Title = 'Digital Fracture'
);




