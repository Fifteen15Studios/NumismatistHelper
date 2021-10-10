package items

import NumismatistAPI
import java.io.FileNotFoundException
import java.sql.SQLException

/**
 * A slot in a BookPage, which is in a Book. A slot can hold a single coin. Slots can be of various physical size
 * based on the coin that it is designed to hold. A slot can have multiple lines of text below it, which describe the
 * coin which the slot is designed for. Slots are generally aligned in rows on the page.
 *
 * @see Book
 * @see BookPage
 * @see Coin
 */
class PageSlot() : DatabaseItem() {

    companion object {
        const val SIZE_PENNY = 35
        const val SIZE_NICKEL = 40
        const val SIZE_DIME = 30
        const val SIZE_QUARTER = 50
        const val SIZE_HALF = 60
        const val SIZE_DOLLAR = 70
    }

    /**
     * The coin that is in this slot. If no coin is in the slot, coin should be null.
     */
    var coin : Coin? = null
        set(value) {
            // Set old coin's slot id to invalid
            field?.id = ID_INVALID

            // Set new coin's slot id to this slot
            value?.slotId = id

            field = value
        }

    /**
     * The page which this slot is in
     */
    var bookPage = BookPage()
    var line1Text = ""
    var line2Text = ""
    var line3Text = ""
    var size = SIZE_DOLLAR
    var rowNum = -1
    var colNum = -1
    var denomination = 0.0

    constructor(size: Int, line1Text: String, line2Text: String) : this() {
        this.size = size
        this.line1Text = line1Text
        this.line2Text = line2Text
    }

    /**
     * Finds if slot has a coin in it
     *
     * @return True if slot has a coin in it, otherwise false
     */
    fun isSlotFilled() : Boolean {
        // If coin is not null, it's filled
        return coin != null
    }

    fun removeCoin() {
        coin = null
    }

    /**
     * Saves a book page slot to the database. If the slot is not already in the database (id = 0) the slot is added.
     *   If the slot is already in the database, it is updated to reflect any changes.
     *
     * @param api an API object to use to connect to the database
     *
     * @return How many rows were affected by the sql command
     */
    override fun saveToDb(api: NumismatistAPI) : Int {
        val sql : String

        val rows : Int

        if(id != ID_INVALID) {
            sql = "UPDATE PageSlots SET PageID=${bookPage.id}, RowNum=${rowNum}, ColNum=${colNum}, " +
                    "Denomination=${denomination}, Label=\"${line1Text}\", Label2=\"${line2Text}\", " +
                    "Label3=\"${line3Text}\"\n" +
                    "WHERE ID=${id};"

            rows = api.runUpdate(sql)

            if(rows == 1 && coin != null)
                coin!!.saveToDb(api)
        }
        else {
            sql = "INSERT INTO PageSlots(PageID, RowNum, ColNum, Denomination, Label, Label2, Label3)\n" +
                    "VALUES(${bookPage.id}, ${rowNum}, ${colNum}, ${denomination}, " +
                    "\"${line1Text}\", \"${line2Text}\", \"${line3Text}\");"

            rows = api.runUpdate(sql)

            // If successful, set the new ID to this object
            if(rows == 1) {
                val newID = api.getGeneratedKeys()
                if(newID.next()) {
                    id = Integer.parseInt(newID.getString("GENERATED_KEY"))
                }

                if(coin != null)
                    // TODO: Handle Errors
                    coin?.saveToDb(api)
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

        val sql = "DELETE FROM PageSlots WHERE ID=${id}"

        val rows = api.runUpdate(sql)

        api.disconnect()

        if(rows==1) {
            // Remove the coin in the slot
            // TODO: Handle Error
            coin?.removeFromDb(api)
        }

        return rows == 1
    }
}