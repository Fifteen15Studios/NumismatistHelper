package items

import kotlin.collections.ArrayList

class Country() {

    /**
     * Currencies used over a certain range of years
     */
    class Range() {

        companion object {
            const val YEAR_START_INVALID = 9999
            const val YEAR_END_INVALID = 0
        }

        constructor(currency: Currency, yrStart: Int, yrEnd : Int) : this() {
            this.currency = currency
            this.yrStart = yrStart
            this.yrEnd = yrEnd
        }

        /**
         * Currency in use during this range of years.
         */
        var currency = Currency()
        /**
         * Year that the currency started being used in this country. If unknown, use YEAR_START_INVALID
         */
        var yrStart = YEAR_START_INVALID
        /**
         * Year that the currency stopped being used in this country. If unknown, or currency is still in use, use YEAR_END_INVALID
         */
        var yrEnd = YEAR_END_INVALID

        fun contains(currency: Currency) : Boolean {
            return currency.nameAbbr == this.currency.nameAbbr
        }

        fun contains(year: Int) : Boolean {
            return if(yrStart == YEAR_START_INVALID)
                year <= yrEnd
            else if (yrEnd == YEAR_END_INVALID)
                year >= yrStart
            else
                year in yrStart until yrEnd + 1
        }
    }

    var name = ""
    /**
     * Unique ID for this item. Provided by the SQL database
     */
    var id = 0
    /**
     * A list of currencies that this country has had
     */
    var ranges = ArrayList<Range>()

    constructor(name: String, id: Int, ranges: ArrayList<Range>) : this() {
        this.name = name
        this.id = id
        this.ranges = ranges
    }

    companion object {

        /**
         * Sorts a list of countries by name
         *
         * @param countries A list of countries to sort
         * @param customOrder A list of countries to leave at the top of the list, disregarding alphabetical order
         *
         * @return A sorted list of countries
         */
        fun sort(countries: ArrayList<Country>, customOrder : ArrayList<String>) : ArrayList<Country> {

            // Put customs at the beginning
            // Add custom list as the first list item, in reverse order. Which results in them
            //   being in the order we want them to be.
            for(i in customOrder.size - 1 downTo 0 ) {
                for (j in 0 until countries.size) {
                    if(countries[j].name == customOrder[i]) {
                        val country = countries[j]
                        countries.remove(country)
                        countries.add(0, country)
                        // Break out of inner for loop
                        break
                    }
                }
            }

            // Alphabetize the rest
            do {
                var moves = 0
                // - 1 is necessary because of the + 1 in the next line
                for (i in customOrder.size until countries.size - 1) {
                    if (countries[i].name > countries[i + 1].name) {
                        val country = countries[i]
                        countries[i] = countries[i+1]
                        countries[i+1] = country
                        moves++
                    }
                }
            }while (moves > 0)

            return countries
        }

        /**
         * Finds a specific country object
         *
         * @param countries A list of countries to search through
         * @param name The name of the country to find
         *
         * @return A country with the name provided. If not found, returns a new Country object with no name
         */
        fun getCountry(countries : ArrayList<Country>, name: String) : Country {
            for(country in countries) {
                if(country.name == name)
                    return country
            }

            return Country()
        }

        /*fun createList(): ArrayList<Country> {

            val euro = Currency("Euro", 0, "EUR", "€")
            val usd = Currency("US Dollar", 0, "USD", "$")

            // List of countries
            return arrayListOf(
                //Country("United States (USA)", arrayListOf(usd)),
                //Country("Canada", arrayListOf(Currency("Canadian Dollar", "CAD", "$"))),
                //Country("Mexico", arrayListOf(Currency("Peso", "MXN", "$"))),
                //Country("Afghanistan", arrayListOf()),
                //Country("Albania", arrayListOf()),
                //Country("Algeria", arrayListOf()),
                Country("Andorra", arrayListOf()),
                Country("Angola", arrayListOf()),
                Country("Anguilla", arrayListOf()),
                Country("Antigua & Barbuda", arrayListOf()),
                Country("Argentina", arrayListOf()),
                Country("Armenia", arrayListOf()),
                Country("Australia", arrayListOf()),
                Country("Austria", arrayListOf(euro)),
                Country("Azerbaijan", arrayListOf()),
                Country("Bahamas", arrayListOf()),
                Country("Bahrain", arrayListOf()),
                Country("Bangladesh", arrayListOf()),
                Country("Barbados", arrayListOf()),
                Country("Belarus", arrayListOf()),
                Country("Belgium", arrayListOf()),
                Country("Belize", arrayListOf()),
                Country("Benin", arrayListOf()),
                Country("Bermuda", arrayListOf()),
                Country("Bhutan", arrayListOf()),
                Country("Bolivia", arrayListOf()),
                Country("Bosnia & Herzegovina", arrayListOf()),
                Country("Botswana", arrayListOf()),
                Country("Brazil", arrayListOf()),
                Country("Brunei Darussalam", arrayListOf()),
                Country("Bulgaria", arrayListOf()),
                Country("Burkina Faso", arrayListOf()),
                Country("Burundi", arrayListOf()),
                Country("Cambodia", arrayListOf()),
                Country("Cameroon", arrayListOf()),
                Country("Cape Verde", arrayListOf()),
                Country("Cayman Islands", arrayListOf()),
                Country("Central African Republic", arrayListOf()),
                Country("Chad", arrayListOf()),
                Country("Chile", arrayListOf()),
                Country("China", arrayListOf()),
                Country("China - Hong Kong / Macau", arrayListOf()),
                Country("Colombia", arrayListOf()),
                Country("Comoros", arrayListOf()),
                Country("Congo", arrayListOf()),
                Country("Congo, Democratic Republic of (DRC)", arrayListOf()),
                Country("Costa Rica", arrayListOf()),
                Country("Croatia", arrayListOf()),
                Country("Cuba", arrayListOf()),
                Country("Cyprus", arrayListOf()),
                Country("Czech Republic", arrayListOf()),
                Country("Denmark", arrayListOf()),
                Country("Djibouti", arrayListOf()),
                Country("Dominica", arrayListOf()),
                Country("Dominican Republic", arrayListOf()),
                Country("Ecuador", arrayListOf()),
                Country("Egypt", arrayListOf()),
                Country("El Salvador", arrayListOf()),
                Country("Equatorial Guinea", arrayListOf()),
                Country("Eritrea", arrayListOf()),
                Country("Estonia", arrayListOf()),
                Country("Eswatini", arrayListOf()),
                Country("Ethiopia", arrayListOf()),
                Country("Fiji", arrayListOf()),
                Country("Finland", arrayListOf()),
                Country("France", arrayListOf(euro)),
                Country("French Guiana", arrayListOf()),
                Country("Gabon", arrayListOf()),
                Country("Gambia, Republic of The", arrayListOf()),
                Country("Georgia", arrayListOf()),
                //Country("Germany", arrayListOf(euro, Currency("Deutschmark", "DM", "DM"), Currency("Reichsmark", "RM", "ℛℳ", false))),
                Country("Ghana", arrayListOf()),
                Country("Great Britain", arrayListOf(euro)),
                Country("Greece", arrayListOf(euro)),
                Country("Grenada", arrayListOf()),
                Country("Guadeloupe", arrayListOf()),
                Country("Guatemala", arrayListOf()),
                Country("Guinea", arrayListOf()),
                Country("Guinea-Bissau", arrayListOf()),
                Country("Guyana", arrayListOf()),
                Country("Haiti", arrayListOf()),
                Country("Honduras", arrayListOf()),
                Country("Hungary", arrayListOf(euro)),
                Country("Iceland", arrayListOf()),
                Country("India", arrayListOf()),
                Country("Indonesia", arrayListOf()),
                Country("Iran", arrayListOf()),
                Country("Iraq", arrayListOf()),
                Country("Israel and the Occupied Territories", arrayListOf()),
                Country("Italy", arrayListOf(euro, Currency("Lira", "ITL", "₤"))),
                Country("Ivory Coast (Cote d'Ivoire)", arrayListOf()),
                Country("Jamaica", arrayListOf()),
                Country("Japan", arrayListOf()),
                Country("Jordan", arrayListOf()),
                Country("Kazakhstan", arrayListOf()),
                Country("Kenya", arrayListOf()),
                Country("Korea, Democratic Republic of (North Korea)", arrayListOf()),
                Country("Korea, Republic of (South Korea)", arrayListOf()),
                Country("Kosovo", arrayListOf()),
                Country("Kuwait", arrayListOf()),
                Country("Kyrgyz Republic (Kyrgyzstan)", arrayListOf()),
                Country("Laos", arrayListOf()),
                Country("Latvia", arrayListOf()),
                Country("Lebanon", arrayListOf()),
                Country("Lesotho", arrayListOf()),
                Country("Liberia", arrayListOf()),
                Country("Libya", arrayListOf()),
                Country("Liechtenstein", arrayListOf()),
                Country("Lithuania", arrayListOf()),
                Country("Luxembourg", arrayListOf()),
                Country("Madagascar", arrayListOf()),
                Country("Malawi", arrayListOf()),
                Country("Malaysia", arrayListOf()),
                Country("Maldives", arrayListOf()),
                Country("Mali", arrayListOf()),
                Country("Malta", arrayListOf()),
                Country("Martinique", arrayListOf()),
                Country("Mauritania", arrayListOf()),
                Country("Mauritius", arrayListOf()),
                Country("Mayotte", arrayListOf()),
                Country("Moldova, Republic of", arrayListOf()),
                Country("Monaco", arrayListOf()),
                Country("Mongolia", arrayListOf()),
                Country("Montenegro", arrayListOf()),
                Country("Montserrat", arrayListOf()),
                Country("Morocco", arrayListOf()),
                Country("Mozambique", arrayListOf()),
                Country("Myanmar/Burma", arrayListOf()),
                Country("Namibia", arrayListOf()),
                Country("Nepal", arrayListOf()),
                Country("Netherlands", arrayListOf()),
                Country("New Zealand", arrayListOf()),
                Country("Nicaragua", arrayListOf()),
                Country("Niger", arrayListOf()),
                Country("Nigeria", arrayListOf()),
                Country("North Macedonia, Republic of", arrayListOf()),
                Country("Norway", arrayListOf()),
                Country("Oman", arrayListOf()),
                Country("Pacific Islands", arrayListOf()),
                Country("Pakistan", arrayListOf()),
                Country("Panama", arrayListOf()),
                Country("Papua New Guinea", arrayListOf()),
                Country("Paraguay", arrayListOf()),
                Country("Peru", arrayListOf()),
                Country("Philippines", arrayListOf()),
                Country("Poland", arrayListOf()),
                Country("Portugal", arrayListOf()),
                Country("Puerto Rico", arrayListOf()),
                Country("Qatar", arrayListOf()),
                Country("Reunion", arrayListOf()),
                Country("Romania", arrayListOf()),
                Country("Russian Federation", arrayListOf()),
                Country("Rwanda", arrayListOf()),
                Country("Saint Kitts and Nevis", arrayListOf()),
                Country("Saint Lucia", arrayListOf()),
                Country("Saint Vincent and the Grenadines", arrayListOf()),
                Country("Samoa", arrayListOf()),
                Country("Sao Tome and Principe", arrayListOf()),
                Country("Saudi Arabia", arrayListOf()),
                Country("Senegal", arrayListOf()),
                Country("Serbia", arrayListOf()),
                Country("Seychelles", arrayListOf()),
                Country("Sierra Leone", arrayListOf()),
                Country("Singapore", arrayListOf()),
                Country("Slovak Republic (Slovakia)", arrayListOf()),
                Country("Slovenia", arrayListOf()),
                Country("Solomon Islands", arrayListOf()),
                Country("Somalia", arrayListOf()),
                Country("South Africa", arrayListOf()),
                Country("South Sudan", arrayListOf()),
                Country("Spain", arrayListOf(euro)),
                Country("Sri Lanka", arrayListOf()),
                Country("Sudan", arrayListOf()),
                Country("Suriname", arrayListOf()),
                Country("Sweden", arrayListOf()),
                Country("Switzerland", arrayListOf()),
                Country("Syria", arrayListOf()),
                Country("Tajikistan", arrayListOf()),
                Country("Tanzania", arrayListOf()),
                Country("Thailand", arrayListOf()),
                Country("Timor Leste", arrayListOf()),
                Country("Togo", arrayListOf()),
                Country("Trinidad & Tobago", arrayListOf()),
                Country("Tunisia", arrayListOf()),
                Country("Turkey", arrayListOf()),
                Country("Turkmenistan", arrayListOf()),
                Country("Turks & Caicos Islands", arrayListOf()),
                Country("Uganda", arrayListOf()),
                Country("Ukraine", arrayListOf()),
                Country("United Arab Emirates", arrayListOf()),
                Country("Uruguay", arrayListOf()),
                Country("Uzbekistan", arrayListOf()),
                Country("Venezuela", arrayListOf()),
                Country("Vietnam", arrayListOf()),
                Country("Virgin Islands (UK)", arrayListOf()),
                Country("Virgin Islands (US)", arrayListOf(usd)),
                Country("Yemen", arrayListOf()),
                Country("Zambia", arrayListOf()),
                Country("Zimbabwe", arrayListOf())
            )
        }*/
    }

    /**
     * Sorts currencies attached to this country
     */
    fun sortCurrencies() {

        do {
            var moves = 0
            for (index in 0 until ranges.size - 1) {
                if (ranges[index].yrStart < ranges[index + 1].yrStart) {
                    val range = ranges[index]
                    ranges[index] = ranges[index + 1]
                    ranges[index + 1] = range
                    moves++
                }
            }
        }while (moves > 0)
    }
}
