package items

import NumismatistAPI
import java.io.FileNotFoundException
import java.io.IOException
import java.sql.SQLException
import kotlin.collections.ArrayList

class Set : SetItem() {

    companion object {
        const val YEAR_NONE = 0
    }

    /**
     * A list of items in this set
     */
    var items = ArrayList<SetItem>()

    /**
     * A list of items that have been removed from this set. List is only valid until the set is saved,
     * then it is cleared.
     */
    val removedItems = ArrayList<SetItem>()

    /**
     * Gets the total face value of all items in the set
     *
     * @return Total face value of all items in this set
     */
    fun getFaceValue() : Double {
        var value = 0.0

        for(item in items)
            value += item.denomination

        return value
    }

    /**
     * Creates a copy of this set, with an id and containerId of ID_INVALID since it will not be in the database,
     * and blank image paths
     *
     * @return A copy of this set
     */
    override fun copy() : Set {
        val newSet = Set()

        newSet.id = ID_INVALID
        newSet.name  = name
        newSet.value = value
        newSet.year = year
        newSet.note = note
        newSet.set = set
        newSet.obvImgPath = obvImgPath
        newSet.revImgPath = revImgPath
        newSet.containerId = containerId

        // Remove items that are not in this object
        for (item in newSet.items) {
            if(!contains(item))
                newSet.items.remove(item)
        }

        // Add all items from this object
        for(item in items) {
            val newItem = item.copy()
            newItem.set = this
            newSet.addItem(newItem)
        }

        return newSet
    }

    /**
     * Adds an item to this set
     *
     * @param item The item you want to add
     */
    fun addItem(item: SetItem) {
        item.set = this

        items.add(item)
    }

    /**
     * Removes an item from the set
     *
     * @param item The item you want to remove
     *
     * @return True if the item was in the list
     */
    fun removeItem(item: SetItem) : Boolean {
        val removed = items.remove(item)
        if(removed)
            removedItems.add(item)

        return removed
    }

    /**
     * @return A string in the form of "[<year> ]<name>"
     */
    override fun toString() : String {
        var output = ""

        if(year != YEAR_NONE)
            output += "$year "

        output += name

        return output
    }

    /**
     * Saves a set to the database. If the set is not already in the database (id = 0) the set is added.
     *   If the set is already in the database, it is updated to reflect any changes.
     *
     * @param api an API object to use to connect to the database
     *
     * @return How many rows were affected by the sql command
     */
    @Throws(IOException::class)
    override fun saveToDb(api: NumismatistAPI) : Int {
        val sql: String

        // set SetID to null if necessary
        val newSetID = if(set == null)
            "null"
        else
            "" + set!!.id

        // set ContainerID to null if necessary
        val containerID = if(containerId == ID_INVALID)
            "null"
        else
            "" + containerId

        if(id != 0) {
            api.setSet(id, this)

            sql = "UPDATE Sets SET Name=\"${name}\", Yr=${year}, CurValue=${value}, ParentID=$newSetID, Note=\"${api.conditionSqlString(note)}\", " +
                    "ContainerID=$containerID\n" +
                    "WHERE ID=${id};"

            var rows = api.runUpdate(sql)
            setInSetList(api)

            if(rows == 1) {
                var errors = 0

                // Add items that have been added
                for (item in items) {
                    item.set = this

                    rows = item.saveToDb(api)
                    if (rows == -1 && errors == 0) {
                        errors++
                    }
                }

                for (item in removedItems) {
                    item.set = null

                    if(item.id !=0) {
                        val itemRows = item.saveToDb(api)
                        // Add back to list of individual items
                        items.add(item)
                        if(itemRows == -1 && errors == 0) {
                            errors++
                        }
                    }
                }

                if(errors > 0) {
                    throw IOException(NumismatistAPI.getString("error_itemInSetSaveError"))
                }
                else
                    return rows
            }
            else {
                api.disconnect()
                return rows
            }
        }
        else {
            sql = "INSERT INTO Sets(Name, Yr, CurValue, ContainerID, ParentID, Note)\n" +
                    "VALUES(\"${name}\", ${year}, ${value}, $containerID, $newSetID, \"${api.conditionSqlString(note)}\");"

            val rows = api.runUpdate(sql)

            // If successful, add items to set
            if(rows == 1) {

                // set the new ID to this object
                val newID = api.getGeneratedKeys()
                if(newID.next()) {
                    id = Integer.parseInt(newID.getString("GENERATED_KEY"))
                }
                setInSetList(api)

                var errors = 0
                try {
                    // Add items that have been added
                    for (item in items) {
                        item.set = this


                        val itemRows = item.saveToDb(api)
                        if (itemRows != 1)
                            errors++
                    }

                    // Remove items that have been removed
                    for (item in removedItems) {
                        item.set = null

                        // If items was removed from set, but not from DB
                        if(item.id !=0) {
                            val itemRows = item.saveToDb(api)
                            if (itemRows != 1)
                                errors++
                        }
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }

            api.disconnect()

            return rows
        }
    }

    /**
     * Adds or removes set to/from api list of sets
     *
     * @param api the api object to add or remove to/from
     */
    private fun setInSetList(api : NumismatistAPI) {
        if(set == null && !api.getSets().contains(this))
            api.getSets().add(this)
        else
            api.getSets().remove(this)
    }

    /**
     * Removes a set from the database
     *
     * @param api an API object to use to connect to the database
     *
     * @return True if set was successfully removed, otherwise false
     */
    @Throws(SQLException::class, FileNotFoundException::class)
    override fun removeFromDb(api: NumismatistAPI) : Boolean {

        var itemsInDb = 0
        var removedItems = 0

        // Remove all items in the set
        for (item in items) {
            if(item.id != ID_INVALID) {
                itemsInDb++
                if (item.removeFromDb(api))
                    removedItems++
            }
        }

        // If all individual items were removed, remove the set
        val rows = if(itemsInDb == removedItems) {
            val sql = "DELETE FROM Sets WHERE ID=${id}"

            try {
                api.runUpdate(sql)
            }
            catch (e : SQLException) {
                throw e
            }
        }
        else
            0

        if(rows==1) {
            api.getSets().remove(this)

            // Delete pictures
            deleteObvImage()
            deleteRevImage()
        }

        api.disconnect()

        return rows == 1
    }

    fun contains(item : SetItem ) : Boolean {
        return this.items.contains(item)
    }
}