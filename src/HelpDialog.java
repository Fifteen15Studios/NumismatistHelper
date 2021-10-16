import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

@SuppressWarnings("rawtypes")
public class HelpDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JList topicsList;
    private JTextArea textArea;
    private JScrollPane infoScrollPane;

    @SuppressWarnings("unchecked")
    public HelpDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle(Main.getString("helpScreen_title"));
        setSize(new Dimension(500,400));
        setMinimumSize(new Dimension(500,400));

        setIconImage(Main.getIcon().getImage());

        buttonOK.addActionListener(e -> onOK());

        DefaultListModel topics = new DefaultListModel();

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
                    textArea.setText(Main.getString("helpScreen_text_coins"));
                    break;
                }
                case 1: {
                    // TODO: Update text when folders are finished
                    textArea.setText(Main.getString("helpScreen_text_folders"));
                    break;
                }
                case 2: {
                    textArea.setText(Main.getString("helpScreen_text_banknotes"));
                    break;
                }
                case 3: {
                    textArea.setText(Main.getString("helpScreen_text_sets"));
                    break;
                }
                case 4: {
                    textArea.setText(Main.getString("helpScreen_text_containers"));
                    break;
                }
                case 5: {
                    textArea.setText(Main.getString("helpScreen_text_countries"));
                    break;
                }
                case 6: {
                    textArea.setText(Main.getString("helpScreen_text_currencies"));
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
