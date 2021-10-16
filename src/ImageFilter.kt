import java.io.File
import java.util.*
import javax.swing.filechooser.FileFilter

object ImageExtensions {

    val ACCEPTABLE_EXTENSIONS = arrayOf("bitmap",  "bmp", "gif", "jpeg", "jpg", "png", "tif", "tiff" )
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

class ImageFilter : FileFilter() {
    override fun accept(f: File): Boolean {
        if (f.isDirectory) {
            return true
        }

        val extension = ImageExtensions.getExtension(f)
        return if (extension != null) {
            ImageExtensions.ACCEPTABLE_EXTENSIONS.contains(extension)
        } else false
    }

    override fun getDescription(): String {
        var string =  "Image Files ("

        for((count, extension) in ImageExtensions.ACCEPTABLE_EXTENSIONS.withIndex()) {
            if (count > 0)
                string += ", "
            string += "*.$extension"
        }

        string += ")"

        return string
    }
}