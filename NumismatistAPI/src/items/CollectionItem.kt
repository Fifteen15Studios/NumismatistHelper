package items

import NumismatistAPI
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

/**
 * CollectionItems are DatabaseItems which can have images attached to them, and can be put in a Container, such as Books.
 * Images can be of the obverse (front) or reverse (back) of the item.
 *
 * Subclasses include SetItems
 *
 * @see DatabaseItem
 * @see SetItem
 * @see Container
 * @see Book
 */
@Suppress("unused")
abstract class CollectionItem : DatabaseItem() {

    /**
     * Location of the image of the front of the item, including file extension
     */
    var obvImgPath = ""
        set(value) {
            if(oldObvPath=="")
                oldObvPath = field
            field = value
        }
    private var oldObvPath = ""

    /**
     * Location of the image of the back of the item, including file extension
     */
    var revImgPath = ""
        set(value) {
            if(oldRevPath=="")
                oldRevPath = field
            field = value
        }
    private var oldRevPath = ""
    var note = ""

    /**
     * ID of a container this item is in. If not in a container, should be ID_INVALID
     */
    var containerId = ID_INVALID

    /**
     * Gets the file extension of the image file for the front of the item
     *
     * @return File extension
     */
    fun getObvImageExtFromPath() : String {
        if(obvImgPath.contains("."))
            return obvImgPath.substring(obvImgPath.lastIndexOf('.'))

        return ""
    }

    /**
     * Gets the file extension of the image file for the back of the item
     *
     * @return File extension
     */
    fun getRevImageExtFromPath() : String {
        if(revImgPath.contains("."))
            return revImgPath.substring(revImgPath.lastIndexOf('.'))

        return ""
    }

    /**
     * Gets the default image path for an item
     *
     * @param obverse True if looking for path of image of front of the item, otherwise false
     * @param ext The file extension of the image file
     */
    fun generateImagePath(obverse: Boolean, ext: String) : String {
        if(this is Coin) {
            return if (obverse)
                "coin-$id-obv.$ext"
            else
                "coin-$id-rev.$ext"
        }
        if(this is Set) {
            return if (obverse)
                "set-$id-obv.$ext"
            else
                "set-$id-rev.$ext"
        }
        if(this is Bill) {
            return if (obverse)
                "bill-$id-obv.$ext"
            else
                "bill-$id-rev.$ext"
        }

        return if(obverse)
            "item-$id-obv.$ext"
        else
            "item-$id-rev.$ext"
    }

    /**
     * Saves both images to the file system
     *
     * @return True if save successful, otherwise false
     */
    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun saveImages() : Boolean {
            return saveObvImage() && saveRevImage()
    }

    /**
     * Saves image of the front of the item to the file system
     *
     * @return True if save successful, otherwise false
     */
    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun saveObvImage() : Boolean {

        // Delete old file
        if(File(oldObvPath).exists()) {
            try {
                File(oldObvPath).delete()
            }
            catch (se : SecurityException) {
                throw se
            }

            oldObvPath = ""
        }

        // Copy the image to a new location, then use that location
        if (obvImgPath != "" ) {
            val success = if (File(obvImgPath).exists()) {
                try {
                    !NumismatistAPI.copyFile(
                        obvImgPath,
                        generateImagePath(true, getObvImageExtFromPath()))
                }
                catch (ioe: IOException) {
                    throw ioe
                }
            }
            else {
                val fnf = FileNotFoundException(obvImgPath)
                throw fnf
            }

            return success
        }

        return true
    }

    /**
     * Saves image of the back of the item to the file system
     *
     * @return True if save successful, otherwise false
     */
    @Throws(SecurityException::class, IOException::class, FileNotFoundException::class)
    fun saveRevImage() : Boolean {
        // Delete old file
        if(File(oldRevPath).exists()) {
            try {
                File(oldRevPath).delete()
            }
            catch (se : SecurityException) {
                throw se
            }

            oldRevPath = ""
        }

        // Copy the image to a new location, then use that location
        if (revImgPath != "" ) {
            val success = if (File(revImgPath).exists()) {
                try {
                    !NumismatistAPI.copyFile(
                        revImgPath,
                        generateImagePath(false, getRevImageExtFromPath()))
                }
                catch (ioe: IOException) {
                    throw ioe
                }
            }
            else {
                val fnf = FileNotFoundException(revImgPath)
                throw fnf
            }

            return success
        }

        return true
    }

    /**
     * Deletes both images from the file system
     *
     * @return True if delete is successful, or if no file path is set. Otherwise false
     */
    @Throws(FileNotFoundException::class)
    fun deleteImages() : Boolean {
        return deleteObvImage() && deleteRevImage()
    }

    /**
     * Deletes image of the front of the item from the file system
     *
     * @return True if delete is successful, or if no file path is set. Otherwise false
     */
    @Throws(FileNotFoundException::class)
    fun deleteObvImage() : Boolean {

        if(obvImgPath != "" && obvImgPath == generateImagePath(true, getObvImageExtFromPath())) {
            val obvFile = File(obvImgPath)

            // Delete file
            if(obvFile.exists()) {
                val success = obvFile.delete()
                if(success)
                    obvImgPath = ""

                return success
            }
            else {
                val fnf = FileNotFoundException(obvImgPath)
                throw fnf
            }
        }

        return true
    }

    /**
     * Deletes image of the front of the item from the file system
     *
     * @return True if delete is successful, or if no file path is set. Otherwise false
     */
    @Throws(FileNotFoundException::class)
    fun deleteRevImage() : Boolean {
        if(revImgPath != "" && revImgPath == generateImagePath(false, getRevImageExtFromPath())) {
            val obvFile = File(revImgPath)

            // Delete file
            if(obvFile.exists()) {
                val success = obvFile.delete()
                if(success)
                    revImgPath = ""

                return success
            }
            else {
                val fnf = FileNotFoundException(revImgPath)
                throw fnf
            }
        }

        return true
    }

}