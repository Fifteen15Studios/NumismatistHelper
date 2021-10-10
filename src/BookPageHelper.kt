import items.PageSlot
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JRadioButton

class BookPageHelper {

    companion object {
        const val MODE_EMPTY = 1
        const val MODE_EMPTY_HOVERED = 2
        const val MODE_FILLED = 3
        const val MODE_FILLED_HOVERED = 4
    }

    private val penny : ImageIcon
    private val pennyEmpty : ImageIcon
    private val pennyEmptyHovered : ImageIcon
    private val pennyHovered : ImageIcon
    private val nickel : ImageIcon
    private val nickelEmpty : ImageIcon
    private val nickelEmptyHovered : ImageIcon
    private val nickelHovered : ImageIcon
    private val dime : ImageIcon
    private val dimeEmpty : ImageIcon
    private val dimeEmptyHovered : ImageIcon
    private val dimeHovered : ImageIcon
    private val quarter : ImageIcon
    private val quarterEmpty : ImageIcon
    private val quarterEmptyHovered : ImageIcon
    private val quarterHovered : ImageIcon
    private val half : ImageIcon
    private val halfEmpty : ImageIcon
    private val halfEmptyHovered : ImageIcon
    private val halfHovered : ImageIcon
    private val dollar : ImageIcon
    private val dollarEmpty : ImageIcon
    private val dollarEmptyHovered : ImageIcon
    private val dollarHovered : ImageIcon

    init {

        val resPath = NumismatistAPI.getResPath("images")

        // "cache" the images
        penny = resizeIcon(
            ImageIcon(
                ImageIO.read(
                    NumismatistAPI.getFileFromRes("${resPath}cent.png")
                )
            ), PageSlot.SIZE_PENNY, PageSlot.SIZE_PENNY
        )
        pennyEmpty = resizeIcon(
            ImageIcon(
                ImageIO.read(
                    NumismatistAPI.getFileFromRes("${resPath}radioButton.png"))),
            PageSlot.SIZE_PENNY, PageSlot.SIZE_PENNY)
        pennyEmptyHovered = resizeIcon(
            ImageIcon(
                ImageIO.read(
                    NumismatistAPI.getFileFromRes("${resPath}radioButton_hover.png"))),
            PageSlot.SIZE_PENNY, PageSlot.SIZE_PENNY)
        pennyHovered = resizeIcon(
            ImageIcon(
                ImageIO.read(
                    NumismatistAPI.getFileFromRes("${resPath}cent_hover.png"))),
            PageSlot.SIZE_PENNY, PageSlot.SIZE_PENNY)

        nickel = resizeIcon(
            ImageIcon(
                ImageIO.read(
                    NumismatistAPI.getFileFromRes("${resPath}nickel.png"))
            ), PageSlot.SIZE_NICKEL, PageSlot.SIZE_NICKEL)
        nickelEmpty = resizeIcon(
            ImageIcon(
                ImageIO.read(
                    NumismatistAPI.getFileFromRes("${resPath}radioButton.png"))),
            PageSlot.SIZE_NICKEL, PageSlot.SIZE_NICKEL)
        nickelEmptyHovered = resizeIcon(
            ImageIcon(
                ImageIO.read(
                    NumismatistAPI.getFileFromRes("${resPath}radioButton_hover.png"))),
            PageSlot.SIZE_NICKEL, PageSlot.SIZE_NICKEL)
        nickelHovered = resizeIcon(
            ImageIcon(
                ImageIO.read(
                    NumismatistAPI.getFileFromRes("${resPath}nickel_hover.png")
                )
            ), PageSlot.SIZE_NICKEL, PageSlot.SIZE_NICKEL
        )

        dime = resizeIcon (ImageIcon(
            ImageIO.read(
                NumismatistAPI.getFileFromRes("${resPath}dime.png")
            )
        ), PageSlot.SIZE_DIME, PageSlot.SIZE_DIME)
        dimeEmpty = resizeIcon(
            ImageIcon(
                ImageIO.read(
                    NumismatistAPI.getFileFromRes("${resPath}radioButton.png"))),
            PageSlot.SIZE_DIME, PageSlot.SIZE_DIME)
        dimeEmptyHovered = resizeIcon(
            ImageIcon(
                ImageIO.read(
                    NumismatistAPI.getFileFromRes("${resPath}radioButton_hover.png"))),
            PageSlot.SIZE_DIME, PageSlot.SIZE_DIME)
        dimeHovered = resizeIcon(
            ImageIcon(
                ImageIO.read(
                    NumismatistAPI.getFileFromRes("${resPath}dime_hover.png")
                )
            ), PageSlot.SIZE_DIME, PageSlot.SIZE_DIME
        )

        quarter = resizeIcon(
            ImageIcon(
                ImageIO.read(
                    NumismatistAPI.getFileFromRes("${resPath}quarter.png")
                )
            ), PageSlot.SIZE_QUARTER, PageSlot.SIZE_QUARTER
        )
        quarterEmpty = resizeIcon(
            ImageIcon(
                ImageIO.read(
                    NumismatistAPI.getFileFromRes("${resPath}radioButton.png"))),
            PageSlot.SIZE_QUARTER, PageSlot.SIZE_QUARTER)
        quarterEmptyHovered = resizeIcon(
            ImageIcon(
                ImageIO.read(
                    NumismatistAPI.getFileFromRes("${resPath}radioButton_hover.png"))),
            PageSlot.SIZE_QUARTER, PageSlot.SIZE_QUARTER)
        quarterHovered = resizeIcon (ImageIcon(
            ImageIO.read(
                NumismatistAPI.getFileFromRes("${resPath}quarter_hover.png")
            )
        ), PageSlot.SIZE_QUARTER, PageSlot.SIZE_QUARTER)

        half = resizeIcon(
                ImageIcon(
                    ImageIO.read(
                        NumismatistAPI.getFileFromRes("${resPath}half.png")
                    )
                ), PageSlot.SIZE_HALF, PageSlot.SIZE_HALF
            )
        halfEmpty = resizeIcon(
            ImageIcon(
                ImageIO.read(
                    NumismatistAPI.getFileFromRes("${resPath}radioButton.png"))),
            PageSlot.SIZE_HALF, PageSlot.SIZE_HALF)
        halfEmptyHovered = resizeIcon(
            ImageIcon(
                ImageIO.read(
                    NumismatistAPI.getFileFromRes("${resPath}radioButton_hover.png"))),
            PageSlot.SIZE_HALF, PageSlot.SIZE_HALF)
        halfHovered = resizeIcon(
            ImageIcon(
                ImageIO.read(
                    NumismatistAPI.getFileFromRes("${resPath}half_hover.png")
                )
            ), PageSlot.SIZE_HALF, PageSlot.SIZE_HALF
        )

        dollar = resizeIcon (ImageIcon(
            ImageIO.read(
                NumismatistAPI.getFileFromRes("${resPath}dollar.png")
            )
        ), PageSlot.SIZE_DOLLAR, PageSlot.SIZE_DOLLAR)
        dollarEmpty = resizeIcon(
            ImageIcon(
                ImageIO.read(
                    NumismatistAPI.getFileFromRes("${resPath}radioButton.png"))),
            PageSlot.SIZE_DOLLAR, PageSlot.SIZE_DOLLAR)
        dollarEmptyHovered = resizeIcon(
            ImageIcon(
                ImageIO.read(
                    NumismatistAPI.getFileFromRes("${resPath}radioButton_hover.png"))),
            PageSlot.SIZE_DOLLAR, PageSlot.SIZE_DOLLAR)
        dollarHovered = resizeIcon(
                ImageIcon(
                    ImageIO.read(
                        NumismatistAPI.getFileFromRes("${resPath}dollar_hover.png")
                    )
                ), PageSlot.SIZE_DOLLAR, PageSlot.SIZE_DOLLAR
            )
    }

    /**
     * Resizes a BufferedImage. Takes transparency into effect
     *
     * @param originalImage The image to resize
     * @param newWidth How wide to make the new image
     * @param newHeight How tall to make the new image
     *
     * @return The newly resized image
     */
    private fun resizeImage(originalImage: BufferedImage?, newWidth: Int, newHeight: Int): BufferedImage {

        val resizedImage = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics2D = resizedImage.createGraphics()
        graphics2D.drawImage(originalImage, 0, 0, newWidth, newHeight, null)
        graphics2D.dispose()
        return resizedImage
    }

    /**
     * Resizes an Icon. Takes transparency into effect
     *
     * @param originalIcon The icon to resize
     * @param newWidth How wide to make the new icon
     * @param newHeight How tall to make the new icon
     *
     * @return The newly resized icon
     */
    private fun resizeIcon(originalIcon: ImageIcon, newWidth: Int, newHeight: Int) : ImageIcon {
        // Turn icon into BufferedImage
        val iconImage = BufferedImage(originalIcon.iconWidth, originalIcon.iconWidth, BufferedImage.TYPE_INT_ARGB)
        val g = iconImage.createGraphics()
        originalIcon.paintIcon(JRadioButton(), g, 0,0)

        // Resize, turn back into icon, and return
        return ImageIcon(resizeImage(iconImage, newWidth, newHeight))
    }

    fun getIcon(mode: Int, size: Int) : ImageIcon {
        when(mode) {
            MODE_EMPTY_HOVERED -> return when(size) {
                PageSlot.SIZE_PENNY -> pennyEmptyHovered
                PageSlot.SIZE_NICKEL -> nickelEmptyHovered
                PageSlot.SIZE_QUARTER -> quarterEmptyHovered
                PageSlot.SIZE_HALF -> halfEmptyHovered
                PageSlot.SIZE_DOLLAR -> dollarEmptyHovered
                else -> dimeEmptyHovered
            }
            MODE_FILLED -> return when(size) {
                PageSlot.SIZE_PENNY -> penny
                PageSlot.SIZE_NICKEL -> nickel
                PageSlot.SIZE_QUARTER -> quarter
                PageSlot.SIZE_HALF -> half
                PageSlot.SIZE_DOLLAR -> dollar
                else -> dime
            }
            MODE_FILLED_HOVERED -> return when(size) {
                PageSlot.SIZE_PENNY -> pennyHovered
                PageSlot.SIZE_NICKEL -> nickelHovered
                PageSlot.SIZE_QUARTER -> quarterHovered
                PageSlot.SIZE_HALF -> halfHovered
                PageSlot.SIZE_DOLLAR -> dollarHovered
                else -> dimeHovered
            }
            else -> return when(size) {
                PageSlot.SIZE_PENNY -> pennyEmpty
                PageSlot.SIZE_NICKEL -> nickelEmpty
                PageSlot.SIZE_QUARTER -> quarterEmpty
                PageSlot.SIZE_HALF -> halfEmpty
                PageSlot.SIZE_DOLLAR -> dollarEmpty
                else -> dimeEmpty
            }
        }
    }
}