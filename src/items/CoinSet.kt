package items

import Main
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import kotlin.collections.ArrayList

class CoinSet {

    companion object {
        fun getSetsFromSql(connection: DatabaseConnection, sql: String = "") : ArrayList<CoinSet> {
            val sets = ArrayList<CoinSet>()

            val query = if (sql == "")
                """
                SELECT *
                FROM Sets
                ORDER BY ID;
                """.trimIndent()
            else
                sql

            val results: ResultSet? = connection.runQuery(query)

            // Show sets
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

                    // for each set found
                    for (i in 0 until size) {
                        results.next()

                        val newSet = CoinSet()
                        newSet.id = results.getInt("ID")
                        newSet.name = Objects.requireNonNullElse(results.getString("Name"), "")
                        newSet.year = results.getInt("Yr")
                        newSet.value = results.getDouble("CurValue")
                        newSet.note = Objects.requireNonNullElse(results.getString("Note"), "")

                        // Add coins to set
                        newSet.getCoinsFromSet()

                        sets.add(newSet)
                    }
                }
            }
            catch (e: SQLException) {
                e.printStackTrace()
            }

            return sets
        }
    }

    var id = 0
    var name  = ""
    var value = 0.0
    var year = 0
    var note = ""
    var obvImgExt = ""
    var revImgExt = ""

    val coins = ArrayList<Coin>()
    val removedCoins = ArrayList<Coin>()

    fun getFaceValue() : Double {
        var value = 0.0

        for(coin in coins)
            value += coin.denomination

        return value
    }

    fun saveToDb(connection: DatabaseConnection, editing: Boolean = false) : String {
        val sql: String
        val returnMessage: String

        if(editing) {
            sql = "UPDATE Sets SET Name=\"$name\", Yr=$year, CurValue=$value, Note=\"$note\"\n" +
                    "WHERE ID=$id;"

            returnMessage = when (connection.runUpdate(sql)) {
                0 -> {
                    DatabaseConnection.NO_CHANGE_MESSAGE
                }
                1 -> {
                    DatabaseConnection.SUCCESS_MESSAGE
                }
                -1 -> {
                    DatabaseConnection.ERROR_MESSAGE
                }
                else -> {
                    DatabaseConnection.MULTIPLE_ROWS_MESSAGE
                }
            }
        }
        else {
            sql = "INSERT INTO Sets(Name, Yr, CurValue, Note)\n" +
                    "VALUES(\"$name\", $year, $value, \"$note\");"

            val rows = connection.runUpdate(sql)

            // If successful, add coins to set
            returnMessage = if(rows == 1) {
                var errors = 0
                val results: ResultSet? = connection.runQuery("SELECT LAST_INSERT_ID();")
                try {
                    // Find result of newly added set
                    if (results != null) {
                        results.next()
                        id = results.getInt("LAST_INSERT_ID()")
                    }

                    // Add coins that have been added
                    for (coin in coins) {
                        coin.setID = id

                        val coinSql = "UPDATE Coins SET SetID=${coin.setID}\n" +
                                "WHERE ID=${coin.id};"
                        val coinRows = connection.runUpdate(coinSql)
                        if(coinRows != 1)
                            errors++
                    }

                    // Remove coins that have been removed
                    for (coin in removedCoins) {
                        coin.setID = 0

                        val coinSql = "UPDATE Coins SET SetID=null\n" +
                                "WHERE ID=${coin.id};"
                        val coinRows = connection.runUpdate(coinSql)
                        if(coinRows != 1)
                            errors++
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                }

                if(errors == 0)
                    DatabaseConnection.SUCCESS_MESSAGE
                else
                    DatabaseConnection.ERROR_MESSAGE
            }
            else if(rows == 0) {
                DatabaseConnection.NO_CHANGE_MESSAGE
            }
            else if (rows == -1) {
                DatabaseConnection.ERROR_MESSAGE
            }
            else {
                DatabaseConnection.MULTIPLE_ROWS_MESSAGE
            }
        }

        return returnMessage
    }

    fun deleteFromDb(connection: DatabaseConnection) : Boolean {

        getCoinsFromSet()

        val totalCoins = coins.size
        var removedCoins = 0

        // Remove all of the coins in the set
        for (coin in coins)
            if(coin.deleteFromDb(connection))
                removedCoins++

        // If all of the individual coins were removed, remove the set
        if(totalCoins == removedCoins) {
            val sql = "DELETE FROM Sets WHERE ID=$id"

            val rows = connection.runUpdate(sql)

            if (rows == 1)
                return true
        }

        return false
    }

    fun addCoin(coin: Coin) {
        coins.add(coin)
    }

    fun removeCoin(coin: Coin) {
        removedCoins.add(coin)
        coins.remove(coin)
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
        return "set-$id-obv"
    }

    private fun getRevImageName() : String {
        return "set-$id-rev"
    }

    fun getCoinsFromSet() {

        coins.clear()

        val connection = DatabaseConnection(Main.getSettingDatabaseUsername(),
                Main.getSettingDatabasePassword(),
                Main.getSettingDatabaseName(),
                Main.getSettingDatabaseServer())

        val sql = "SELECT * FROM Coins WHERE SetID=$id;"

        val results = connection.runQuery(sql)

        if(results !=null) {
            try {
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

                    addCoin(newCoin)
                }
            }
            catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }
}