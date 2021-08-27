# CoinCollection

This is a program that I designed to document my collection of Coins, Coin Sets, and Bank Notes (Dollar Bills.) It uses a self hosted MySQL database, and a Java/Kotlin program. 

Coins, bills, and coin sets can be added to the collection, and then the collection can be viewed in a table format (like Excel.) these tables can also be exported to an xls, xlsx, or csv file for later use.

Each item (Coin, Set, or Bill) can also have 2 images attached to it... 1 for the front (obverse) of the item and 1 for the back (reverse) of the item. These images are stored in the user's file system, and the user can decide where to put these images. Only the file extension is stored in the database.

This program was created and tested on a Linux variant based on Ubuntu. It also works on Windows and probably Mac (though Mac is untested.)

I will also add screenshots to this ReadMe in the future.

# Database

A diagram of the database setup is included in `Coin Collection.pdf`. This diagram was created using https://dbdiagram.io

The database is pretty straight forward, with very little complication involved.

# Installation

## Linux

There is a folder called "linux" with files necessary to create the necessary database and run the program. Run the "CreateDatabase.sh" file and it should install all necessary packages (mariadb-client and mariadb-server), create the database, create a user for the database, and install the necessary JDK-15 to run the program.

Once the database is created, simply run `CoinCollection.jar` and you should be able to start documenting your collection.

## Windows

At this point this has not been tested on Windows. However, if you know how to create a MySQL Server, install a MySQL Client on your PC, and install JDK 15 on your PC, you should be able to run the program.

The sql file to create the database is included in a folder called "Windows". (It's the same as the file in the "linux" directory.) This file should be all you need to create the database. Once the database is created, you should be able to run the program by running the `CoinCollection.jar` file.

# Current Work

I am still actively developing this program. Here are some things I've been working on:

## Name Change

The next version will be called "Numismatists Helper" instead of "Coin Collection." The program can be used to document and organize more than just coins, so the name should reflect that.

## Speed Improvements

The new version will only connect to the database once for queries, and then will no longer query the database unless the database settings are changed. The information is retrieved from the DB once and then "cached" for later use. Subsequent connections to the database will only be made for updates, inserts and deletes.

## Background Jobs

I'm implementing background jobs for talking to the database, and displaying a notice to the user that there is background work being done. In some cases this notification may block the user from interacting with the program, in other cases it may not.

## Coin Folders

Currently you can store coins, bank notes (dollar bills), and coin sets. The next version will include coin folders. These folders will be customizable, and can be imported using an xml file. (Details to come.)

## Containers

I don't know about you, but my collection is large enough that I have a few different places where my collection held. Items (coins, sets, bank notes, and books) can now be put inside of a container. Containers can also be put inside of another container (parent / child relationship.) This is helpful if you have a cabinet with multiple drawers, or if you put your items in a box and then that box inside of a cabinet or another box.

## Countries and Currencies

I have modified the database to hold countries and currencies, and to associate the two together. The currencies include years in which they were used, so that input boxes can change based on information that has already been entered. Each coin and bank note will have a country and currency attached to it. This is a HUGE job to include all countries and currencies, so it will likely take some time to have a full list.

# Future Additions / Improvements

## Uncut Sheets 

In the future I would like to add the ability to also enter uncut sheets of bank notes. These will be treated in a similar way to Coin Sets. I, personally, do not own any of these sheets at this time, so it is not a priority for me.

## Android Companion App

I have started work on an Android companion app, which right now is a read-only app. Next steps for the app is to allow the user to take pictures of items. This is only possible if the pictures directory is pointing to a cloud platform like Google Drive or Dropbox. Because of this, I would also like to implement API access to such services in both the desktop and mobile applications.

## Kotlin Multiplatform

I would like to implement Kotlin Multiplatform when it officially release (currently in Alpha) so that the desktop and Android apps can have a single code base.

# Building from Source

This was originally built using [Intellij Idea](https://www.jetbrains.com/idea/). You will need [mysql-connector-java-*.jar](https://dev.mysql.com/downloads/connector/j/) where * is the current version. This jar is neseccary to connect to the MySQL database. If you do not include this file (or do so improperly) you will receive an error when the program loads, which will tell you to add the file to your project.

The program was built using OpenJDK-15, but any JDK of the same (or higher) version level should work fine.

The source is a mix of Kotlin and Java. Any file that was generated by Intellij is Java, and the files created by me are Kotlin. This mix of Kotlin and Java may lead to issues if you try to build it with a different tool, but I find Kotlin much easier to both read and write.

## Error Handling

Some of the error handling could admittedly be a little better - mostly returning more precise information. There are a lot of Try/Catch blocks, but some of them do not report the error back in as much detail as it probably should. This is something that will likely be improved in later versions, or if people (other than me) actually start using this. 

Also, because of the way that some of the errors are handled, some operations could potentially end up partially done and partially undone. Again, this will likely be addressed in a later version. These scenarios are extremely unlikely, but should stil be addressed in the rare case that one of those extremely unlikey scenarios occurs.
