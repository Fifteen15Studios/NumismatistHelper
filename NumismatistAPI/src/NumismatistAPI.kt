import items.*
import items.Currency
import items.Set
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.*
import java.net.URLDecoder
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

@Suppress("unused")
class NumismatistAPI {

    private var connection = DatabaseConnection()

    private var databaseServer = DatabaseConnection.DEFAULT_DATABASE_SERVER
    private var databaseName = DatabaseConnection.DEFAULT_DATABASE_NAME
    private var databasePort = DatabaseConnection.DEFAULT_PORT_NUMBER
    private var databaseUserName = DatabaseConnection.DEFAULT_DATABASE_USERNAME
    private var databasePassword = DatabaseConnection.DEFAULT_DATABASE_PASSWORD

    var connectionTimeout = DatabaseConnection.DEFAULT_TIMEOUT_SECONDS
        set(value) {
            field = value
            connection.timeout = value
        }

    var imagePath = ""

    // The below objects act like a local cache of the database
    /**
     * A list of coins that are not in a set or book
     */
    private var coins = ArrayList<Coin>()
    //A list of coins that are in a set. Should be empty after all database items are filled.
    private var coinsInSets = ArrayList<Coin>()
    //A list of coins that are in a book. Should be empty after all database items are filled.
    private var coinsInBooks = ArrayList<Coin>()
    private var bills = ArrayList<Bill>()
    private var billsInSets = ArrayList<Bill>()
    private var sets = ArrayList<Set>()
    private var setsInSets = ArrayList<Set>()
    private var books = ArrayList<Book>()
    private var containers = ArrayList<Container>()
    private var countries = ArrayList<Country>()
    private var currencies = ArrayList<Currency>()

    var topOfCountriesList = ArrayList<String>()
        set(value) {
            field = value
            countries = Country.sort(countries, topOfCountriesList)
            countryListener?.countryListChanged(countries)
        }

    var coinListener : CoinListener? = null
    var billListener : BillListener? = null
    var setListener : SetListener? = null
    var bookListener : BookListener? = null
    var countryListener : CountryListener? = null

    interface CoinListener {
        /**
         * @param coins A list of coins retrieved from the database
         */
        fun coinListRetrievedFromFb(coins :ArrayList<Coin>) {
        }
    }

    interface BillListener {
        /**
         * @param bills A list of bills (banknotes) retrieved from the database
         */
        fun billListRetrievedFromFb(bills :ArrayList<Bill>) {
        }
    }

    interface SetListener {
        /**
         * @param sets A list of sets retrieved from the database
         */
        fun setListRetrievedFromFb(sets :ArrayList<Set>)  {
        }
    }

    interface BookListener {
        /**
         * @param books A list of books retrieved from the database
         */
        fun bookListRetrievedFromFb(books :ArrayList<Book>)  {
        }
    }

    interface CountryListener {
        /**
         * @param countries A list of countries
         */
        fun countryListChanged(countries :ArrayList<Country>)  {}
    }

    companion object {

        fun getString(stringName: String): String? {
            return getString(stringName, Locale.getDefault())
        }

        fun getString(stringName: String, locale: Locale): String? {
            return ResourceBundle.getBundle("res.apiStrings", locale).getString(stringName)
        }

        /**
         * Gets the full path of the res folder. Takes into effect whether the program is running from a .jar file
         *
         * @param path The base folder to look in. Such as XML
         * @param subPath Sub-folders to look in, if any
         *
         * @return The full path of the specific resource folder you are looking for
         */
        fun getResPath(path: String, vararg subPath: String): String {

            var newPath = path
            // Becomes / for *nix systems, and \ for Windows
            val separator = System.getProperty("file.separator")
            newPath = "res/$newPath"

            // If running from a JAR file
            return if (!NumismatistAPI::class.java.getResource("NumismatistAPI.class")!!.toString().startsWith("file:")) {
                val finalPath = StringBuilder(newPath)
                finalPath.append("/")
                for (addPath in subPath) {
                    finalPath.append(addPath)
                    finalPath.append("/")
                }
                Thread.currentThread().contextClassLoader.getResource(finalPath.toString())!!.toString()
            } else {
                var resPath = ""
                try {
                    val classLoader = javaClass.classLoader

                    // String returned is URL, so it's HTML encoded. Must decode first
                    val decodedPath = URLDecoder.decode(classLoader.getResource(newPath)?.file, "UTF-8")
                    val file = File(decodedPath)
                    resPath = file.absolutePath
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
                // set path based on OS - slashes change based on OS
                val finalPath = StringBuilder(resPath)
                finalPath.append(separator)
                for (addPath in subPath) {
                    finalPath.append(addPath)
                    finalPath.append(separator)
                }
                finalPath.toString()
            }
        }

        /**
         * Gets a file from the resources folder
         *
         * @param path which resource folder to get the file from
         * @return If the path is not blank, and path contains a file, returns the requested file. Otherwise, returns null
         */
        fun getFileFromRes(path: String) : File? {
            return if(path.isBlank()) {
                println("path is blank")
                null
            }
            else {
                // If we're running a jar file
                if(path.startsWith("jar")) {
                    val pathSub = path.substring(path.lastIndexOf("!") + 2)
                    streamToFile(NumismatistAPI::class.java.classLoader.getResourceAsStream(pathSub))
                }
                else {
                    File(path)
                }
            }
        }

        /**
         * Converts an InputStream to a File
         *
         * @param in InputStream to convert
         * @return Resulting file
         */
        private fun streamToFile(`in`: InputStream?): File? {
            return if (`in` == null) {
                null
            } else try {
                val f = File.createTempFile(`in`.hashCode().toString(), ".tmp")
                f.deleteOnExit()
                val out = FileOutputStream(f)
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (`in`.read(buffer).also { bytesRead = it } != -1) {
                    out.write(buffer, 0, bytesRead)
                }
                f
            } catch (e: IOException) {
                null
            }
        }

        /**
         * Copies source file to destination file.
         *
         * @param src The source file to be copied
         * @param dest Path of the destination of the copied file
         *
         * @return True if file is successfully copied. False if source file doesn't exist or if copy fails.
         */
        @Throws(IOException::class)
        fun copyFile(src: File, dest: String): Boolean {
            return copyFile(src, File(dest))
        }

        /**
         * Copies source file to destination file.
         *
         * @param src Path of the source file to be copied
         * @param dest The destination of the copied file
         *
         * @return True if file is successfully copied. False if source file doesn't exist or if copy fails.
         */
        @Throws(IOException::class)
        fun copyFile(src: String, dest: File): Boolean {
            return copyFile(File(src), dest)
        }

        /**
         * Copies source file to destination file.
         *
         * @param src Path of the source file to be copied
         * @param dest Path of the destination of the copied file
         *
         * @return True if file is successfully copied. False if source file doesn't exist or if copy fails.
         */
        @Throws(IOException::class)
        fun copyFile(src: String, dest: String): Boolean {
            return copyFile(File(src), File(dest))
        }

        /**
         * Copies source file to destination file.
         *
         * @param src The source file to be copied
         * @param dest The destination of the copied file
         *
         * @return True if file is successfully copied. False if source file doesn't exist or if copy fails.
         */
        @Throws(IOException::class)
        fun copyFile(src: File, dest: File): Boolean {
            if (!src.exists()) return false

            // Create directories and file if it doesn't exist, if file already exists will do nothing
            try {
                if (dest.isFile) {
                    dest.parentFile.mkdirs()
                    dest.createNewFile()
                }
            } catch (e: IOException) {
                throw e
            }

            // Copy the file
            try {
                FileInputStream(src).use { `is` ->
                    FileOutputStream(dest).use { os ->
                        // buffer size 1K
                        val buf = ByteArray(1024)
                        var bytesRead: Int
                        while (`is`.read(buf).also { bytesRead = it } > 0) {
                            os.write(buf, 0, bytesRead)
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                return false
            }
            return true
        }
    }

    /**
     * Does an initial query of the database to get all relevant information and cache it in objects. This should
     * be run in a background process
     */
    private fun getAllInfoFromDB() {

        currencies = ArrayList()
        countries = ArrayList()
        containers = ArrayList()
        coins = ArrayList()
        bills = ArrayList()
        sets = ArrayList()
        books = ArrayList()

        getCurrenciesFromDb()
        getCountriesFromDb()

        getContainersFromDb()
        getCoinsFromDb()
        getBillsFromDb()
        getSetsFromDb()
        // Needs to be last, otherwise this could take much longer
        getBooksFromDb()
    }

    /**
     * Sets the connection information for the database, connects to the database, and then calls getAllInfoFromDB
     * to cache the initial queries. This should be called from a background process.
     *
     * @see getAllInfoFromDB
     *
     * @param server Name or IP address of the database server
     * @param dbName Name of the database to connect to on the server
     * @param port Port number to use for the connection
     * @param username Username to use for the database connection
     * @param password Password to use for the database connection
     */
    @Throws(ClassNotFoundException::class, SQLException::class)
    fun setDbInfo(server : String, dbName: String, port: Int, username: String, password : String) {
        databaseServer = server
        databaseName = dbName
        databasePort = port
        databaseUserName = username
        databasePassword = password

        connection = DatabaseConnection(databaseUserName, databasePassword, databaseName, databaseServer, "" + databasePort)

        connection.connect(databaseUserName, databasePassword, databaseName, databaseServer, databasePort)
        getAllInfoFromDB()
    }

    /**
     * Sets the connection information for the database, connects to the database, and then calls getAllInfoFromDB
     * to cache the initial queries. This should be called from a background process.
     *
     * @see getAllInfoFromDB
     *
     * @param server Name or IP address of the database server
     * @param dbName Name of the database to connect to on the server
     * @param port Port number to use for the connection
     * @param username Username to use for the database connection
     * @param password Password to use for the database connection
     */
    @Throws(ClassNotFoundException::class, SQLException::class)
    fun setDbInfo(server : String, dbName: String, port: String, username: String, password : String) {
        setDbInfo(server, dbName, Integer.parseInt(port), username, password)
    }

    /**
     * Disconnects from the database
     */
    fun disconnect() {
        connection.disconnect()
    }

    /**
     * Queries the database
     *
     * @param query The MySQL query to run
     *
     * @return The results of the query
     */
    fun runQuery(query: String) : ResultSet? {
        return try {
            connection.runQuery(query)
        }
        catch (e: SQLException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Run a command on the database which changes existing data (like UPDATE or INSERT)
     *
     * @param sql The MySQL command to run
     *
     * @return Number of rows affected
     */
    fun runUpdate(sql: String) : Int {
        return try {
            return connection.runUpdate(sql)
        }
        catch (e: SQLException) {
            e.printStackTrace()
            -1
        }
    }

    /**
     * Gets the current list of coins which are not in a set or book
     *
     * @return A list of coins in the collection which are not in a set or book
     */
    fun getCoins() : ArrayList<Coin> {
        return coins
    }

    /**
     * Finds a coin with a specific ID in the list and sets it equal to a different coin.
     *
     * @param id The ID of the coin to find
     * @param coin The coin to replace it with. If null, the original coin is removed from the list.
     */
    fun setCoin(id: Int, coin: Coin?) {
        for(i in 0 until coins.size) {
            if(coins[i].id == id) {
                if (coin != null)
                    coins[i] = coin
                else
                    coins.remove(coins[i])

                return
            }
        }
    }

    /**
     * Gets all coins from the database
     *
     * @return List of coins that fit your query
     */
    @Throws(SQLException::class, ClassNotFoundException::class)
    private fun getCoinsFromDb(){

        val query =
            """
                SELECT *
                FROM Coins                
                ORDER BY ID;
                """.trimIndent()


        val results: ResultSet? = try {
            connection.runQuery(query)
        }
        catch (sqlE : SQLException) {
            sqlE.printStackTrace()
            throw  sqlE
        }
        catch (cnf : ClassNotFoundException) {
            throw cnf
        }

        // Show coins
        try {
            if (results != null) {
                // Find out how many rows are in the result
                var size = 0
                try {
                    results.last()
                    size = results.row
                    results.beforeFirst()

                    coins = ArrayList()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }

                // for each coin found
                for (i in 0 until size) {
                    results.next()



                    val newCoin = Coin()
                    newCoin.id = results.getInt("ID")
                    newCoin.countryName = results.getString("CountryName")
                    // Can't get full currency here because it will start a new query
                    newCoin.currency.nameAbbr = results.getString("CurrencyAbbr")
                    newCoin.name = requireNonNullElse(results.getString("Type"), "")
                    newCoin.year = results.getInt("Yr")
                    newCoin.denomination = results.getDouble("Denomination")
                    newCoin.value = results.getDouble("CurValue")
                    newCoin.mintMark = requireNonNullElse(results.getString("MintMark"), "")
                    newCoin.graded = results.getBoolean("Graded")
                    newCoin.condition = requireNonNullElse(results.getString("Grade"), "")
                    newCoin.error = results.getBoolean("Error")
                    newCoin.errorType = requireNonNullElse(results.getString("ErrorType"), "")
                    newCoin.note = requireNonNullElse(results.getString("Note"), "")
                    val setId =  requireNonNullElse(results.getInt("SetID"), DatabaseItem.ID_INVALID)
                    newCoin.addSetId(setId)
                    newCoin.slotId = requireNonNullElse(results.getInt("SlotID"), DatabaseItem.ID_INVALID)
                    newCoin.containerId = requireNonNullElse(results.getInt("ContainerID"), DatabaseItem.ID_INVALID)

                    if(results.getString("ObvImgExt") != null && results.getString("ObvImgExt") !="")
                        newCoin.obvImgPath = newCoin.generateImagePath(true, results.getString("ObvImgExt"))
                    if(results.getString("RevImgExt") != null && results.getString("RevImgExt") !="")
                        newCoin.revImgPath = newCoin.generateImagePath(false, results.getString("RevImgExt"))

                    newCoin.currency = findCurrency(newCoin.currency.nameAbbr)

                    if(setId != DatabaseItem.ID_INVALID)
                        coinsInSets.add(newCoin)
                    else if(newCoin.slotId != DatabaseItem.ID_INVALID)
                        coinsInBooks.add(newCoin)
                    else
                        coins.add(newCoin)

                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            throw  e
        }

        disconnect()

        coinListener?.coinListRetrievedFromFb(coins)
    }

    /**
     * Gets the current list of bills
     *
     * @return A list of bills in the collection
     */
    fun getBills() : ArrayList<Bill> {
        return bills
    }

    /**
     * Finds a bill with a specific ID in the list and sets it equal to a different bill.
     *
     * @param id The ID of the bill to find
     * @param bill The bill to replace it with. If null, the original bill is removed from the list.
     */
    fun setBill(id: Int, bill: Bill?) {
        for(i in 0 until bills.size) {
            if(bills[i].id == id) {
                if (bill != null)
                    bills[i] = bill
                else
                    bills.remove(bills[i])

                return
            }
        }
    }

    /**
     * Get all bills from database
     *
     * @return A list of bills that fit your query
     */
    private fun getBillsFromDb(){

        val query =
            """
                SELECT *
                FROM Bills
                ORDER BY ID;
                """.trimIndent()

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

                    bills = ArrayList()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }

                // for each bill found
                for (i in 0 until size) {
                    results.next()

                    val newBill = Bill()
                    newBill.id = results.getInt("ID")
                    newBill.countryName = results.getString("CountryName")
                    newBill.currency.nameAbbr = results.getString("CurrencyAbbr")
                    newBill.name = requireNonNullElse(results.getString("Type"), "")
                    newBill.year = results.getInt("Yr")
                    newBill.seriesLetter = results.getString("SeriesLetter")
                    newBill.denomination = results.getDouble("Denomination")
                    newBill.value = results.getDouble("CurValue")
                    newBill.graded = results.getBoolean("Graded")
                    newBill.serial = results.getString("Serial")
                    newBill.signatures = results.getString("Signatures")
                    newBill.condition = requireNonNullElse(results.getString("Grade"), "")
                    newBill.error = results.getBoolean("Error")
                    newBill.errorType = requireNonNullElse(results.getString("ErrorType"), "")
                    newBill.replacement = results.getBoolean("Replacement")
                    val setId = requireNonNullElse(results.getInt("SetID"), DatabaseItem.ID_INVALID)
                    newBill.addSetId(setId)
                    newBill.note = requireNonNullElse(results.getString("Note"), "")
                    newBill.containerId = requireNonNullElse(results.getInt("ContainerID"), DatabaseItem.ID_INVALID)

                    if(results.getString("ObvImgExt") != null)
                        newBill.obvImgPath = newBill.generateImagePath(true, results.getString("ObvImgExt"))
                    if(results.getString("RevImgExt") != null)
                        newBill.revImgPath = newBill.generateImagePath(false, results.getString("RevImgExt"))

                    newBill.currency = findCurrency(newBill.currency.nameAbbr)

                    if(setId != DatabaseItem.ID_INVALID)
                        billsInSets.add(newBill)
                    else
                        bills.add(newBill)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        disconnect()

        billListener?.billListRetrievedFromFb(bills)
    }

    /**
     * Gets the current list of coin sets
     *
     * @return A list of coin sets in the collection
     */
    fun getSets() : ArrayList<Set> {
        return sets
    }

    /**
     * Find all sets
     *
     * @return List of coin sets that fit your query
     */
    private fun getSetsFromDb() {

        val query =
            """
                SELECT *
                FROM Sets
                ORDER BY ID;
                """.trimIndent()

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

                    sets = ArrayList()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }

                // for each set found
                for (i in 0 until size) {
                    results.next()

                    val newSet = Set()
                    newSet.id = results.getInt("ID")
                    newSet.name = requireNonNullElse(results.getString("Name"), "")
                    newSet.year = results.getInt("Yr")
                    newSet.value = results.getDouble("CurValue")
                    newSet.note = requireNonNullElse(results.getString("Note"), "")
                    val parentId = requireNonNullElse(results.getInt("ParentID"), DatabaseItem.ID_INVALID)
                    newSet.addSetId(parentId)
                    newSet.containerId = requireNonNullElse(results.getInt("ContainerID"), DatabaseItem.ID_INVALID)

                    if(parentId != DatabaseItem.ID_INVALID)
                        setsInSets.add(newSet)
                    else
                        sets.add(newSet)
                }

                // Remove coins that are in sets from the coins list
                val removedCoins = ArrayList<Coin>()

                for(coin in coinsInSets) {
                    if(coin.set != null) {
                        findSet(coin.set!!.id)?.addItem(coin)
                        removedCoins.add(coin)
                    }
                }

                coinsInSets.removeAll(removedCoins)

                // Remove bills that are in sets from the bills list
                val removedBills = ArrayList<Bill>()

                for(bill in billsInSets) {
                    if(bill.set != null) {
                        findSet(bill.set!!.id)?.addItem(bill)
                        removedBills.add(bill)
                    }
                }

                bills.removeAll(removedBills)

                // Remove sets that are in other sets from the sets list
                val removedSets = ArrayList<Set>()

                for(set in setsInSets) {
                    if(set.set != null) {
                        findSet(set.set!!.id)?.addItem(set)
                        removedSets.add(set)
                    }
                }

                setsInSets.removeAll(removedSets)
            }
        }
        catch (e: SQLException) {
            e.printStackTrace()
        }

        disconnect()

        setListener?.setListRetrievedFromFb(sets)
    }

    /**
     * Gets the current list of countries
     *
     * @return An ArrayList of countries
     */
    fun getCountries() : ArrayList<Country> {
        return countries
    }

    fun findSet(id: Int) : Set? {
        for (set in sets)
            if (set.id == id)
                return set
        for (set in setsInSets)
            if (set.id == id)
                return set

        return null
    }

    /**
     * Finds a set with a specific ID in the list and sets it equal to a different set.
     *
     * @param id The ID of the coin set to find
     * @param set The set to replace it with. If null, the original set is removed from the list.
     */
    fun setSet(id: Int, set: Set?) {
        for(i in 0 until sets.size) {
            if(sets[i].id == id) {
                if (set != null)
                    sets[i] = set
                else
                    sets.remove(sets[i])

                return
            }
        }
    }

    /**
     * Gets a list of all countries from the database
     *
     * @return An ArrayList of all countries in the database. The list is sorted alphabetically by name.
     */
    private fun getCountriesFromDb() {
        val results = connection.runQuery("SELECT * FROM Countries ORDER BY Name ASC")

        try {
            if (results != null) {
                // Find out how many rows are in the result
                var size = 0
                try {
                    results.last()
                    size = results.row
                    results.beforeFirst()

                    countries = ArrayList()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }

                // for each country found, get info
                for (i in 0 until size) {
                    results.next()

                    val name = requireNonNullElse(results.getString("Name"), "")
                    val id = results.getInt("ID")
                    val newCountry = Country(name, id, ArrayList())

                    countries.add(newCountry)
                }

                Country.sort(countries, topOfCountriesList)

                // Add currencies to countries
                // Can't do this above because it ruins the connection / results
                for (country in countries) {
                    country.currencies = getCountryCurrencies(country.name)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        disconnect()

        countryListener?.countryListChanged(countries)
    }

    /**
     * Gets the current list of containers
     *
     * @return An ArrayList of containers
     */
    fun getContainers() : ArrayList<Container> {
        return containers
    }

    /**
     * Gets all containers from the database
     *
     * @return A list of all containers. The list is sorted alphabetically by name
     */
    private fun getContainersFromDb() {
        val results = connection.runQuery( "SELECT * From Containers ORDER BY Name")

        try {
            if (results != null) {
                // Find out how many rows are in the result
                var size = 0
                try {
                    results.last()
                    size = results.row
                    results.beforeFirst()

                    containers = ArrayList()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }

                // for each set found
                for (i in 0 until size) {
                    results.next()

                    val container = Container()

                    container.id = results.getInt("ID")
                    container.name = requireNonNullElse(results.getString("Name"), "")
                    container.parentID =
                        requireNonNullElse(results.getInt("ParentID"), DatabaseItem.ID_INVALID)

                    containers.add(container)
                }
            }
        }
        catch (e: SQLException) {
            e.printStackTrace()
        }

        disconnect()
    }

    /**
     * Finds a container with a specific ID in the list and sets it equal to a different container.
     *
     * @param id The ID of the coin to find
     * @param container The container to replace it with. If null, the original container is removed from the list.
     */
    fun setContainer(id: Int, container: Container?) {
        for(i in 0 until containers.size) {
            if(containers[i].id == id) {
                if (container != null)
                    containers[i] = container
                else
                    containers.remove(containers[i])

                return
            }
        }
    }

    /**
     * Finds a container with a specific name
     *
     * @param name The name of the container to look for
     * @return The container with the provided name
     */
    fun findContainer(name: String) : Container {
        containers = getContainers()

        for (container in containers) {
            if(container.name == name)
                return container
        }

        return Container()
    }

    /**
     * Finds a container with a specific ID
     *
     * @param id The ID of the container to look for
     * @return The container with the provided ID
     */
    fun findContainer(id: Int) : Container {
        containers = getContainers()

        for (container in containers) {
            if(container.id == id)
                return container
        }

        return Container()
    }

    /**
     * Gets the current list of currencies
     *
     * @return An ArrayList containing all possible currencies
     */
    private fun getCurrencies() : ArrayList<Currency> {
        return currencies
    }

    /**
     * Retrieves all currencies from the Database
     */
    private fun getCurrenciesFromDb() {
        val results = connection.runQuery("SELECT * From Currencies " +
                "ORDER BY Name")

        try {
            if (results != null) {
                // Find out how many rows are in the result
                var size = 0
                try {
                    results.last()
                    size = results.row
                    results.beforeFirst()

                    currencies = ArrayList()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }

                // for each set found
                for (i in 0 until size) {
                    results.next()

                    val id = results.getInt("ID")
                    val name = requireNonNullElse(results.getString("Name"), "")
                    val abbreviation = requireNonNullElse(results.getString("Abbreviation"), "")
                    val symbol = requireNonNullElse(results.getString("Symbol"), "")
                    val symbolBefore = results.getBoolean("SymbolBefore")
                    val start = results.getInt("YrStart")
                    val end : Int = results.getInt("YrEnd") // Will be 0 if null

                    currencies.add(Currency(name, id, abbreviation, symbol, symbolBefore, start, end))
                }
            }
        }
        catch (e: SQLException) {
            e.printStackTrace()
        }

        disconnect()
    }

    /**
     * Gets the currencies used in a specific country
     *
     * @param countryName The name of the country whose currencies you are looking for
     * @return A list of currencies used by the provided country
     */
    private fun getCountryCurrencies(countryName: String): ArrayList<Currency> {

        val results = connection.runQuery("SELECT C.* From Currencies As C\n" +
                "JOIN CountryCurrencies As CC\n" +
                "ON CC.CurrencyAbbr = C.Abbreviation\n" +
                "WHERE CC.CountryName = \"$countryName\"\n" +
                "ORDER BY COALESCE(C.YrEnd, 'zz') DESC;")

        val currencies = ArrayList<Currency>()

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

                    val id = results.getInt("ID")
                    val name = requireNonNullElse(results.getString("Name"), "")
                    val abbreviation = requireNonNullElse(results.getString("Abbreviation"), "")
                    val symbol = requireNonNullElse(results.getString("Symbol"), "")
                    val symbolBefore = results.getBoolean("SymbolBefore")
                    val start = results.getInt("YrStart")
                    val end : Int = results.getInt("YrEnd") // Will be 0 if null

                    currencies.add(Currency(name, id, abbreviation, symbol, symbolBefore, start, end))
                }
            }
        }
        catch (e: SQLException) {
            e.printStackTrace()
        }

        disconnect()

        return currencies
    }

    /**
     * Find a currency based on its unique abbreviation
     *
     * @param abbr The unique abbreviation of the requested currency
     * @return The currency which uses the provided abbreviation
     */
    fun findCurrency(abbr: String) : Currency {
        currencies = getCurrencies()

        for(currency in currencies) {
            if(currency.nameAbbr == abbr)
                return currency
        }

        return Currency()
    }

    /**
     * Gets the current list of books
     *
     * @return A list of books in the collection
     */
    fun getBooks() : ArrayList<Book> {
        return books
    }

    /**
     * Gets books from the database. Also retrieves the pages, slots, and coins in the books.
     *
     * @return An ArrayList of books in the database.
     */
    private fun getBooksFromDb() {
        val results = connection.runQuery("SELECT * From Books")

        if (results != null) {
            var size = 0

            try {
                results.last()
                size = results.row
                results.beforeFirst()

                books = ArrayList()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            // for each book found
            for (i in 0 until size) {
                results.next()

                val id = results.getInt("ID")
                val title = results.getString("Title")
                val denomination = requireNonNullElse(results.getDouble("Denomination"), 0.0)
                val startYear = requireNonNullElse(results.getInt("StartYear"), 0)
                val endYear = requireNonNullElse(results.getInt("EndYear"), 9999)
                val containerId = requireNonNullElse(results.getInt("ContainerID"), DatabaseItem.ID_INVALID)

                val book = Book()
                book.id = id
                book.title = title
                book.denomination = denomination
                book.startYear = startYear
                book.endYear = endYear
                book.containerId = containerId

                books.add(book)
            }
        }

        disconnect()

        for (book in books) {
            val pages = getBookPagesFromDb(book.id)
            for(page in pages) {
                page.book = book
                book.addPage(page)
            }
        }

        bookListener?.bookListRetrievedFromFb(books)
    }

    /**
     * Gets a list of pages in a specific book from the database
     *
     * @param bookId The unique ID of the book
     * @return An ArrayList of pages in the provided book
     */
    private fun getBookPagesFromDb(bookId: Int) : ArrayList<BookPage> {
        val results = connection.runQuery("SELECT R.* From BookPages where BookID = $bookId")

        val pages = ArrayList<BookPage>()

        if (results != null) {
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

                val id = results.getInt("ID")
                val pageNum = results.getInt("PageNum")

                val page = BookPage()
                page.id = id
                page.pageNum = pageNum

                pages.add(page)
            }
        }

        disconnect()

        for(page in pages) {
            val rows = getPageSlotsFromDb(page.id)

            // Add rows to page
            for(row in rows) {
                page.rows.add(row)
                // Fill rows with slots
                for(slot in row) {
                    row.add(slot)
                    val coinsToRemove = ArrayList<Coin>()
                    // Fill slots with coins
                    for (coin in coinsInBooks) {
                        if (coin.slotId == slot.id) {
                            slot.coin = coin
                            coinsToRemove.add(coin)
                            break
                        }
                    }

                    for(coin in coinsToRemove){
                        coinsInBooks.remove(coin)
                    }
                }
            }
        }

        return pages
    }

    /**
     * Gets slots in a specific book page from the database
     *
     * @param pageId The unique ID of the BookPage
     * @return An ArrayList of an ArrayList of Page slots. This list within a list represents rows of slots
     */
    private fun getPageSlotsFromDb(pageId: Int) : ArrayList<ArrayList<PageSlot>> {
        val results = connection.runQuery("SELECT * From PageSlots\n" +
                "Where PageID = $pageId\n" +
                "ORDER BY RowNum, ColNum")

        val rows = ArrayList<ArrayList<PageSlot>>()

        if (results != null) {
            var size = 0

            try {
                results.last()
                size = results.row
                results.beforeFirst()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            results.next()

            var currentRowNum = 1
            var currentRow = ArrayList<PageSlot>()

            // For each slot found
            for (i in 0 until size) {

                val id = results.getInt("ID")
                val rowNum = results.getInt("RowNum")
                val colNum = results.getInt("ColNum")
                val denomination = requireNonNullElse(results.getDouble("Denomination"), 0.10)
                val label = requireNonNullElse(results.getString("Label"), "")
                val label2 = requireNonNullElse(results.getString("Label2"), "")
                val label3 = requireNonNullElse(results.getString("Label3"), "")

                val slot = PageSlot()
                slot.id = id
                slot.rowNum = rowNum
                slot.colNum = colNum
                slot.line1Text = label
                slot.line2Text = label2
                slot.line3Text = label3

                slot.size = when(denomination) {
                    .01 -> { PageSlot.SIZE_PENNY}
                    .05 -> { PageSlot.SIZE_NICKEL }
                    .10 -> { PageSlot.SIZE_DIME }
                    .25 -> { PageSlot.SIZE_QUARTER }
                    .5 -> { PageSlot.SIZE_HALF }
                    else -> { PageSlot.SIZE_DOLLAR }
                }

                // If we started a new row
                if(rowNum == currentRowNum) {
                    rows.add(currentRow)
                    currentRow = ArrayList()
                    currentRowNum++
                }

                currentRow.add(slot)
            }

            rows.add(currentRow)
        }

        disconnect()

        return rows
    }

    /**
     * Finds a book with a specific ID in the list and sets it equal to a different book.
     *
     * @param id The ID of the coin to find
     * @param book The book to replace it with. If null, the original book is removed from the list.
     */
    fun setBook(id: Int, book: Book?) {
        for(i in 0 until books.size) {
            if(books[i].id == id) {
                if (book != null)
                    books[i] = book
                else
                    books.remove(books[i])

                return
            }
        }
    }

    /**
     * Imports a coin folder from an XML file
     *
     * @param path The location of the XML file to import
     * @return The book that was created from the XML input file
     */
    fun importBook(path: String): Book {

        fun processNode(node: Node, book: Book): Any {

            when (node.nodeName.toLowerCase()) {
                "page" -> {
                    val page = BookPage()

                    for (newChild in 0 until node.childNodes.length) {
                        val item = processNode(node.childNodes.item(newChild), book)

                        // if it's a row
                        if (item is ArrayList<*> && item[0] is PageSlot) {
                            page.rows.add(item as ArrayList<PageSlot>)
                            for (slot in item) {
                                slot.bookPage = page
                            }
                        }
                    }

                    return page
                }
                "row" -> {
                    val row = ArrayList<PageSlot>()

                    for (newChild in 0 until node.childNodes.length) {
                        val item = processNode(node.childNodes.item(newChild), book)

                        // if it's a row
                        if (item is PageSlot) {
                            row.add(item)
                        }
                    }

                    return row
                }
                "slot" -> {
                    var line1Text = ""
                    var line2Text = ""
                    var size = ""

                    if (node.nodeType == Node.ELEMENT_NODE) {
                        val element = node as Element
                        if (element.hasAttribute("line1"))
                            line1Text = element.getAttribute("line1")
                        if (element.hasAttribute("line2"))
                            line2Text = element.getAttribute("line2")
                        if (element.hasAttribute("size"))
                            size = element.getAttribute("size")
                    }

                    val sizeInt = if(size == "") {
                        when (book.denomination) {
                            .01 -> {PageSlot.SIZE_PENNY}
                            .05 -> {PageSlot.SIZE_NICKEL}
                            .1 -> {PageSlot.SIZE_DIME}
                            .25 -> {PageSlot.SIZE_QUARTER}
                            .5 -> {PageSlot.SIZE_HALF}
                            else -> {PageSlot.SIZE_DOLLAR}
                        }
                    }
                    else
                        when (size.toLowerCase()) {
                            "penny" -> {PageSlot.SIZE_PENNY}
                            "cent" -> {PageSlot.SIZE_PENNY}
                            "nickel" -> {PageSlot.SIZE_NICKEL}
                            "dime" -> {PageSlot.SIZE_DIME}
                            "quarter" -> {PageSlot.SIZE_QUARTER}
                            "half" -> {PageSlot.SIZE_HALF}
                            else -> {PageSlot.SIZE_DOLLAR}
                        }

                    return PageSlot(sizeInt, line1Text, line2Text)
                }
            }

            return ""
        }

        val book = Book()

        val file = getFileFromRes(path)

        if(file == null || !file.exists())
            return Book()

        val dbf: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val db: DocumentBuilder = dbf.newDocumentBuilder()
        val document: Document = db.parse(file)
        document.documentElement.normalize()

        val bookNode = document.getElementsByTagName("Book")

        if (bookNode.item(0).nodeType == Node.ELEMENT_NODE) {
            val element = bookNode.item(0) as Element
            if (element.hasAttribute("title"))
                book.title = element.getAttribute("title")
            if (element.hasAttribute("startYear"))
                book.startYear = element.getAttribute("startYear").toInt()
            if (element.hasAttribute("endYear"))
                book.endYear = element.getAttribute("endYear").toInt()
            if (element.hasAttribute("denomination"))
                book.denomination = element.getAttribute("denomination").toDouble()
        }

        val pageNodes = document.getElementsByTagName("Page")

        for (pageNum in 1 .. pageNodes.length) {
            val page = processNode(pageNodes.item(pageNum - 1), book)

            (page as BookPage).pageNum = pageNum
            page.book = book
            book.pages.add(page)
        }

        return book
    }

    /**
     * Imports a list of countries from an XML file
     *
     * @param path The location of the XML file to import
     * @return An ArrayList containing the countries from the XML input file
     */
    fun importCountries(path: String) : ArrayList<Country> {

        fun parseXmlNode(node: Node) : Any {

            when (node.nodeName.toLowerCase()) {
                "country" -> {
                    val country = Country()
                    if (node.nodeType == Node.ELEMENT_NODE) {
                        val element = node as Element
                        if (element.hasAttribute("name"))
                            country.name = element.getAttribute("name")
                    }

                    for(child in 0  until node.childNodes.length) {
                        try {
                            country.currencies.add(parseXmlNode(node.childNodes.item(child)) as Currency)
                        } catch (e: Exception) {

                        }
                    }

                    country.currencies = Currency.sort(country.currencies)

                    return country
                }
                "currency" -> {
                    val currency = Currency()

                    if (node.nodeType == Node.ELEMENT_NODE) {
                        val element = node as Element
                        if (element.hasAttribute("name"))
                            currency.name = element.getAttribute("name")
                        if (element.hasAttribute("abbr"))
                            currency.nameAbbr = element.getAttribute("abbr")
                        if (element.hasAttribute("symbol"))
                            currency.symbol = element.getAttribute("symbol")
                        if (element.hasAttribute("symbol_before"))
                            currency.symbolBefore = element.getAttribute("symbol_before").equals("true")
                        if (element.hasAttribute("start_year"))
                            currency.yrStart = element.getAttribute("start_year").toInt()
                        if (element.hasAttribute("end_year"))
                            currency.yrEnd = element.getAttribute("end_year").toInt()
                    }

                    return currency
                }
                else -> return ""
            }
        }

        val countries = ArrayList<Country>()

        val file = File(path)

        val dbf: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val db: DocumentBuilder = dbf.newDocumentBuilder()
        val document: Document = db.parse(file)
        document.documentElement.normalize()

        val countryNodes = document.getElementsByTagName("Country")

        for(index in 0 until countryNodes.length) {
            countries.add(parseXmlNode(countryNodes.item(index)) as Country)
        }

        return Country.sort(countries, topOfCountriesList)
    }

    /**
     * Finds the coin in a specific slot in a book
     *
     * @param slot The slot to investigate
     * @return The coin in the slot. If the coin had an ID of CollectionItem.INVALID_ID then the slot is empty.
     */
    fun findCoinInSlot(slot: PageSlot) : Coin {

        for(coin in coins) {
            if(coin.slotId == slot.id)
                return coin
        }

        return Coin()
    }

    /**
     * Returns status of an update or insert. Implies that only 1 row should be updated at a time.
     *
     * @param rows How many rows were modified by the insert or update
     * @return A message about whether the command was successful
     */
    fun getSuccessMessage(rows: Int): String {
        return connection.wasSuccessful(rows)
    }

    fun getGeneratedKeys() : ResultSet {
        return connection.statement!!.generatedKeys
    }

    /**
     * Returns the first argument if it is non-`null` and
     * otherwise returns the non-`null` second argument.
     *
     * @param obj an object
     * @param defaultObj a non-`null` object to return if the first argument
     * is `null`
     * @param <T> the type of the reference
     * @return the first argument if it is non-`null` and
     * otherwise the second argument if it is non-`null`
     * @throws NullPointerException if both `obj` is null and
     * `defaultObj` is `null`
     * @since 9
    </T> */
    fun <T> requireNonNullElse(obj: T?, defaultObj: T): T {
        return obj ?: Objects.requireNonNull(defaultObj, "defaultObj")
    }


    /**
     * Checks that the specified object reference is not `null` and
     * throws a customized [NullPointerException] if it is. This method
     * is designed primarily for doing parameter validation in methods and
     * constructors with multiple parameters, as demonstrated below:
     * <blockquote><pre>
     * public Foo(Bar bar, Baz baz) {
     * this.bar = Objects.requireNonNull(bar, "bar must not be null");
     * this.baz = Objects.requireNonNull(baz, "baz must not be null");
     * }
    </pre></blockquote> *
     *
     * @param obj     the object reference to check for nullity
     * @param message detail message to be used in the event that a `NullPointerException` is thrown
     * @param <T> the type of the reference
     * @return `obj` if not `null`
     * @throws NullPointerException if `obj` is `null`
    </T> */
    fun <T> requireNonNull(obj: T?, message: String?): T {
        if (obj == null) throw java.lang.NullPointerException(message)
        return obj
    }
}