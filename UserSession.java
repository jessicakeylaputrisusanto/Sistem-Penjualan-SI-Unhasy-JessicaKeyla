public final class UserSession {
    public enum Role {
        ADMIN("Admin"),
        MAHASISWA("Mahasiswa");

        private final String label;

        Role(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final Role role;
    private final String username;
    private final String nim;
    private final String nama;

    UserSession(Role role, String username, String nim, String nama) {
        if (role == null) throw new IllegalArgumentException("Peran pengguna wajib diisi.");
        this.role = role;
        this.username = username == null ? "" : username;
        this.nim = nim == null ? "" : nim;
        this.nama = nama == null ? "" : nama;
    }

    public Role getRole() {
        return role;
    }

    public String getUsername() {
        return username;
    }

    public String getNim() {
        return nim;
    }

    public String getNama() {
        return nama;
    }
}
