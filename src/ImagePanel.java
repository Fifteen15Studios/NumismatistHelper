import javax.swing.*;
import java.awt.*;

class ImagePanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private Image img;
    private Image scaled;

    public void setImg(String img) {
        this.img = new ImageIcon(img).getImage();
    }

    public void setImg(Image img) {
        this.img = img;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        int width = getWidth();
        int height = getHeight();

        if (width > 0 && height > 0) {
            scaled = img.getScaledInstance(getWidth(), getHeight(),
                    Image.SCALE_SMOOTH);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(scaled, 0, 0, null);
    }

    @Override
    public Dimension getPreferredSize() {
        return img == null ? new Dimension(200, 200) : new Dimension(
                img.getWidth(this), img.getHeight(this));
    }
}
