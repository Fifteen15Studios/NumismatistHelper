# CoinCollection

This is a program that I designed to document my collection of Coins, Coin Sets, and Dollar Bills. It uses a MySQL database, and a Java/Kotlin program.

Coins, Bills, and Coin sets can be added to the collection, and then the collection can be viewed in a table format (like Excel.) these tables can also be exported to an xls, xlsx, or csv file for later use.

Each item (Coin, Set, or Bill) can also have 2 images attached to it... 1 for the front (obverse) of the item and 1 for the back (reverse) of the item. These images are stores in the user's file system, and the user can decide where ot put these images. Only the file extension is stored in the database.

This program was created and tested on a Linux variant based on Ubuntu. I have not done any testing (yet) on other operating systems, but it should work on Windows and probably Mac.

# Database

A diagram of the database setup is included in `Coin Collection.pdf`. This diagram was created using https://dbdiagram.io

# Installation

## Linux

There is a folder called "linux" with files necessary to create the necessary database and run the program. Run the "CreateDatabase.sh" file and it should install all necessary packages (mariadb-client and mariadb-server), create the database, create a user for the database, and install the necessary JDK-15 to run the program.

Once the database is created, simply run "run_linux.sh" and you should be able to start documenting your collection.

## Windows

At this point this has not been tested on Windows. However, if you know how to create a MySQL Server, install a MySQL Client on your PC, and install JDK 15 on your PC, you should be able to run the program.

The sql file to create the database is included in a folder called "Windows". (It's the same as the file in the "linux" directory.) This file should be all you need to create the database. Once the database is created, you should be able to use the following command to run the program: `java -cp ".\out\production\Coin Program\" Main` assuming that this command is being run from the base directory of the project.
