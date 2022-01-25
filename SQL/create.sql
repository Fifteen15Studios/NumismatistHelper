-- phpMyAdmin SQL Dump
-- version 4.9.7
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- Generation Time: Oct 10, 2021 at 09:35 AM
-- Server version: 10.3.29-MariaDB
-- PHP Version: 7.3.24

SET FOREIGN_KEY_CHECKS=0;
-- SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
-- SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `CoinCollection`
--
CREATE DATABASE IF NOT EXISTS `CoinCollection` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `CoinCollection`;

-- --------------------------------------------------------

--
-- Table structure for table `Bills`
--

CREATE TABLE `Bills` (
  `ID` bigint(20) UNSIGNED NOT NULL,
  `CountryName` varchar(100) NOT NULL,
  `CurrencyAbbr` varchar(10) NOT NULL,
  `Type` varchar(100) DEFAULT NULL,
  `Yr` int(11) NOT NULL,
  `SeriesLetter` varchar(3) DEFAULT NULL,
  `Serial` varchar(20) DEFAULT NULL,
  `Denomination` decimal(20,3) UNSIGNED NOT NULL,
  `CurValue` decimal(15,2) UNSIGNED DEFAULT NULL,
  `Graded` tinyint(1) DEFAULT NULL,
  `Grade` varchar(10) DEFAULT NULL,
  `Error` tinyint(1) DEFAULT 0,
  `ErrorType` varchar(100) DEFAULT NULL,
  `Replacement` tinyint(1) DEFAULT 0,
  `Signatures` varchar(255) DEFAULT NULL,
  `ObvImgExt` varchar(10) DEFAULT NULL,
  `RevImgExt` varchar(10) DEFAULT NULL,
  `Note` varchar(1024) DEFAULT NULL,
  `ContainerID` bigint(20) UNSIGNED DEFAULT NULL,
  `SetID` bigint(20) UNSIGNED DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `BookPages`
--

CREATE TABLE `BookPages` (
  `ID` bigint(20) UNSIGNED NOT NULL,
  `BookID` bigint(20) UNSIGNED NOT NULL,
  `PageNum` int(10) UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `Books`
--

CREATE TABLE `Books` (
  `ID` bigint(20) UNSIGNED NOT NULL,
  `Title` varchar(64) NOT NULL,
  `Denomination` decimal(20,3) UNSIGNED NOT NULL,
  `StartYear` int(10) UNSIGNED NOT NULL,
  `EndYear` int(10) UNSIGNED NOT NULL,
  `ContainerID` bigint(20) UNSIGNED DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `Coins`
--

CREATE TABLE `Coins` (
  `ID` bigint(20) UNSIGNED NOT NULL,
  `CountryName` varchar(100) NOT NULL,
  `CurrencyAbbr` varchar(10) NOT NULL,
  `Type` varchar(100) DEFAULT NULL,
  `Yr` int(11) NOT NULL,
  `Denomination` decimal(20,3) UNSIGNED NOT NULL,
  `CurValue` decimal(20,2) UNSIGNED DEFAULT NULL,
  `MintMark` varchar(3) DEFAULT NULL,
  `Graded` tinyint(1) DEFAULT 0,
  `Grade` varchar(10) DEFAULT NULL,
  `Error` tinyint(1) DEFAULT 0,
  `ErrorType` varchar(100) DEFAULT NULL,
  `SetID` bigint(20) UNSIGNED DEFAULT NULL CHECK (`SetID` is null or `SlotID` is null),
  `SlotID` bigint(20) UNSIGNED DEFAULT NULL,
  `ObvImgExt` varchar(10) DEFAULT NULL,
  `RevImgExt` varchar(10) DEFAULT NULL,
  `Note` varchar(1024) DEFAULT NULL,
  `ContainerID` bigint(20) UNSIGNED DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `Containers`
--

CREATE TABLE `Containers` (
  `ID` bigint(20) UNSIGNED NOT NULL,
  `Name` varchar(100) NOT NULL,
  `ParentID` bigint(20) UNSIGNED DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `Countries`
--

CREATE TABLE `Countries` (
  `ID` bigint(20) UNSIGNED NOT NULL,
  `Name` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `CountryCurrencies`
--

CREATE TABLE `CountryCurrencies` (
  `ID` bigint(20) UNSIGNED NOT NULL,
  `CountryName` varchar(100) NOT NULL,
  `CurrencyAbbr` varchar(10) NOT NULL,
  `YrStart` int(10) UNSIGNED NOT NULL DEFAULT 9999,
  `YrEnd` int(10) UNSIGNED DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `Currencies`
--

CREATE TABLE `Currencies` (
  `ID` bigint(20) UNSIGNED NOT NULL,
  `Name` varchar(100) DEFAULT NULL,
  `Abbreviation` varchar(10) DEFAULT NULL,
  `Symbol` varchar(10) DEFAULT NULL,
  `SymbolBefore` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `PageSlots`
--

CREATE TABLE `PageSlots` (
  `ID` bigint(20) UNSIGNED NOT NULL,
  `PageID` bigint(20) UNSIGNED NOT NULL,
  `RowNum` int(10) UNSIGNED NOT NULL,
  `ColNum` int(10) UNSIGNED NOT NULL,
  `Denomination` decimal(20,3) UNSIGNED DEFAULT NULL,
  `Label` varchar(50) DEFAULT NULL,
  `Label2` varchar(50) DEFAULT NULL,
  `Label3` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `Sets`
--

CREATE TABLE `Sets` (
  `ID` bigint(20) UNSIGNED NOT NULL,
  `Name` varchar(100) DEFAULT NULL,
  `Yr` int(11) DEFAULT NULL,
  `CurValue` decimal(15,2) UNSIGNED DEFAULT NULL,
  `ObvImgExt` varchar(10) DEFAULT NULL,
  `RevImgExt` varchar(10) DEFAULT NULL,
  `Note` varchar(1024) DEFAULT NULL,
  `ContainerID` bigint(20) UNSIGNED DEFAULT NULL,
  `ParentID` bigint(20) UNSIGNED DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `Bills`
--
ALTER TABLE `Bills`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `CountryName` (`CountryName`),
  ADD KEY `CurrencyAbbr` (`CurrencyAbbr`),
  ADD KEY `ContainerID` (`ContainerID`),
  ADD KEY `SetID` (`SetID`);

--
-- Indexes for table `BookPages`
--
ALTER TABLE `BookPages`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `BookID` (`BookID`);

--
-- Indexes for table `Books`
--
ALTER TABLE `Books`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `ContainerID` (`ContainerID`);

--
-- Indexes for table `Coins`
--
ALTER TABLE `Coins`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `SetID` (`SetID`),
  ADD KEY `SlotID` (`SlotID`),
  ADD KEY `CountryName` (`CountryName`),
  ADD KEY `CurrencyAbbr` (`CurrencyAbbr`),
  ADD KEY `ContainerID` (`ContainerID`);

--
-- Indexes for table `Containers`
--
ALTER TABLE `Containers`
  ADD PRIMARY KEY (`ID`),
  ADD UNIQUE KEY `Name` (`Name`),
  ADD KEY `ParentID` (`ParentID`);

--
-- Indexes for table `Countries`
--
ALTER TABLE `Countries`
  ADD PRIMARY KEY (`ID`),
  ADD UNIQUE KEY `Name` (`Name`);

--
-- Indexes for table `CountryCurrencies`
--
ALTER TABLE `CountryCurrencies`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `CountryName` (`CountryName`),
  ADD KEY `CurrencyAbbr` (`CurrencyAbbr`);

--
-- Indexes for table `Currencies`
--
ALTER TABLE `Currencies`
  ADD PRIMARY KEY (`ID`),
  ADD UNIQUE KEY `Abbreviation` (`Abbreviation`);

--
-- Indexes for table `PageSlots`
--
ALTER TABLE `PageSlots`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `PageID` (`PageID`);

--
-- Indexes for table `Sets`
--
ALTER TABLE `Sets`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `ContainerID` (`ContainerID`),
  ADD KEY `ParentID` (`ParentID`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `Bills`
--
ALTER TABLE `Bills`
  MODIFY `ID` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `BookPages`
--
ALTER TABLE `BookPages`
  MODIFY `ID` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `Books`
--
ALTER TABLE `Books`
  MODIFY `ID` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `Coins`
--
ALTER TABLE `Coins`
  MODIFY `ID` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `Containers`
--
ALTER TABLE `Containers`
  MODIFY `ID` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `Countries`
--
ALTER TABLE `Countries`
  MODIFY `ID` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `CountryCurrencies`
--
ALTER TABLE `CountryCurrencies`
  MODIFY `ID` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `Currencies`
--
ALTER TABLE `Currencies`
  MODIFY `ID` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `PageSlots`
--
ALTER TABLE `PageSlots`
  MODIFY `ID` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `Sets`
--
ALTER TABLE `Sets`
  MODIFY `ID` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `Bills`
--
ALTER TABLE `Bills`
  ADD CONSTRAINT `Bills_ibfk_1` FOREIGN KEY (`CountryName`) REFERENCES `Countries` (`Name`),
  ADD CONSTRAINT `Bills_ibfk_2` FOREIGN KEY (`CurrencyAbbr`) REFERENCES `Currencies` (`Abbreviation`),
  ADD CONSTRAINT `Bills_ibfk_3` FOREIGN KEY (`ContainerID`) REFERENCES `Containers` (`ID`),
  ADD CONSTRAINT `Bills_ibfk_4` FOREIGN KEY (`SetID`) REFERENCES `Sets` (`ID`);

--
-- Constraints for table `BookPages`
--
ALTER TABLE `BookPages`
  ADD CONSTRAINT `BookPages_ibfk_1` FOREIGN KEY (`BookID`) REFERENCES `Books` (`ID`);

--
-- Constraints for table `Books`
--
ALTER TABLE `Books`
  ADD CONSTRAINT `Books_ibfk_1` FOREIGN KEY (`ContainerID`) REFERENCES `Containers` (`ID`);

--
-- Constraints for table `Coins`
--
ALTER TABLE `Coins`
  ADD CONSTRAINT `Coins_ibfk_1` FOREIGN KEY (`SetID`) REFERENCES `Sets` (`ID`),
  ADD CONSTRAINT `Coins_ibfk_6` FOREIGN KEY (`CountryName`) REFERENCES `Countries` (`Name`),
  ADD CONSTRAINT `Coins_ibfk_7` FOREIGN KEY (`CurrencyAbbr`) REFERENCES `Currencies` (`Abbreviation`),
  ADD CONSTRAINT `Coins_ibfk_8` FOREIGN KEY (`ContainerID`) REFERENCES `Containers` (`ID`);

--
-- Constraints for table `Containers`
--
ALTER TABLE `Containers`
  ADD CONSTRAINT `Containers_ibfk_1` FOREIGN KEY (`ParentID`) REFERENCES `Containers` (`ID`);

--
-- Constraints for table `CountryCurrencies`
--
ALTER TABLE `CountryCurrencies`
  ADD CONSTRAINT `CountryCurrencies_ibfk_1` FOREIGN KEY (`CountryName`) REFERENCES `Countries` (`Name`),
  ADD CONSTRAINT `CountryCurrencies_ibfk_2` FOREIGN KEY (`CurrencyAbbr`) REFERENCES `Currencies` (`Abbreviation`);

--
-- Constraints for table `PageSlots`
--
ALTER TABLE `PageSlots`
  ADD CONSTRAINT `PageSlots_ibfk_1` FOREIGN KEY (`PageID`) REFERENCES `BookPages` (`ID`);

--
-- Constraints for table `Sets`
--
ALTER TABLE `Sets`
  ADD CONSTRAINT `Sets_ibfk_1` FOREIGN KEY (`ContainerID`) REFERENCES `Containers` (`ID`),
  ADD CONSTRAINT `Sets_ibfk_2` FOREIGN KEY (`ParentID`) REFERENCES `Sets` (`ID`);
SET FOREIGN_KEY_CHECKS=1;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
