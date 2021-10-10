import java.io.File
import javax.swing.filechooser.FileFilter

object CsvExtensions {

    val ACCEPTABLE_EXTENSIONS = arrayOf("csv")
    /*
    * Get the extension of a file.
    */
    fun getExtension(f: File): String? {
        var ext: String? = null
        val s = f.name
        val i = s.lastIndexOf('.')
        if (i > 0 && i < s.length - 1) {
            ext = s.substring(i + 1).toLowerCase()
        }
        return ext
    }
}

class CsvFilter : FileFilter() {
    override fun accept(f: File): Boolean {
        if (f.isDirectory) {
            return true
        }

        val extension = CsvExtensions.getExtension(f)
        return if (extension != null) {
            CsvExtensions.ACCEPTABLE_EXTENSIONS.contains(extension)
        } else false
    }

    override fun getDescription(): String {
        var string =  "CSV File ("

        for((count, extension) in CsvExtensions.ACCEPTABLE_EXTENSIONS.withIndex()) {
            if (count > 0)
                string += ", "
            string += "*.$extension"
        }

        string += ")"

        return string
    }
}