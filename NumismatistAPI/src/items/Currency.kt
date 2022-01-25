package items

import kotlin.collections.ArrayList

class Currency() {

    var name: String
    /**
     * Unique ID for this item. Provided by the SQL database
     */
    var id : Int
    var nameAbbr: String
    /**
     * Currency symbol like $
     */
    var symbol: String
    /**
     * True if the symbol appears before the value, otherwise false
     */
    var symbolBefore: Boolean = true

    init {
        name = ""
        id = -1
        nameAbbr = ""
        symbol = ""
        symbolBefore = true
    }

    constructor(name: String, id : Int, nameAbbr: String, symbol: String, symbolBefore: Boolean = true) : this() {
        this.name = name
        this.id = id
        this.nameAbbr = nameAbbr
        this.symbol = symbol
        this.symbolBefore = symbolBefore
    }

    companion object {

        /**
         * Sorts a list of currencies by name
         *
         * @param currencies List of currencies to sort
         * @return Sorted list of currencies
         */
        fun sort(currencies: ArrayList<Currency>) : ArrayList<Currency> {
            do {
                var moves = 0
                for (index in 0 until currencies.size - 1) {
                    if (currencies[index].name < currencies[index + 1].name) {
                        val currency = currencies[index]
                        currencies[index] = currencies[index + 1]
                        currencies[index + 1] = currency
                        moves++
                    }
                }
            }while (moves > 0)

            return currencies
        }
    }

    /**
     * Compares 2 currencies to see if they are the same currency. Uses nameAbbr for this comparison
     */
    override fun equals(other: Any?) : Boolean {
        return other is Currency && other.nameAbbr == this.nameAbbr
    }

    override fun hashCode(): Int {
        return nameAbbr.hashCode()
    }
}