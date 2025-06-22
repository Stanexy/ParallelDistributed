import javax.swing.*;
import java.awt.*;

class RoundedPanel extends JPanel {
    private final int cornerRadius;
    private final Color bgColor;

    public RoundedPanel(int radius, Color bgColor) {
        this.cornerRadius = radius;
        this.bgColor = bgColor;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
    }
}
