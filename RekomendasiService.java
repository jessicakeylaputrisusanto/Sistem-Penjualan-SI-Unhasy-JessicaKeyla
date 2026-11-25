import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RekomendasiService {
    public static final List<String> BIDANG = Arrays.asList(
            "UI/UX Design",
            "Manajemen Proyek Sistem Informasi",
            "Audit dan Tata Kelola Sistem Informasi");

    public HasilRekomendasi analisis(Mahasiswa mahasiswa, List<NilaiMataKuliah> nilai,
                                     List<BidangPeminatan> bidang) {
        return analisis(mahasiswa, nilai, bidang, KurikulumService.semuaMataKuliah());
    }

    public HasilRekomendasi analisis(Mahasiswa mahasiswa, List<NilaiMataKuliah> nilai,
                                     List<BidangPeminatan> bidang,
                                     List<MataKuliah> kurikulum) {
        Map<String, NilaiMataKuliah> nilaiPerKode = new HashMap<>();
        Map<String, MataKuliah> mataKuliahPerKode = new HashMap<>();
        for (MataKuliah course : kurikulum) mataKuliahPerKode.put(course.getKode(), course);
        for (NilaiMataKuliah item : nilai) {
            NilaiMataKuliah existing = nilaiPerKode.get(item.getKode());
            if (existing == null || item.getSemester() >= existing.getSemester()) {
                nilaiPerKode.put(item.getKode(), item);
            }
        }
        List<SkorBidang> hasil = new ArrayList<>();
        for (BidangPeminatan spesialisasi : bidang) {
            List<String> pendukung = new ArrayList<>();
            List<String> belumLengkap = new ArrayList<>();
            List<String> perluDiperbaiki = new ArrayList<>();
            int totalNilai = 0;
            int jumlahNilai = 0;
            for (String kode : spesialisasi.getKodeMataKuliahPendukung()) {
                NilaiMataKuliah item = nilaiPerKode.get(kode);
                MataKuliah course = mataKuliahPerKode.get(kode);
                String nama = course == null ? kode : course.getNama();
                if (item == null || item.getNilaiHuruf().isEmpty()) {
                    belumLengkap.add(nama);
                    continue;
                }
                pendukung.add(kode + " — " + nama + " (" + item.getNilaiHuruf() + ")");
                totalNilai += nilaiAngka(item.getNilaiHuruf());
                jumlahNilai++;
                if (!"Lulus".equals(item.getStatusKelulusan())) {
                    perluDiperbaiki.add(nama + " (" + item.getNilaiHuruf() + ")");
                }
            }
            double averageGrade = jumlahNilai == 0 ? 0d : (double) totalNilai / jumlahNilai;
            boolean eligible = !spesialisasi.getKodeMataKuliahPendukung().isEmpty()
                    && belumLengkap.isEmpty() && perluDiperbaiki.isEmpty();
            List<String> reasons = new ArrayList<>();
            if (eligible) {
                reasons.add("Seluruh mata kuliah pendukung lulus; rata-rata nilai "
                        + String.format(java.util.Locale.ROOT, "%.2f", averageGrade) + " dari 4.");
            } else if (!perluDiperbaiki.isEmpty()) {
                reasons.add("Perlu memperbaiki: " + String.join(", ", perluDiperbaiki) + ".");
            }
            if (!belumLengkap.isEmpty()) {
                reasons.add("Nilai belum lengkap: " + String.join(", ", belumLengkap) + ".");
            }
            if (spesialisasi.getKodeMataKuliahPendukung().isEmpty()) {
                reasons.add("Mata kuliah pendukung belum diatur.");
            }
            hasil.add(new SkorBidang(spesialisasi.getNama(), averageGrade, eligible,
                    reasons, pendukung, belumLengkap, perluDiperbaiki));
        }
        hasil.sort(Comparator.comparing(SkorBidang::isLayak).reversed()
                .thenComparing(Comparator.comparingDouble(
                        SkorBidang::getRasioMataKuliahLulus).reversed())
                .thenComparing(Comparator.comparingDouble(SkorBidang::getRataRata).reversed()));
        boolean lengkap = hasil.stream().anyMatch(SkorBidang::isLayak);
        return new HasilRekomendasi(mahasiswa.getNim(), LocalDate.now(), hasil,
                !lengkap);
    }

    private int nilaiAngka(String grade) {
        switch (grade) {
            case "A": return 4;
            case "B": return 3;
            case "C": return 2;
            case "D": return 1;
            default: return 0;
        }
    }

    public static String normalisasiBidang(String bidang) {
        if (bidang == null) return "";
        String value = bidang.trim();
        if ("UI/UX (User Interface / User Experience)".equals(value)) return BIDANG.get(0);
        if ("Manajemen Proyek SI".equals(value)) return BIDANG.get(1);
        if ("Audit Sistem Informasi".equals(value)) return BIDANG.get(2);
        if ("Manajemen Hubungan Pelanggan (CRM)".equals(value)) return BIDANG.get(1);
        if ("Perencanaan Strategis SI/TI".equals(value)
                || "Pengukuran Kinerja & Evaluasi TI".equals(value)) return BIDANG.get(2);
        return BIDANG.contains(value) ? value : "";
    }

    public static class SkorBidang implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String bidang;
        private final int skor;
        private final List<String> alasan;
        private final List<String> mataKuliahPendukung;
        private final double rataRata;
        private final boolean layak;
        private final int jumlahMataKuliahLulus;
        private final int jumlahMataKuliahPendukung;
        private final List<String> belumLengkap;
        private final List<String> perluDiperbaiki;
        private final String statusKelayakan;

        public SkorBidang(String bidang, int skor, List<String> alasan, List<String> mataKuliahPendukung) {
            this.bidang = bidang;
            this.skor = skor;
            this.alasan = new ArrayList<>(alasan);
            this.mataKuliahPendukung = new ArrayList<>(mataKuliahPendukung);
            this.rataRata = skor;
            this.layak = false;
            this.jumlahMataKuliahLulus = 0;
            this.jumlahMataKuliahPendukung = mataKuliahPendukung.size();
            this.belumLengkap = new ArrayList<>();
            this.perluDiperbaiki = new ArrayList<>();
            this.statusKelayakan = "BELUM LENGKAP";
        }

        public SkorBidang(String bidang, double rataRata, boolean layak, List<String> alasan,
                          List<String> mataKuliahPendukung, List<String> belumLengkap,
                          List<String> perluDiperbaiki) {
            this.bidang = bidang;
            this.rataRata = rataRata;
            this.skor = (int) Math.round(rataRata * 25);
            this.layak = layak;
            this.alasan = new ArrayList<>(alasan);
            this.mataKuliahPendukung = new ArrayList<>(mataKuliahPendukung);
            this.jumlahMataKuliahLulus = Math.max(0,
                    mataKuliahPendukung.size() - perluDiperbaiki.size());
            this.jumlahMataKuliahPendukung = mataKuliahPendukung.size()
                    + belumLengkap.size();
            this.belumLengkap = new ArrayList<>(belumLengkap);
            this.perluDiperbaiki = new ArrayList<>(perluDiperbaiki);
            this.statusKelayakan = layak ? "MEMENUHI SYARAT"
                    : !perluDiperbaiki.isEmpty() ? "TIDAK MEMENUHI SYARAT" : "BELUM LENGKAP";
        }

        public String getBidang() { return bidang; }
        public int getSkor() { return skor; }
        public List<String> getAlasan() { return new ArrayList<>(alasan); }
        public List<String> getMataKuliahPendukung() { return new ArrayList<>(mataKuliahPendukung); }
        public double getRataRata() { return rataRata; }
        public boolean isLayak() { return layak; }
        public int getJumlahMataKuliahLulus() { return jumlahMataKuliahLulus; }
        public int getJumlahMataKuliahPendukung() { return jumlahMataKuliahPendukung; }
        public double getRasioMataKuliahLulus() {
            return jumlahMataKuliahPendukung == 0 ? 0d
                    : (double) jumlahMataKuliahLulus / jumlahMataKuliahPendukung;
        }
        public List<String> getBelumLengkap() { return new ArrayList<>(belumLengkap); }
        public List<String> getPerluDiperbaiki() { return new ArrayList<>(perluDiperbaiki); }
        public String getStatusKelayakan() { return statusKelayakan; }
    }

    public static class HasilRekomendasi implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String nim;
        private final LocalDate tanggal;
        private final List<SkorBidang> skor;
        private final boolean sementara;

        public HasilRekomendasi(String nim, LocalDate tanggal, List<SkorBidang> skor, boolean sementara) {
            this.nim = nim;
            this.tanggal = tanggal;
            this.skor = new ArrayList<>(skor);
            this.sementara = sementara;
        }
        public String getNim() { return nim; }
        public LocalDate getTanggal() { return tanggal; }
        public List<SkorBidang> getSkor() { return new ArrayList<>(skor); }
        public boolean isSementara() { return sementara; }
    }
}
