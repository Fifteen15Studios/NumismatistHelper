package items

import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import kotlin.collections.ArrayList

class Bill{

    var id = 0
    var country = "US"
    var year = 0
    var seriesLetter = ""
    var denomination = 0.0
    var value = 0.0
    var graded = false
    var condition = ""
    var name = ""
    var serial = ""
    var signatures = ""
    var star = false
    var error = false
    var errorType = ""
    var note = ""
    var district = ""
    var plateSeriesObv = ""
    var plateSeriesRev = ""
    var notePosition = ""
    var obvImgExt = ""
    var revImgExt = ""

    companion object {

        val CONDITIONS = arrayOf("", "G-4", "VG-8", "F-12", "VF-20", "EF-40", "AU-50", "UC-63")

        fun getBillsFromSql(connection: DatabaseConnection, sql: String = "") : ArrayList<Bill> {
            val bills = ArrayList<Bill>()

            val query = if (sql == "")
                """
                SELECT *
                FROM Bills
                ORDER BY ID;
                """.trimIndent()
            else
                sql

            val results: ResultSet? = connection.runQuery(query)

            // Show bills
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

                    // for each bill found
                    for (i in 0 until size) {
                        results.next()

                        val newBill = Bill()
                        newBill.id = results.getInt("ID")
                        newBill.country = Objects.requireNonNullElse(results.getString("Country"), "US")
                        newBill.name = Objects.requireNonNullElse(results.getString("Type"), "")
                        newBill.year = results.getInt("Yr")
                        newBill.seriesLetter = results.getString("SeriesLetter")
                        newBill.denomination = results.getDouble("Denomination")
                        newBill.value = results.getDouble("CurValue")
                        newBill.graded = results.getBoolean("Graded")
                        newBill.serial = results.getString("Serial")
                        newBill.condition = Objects.requireNonNullElse(results.getString("Grade"), "")
                        newBill.error = results.getBoolean("Error")
                        newBill.errorType = Objects.requireNonNullElse(results.getString("ErrorType"), "")
                        newBill.plateSeriesObv = Objects.requireNonNullElse(results.getString("PlateSeriesObv"), "")
                        newBill.plateSeriesRev = Objects.requireNonNullElse(results.getString("PlateSeriesRev"), "")
                        newBill.notePosition = Objects.requireNonNullElse(results.getString("NotePosition"), "")
                        newBill.star = results.getBoolean("Star")
                        newBill.note = Objects.requireNonNullElse(results.getString("Note"), "")

                        if(results.getString("ObvImgExt") != null)
                            newBill.obvImgExt = results.getString("ObvImgExt")
                        if(results.getString("RevImgExt") != null)
                            newBill.revImgExt = results.getString("RevImgExt")

                        bills.add(newBill)
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }

            return bills
        }
    }

    fun saveToDb(connection: DatabaseConnection, editing: Boolean): Int {
        val sql = if(editing) {
            "UPDATE Bills SET Country=\"$country\", Type=\"$name\", Yr=$year, " +
                    "Denomination=$denomination, CurValue=$value, Graded=$graded, Grade=\"$condition\", " +
                    "SeriesLetter=\"$seriesLetter\", Serial=\"$serial\", Signatures=\"$signatures\", " +
                    "Error=$error, ErrorType=\"$errorType\", Note=\"$note\", " +
                    "PlateSeriesObv=\"$plateSeriesObv\", PlateSeriesRev=\"$plateSeriesRev\", " +
                    "NotePosition=\"$notePosition\", District=\"$district\", Star=$star\n" +
                    "WHERE ID=$id;"
        }
        else {
            "INSERT INTO Bills(Country, Type, Yr, SeriesLetter, Denomination, CurValue, Graded, Grade, Error, " +
                    "ErrorType, Serial, Signatures, Note, PlateSeriesObv, PlateSeriesRev, NotePosition, " +
                    "District, Star)\n" +
                    "VALUES(\"$country\", \"$name\", $year, \"$seriesLetter\", $denomination, $value, " +
                    "$graded, \"$condition\", $error, \"$errorType\", \"$serial\", \"$signatures\", \"$note\", " +
                    "\"$plateSeriesObv\", \"$plateSeriesRev\", \"$notePosition\", \"$district\", $star);"
        }

        return connection.runUpdate(sql)
    }

    fun deleteFromDb(connection: DatabaseConnection) : Boolean {

        val sql = "DELETE FROM Bills WHERE ID=$id"

        val rows = connection.runUpdate(sql)

        if(rows == 1)
            return true

        return false
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
        return "bill-$id-obv"
    }

    private fun getRevImageName() : String {
        return "bill-$id-rev"
    }

    override fun toString(): String {
        var out = "$year"

        if(seriesLetter != "")
            out += "-$seriesLetter"

        out += " $name"

        return out
    }
}