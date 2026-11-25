import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.sql.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
} catch (ClassNotFoundException | InstantiationException
                    | IllegalAccessException | UnsupportedLookAndFeelException exception) {
                System.err.println("Tema sistem tidak dapat dimuat: " + exception.getMessage());
            }

            try {
                DataService dataService = new DataService();
                tampilkanLogin(dataService);
            } catch (IOException | ClassNotFoundException exception) {
                JOptionPane.showMessageDialog(null,
                        "Aplikasi tidak dapat dimulai:\n" + exception.getMessage(),
                        "Gagal memulai", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    static void tampilkanLogin(DataService dataService) {
        LoginFrame loginFrame = new LoginFrame(dataService,
                (frame, session) -> prosesLogin(frame, dataService, session));
        loginFrame.setVisible(true);
    }

    private static void prosesLogin(LoginFrame loginFrame, DataService dataService,
                                    UserSession session) {
        loginFrame.setVisible(false);
        try {
            SistemPeminatanFTI sistem = new SistemPeminatanFTI(
                    dataService, session, () -> tampilkanLogin(dataService));
            sistem.setVisible(true);
            loginFrame.dispose();
        } catch (IOException | ClassNotFoundException | IllegalArgumentException exception) {
            loginFrame.setVisible(true);
            JOptionPane.showMessageDialog(loginFrame,
                    "Sistem utama tidak dapat dibuka:\n" + exception.getMessage(),
                    "Gagal membuka sistem", JOptionPane.ERROR_MESSAGE);
        }
        }
}
