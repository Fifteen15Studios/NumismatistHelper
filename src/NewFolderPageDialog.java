import items.Book;
import items.BookPage;
import items.PageSlot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.MessageFormat;
import java.util.ArrayList;

public class NewFolderPageDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JCheckBox alternatingCheckbox;
    private JCheckBox customCheckbox;
    private JComboBox sizeDropDown;
    private JSpinner rowsSpinner;
    private JSpinner perRowSpinner;
    private JSpinner perRowSpinner2;

    private final JFrame parent;
    private final Book book;
    private final int pageNum;

    private final BookPage page;

    boolean cancelled = false;

    public NewFolderPageDialog(JFrame parent, Book book, int pageNum) {
        super(parent);
        setContentPane(contentPane);
        setModal(true);
        setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
        getRootPane().setDefaultButton(buttonOK);
        setResizable(false);

        this.parent = parent;
        this.book = book;
        this.pageNum = pageNum;

        this.page = new BookPage();

        setTitle(MessageFormat.format(Main.getString("newPage_title"), book.getTitle(), pageNum));

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        sizeDropDown.addItem(Main.getString("newPage_dropdown_cent"));
        sizeDropDown.addItem(Main.getString("newPage_dropdown_nickel"));
        sizeDropDown.addItem(Main.getString("newPage_dropdown_dime"));
        sizeDropDown.addItem(Main.getString("newPage_dropdown_quarter"));
        sizeDropDown.addItem(Main.getString("newPage_dropdown_half"));
        sizeDropDown.addItem(Main.getString("newPage_dropdown_dollar"));

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // Handle checkbox clicks
        customCheckbox.addActionListener(e -> {
            if(customCheckbox.isSelected()) {
                alternatingCheckbox.setEnabled(false);
                perRowSpinner.setEnabled(false);
                perRowSpinner2.setEnabled(false);
            }
            else {
                alternatingCheckbox.setEnabled(true);
                perRowSpinner.setEnabled(true);
                if(alternatingCheckbox.isSelected())
                    perRowSpinner2.setEnabled(true);
            }
        });

        alternatingCheckbox.addActionListener(e -> perRowSpinner2.setEnabled(alternatingCheckbox.isSelected()));
    }

    private void onOK() {

        int rows = (Integer)rowsSpinner.getValue();
        int perRow = (Integer)perRowSpinner.getValue();
        int perRow2 = (Integer)perRowSpinner2.getValue();

        ArrayList<PageSlot> currentRow;

        String errorMessage = "";

        // Validate input
        if(rows < 1) {
            errorMessage += Main.getString("newPage_error_invalidRows");
        }
        if(perRow < 1) {
            if(!errorMessage.equals("")) {
                errorMessage += "\n";
            }
            errorMessage += Main.getString("newPage_error_invalidCoins");
        }
        if(alternatingCheckbox.isSelected() && perRow2 < 1) {
            if(!errorMessage.equals("")) {
                errorMessage += "\n";
            }
            errorMessage += Main.getString("newPage_error_invalidRow2");
        }

        // If input not valid
        if(!errorMessage.equals("")) {
            JOptionPane.showMessageDialog(parent, errorMessage,
                    Main.getString("newPage_error_title"), JOptionPane.ERROR_MESSAGE);
        }
        // If input is valid
        else {

            String sizeStr = sizeDropDown.getSelectedItem().toString();
            int size;

            if(sizeStr.equals(Main.getString("newPage_dropdown_cent")))
                size = PageSlot.SIZE_PENNY;
            else if(sizeStr.equals(Main.getString("newPage_dropdown_nickel")))
                size = PageSlot.SIZE_NICKEL;
            else if(sizeStr.equals(Main.getString("newPage_dropdown_dime")))
                size = PageSlot.SIZE_DIME;
            else if(sizeStr.equals(Main.getString("newPage_dropdown_quarter")))
                size = PageSlot.SIZE_QUARTER;
            else if(sizeStr.equals(Main.getString("newPage_dropdown_half")))
                size = PageSlot.SIZE_HALF;
            else
                size = PageSlot.SIZE_DOLLAR;

            // for each row
            for (int i = 0; i < rows; i++) {

                currentRow = new ArrayList<>();

                // If all rows are the same or if variable i is even
                if (!customCheckbox.isSelected() && (!alternatingCheckbox.isSelected() || i % 2 == 0)) {
                    for (int c = 0; c < perRow; c++) {
                        PageSlot slot = new PageSlot();
                        slot.setSize(size);
                        slot.setBookPage(page);
                        slot.setRowNum(i+1);
                        slot.setColNum(c+1);

                        currentRow.add(slot);
                    }
                }
                // If on alternating row
                else if (!customCheckbox.isSelected()) {
                    for (int c = 0; c < perRow2; c++) {
                        PageSlot slot = new PageSlot();
                        slot.setSize(size);
                        slot.setBookPage(page);
                        slot.setRowNum(i+1);
                        slot.setColNum(c+1);

                        currentRow.add(slot);
                    }
                }
                page.getRows().add(currentRow);
            }

            page.setPageNum(pageNum);
            page.setBook(book);
            book.getPages().set(pageNum - 1, page);

            // TODO: Handle custom page

            if(pageNum == book.getPages().size()) {
                //TODO: save book and pages

                //show book
                BookPageDisplay pageDisplay = new BookPageDisplay(parent, book.getPages().get(0));
                ((Main) parent).changeScreen(pageDisplay.getPanel(),
                        MessageFormat.format(Main.getString("book_title"), book.getTitle(), pageNum));

                dispose();
            }
            //Go to next page if there are more
            else {
                NewFolderPageDialog newPageDialog = new NewFolderPageDialog(parent, book, pageNum +1);
                newPageDialog.pack();
                newPageDialog.setLocationRelativeTo(this);
                // Hide for now
                setVisible(false);
                newPageDialog.setVisible(true);

                // Remove is next dialog is finished
                if(!newPageDialog.cancelled)
                    dispose();
                // Show if next dialog was cancelled
                else
                    setVisible(true);
            }
        }
    }

    private void onCancel() {

        cancelled = true;
        // add your code here if necessary
        dispose();
    }
}
