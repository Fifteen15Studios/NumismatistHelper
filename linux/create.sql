-- MySQL dump 10.16  Distrib 10.1.44-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: CoinCollection
-- ------------------------------------------------------
-- Server version	10.1.44-MariaDB-0ubuntu0.18.04.1

CREATE DATABASE CoinCollection;
USE CoinCollection;

--
-- Table structure for table `Bills`
--

DROP TABLE IF EXISTS `Bills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Bills` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `Country` varchar(32) NOT NULL DEFAULT 'United States',
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Sets`
--

DROP TABLE IF EXISTS `Sets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Sets` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `Name` varchar(100) DEFAULT NULL,
  `Yr` int(11) DEFAULT NULL,
  `CurValue` decimal(15,2) unsigned DEFAULT NULL,
  `Note` varchar(1024) DEFAULT NULL,
  `ObvImgExt` varchar(10) DEFAULT NULL,
  `RevImgExt` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Coins`
--

DROP TABLE IF EXISTS `Coins`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Coins` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `Country` varchar(32) NOT NULL DEFAULT 'United States',
  `Type` varchar(100) DEFAULT NULL,
  `Yr` int(11) NOT NULL,
  `Denomination` decimal(7,2) unsigned NOT NULL,
  `CurValue` decimal(15,2) unsigned DEFAULT NULL,
  `MintMark` enum('', 'C','CC','D','O','P','S','W') DEFAULT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;