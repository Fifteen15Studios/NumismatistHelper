package items

import NumismatistAPI

/**
 * DatabaseItems are items that are held and represented in the database. Subclasses include CollectionItem and SetItem
 *
 * @see CollectionItem
 * @see SetItem
 */
abstract class DatabaseItem {

    companion object {
        /**
         * ID to use for an item that is not in the database
         */
        const val ID_INVALID = 0
    }

    /**
     * Unique ID for this item. Provided by the SQL database
     */
    var id = 0

    abstract fun saveToDb(api: NumismatistAPI) : Int

    abstract fun removeFromDb(api: NumismatistAPI) : Boolean
}