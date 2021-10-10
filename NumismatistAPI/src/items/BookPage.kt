package items

import NumismatistAPI
import java.io.FileNotFoundException
import java.sql.SQLException

/**
 * A page of a Book (or folder) that holds Coins. Each page can have multiple slots. Each slot holds a single coin.
 *
 * @see Book
 * @see PageSlot
 * @see Coin
 */
class BookPage : DatabaseItem() {

    /**
     * The book that this page is in
     */
    var book = Book()
    /**
     * Page number of this page inside the book
     */
    var pageNum = 0

    /**
     * Contains a list of coins which the user has selected to remove from the database
     */
    val coinsToDelete = ArrayList<Coin>()

    val rows = ArrayList<ArrayList<PageSlot>>()

    /**
     * Saves a book page to the database. If the page is not already in the database (id = 0) the page is added.
     *   If the page is already in the database, it is updated to reflect any changes.
     *
     * @param api an API object to use to connect to the database
     *
     * @return How many rows were affected by the sql command
     */
    override fun saveToDb(api: NumismatistAPI) : Int {

        val sql : String

        val rows : Int

        if(id != ID_INVALID) {
            sql = "UPDATE BookPages SET BookID=${book.id}, PageNum=${pageNum}" +
                    "WHERE ID=${id};"

            rows = api.runUpdate(sql)

            if(rows ==1) {
                for (row in this.rows) {
                    for (slot in row)
                    // TODO: Handle Errors
                        slot.saveToDb(api)
                }
            }
        }
        else {

            sql = "INSERT INTO BookPages(BookID, PageNum)\n" +
                    "VALUES(${book.id}, ${pageNum});"

            rows = api.runUpdate(sql)

            // If successful, set the new ID to this object
            if(rows == 1) {
                val newID = api.getGeneratedKeys()
                if(newID.next()) {
                    id = Integer.parseInt(newID.getString("GENERATED_KEY"))
                }

                for (row in this.rows) {
                    for (slot in row) {
                        // TODO: Handle Errors
                        slot.saveToDb(api)
                    }
                }
            }
        }

        return rows
    }

    /**
     * Removes a book from the database
     *
     * @param api an API object to use to connect to the database
     *
     * @return True if book was successfully removed, otherwise false
     */
    @Throws(SQLException::class, FileNotFoundException::class)
    override fun removeFromDb(api: NumismatistAPI) : Boolean {

        val sql = "DELETE FROM BookPages WHERE ID=${id}"

        val rows = api.runUpdate(sql)

        api.disconnect()

        if(rows==1) {
            // Remove Page Slots
            // TODO: Handle Error
            for(row in this.rows) {
               for(slot in row)
                   slot.removeFromDb(api)
            }
        }

        return rows == 1
    }
}