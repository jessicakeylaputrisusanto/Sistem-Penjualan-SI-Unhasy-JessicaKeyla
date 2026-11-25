import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class KoneksiDB {
    private static final String URL = "jdbc:mysql://localhost:3306/db_peminatan_fti";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Default XAMPP kosong

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("Koneksi ke MySQL berhasil!");
        } catch (SQLException e) {
            System.out.println("Koneksi gagal: " + e.getMessage());
        }
    }
}