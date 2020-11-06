package items

import Main
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import kotlin.collections.ArrayList

class Coin
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

        fun getCoinsFromSql(connection: DatabaseConnection, sql: String = "") : ArrayList<Coin> {
            val coins = ArrayList<Coin>()

            val query = if (sql == "")
                """
                SELECT *
                FROM Coins
                WHERE SetID is null
                ORDER BY ID;
                """.trimIndent()
            else
                sql

            val results: ResultSet? = connection.runQuery(query)

            // Show coins
            try {
                if (results != null) {
                    // Find out how many rows are in the result
                    var size = 0
                    try {
                        results.last()
                        size = results.row
                        results.beforeFirst()
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }

                    // for each coin found
                    for (i in 0 until size) {
                        results.next()

                        val newCoin = Coin()
                        newCoin.id = results.getInt("ID")
                        newCoin.country = Objects.requireNonNullElse(results.getString("Country"), "US")
                        newCoin.name = Objects.requireNonNullElse(results.getString("Type"), "")
                        newCoin.year = results.getInt("Yr")
                        newCoin.denomination = results.getDouble("Denomination")
                        newCoin.value = results.getDouble("CurValue")
                        newCoin.mintMark = Objects.requireNonNullElse(results.getString("MintMark"), "")
                        newCoin.graded = results.getBoolean("Graded")
                        newCoin.condition = Objects.requireNonNullElse(results.getString("Grade"), "")
                        newCoin.error = results.getBoolean("Error")
                        newCoin.errorType = Objects.requireNonNullElse(results.getString("ErrorType"), "")
                        newCoin.note = Objects.requireNonNullElse(results.getString("Note"), "")

                        if(results.getString("ObvImgExt") != null)
                            newCoin.obvImgExt = results.getString("ObvImgExt")
                        if(results.getString("RevImgExt") != null)
                            newCoin.revImgExt = results.getString("RevImgExt")

                        coins.add(newCoin)
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }

            return coins
        }
    }

    fun copy() : Coin {
        val newCoin = Coin()

        newCoin.id = 0
        newCoin.country = country
        newCoin.year = year
        newCoin.denomination = denomination
        newCoin.mintMark = mintMark
        newCoin.graded = graded
        newCoin.condition = condition
        newCoin.value = value
        newCoin.note = note
        newCoin.name = name
        newCoin.setID = null

        newCoin.obvImgExt = ""
        newCoin.revImgExt = ""

        return newCoin
    }

    var id = 0
    var country = "US"
    var year = 0
    var denomination = 0.0
    set(value) {
        if(value > 0)
            field = value
    }

    var mintMark = ""
    set(value) {
        if(MINT_MARKS.contains(value))
            field = value
    }

    var condition = ""
    set(value) {
        if(CONDITIONS.contains(value))
            field = value
    }

    var graded = false
    var name = ""
    var value = 0.0
    set(value) {
        if(value > 0)
            field = value
    }

    var error = false
    var errorType = ""
    var setID: Int? = null
    var note = ""

    var obvImgExt = ""
    var revImgExt = ""

    override fun toString() : String {

        var string = "$year"
        if(mintMark != "")
            string += "-$mintMark"

        string += " $name"

        return string
    }

    fun saveToDb(connection: DatabaseConnection) : Int {

        val sql: String

        val rows: Int

        // set SetID to null if necessary
        val newSetID = if(setID == null || setID == 0)
            "null"
        else
            "" + setID

        if(id != 0) {
            sql = "UPDATE Coins SET Country=\"" + country + "\"" +
                    ",Type=\"" + name + "\"" +
                    ",Yr=" + year.toString() +
                    ",Denomination=" + denomination.toString() +
                    ",CurValue=" + value.toString() +
                    ",MintMark=\"" + mintMark + "\"" +
                    ",Graded=" + graded.toString() +
                    ",Grade=\"" + condition + "\"" +
                    ",Error=" + error.toString() +
                    ",ErrorType=\"" + errorType + "\"" +
                    ",SetID=" + newSetID +
                    ",Note=\"" + note + "\"" +
                    "WHERE ID=" + id.toString() + ";"

            rows = connection.runUpdate(sql)
        }
        else {
            sql = "INSERT INTO Coins(Country, Type, Yr, Denomination, CurValue, MintMark, Graded," +
                    " Grade, Error, ErrorType, SetID, Note)\n" +
                    "VALUES(\"$country\", \"$name\", $year, $denomination, $value, \"$mintMark\", $graded," +
                    " \"$condition\", $error, \"$errorType\", $newSetID, \"$note\");"

            rows = connection.runUpdate(sql)

            // If successful, set the new ID to this object
            if(rows == 1) {
                val results: ResultSet? = connection.runQuery("SELECT LAST_INSERT_ID();")
                try {
                    // Find result of newly added set
                    if (results != null) {
                        results.next()
                        id = results.getInt("LAST_INSERT_ID()")
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        }

        return rows
    }

    fun getImagePath(obverse: Boolean) : String {
        val os = System.getProperty("os.name")
        val slash = if (os.toLowerCase().contains("windows")) "\\" else "/"

        val path = Main.getSettingImagePath() + slash

        return if(obverse)
            path + getObvImageName() + obvImgExt
        else
            path + getRevImageName() + revImgExt
    }

    private fun getObvImageName() : String {
        return "coin-$id-obv"
    }

    private fun getRevImageName() : String {
        return "coin-$id-rev"
    }

    fun deleteFromDb(connection: DatabaseConnection) : Boolean {

        val sql = "DELETE FROM Coins WHERE ID=$id"

        val rows = connection.runUpdate(sql)

        if(rows == 1)
            return true

        return false
    }
}