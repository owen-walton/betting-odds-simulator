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
    Name VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE CricketMatchData.Venue
(
    VenueID INT AUTO_INCREMENT PRIMARY KEY,
    GroundName VARCHAR(80) NOT NULL,
    City VARCHAR(50) NOT NULL,
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
    MatchID VARCHAR(40) NOT NULL,
    DataSource ENUM('CRICSHEET', 'CRICAPI') NOT NULL,
    FormatName VARCHAR(10) NOT NULL,
    VenueID INT NOT NULL,
    StartDate DATE NOT NULL,
    PRIMARY KEY (MatchID, DataSource),
    FOREIGN KEY (FormatName) REFERENCES CricketMatchData.MatchFormat(FormatName),
    FOREIGN KEY (VenueID) REFERENCES CricketMatchData.Venue(VenueID)
);

CREATE TABLE CricketMatchData.MatchResult
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
    FOREIGN KEY (MatchID, DataSource) REFERENCES CricketMatchData.CricketMatch(MatchID, DataSource),
    FOREIGN KEY (WinningTeamID) REFERENCES CricketMatchData.Team(TeamID)
);

-- assign table for match and team tables
CREATE TABLE CricketMatchData.MatchTeam
(
    MatchTeamID INT AUTO_INCREMENT PRIMARY KEY,
    MatchID VARCHAR(40) NOT NULL,
    DataSource ENUM('CRICSHEET', 'CRICAPI') NOT NULL,
    TeamID INT NOT NULL,
    FOREIGN KEY (MatchID, DataSource) REFERENCES CricketMatchData.CricketMatch(MatchID, DataSource),
    FOREIGN KEY (TeamID) REFERENCES CricketMatchData.Team(TeamID),
    UNIQUE (MatchID, DataSource, TeamID)
);

CREATE TABLE CricketMatchData.TunedParameters
(
    TuningID INT NOT NULL,
    FormatName VARCHAR(10) NOT NULL,
    e_value DOUBLE NOT NULL,
    k_factor DOUBLE NOT NULL,
    starting_elo DOUBLE NOT NULL,
    home_adv DOUBLE NOT NULL,
    toss_adv DOUBLE NOT NULL,
    win_margin DOUBLE, -- nullable if not implemented yet
    max_draw_chance DOUBLE NOT NULL,
    CreatedAt TIMESTAMP,
    PRIMARY KEY(TuningID, FormatName)
);