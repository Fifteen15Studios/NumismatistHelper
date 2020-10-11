import items.DatabaseConnection;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.prefs.Preferences;

public class ItemLocationsWindow extends JDialog {
    private JPanel contentPane;
    private JButton cancelButton;
    private JButton OKButton;
    private JTextField databaseServerInput;
    private JTextField imagesInput;
    private JTextField databaseNameInput;
    private JPasswordField passwordInput;
    private JButton browseButton;
    private JTextField databaseUsernameInput;
    private JButton setDefaultServerButton;
    private JButton setDefaultNameButton;
    private JButton setDefaultUsernameButton;
    private JButton setDefaultPasswordButton;
    private JButton setDefaultLocationButton;

    private final JFrame parent;

    private boolean passwordChanged = false;

    public ItemLocationsWindow(JFrame parent) {
        super(parent);
        setContentPane(contentPane);
        setModal(true);
        setMinimumSize(new Dimension(150,150));
        setResizable(false);
        getRootPane().setDefaultButton(OKButton);

        this.parent = parent;

        setTitle("Item Locations");

        databaseServerInput.setText(Main.getSettingDatabaseServer());
        databaseNameInput.setText(Main.getSettingDatabaseName());
        databaseUsernameInput.setText(Main.getSettingDatabaseUsername());
        imagesInput.setText(Main.getSettingImagePath());

        passwordInput.setText("12345678");
        passwordInput.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                passwordChanged = true;
            }
            public void removeUpdate(DocumentEvent e) {
                passwordChanged = true;
            }
            public void insertUpdate(DocumentEvent e) {
                passwordChanged = true;
            }
        });

        OKButton.addActionListener(e -> onOK());
        cancelButton.addActionListener(e -> onCancel());
        browseButton.addActionListener( e -> openFileChooser());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        setDefaultServerButton.addActionListener( e -> databaseServerInput.setText(Main.getSettingDatabaseServer()));
        setDefaultNameButton.addActionListener( e -> databaseNameInput.setText(Main.getSettingDatabaseName()));
        setDefaultUsernameButton.addActionListener( e -> databaseUsernameInput.setText(Main.getSettingDatabaseUsername()));
        setDefaultPasswordButton.addActionListener( e -> passwordInput.setText(Main.getSettingDatabasePassword()));
        setDefaultLocationButton.addActionListener( e -> imagesInput.setText(Main.getSettingImagePath()));
    }

    private void onOK() {

        Preferences prefs = Preferences.userNodeForPackage(Main.class);

        boolean error = false;

        String password;

        if(passwordChanged)
            password = String.valueOf(passwordInput.getPassword());
        else
            password = Main.getSettingDatabasePassword();

        DatabaseConnection testConnection = null;

        // Test connection
        try {

            testConnection = new DatabaseConnection(
                    databaseUsernameInput.getText(),
                    password,
                    databaseNameInput.getText(),
                    databaseServerInput.getText()
            );
        }
        // If connection failed
        catch (Exception e) {
            int result = JOptionPane.showConfirmDialog(parent, "Database connection failed. Continue anyway?",
                    "Connection Failed", JOptionPane.YES_NO_OPTION);

            if(result != JOptionPane.YES_OPTION)
                error = true;
        }

        File directory = new File(imagesInput.getText());

        // Check to make sure file directory exists
        if(directory.exists()) {
            prefs.put(Main.SETTING_IMAGE_PATH, imagesInput.getText());
        }
        else {
            // If directory doesn't exist, ask if we should create it
            int result = JOptionPane.showConfirmDialog(parent, "Images directory doesn't exist. Create it now?",
                    "Create Directory", JOptionPane.YES_NO_OPTION);

            // If we should create it
            if(result == JOptionPane.YES_OPTION) {

                boolean created = directory.mkdirs();

                if(!created)
                {
                    JOptionPane.showMessageDialog(parent, "Failed to create directory!",
                            "Error", JOptionPane.ERROR_MESSAGE);

                    error = true;
                }
            }
            else
                error = true;
        }

        // If everything is OK, save everything and close the window
        if(!error) {

            prefs.put(Main.SETTING_DATABASE_NAME, databaseNameInput.getText());
            prefs.put(Main.SETTING_DATABASE_SERVER, databaseServerInput.getText());
            prefs.put(Main.SETTING_DATABASE_USERNAME, databaseUsernameInput.getText());
            prefs.put(Main.SETTING_IMAGE_PATH, imagesInput.getText());
            prefs.put(Main.SETTING_DATABASE_PASSWORD, password);

            ((Main)parent).databaseConnection = testConnection;

            dispose();
        }
    }

    void openFileChooser() {
        final JFileChooser fc = new JFileChooser();

        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnVal = fc.showOpenDialog(parent);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            imagesInput.setText(file.getAbsolutePath());
        }
    }

    private void onCancel() {
        dispose();
    }
}
