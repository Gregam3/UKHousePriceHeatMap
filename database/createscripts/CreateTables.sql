CREATE DATABASE IF NOT EXISTS AppDB;

USE AppDB;

CREATE TABLE IF NOT EXISTS Users (
	UserID CHAR(32),
    
	PRIMARY KEY(UserID)
);


CREATE TABLE IF NOT EXISTS Locations (
	UserID CHAR(32),
	Timelog DATETIME,
	Longitude FLOAT,
	Latitude FLOAT,
	Altitude FLOAT,
	Delivered BOOLEAN,

	PRIMARY KEY(UserID, Timelog),
	FOREIGN KEY(UserID) REFERENCES Users(UserID)
	-- Delete all users's locations if user is deleted
		ON DELETE CASCADE
	-- Dont allow user to update ID if has locations
		ON UPDATE RESTRICT
);