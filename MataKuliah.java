import java.io.Serializable;

public class MataKuliah implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String kode;
    private final String nama;
    private final int semester;
    private final int sks;
    private final boolean simulasi;
    private final boolean usulan;

    public MataKuliah(String kode, String nama, int semester, int sks, boolean simulasi) {
        this(kode, nama, semester, sks, simulasi, false);
    }

    public MataKuliah(String kode, String nama, int semester, int sks, boolean simulasi, boolean usulan) {
        this.kode = kode;
        this.nama = nama;
        this.semester = semester;
        this.sks = sks;
        this.simulasi = simulasi;
        this.usulan = usulan;
    }

    public String getKode() { return kode; }
    public String getNama() { return nama; }
    public int getSemester() { return semester; }
    public int getSks() { return sks; }
    public boolean isSimulasi() { return simulasi; }
    public boolean isUsulan() { return usulan; }

    @Override
    public String toString() {
        return "S" + semester + " - " + nama + " (" + sks + " SKS)"
                + (usulan ? " [USULAN]" : simulasi ? " [SIMULASI]" : "");
    }
}
