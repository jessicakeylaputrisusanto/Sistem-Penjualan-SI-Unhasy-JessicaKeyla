import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RiwayatPenjurusan implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String nim;
    private final String bidang;
    private final LocalDate tanggal;
    private final List<String> mataKuliahPendukung;
    private final boolean layak;
    private final boolean simulasi;

    public RiwayatPenjurusan(String nim, String bidang, LocalDate tanggal,
                             List<String> mataKuliahPendukung, boolean layak, boolean simulasi) {
        this.nim = nim;
        this.bidang = bidang;
        this.tanggal = tanggal;
        this.mataKuliahPendukung = new ArrayList<>(mataKuliahPendukung);
        this.layak = layak;
        this.simulasi = simulasi;
    }

    public String getNim() { return nim; }
    public String getBidang() { return bidang; }
    public LocalDate getTanggal() { return tanggal; }
    public List<String> getMataKuliahPendukung() {
        return Collections.unmodifiableList(mataKuliahPendukung);
    }
    public boolean isLayak() { return layak; }
    public boolean isSimulasi() { return simulasi; }
}
