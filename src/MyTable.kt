
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.swing.JTable
import javax.swing.table.TableModel
import javax.swing.table.TableRowSorter

class MyTable : JTable() {
    fun hideColumn(columnName: String) {
        getColumn(columnName).minWidth = 0 // Must be set before maxWidth!!
        getColumn(columnName).maxWidth = 0
        getColumn(columnName).width = 0
    }

    fun addSort(numberColumns: ArrayList<Int>? = null, currencyColumns: ArrayList<Int>? = null) {
        val sorter: TableRowSorter<TableModel> = TableRowSorter()
        rowSorter = sorter
        sorter.model = model
        val textColumns = ArrayList<Int>()
        textColumns.add(1)

        val numComparator = Comparator { name1: String, name2: String ->
            try {
                val one = name1.toDouble()
                val two = name2.toDouble()
                one.compareTo(two)
            } catch (e: NumberFormatException) {
                0
            }
        } as Comparator<String>

        val currencyComparator = Comparator { name1: String, name2: String ->
            if (name1 == " " || name2 == " " ||
                name1.startsWith("<HTML>") || name2.startsWith("<HTML>")
            )
                0
            else {
                // will filter out the numbers from the currency symbols
                val matcher1: Matcher = Pattern.compile("\\d+[.]*\\d*").matcher(name1)
                val matcher2: Matcher = Pattern.compile("\\d+[.]*\\d*").matcher(name2)
                matcher1.find()
                matcher2.find()

                matcher1.group().toDouble().compareTo(matcher2.group().toDouble())
            }
        } as Comparator<String>

        val textComparator = Comparator { name1: String, name2: String ->
            if (name1 == " " || name2 == " " ||
                name1.startsWith("<HTML>") || name2.startsWith("<HTML>"))
                0
            else {
                name1.compareTo(name2)
            }
        } as Comparator<String>

        // Add all non-number rows to the list of text rows
        for (i in 0 until columnCount)
            textColumns.add(i)

        if(numberColumns != null) {
            // Number based columns
            for (num in numberColumns) {
                sorter.setComparator(num, numComparator)
                textColumns.remove(num)
            }
        }

        if(currencyColumns != null) {
            for (num in currencyColumns) {
                sorter.setComparator(num, currencyComparator)
                textColumns.remove(num)
            }
        }

        // Text based columns
        for (num in textColumns) {
            sorter.setComparator(num, textComparator)
        }
    }

    fun resizeColumns() {
        for (column in 0 until columnCount) {
            val tableColumn = columnModel.getColumn(column)
            var preferredWidth = tableColumn.minWidth
            val maxWidth = tableColumn.maxWidth
            for (row in 0 until rowCount) {
                val cellRenderer = getCellRenderer(row, column)
                val c = prepareRenderer(cellRenderer, row, column)
                val width = c.preferredSize.width + intercellSpacing.width
                preferredWidth = Math.max(preferredWidth, width)

                //  We've exceeded the maximum width, no need to check other rows
                if (preferredWidth >= maxWidth) {
                    preferredWidth = maxWidth
                    break
                }
            }
            tableColumn.preferredWidth = preferredWidth
        }
    }
}