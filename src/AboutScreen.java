import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class AboutScreen extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JTextPane descriptionPane;
    private JLabel versionLabel;

    public AboutScreen(JFrame parent) {
        super(parent);
        setContentPane(contentPane);
        setModal(true);
        setMinimumSize(new Dimension(400, 300));
        setTitle("About");
        setResizable(false);
        getRootPane().setDefaultButton(buttonOK);

        // close on ESCAPE
        contentPane.registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        buttonOK.addActionListener(e -> onOK());

        descriptionPane.setText("This program was designed to document and track a collection of coins, " +
                "coin sets, and paper money.\n" +
                "\nDesigned and written by Randy Havens of Fifteen 15 Studios.\n" +
                "\nCopyright 2020");

        versionLabel.setText("Version: " + Main.version);
    }

    private void onOK() {
        dispose();
    }
}
