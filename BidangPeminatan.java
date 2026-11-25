import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BidangPeminatan implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String nama;
    private final List<String> kodeMataKuliahPendukung;
    private final boolean simulasi;

    public BidangPeminatan(String nama, List<String> kodeMataKuliahPendukung, boolean simulasi) {
        if (!RekomendasiService.BIDANG.contains(nama)) {
            throw new IllegalArgumentException("Bidang peminatan tidak dikenal.");
        }
        this.nama = nama;
        this.kodeMataKuliahPendukung = new ArrayList<>();
        for (String kode : kodeMataKuliahPendukung) {
            String value = kode.trim().toUpperCase(java.util.Locale.ROOT);
            if (!value.isEmpty() && !this.kodeMataKuliahPendukung.contains(value)) {
                this.kodeMataKuliahPendukung.add(value);
            }
        }
        this.simulasi = simulasi;
    }

    public String getNama() { return nama; }
    public List<String> getKodeMataKuliahPendukung() {
        return Collections.unmodifiableList(kodeMataKuliahPendukung);
    }
    public boolean isSimulasi() { return simulasi; }
}
