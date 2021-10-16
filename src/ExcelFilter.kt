import java.io.File
import java.util.*
import javax.swing.filechooser.FileFilter

object ExcelExtensions {

    val ACCEPTABLE_EXTENSIONS = arrayOf("csv", "xls", "xlsx")
    /*
    * Get the extension of a file.
    */
    fun getExtension(f: File): String? {
        var ext: String? = null
        val s = f.name
        val i = s.lastIndexOf('.')
        if (i > 0 && i < s.length - 1) {
            ext = s.substring(i + 1).lowercase(Locale.ROOT)
        }
        return ext
    }
}

class ExcelFilter : FileFilter() {
    override fun accept(f: File): Boolean {
        if (f.isDirectory) {
            return true
        }

        val extension = ExcelExtensions.getExtension(f)
        return if (extension != null) {
            ExcelExtensions.ACCEPTABLE_EXTENSIONS.contains(extension)
        } else false
    }

    override fun getDescription(): String {
        var string =  "Excel Files ("

        for((count, extension) in ExcelExtensions.ACCEPTABLE_EXTENSIONS.withIndex()) {
            if (count > 0)
                string += ", "
            string += "*.$extension"
        }

        string += ")"

        return string
    }
}