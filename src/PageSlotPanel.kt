import items.PageSlot
import javafx.scene.input.KeyCode
import java.awt.Component
import java.awt.Dimension
import java.awt.event.*
import javax.swing.*


// A JPanel for displaying a coin slot, including 2 lines of text for year and mintage, or whatever else may be there
class PageSlotPanel(private var buttonSize: Int = PageSlot.SIZE_DOLLAR, line1Text: String = "", line2Text: String = "") : JPanel() {

    lateinit var page: BookPageDisplay
    val radioButton = JRadioButton()
    private val line1 = JLabel()
    private val line2 = JLabel()
    private val line1Input = JTextField()
    private val line2Input = JTextField()

    private val clickToChange = Main.getString("book_clickToChange")

    var slot = PageSlot()
    set(value) {

        val oldSize = field.size
        field = value

        if(value.size == -1)
            setSlotSize()

        if(oldSize != value.size)
            // Resize all icons and set them
            setIcons()
    }

    init {

        // Set layout to a vertical box layout
        val boxLayout = BoxLayout(this, BoxLayout.Y_AXIS)
        layout = boxLayout
        // set margins around the slot - to get spacing between slots
        border = BorderFactory.createEmptyBorder(10,10,10,10)

        // Center each item before adding
        radioButton.alignmentX = Component.CENTER_ALIGNMENT
        add(radioButton)

        line1.alignmentX = Component.CENTER_ALIGNMENT
        add(line1)
        line1.text = line1Text.ifBlank { clickToChange }

        line1Input.setSize(line1.width, line1.height)
        add(line1Input)
        line1Input.isVisible = false

        line2.alignmentX = Component.CENTER_ALIGNMENT
        add(line2)
        line2.text = line2Text.ifBlank { clickToChange }

        line2Input.setSize(line2.width, line2.height)
        add(line2Input)
        line2Input.isVisible = false

        // Add enter key listener
        line1Input.addActionListener {
            run {
                slot.line1Text = line1Input.text

                if(line1Input.text.isNotBlank()) {
                    line1.text = line1Input.text
                }
                else {
                    line1.text = clickToChange
                }

                line1Input.isVisible = false
                line1.isVisible = true
            }
        }

        // Add escape key listener
        line1Input.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent?) {
            }

            override fun keyPressed(e: KeyEvent?) {
                if(e?.keyCode == KeyCode.ESCAPE.code) {
                    line1Input.isVisible = false
                    line1.isVisible = true
                }
            }

            override fun keyReleased(e: KeyEvent?) {
            }

        })

        // Add enter key listener
        line2Input.addActionListener {
            run {
                slot.line2Text = line2Input.text

                if(line2Input.text.isNotBlank()) {
                    line2.text = line2Input.text
                }
                else
                    line2.text = clickToChange

                line2Input.isVisible = false
                line2.isVisible = true
            }
        }

        // Add escape key listener
        line2Input.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent?) {
            }

            override fun keyPressed(e: KeyEvent?) {
                if(e?.keyCode == KeyCode.ESCAPE.code) {
                    line2Input.isVisible = false
                    line2.isVisible = true
                }
            }

            override fun keyReleased(e: KeyEvent?) {
            }

        })

        // Crate a mouse listener to listen for mouse over and clicks on labels and panel
        // This will change the icon for the radio button and perform a click on the radio button
        val mouseAdapter = object: MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {

                // If right-clicked
                if(SwingUtilities.isRightMouseButton(e)) {

                    // Create right click menu
                    val rightClickMenu = JPopupMenu()
                    val edit = JMenuItem(Main.getString("book_rightClick_edit"))

                    if(e?.source == line1) {
                        // Create edit item
                        edit.addActionListener {

                            val maxWidth = if(line1.text == clickToChange)
                                line1.width
                            else
                                line1.width + 25

                            // Allow Edit
                            line1Input.maximumSize = Dimension(maxWidth, line1.height)
                            line1.isVisible = false
                            if(line1Text != clickToChange)
                                line1Input.text = line1.text
                            line1Input.isVisible = true
                            line1Input.grabFocus()
                        }

                        rightClickMenu.add(edit)

                        // Show the menu at the location of the click
                        rightClickMenu.show(line1, e.point.x, e.point.y)
                    }
                    else if(e?.source == line2) {

                        val maxWidth = if(line2.text == clickToChange)
                            line2.width
                        else
                            line2.width + 25

                        // Create edit item
                        edit.addActionListener {
                            // Allow Edit
                            line2Input.maximumSize = Dimension(maxWidth, line2.height)
                            line2.isVisible = false
                            if(line2Text != clickToChange)
                                line2Input.text = line2.text
                            line2Input.isVisible = true
                            line2Input.grabFocus()
                        }

                        rightClickMenu.add(edit)

                        // Show the menu at the location of the click
                        rightClickMenu.show(line2, e.point.x, e.point.y)
                    }
                }
                // If it's a label, and label is empty, change label
                else if(e?.source == line1 && slot.line1Text.isBlank()) {
                    line1Input.maximumSize = Dimension(line1.width, line1.height)
                    line1.isVisible = false
                    line1Input.isVisible = true
                    line1Input.grabFocus()
                }
                else if(e?.source == line2 && slot.line2Text.isBlank()) {
                    line2Input.maximumSize = Dimension(line2.width, line2.height)
                    line2.isVisible = false
                    line2Input.isVisible = true
                    line2Input.grabFocus()
                }
                // Handle the click
                else {
                    // If slot is empty
                    if(!radioButton.isSelected) {
                        val options1 = arrayOf<Any>(Main.getString("book_clickMenu_newCoin"),
                            Main.getString("book_clickMenu_existingCoin"),
                            Main.getString("cancel"))

                        val result = JOptionPane.showOptionDialog(null,
                            Main.getString("book_clickMenu_emptyMessage"),
                            Main.getString("book_clickMenu_emptyTitle"),
                                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                                null, options1, options1[2])
                        if (result == JOptionPane.YES_OPTION) {
                            // TODO: Show New Coin Screen

                            radioButton.doClick()
                        }
                        if(result == JOptionPane.NO_OPTION) {
                            // TODO: Show Existing coin selection

                            radioButton.doClick()
                        }
                    }
                    else {
                        // Display "Remove from collection", "Remove from Book", "Cancel" Dialog
                        val options1 = arrayOf<Any>(Main.getString("book_clickMenu_removeColl"),
                            Main.getString("book_clickMenu_removeBook"),
                            Main.getString("cancel"))

                        val result = JOptionPane.showOptionDialog(null,
                                Main.getString("book_clickMenu_filledMessage"),
                                Main.getString("book_clickMenu_filledTitle"),
                                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                                null, options1, options1[2])
                        // Remove from collection
                        if (result == JOptionPane.YES_OPTION) {
                            val remove = JOptionPane.showConfirmDialog(null,
                                Main.getString("book_dialog_sureMessage"),
                                Main.getString("book_dialog_sureTitle"),
                                JOptionPane.YES_NO_OPTION)

                            if(remove == JOptionPane.YES_OPTION) {
                                // Add coin to list to be removed from collection
                                slot.bookPage.coinsToDelete.add(slot.coin!!)
                                slot.removeCoin()

                                radioButton.doClick()
                            }
                        }
                        // Remove from book
                        if(result == JOptionPane.NO_OPTION) {
                            slot.removeCoin()

                            radioButton.doClick()
                        }
                    }

                    setIcons(true)
                }
            }
            override fun mouseEntered(e: MouseEvent?) {
                // If on a label, and label can change
                if((e?.source == line1 && slot.line1Text.isBlank()) ||
                        (e?.source == line2 && slot.line2Text.isBlank())) {
                    // Don't highlight
                }
                else
                    setIcons(true)
            }

            override fun mouseExited(e: MouseEvent?) {
                setIcons()
            }
        }

        // Apply the mouse listener to all items
        line1.addMouseListener(mouseAdapter)
        line2.addMouseListener(mouseAdapter)
        addMouseListener(mouseAdapter)
        for(listener in radioButton.mouseListeners) {
            radioButton.removeMouseListener(listener)
        }
        radioButton.addMouseListener(mouseAdapter)
    }

    /**
     * Resize all icons and set them
     *
     * @param hovered True if something is being hovered over, otherwise false
     */
    private fun setIcons(hovered: Boolean = false) {

        // TODO: Create disabled icons?
        radioButton.rolloverIcon = page.bookPageHelper.getIcon(BookPageHelper.MODE_EMPTY_HOVERED, buttonSize)

        if(hovered)
            radioButton.icon = page.bookPageHelper.getIcon(BookPageHelper.MODE_EMPTY_HOVERED, buttonSize)
        else {
            radioButton.icon = page.bookPageHelper.getIcon(BookPageHelper.MODE_EMPTY, buttonSize)
        }

        when (buttonSize) {
            PageSlot.SIZE_PENNY -> {
                setSelectedIcon(page.bookPageHelper.getIcon(BookPageHelper.MODE_FILLED, PageSlot.SIZE_PENNY),
                    page.bookPageHelper.getIcon(BookPageHelper.MODE_FILLED_HOVERED,
                        PageSlot.SIZE_PENNY), hovered)
            }
            PageSlot.SIZE_NICKEL -> {
                setSelectedIcon(page.bookPageHelper.getIcon(BookPageHelper.MODE_FILLED, PageSlot.SIZE_NICKEL),
                    page.bookPageHelper.getIcon(BookPageHelper.MODE_FILLED_HOVERED, PageSlot.SIZE_NICKEL), hovered)
            }
            PageSlot.SIZE_DIME -> {
                setSelectedIcon(page.bookPageHelper.getIcon(BookPageHelper.MODE_FILLED, PageSlot.SIZE_DIME),
                    page.bookPageHelper.getIcon(BookPageHelper.MODE_FILLED_HOVERED, PageSlot.SIZE_DIME), hovered)
            }
            PageSlot.SIZE_QUARTER -> {
                setSelectedIcon(page.bookPageHelper.getIcon(BookPageHelper.MODE_FILLED, PageSlot.SIZE_QUARTER),
                    page.bookPageHelper.getIcon(BookPageHelper.MODE_FILLED_HOVERED, PageSlot.SIZE_QUARTER), hovered)
            }
            PageSlot.SIZE_HALF -> {
                setSelectedIcon(page.bookPageHelper.getIcon(BookPageHelper.MODE_FILLED, PageSlot.SIZE_HALF),
                    page.bookPageHelper.getIcon(BookPageHelper.MODE_FILLED_HOVERED, PageSlot.SIZE_HALF), hovered)
            }
            else -> {
                setSelectedIcon(page.bookPageHelper.getIcon(BookPageHelper.MODE_FILLED, PageSlot.SIZE_DOLLAR),
                    page.bookPageHelper.getIcon(BookPageHelper.MODE_FILLED_HOVERED, PageSlot.SIZE_DOLLAR), hovered)
            }

        }
    }

    /**
     * sets icons for when something is hovered over and when it's selcted
     *
     * @param icon icon to display when not hovered
     * @param hoverIcon icon to display when hovering
     * @param hovered whether it is currently being hovered over
     */
    private fun setSelectedIcon(icon: Icon, hoverIcon: Icon, hovered: Boolean) {

        radioButton.rolloverSelectedIcon = hoverIcon

        if(hovered)
            radioButton.selectedIcon = hoverIcon
        else {
            radioButton.selectedIcon = icon
        }
    }

    /**
     * If slot has a size, use that for button size. Otherwise, use denomination from book to determine size
     */
    private fun setSlotSize() {
        if(slot.size == -1) {
            buttonSize = when (slot.bookPage.book.denomination) {
                .01 -> PageSlot.SIZE_PENNY
                .05 -> PageSlot.SIZE_NICKEL
                .25 -> PageSlot.SIZE_QUARTER
                .5 -> PageSlot.SIZE_HALF
                else -> PageSlot.SIZE_DOLLAR
            }
            slot.size = buttonSize
        }
        else {
            buttonSize = slot.size
        }
    }
}