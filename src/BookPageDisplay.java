import items.BookPage;
import items.Coin;
import items.PageSlot;

import javax.swing.*;
import java.io.IOException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;

public class BookPageDisplay extends MyScreen {

    private BookPage page;
    private JPanel panel;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JPanel displayPanel;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton nextPageButton;
    private JButton prevPageButton;

    BookPageHelper bookPageHelper = null;

    JFrame parent;

    private final NumismatistAPI api;

    public BookPageDisplay(JFrame parent) {
        super(parent);

        api = ((Main) parent).api;
        setPanel(panel);
        this.parent = parent;

        displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));

        buttonOK.addActionListener(e -> {

            StringBuilder errorMessage = new StringBuilder();

            // Delete coins in delete list
            for(Coin coin : page.getCoinsToDelete()) {
                try {
                    if(coin.removeFromDb(api)) {
                        if (!coin.deleteObvImage()) {
                            if(!errorMessage.toString().equals(""))
                                errorMessage.append("\n");

                            errorMessage.append(MessageFormat.format(Main.getString("error_deletingFile_message"), coin.getObvImgPath()));
                        }
                        if (!coin.deleteRevImage()) {
                            if(!errorMessage.toString().equals(""))
                                errorMessage.append("\n");

                            errorMessage.append(MessageFormat.format(Main.getString("error_deletingFile_message"), coin.getRevImgPath()));
                        }
                    }
                } catch (SecurityException | IOException | SQLException securityException) {
                    if(!errorMessage.toString().equals(""))
                        errorMessage.append("\n");

                    errorMessage.append(securityException.getMessage());
                }
            }

            if(errorMessage.toString().equals("")) {

                // Save book
                int rows = getPage().getBook().saveToDb(api);

                if(rows == 1)
                    ((Main) parent).changeScreen(((Main) parent).getPanel(), "");
                else {
                    errorMessage.append(api.getSuccessMessage(rows));
                }
            }

            // Display error message
            if(!errorMessage.toString().equals("")) {
                JOptionPane.showMessageDialog(parent, errorMessage.toString(),
                        Main.getString("error_genericTitle"), JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonCancel.addActionListener(e -> ((Main) parent).changeScreen(((Main) parent).getPanel(), ""));

        // Set margins around the edges of the page
        getPanel().setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        // Handle next page and previous page clicks
        prevPageButton.addActionListener(e -> {
            BookPageDisplay pageDisplay = new BookPageDisplay(parent, page.getBook().getPages().get(page.getPageNum()-2), bookPageHelper);
            ((Main) parent).changeScreen(pageDisplay.getPanel(), MessageFormat.format(Main.getString("bookPage_title"),page.getPageNum()-1));
        });

        nextPageButton.addActionListener(e -> {
            BookPageDisplay pageDisplay = new BookPageDisplay(parent, page.getBook().getPages().get(page.getPageNum()), bookPageHelper);
            ((Main) parent).changeScreen(pageDisplay.getPanel(), MessageFormat.format(Main.getString("bookPage_title"),(page.getPageNum()+1)));
        });
    }

    public BookPageDisplay(JFrame parent, BookPage page) {
        this(parent);

        setPage(page);
        titleLabel.setText(page.getBook().getTitle());
        String subtitle = MessageFormat.format(Main.getString("bookPage_title"),page.getPageNum());
        subtitleLabel.setText(subtitle);

        if(page.getPageNum() == 1) {
            prevPageButton.setEnabled(false);
        }
        if(page.getPageNum() == page.getBook().getPages().size()) {
            nextPageButton.setEnabled(false);
        }
    }

    public BookPageDisplay(JFrame parent, BookPage page, BookPageHelper helper) {
        this(parent, page);

        bookPageHelper = helper;
    }

    private void setPage(BookPage page) {
        this.page = page;

        displayPanel.removeAll();

        for(ArrayList<PageSlot> row : page.getRows()) {
            addRow(row);
        }
    }

    public BookPage getPage() {
        return page;
    }

    public void addRow(ArrayList<PageSlot> slots) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));

        // Show each slot
        for(PageSlot slot : slots) {
            PageSlotPanel rowPanel = new PageSlotPanel(slot.getSize(), slot.getLine1Text(), slot.getLine2Text());
            rowPanel.page = this;
            if(bookPageHelper == null)
                bookPageHelper = new BookPageHelper();
            rowPanel.setSlot(slot);
            slot.setBookPage(page);

            // Show which slots are full
            rowPanel.getRadioButton().setSelected(slot.isSlotFilled());

            row.add(rowPanel);
        }

        displayPanel.add(row);
    }
}