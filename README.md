# Numismatist Helper

This is a program that I designed to document my collection of Coins, Coin Sets, and Banknotes (Dollar Bills.) 
It uses a self-hosted MySQL database, and a Java/Kotlin program.

Coins, banknotes, and sets can be added to the collection, and then the collection can be viewed in a table format (like MS Excel.) 
These tables can also be exported to a csv file for later use.

Each item (Coin, Set, or Banknote) can also have 2 images attached to it... 1 for the front (obverse) of the item and 1 for the back (reverse) of the item. 
These images are stored in the user's file system, and the user can decide where to put these images. Only the file extension is stored in the database.

This program is compatible with Windows, Linux, and Mac. However, Mac is untested as I do not own a Mac to test with. It was developed using a combination of 
an Ubuntu variant of Linux, and Windows.

I will also add screenshots to this ReadMe in the future.

# WARNING

This program is still in development, and big changes are likely to be still coming - Including possible changes to the database. 
If you are not comfortable with possibly performing some manual changes to your database, it might be best for you to wait a bit before using 
this for data that you rely on.

# Features

## Multiple countries and currencies

The database supports multiple countries and their currencies, so regardless of how diverse your collection is, it will still be able to be properly  
documented and displayed. You can even add new countries and currencies if something is missing. 
(Currently this has to be done at the database level, but future versions will include this functionality in app.)

Each country can have multiple currencies associated with it, and those currencies also contain the years in which they were used 
so that input boxes can change based on information that has already been entered. Each coin and banknote will have a country and currency attached to it. 
This is a HUGE job to include all countries and currencies, so it will likely take some time to have a full list. 

**If you're willing to help build this list, it would be much appreciated.**

## Containers

Is your collection spread across multiple boxes or storage containers? No problem! You can add containers and document where each item is.  

# Database

A diagram of the database setup is included in `Numismatist Helper.pdf`. This diagram was created using https://dbdiagram.io

# Installation

## Linux

There is a folder called "linux" with a script that will install the necessary software, create the database and run the program. 

Run the "CreateDatabase.sh" file, and it should install all necessary packages (mariadb-client and mariadb-server), create the database, create a user for the database, and install the necessary JDK to run the program.

Once the database is created, simply run the command `java -jar NumismatistHelper.jar` and you should be able to start documenting your collection.

## Windows

I have not hosted the database on Windows, however the main program will run in Windows. An exe file is included, which is all you need to run the program.
The exe was built using Launch4j.

The sql file to create the database is included in a folder called "Windows". This file should be all you need to create the database. Once the database is created, you should be able to run the program by running the `Numismatist Helper.exe` file.

# Current Work

I am still actively developing this program. Here are some things I've been working on:

## Coin Folders

Currently, you can store coins, banknotes (dollar bills), and coin sets. A later version will include coin folders. These folders will be customizable, and can be imported using an xml file. (Details to come.)

## Android Companion App

I have started work on an Android companion app, which right now is a read-only app. Next steps for the app is to allow the user to take pictures of items. This is only possible if the pictures directory is pointing to a cloud platform like Google Drive or Dropbox. Because of this, I would also like to implement API access to such services in both the desktop and mobile applications.

# Future Additions / Improvements

## Table View of Collection

Right now the only way to view your collection is in a spreadsheet view. I would like to add a table view, which would be customizable as to how the table is
laid out.

## Spreadsheet View Manipulation

I would like to add the ability to add and remove columns from the spreadsheet view, so that all information you need is displayed, and nothing more.

## Uncut Sheets

In the future I would like to add the ability to also enter uncut sheets of banknotes. These will be treated in a similar way to Coin Sets. I, personally, do not own any of these sheets at this time, so it is not a priority for me.
This can actually be achieved right now by adding each banknote in the sheet to a set. However, this may improve in the future.

## Cloud Storage Connectivity

I would like to add out of the box connection to cloud storage providers like Dropbox, Google Drive, and OneDrive to use for storing images.

## Database Backups

The ability to create a backup of the database from within the app would be a great feature.

## Kotlin Multiplatform

I would like to implement Kotlin Multiplatform when it officially releases (currently in Alpha) so that the desktop and Android apps can have a single code base.

# Building from Source

This was originally built using [Intellij Idea](https://www.jetbrains.com/idea/). You will need [mysql-connector-java-5.1.49.jar](https://downloads.mysql.com/archives/c-j/). 
This jar is necessary to connect to the MySQL database. 
If you do not include this file (or do so improperly) you will receive an error when the program loads, which will tell you to add the file to your project.
The program was built using OpenJDK-15, but any JDK of the same (or higher) version level should work fine.

The source is a mix of Kotlin and Java. Any file that was generated by Intellij is Java, and the files created by me are Kotlin. 
This mix of Kotlin and Java may lead to issues if you try to build it with a different tool, but I find Kotlin much easier to both read and write.
