import items.Book;
import items.BookPage;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.MessageFormat;

public class NewFolderDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField titleInput;
    private JTextField pagesInput;
    private JTextField denominationInput;
    private JTextField startYearInput;
    private JTextField endYearInput;
    private JButton importButton;
    private JButton preMadeButton;

    private final JFrame parent;

    public NewFolderDialog(JFrame parent) {
        super(parent);
        setContentPane(contentPane);
        setModal(true);
        setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
        setResizable(false);
        getRootPane().setDefaultButton(buttonOK);

        //NumismatistAPI api = ((Main)parent).api;

        this.parent = parent;

        setTitle(Main.getString("newFolder_title"));

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        // Set input filters
        MyIntFilter pagesFilter = new MyIntFilter();
        pagesFilter.setMinValue(1);
        pagesFilter.setMaxDigits(2);
        ((PlainDocument) pagesInput.getDocument()).setDocumentFilter(pagesFilter);

        ((PlainDocument) startYearInput.getDocument()).setDocumentFilter(MyDocFilter.Companion.getCurrentYearFilter());
        ((PlainDocument) endYearInput.getDocument()).setDocumentFilter(MyDocFilter.Companion.getCurrentYearFilter());

        ((PlainDocument) denominationInput.getDocument()).setDocumentFilter(MyDocFilter.Companion.getDenominationFilter());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        importButton.addActionListener(e -> {
            final JFileChooser fc = new JFileChooser();

            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fc.setFileFilter(new XmlFilter());
            fc.setCurrentDirectory(new File(NumismatistAPI.Companion.getResPath("xml", "books")));

            int returnVal = fc.showOpenDialog(parent);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();

                BookPage page = ((Main) parent).api.importBook(file.getAbsolutePath()).getPages().get(0);
                BookPageDisplay pageDisplay = new BookPageDisplay(parent, page);
                ((Main) parent).changeScreen(pageDisplay.getPanel(), MessageFormat.format(Main.getString("book_title"),
                        page.getBook().getTitle()  ,1));

                dispose();
            }
        });

        preMadeButton.addActionListener(e -> {
            PremadeFolderDialog folderDialog = new PremadeFolderDialog(parent);
            folderDialog.pack();
            folderDialog.setLocationRelativeTo(this);
            folderDialog.setVisible(true);

            if(!folderDialog.cancelled)
                dispose();
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        int startYear = 0;
        int endYear = 0;

        try {
            startYear = Integer.parseInt(startYearInput.getText());
        }
        catch (NumberFormatException ignored) {
        }

        try {
            endYear = Integer.parseInt(endYearInput.getText());
        }
        catch (NumberFormatException ignored) {

        }

        String title = "";
        String errorMessage = "";

        if(titleInput.getText().isBlank()) {
            errorMessage += Main.getString("newFolder_error_emptyTitle");
        }
        else {
            title = titleInput.getText();
        }

        int pages = 0;

        if(pagesInput.getText().isBlank()) {

            if(!errorMessage.equals("")) {
                errorMessage += "\n";
            }
            errorMessage += Main.getString("newFolder_error_emptyPages");
        }
        else {
            pages = Integer.parseInt(pagesInput.getText());
        }

        if(startYear > endYear && endYear != 0) {
            if(!errorMessage.equals("")) {
                errorMessage += "\n";
            }
            errorMessage += Main.getString("newFolder_error_startYearGreater");
        }
        else if(startYear == 0 && endYear != 0) {
            if(!errorMessage.equals("")) {
                errorMessage += "\n";
            }
            errorMessage += Main.getString("newFolder_error_startYearEmpty");
        }

        // Input is valid
        if(errorMessage.equals("")) {

            Book book = new Book();
            book.setTitle(title);

            book.setStartYear(startYear);
            book.setEndYear(endYear);

            try {
                if (!denominationInput.getText().isBlank()) {
                    book.setDenomination(Double.parseDouble(denominationInput.getText()));
                }
            }
            catch (NumberFormatException nfe) {
                book.setDenomination(-1);
            }

            // Add pages to book
            for(int i = 0; i < pages; i++) {
                BookPage page = new BookPage();
                book.addPage(page);
                page.setBook(book);
                page.setPageNum(i+1);
            }

            // Show new page dialog
            NewFolderPageDialog newPageDialog = new NewFolderPageDialog(parent, book, 1);
            newPageDialog.pack();
            newPageDialog.setLocationRelativeTo(this);
            newPageDialog.setVisible(true);

            if(!newPageDialog.cancelled)
                dispose();
        }
        else {
            JOptionPane.showMessageDialog(parent, errorMessage,
                    Main.getString("newFolder_error_title"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCancel() {
        dispose();
    }

    private void createUIComponents() {


    }
}
