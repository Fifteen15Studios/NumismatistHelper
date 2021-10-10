import java.sql.*

class DatabaseConnection() {

    companion object {
        const val DEFAULT_DATABASE_SERVER = "localhost"
        const val DEFAULT_DATABASE_NAME = "CoinCollection"
        const val DEFAULT_DATABASE_USERNAME = "coins"
        const val DEFAULT_DATABASE_PASSWORD = "coinDatabasePassword1!"
        const val DEFAULT_PORT_NUMBER = 3306

        const val DEFAULT_TIMEOUT_SECONDS = 30
    }

    var statement: Statement? = null
    private lateinit var connection : Connection

    private var username = DEFAULT_DATABASE_USERNAME
    private var password = DEFAULT_DATABASE_PASSWORD
    private var databaseName = DEFAULT_DATABASE_NAME
    private var databaseServer = DEFAULT_DATABASE_SERVER
    private var port = DEFAULT_PORT_NUMBER
    var timeout = DEFAULT_TIMEOUT_SECONDS

    /**
     * @throws SQLException if connection to SQL Database fails
     * @throws ClassNotFoundException if jar file not loaded
     */
    @Throws(ClassNotFoundException::class, SQLException::class)
    constructor(username: String = DEFAULT_DATABASE_USERNAME,
                password: String = DEFAULT_DATABASE_PASSWORD,
                databaseName: String = DEFAULT_DATABASE_NAME,
                databaseServer: String = DEFAULT_DATABASE_SERVER,
                port: String =  "" + DEFAULT_PORT_NUMBER) : this()  {

        this.username = username
        this.password = password
        this.databaseServer = databaseServer
        this.databaseName = databaseName
        this.port = Integer.parseInt(port)

        try {
            connect(username, password, databaseName, databaseServer, port)
            disconnect()
        }
        catch (e: ClassNotFoundException) {
            throw e
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
     * @param port: IP port to use while connecting
     *
     * @throws SQLException if connection fails
     *
     * Connects to a database and stores the statement for later use.
     */
    @Throws(ClassNotFoundException::class, SQLException::class)
    fun connect(username: String = DEFAULT_DATABASE_USERNAME,
                password: String = DEFAULT_DATABASE_PASSWORD,
                databaseName: String = DEFAULT_DATABASE_USERNAME,
                databaseLocation: String = DEFAULT_DATABASE_SERVER,
                port: Int = DEFAULT_PORT_NUMBER) {
        connect(username, password, databaseName, databaseLocation, "" + port)
    }

    /**
     * @param username: Username for database access
     * @param password: Password for database access
     * @param databaseName: Name of database to be loaded. Defaults to "CoinProgram"
     * @param databaseLocation: Name or IP address of the SQL server hosting the database. Defaults to "localhost"
     * @param port: IP port to use while connecting
     *
     * @throws SQLException if connection fails
     *
     * Connects to a database and stores the statement for later use.
     */
    @Throws(ClassNotFoundException::class, SQLException::class)
    fun connect(username: String = DEFAULT_DATABASE_USERNAME,
                password: String = DEFAULT_DATABASE_PASSWORD,
                databaseName: String = DEFAULT_DATABASE_USERNAME,
                databaseLocation: String = DEFAULT_DATABASE_SERVER,
                port: String = "" + DEFAULT_PORT_NUMBER) {

        val url = "jdbc:mysql://$databaseLocation:$port/$databaseName"

        // Set timeout length. Unlimited if this is not called
        DriverManager.setLoginTimeout(timeout)

        // Try to connect to database
        connection = try {
            // Load the SQL driver
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(
                url, username, password
            )
        }catch (cnf : ClassNotFoundException) {
            throw cnf
        }
        catch (e: SQLException) {
            throw e
        }

        statement = connection.createStatement()
    }

    /**
     * Disconnects from the database
     */
    fun disconnect() {
        try {
            statement?.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * @param query SQL query to execute
     * @return Results from SQL query
     *
     * Runs a SQL Query and returns the results. If there was an error, returns null.
     */
    fun runQuery(query: String) : ResultSet? {

        return try {
            connect(username, password, databaseName, databaseServer, port)
            statement?.executeQuery(query)
        }
        catch (e: SQLException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * @param sql SQL command to run
     * @return Number of rows affected
     *
     * Runs a command like INSERT, UPDATE or DELETE and returns the affected number of rows
     * Returns -1 if there was an error
     */
    @Throws(SQLException::class)
    fun runUpdate(sql: String) : Int {
        return try {
            connect(username, password, databaseName, databaseServer, port)
            val changes = statement?.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS)

            return changes ?: -1
        }
        catch (e: SQLException) {
            e.printStackTrace()
            -1
        }
    }

    /**
     * Returns status of an update or insert. Implies that only 1 row should be updated at a time.
     *
     * @param rows How many rows were modified by the insert or update
     * @return A message about whether the command was successful
     */
    fun wasSuccessful(rows: Int): String {
        return when(rows) {
            1 -> NumismatistAPI.getString("db_message_success")!!
            0 -> NumismatistAPI.getString("db_message_noChange")!!
            -1 -> NumismatistAPI.getString("db_message_error")!!
            else -> NumismatistAPI.getString("db_message_multipleRows")!!
        }
    }
}