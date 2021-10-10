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


if [ 1 -eq 0 ]; then
username="coins"
password="coinDatabasePassword1!"
databaseName="CoinCollection"

# Create database and start using it
echo "Creating database..."
echo ""
sudo mysql -e "CREATE DATABASE $databaseName;"
sudo mysql -e "USE $databaseName;"

# Create countries table
sudo mysql -e "DROP TABLE IF EXISTS `Countries`;"
sudo mysql -e "CREATE TABLE Countries (
  ID bigint UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  Name varchar(100) UNIQUE
  );"
  
# Create currencies table
sudo mysql -e "DROP TABLE IF EXISTS `Currencies`;"
sudo mysql -e "CREATE TABLE Currencies (
  ID bigint UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  Name varchar(100)  NOT NULL,
  Abbreviation varchar(10) UNIQUE,
  Symbol varchar(10),
  SymbolBefore boolean default true,
  YrStart int UNSIGNED NOT NULL default 9999,
  YrEnd int UNSIGNED default NULL
  );"
  
# Create countryCurrencies table
sudo mysql -e "DROP TABLE IF EXISTS `CountryCurrencies`;"
sudo mysql -e "CREATE TABLE CountryCurrencies (
  ID bigint UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  CountryName varchar(100) NOT NULL,
  CurrencyAbbr varchar(10) NOT NULL,
  FOREIGN KEY(CountryName) REFERENCES Countries(Name),
  FOREIGN KEY(CurrencyAbbr) REFERENCES Currencies(Abbreviation)
  );"

# Create containers table
sudo mysql -e "DROP TABLE IF EXISTS `Containers`;"
sudo mysql -e "CREATE TABLE Containers (
  ID bigint UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  Name varchar(100) UNIQUE NOT NULL,
  ParentID bigint UNSIGNED,
  FOREIGN KEY(ParentID) REFERENCES Containers(ID)
);"

# Create Sets table
sudo mysql -e "DROP TABLE IF EXISTS `Sets`;"
sudo mysql -e "CREATE TABLE Sets (
  ID bigint UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  Name varchar(100),
  Yr int,
  CurValue Decimal(20,2) UNSIGNED,
  ObvImgExt varchar(10),
  RevImgExt varchar(10),
  Note varchar(1024),
  ContainerID bigint UNSIGNED,
  ParentID bigint UNSIGNED,
  FOREIGN KEY(ParentID) REFERENCES Sets(ID),
  FOREIGN KEY(ContainerID) REFERENCES Containers(ID)
);"

# Create Coins table
sudo mysql -e "DROP TABLE IF EXISTS `Coins`;"
sudo mysql -e "CREATE TABLE Coins (
  ID bigint UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  CountryName varchar(100) NOT NULL,
  CurrencyAbbr varchar(10) NOT NULL,
  Type varchar(100),
  Yr int NOT NULL,
  Denomination Decimal(20,3) UNSIGNED NOT NULL,
  CurValue Decimal(20,2) UNSIGNED,
  MintMark varchar(3),
  Graded Boolean default false,
  Grade varchar(10),
  Error boolean default false,
  ErrorType varchar(100),
  SetID bigint UNSIGNED default NULL
    check (SetID is null or SlotID is null),
  SlotID bigint UNSIGNED default NULL
    check (SetID is null or SlotID is null),
  ObvImgExt varchar(10),
  RevImgExt varchar(10),
  Note varchar(1024),
  ContainerID bigint UNSIGNED,
  FOREIGN KEY(ContainerID) REFERENCES Containers(ID),
  FOREIGN KEY(SetID) REFERENCES Sets(ID),
  FOREIGN KEY(SlotID) REFERENCES RowSlots(ID),
  FOREIGN KEY(CountryName) REFERENCES Countries(Name),
  FOREIGN KEY(CurrencyAbbr) REFERENCES Currencies(Abbreviation)
);"

# Create bills table
sudo mysql -e "DROP TABLE IF EXISTS `Bills`;"
sudo mysql -e "CREATE TABLE Bills (
  ID bigint UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  CountryName varchar(100) NOT NULL,
  CurrencyAbbr varchar(10)  NOT NULL,
  Type varchar(100),
  Yr int NOT NULL,
  SeriesLetter varchar(3),
  Serial varchar(20),
  Denomination Decimal(20,3) UNSIGNED NOT NULL,
  CurValue Decimal(20,2) UNSIGNED,
  Graded boolean,
  Grade varchar(10),
  Error boolean default false,
  ErrorType varchar(100),
  Replacement boolean default false,
  Signatures varchar(255),
  ObvImgExt varchar(10),
  RevImgExt varchar(10),
  Note varchar(1024),
  ContainerID bigint UNSIGNED,
  SetID bigint UNSIGNED,
  FOREIGN KEY(SetID) REFERENCES Sets(ID),
  FOREIGN KEY(ContainerID) REFERENCES Containers(ID),
  FOREIGN KEY(CountryName) REFERENCES Countries(Name),
  FOREIGN KEY(CurrencyAbbr) REFERENCES Currencies(Abbreviation)
);"

# Create user and give permissons to database
# Use % to allow remote access
echo "Creating SQL user and granting permissions..."
echo ""
sudo mysql -e "CREATE USER \"$username\"@\"%\" IDENTIFIED BY \"$password\";"
sudo mysql -e "GRANT ALL ON $databaseName.* TO \"$username\"@\"%\" IDENTIFIED BY \"$password\";"
# Apply new permissions
sudo mysql -e "FLUSH PRIVILEGES;"
fi

echo "Installing JDK 16..."
echo ""

# Install Jdk 16
sudo add-apt-repository ppa:linuxuprising/java
sudo apt update
sudo apt install oracle-java16-set-default -y

echo "Done!"