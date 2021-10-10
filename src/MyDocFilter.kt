import java.util.*
import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter

/**
 * A document filter for text fields, which will only accept certain formats of input.
 * Input will be rejected regardless of how it got there... whether it is typed, pasted, or inserted some other way.
 *
 * This class, as it stands, does not filter anything. You must use a subclass in order to filter input.
 *
 * When creating a custom filter, simply create a subclass of MyDocFilter and override the test function.
 * This function determines whether or not input is acceptable for the filter. If this method returns true,
 * the input will be allowed, if it returns false the input will not be entered.
 *
 * Example of setting filter on JTextField in Java:
 * ((PlainDocument) textField.getDocument()).setDocumentFilter(new MyIntFilter());
 * in Kotlin:
 * (textField.document as PlainDocument).setDocumentFilter(MyIntFilter())
 */
open class MyDocFilter : DocumentFilter() {

    companion object {
        /**
         * Create a filter that only accepts years as values, with the current year as max accepted value.
         * Negative year is BC, or BCE. Positive is AD, or CE
         * 0 is invalid, but is accepted here in case you want to use 0 as something like "No year provided"
         */
        fun getCurrentYearFilter() : MyIntFilter {
            val yearFilter = MyIntFilter()
            // Current year is the max value - If you need to accept future years, use a different filter
            yearFilter.setMaxValue(Calendar.getInstance()[Calendar.YEAR])

            return yearFilter
        }

        /**
         * Create a filter that accepts denominations for money (coins and banknotes)
         */
        fun getDenominationFilter() : MyDoubleFilter {
            val denominationFilter = MyDoubleFilter()
            // Denomination is always more than 0
            denominationFilter.minValue = 0.0
            // Allow 3 decimal points of precision for half cents
            denominationFilter.maxPrecision = 3

            return denominationFilter
        }

        /**
         * Creates a filter that accepts the value of something
         */
        fun getValueFilter() : MyDoubleFilter {
            val valueFilter = MyDoubleFilter()
            // value is always more than 0
            valueFilter.minValue = 0.0
            valueFilter.maxPrecision = 2

            return valueFilter
        }
    }

    // Called when text is inserted
    override fun insertString(fb: FilterBypass, offset: Int, string: String,
                              attr: AttributeSet) {
        val doc = fb.document
        val sb = StringBuilder()
        sb.append(doc.getText(0, doc.length))
        sb.insert(offset, string)

        if (test(sb.toString())) {
            super.insertString(fb, offset, string, attr)
        } else {
            // warn the user and don't allow the insert
        }
    }

    /**
     * Override this function, and only this function, when creating a custom filter.
     * It should return true when the input is valid, otherwise false
     */
    protected open fun test(text: String): Boolean {
        return true
    }

    // Called when text is replaced
    override fun replace(fb: FilterBypass, offset: Int, length: Int, text: String,
                         attrs: AttributeSet?) {
        val doc = fb.document
        val sb = StringBuilder()
        sb.append(doc.getText(0, doc.length))
        sb.replace(offset, offset + length, text)

        if (test(sb.toString())) {
            super.replace(fb, offset, length, text, attrs)
        } else {
            // warn the user and don't allow the insert
        }
    }

    // Called when text is removed
    override fun remove(fb: FilterBypass, offset: Int, length: Int) {
        val doc = fb.document
        val sb = StringBuilder()
        sb.append(doc.getText(0, doc.length))
        sb.delete(offset, offset + length)

        // If input is deleted entirely, or input is valid
        if (sb.toString().isEmpty() || test(sb.toString())) {
            super.remove(fb, offset, length)
        } else {
            // warn the user and don't allow the insert
        }
    }
}

/**
 * A filter that only accepts letters
 */
class MyLetterFilter : MyDocFilter() {

    var maxLetters = Int.MAX_VALUE

    override fun test(text: String): Boolean {

        // allow empty input
        if(text == "")
            return true

        // Limit to certain # of characters
        if(text.length > maxLetters)
            return false

        // If anything is not a letter, return false
        for(char in text) {
            if(!Character.isLetter(char))
                return false
        }

        // If it got here, we're OK
        return true
    }
}

/**
 * A filter that only accepts integers
 */
class MyIntFilter : MyDocFilter() {

    // Limit input to a specific range
    private var minValue = Int.MIN_VALUE
    private var maxValue = Int.MAX_VALUE

    // Limit input to a specific number of digits
    var maxDigits = Int.MAX_VALUE

    override fun test(text: String): Boolean {
        try {

            // Accept empty input
            if(text == "")
                return true

            // Accept negative sign
            if (text == "-" && minValue < 0)
                return true

            // don't count - as a digit, if it's there
            val digits = if(text[0] == '-')
                text.length - 1
            else
                text.length

            // Limit to certain # of digits
            if(digits > maxDigits)
                return false

            // If not an int, will fall to catch
            val value = text.toInt()

            // Make sure value is in range
            if (value in minValue..maxValue)
                return true
        } catch (e: NumberFormatException) {
        }

        return false
    }

    fun setMinValue(minValue: Int) {
        this.minValue = minValue
    }

    fun setMaxValue(maxValue: Int) {
        this.maxValue = maxValue
    }
}

/**
 * A filter that only accepts integer and decimal number values (Doubles)
 */
class MyDoubleFilter : MyDocFilter() {

    // Limit input to a specific range
    var minValue = Double.NEGATIVE_INFINITY
    var maxValue = Double.MAX_VALUE

    // Maximum digits allowed before the decimal point
    var maxIntDigits = Int.MAX_VALUE
    // Maximum digits allowed after the decimal point
    var maxPrecision = Int.MAX_VALUE
    // Max total digits allowed
    var maxTotalDigits = Int.MAX_VALUE

    // Find number of integer places. Answer differs based on if a decimal point and/or negative sign is present
    fun getIntegerDigits(text: String) : Int {

        val decimalPos = text.indexOf('.')

        return when {
            // If it contains a Decimal AND - sign
            decimalPos != -1 && text[0] == '-' ->
                decimalPos - 1
            // If it contains Decimal but no - sign
            decimalPos != -1 ->
                decimalPos
            // If it contains - sign but no decimal
            text[0] == '-' ->
                text.length - 1
            // If it contains no decimal or - sign
            else ->
                text.length
        }
    }

    // Find number of decimal places. Answer differs based on if a decimal point and/or negative sign is present
    fun getDecimalPrecision(text: String) : Int {

        // If text has a decimal point
        return if(text.contains(".")) {

            val integerPlaces = getIntegerDigits(text)

            // If also has negative sign
            if(text[0] == '-')
                text.length - integerPlaces - 2
            else
                text.length - integerPlaces - 1
        }
        // If no decimal point, no decimal precision
        else
            0
    }

    override fun test(text: String): Boolean {
        try {

            // Accept empty input
            if(text == "")
                return true

            // Accept a decimal point alone, and/or negative sign
            if (text == "." || ((text == "-" || text == "-.") && minValue < 0))
                return true

            val integerPlaces = getIntegerDigits(text)
            val decimalPlaces = getDecimalPrecision(text)
            val totalDigits = integerPlaces + decimalPlaces

            // A "d" or "f" at the end of a number is accepted, as this indicates how to format the number.
            // Also accepts a space (" "). We do not want this, so explicitly reject these scenarios.
            // Also, check max precision, max int digits, and max total digits
            if (text.toLowerCase(Locale.ROOT).contains("d") || text.toLowerCase(Locale.ROOT).contains("f") ||
                    text.contains(" ") || decimalPlaces > maxPrecision || integerPlaces > maxIntDigits ||
                    totalDigits > maxTotalDigits)
                        return false

            // If not a double, will fall to catch
            val value = text.toDouble()

            // Make sure value is in range
            if (value in minValue..maxValue)
                return true
        } catch (e: NumberFormatException) {
        }

        return false
    }
}