package items

import NumismatistAPI
import java.io.FileNotFoundException
import java.sql.SQLException
import java.util.*

class Coin : SetItem()
{
    companion object {
        const val HALF_PENNY = 0.005
        const val PENNY = 0.01
        const val NICKEL = 0.05
        const val DIME = 0.10
        const val QUARTER = 0.25
        const val HALF_DOLLAR = 0.50
        const val DOLLAR = 1.0

        val MINT_MARKS = arrayOf("", "C", "CC", "D", "O", "P", "S", "W")
        val CONDITIONS = arrayOf("", "AG-3", "G-4", "VG-8", "F-12", "VF-20", "EF-40", "AU-50", "MS-60", "MS-63", "MS-65", "MS-66", "MS-67", "PF-67")
    }

    var mintMark = ""
        set(value) {
            field = if(value.length <= 3)
                value
            else
                value.substring(0 .. 2)
        }

    /**
     * ID of the book slot this coin is in. If set to ID_INVALID then it's not in a book
     */
    var slotId : Int = ID_INVALID

    /**
     * Creates a copy of this coin.
     *
     * @return A copy of this coin
     */
    override fun copy() : Coin {
        val newCoin = Coin()

        newCoin.id = id
        newCoin.countryName = countryName
        newCoin.currency = currency
        newCoin.year = year
        newCoin.denomination = denomination
        newCoin.mintMark = mintMark
        newCoin.graded = graded
        newCoin.condition = condition
        newCoin.value = value
        newCoin.note = note
        newCoin.name = name
        newCoin.set = set

        newCoin.obvImgPath = obvImgPath
        newCoin.revImgPath = revImgPath
        newCoin.slotId = slotId
        newCoin.containerId = containerId

        return newCoin
    }

    /**
     * @return A string in the form of "<year>[-<mintMark>] <name>"
     */
    override fun toString() : String {

        var string = "$year"
        if(mintMark != "")
            string += "-$mintMark"

        string += " $name"

        return string
    }

    /**
     * Saves this coin to the database. If the coin is not already in the database (id = 0) the coin is added.
     *   If the coin is already in the database, it is updated to reflect any changes.
     *
     * @param api an API object to use to connect to the database
     *
     * @return How many rows were affected by the sql command
     */
    override fun saveToDb(api: NumismatistAPI) : Int {

        val sql: String

        val rows: Int

        // set SetID to null if necessary
        val newSetID = if(set == null)
            "null"
        else
            "" + set!!.id

        // set SlotID to null if necessary
        val slotID = if(slotId == ID_INVALID)
            "null"
        else
            "" + slotId

        // set ContainerID to null if necessary
        val containerID = if(containerId == ID_INVALID)
            "null"
        else
            "" + containerId

        val obvImgExt = if(obvImgPath != "")
            getObvImageExtFromPath()
        else
            ""

        val revImgExt = if(revImgPath != "")
            getRevImageExtFromPath()
        else
            ""

        if(id != 0) {

            sql = "UPDATE Coins SET CountryName=\"${countryName}\", CurrencyAbbr=\"${currency.nameAbbr}\", Type=\"${name}\", Yr=${year}, " +
                    "Denomination=${denomination}, CurValue=${value}, MintMark=\"${mintMark}\", Graded=${graded}, " +
                    "Grade=\"${condition}\", Error=${error}, ErrorType=\"${errorType}\", SetID=$newSetID, " +
                    "ObvImgExt=\"$obvImgExt\", RevImgExt=\"$revImgExt\", Note=\"${note}\", SlotID=$slotID, " +
                    "ContainerID=$containerID " +
                    "WHERE ID=${id};"

            rows = api.runUpdate(sql)

            if(rows == 1) {
                api.setCoin(id, this)
            }
        }
        else {

            sql = "INSERT INTO Coins(CountryName, CurrencyAbbr, Type, Yr, Denomination, CurValue, MintMark, Graded," +
                    " Grade, Error, ErrorType, SetID, SlotID, ContainerID, ObvImgExt, RevImgExt, Note)\n" +
                    "VALUES(\"${countryName}\", \"${currency.nameAbbr}\", \"${name}\", ${year}, ${denomination}, ${value}, \"${mintMark.uppercase(Locale.ROOT)}\", ${graded}," +
                    " \"${condition}\", ${error}, \"${errorType}\", $newSetID, $slotID, $containerID, \"$obvImgExt\", \"$revImgExt\", \"${note}\");"

            rows = api.runUpdate(sql)

            // If successful, set the new ID to this object
            if(rows == 1) {
                val newID = api.getGeneratedKeys()
                if(newID.next()) {
                    id = Integer.parseInt(newID.getString("GENERATED_KEY"))
                }
                api.getCoins().add(this)
            }
        }

        api.disconnect()

        return rows
    }

    /**
     * Removes this coin from the database
     *
     * @param api an API object to use to connect to the database
     *
     * @return True if coin was successfully removed, otherwise false
     */
    @Throws(SQLException::class, FileNotFoundException::class)
    override fun removeFromDb(api: NumismatistAPI) : Boolean {

        val sql = "DELETE FROM Coins WHERE ID=${id}"

        val rows = api.runUpdate(sql)

        api.disconnect()

        if(rows==1) {
            api.getCoins().remove(this)

            // Delete pictures
            deleteObvImage()
            deleteRevImage()
        }

        return rows == 1
    }
}