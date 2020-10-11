
import items.Coin;
import items.CoinSet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class ExistingCoinDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable coinTable;

    private boolean cancelled = false;
    private final ArrayList<Coin> coins;

    public ExistingCoinDialog(JFrame parent, CoinSet set) {
        super(parent);
        setContentPane(contentPane);
        setModal(true);
        setTitle("Add coin to set");
        getRootPane().setDefaultButton(buttonOK);
        setMinimumSize(new Dimension(400,300));

        String[] columnNames = {"ID",
                "Coin",
                "Country",
                "Grade",
                "Error",
                "Note",};

        coins = Coin.Companion.getCoinsFromSql(((Main)parent).databaseConnection, "");

        // Show coins
        Object[][] data = new Object[coins.size()][columnNames.length];

        int duplicates = 0;

        int count = 0;
        // for each coin found
        for (Coin coin : coins) {

            boolean duplicate = false;
            // For each column
            for(int j = 0; j < columnNames.length; j++) {
                String columnData = "";
                switch (j) {
                    case 0 -> {
                        // Find coins that have already been added to the set
                        int id = coin.getId();
                        for (int c = 0; c < set.getCoins().size(); c++) {
                            // If this is a duplicate, don't add it
                            if (set.getCoins().get(c).getId() == id) {
                                duplicate = true;
                                duplicates++;
                            }
                        }
                        columnData += coin.getId();
                    }
                    case 1 -> columnData = coin.toString();
                    case 2 -> columnData = coin.getCountry();
                    case 3 -> columnData = coin.getCondition();
                    case 4 -> columnData = coin.getErrorType();
                    case 5 -> columnData = coin.getNote();
                }
                if(!duplicate)
                    data[count-duplicates][j] = columnData;
            }
            count++;
        }

        // Set data for table
        DefaultTableModel dataModel = new DefaultTableModel();
        dataModel.setDataVector(data, columnNames);
        coinTable.setModel(dataModel);

        // Remove empty rows, created due to already adding an existing coin
        for(int i = 0; i < duplicates; i++)
            ((DefaultTableModel)coinTable.getModel()).removeRow(coinTable.getModel().getRowCount()-1);

        // Make table non-editable, and single selection
        coinTable.setDefaultEditor(Object.class, null);
        coinTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        coinTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    onOK();
                }
            }
        });

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e ->
                onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    Coin showDialog() {
        setVisible(true);
        if(!cancelled) {
            Object idString = coinTable.getValueAt(coinTable.getSelectedRow(), 0);
            // If didn't select empty row
            if(idString != null) {
                int id = Integer.parseInt(idString.toString());
                for (Coin coin : coins)
                    if (coin.getId() == id)
                        return coin;
            }
        }
        return null;
    }

    private void onCancel() {
        // add your code here if necessary
        cancelled = true;
        dispose();
    }
}
