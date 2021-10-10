import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Calendar;

public class AboutScreen extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JTextPane descriptionPane;
    private JLabel versionLabel;
    private JLabel buildLabel;
    private JLabel titleDisplay;

    public AboutScreen(JFrame parent) {
        super(parent);
        setContentPane(contentPane);
        setModal(true);
        setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
        setMinimumSize(new Dimension(400, 300));
        setTitle(Main.getString("aboutScreen_title"));
        setResizable(false);
        getRootPane().setDefaultButton(buttonOK);
        setIconImage(Main.getIcon().getImage());

        // close on ESCAPE
        contentPane.registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        buttonOK.addActionListener(e -> onOK());

        String buildDate = "";
        Font titleFont = titleDisplay.getFont();
        titleDisplay.setFont(new Font(titleFont.getName(), Font.BOLD, titleFont.getSize()));

        try {
            File jarFile = new File
                    (this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            DateFormat dateFormat = DateFormat.getDateInstance();
            buildDate += dateFormat.format(jarFile.lastModified());
        } catch (URISyntaxException e1) {
            //ignore
        }

        int year = Calendar.getInstance().get(Calendar.YEAR);

        descriptionPane.setText(MessageFormat.format(Main.getString("aboutScreen_message"), "" + year));

        String version = MessageFormat.format(Main.getString("aboutScreen_label_version"), ((Main) parent).getVersion());
        versionLabel.setText(version);

        if(!buildDate.isBlank()) {
            String buildString = MessageFormat.format(Main.getString("aboutScreen_label_build"), buildDate);
            buildLabel.setText(buildString);
        }
    }

    private void onOK() {
        dispose();
    }
}
