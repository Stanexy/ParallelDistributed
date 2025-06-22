import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.image.BufferedImage;

class LoginScreen extends JFrame {
    private final JTextField userField;
    private final JPasswordField passField;
    private final JLabel feedback;
    private final String USERNAME = "admin";
    private final String PASSWORD = "admin123";

    public LoginScreen() {
        setTitle("TeamTasker Login");
        setSize(480, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(true);

        JPanel bg = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x6a11cb), 0, getHeight(), new Color(0x2575fc));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bg.setLayout(new GridBagLayout());

        JPanel card = new RoundedPanel(32, new Color(255, 255, 255, 235));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(350, 420));
        card.setBorder(new EmptyBorder(32, 32, 32, 32));

        JLabel logo = new JLabel(new ImageIcon(drawLogo(64, 64)));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(logo);

        JLabel title = new JLabel("TeamTasker");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(0x2575fc));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(24));

        userField = new JTextField(16);
        passField = new JPasswordField(16);
        styleField(userField, "Username");
        styleField(passField, "Password");

        card.add(userField);
        card.add(Box.createVerticalStrut(16));
        card.add(passField);

        JCheckBox showPass = new JCheckBox("Show Password");
        showPass.setForeground(new Color(0x2575fc));
        showPass.setOpaque(false);
        showPass.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        showPass.setAlignmentX(Component.CENTER_ALIGNMENT);
        showPass.addActionListener(_e -> passField.setEchoChar(showPass.isSelected() ? (char) 0 : '•'));
        card.add(showPass);

        card.add(Box.createVerticalStrut(16));
        feedback = new JLabel(" ");
        feedback.setForeground(new Color(255, 80, 80));
        feedback.setFont(new Font("Segoe UI", Font.BOLD, 13));
        feedback.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(feedback);

        JButton loginButton = new JButton("Login");
        styleButton(loginButton, new Color(0x2575fc));
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(_e -> authenticate());
        card.add(Box.createVerticalStrut(12));
        card.add(loginButton);

        JButton closeBtn = new JButton("×");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        closeBtn.setForeground(new Color(0x2575fc));
        closeBtn.setBackground(new Color(0, 0, 0, 0));
        closeBtn.setBorder(null);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.addActionListener(_e -> System.exit(0));
        card.add(Box.createVerticalStrut(8));
        card.add(closeBtn);

        bg.add(card, new GridBagConstraints());
        setContentPane(bg);
        setVisible(true);
    }

    private void authenticate() {
        String user = userField.getText();
        String pass = new String(passField.getPassword());
        if (user.equals(USERNAME) && pass.equals(PASSWORD)) {
            new CalendarUI(user);
            dispose();
        } else {
            feedback.setText("Invalid username or password.");
        }
    }

    private void styleField(JTextField field, String placeholder) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setMaximumSize(new Dimension(340, 40));
        field.setBorder(new CompoundBorder(
                new LineBorder(new Color(0x2575fc), 2, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        field.setBackground(new Color(245, 245, 255));
        field.setForeground(new Color(30, 30, 30));
        field.setCaretColor(new Color(0x2575fc));
        field.putClientProperty("JTextField.placeholderText", placeholder);
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 40));
    }

    private Image drawLogo(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0x2575fc));
        g.fillOval(0, 0, w, h);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, w / 2));
        FontMetrics fm = g.getFontMetrics();
        String s = "T";
        int sw = fm.stringWidth(s);
        int sh = fm.getAscent();
        g.drawString(s, (w - sw) / 2, (h + sh) / 2 - 4);
        g.dispose();
        return img;
    }
}
