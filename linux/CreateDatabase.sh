#!/bin/bash

aptUpdated=$false

# Check if SQL Client installed
status=$(dpkg-query -W -f='${Status}\n' mariadb-client)

# If not installed, install it
if [ "$status" != 'install ok installed' ]
then
    echo "Updatig package list..."
    sudo apt-get update
    echo ""
    $aptUpdated=$true
    
    echo "Installing prerequisite - MySQL Client..."
    sudo apt-get install mariadb-client -y
    echo ""
fi

# Check if SQL Server installed
status=$(dpkg-query -W -f='${Status}\n' mariadb-server)

# If not installed, install it
if [ "$status" != 'install ok installed' ]
then
    if [ $aptUpdated != $true ]
    then
        echo "Updatig package list..."
        sudo apt-get update
        echo ""
        $aptUpdated=$true
    fi
    
    echo "Installing prerequisite - MySQL Server..."
    sudo apt-get install mariadb-server -y
    echo ""
fi

username="coins"
password="coinDatabasePassword"
databaseName="CoinCollection"

# Create database and start using it
sudo mysql -e "CREATE DATABASE $databaseName;"
sudo mysql -e "USE $databaseName;"

# Create bills table
sudo mysql -e "DROP TABLE IF EXISTS `Bills`;"
sudo mysql -e "CREATE TABLE `Bills` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `Country` varchar(32) NOT NULL DEFAULT 'US',
  `Type` varchar(100) DEFAULT NULL,
  `Yr` int(11) NOT NULL,
  `Denomination` decimal(7,2) unsigned NOT NULL,
  `CurValue` decimal(15,2) unsigned DEFAULT NULL,
  `Grade` varchar(10) DEFAULT NULL,
  `Error` tinyint(1) DEFAULT '0',
  `ErrorType` varchar(100) DEFAULT NULL,
  `Note` varchar(1024) DEFAULT NULL,
  `PlateSeriesObv` varchar(10) DEFAULT NULL,
  `PlateSeriesRev` varchar(10) DEFAULT NULL,
  `Star` tinyint(1) DEFAULT NULL,
  `NotePosition` varchar(10) DEFAULT NULL,
  `District` varchar(5) DEFAULT NULL,
  `ObvImgExt` varchar(10) DEFAULT NULL,
  `RevImgExt` varchar(10) DEFAULT NULL,
  `Graded` tinyint(1) DEFAULT NULL,
  `Signatures` varchar(255) DEFAULT NULL,
  `SeriesLetter` varchar(3) DEFAULT NULL,
  PRIMARY KEY (`ID`)
);"

# Create Sets table
sudo mysql -e "DROP TABLE IF EXISTS `Sets`;"
sudo mysql -e "CREATE TABLE `Sets` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `Name` varchar(100) DEFAULT NULL,
  `Yr` int(11) DEFAULT NULL,
  `CurValue` decimal(15,2) unsigned DEFAULT NULL,
  `Note` varchar(1024) DEFAULT NULL,
  `ObvImgExt` varchar(10) DEFAULT NULL,
  `RevImgExt` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`ID`)
);"

# Create Coins table
sudo mysql -e "DROP TABLE IF EXISTS `Coins`;"
sudo mysql -e "CREATE TABLE `Coins` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `Country` varchar(32) NOT NULL DEFAULT 'US',
  `Type` varchar(100) DEFAULT NULL,
  `Yr` int(11) NOT NULL,
  `Denomination` decimal(7,2) unsigned NOT NULL,
  `CurValue` decimal(15,2) unsigned DEFAULT NULL,
  `MintMark` enum('C','CC','D','O','P','S','W') DEFAULT NULL,
  `Grade` varchar(10) DEFAULT NULL,
  `Error` tinyint(1) DEFAULT '0',
  `ErrorType` varchar(100) DEFAULT NULL,
  `SetID` bigint(20) unsigned DEFAULT NULL,
  `Note` varchar(1024) DEFAULT NULL,
  `Graded` tinyint(1) DEFAULT '0',
  `ObvImgExt` varchar(10) DEFAULT NULL,
  `RevImgExt` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `SetID` (`SetID`),
  CONSTRAINT `Coins_ibfk_1` FOREIGN KEY (`SetID`) REFERENCES `Sets` (`ID`)
);"

# Create user and give permissons to database
sudo mysql -e "CREATE USER $username@localhost IDENTIFIED BY \"$password\";"
sudo mysql -e "GRANT ALL ON $databaseName.* TO $username@localhost IDENTIFIED BY \"$password\";"
# Apply new permissions
sudo mysql -e "FLUSH PRIVILEGES;"

echo "Installing JDK 15..."

# Install Jdk 15
sudo add-apt-repository ppa:linuxuprising/java
sudo apt update
sudo apt install oracle-java15-installer -y