import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BillInfoDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JTextPane topTextArea;
    private JTextPane plateSeriesExplanation;
    private JTextPane districtExplanation;
    private JTextPane notePositionDescription;
    private JScrollPane scrollPane;
    private JLabel districtImageLabel;
    private JLabel plateSeriesObvImageLabel;
    private JLabel notePositionImageLabel;
    private JLabel plateSeriesRevImageLabel;

    public BillInfoDialog(JFrame parent) {
        super(parent);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setMinimumSize(new Dimension(400, 300));
        setTitle("Bill Information");
        setResizable(false);

        // close on ESCAPE
        contentPane.registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        topTextArea.setText("Dollar bills have a lot of information on them. " +
                "Some of this information is useful for determining the value of a bill.");

        districtExplanation.setText("The district is probably the most important of these items for " +
                "determining the value of a bill. The district identifies where the note was printed. " +
                "Districts are identified by both a letter and a number." +
                "Only the letter OR the number is required to identify a district.");

        plateSeriesExplanation.setText("The plate series identifies engraving plate was used to strike " +
                "this specific bill. There is a plate series printed on the front and the back of the " +
                "bill. These number will be different, as different plates are used to press each side. " +
                "The number is located in the bottom right of the bill on both sides.");

        notePositionDescription.setText("The note position can be found in the upper left of the bill. " +
                "It identifies where on the plate this bill was struck. It is a letter followed by a " +
                "number. Each plate is split into quadrants, identified by the number, and each " +
                "quadrant has multiple locations for bills to be struck, identified by the letter. " +
                "This letter-number combination can identify exactly where on the plate the bill " +
                "was located.");

        String os = System.getProperty("os.name");
        String slash;
        if (os.toLowerCase().contains("windows"))
            slash = "\\";
        else
            slash = "/";

        // Add images
        try {
            BufferedImage image = ImageIO.read(new File(
                    "." + slash + "images" + slash + "District.jpg"));

            districtImageLabel.setIcon(new ImageIcon(scaleImage(image)));
        }
        catch (IOException ignore) {
        }

        try {
            BufferedImage image = ImageIO.read(new File(
                    "." + slash + "images" + slash + "Note_Position.jpg"));

            notePositionImageLabel.setIcon(new ImageIcon(scaleImage(image)));
        }
        catch (IOException ignore) {
        }

        try {
            BufferedImage image = ImageIO.read(new File(
                    "." + slash + "images" + slash + "Plate_Series_obv.jpg"));

            plateSeriesObvImageLabel.setIcon(new ImageIcon(scaleImage(image)));
        }
        catch (IOException ignore) {
        }

        try {
            BufferedImage image = ImageIO.read(new File(
                    "." + slash + "images" + slash + "Plate_Series_rev.jpg"));

            plateSeriesRevImageLabel.setIcon(new ImageIcon(scaleImage(image)));
        }
        catch (IOException ignore) {
        }

        // Scroll to the top, since for some reason it scrolls down as things are added.
        javax.swing.SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));

        buttonOK.addActionListener(e -> onOK());
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private Image scaleImage(BufferedImage img) {
        double scaleFactor = (float)355 / img.getWidth(this);

        int newHeight = (int)(img.getHeight(this) * scaleFactor);
        int newWidth = (int)(img.getWidth(this) * scaleFactor);

        Image scaled;
        // Only scale image if it's larger than we want
        if(scaleFactor < 1) {
            // Scale to new size
            scaled = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        }
        else
            scaled = img;

        return scaled;
    }
}
