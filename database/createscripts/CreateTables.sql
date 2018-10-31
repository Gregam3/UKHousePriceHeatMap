-- Set these values
CREATE DATABASE IF NOT EXISTS development;

USE development;

CREATE TABLE IF NOT EXISTS user_data (
	USER_ID CHAR(64),
    
	PRIMARY KEY(USER_ID)
);


CREATE TABLE IF NOT EXISTS location_data (
	USER_ID CHAR(64),
	TIMELOG DATETIME,
	LONGITUDE FLOAT,
	LATITUDE FLOAT,
	ALTITUDE FLOAT,
	DELIVERED BOOLEAN,

	PRIMARY KEY(USER_ID, TIMELOG),
	FOREIGN KEY(USER_ID) REFERENCES user_data(USER_ID)
	-- Delete all user's locations if user is deleted
		ON DELETE CASCADE
	-- Dont allow user to update ID if they have locations
		ON UPDATE RESTRICT
);