DROP DATABASE IF EXISTS CricketMatchData;
CREATE DATABASE CricketMatchData;
USE CricketMatchData;

-- lookup table for .CricketMatch, to allow number of days to tie to a format, populated with T20, ODI and Test
CREATE TABLE CricketMatchData.MatchFormat
(
    FormatName VARCHAR(10) PRIMARY KEY,
    MatchLengthDays INT NOT NULL
);
INSERT INTO CricketMatchData.MatchFormat(FormatName, MatchLengthDays) VALUES
("Test", 5),
("ODI", 1),
("T20", 1);

CREATE TABLE CricketMatchData.Team
(
    TeamID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE CricketMatchData.Venue
(
    VenueID INT AUTO_INCREMENT PRIMARY KEY,
    GroundName VARCHAR(40) NOT NULL,
    City VARCHAR(30) NOT NULL,
    UNIQUE (GroundName, City)
);

-- assign table for all venues that are a home ground for each team
CREATE TABLE CricketMatchData.TeamHomeVenue
(
    TeamHomeVenueID INT AUTO_INCREMENT PRIMARY KEY,
    TeamID INT NOT NULL,
    VenueID INT NOT NULL,
    FOREIGN KEY (TeamID) REFERENCES CricketMatchData.Team(TeamID),
    FOREIGN KEY (VenueID) REFERENCES CricketMatchData.Venue(VenueID)
);

-- MATCH is a reserved sql keyword
CREATE TABLE CricketMatchData.CricketMatch
(
    MatchID INT AUTO_INCREMENT PRIMARY KEY,
    FormatName VARCHAR(10) NOT NULL,
    VenueID INT NOT NULL,
    StartDate DATE NOT NULL,
    FOREIGN KEY (FormatName) REFERENCES CricketMatchData.MatchFormat(FormatName),
    FOREIGN KEY (VenueID) REFERENCES CricketMatchData.Venue(VenueID)
);

CREATE TABLE CricketMatchData.MatchResult
(
    MatchID INT PRIMARY KEY,
    WinningTeamID INT, -- if draw or no result then nullable
    TossWinningTeamID INT NOT NULL,
    TossDecision ENUM('Bat', 'Field') NOT NULL,
    Result ENUM('Win', 'Draw', 'No Result'),
    MarginSize INT, -- how many runs/wickets won by
    MarginType ENUM('Wickets', 'Runs'),
    FOREIGN KEY (MatchID) REFERENCES CricketMatchData.CricketMatch(MatchID),
    FOREIGN KEY (WinningTeamID) REFERENCES CricketMatchData.Team(TeamID)
);

-- assign table for match and team tables
CREATE TABLE CricketMatchData.MatchTeam
(
    MatchTeamID INT AUTO_INCREMENT PRIMARY KEY,
    MatchID INT NOT NULL,
    TeamID INT NOT NULL,
    FOREIGN KEY (MatchID) REFERENCES CricketMatchData.CricketMatch(MatchID),
    FOREIGN KEY (TeamID) REFERENCES CricketMatchData.Team(TeamID)
);