package items

import NumismatistAPI
import java.io.FileNotFoundException
import java.sql.SQLException

/**
 * A book (or folder) that holds loose coins. These books often have multiple pages, and those pages have multiple
 * slots that hold Coins. Each slot can only hold a single coin.
 *
 * @see BookPage
 * @see PageSlot
 * @see Coin
 */
class Book : CollectionItem() {

    companion object {
        /**
         * Use this as startYear if the book has no starting year
         */
        const val DEFAULT_START_YEAR = 0
        /**
         * Use this as endYear if the book has no ending year
         */
        const val DEFAULT_END_YEAR = 9999
    }

    var title = ""
    /**
     * An optional parameter, used if every coin is the same denomination. Otherwise, use SetItem.NO_DENOMINATION
     */
    var denomination = SetItem.NO_DENOMINATION
    var startYear = DEFAULT_START_YEAR
    var endYear = DEFAULT_END_YEAR

    val pages = ArrayList<BookPage>()

    /**
     * Adds a page to this book
     *
     * @param page The page you want to add
     */
    fun addPage(page: BookPage) {
        pages.add(page)
    }

    /**
     * Saves a book to the database. If the book is not already in the database (id = 0) the book is added.
     *   If the book is already in the database, it is updated to reflect any changes.
     *
     * @param api an API object to use to connect to the database
     *
     * @return How many rows were affected by the sql command
     */
    override fun saveToDb(api: NumismatistAPI) : Int {

        val sql: String

        val rows: Int

        // set ContainerID to null if necessary
        val containerID = if(containerId == ID_INVALID)
            "null"
        else
            "" + containerId

        if(id != 0) {

            sql = "UPDATE Books SET Title=\"${title}\", Denomination=${denomination}, " +
                    "StartYear=${startYear}, EndYear=${endYear}, ContainerID=$containerID\n" +
                    "WHERE ID=${id};"

            rows = api.runUpdate(sql)

            if(rows == 1) {
                for(page in pages) {
                    page.saveToDb(api)
                }

                api.setBook(id, this)
            }
        }
        else {

            sql = "INSERT INTO Books(Title, Denomination, StartYear, EndYear, ContainerID)\n" +
                    "VALUES(\"${title}\", ${denomination}, ${startYear}, ${endYear}, " +
                    "$containerID);"

            rows = api.runUpdate(sql)

            // If successful, set the new ID to this object
            if(rows == 1) {
                val newID = api.getGeneratedKeys()

                if(newID.next()) {
                    id = Integer.parseInt(newID.getString("GENERATED_KEY"))
                }

                for(page in pages) {
                    // TODO: Handle Errors
                    page.saveToDb(api)
                }

                // TODO: if no errors
                api.getBooks().add(this)
            }
        }

        api.disconnect()

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

        val sql = "DELETE FROM Books WHERE ID=${id}"

        val rows = api.runUpdate(sql)

        api.disconnect()

        if(rows==1) {
            // Remove the pages in the book
            // TODO: Handle Error
            for(page in pages)
                page.removeFromDb(api)

            api.getBooks().remove(this)
        }

        return rows == 1
    }
}