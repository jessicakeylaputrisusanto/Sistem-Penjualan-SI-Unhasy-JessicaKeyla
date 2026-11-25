import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.BiConsumer;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.AbstractBorder;

public final class LoginFrame extends JFrame {
    private static final Color NAVY = new Color(20, 45, 80);
    private static final Color NAVY_DARK = new Color(13, 32, 59);
    private static final Color BLUE = new Color(36, 109, 179);
    private static final Color LIGHT_BLUE = new Color(234, 242, 250);
    private static final Color GOLD = new Color(212, 175, 55);
    private static final Color GOLD_DARK = new Color(181, 138, 42);
    private static final Color TEXT = new Color(82, 97, 116);
    private static final Color MUTED = new Color(82, 97, 116);
    private static final Color BORDER_BLUE = new Color(195, 214, 235);

    private final AuthService authService;
    private final BiConsumer<LoginFrame, UserSession> authenticatedHandler;
    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pages = new JPanel(pageLayout);
    private final BufferedImage backgroundImage =
            loadAsset("Kampus UNHASY di Bawah Langit Cerah.png");
    private final JTextField usernameField = new JTextField(22);
    private final JPasswordField passwordField = new JPasswordField(22);
    private final char passwordEchoChar = passwordField.getEchoChar();
    private final JToggleButton showPassword = new JToggleButton("Tampilkan password");
    private final JLabel loginTitleLabel = new JLabel();
    private final JLabel loginHintLabel = new JLabel();
    private final JLabel statusLabel = new JLabel(" ");
    private JLabel loginRoleIconLabel;
    private JButton loginButton;
    private UserSession.Role activeRole;
    private UserSession authenticatedSession;

    public LoginFrame(DataService dataService) {
        this(dataService, null);
    }

    public LoginFrame(DataService dataService,
                      BiConsumer<LoginFrame, UserSession> authenticatedHandler) {
        super("Login - Sistem Informasi Penjurusan FTI UNHASY");
        this.authService = new AuthService(dataService);
        this.authenticatedHandler = authenticatedHandler;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 720));
        setSize(1440, 810);
        setLocationRelativeTo(null);
        setContentPane(createContent());
        getRootPane().setDefaultButton(loginButton);
    }

    private JPanel createContent() {
        BackgroundPanel root = new BackgroundPanel(backgroundImage);
        root.setLayout(new BorderLayout());
        pages.setOpaque(false);
        pages.add(createWelcomePage(), "welcome");
        pages.add(createLoginPage(), "login");
        root.add(pages, BorderLayout.CENTER);
        pageLayout.show(pages, "welcome");
        return root;
    }

    private JPanel createWelcomePage() {
        JPanel page = new JPanel(null) {
            @Override
            public void doLayout() {
                int leftWidth = (int) Math.round(getWidth() * 0.57);
                getComponent(0).setBounds(0, 0, leftWidth, getHeight());
                getComponent(1).setBounds(leftWidth, 0,
                        getWidth() - leftWidth, getHeight());
            }
        };
        page.setOpaque(false);
        page.add(createBrandPanel());
        page.add(createWelcomePanel());
        return page;
    }

    private JPanel createBrandPanel() {
        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(BorderFactory.createEmptyBorder(38, 68, 155, 28));

        JPanel identity = new JPanel();
        identity.setOpaque(false);
        identity.setLayout(new BoxLayout(identity, BoxLayout.X_AXIS));
        identity.setMaximumSize(new Dimension(1200, 135));
        JLabel logo = createLogoLabel();
        logo.setPreferredSize(new Dimension(130, 130));
        logo.setMaximumSize(new Dimension(130, 130));
        identity.add(logo);
        identity.add(Box.createHorizontalStrut(24));
        JPanel divider = new JPanel();
        divider.setBackground(GOLD);
        divider.setPreferredSize(new Dimension(3, 120));
        divider.setMaximumSize(new Dimension(3, 120));
        identity.add(divider);
        identity.add(Box.createHorizontalStrut(28));
        JPanel university = new JPanel();
        university.setOpaque(false);
        university.setLayout(new BoxLayout(university, BoxLayout.Y_AXIS));
        JLabel universityName = new JLabel(
                "<html><b>UNIVERSITAS<br>HASYIM ASY'ARI</b></html>");
        universityName.setFont(new Font("SansSerif", Font.BOLD, 27));
        universityName.setForeground(Color.WHITE);
        JLabel campus = new JLabel("TEBUIRENG JOMBANG");
        campus.setFont(new Font("SansSerif", Font.BOLD, 17));
        campus.setForeground(new Color(255, 202, 91));
        university.add(Box.createVerticalGlue());
        university.add(universityName);
        university.add(Box.createVerticalStrut(7));
        university.add(campus);
        university.add(Box.createVerticalGlue());
        university.setMaximumSize(new Dimension(600, 132));
        identity.add(university);
        identity.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(identity);
        brand.add(Box.createVerticalStrut(58));

        JLabel systemName = new JLabel(
                "<html><b>Sistem Informasi<br>"
                + "<font color='#F6C45B'>Penjurusan Mata Kuliah</font></b></html>");
        systemName.setFont(new Font("SansSerif", Font.BOLD, 50));
        systemName.setForeground(Color.WHITE);
        systemName.setMaximumSize(new Dimension(680, 125));
        systemName.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(systemName);
        brand.add(Box.createVerticalStrut(16));

        JPanel accent = new GoldRule(600, 5);
        accent.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(accent);
        brand.add(Box.createVerticalStrut(18));

        JLabel subtitle = new JLabel(
                "<html><i>Rekomendasi Peminatan<br>"
                + "Program Studi Sistem Informasi</i></html>");
        subtitle.setFont(new Font("SansSerif", Font.ITALIC, 20));
        subtitle.setForeground(new Color(239, 244, 251));
        subtitle.setMaximumSize(new Dimension(620, 58));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(subtitle);
        brand.add(Box.createVerticalGlue());
        brand.add(createFeatureRow());
        return brand;
    }

    private JPanel createFeatureRow() {
        JPanel features = new JPanel(new GridLayout(1, 3));
        features.setOpaque(false);
        features.setMaximumSize(new Dimension(620, 62));
        features.setAlignmentX(Component.LEFT_ALIGNMENT);
        features.add(createFeature(FeatureKind.CAP, "Akademik", "Terintegrasi", true));
        features.add(createFeature(FeatureKind.CHART, "Rekomendasi", "Peminatan", true));
        features.add(createFeature(FeatureKind.LAPTOP, "Mendukung", "Masa Depan", false));
        return features;
    }

    private JPanel createFeature(FeatureKind kind, String firstLine,
                                 String secondLine, boolean divider) {
        JPanel feature = new JPanel(new BorderLayout(10, 0));
        feature.setOpaque(false);
        feature.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, divider ? 14 : 0));
        JLabel icon = new JLabel(new FeatureIcon(kind));
        feature.add(icon, BorderLayout.WEST);
        JLabel label = new JLabel("<html>" + firstLine + "<br>" + secondLine + "</html>");
        label.setFont(new Font("SansSerif", Font.PLAIN, 15));
        label.setForeground(Color.WHITE);
        feature.add(label, BorderLayout.CENTER);
        if (divider) {
            feature.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, 2, GOLD),
                    BorderFactory.createEmptyBorder(0, 14, 0, 14)));
        }
        return feature;
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(22, 22, 22, 22);
        center.add(createWelcomeCard(), constraints);
        panel.add(center, BorderLayout.CENTER);

        JLabel footer = new JLabel("UNHASY    •    Tebuireng Jombang");
        footer.setFont(new Font("SansSerif", Font.PLAIN, 13));
        footer.setForeground(Color.WHITE);
        footer.setHorizontalAlignment(SwingConstants.RIGHT);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 0, 20, 32));
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createWelcomeCard() {
        RoundedPanel welcomeCard = new RoundedPanel(
                new Color(255, 255, 255, 238), new Color(231, 183, 74));
        welcomeCard.setLayout(new BorderLayout(0, 20));
        welcomeCard.setBorder(BorderFactory.createEmptyBorder(100, 26, 22, 26));
        welcomeCard.setPreferredSize(new Dimension(560, 500));
        welcomeCard.setMinimumSize(new Dimension(370, 470));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel welcome = new JLabel("Selamat Datang", SwingConstants.CENTER);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 38));
        welcome.setForeground(NAVY);
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel instruction = new JLabel(
                "Silakan pilih jenis akun untuk melanjutkan", SwingConstants.CENTER);
        instruction.setFont(new Font("SansSerif", Font.PLAIN, 16));
        instruction.setForeground(MUTED);
        instruction.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel accent = new GoldRule(150, 4);
        accent.setAlignmentX(Component.CENTER_ALIGNMENT);
        heading.add(welcome);
        heading.add(Box.createVerticalStrut(8));
        heading.add(instruction);
        heading.add(Box.createVerticalStrut(16));
        heading.add(accent);
        welcomeCard.add(heading, BorderLayout.NORTH);

        JPanel choices = new JPanel(new GridLayout(1, 2, 18, 0));
        choices.setOpaque(false);
        choices.add(createRoleCard(UserSession.Role.MAHASISWA));
        choices.add(createRoleCard(UserSession.Role.ADMIN));
        welcomeCard.add(choices, BorderLayout.CENTER);

        JLabel emblem = new JLabel(new WelcomeIcon(), SwingConstants.CENTER);
        JPanel cardLayer = new JPanel(null) {
            @Override
            public void doLayout() {
                int cardTop = 52;
                welcomeCard.setBounds(0, cardTop, getWidth(),
                        Math.max(0, getHeight() - cardTop));
                emblem.setBounds((getWidth() - 112) / 2, 0, 112, 112);
            }
        };
        cardLayer.setOpaque(false);
        cardLayer.setPreferredSize(new Dimension(560, 552));
        cardLayer.setMinimumSize(new Dimension(370, 522));
        cardLayer.add(welcomeCard);
        cardLayer.add(emblem);
        cardLayer.setComponentZOrder(emblem, 0);
        return cardLayer;
    }

    private JPanel createRoleCard(UserSession.Role role) {
        boolean student = role == UserSession.Role.MAHASISWA;
        Color accent = student ? BLUE : GOLD;
        RoundedPanel card = new RoundedPanel(
                student ? new Color(225, 239, 253, 245) : new Color(255, 250, 239, 245),
                accent);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(14, 12, 12, 12));
        card.setPreferredSize(new Dimension(220, 260));

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        JLabel icon = new JLabel(new RoleIcon(student, NAVY), SwingConstants.CENTER);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel title = new JLabel(student
                ? "<html><center>LOGIN<br>MAHASISWA</center></html>"
                : "<html><center>LOGIN<br>ADMIN</center></html>",
                SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(NAVY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        details.add(Box.createVerticalGlue());
        details.add(icon);
        details.add(Box.createVerticalStrut(8));
        details.add(title);
        details.add(Box.createVerticalGlue());
        card.add(details, BorderLayout.CENTER);

        JButton choose = styledButton("", student ? NAVY_DARK : GOLD_DARK);
        choose.setIcon(new ArrowCircleIcon(student ? NAVY_DARK : GOLD_DARK));
        choose.setToolTipText(student ? "Masuk sebagai Mahasiswa" : "Masuk sebagai Admin");
        choose.getAccessibleContext().setAccessibleName(
                student ? "Login Mahasiswa" : "Login Admin");
        choose.setPreferredSize(new Dimension(0, 48));
        choose.addActionListener(event -> pilihPeran(role));
        card.add(choose, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createLoginPage() {
        JPanel page = new JPanel(new GridBagLayout());
        page.setOpaque(false);
        page.setBorder(BorderFactory.createEmptyBorder(28, 24, 30, 24));

        RoundedPanel card = new RoundedPanel(new Color(255, 255, 255, 248));
        card.setLayout(new BorderLayout(0, 20));
        card.setBorder(BorderFactory.createEmptyBorder(34, 42, 34, 42));
        card.setPreferredSize(new Dimension(540, 500));

        JPanel heading = new JPanel(new BorderLayout(16, 0));
        heading.setOpaque(false);
        loginRoleIconLabel = new JLabel();
        loginRoleIconLabel.setPreferredSize(new Dimension(58, 58));
        heading.add(loginRoleIconLabel, BorderLayout.WEST);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        loginTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 23));
        loginTitleLabel.setForeground(NAVY);
        loginHintLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        loginHintLabel.setForeground(MUTED);
        titles.add(Box.createVerticalGlue());
        titles.add(loginTitleLabel);
        titles.add(Box.createVerticalStrut(6));
        titles.add(loginHintLabel);
        titles.add(Box.createVerticalGlue());
        heading.add(titles, BorderLayout.CENTER);
        card.add(heading, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        addLoginField(fields, 0, "Username", usernameField);
        addLoginField(fields, 1, "Password", passwordField);
        showPassword.setOpaque(false);
        showPassword.setForeground(MUTED);
        showPassword.setFont(new Font("SansSerif", Font.PLAIN, 12));
        showPassword.setIcon(new UiIcon(UiGlyph.EYE, NAVY, 18));
        showPassword.setFocusPainted(false);
        showPassword.setBorderPainted(false);
        showPassword.setContentAreaFilled(false);
        showPassword.setHorizontalAlignment(SwingConstants.LEFT);
        showPassword.addActionListener(event -> {
            boolean visible = showPassword.isSelected();
            passwordField.setEchoChar(visible ? (char) 0 : passwordEchoChar);
            showPassword.setText(visible ? "Sembunyikan password" : "Tampilkan password");
            showPassword.setIcon(new UiIcon(
                    visible ? UiGlyph.EYE_OFF : UiGlyph.EYE, NAVY, 18));
        });
        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 0;
        fieldConstraints.gridy = 4;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(3, 0, 4, 0);
        fields.add(showPassword, fieldConstraints);
        card.add(fields, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(0, 12));
        bottom.setOpaque(false);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLabel.setForeground(MUTED);
        bottom.add(statusLabel, BorderLayout.NORTH);

        JPanel actions = new JPanel(new BorderLayout(12, 0));
        actions.setOpaque(false);
        JButton backButton = new JButton("Kembali ke pilihan akun");
        backButton.setIcon(new UiIcon(UiGlyph.BACK, NAVY, 18));
        backButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        backButton.setForeground(NAVY);
        backButton.setBackground(LIGHT_BLUE);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_BLUE),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        setHoverColors(backButton, LIGHT_BLUE, new Color(218, 232, 247));
        backButton.addActionListener(event -> kembaliKePilihan());
        actions.add(backButton, BorderLayout.WEST);

        loginButton = styledButton("LOGIN", BLUE);
        loginButton.setPreferredSize(new Dimension(170, 42));
        loginButton.addActionListener(event -> login());
        actions.add(loginButton, BorderLayout.CENTER);
        bottom.add(actions, BorderLayout.SOUTH);
        card.add(bottom, BorderLayout.SOUTH);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        page.add(card, constraints);
        return page;
    }

    private void addLoginField(JPanel fields, int row, String title, JTextField input) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row * 2;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(row == 0 ? 0 : 13, 0, 6, 0);
        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(TEXT);
        label.setLabelFor(input);
        fields.add(label, constraints);

        input.setFont(new Font("SansSerif", Font.PLAIN, 14));
        input.setForeground(NAVY);
        input.setBackground(Color.WHITE);
        input.setCaretColor(NAVY);
        input.setMargin(new Insets(8, 8, 8, 8));
        input.setPreferredSize(new Dimension(360, 42));
        input.setBorder(new IconFieldBorder(new UiIcon(
                row == 0 ? UiGlyph.USER : UiGlyph.LOCK, MUTED, 18), BORDER_BLUE));
        constraints.gridy = row * 2 + 1;
        constraints.insets = new Insets(0, 0, 2, 0);
        fields.add(input, constraints);
    }

    private void pilihPeran(UserSession.Role role) {
        activeRole = role;
        authenticatedSession = null;
        usernameField.setText("");
        passwordField.setText("");
        showPassword.setSelected(false);
        passwordField.setEchoChar(passwordEchoChar);
        statusLabel.setText(" ");
        loginTitleLabel.setText("Login " + role);
        loginRoleIconLabel.setIcon(new RoleIcon(role == UserSession.Role.MAHASISWA,
                role == UserSession.Role.ADMIN ? NAVY : BLUE));
        loginHintLabel.setText(role == UserSession.Role.MAHASISWA
                ? "Gunakan NIM sebagai username dan password."
                : "Masuk dengan akun administrator.");
        loginButton.setText("LOGIN " + role.toString().toUpperCase(java.util.Locale.ROOT));
        setButtonColor(loginButton, role == UserSession.Role.ADMIN ? NAVY_DARK : BLUE);
        pageLayout.show(pages, "login");
        SwingUtilities.invokeLater(() -> usernameField.requestFocusInWindow());
    }

    private void kembaliKePilihan() {
        activeRole = null;
        authenticatedSession = null;
        usernameField.setText("");
        passwordField.setText("");
        showPassword.setSelected(false);
        passwordField.setEchoChar(passwordEchoChar);
        statusLabel.setText(" ");
        pageLayout.show(pages, "welcome");
    }

    private void login() {
        authenticatedSession = null;
        char[] password = passwordField.getPassword();
        try {
            authenticatedSession = authService.authenticate(
                    activeRole, usernameField.getText(), password);
            statusLabel.setForeground(new Color(31, 126, 78));
            statusLabel.setText("Login berhasil sebagai " + authenticatedSession.getRole() + ".");
            if (authenticatedHandler == null) {
                JOptionPane.showMessageDialog(this,
                        "Autentikasi berhasil. Integrasi ke dashboard belum diaktifkan.",
                        "Login berhasil", JOptionPane.INFORMATION_MESSAGE);
            } else {
                authenticatedHandler.accept(this, authenticatedSession);
            }
        } catch (IllegalArgumentException exception) {
            statusLabel.setForeground(new Color(180, 62, 62));
            statusLabel.setText(exception.getMessage());
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Login gagal", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(password, '\0');
            passwordField.setText("");
        }
    }

    private JButton styledButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.darker()),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        setHoverColors(button, background, buttonHoverColor(background));
        return button;
    }

    private void setButtonColor(JButton button, Color background) {
        button.setBackground(background);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.darker()),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        setHoverColors(button, background, buttonHoverColor(background));
    }

    private Color buttonHoverColor(Color background) {
        return background.equals(NAVY_DARK) ? NAVY : background.darker();
    }

    private void setHoverColors(JButton button, Color normal, Color hovered) {
        button.putClientProperty("login.normalBackground", normal);
        button.putClientProperty("login.hoverBackground", hovered);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(normal);
        if (button.getClientProperty("login.hoverListenerInstalled") == null) {
            button.putClientProperty("login.hoverListenerInstalled", Boolean.TRUE);
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent event) {
                    if (button.isEnabled()) {
                        button.setBackground((Color) button.getClientProperty(
                                "login.hoverBackground"));
                    }
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent event) {
                    button.setBackground((Color) button.getClientProperty(
                            "login.normalBackground"));
                }
            });
        }
    }

    private JLabel createLogoLabel() {
        BufferedImage image = loadAsset("LOGO UNHASY.png");
        if (image == null) {
            JLabel fallback = new JLabel("UNHASY", SwingConstants.CENTER);
            fallback.setFont(new Font("SansSerif", Font.BOLD, 13));
            fallback.setForeground(Color.WHITE);
            return fallback;
        }
        double scale = Math.min(130d / image.getWidth(), 130d / image.getHeight());
        int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(image.getHeight() * scale));
        return new JLabel(new ImageIcon(image.getScaledInstance(
                width, height, java.awt.Image.SCALE_SMOOTH)));
    }

    private static BufferedImage loadAsset(String fileName) {
        File file = new File("asset" + File.separator + fileName);
        if (!file.isFile()) {
            System.err.println("Aset gambar tidak ditemukan: " + file.getPath());
            return null;
        }
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                System.err.println("Format aset gambar tidak dapat dibaca: " + file.getPath());
            }
            return image;
        } catch (IOException exception) {
            System.err.println("Gagal membaca aset gambar " + file.getPath()
                    + ": " + exception.getMessage());
            return null;
        }
    }

    public UserSession getAuthenticatedSession() {
        return authenticatedSession;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                DataService dataService = new DataService();
                new LoginFrame(dataService).setVisible(true);
            } catch (java.io.IOException | ClassNotFoundException exception) {
                JOptionPane.showMessageDialog(null,
                        "Halaman login tidak dapat dibuka:\n" + exception.getMessage(),
                        "Gagal memulai", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static class BackgroundPanel extends JPanel {
        private final BufferedImage image;

        private BackgroundPanel(BufferedImage image) {
            this.image = image;
            setBackground(LIGHT_BLUE);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int width = getWidth();
            int height = getHeight();
            g.setPaint(new GradientPaint(0, 0, new Color(8, 32, 67),
                    Math.max(1, width), 0, new Color(13, 62, 107)));
            g.fillRect(0, 0, width, height);

            if (image != null && getWidth() > 0 && getHeight() > 0) {
                int photoMaxWidth = (int) Math.round(width * 0.46);
                double scale = Math.max((double) photoMaxWidth / image.getWidth(),
                        (double) height / image.getHeight());
                int photoWidth = (int) Math.round(image.getWidth() * scale);
                int photoHeight = (int) Math.round(image.getHeight() * scale);
                int photoY = (height - photoHeight) / 2;
                g.drawImage(image, 0, photoY, photoWidth, photoHeight, null);

                g.setComposite(AlphaComposite.SrcOver.derive(0.44f));
                g.setColor(NAVY_DARK);
                g.fillRect(0, photoY, photoWidth, photoHeight);
                g.setComposite(AlphaComposite.SrcOver);
                g.setPaint(new GradientPaint(0, 0, new Color(8, 32, 67, 0),
                        Math.max(1, photoWidth), 0, new Color(8, 40, 75, 220)));
                g.fillRect(0, 0, photoWidth, height);
            }
            g.setComposite(AlphaComposite.SrcOver);
            paintAiAccents(g, width, height);
            paintDecorations(g);
            g.dispose();
        }

        private void paintAiAccents(Graphics2D g, int width, int height) {
            float radius = Math.max(1f, width * 0.38f);
            g.setPaint(new RadialGradientPaint(width * 0.78f, height * 0.22f, radius,
                    new float[] {0f, 1f},
                    new Color[] {new Color(73, 176, 255, 54),
                        new Color(73, 176, 255, 0)}));
            g.fillRect(0, 0, width, height);

            g.setColor(new Color(129, 203, 255, 42));
            g.setStroke(new java.awt.BasicStroke(1.2f,
                    java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            Path2D circuit = new Path2D.Double();
            circuit.moveTo(width * 0.70, height * 0.13);
            circuit.lineTo(width * 0.78, height * 0.13);
            circuit.lineTo(width * 0.78, height * 0.20);
            circuit.lineTo(width * 0.90, height * 0.20);
            circuit.moveTo(width * 0.91, height * 0.72);
            circuit.lineTo(width * 0.84, height * 0.72);
            circuit.lineTo(width * 0.84, height * 0.64);
            circuit.lineTo(width * 0.76, height * 0.64);
            g.draw(circuit);
            g.setColor(new Color(212, 175, 55, 75));
            g.fillOval((int) (width * 0.78) - 3, (int) (height * 0.20) - 3, 6, 6);
            g.fillOval((int) (width * 0.84) - 3, (int) (height * 0.64) - 3, 6, 6);
        }

        private void paintDecorations(Graphics2D g) {
            int width = getWidth();
            int height = getHeight();
            Path2D goldTop = new Path2D.Double();
            goldTop.moveTo(width - 230, 0);
            goldTop.lineTo(width, 0);
            goldTop.lineTo(width, 76);
            goldTop.quadTo(width - 120, 54, width - 230, 0);
            goldTop.closePath();
            g.setColor(new Color(212, 175, 55, 190));
            g.fill(goldTop);

            Path2D navyTop = new Path2D.Double();
            navyTop.moveTo(width - 172, 0);
            navyTop.lineTo(width, 0);
            navyTop.lineTo(width, 50);
            navyTop.quadTo(width - 93, 34, width - 172, 0);
            navyTop.closePath();
            g.setColor(new Color(13, 32, 59, 190));
            g.fill(navyTop);

            Path2D navyBottom = new Path2D.Double();
            navyBottom.moveTo(0, height - 82);
            navyBottom.curveTo(width * 0.18, height - 128, width * 0.34, height - 28,
                    width * 0.55, height - 26);
            navyBottom.curveTo(width * 0.78, height - 30, width * 0.90, height - 112,
                    width, height - 122);
            navyBottom.lineTo(width, height);
            navyBottom.lineTo(0, height);
            navyBottom.closePath();
            g.setColor(new Color(13, 32, 59, 205));
            g.fill(navyBottom);

            Path2D goldBottom = new Path2D.Double();
            goldBottom.moveTo(0, height - 135);
            goldBottom.curveTo(width * 0.18, height - 175, width * 0.34, height - 66,
                    width * 0.55, height - 66);
            goldBottom.curveTo(width * 0.78, height - 76, width * 0.90, height - 153,
                    width, height - 168);
            goldBottom.lineTo(width, height - 146);
            goldBottom.curveTo(width * 0.90, height - 132, width * 0.78, height - 54,
                    width * 0.55, height - 45);
            goldBottom.curveTo(width * 0.34, height - 45, width * 0.18, height - 145,
                    0, height - 112);
            goldBottom.closePath();
            g.setColor(new Color(212, 175, 55, 225));
            g.fill(goldBottom);

            Path2D whiteLine = new Path2D.Double();
            whiteLine.moveTo(0, height - 107);
            whiteLine.curveTo(width * 0.18, height - 145, width * 0.34, height - 48,
                    width * 0.55, height - 42);
            whiteLine.curveTo(width * 0.78, height - 42, width * 0.90, height - 119,
                    width, height - 138);
            g.setColor(Color.WHITE);
            g.setStroke(new java.awt.BasicStroke(4.5f,
                    java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            g.draw(whiteLine);
        }
    }

    private static class WelcomeIcon implements javax.swing.Icon {
        @Override
        public int getIconWidth() {
            return 112;
        }

        @Override
        public int getIconHeight() {
            return 112;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.translate(x, y);
            g.scale(112d / 92d, 112d / 92d);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(231, 183, 74));
            g.fillOval(1, 1, 90, 90);
            g.setColor(new Color(14, 58, 108));
            g.fillOval(6, 6, 80, 80);
            g.setColor(Color.WHITE);
            int[] capX = {20, 46, 72, 46};
            int[] capY = {38, 27, 38, 49};
            g.fillPolygon(capX, capY, capX.length);
            g.fillRoundRect(31, 43, 30, 18, 3, 3);
            g.setColor(new Color(231, 183, 74));
            g.setStroke(new java.awt.BasicStroke(2.2f,
                    java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            g.drawLine(71, 38, 71, 58);
            g.fillOval(68, 57, 6, 6);
            g.dispose();
        }
    }

    private enum FeatureKind {
        CAP,
        CHART,
        LAPTOP
    }

    private static class FeatureIcon implements javax.swing.Icon {
        private final FeatureKind kind;

        private FeatureIcon(FeatureKind kind) {
            this.kind = kind;
        }

        @Override
        public int getIconWidth() {
            return 42;
        }

        @Override
        public int getIconHeight() {
            return 42;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.translate(x, y);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.setStroke(new java.awt.BasicStroke(2.4f,
                    java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            switch (kind) {
                case CAP:
                    int[] capX = {2, 20, 38, 20};
                    int[] capY = {14, 6, 14, 22};
                    g.fillPolygon(capX, capY, capX.length);
                    g.drawArc(9, 15, 22, 18, 180, 180);
                    g.drawLine(37, 14, 37, 29);
                    g.setColor(GOLD);
                    g.fillOval(34, 28, 6, 6);
                    break;
                case CHART:
                    g.drawLine(4, 36, 39, 36);
                    g.fillRoundRect(7, 24, 6, 12, 2, 2);
                    g.fillRoundRect(19, 17, 6, 19, 2, 2);
                    g.fillRoundRect(31, 8, 6, 28, 2, 2);
                    g.drawLine(7, 20, 20, 12);
                    g.drawLine(20, 12, 32, 5);
                    break;
                case LAPTOP:
                    g.drawRoundRect(5, 5, 32, 25, 3, 3);
                    g.drawLine(2, 35, 40, 35);
                    g.drawLine(8, 30, 5, 35);
                    g.drawLine(34, 30, 37, 35);
                    g.drawLine(15, 12, 27, 12);
                    break;
                default:
                    throw new IllegalStateException("Ikon fitur tidak didukung: " + kind);
            }
            g.dispose();
        }
    }

    private static class ArrowCircleIcon implements javax.swing.Icon {
        private final Color arrowColor;

        private ArrowCircleIcon(Color arrowColor) {
            this.arrowColor = arrowColor;
        }

        @Override
        public int getIconWidth() {
            return 28;
        }

        @Override
        public int getIconHeight() {
            return 28;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.translate(x, y);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillOval(1, 1, 26, 26);
            g.setColor(arrowColor);
            g.setStroke(new java.awt.BasicStroke(2.2f,
                    java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            g.drawLine(7, 14, 20, 14);
            g.drawLine(15, 8, 21, 14);
            g.drawLine(21, 14, 15, 20);
            g.dispose();
        }
    }

    private static class TranslucentPanel extends JPanel {
        private final Color fill;
        private final int opacity;

        private TranslucentPanel(Color fill, int opacity) {
            this.fill = fill;
            this.opacity = opacity;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setComposite(AlphaComposite.SrcOver.derive(opacity / 255f));
            g.setColor(fill);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class GoldRule extends JPanel {
        private final int lineWidth;
        private final int lineHeight;

        private GoldRule(int lineWidth, int lineHeight) {
            this.lineWidth = lineWidth;
            this.lineHeight = lineHeight;
            setOpaque(false);
            setPreferredSize(new Dimension(lineWidth, lineHeight));
            setMaximumSize(new Dimension(lineWidth, lineHeight));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0, 0, new Color(255, 224, 153),
                    Math.max(1, getWidth()), 0, GOLD));
            g.setStroke(new java.awt.BasicStroke(lineHeight,
                    java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            int y = getHeight() / 2;
            g.drawLine(lineHeight / 2, y, Math.max(lineHeight / 2, getWidth() - lineHeight / 2), y);
            g.dispose();
        }
    }

    private static class RoundedPanel extends JPanel {
        private final Color fill;
        private final Color border;

        private RoundedPanel(Color fill) {
            this(fill, BORDER_BLUE);
        }

        private RoundedPanel(Color fill, Color border) {
            this.fill = fill;
            this.border = border;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(13, 32, 59, 12));
            g.fillRoundRect(2, 5, getWidth() - 5, getHeight() - 7, 32, 32);
            g.setColor(new Color(13, 32, 59, 10));
            g.fillRoundRect(1, 3, getWidth() - 5, getHeight() - 7, 32, 32);
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 5, 32, 32);
            g.setColor(border);
            g.setStroke(new java.awt.BasicStroke(1.6f));
            g.drawRoundRect(0, 0, getWidth() - 4, getHeight() - 5, 32, 32);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class RoleIcon implements javax.swing.Icon {
        private final boolean student;
        private final Color color;

        private RoleIcon(boolean student, Color color) {
            this.student = student;
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return 90;
        }

        @Override
        public int getIconHeight() {
            return 90;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.translate(x, y);
            double scale = 90d / 58d;
            g.scale(scale, scale);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(255, 255, 255, 235));
            g.fillOval(1, 1, 56, 56);
            g.setColor(color);
            if (student) {
                int[] capX = {8, 29, 50, 29};
                int[] capY = {21, 12, 21, 30};
                g.fillPolygon(capX, capY, capX.length);
                g.fillRoundRect(15, 25, 28, 8, 2, 2);
                g.drawLine(49, 21, 49, 35);
                g.fillOval(46, 34, 6, 6);
            } else {
                int[] shieldX = {29, 46, 43, 38, 29, 20, 15, 12};
                int[] shieldY = {10, 16, 31, 40, 49, 40, 31, 16};
                g.fillPolygon(shieldX, shieldY, shieldX.length);
                g.setColor(Color.WHITE);
                g.fillOval(25, 18, 9, 9);
                g.fillRoundRect(20, 30, 20, 12, 6, 6);
            }
            g.dispose();
        }
    }

    private enum UiGlyph {
        USER,
        LOCK,
        BACK,
        EYE,
        EYE_OFF
    }

    private static class IconFieldBorder extends AbstractBorder {
        private final javax.swing.Icon icon;
        private final Color color;

        private IconFieldBorder(javax.swing.Icon icon, Color color) {
            this.icon = icon;
            this.color = color;
        }

        @Override
        public Insets getBorderInsets(Component component, Insets insets) {
            insets.set(0, 42, 0, 10);
            return insets;
        }

        @Override
        public void paintBorder(Component component, Graphics graphics,
                                int x, int y, int width, int height) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);
            g.drawRoundRect(x, y, width - 1, height - 1, 10, 10);
            icon.paintIcon(component, g, x + 12, y + (height - icon.getIconHeight()) / 2);
            g.dispose();
        }
    }

    private static class UiIcon implements javax.swing.Icon {
        private final UiGlyph glyph;
        private final Color color;
        private final int size;

        private UiIcon(UiGlyph glyph, Color color, int size) {
            this.glyph = glyph;
            this.color = color;
            this.size = size;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.translate(x, y);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);
            g.setStroke(new java.awt.BasicStroke(1.7f,
                    java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            int center = size / 2;
            switch (glyph) {
                case USER:
                    g.drawOval(center - 3, 2, 6, 6);
                    g.drawArc(center - 7, 9, 14, 11, 0, 180);
                    break;
                case LOCK:
                    g.drawArc(center - 5, 1, 10, 12, 0, 180);
                    g.drawRoundRect(center - 7, 8, 14, 10, 2, 2);
                    g.drawLine(center, 11, center, 15);
                    break;
                case BACK:
                    g.drawLine(2, center, size - 2, center);
                    g.drawLine(2, center, 8, center - 6);
                    g.drawLine(2, center, 8, center + 6);
                    break;
                case EYE:
                case EYE_OFF:
                    g.drawArc(1, center - 5, size - 2, 10, 0, 180);
                    g.drawArc(1, center - 5, size - 2, 10, 180, 180);
                    g.drawOval(center - 2, center - 2, 4, 4);
                    if (glyph == UiGlyph.EYE_OFF) {
                        g.drawLine(2, size - 2, size - 2, 2);
                    }
                    break;
                default:
                    throw new IllegalStateException("Ikon tidak didukung: " + glyph);
            }
            g.dispose();
        }
    }
}
