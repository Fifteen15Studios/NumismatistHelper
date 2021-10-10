import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// TODO: Translate this file
public class HelpDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JList topicsList;
    private JTextArea textArea;
    private JScrollPane infoScrollPane;

    private DefaultListModel topics = new DefaultListModel();

    public HelpDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle(Main.getString("helpScreen_title"));
        setSize(new Dimension(500,400));
        setMinimumSize(new Dimension(500,400));

        setIconImage(Main.getIcon().getImage());

        buttonOK.addActionListener(e -> onOK());

        topics.addElement(Main.getString("helpScreen_topic_coins"));
        topics.addElement(Main.getString("helpScreen_topic_coinFolders"));
        topics.addElement(Main.getString("helpScreen_topic_bills"));
        topics.addElement(Main.getString("helpScreen_topic_sets"));
        topics.addElement(Main.getString("helpScreen_topic_containers"));
        topics.addElement(Main.getString("helpScreen_topic_countries"));
        topics.addElement(Main.getString("helpScreen_topic_currencies"));

        topicsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        topicsList.setLayoutOrientation(JList.VERTICAL);
        topicsList.setListData(topics.toArray());

        topicsList.addListSelectionListener(e -> {

            // TODO: Use markdown
            int index = topicsList.getSelectedIndex();

            switch (index) {
                case 0: {
                    textArea.setText("Coins are one of the main focus of this program. When entering a new coin required fields include Country, Year, Currency, and Denomination. " +
                            "Non-required fields can simply be left blank.\n\n" +
                            "Zero (0) is a legal entry for both year and denomination. This may be used if the item being entered is a token, and therefore doesn't have " +
                            "a year or a denomination.\n\n" +
                            "Denomination is the face value of the coin. It can be as small as 0 and can be up to 20 digits long (including decimal places.) " +
                            "It can also hold fractional numbers up to 3 decimal places. For example, a half cent piece has a denomination of 0.005.\n\n" +
                            "Coins can be associated with up to 2 images. One image for the front (obverse) of the coin, and one for the back (reverse) of the coin.\n\n" +
                            "Coins can be placed in sets or in folders. Coins that are in sets or folders do not appears in the list of coins.\n\n" +
                            "Coins can also be placed inside of a container.");
                    break;
                }
                case 1: {
                    // TODO: Update when folders are finished
                    textArea.setText("Coin folders are used to hold specific coins. Generally these folders are used to hold a specific type of coin " +
                            "and collect every date and mint mark for that coin type.\n\n" +
                            "Folders can have multiple pages, and each page has multiple slots, each of which holds a single coin.\n\n" +
                            "Coins that are inside of a coin folder will not show in the coins tab of the collection spreadsheet.\n\n" +
                            "Coin Folders can be placed inside of a container.\n\n" +
                            "Coin folders are still a work in progress, and will be added at a later date. More information will also be added at the time.");
                    break;
                }
                case 2: {
                    textArea.setText("Banknotes are paper money. When entering a new banknote required fields include Country, Year, Currency, and Denomination. " +
                            "Non-required fields can simply be left blank.\n\n" +
                            "Countries and Currencies are pulled from a list in the database, and currencies only appear " +
                            "as available in countries where they were used, during years while they were used in that country.\n\n" +
                            "Denomination can be as low as 0, and can be up to 20 digits long (including decimal places.) It can also hold fractional numbers up to 3 decimal places.\n\n" +
                            "Banknotes can be associated with up to 2 images. One image for the front (obverse), and one for the back (reverse).\n\n" +
                            "Banknotes can be put inside of a container.");
                    break;
                }
                case 3: {
                    textArea.setText("Sets can hold coins, banknotes, and other sets.\n\n" +
                            "One reason to have a set inside of a set is if the set has multiple slabs and you want to have separate pictures of each slab.\n\n" +
                            "Like coins and banknotes, sets can also be associated with up to 2 images. One image for the front (obverse), and one for the back (reverse).\n\n" +
                            "Sets can also be placed inside of a container.");
                    break;
                }
                case 4: {
                    textArea.setText("Containers are physical items that hold your collection. This can be a box, a display case, a building, a room... whatever " +
                            "you choose to use as a way to represent where the items in your collection reside.\n\n" +
                            "The point of containers is to allow you to keep track of not only what is in your collection, but where each piece of your collection is.\n\n" +
                            "Containers can be placed inside of another container, in a parent/child relationship. The child resides inside of the parent. This is useful " +
                            "for when you have a box in a box, or a chest of drawers with multiple drawers. The parent can be the chest, and each drawer can be a child.\n\n" +
                            "Containers can hold a variety of items like coins, banknotes, sets, coin folders, and other containers.");
                    break;
                }
                case 5: {
                    textArea.setText("Countries only have a name, and a list of currencies. When you select a country, the currency dropdown will fill in with " +
                            "possible currencies for that country. When you enter a year, the currency list will narrow further to the currencies used during that year.");
                    break;
                }
                case 6: {
                    textArea.setText("Currencies have a name, a unique abbreviation for that currency, a symbol (ex: $), an indication of whether the currency should " +
                            "be placed before or after the numbers, a start date, and an end date.\n\n" +
                            "The start and end dates are used to select the proper currency when a country and year is selected.\n\n" +
                            "A currency is also associated with a country, or list of countries.");
                    break;
                }
            }

            // Scroll to top of selected area
            javax.swing.SwingUtilities.invokeLater(() -> infoScrollPane.getVerticalScrollBar().setValue(0));
        });

        topicsList.setSelectedIndex(0);

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onOK();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onOK(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        dispose();
    }
}
