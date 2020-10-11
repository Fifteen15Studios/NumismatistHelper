package items

import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.*

class DatabaseConnection() {

    companion object {
        const val SUCCESS_MESSAGE = "Success!"
        const val NO_CHANGE_MESSAGE = "No changes made in database. Something probably went wrong."
        const val ERROR_MESSAGE = "Error occurred while updating database. Please try again"
        const val MULTIPLE_ROWS_MESSAGE = "Updated multiple rows. Make sure that this is what you wanted!"
    }

    var statement: Statement? = null

    /**
     * @throws SQLException if connection to SQL Database fails
     * @throws ClassNotFoundException if jar file not loaded
     */
    @Throws(ClassNotFoundException::class, SQLException::class)
    constructor(username: String, password: String, databaseName: String = "CoinProgram",
                databaseServer: String = "localhost") : this()  {

        try {
            // Load sql driver
            Class.forName("com.mysql.jdbc.Driver")
        }
        catch (e: ClassNotFoundException) {
            throw e
        }
        try {
            connect(username, password, databaseName, databaseServer)
        }
        catch (e: SQLException) {
            throw e
        }
    }

    /**
     * @param username: Username for database access
     * @param password: Password for database access
     * @param databaseName: Name of database to be loaded. Defaults to "CoinProgram"
     * @param databaseLocation: Name or IP address of the SQL server hosting the database. Defaults to "localhost"
     *
     * @throws SQLException if connection fails
     *
     * Connects to a database and stores the statement for later use.
     */
    fun connect(username: String, password: String, databaseName: String = "CoinProgram",
                databaseLocation: String = "localhost") {

        // Set username and password for connection
        val connectionProps = Properties()
        connectionProps["user"] = username
        connectionProps["password"] = password

        val url = "jdbc:mysql://$databaseLocation:3306/"

        // Try to connect to database
        val connection = try {
            DriverManager.getConnection(
                    url,
                    connectionProps
            )
        }
        catch (e: SQLException) {
            throw e
        }

        // Create statement and start using database
        if(connection != null) {
            statement = connection.createStatement()
            statement?.executeQuery("USE $databaseName;")
        }
    }

    /**
     * @param query: SQL query to execute
     * @return : Results from SQL query
     *
     * Runs a SQL Query and returns the results. If there was an error, returns null.
     */
    fun runQuery(query: String) : ResultSet? {
        return try {
            statement?.executeQuery(query)
        }
        catch (e: SQLException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * @param sql: SQL command to run
     * @return : number of rows affected
     *
     * Runs a command like INSERT, UPDATE or DELETE and returns the affected number of rows
     * Returns -1 if there was an error
     */
    fun runUpdate(sql: String) : Int {
        return try {
            val changes = statement?.executeUpdate(sql)

            return changes ?: -1
        }
        catch (e: SQLException) {
            e.printStackTrace()
            -1
        }
    }

    fun wasSuccessful(rows: Int): String {
        return when(rows) {
            1 -> SUCCESS_MESSAGE
            0 -> NO_CHANGE_MESSAGE
            -1 -> ERROR_MESSAGE
            else -> MULTIPLE_ROWS_MESSAGE
        }
    }
}