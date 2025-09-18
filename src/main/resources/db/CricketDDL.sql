DROP DATABASE IF EXISTS Cricket;
CREATE DATABASE Cricket;
USE Cricket;

-- lookup table for .CricketMatch, to allow number of days to tie to a format, populated with T20, ODI and Test
CREATE TABLE Cricket.MatchFormat
(
    FormatName VARCHAR(10) PRIMARY KEY,
    MatchLengthDays INT NOT NULL
);
INSERT INTO Cricket.MatchFormat(FormatName, MatchLengthDays) VALUES
("Test", 5),
("ODI", 1),
("T20", 1);

CREATE TABLE Cricket.Team
(
    TeamID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE Cricket.Venue
(
    VenueID INT AUTO_INCREMENT PRIMARY KEY,
    GroundName VARCHAR(80) NOT NULL,
    City VARCHAR(50) NOT NULL,
    UNIQUE (GroundName, City)
);

-- assign table for all venues that are a home ground for each team
CREATE TABLE Cricket.TeamHomeVenue
(
    TeamHomeVenueID INT AUTO_INCREMENT PRIMARY KEY,
    TeamID INT NOT NULL,
    VenueID INT NOT NULL,
    FOREIGN KEY (TeamID) REFERENCES Cricket.Team(TeamID),
    FOREIGN KEY (VenueID) REFERENCES Cricket.Venue(VenueID)
);

-- MATCH is a reserved sql keyword
CREATE TABLE Cricket.CricketMatch
(
    MatchID VARCHAR(40) NOT NULL,
    DataSource ENUM('CRICSHEET', 'CRICAPI') NOT NULL,
    FormatName VARCHAR(10) NOT NULL,
    VenueID INT NOT NULL,
    StartDate DATE NOT NULL,
    PRIMARY KEY (MatchID, DataSource),
    FOREIGN KEY (FormatName) REFERENCES Cricket.MatchFormat(FormatName),
    FOREIGN KEY (VenueID) REFERENCES Cricket.Venue(VenueID)
);

CREATE TABLE Cricket.MatchResult
(
    MatchID VARCHAR(40),
    DataSource ENUM('CRICSHEET', 'CRICAPI') NOT NULL,
    WinningTeamID INT, -- if draw or no result then nullable
    TossWinningTeamID INT NOT NULL,
    TossDecision ENUM('Bat', 'Field') NOT NULL,
    Result ENUM('Win', 'Draw', 'No Result', 'Tie', 'Win In Bowl Off', 'Win In Super Over'),
    MarginSize INT, -- how many runs/wickets won by
    MarginType ENUM('Wickets', 'Runs', 'One Innings And Runs', 'Unknown'),
    PRIMARY KEY (MatchID, DataSource),
    FOREIGN KEY (MatchID, DataSource) REFERENCES Cricket.CricketMatch(MatchID, DataSource),
    FOREIGN KEY (WinningTeamID) REFERENCES Cricket.Team(TeamID)
);

-- assign table for match and team tables
CREATE TABLE Cricket.MatchTeam
(
    MatchTeamID INT AUTO_INCREMENT PRIMARY KEY,
    MatchID VARCHAR(40) NOT NULL,
    DataSource ENUM('CRICSHEET', 'CRICAPI') NOT NULL,
    TeamID INT NOT NULL,
    FOREIGN KEY (MatchID, DataSource) REFERENCES Cricket.CricketMatch(MatchID, DataSource),
    FOREIGN KEY (TeamID) REFERENCES Cricket.Team(TeamID),
    UNIQUE (MatchID, DataSource, TeamID)
);

-- contains the value of all factors that tune the elo model
CREATE TABLE Cricket.Factors
(
    FactorID INT AUTO_INCREMENT PRIMARY KEY,
    FactorName VARCHAR(40) UNIQUE
);

-- tells how each factor is applied to 'K' or 'E'
CREATE TABLE Cricket.FactorApplication
(
    ApplyOrder INT NOT NULL,
    AppliesTo ENUM('E', 'K') NOT NULL,
    FactorID INT NOT NULL,
    FactorValue FLOAT NOT NULL,
    -- assuming X = FactorValue and Y = AppliesTo
    Operation ENUM('+', '*', 'LogBaseX', 'LogBaseY', 'X^Y', 'Y^X') NOT NULL,
    FOREIGN KEY (FactorID) REFERENCES Cricket.Factors(FactorID),
    PRIMARY KEY(ApplyOrder, AppliesTo)
);