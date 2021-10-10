package items

import NumismatistAPI

/**
 * Containers are boxes or other containers which contain CollectionItems
 *
 * @see CollectionItem
 */
class Container : DatabaseItem() {

    var name = ""

    /**
     * Unique ID of the container that holds this container. This container is not inside another container if this
     * parentID is DatabaseItem.ID_INVALID
     */
    var parentID = ID_INVALID

    /**
     * Given a list of containers, find containers which are inside (or children of) this one
     *
     * @param containers A list of possible children
     *
     * @return Containers which are inside (children of) this one
     */
    fun getChildContainers(containers : ArrayList<Container>) : ArrayList<Container> {
        val children = ArrayList<Container>()

        for (cont in containers) {
            if(cont.parentID == id)
                children.add(cont)
        }

        return children
    }

    /**
     * Find coins that are inside a specific container and its children
     *
     * @param api NumismatistAPI that holds list of containers
     * @param includeChildren True if you want to get items inside children containers. Otherwise false
     *
     * @return A list of items inside the container and optionally inside its children
     */
    fun getCoinsInContainer(api: NumismatistAPI, includeChildren: Boolean = true) : ArrayList<CollectionItem> {

        val childrenIDs = ArrayList<Int>()
        val children = getChildContainers(api.getContainers())
        // Find the IDs of all child containers
        if (includeChildren)
            for (child in children)
                childrenIDs.add(child.id)

        // A list of coins in this container
        val containerItems = ArrayList<CollectionItem>()

        val allItems = ArrayList<CollectionItem>()

        allItems.addAll(api.getCoins())
        allItems.addAll(api.getBills())
        allItems.addAll(api.getSets())
        allItems.addAll(api.getBooks())

        for(item in allItems) {
            // If the coin is in this container or its children
            if(item.containerId == id ||
                (includeChildren && childrenIDs.contains(item.containerId))) {
                containerItems.add(item)
            }
        }

        return containerItems
    }

    /**
     * Save a container to the database
     *
     * @param api an API object to use to connect to the database
     *
     * @return Number of rows affected in the database. This should always be 1, otherwise a problem occurred
     */
    override fun saveToDb(api: NumismatistAPI): Int {

        val parentID = if(parentID == ID_INVALID)
            "null"
        else
            "" + parentID

        val rows = if(id != 0) {
            api.setContainer(id, this)

            api.runUpdate("UPDATE Containers SET Name=${name}, ParentID=$parentID" +
                    "WHERE ID=${id};")
        }
        else {

            val newRows = api.runUpdate("INSERT INTO Containers(Name, ParentID)\n" +
                    "VALUES(\"${name}\", $parentID);")

            if(newRows == 1) {
                val newID = api.getGeneratedKeys()
                if(newID.next()) {
                    id = Integer.parseInt(newID.getString("GENERATED_KEY"))
                }
                api.getContainers().add(this)
            }

            newRows
        }

        api.disconnect()

        return rows
    }

    /**
     * Removes a container from the database
     *
     * @param api an API object to use to connect to the database
     *
     * @return True if container was successfully removed, otherwise false
     */
    override fun removeFromDb(api: NumismatistAPI) : Boolean {

        val sql = "DELETE FROM Containers WHERE ID=${id}"

        val rows = api.runUpdate(sql)

        if(rows==1)
            api.getContainers().remove(this)

        api.disconnect()

        return rows == 1
    }
}