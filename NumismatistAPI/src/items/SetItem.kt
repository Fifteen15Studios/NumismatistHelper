package items

import java.util.*

/**
 * SetItems are CollectionItems that can be put in a set, such as Coins, Bills, and Sets
 *
 * @see CollectionItem
 * @see Coin
 * @see Bill
 * @see Set
 */
@Suppress("unused")
abstract class SetItem : CollectionItem() {

    companion object {
        /**
         * Default value for year of a coin. If left at this value, the coin is likely a token or medal
         */
        const val YEAR_NONE = 0

        const val NO_DENOMINATION = 0.0
    }

    /**
     * The set this item is in. If null then it's not in a set
     */
    var set : Set? = null

    var name  = ""
    var countryName = ""
    var currency = Currency()

    /**
     * Should be true if the item is professionally graded, otherwise false
     */
    var graded = false
    var condition = ""
    var error = false
    var errorType = ""

    /**
     * Face value of the item
     */
    var denomination = NO_DENOMINATION
        set(value) {
            if(value >= 0.0)
                field = value
        }

    /**
     * Current approximate sell value of the item
     */
    var value = 0.0
        set(value) {
            if(value >= 0.0)
                field = value
            else {
                throw NumberFormatException(
                    ResourceBundle.getBundle("res.strings", Locale.getDefault())
                    .getString("error_negativeValue"))
            }
        }

    /**
     * Year on the coin. Can be negative if coin is from BC era, or 0 if no date is on coin (like a token)
     */
    var year = YEAR_NONE

    fun addToSet(set: Set) {
        set.addItem(this)
    }

    abstract fun copy() : SetItem

    /**
     * Adds ID to the set this is in. If ID is invalid, does nothing
     *
     * @param id ID of the set this is in
      */
    fun addSetId(id : Int) {
        if(id != ID_INVALID) {
            if(set == null)
                set = Set()

            set!!.id = id
        }
    }
}