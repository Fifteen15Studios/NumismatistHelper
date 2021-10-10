import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.SQLException;
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
    private JButton setDefaultPortButton;
    private JSpinner portNumberInput;
    private JSpinner timeoutInput;
    private JButton setDefaultTimeoutButton;
    private JButton timeoutHelpButton;

    private final JFrame parent;

    private boolean passwordChanged = false;

    public ItemLocationsWindow(JFrame parent) {
        super(parent);
        setContentPane(contentPane);
        setModal(true);
        setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
        setResizable(false);
        getRootPane().setDefaultButton(OKButton);
        setIconImage(Main.getIcon().getImage());

        this.parent = parent;
        timeoutHelpButton.setBorder(null);


        setTitle(Main.getString("itemLoc_title"));

        databaseServerInput.setText(Main.getSettingDatabaseServer());
        databaseNameInput.setText(Main.getSettingDatabaseName());
        databaseUsernameInput.setText(Main.getSettingDatabaseUsername());
        imagesInput.setText(Main.getSettingImagePath());
        SpinnerModel portModel =
                new SpinnerNumberModel(Main.getSettingPortNumber(), //initial value
                        0, //min
                        65535, //max
                        1); //step
        portNumberInput.setModel(portModel);
        // Remove comma (,) from numbers (ex: 3306 instead of 3,306)
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(portNumberInput, "#");
        portNumberInput.setEditor(editor);
        SpinnerModel timeoutModel =
                new SpinnerNumberModel(Main.getSettingDbTimeout(), //initial value
                        0, //min
                        Integer.MAX_VALUE, //max
                        1); //step
        timeoutInput.setModel(timeoutModel);

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

        setDefaultServerButton.addActionListener( e -> databaseServerInput.setText(Main.DEFAULT_DATABASE_SERVER));
        setDefaultNameButton.addActionListener( e -> databaseNameInput.setText(Main.DEFAULT_DATABASE_NAME));
        setDefaultUsernameButton.addActionListener( e -> databaseUsernameInput.setText(Main.DEFAULT_DATABASE_USERNAME));
        setDefaultPasswordButton.addActionListener( e -> passwordInput.setText(Main.DEFAULT_DATABASE_PASSWORD));
        setDefaultLocationButton.addActionListener( e -> imagesInput.setText(Main.getDefaultImagesLocation()));
        setDefaultPortButton.addActionListener( e -> portNumberInput.setValue(Main.DEFAULT_PORT_NUMBER));
        setDefaultTimeoutButton.addActionListener( e -> timeoutInput.setValue(Main.DEFAULT_TIMEOUT_SECONDS));

        // Change initial focus from Default server button to server address entry
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                super.windowOpened(e);
                databaseServerInput.requestFocusInWindow();
            }
        });
    }

    private void onOK() {

        Preferences prefs = Preferences.userNodeForPackage(Main.class);

        boolean error = false;

        String password;

        if(passwordChanged)
            password = String.valueOf(passwordInput.getPassword());
        else
            password = Main.getSettingDatabasePassword();

        File directory = new File(imagesInput.getText());

        // Check to make sure file directory exists
        if(directory.exists()) {
            prefs.put(Main.SETTING_IMAGE_PATH, imagesInput.getText());
        }
        else {
            // If directory doesn't exist, ask if we should create it
            int result = JOptionPane.showConfirmDialog(parent,
                    Main.getString("itemLoc_error_message_imagesDirectoryMissing"),
                    Main.getString("itemLoc_error_title_imagesDirectoryMissing"),
                    JOptionPane.YES_NO_OPTION);

            // If we should create it
            if(result == JOptionPane.YES_OPTION) {

                boolean created = directory.mkdirs();

                if(!created)
                {
                    JOptionPane.showMessageDialog(parent,
                            Main.getString("itemLoc_error_message_directoryCreateFail"),
                            Main.getString("itemLoc_error_title_directoryCreateFail"),
                            JOptionPane.ERROR_MESSAGE);

                    error = true;
                }
            }
            else
                error = true;
        }

        // If everything is OK, save everything and close the window
        if(!error) {

            ItemLocationsWindow window = this;

            ((Main) parent).api.setConnectionTimeout(Integer.parseInt(timeoutInput.getValue().toString()));
            ((Main) parent).api.setImagePath(imagesInput.getText());

            SwingWorker<Void, Void> worker = new SwingWorker<>() {

                boolean sqlE = false;
                boolean cnf = false;

                @Override
                public Void doInBackground() {
                    try {
                        ((Main) parent).api.setDbInfo(databaseServerInput.getText(),
                                databaseNameInput.getText(),
                                portNumberInput.getValue().toString(),
                                databaseUsernameInput.getText(),
                                password);
                    }
                    catch (SQLException sqlException) {
                        sqlE = true;
                    }
                    catch (ClassNotFoundException cnfE) {
                        cnf = true;
                    }

                    return null;
                }

                @Override
                public void done() {
                    if(sqlE) {

                        JOptionPane.showMessageDialog(window,
                                Main.getString("itemLoc_error_message_databaseConnFail"),
                                Main.getString("itemLoc_error_title_databaseConnFail"),
                                JOptionPane.ERROR_MESSAGE);
                    }
                    else if(cnf) {
                        JOptionPane.showMessageDialog(window,
                                Main.getString("itemLoc_error_message_driverMissing"),
                                Main.getString("itemLoc_error_title_driverMissing"),
                                JOptionPane.ERROR_MESSAGE);
                    }
                    else {
                        Main.setSettingDatabaseName(databaseNameInput.getText());
                        Main.setSettingDatabaseServer(databaseServerInput.getText());
                        Main.setSettingDatabaseUsername(databaseUsernameInput.getText());
                        Main.setSettingImagePath(imagesInput.getText());
                        Main.setSettingDatabasePassword(password);
                        Main.setSettingPortNumber((int) portNumberInput.getValue());

                        Main.setSettingDbTimeout((int)timeoutInput.getValue());

                        dispose();
                    }
                }
            };

            Main.showBackgroundPopup(contentPane, Main.getString("databaseQueryWindow_message"), Main.getString("databaseQueryWindow_title"), worker);
        }
    }

    void openFileChooser() {
        final JFileChooser fc = new JFileChooser();

        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // Default to directory in text box
        if(imagesInput.getText() != null && !imagesInput.getText().equals(""))
            fc.setCurrentDirectory(new File(imagesInput.getText()));

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
