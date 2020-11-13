import items.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.SQLException;
import java.util.prefs.Preferences;

public class Main extends JFrame {

    static String app_name = "Coin Collection";
    static String version = "1.0";

    static final String DEFAULT_DATABASE_SERVER = "localhost";
    static final String DEFAULT_DATABASE_NAME = "CoinCollection";
    static final String DEFAULT_DATABASE_USERNAME = "coins";
    static final String DEFAULT_DATABASE_PASSWORD = "coinDatabasePassword";

    static final String SETTING_IMAGE_PATH = "imagesFolder";
    static final String SETTING_DATABASE_SERVER = "databaseServer";
    static final String SETTING_DATABASE_NAME = "databaseName";
    static final String SETTING_DATABASE_USERNAME = "databaseUsername";
    static final String SETTING_DATABASE_PASSWORD = "password";

    private JPanel contentPane;
    private JButton addCoinButton;
    private JButton addSetButton;
    private JButton addBillButton;
    private JButton viewCollectionTreeButton;
    private JButton viewSpreadsheetButton;
    private JButton viewTotalValuesButton;

    DatabaseConnection databaseConnection;

    ResizeListener resizeListener = null;

    public interface ResizeListener{
        void onResize(int width, int height);
    }

    public static boolean copyFile(File scr, String dest) {
        return copyFile(scr, new File(dest));
    }

    public static boolean copyFile(String scr, File dest) {
        return copyFile(new File(scr), dest);
    }

    public static boolean copyFile(String scr, String dest) {
        return copyFile(new File(scr), new File(dest));
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

    /* Requires JRE 7, Java 1.7
    public static void copyFile(String from, String to) throws IOException{
        Path src = Paths.get(from);
        Path dest = Paths.get(to);
        Files.copy(src.toFile(), dest.toFile());
    }*/

    public static String getSettingDatabasePassword() {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        return prefs.get(SETTING_DATABASE_PASSWORD, DEFAULT_DATABASE_PASSWORD);
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
            imageDirectory += "\\My Pictures\\Coin Collection";
        else if(os.toLowerCase().contains("linux"))
            imageDirectory += "/Pictures/CoinCollection";

        return imageDirectory;
    }

    public static String getSettingDatabaseServer() {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        return prefs.get(SETTING_DATABASE_SERVER, DEFAULT_DATABASE_SERVER);
    }

    public static String getSettingDatabaseName() {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        return prefs.get(SETTING_DATABASE_NAME, DEFAULT_DATABASE_NAME);
    }

    public static String getSettingDatabaseUsername() {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        return prefs.get(SETTING_DATABASE_USERNAME, DEFAULT_DATABASE_USERNAME);
    }

    public static void main(String[] args) {
        Main dialog = new Main();
        dialog.pack();
        dialog.setVisible(true);
    }

    public Main() {
        setContentPane(contentPane);

        try {
            // Get icon from file
            ImageIcon icon = new ImageIcon("icon.png");
            // If file doesn't exist, try to get from resources
            if (icon.getIconHeight() == -1)
                icon = new ImageIcon(getClass().getResource("/Images/icon.png"));
            setIconImage(icon.getImage());
        }
        catch(Exception e){
            e.printStackTrace();
        }

        setMinimumSize(new Dimension(800,600));

        setTitle(app_name);

        addMenu();

        addCoinButton.addActionListener(e -> showNewCoinWindow());

        addBillButton.addActionListener(e -> showNewBillWindow() );

        addSetButton.addActionListener( e -> showNewSetWindow());

        viewSpreadsheetButton.addActionListener( e-> {
            CollectionTableScreen collectionTableScreen = new CollectionTableScreen(this);
            changeScreen(collectionTableScreen.getPanel(), "Collection");
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // Hide these buttons until they are ready
        viewCollectionTreeButton.setVisible(false);
        viewTotalValuesButton.setVisible(false);

        // Close the database connection when program closes
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            try {
                    databaseConnection.getStatement().close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }, "Shutdown-thread"));
    }

    private void addMenu() {

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu  = new JMenu("File");
        JMenu settingsMenu = new JMenu("Settings");
        JMenu helpMenu = new JMenu("Help");

        setJMenuBar(menuBar);

        // Setup file menu
        JMenuItem newCoin = new JMenuItem("New Coin");
        newCoin.addActionListener( e-> showNewCoinWindow());
        fileMenu.add(newCoin);
        JMenuItem newSet = new JMenuItem("New Set");
        newSet.addActionListener( e -> showNewSetWindow());
        fileMenu.add(newSet);
        fileMenu.addSeparator();
        JMenuItem newBill = new JMenuItem("New Bill");
        newBill.addActionListener( e -> showNewBillWindow());
        fileMenu.add(newBill);
        fileMenu.addSeparator();
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener( e -> onCancel());
        fileMenu.add(exit);

        JMenuItem itemLocations = new JMenuItem(("Item Locations"));
        itemLocations.addActionListener( e-> {
            ItemLocationsWindow itemLocationsWindow = new ItemLocationsWindow(this);
            itemLocationsWindow.pack();
            itemLocationsWindow.setVisible(true);
        });

        settingsMenu.add(itemLocations);

        JMenuItem about = new JMenuItem("About");
        about.addActionListener( e-> {
            AboutScreen aboutScreen = new AboutScreen(this);
            aboutScreen.pack();
            aboutScreen.setVisible(true);
        });
        helpMenu.add(about);

        menuBar.add(fileMenu);
        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);

    }

    private void showNewCoinWindow() {
        AddCoinScreen newScreen  = new AddCoinScreen(this);
        changeScreen(newScreen.getPanel(), "Add Coin");
    }

    private void showNewBillWindow() {
        AddBillScreen newScreen  = new AddBillScreen(this);
        changeScreen(newScreen.getPanel(), "Add Bill");
    }

    private void showNewSetWindow() {
        AddSetScreen newScreen = new AddSetScreen(this);
        changeScreen(newScreen.getPanel(), "Add Set");
    }

    private void onCancel() {
        dispose();
    }

    public static String getAppName() {
        return app_name;
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

        // Setup database connection
        try {
            databaseConnection = new DatabaseConnection(Main.getSettingDatabaseUsername(),
                    Main.getSettingDatabasePassword(),
                    Main.getSettingDatabaseName(),
                    Main.getSettingDatabaseServer());
        }
        catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this,
                    "SQL driver not found. Please load mysql-connector-java-*.jar into your project - " +
                            "where * is the latest version.",
                    "SQL Driver Missing",
                    JOptionPane.ERROR_MESSAGE);
        }
        // Show settings window if connection was unsuccessful
        catch (SQLException ex) {

            JOptionPane.showMessageDialog(this,
                    "Error connecting to database. Please check your settings.",
                    " Connection Error",
                    JOptionPane.ERROR_MESSAGE);

            ItemLocationsWindow itemLocationsWindow = new ItemLocationsWindow(this);
            itemLocationsWindow.pack();
            itemLocationsWindow.setVisible(true);super.setVisible(b);
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
