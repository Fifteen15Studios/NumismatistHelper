import java.io.File
import javax.swing.filechooser.FileFilter

object XmlExtensions {

    val ACCEPTABLE_EXTENSIONS = arrayOf("xml")
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

class XmlFilter : FileFilter() {
    override fun accept(f: File): Boolean {
        if (f.isDirectory) {
            return true
        }

        val extension = XmlExtensions.getExtension(f)
        return if (extension != null) {
            XmlExtensions.ACCEPTABLE_EXTENSIONS.contains(extension)
        } else false
    }

    override fun getDescription(): String {
        var string =  "XML File ("

        for((count, extension) in XmlExtensions.ACCEPTABLE_EXTENSIONS.withIndex()) {
            if (count > 0)
                string += ", "
            string += "*.$extension"
        }

        string += ")"

        return string
    }
}