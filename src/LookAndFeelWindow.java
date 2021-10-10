import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.prefs.Preferences;

public class LookAndFeelWindow extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JComboBox themeComboBox;

    private final JFrame parent;

    LookAndFeel startingLook;
    UIManager.LookAndFeelInfo[] looks;

    public LookAndFeelWindow(JFrame parent) {
        super((parent));
        setContentPane(contentPane);
        setModal(true);
        this.parent = parent;
        setMinimumSize(new Dimension(250,100));
        getRootPane().setDefaultButton(buttonOK);
        startingLook = UIManager.getLookAndFeel();

        setTitle("Look and Feel");

        // Find available looks
         looks = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo look : looks) {
            themeComboBox.addItem(look.getName());
        }

        themeComboBox.setSelectedItem(startingLook.getName());

        Window window = this;

        themeComboBox.addActionListener(e -> {
            String className = looks[themeComboBox.getSelectedIndex()].getClassName();
            try {
                UIManager.setLookAndFeel(className);
                SwingUtilities.updateComponentTreeUI(window);
                window.pack();
                SwingUtilities.updateComponentTreeUI(parent);
                parent.pack();
                parent.invalidate();

            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
            }
        });

        buttonOK.addActionListener(e -> onOK());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onOK();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onOK(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {

        String className = looks[themeComboBox.getSelectedIndex()].getClassName();

        if(startingLook.equals(themeComboBox.getSelectedItem())) {
            try {
                UIManager.setLookAndFeel(className);
                SwingUtilities.updateComponentTreeUI(SwingUtilities.windowForComponent(parent));
                parent.pack();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        prefs.put(Main.SETTING_LOOK_AND_FEEL, className);

        dispose();
    }
}
