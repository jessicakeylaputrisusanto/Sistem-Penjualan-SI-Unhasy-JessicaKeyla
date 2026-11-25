import java.io.Serializable;

public class NilaiMataKuliah implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String nim;
    private final String kode;
    private final String nama;
    private final int semester;
    private final int sks;
    private final String nilaiHuruf;
    private final Boolean terverifikasi;

    public NilaiMataKuliah(String nim, String kode, String nama, int semester, int sks, String nilaiHuruf) {
        this(nim, kode, nama, semester, sks, nilaiHuruf, true);
    }

    public NilaiMataKuliah(String nim, String kode, String nama, int semester, int sks,
                           String nilaiHuruf, boolean terverifikasi) {
        if (nim == null || nim.trim().isEmpty()) throw new IllegalArgumentException("NIM wajib diisi.");
        if (kode == null || kode.trim().isEmpty()) throw new IllegalArgumentException("Kode mata kuliah wajib diisi.");
        if (nama == null || nama.trim().isEmpty()) throw new IllegalArgumentException("Nama mata kuliah wajib diisi.");
        if (semester < 1 || semester > 8) throw new IllegalArgumentException("Semester mata kuliah harus 1 sampai 8.");
        if (sks < 1 || sks > 20) throw new IllegalArgumentException("SKS harus berada pada rentang 1 sampai 20.");
        String grade = nilaiHuruf == null ? "" : nilaiHuruf.trim().toUpperCase(java.util.Locale.ROOT);
        if (!java.util.Arrays.asList("", "A", "B", "C", "D", "E").contains(grade)) {
            throw new IllegalArgumentException("Nilai huruf harus A, B, C, D, atau E.");
        }
        this.nim = nim.trim();
        this.kode = kode.trim().toUpperCase(java.util.Locale.ROOT);
        this.nama = nama.trim();
        this.semester = semester;
        this.sks = sks;
        this.nilaiHuruf = grade;
        this.terverifikasi = Boolean.valueOf(terverifikasi);
    }

    public String getNim() { return nim; }
    public String getKode() { return kode; }
    public String getNama() { return nama; }
    public int getSemester() { return semester; }
    public int getSks() { return sks; }
    public String getNilaiHuruf() { return nilaiHuruf; }
    public boolean isTerverifikasi() { return terverifikasi == null || terverifikasi.booleanValue(); }
    public String getStatusKelulusan() {
        if (nilaiHuruf.isEmpty()) return "Belum Dinilai";
        return "ABC".contains(nilaiHuruf) ? "Lulus" : "Tidak Lulus";
    }
}
