
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class PremadeFolderDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JList list;

    private final JFrame parent;

    boolean cancelled = false;

    private final NumismatistAPI api;

    public PremadeFolderDialog(JFrame parent) {
        super(parent);
        api = ((Main) parent).api;
        setContentPane(contentPane);
        setModal(true);
        setTitle("Select a folder");
        setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
        getRootPane().setDefaultButton(buttonOK);
        setResizable(false);

        this.parent = parent;

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
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {

        String selected = NumismatistAPI.Companion.getResPath("xml", "books") + list.getSelectedValue().toString();

        BookPageDisplay pageDisplay =
            new BookPageDisplay(parent, api.importBook(selected).getPages().get(0));

        ((Main) parent).changeScreen(pageDisplay.getPanel(), "Page " + 1);

        // add your code here
        dispose();
    }

    private void onCancel() {
        cancelled = true;
        // add your code here if necessary
        dispose();
    }

    private void createUIComponents() {
        // Create list of files in custom books
        ArrayList<String> paths = new ArrayList<>();
        try {
            paths = Main.getResourcesList("xml/books");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        list = new JList(paths.toArray());
    }
}
