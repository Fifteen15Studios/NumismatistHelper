
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.prefs.Preferences;

@SuppressWarnings("unused")
public class Main extends JFrame {

    static final String DEFAULT_DATABASE_SERVER = DatabaseConnection.DEFAULT_DATABASE_SERVER;
    static final String DEFAULT_DATABASE_NAME = DatabaseConnection.DEFAULT_DATABASE_NAME;
    static final String DEFAULT_DATABASE_USERNAME = DatabaseConnection.DEFAULT_DATABASE_USERNAME;
    static final String DEFAULT_DATABASE_PASSWORD = DatabaseConnection.DEFAULT_DATABASE_PASSWORD;
    static final int DEFAULT_PORT_NUMBER = DatabaseConnection.DEFAULT_PORT_NUMBER;
    static final int DEFAULT_TIMEOUT_SECONDS = DatabaseConnection.DEFAULT_TIMEOUT_SECONDS;

    static final String SETTING_IMAGE_PATH = "imagesFolder";
    static final String SETTING_DATABASE_SERVER = "databaseServer";
    static final String SETTING_DATABASE_NAME = "databaseName";
    static final String SETTING_DATABASE_USERNAME = "databaseUsername";
    static final String SETTING_DATABASE_PASSWORD = "password";
    static final String SETTING_PORT_NUMBER = "portNumber";
    static final String SETTING_DB_TIMEOUT = "timeout";

    static final String SETTING_CUSTOM_COUNTRIES = "customCountryList";

    static final String SETTING_LOOK_AND_FEEL = "lookAndFeel";
    static final String SETTING_LAST_DIRECTORY = "lastDirectoryLocation";

    static final Color COLOR_SUCCESS = new Color(0, 100, 0);
    static final Color COLOR_WARNING = new Color(255, 100, 0);
    static final Color COLOR_ERROR = new Color(200, 0, 0);

    private JPanel contentPane;
    private JButton addCoinButton;
    private JButton addSetButton;
    private JButton addBillButton;
    private JButton addBookFolderButton;
    private JButton viewSpreadsheetButton;
    private JLabel titleLabel;
    private JButton addContainerButton;
    // TODO: Make these work
    private JButton addCountryButton;
    private JButton addCurrencyButton;

    NumismatistAPI api = new NumismatistAPI();

    ResizeListener resizeListener = null;

    private boolean firstOpen = true;

    public String getVersion() {
        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {

                Manifest manifest = new Manifest(resources.nextElement().openStream());
                String title = manifest.getMainAttributes().getValue("Implementation-Title");

                // check that this is the proper manifest
                if(title != null && title.equals("Numismatist Helper")) {
                    return manifest.getMainAttributes().getValue("Implementation-Version");
                }
            }
        }
        catch (IOException E) {
            // ignore
        }

        return "";
    }

    public static ImageIcon getIcon() {
        ImageIcon icon = null;
        try {
            // Get icon from file
            icon = new ImageIcon("icon.png");
            // If file doesn't exist, try to get from resources
            if (icon.getIconHeight() == -1)
                icon = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/Images/icon.png")));
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return icon;
    }

    public static void showBackgroundPopup(Component parent, String message, String title, SwingWorker worker) {

        final JOptionPane optionPane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);

        JDialog dialog = new JDialog();
        dialog.setTitle(title);

        // Set window icon to app icon
        dialog.setIconImage(getIcon().getImage());
        dialog.setContentPane(optionPane);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.pack();
        // Center in the window
        dialog.setLocationRelativeTo(parent);

        // Cancel the worker if dialog is close with X
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);

                worker.cancel(true);
            }
        });

        // Show the dialog when the work starts, and close it when the work ends
        worker.addPropertyChangeListener(evt -> {
            if(evt.getPropertyName().equals("state") &&
                    evt.getNewValue().toString().equals("STARTED")) {
                dialog.setModal(false);
                dialog.setVisible(true);
                parent.setEnabled(false);
            }
            if(evt.getPropertyName().equals("state") &&
                    evt.getNewValue().toString().equals("DONE")) {
                parent.setEnabled(true);
                dialog.dispose();
            }
        });
        try {
            worker.execute();
        }
        catch (Exception e) {
            dialog.dispose();
            throw e;
        }
    }

    public static ArrayList<String> getResourcesList(String directoryName) throws URISyntaxException, IOException {
        ArrayList<String> filenames = new ArrayList<>();

        directoryName = "res/" + directoryName;
        URL url = Thread.currentThread().getContextClassLoader().getResource(directoryName);
        if (url != null) {
            if (url.getProtocol().equals("file")) {
                File file = Paths.get(url.toURI()).toFile();
                File[] files = file.listFiles();
                if (files != null) {
                    for (File filename : files) {
                        filenames.add(filename.getName());
                    }
                }
            } else if (url.getProtocol().equals("jar")) {
                String dirname = directoryName + "/";
                String path = url.getPath();
                String jarPath = path.substring(5, path.indexOf("!"));
                try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8.name()))) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.startsWith(dirname) && !dirname.equals(name)) {
                            URL resource = Thread.currentThread().getContextClassLoader().getResource(name);
                            if(resource != null) {
                                String resString = resource.toString().substring(resource.toString().lastIndexOf("/") + 1);
                                filenames.add(resString);
                            }
                        }
                    }
                }
            }
        }
        return filenames;
    }

    public interface ResizeListener{
        void onResize(int width, int height);
    }

    /* Requires JRE 7, Java 1.7
    public static void copyFile(String from, String to) throws IOException{
        Path src = Paths.get(from);
        Path dest = Paths.get(to);
        Files.copy(src.toFile(), dest.toFile());
    }*/

    public static boolean copyFile(File src, String dest) {
        return copyFile(src, new File(dest));
    }

    public static boolean copyFile(String src, File dest) {
        return copyFile(new File(src), dest);
    }

    public static boolean copyFile(String src, String dest) {
        return copyFile(new File(src), new File(dest));
    }

    public static boolean copyFile(File src, File dest) {

        if(!src.exists())
            return false;

        // Create directories and file if it doesn't exist, if file already exists will do nothing
        try {
            if(dest.isFile()) {
                dest.getParentFile().mkdirs();
                dest.createNewFile();
            }
        }
        catch (IOException e) {
            return false;
        }

        // Copy the file
        try (InputStream is = new FileInputStream(src); OutputStream os = new FileOutputStream(dest)) {
            // buffer size 1K
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buf)) > 0) {
                os.write(buf, 0, bytesRead);
            }
        }
        catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * Takes a String and adds escape characters for anything that needs them
     *
     * @param value The original String that needs to be modified
     * @return The modified String
     */
    public static String escapeForJava(String value)
    {
        StringBuilder builder = new StringBuilder();

        for( char c : value.toCharArray() )
        {
            if( c == '\'' )
                builder.append( "\\'" );
            else if ( c == '\"' )
                builder.append( "\\\"" );
            else if( c == '\r' )
                builder.append( "\\r" );
            else if( c == '\n' )
                builder.append( "\\n" );
            else if( c == '\t' )
                builder.append( "\\t" );
            else if( c < 32 || c >= 127 )
                builder.append( String.format( "\\u%04x", (int)c ) );
            else
                builder.append( c );
        }
        return builder.toString();
    }

    public static void setSettingCustomCountries(ArrayList<String> customCountries) {
        String csv = String.join(",", customCountries);
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        prefs.put(SETTING_CUSTOM_COUNTRIES, csv);
    }

    public ArrayList<String> getSettingCustomCountries() {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);

        String csv = prefs.get(SETTING_CUSTOM_COUNTRIES, "");

        return new ArrayList<>(Arrays.asList(csv.split(",")));
    }

    public static void setSettingDatabasePassword(String password) {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        prefs.put(Main.SETTING_DATABASE_PASSWORD, password);
    }
    public static String getSettingDatabasePassword() {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        return prefs.get(SETTING_DATABASE_PASSWORD, DEFAULT_DATABASE_PASSWORD);
    }

    public static int getSettingPortNumber() {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        return prefs.getInt(SETTING_PORT_NUMBER, DEFAULT_PORT_NUMBER);
    }

    public static void setSettingPortNumber(int port) {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        prefs.putInt(SETTING_PORT_NUMBER, port);
    }

    public static void setSettingPortNumber(String port) {
        setSettingPortNumber(Integer.parseInt(port));
    }

    public static int getSettingDbTimeout() {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        return prefs.getInt(SETTING_DB_TIMEOUT, DEFAULT_TIMEOUT_SECONDS);
    }

    public static void setSettingDbTimeout(int timeout) {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        prefs.putInt(SETTING_DB_TIMEOUT, timeout);
    }

    public static String getSettingLookAndFeel() {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        return prefs.get(SETTING_LOOK_AND_FEEL, UIManager.getSystemLookAndFeelClassName());
    }

    public static String getSettingLastDirectory() {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        return prefs.get(SETTING_LAST_DIRECTORY, FileSystemView.getFileSystemView().getDefaultDirectory().getPath());
    }

    public static void setSettingLastDirectory(String path) {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        prefs.put(SETTING_LAST_DIRECTORY, path);
    }

    public static void setSettingImagePath(String path) {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        prefs.put(SETTING_IMAGE_PATH, path);
    }

    public static String getSettingImagePath() {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);

        return prefs.get(SETTING_IMAGE_PATH, getDefaultImagesLocation());
    }

    public static String getDefaultImagesLocation() {
        String os = System.getProperty("os.name");
        // Get home directory of user
        String imageDirectory = System.getProperty("user.home");

        if(os.toLowerCase().contains("windows"))
            imageDirectory += "\\My Pictures\\" + Main.getString("app_name");
        else if(os.toLowerCase().contains("linux"))
            imageDirectory += "/Pictures/" + Main.getString("app_name");

        return imageDirectory;
    }

    public static void setSettingDatabaseServer(String server) {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        prefs.put(Main.SETTING_DATABASE_SERVER, server);
    }

    public static String getSettingDatabaseServer() {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        return prefs.get(SETTING_DATABASE_SERVER, DEFAULT_DATABASE_SERVER);
    }

    public static void setSettingDatabaseName(String name) {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        prefs.put(Main.SETTING_DATABASE_NAME, name);
    }

    public static String getSettingDatabaseName() {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        return prefs.get(SETTING_DATABASE_NAME, DEFAULT_DATABASE_NAME);
    }

    public static void setSettingDatabaseUsername(String username) {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        prefs.put(Main.SETTING_DATABASE_USERNAME, username);
    }
    public static String getSettingDatabaseUsername() {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        return prefs.get(SETTING_DATABASE_USERNAME, DEFAULT_DATABASE_USERNAME);
    }

    public static void main(String[] args) {
        Main dialog = new Main();
        dialog.pack();

        // Center window in screen
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public Main() {
        setContentPane(contentPane);

        Locale.setDefault(new Locale("en", "US"));

        // set theme
        try {
            UIManager.setLookAndFeel(getSettingLookAndFeel());
            SwingUtilities.updateComponentTreeUI(SwingUtilities.windowForComponent(contentPane));
        }
        catch (Exception e) {
            //ignore
        }

        ImageIcon icon = getIcon();
        if(icon != null && icon.getImage() != null)
            setIconImage(icon.getImage());

        setMinimumSize(new Dimension(800,600));

        setTitle(getAppName());
        titleLabel.setText(getAppName());

        // Make title bold and larger
        Font titleFont = titleLabel.getFont();
        titleLabel.setFont(new Font(titleFont.getName(), Font.BOLD, 36));

        api.setTopOfCountriesList(getSettingCustomCountries());

        addMenu();

        addCoinButton.addActionListener(e -> showNewCoinWindow());
        addBillButton.addActionListener(e -> showNewBillWindow() );
        addSetButton.addActionListener( e -> showNewSetWindow());
        addBookFolderButton.addActionListener(e -> showNewBookWindow());

        addContainerButton.addActionListener(e -> {
            NewContainerDialog newContainerDialog = new NewContainerDialog(this);
            newContainerDialog.pack();
            newContainerDialog.setLocationRelativeTo(this);
            newContainerDialog.setVisible(true);
        });

        viewSpreadsheetButton.addActionListener( e-> {
            CollectionTableScreen collectionTableScreen = new CollectionTableScreen(this);
            changeScreen(collectionTableScreen.getPanel(), getString("viewColl_title"));
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
    }

    private void addMenu() {

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu  = new JMenu(getString("menu_file"));
        JMenu collectionMenu  = new JMenu(getString("menu_collection"));
        JMenu settingsMenu = new JMenu(getString("menu_settings"));
        JMenu viewMenu = new JMenu(getString("menu_view"));
        JMenu helpMenu = new JMenu(getString("menu_help"));

        setJMenuBar(menuBar);

        // Setup file menu
        JMenuItem home = new JMenuItem(getString("fileMenu_home"));
        home.addActionListener( e -> changeScreen(getPanel(), ""));
        fileMenu.add(home);
        fileMenu.addSeparator();
        JMenuItem exit = new JMenuItem(getString("fileMenu_exit"));
        exit.addActionListener( e -> onCancel());
        fileMenu.add(exit);

        // Setup collection menu
        JMenuItem newCoin = new JMenuItem(getString("collMenu_addCoin"));
        newCoin.addActionListener( e-> showNewCoinWindow());
        collectionMenu.add(newCoin);

        JMenuItem newBook = new JMenuItem(getString("collMenu_addCoinFolder"));
        newBook.addActionListener(e -> showNewBookWindow());
        //collectionMenu.add(newBook);

        JMenuItem newSet = new JMenuItem(getString("collMenu_addCoinSet"));
        newSet.addActionListener( e -> showNewSetWindow());
        collectionMenu.add(newSet);

        JMenuItem newBill = new JMenuItem(getString("collMenu_addBill"));
        newBill.addActionListener( e -> showNewBillWindow());
        collectionMenu.add(newBill);
        collectionMenu.addSeparator();

        JMenuItem newContainer = new JMenuItem(getString("collMenu_addContainer"));
        newContainer.addActionListener(e -> {
            NewContainerDialog newContainerDialog = new NewContainerDialog(this);
            newContainerDialog.pack();
            newContainerDialog.setLocationRelativeTo(this);
            newContainerDialog.setVisible(true);
        });
        collectionMenu.add(newContainer);
        collectionMenu.addSeparator();

        JMenuItem viewCollection = new JMenuItem(getString("collMenu_viewColl"));
        viewCollection.addActionListener( e -> {
            CollectionTableScreen collectionTableScreen = new CollectionTableScreen(this);
            changeScreen(collectionTableScreen.getPanel(), getString("viewColl_title"));
        });
        collectionMenu.add(viewCollection);

        JMenuItem itemLocations = new JMenuItem((getString("setMenu_itemLoc")));
        itemLocations.addActionListener( e-> {
            ItemLocationsWindow itemLocationsWindow = new ItemLocationsWindow(this);
            itemLocationsWindow.pack();
            // Center window in this window
            itemLocationsWindow.setLocationRelativeTo(this);
            itemLocationsWindow.setVisible(true);

            getPanel().invalidate();
        });

        JMenuItem customize = new JMenuItem((getString("setMenu_custom")));
        customize.addActionListener( e -> {
            DoubleListDialog customizeDialog = new DoubleListDialog(api.getCountries(), api.getTopOfCountriesList());
            customizeDialog.setSourceMethod("getName");
            customizeDialog.setSourceName(Main.getString("customizeCountries_label_available"));
            customizeDialog.setDestName(Main.getString("customizeCountries_label_keepOnTop"));
            customizeDialog.setTitle(Main.getString("customizeCountries_title"));
            customizeDialog.setModalityType(Dialog.DEFAULT_MODALITY_TYPE);

            customizeDialog.pack();
            customizeDialog.setLocationRelativeTo(this);
            customizeDialog.setVisible(true);

            // Runs when dialog closes
            if(!customizeDialog.cancelled) {
                // Get items in destination
                ArrayList<Object> objects = new ArrayList<>(Arrays.asList(customizeDialog.getDest()));
                // Remove randomly added empty String item
                objects.remove("");

                // Convert Objects to Strings
                ArrayList<String> strings = new ArrayList<>(objects.size());
                for (Object item : objects) {
                    strings.add(item.toString());
                }

                // Set new list and save it to settings
                api.setTopOfCountriesList(strings);
                setSettingCustomCountries(api.getTopOfCountriesList());
            }
        });

        settingsMenu.add(itemLocations);
        settingsMenu.add(customize);

        JMenuItem lookAndFeel = new JMenuItem((getString("viewMenu_look")));
        lookAndFeel.addActionListener( e-> {
            LookAndFeelWindow lookAndFeelWindow = new LookAndFeelWindow(this);
            lookAndFeelWindow.pack();
            // Center window in this window
            lookAndFeelWindow.setLocationRelativeTo(this);
            lookAndFeelWindow.setVisible(true);
        });
        viewMenu.add(lookAndFeel);

        JMenuItem help = new JMenuItem(getString("helpMenu_help"));
        help.addActionListener( e-> {
            HelpDialog helpDialog = new HelpDialog();
            helpDialog.pack();
            // Center in this window
            helpDialog.setLocationRelativeTo(this);
            helpDialog.setVisible(true);
        });

        JMenuItem about = new JMenuItem(getString("helpMenu_about"));
        about.addActionListener( e-> {
            AboutScreen aboutScreen = new AboutScreen(this);
            aboutScreen.pack();
            // Center in this window
            aboutScreen.setLocationRelativeTo(this);
            aboutScreen.setVisible(true);
        });
        helpMenu.add(help);
        helpMenu.add(about);

        menuBar.add(fileMenu);
        menuBar.add(collectionMenu);
        menuBar.add(settingsMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);

    }

    private void showNewCoinWindow() {
        AddCoinScreen newScreen  = new AddCoinScreen(this);
        changeScreen(newScreen.getPanel(), getString("addCoin_title_add"));
    }

    private void showNewBookWindow() {

        NewFolderDialog newFolderDialog = new NewFolderDialog(this);
        newFolderDialog.pack();
        newFolderDialog.setLocationRelativeTo(this);
        newFolderDialog.setVisible(true);
    }

    private void showNewBillWindow() {
        AddBillScreen newScreen  = new AddBillScreen(this);
        changeScreen(newScreen.getPanel(), Main.getString("addBill_title_add"));
    }

    private void showNewSetWindow() {
        AddSetScreen newScreen = new AddSetScreen(this);
        changeScreen(newScreen.getPanel(), Main.getString("addSet_title_add"));
    }

    private void onCancel() {
        dispose();

        // Cancels any background tasks and closes child windows
        System.exit(0);
    }

    public static String getAppName() {
        return getString("app_name", Locale.getDefault());
    }

    public static String getString(String stringName) {
        return getString(stringName, Locale.getDefault());
    }

    public static String getString(String stringName, Locale locale) {
        return ResourceBundle.getBundle("res.strings", locale).getString(stringName);
    }

    public void changeScreen(JPanel newPanel, String title) {
        setContentPane(newPanel);

        String newTitle = getAppName();

        if(!title.equals(""))
            newTitle += " - " + title;

        setTitle(newTitle);

        revalidate();
    }

    public JPanel getPanel() {
        return contentPane;
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);

        Main window = this;

        api.setImagePath(getSettingImagePath());

        // Connect to database in background
        if(firstOpen) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {

                boolean sqlE = false;
                boolean cnf = false;

                @Override
                public Void doInBackground() {

                    try {
                        window.api.setDbInfo(getSettingDatabaseServer(),
                                getSettingDatabaseName(),
                                getSettingPortNumber(),
                                getSettingDatabaseUsername(),
                                getSettingDatabasePassword());
                    } catch (SQLException sqlException) {
                        sqlE = true;
                    } catch (ClassNotFoundException cnfE) {
                        cnf = true;
                    }

                    return null;
                }

                @Override
                public void done() {
                    if (sqlE) {
                        JOptionPane.showMessageDialog(window,
                                Main.getString("itemLoc_error_message_databaseConnFail"),
                                Main.getString("itemLoc_error_title_databaseConnFail"),
                                JOptionPane.ERROR_MESSAGE);

                        ItemLocationsWindow itemLocationsWindow = new ItemLocationsWindow(window);
                        itemLocationsWindow.pack();
                        itemLocationsWindow.setLocationRelativeTo(window);
                        itemLocationsWindow.setVisible(true);
                    } else if (cnf) {
                        JOptionPane.showMessageDialog(window,
                                Main.getString("itemLoc_error_message_driverMissing"),
                                Main.getString("itemLoc_error_title_driverMissing"),
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            Main.showBackgroundPopup(this,
                    Main.getString("databaseQueryWindow_message"),
                    Main.getString("databaseQueryWindow_title"),
                    worker);

            firstOpen = false;
        }
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);

        if(this.resizeListener!= null){
            this.resizeListener.onResize(width, height);
        }
    }

    @Override
    public void setSize(Dimension d) {
        super.setSize(d);

        if(this.resizeListener!= null){
            this.resizeListener.onResize(d.width, d.height);
        }
    }
}
