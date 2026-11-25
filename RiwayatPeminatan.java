import java.io.Serializable;
import java.time.LocalDate;

public class RiwayatPeminatan implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String nim;
    private final int semester;
    private String bidang;
    private String catatan;
    private LocalDate tanggal;

    public RiwayatPeminatan(String nim, int semester, String bidang, String catatan, LocalDate tanggal) {
        this.nim = nim;
        this.semester = semester;
        this.bidang = bidang;
        this.catatan = catatan;
        this.tanggal = tanggal;
    }

    public String getNim() { return nim; }
    public int getSemester() { return semester; }
    public String getBidang() { return bidang; }
    public String getCatatan() { return catatan; }
    public LocalDate getTanggal() { return tanggal; }

    public void perbarui(String bidang, String catatan, LocalDate tanggal) {
        this.bidang = bidang;
        this.catatan = catatan;
        this.tanggal = tanggal;
    }
}
