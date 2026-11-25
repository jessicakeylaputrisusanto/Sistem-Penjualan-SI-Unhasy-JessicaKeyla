import java.util.Arrays;

public final class AuthService {
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    private final DataService dataService;

    public AuthService(DataService dataService) {
        if (dataService == null) {
            throw new IllegalArgumentException("DataService wajib disediakan.");
        }
        this.dataService = dataService;
    }

    public UserSession authenticate(UserSession.Role role, String username, char[] password) {
        if (role == null) throw new IllegalArgumentException("Pilih jenis pengguna.");
        String identity = username == null ? "" : username.trim();
        if (identity.isEmpty() || password == null || password.length == 0) {
            throw new IllegalArgumentException("Username dan password wajib diisi.");
        }

        if (role == UserSession.Role.ADMIN) {
            if (ADMIN_USERNAME.equals(identity)
                    && passwordMatches(password, ADMIN_PASSWORD)) {
                return new UserSession(role, identity, "", "Administrator");
            }
            throw new IllegalArgumentException("Username atau password Admin salah.");
        }

        Mahasiswa mahasiswa = dataService.cariMahasiswa(identity);
        if (mahasiswa == null || dataService.isMahasiswaSimulasi(identity)) {
            throw new IllegalArgumentException(
                    "NIM tidak ditemukan pada data mahasiswa asli.");
        }
        if (!passwordMatches(password, identity)) {
            throw new IllegalArgumentException("Password Mahasiswa harus sama dengan NIM.");
        }
        return new UserSession(role, identity, mahasiswa.getNim(), mahasiswa.getNama());
    }

    private boolean passwordMatches(char[] entered, String expected) {
        char[] expectedChars = expected.toCharArray();
        try {
            return Arrays.equals(entered, expectedChars);
        } finally {
            Arrays.fill(expectedChars, '\0');
        }
    }
}
