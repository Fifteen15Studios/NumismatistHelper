package items

import NumismatistAPI
import java.io.FileNotFoundException
import java.sql.SQLException

class Bill : SetItem() {

    var seriesLetter = ""

    var serial = ""
    var signatures = ""
    var replacement = false

    companion object {
        val CONDITIONS = arrayOf("", "G-4", "VG-8", "F-12", "VF-20", "EF-40", "AU-50", "UC-63")
    }

    /**
     * Copies the bill
     *
     * @return A copy of this bill
     */
    override fun copy() : Bill {
        val newBill = Bill()

        newBill.countryName = countryName
        newBill.currency = currency
        newBill.name = name
        newBill.year = year
        newBill.seriesLetter = seriesLetter
        newBill.denomination = denomination
        newBill.value = value
        newBill.set = set
        newBill.graded = graded
        newBill.condition = condition
        newBill.note = note
        newBill.obvImgPath = obvImgPath
        newBill.revImgPath = revImgPath
        newBill.containerId = containerId

        return newBill
    }

    override fun toString(): String {
        var out = "$year"

        if(seriesLetter != "")
            out += "-$seriesLetter"

        out += " $name"

        return out
    }

    /**
     * Saves this bill to the database. If the bill is not already in the database (id = 0) the bill is added.
     *   If the bill is already in the database, it is updated to reflect any changes.
     *
     * @param api an API object to use to connect to the database
     *
     * @return How many rows were affected by the sql command
     */
    override fun saveToDb(api: NumismatistAPI): Int {

        // set SetID to null if necessary
        val newSetID = if(set == null)
            "null"
        else
            "" + set!!.id

        val containerID = if(containerId == ID_INVALID)
            "null"
        else
            "" + containerId

        val sql : String
        val rows : Int
        if(id != 0) {

            sql = "UPDATE Bills SET CountryName=\"${countryName}\", CurrencyAbbr=\"${currency.nameAbbr}\", Type=\"${name}\", Yr=${year}, " +
                    "Denomination=${denomination}, CurValue=${value}, Graded=${graded}, Grade=\"${condition}\", " +
                    "SeriesLetter=\"${seriesLetter}\", Serial=\"${serial}\", Signatures=\"${signatures}\", ContainerID=$containerID, " +
                    "Error=${error}, ErrorType=\"${errorType}\", Note=\"${note}\", SetID=$newSetID, Replacement=${replacement}\n" +
                    "WHERE ID=${id};"

            rows = api.runUpdate(sql)

            if(rows == 1)
                api.setBill(id, this)
        }
        else {

            sql = "INSERT INTO Bills(CountryName, CurrencyAbbr, Type, Yr, SeriesLetter, Denomination, CurValue, Graded, Grade, " +
                    "Error, ErrorType, Serial, Signatures, ContainerID, Note, SetID, Replacement)\n" +
                    "VALUES(\"${countryName}\", \"${currency.nameAbbr}\", \"${name}\", ${year}, \"${seriesLetter}\", ${denomination}, ${value}, " +
                    "${graded}, \"${condition}\", ${error}, \"${errorType}\", \"${serial}\", \"${signatures}\", " +
                    "$containerID, \"${note}\", $newSetID, ${replacement});"

            rows = api.runUpdate(sql)

            // If successful, set the new ID to this object
            if(rows == 1) {
                val newID = api.getGeneratedKeys()
                if(newID.next()) {
                    id = Integer.parseInt(newID.getString("GENERATED_KEY"))
                }
                api.getBills().add(this)
            }
        }

        api.disconnect()

        return rows
    }

    /**
     * Removes this bill from the database
     *
     * @param api an API object to use to connect to the database
     *
     * @return True if bill was successfully removed, otherwise false
     */
    @Throws(SQLException::class, FileNotFoundException::class)
    override fun removeFromDb(api: NumismatistAPI) : Boolean {

        val sql = "DELETE FROM Bills WHERE ID=${id}"

        val rows = api.runUpdate(sql)

        api.disconnect()

        if(rows==1) {
            api.getBills().remove(this)

            // Delete pictures
            deleteObvImage()
            deleteRevImage()
        }

        return rows == 1
    }
}