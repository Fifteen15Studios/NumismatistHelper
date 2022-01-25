
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

@SuppressWarnings("rawtypes")
public class HelpDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JList topicsList;
    private JTextPane textArea;
    private JScrollPane infoScrollPane;

    private final JFrame parent;

    @SuppressWarnings("unchecked")
    public HelpDialog(JFrame parent) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle(Main.getString("helpScreen_title"));

        this.parent = parent;

        setIconImage(Main.getIcon().getImage());

        buttonOK.addActionListener(e -> onOK());
        textArea.setContentType("text/html");
        infoScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

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

            int index = topicsList.getSelectedIndex();
            Parser parser = Parser.builder().build();
            Node document;

            switch (index) {
                case 0: {
                    document = parser.parse(Main.getString("helpScreen_text_coins"));
                    break;
                }
                case 1: {
                    // TODO: Update text when folders are finished
                    document = parser.parse(Main.getString("helpScreen_text_folders"));
                    break;
                }
                case 2: {
                    document = parser.parse(Main.getString("helpScreen_text_banknotes"));
                    break;
                }
                case 3: {
                    document = parser.parse(Main.getString("helpScreen_text_sets"));
                    break;
                }
                case 4: {
                    document = parser.parse(Main.getString("helpScreen_text_containers"));
                    break;
                }
                case 5: {
                    document = parser.parse(Main.getString("helpScreen_text_countries"));
                    break;
                }
                case 6: {
                    document = parser.parse(Main.getString("helpScreen_text_currencies"));
                    break;
                }
                default: {
                    document = parser.parse("");
                    break;
                }
            }

            HtmlRenderer renderer = HtmlRenderer.builder().build();
            textArea.setText(renderer.render(document));

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

    @Override
    public void setVisible(boolean b) {

        setSize(new Dimension(500,400));
        setMinimumSize(new Dimension(500,400));

        setLocationRelativeTo(parent);

        super.setVisible(b);
    }
}
