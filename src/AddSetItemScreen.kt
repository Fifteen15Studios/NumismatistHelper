import items.DatabaseItem
import items.Set
import java.io.File
import java.io.IOException
import java.text.MessageFormat
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JTextField

open class AddSetItemScreen(var parent: JFrame) {

    companion object {
        // Determine which tab should be shown upon returning to collection screen
        const val TAB_COINS = 0
        const val TAB_BILLS = 2
        const val TAB_SETS = 1
    }

    var returnTab = TAB_COINS

    var parentSet: Set? = null
    var editingParent = false
    var previousScreen: AddSetScreen? = null

    var imageObvLocationInput: JTextField? = null
    var imageRevLocationInput: JTextField? = null

    var obvPicPanel: JPanel? = null
    var revPicPanel: JPanel? = null

    var obvImageLocation = ""
    var revImageLocation = ""

    var validObvImg = false
    var validRevImg = false

    var fromCollection = false

    val BUTTON_OK = 1
    val BUTTON_CANCEL= 2;
    val BUTTON_SAVE_NEW = 3
    val BUTTON_SAVE_COPY = 4

    lateinit var api: NumismatistAPI

    open fun addImage(pathToImage: String, obverse: Boolean) : Boolean {
        return try {
            ImageIO.read(File(pathToImage))
            if (obverse) {
                obvImageLocation = pathToImage
                validObvImg = true
                // Force it to draw immediately
                obvPicPanel?.update(obvPicPanel?.graphics)
            } else {
                revImageLocation = pathToImage
                validRevImg = true
                // Force it to draw immediately
                revPicPanel?.update(revPicPanel?.graphics)
            }
            true
        } catch (e: IOException) {
            false
        }
    }

    /**
     * Opens a file chooser to choose an image for this item.
     *
     * @param obverse True if this image is for the obverse (front) of the item, false if for the reverse (back)
     *
     * @return Path of the chosen file
     */
    open fun openFileChooser(obverse: Boolean) : String {
        val fc = JFileChooser()
        val imageFilter = ImageFilter()

        val imageLocation : String

        fc.addChoosableFileFilter(imageFilter)
        fc.fileFilter = imageFilter
        fc.currentDirectory = File(Main.getSettingLastDirectory())
        val returnVal = fc.showOpenDialog(parent)
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            val file = fc.selectedFile
            if (obverse) {
                obvImageLocation = file.absolutePath
                imageObvLocationInput?.text = obvImageLocation
                Main.setSettingLastDirectory(obvImageLocation)
            } else {
                revImageLocation = file.absolutePath
                imageRevLocationInput?.text = revImageLocation
                Main.setSettingLastDirectory(revImageLocation)
            }
            imageLocation = file.absolutePath
            addImage(file.absolutePath, obverse)
        } else if (returnVal != JFileChooser.CANCEL_OPTION) {
            imageLocation = ""
        }
        else
            imageLocation = ""

        return imageLocation
    }

    /**
     * Removes an image from this item.
     *
     * @param obverse True if this image is for the obverse (front) of the item, false if for the reverse (back)
     */
    open fun removeImage(obverse: Boolean) {
        if (obverse) {
            obvImageLocation = ""
            imageObvLocationInput!!.text = ""
            obvPicPanel!!.update(obvPicPanel!!.graphics)
            validObvImg = true
        } else {
            revImageLocation = ""
            imageRevLocationInput!!.text = ""
            revPicPanel!!.update(revPicPanel!!.graphics)
            validRevImg = true
        }
    }

    fun goHome() {
        // If this item is in a set
        if (parentSet != null) {
            val setScreen: AddSetScreen
            // Theoretically, this should never be false
            if (previousScreen != null)
                setScreen = previousScreen!!
            else {
                setScreen = AddSetScreen(parent, parentSet)
                setScreen.fromCollection = fromCollection
            }

            val newTitle =
                if (previousScreen != null && setScreen.set.id == DatabaseItem.ID_INVALID) {
                    if (previousScreen!!.parentSet != null) {
                        if(previousScreen!!.parentSet!!.name != "")
                            MessageFormat.format(Main.getString("addSet_title_addToSet"), previousScreen!!.parentSet!!.name)
                        else
                            MessageFormat.format(Main.getString("addSet_title_addToSet"), NumismatistAPI.getString("property_set_toString"))
                    } else {
                        Main.getString("addSet_title_add")
                    }
                } else if (parentSet?.id == DatabaseItem.ID_INVALID) {
                    Main.getString("addSet_title_add")
                }
                else {
                    val name = if(parentSet!!.name == "")
                        NumismatistAPI.getString("property_set_toString")
                    else
                        parentSet!!.name

                    MessageFormat.format(Main.getString("addSet_title_edit"), name)
                }

            (parent as Main).changeScreen(setScreen.panel, newTitle)
        }
        // If previous screen was collection screen
        else if (fromCollection) {
            val collectionTableScreen = CollectionTableScreen(parent)
            collectionTableScreen.setTab(returnTab)
            (parent as Main).changeScreen(collectionTableScreen.panel, Main.getString("viewColl_title"))
        }
        else {
            (parent as Main).changeScreen((parent as Main).panel, "")
        }
    }
}