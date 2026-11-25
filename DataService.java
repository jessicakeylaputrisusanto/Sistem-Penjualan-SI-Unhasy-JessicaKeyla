import java.sql.*;
import java.util.Arrays;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DataService {
    private static final Path FILE_DATA = Paths.get(
            System.getProperty("user.home"), ".fti-unhasy", "data.ser");
    private static final String[] NAMA_MAHASISWA_DUMMY = {
        "Anisa Pertiwi", "Bima Aditya", "Citra Lestari",
        "Dimas Pratama", "Eka Safitri", "Fajar Nugroho",
        "Gita Maharani", "Hendra Saputra", "Intan Permata",
        "Joko Firmansyah", "Kartika Dewi", "Lukman Hakim",
        "Maya Anggraini", "Nanda Putra", "Olivia Rahma",
        "Putra Wibowo", "Qori Azzahra", "Rafi Kurniawan",
        "Salsabila Fitri", "Tegar Ramadhan", "Zahra Nabila",
        "Aulia Rahman", "Bagas Prakoso", "Dewi Anggraini",
        "Farhan Maulana", "Indah Permata", "Kevin Saputra",
        "Nadia Putri", "Rizky Firmansyah", "Shinta Maharani",
        "Yoga Pratama", "Yusuf Pratama", "Laras Kusuma"
    };
    private static final String[] JENIS_KELAMIN_DUMMY = {
        "Perempuan", "Laki-laki", "Perempuan", "Laki-laki", "Perempuan", "Laki-laki",
        "Perempuan", "Laki-laki", "Perempuan", "Laki-laki", "Perempuan", "Laki-laki",
        "Perempuan", "Laki-laki", "Perempuan", "Laki-laki", "Perempuan", "Laki-laki",
        "Perempuan", "Laki-laki", "Perempuan", "Laki-laki", "Perempuan",
        "Laki-laki", "Perempuan", "Laki-laki", "Perempuan", "Laki-laki",
        "Perempuan", "Laki-laki", "Perempuan", "Laki-laki", "Perempuan"
    };
    private static final String[] NILAI_DUMMY = {"A", "B", "C", "D", "E"};

    private final Map<String, Mahasiswa> mahasiswa = new LinkedHashMap<>();
    private final Map<String, Mahasiswa> mahasiswaSimulasi = new LinkedHashMap<>();
    private final List<RiwayatPeminatan> riwayat = new ArrayList<>();
    private final List<RiwayatPeminatan> riwayatSimulasi = new ArrayList<>();
    private final Map<String, RekomendasiService.HasilRekomendasi> rekomendasi = new LinkedHashMap<>();
    private final List<NilaiMataKuliah> nilaiMataKuliah = new ArrayList<>();
    private final List<MataKuliah> kurikulum = new ArrayList<>();
    private final Map<String, BidangPeminatan> bidangPeminatan = new LinkedHashMap<>();
    private final List<RiwayatPenjurusan> penjurusan = new ArrayList<>();
    private boolean riwayatSimulasiDiinisialisasi;
    private boolean dataAkademikDimigrasi;
    private boolean kurikulumSimulasiDimigrasi;
    private boolean dataDummyDiinisialisasi;
    private boolean dataDummyRasioSemester7Dimigrasi;

    public DataService() throws IOException, ClassNotFoundException {
        muatData();
        if (kurikulum.isEmpty()) kurikulum.addAll(KurikulumService.semuaMataKuliah());
        boolean perluSimpan = bersihkanDataSimulasi();
        perluSimpan |= migrasikanBidangPeminatan();
        if (!dataAkademikDimigrasi) {
            dataAkademikDimigrasi = true;
            perluSimpan = true;
        }
        if (!kurikulumSimulasiDimigrasi) {
            kurikulumSimulasiDimigrasi = true;
            perluSimpan = true;
        }
        perluSimpan |= inisialisasiDataDummy();
        if (!dataDummyDiinisialisasi) {
            dataDummyDiinisialisasi = true;
            perluSimpan = true;
        }
        if (perluSimpan) simpanData();
    }

    private boolean inisialisasiDataDummy() {
        boolean berubah = false;
        boolean migrasiRasioSemester7 = !dataDummyRasioSemester7Dimigrasi;
        int indeksNama = 0;
        int indeksMataKuliah = 0;
        for (int semester = 1; semester <= 7; semester++) {
            int jumlahMahasiswa = semester == 7 ? 15 : 3;
            for (int nomor = 1; nomor <= jumlahMahasiswa; nomor++) {
                String nim = (semester * 1000 + nomor) + "";
                if (mahasiswa.containsKey(nim) || mahasiswaSimulasi.containsKey(nim)) {
                    indeksNama++;
                    continue;
                }

                Mahasiswa student = new Mahasiswa(nim, NAMA_MAHASISWA_DUMMY[indeksNama],
                        JENIS_KELAMIN_DUMMY[indeksNama], "Sistem Informasi", semester,
                        2026 - (semester + 1) / 2, "", "", "", "", "", "", "");
                mahasiswa.put(nim, student);
                berubah = true;

                for (MataKuliah course : semuaMataKuliah()) {
                    if (course.getSemester() > semester) continue;
                    String grade;
                    if (semester == 7 && nomor == 1) {
                        grade = new String[]{"A", "B", "C"}[
                                (indeksMataKuliah + nomor) % 3];
                    } else {
                        grade = NILAI_DUMMY[Math.floorMod(
                                indeksNama * 3 + indeksMataKuliah * 2 + semester,
                                NILAI_DUMMY.length)];
                    }
                    nilaiMataKuliah.add(new NilaiMataKuliah(nim, course.getKode(),
                            course.getNama(), course.getSemester(), course.getSks(), grade));
                    indeksMataKuliah++;
                }
                indeksNama++;
            }
        }

        if (migrasiRasioSemester7) {
            aturKelulusanDummySemester7();
            dataDummyRasioSemester7Dimigrasi = true;
            berubah = true;
        }

        for (int nomor = 1; nomor <= 15; nomor++) {
            String nim = (7000 + nomor) + "";
            Mahasiswa student = mahasiswa.get(nim);
            if (!adalahMahasiswaDummySemester7(nim)) continue;

            RiwayatPenjurusan pilihanTercatat = null;
            for (RiwayatPenjurusan item : penjurusan) {
                if (item.getNim().equals(nim)) {
                    pilihanTercatat = item;
                    break;
                }
            }

            String bidang = RekomendasiService.normalisasiBidang(student.getMinatAwal());
            if (bidang.isEmpty() && pilihanTercatat != null) {
                bidang = RekomendasiService.normalisasiBidang(pilihanTercatat.getBidang());
            }
            if (bidang.isEmpty()) bidang = bidangDummyTerbaik(nim);
            if (!bidang.equals(student.getMinatAwal())) {
                student.perbarui(student.getNama(), student.getJenisKelamin(),
                        student.getProgramStudi(), student.getSemester(),
                        student.getTahunAngkatan(), student.getAsalSekolah(),
                        student.getJurusanAsal(), student.getPengalamanTeknologi(),
                        bidang, student.getAlasanMinat(), student.getProyek(),
                        student.getFotoPath());
                berubah = true;
            }

            if (pilihanTercatat == null || migrasiRasioSemester7) {
                List<String> mataKuliahPendukung = buktiMataKuliahPendukung(nim, bidang);
                boolean layak = kelayakanPenjurusan(nim, bidang).isEmpty();
                penjurusan.removeIf(item -> item.getNim().equals(nim));
                penjurusan.add(new RiwayatPenjurusan(nim, bidang, LocalDate.now(),
                        mataKuliahPendukung, layak, false));
                berubah = true;
            }
            if (migrasiRasioSemester7) rekomendasi.remove(nim);
        }
        return berubah;
    }

    private void aturKelulusanDummySemester7() {
        Set<String> semuaKodePendukung = new HashSet<>();
        Set<String> kodeYangGagal = new HashSet<>();
        for (BidangPeminatan bidang : bidangPeminatan.values()) {
            semuaKodePendukung.addAll(bidang.getKodeMataKuliahPendukung());
            if (!bidang.getKodeMataKuliahPendukung().isEmpty()) {
                kodeYangGagal.add(bidang.getKodeMataKuliahPendukung().get(0));
            }
        }

        for (int nomor = 1; nomor <= 15; nomor++) {
            String nim = (7000 + nomor) + "";
            if (!adalahMahasiswaDummySemester7(nim)) continue;
            for (int i = 0; i < nilaiMataKuliah.size(); i++) {
                NilaiMataKuliah nilai = nilaiMataKuliah.get(i);
                if (!nilai.getNim().equals(nim)
                        || !semuaKodePendukung.contains(nilai.getKode())) continue;
                String grade = nilai.getNilaiHuruf();
                if (nomor <= 12) {
                    grade = new String[]{"A", "B", "C"}[
                            Math.floorMod(nomor + nilai.getKode().hashCode(), 3)];
                } else if (kodeYangGagal.contains(nilai.getKode())) {
                    grade = "D";
                }
                if (!grade.equals(nilai.getNilaiHuruf())) {
                    nilaiMataKuliah.set(i, new NilaiMataKuliah(nilai.getNim(),
                            nilai.getKode(), nilai.getNama(), nilai.getSemester(),
                            nilai.getSks(), grade, nilai.isTerverifikasi()));
                }
            }
        }
    }

    private boolean adalahMahasiswaDummySemester7(String nim) {
        int nomor = Integer.parseInt(nim) - 7000;
        if (nomor < 1 || nomor > 15) return false;
        Mahasiswa student = mahasiswa.get(nim);
        return student != null && student.getSemester() == 7
                && "Sistem Informasi".equals(student.getProgramStudi())
                && NAMA_MAHASISWA_DUMMY[17 + nomor].equals(student.getNama());
    }

    private String bidangDummyTerbaik(String nim) {
        Map<String, NilaiMataKuliah> nilai = nilaiTerbaru(nilaiTerverifikasiMahasiswa(nim));
        String bidangTerbaik = RekomendasiService.BIDANG.get(0);
        int nilaiLulusTerbaik = -1;
        int totalNilaiTerbaik = -1;
        for (String namaBidang : RekomendasiService.BIDANG) {
            BidangPeminatan bidang = bidangPeminatan.get(namaBidang);
            int jumlahLulus = 0;
            int totalNilai = 0;
            for (String kode : bidang.getKodeMataKuliahPendukung()) {
                NilaiMataKuliah catatan = nilai.get(kode);
                if (catatan == null) continue;
                if ("Lulus".equals(catatan.getStatusKelulusan())) jumlahLulus++;
                totalNilai += nilaiAngka(catatan.getNilaiHuruf());
            }
            if (jumlahLulus > nilaiLulusTerbaik
                    || (jumlahLulus == nilaiLulusTerbaik && totalNilai > totalNilaiTerbaik)) {
                bidangTerbaik = namaBidang;
                nilaiLulusTerbaik = jumlahLulus;
                totalNilaiTerbaik = totalNilai;
            }
        }
        return bidangTerbaik;
    }

    private List<String> buktiMataKuliahPendukung(String nim, String namaBidang) {
        List<String> bukti = new ArrayList<>();
        BidangPeminatan bidang = bidangPeminatan.get(namaBidang);
        Map<String, NilaiMataKuliah> nilai = nilaiTerbaru(nilaiTerverifikasiMahasiswa(nim));
        for (String kode : bidang.getKodeMataKuliahPendukung()) {
            NilaiMataKuliah catatan = nilai.get(kode);
            MataKuliah mataKuliah = cariMataKuliah(kode);
            String nama = mataKuliah == null ? kode : mataKuliah.getNama();
            if (catatan == null) {
                bukti.add(kode + " — " + nama + " (Belum Dinilai)");
            } else {
                bukti.add(kode + " — " + nama + " (" + catatan.getNilaiHuruf()
                        + ", " + catatan.getStatusKelulusan() + ")");
            }
        }
        return bukti;
    }

    private int nilaiAngka(String nilai) {
        switch (nilai) {
            case "A": return 4;
            case "B": return 3;
            case "C": return 2;
            case "D": return 1;
            default: return 0;
        }
    }

    private boolean migrasikanBidangPeminatan() {
        boolean changed = false;
        Map<String, BidangPeminatan> normalized = new LinkedHashMap<>();
        for (int i = 0; i < RekomendasiService.BIDANG.size(); i++) {
            String name = RekomendasiService.BIDANG.get(i);
            BidangPeminatan existing = bidangPeminatan.get(name);
            if (existing == null) {
                existing = new BidangPeminatan(name, KurikulumService.bidangPendukungDefault(i), false);
                changed = true;
            } else if (existing.isSimulasi()) {
                existing = new BidangPeminatan(name, KurikulumService.bidangPendukungDefault(i), false);
                changed = true;
            }
            normalized.put(name, existing);
        }
        if (bidangPeminatan.size() != normalized.size()
                || !bidangPeminatan.keySet().containsAll(normalized.keySet())) changed = true;
        bidangPeminatan.clear();
        bidangPeminatan.putAll(normalized);

        List<RiwayatPenjurusan> normalizedPlacements = new ArrayList<>();
        for (RiwayatPenjurusan item : penjurusan) {
            String normalizedName = RekomendasiService.normalisasiBidang(item.getBidang());
            if (!normalizedName.isEmpty() && !normalizedName.equals(item.getBidang())) {
                normalizedPlacements.add(new RiwayatPenjurusan(item.getNim(), normalizedName,
                        item.getTanggal(), item.getMataKuliahPendukung(), item.isLayak(), item.isSimulasi()));
                changed = true;
            } else {
                normalizedPlacements.add(item);
            }
        }
        if (changed) {
            penjurusan.clear();
            penjurusan.addAll(normalizedPlacements);
            rekomendasi.clear();
        }
        for (RiwayatPeminatan item : riwayat) {
            String normalizedName = RekomendasiService.normalisasiBidang(item.getBidang());
            if (!normalizedName.isEmpty() && !normalizedName.equals(item.getBidang())) {
                item.perbarui(normalizedName, item.getCatatan(), item.getTanggal());
                changed = true;
            }
        }
        if (changed) rekomendasi.clear();
        return changed;
    }

    private boolean bersihkanDataSimulasi() {
        Set<String> nimSimulasi = new HashSet<>(mahasiswaSimulasi.keySet());
        boolean berubah = !mahasiswaSimulasi.isEmpty() || !riwayatSimulasi.isEmpty()
                || !riwayatSimulasiDiinisialisasi;
        mahasiswaSimulasi.clear();
        riwayatSimulasi.clear();
        berubah |= nilaiMataKuliah.removeIf(item -> nimSimulasi.contains(item.getNim()));
        berubah |= penjurusan.removeIf(item -> item.isSimulasi()
                || nimSimulasi.contains(item.getNim()));
        for (String nim : nimSimulasi) {
            berubah |= rekomendasi.remove(nim) != null;
        }
        berubah |= bidangPeminatan.values().removeIf(BidangPeminatan::isSimulasi);
        riwayatSimulasiDiinisialisasi = true;
        return berubah;
    }

    private void muatData() throws IOException, ClassNotFoundException {
        try (Connection conn = KoneksiDB.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Muat Kurikulum
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM kurikulum ORDER BY semester, kode")) {
                while (rs.next()) {
                    kurikulum.add(new MataKuliah(
                            rs.getString("kode"), rs.getString("nama"),
                            rs.getInt("semester"), rs.getInt("sks"),
                            rs.getBoolean("is_simulasi"), rs.getBoolean("is_usulan")));
                }
            }

            // 2. Muat Mahasiswa
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM mahasiswa")) {
                while (rs.next()) {
                    Mahasiswa m = new Mahasiswa(
                            rs.getString("nim"), rs.getString("nama"),
                            rs.getString("jenis_kelamin"), rs.getString("program_studi"),
                            rs.getInt("semester"), rs.getInt("tahun_angkatan"),
                            rs.getString("asal_sekolah"), rs.getString("jurusan_asal"),
                            rs.getString("pengalaman_teknologi"), rs.getString("minat_awal"),
                            rs.getString("alasan_minat"), rs.getString("proyek"),
                            rs.getString("foto_path"));
                    mahasiswa.put(m.getNim(), m);
                }
            }

            // 3. Muat Nilai Mata Kuliah
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM nilai_mata_kuliah")) {
                while (rs.next()) {
                    nilaiMataKuliah.add(new NilaiMataKuliah(
                            rs.getString("nim"), rs.getString("kode"),
                            rs.getString("nama"), rs.getInt("semester"),
                            rs.getInt("sks"), rs.getString("nilai_huruf"),
                            rs.getBoolean("terverifikasi")));
                }
            }

            // 4. Muat Bidang Peminatan
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM bidang_peminatan")) {
                while (rs.next()) {
                    String raw = rs.getString("kode_pendukung");
                    List<String> kodes = (raw == null || raw.isEmpty())
                            ? new ArrayList<>()
                            : new ArrayList<>(Arrays.asList(raw.split(",")));
                    bidangPeminatan.put(rs.getString("nama"),
                            new BidangPeminatan(rs.getString("nama"), kodes, rs.getBoolean("is_simulasi")));
                }
            }

            // 5. Muat Penjurusan
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM penjurusan")) {
                while (rs.next()) {
                    String raw = rs.getString("mata_kuliah_pendukung");
                    List<String> bukti = (raw == null || raw.isEmpty())
                            ? new ArrayList<>()
                            : new ArrayList<>(Arrays.asList(raw.split("\\|\\|")));
                    Date tgl = rs.getDate("tanggal");
                    penjurusan.add(new RiwayatPenjurusan(
                            rs.getString("nim"), rs.getString("bidang"),
                            tgl != null ? tgl.toLocalDate() : LocalDate.now(),
                            bukti, rs.getBoolean("layak"), rs.getBoolean("is_simulasi")));
                }
            }

            // Jika data mahasiswa sudah ada di DB, tandai dummy sudah pernah diinisialisasi
            if (!mahasiswa.isEmpty()) {
                dataDummyDiinisialisasi = true;
                dataDummyRasioSemester7Dimigrasi = true;
                dataAkademikDimigrasi = true;
                kurikulumSimulasiDimigrasi = true;
            }
        } catch (SQLException e) {
            throw new IOException("Gagal memuat data dari MySQL: " + e.getMessage(), e);
        }
    }

    private void simpanData() throws IOException {
        try (Connection conn = KoneksiDB.getConnection()) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM penjurusan");
                stmt.executeUpdate("DELETE FROM nilai_mata_kuliah");
                stmt.executeUpdate("DELETE FROM bidang_peminatan");
                stmt.executeUpdate("DELETE FROM kurikulum");
                stmt.executeUpdate("DELETE FROM mahasiswa");
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO mahasiswa VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
                for (Mahasiswa m : mahasiswa.values()) {
                    ps.setString(1, m.getNim());
                    ps.setString(2, m.getNama());
                    ps.setString(3, m.getJenisKelamin());
                    ps.setString(4, m.getProgramStudi());
                    ps.setInt(5, m.getSemester());
                    ps.setInt(6, m.getTahunAngkatan());
                    ps.setString(7, m.getAsalSekolah());
                    ps.setString(8, m.getJurusanAsal());
                    ps.setString(9, m.getPengalamanTeknologi());
                    ps.setString(10, m.getMinatAwal());
                    ps.setString(11, m.getAlasanMinat());
                    ps.setString(12, m.getProyek());
                    ps.setString(13, m.getFotoPath());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO kurikulum VALUES (?,?,?,?,?,?)")) {
                for (MataKuliah mk : kurikulum) {
                    ps.setString(1, mk.getKode());
                    ps.setString(2, mk.getNama());
                    ps.setInt(3, mk.getSemester());
                    ps.setInt(4, mk.getSks());
                    ps.setBoolean(5, mk.isSimulasi());
                    ps.setBoolean(6, mk.isUsulan());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO nilai_mata_kuliah (nim,kode,nama,semester,sks,nilai_huruf,terverifikasi) VALUES (?,?,?,?,?,?,?)")) {
                for (NilaiMataKuliah n : nilaiMataKuliah) {
                    ps.setString(1, n.getNim());
                    ps.setString(2, n.getKode());
                    ps.setString(3, n.getNama());
                    ps.setInt(4, n.getSemester());
                    ps.setInt(5, n.getSks());
                    ps.setString(6, n.getNilaiHuruf());
                    ps.setBoolean(7, n.isTerverifikasi());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO bidang_peminatan VALUES (?,?,?)")) {
                for (BidangPeminatan b : bidangPeminatan.values()) {
                    ps.setString(1, b.getNama());
                    ps.setString(2, String.join(",", b.getKodeMataKuliahPendukung()));
                    ps.setBoolean(3, b.isSimulasi());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO penjurusan VALUES (?,?,?,?,?,?)")) {
                for (RiwayatPenjurusan p : penjurusan) {
                    ps.setString(1, p.getNim());
                    ps.setString(2, p.getBidang());
                    ps.setDate(3, Date.valueOf(p.getTanggal()));
                    ps.setString(4, String.join("||", p.getMataKuliahPendukung()));
                    ps.setBoolean(5, p.isLayak());
                    ps.setBoolean(6, p.isSimulasi());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            throw new IOException("Gagal menyimpan data ke MySQL: " + e.getMessage(), e);
        }
    }

    public List<Mahasiswa> semuaMahasiswa() {
        List<Mahasiswa> hasil = new ArrayList<>();
        for (Mahasiswa data : mahasiswa.values()) {
            if ("Sistem Informasi".equals(data.getProgramStudi())) hasil.add(data);
        }
        for (Mahasiswa data : mahasiswaSimulasi.values()) {
            if ("Sistem Informasi".equals(data.getProgramStudi())) hasil.add(data);
        }
        return hasil;
    }
    public Mahasiswa cariMahasiswa(String nim) {
        Mahasiswa dataAsli = mahasiswa.get(nim);
        return dataAsli != null ? dataAsli : mahasiswaSimulasi.get(nim);
    }
    public boolean isMahasiswaSimulasi(String nim) { return mahasiswaSimulasi.containsKey(nim); }
    public int jumlahMahasiswaAsli() {
        int count = 0;
        for (Mahasiswa student : mahasiswa.values()) {
            if ("Sistem Informasi".equals(student.getProgramStudi())) count++;
        }
        return count;
    }
    public int jumlahMahasiswaSimulasi() { return mahasiswaSimulasi.size(); }
    public int jumlahMataKuliah() { return kurikulum.size(); }
    public List<MataKuliah> semuaMataKuliah() { return new ArrayList<>(kurikulum); }
    public List<MataKuliah> mataKuliahSemester(int semester) {
        List<MataKuliah> hasil = new ArrayList<>();
        for (MataKuliah item : kurikulum) {
            if (item.getSemester() == semester) hasil.add(item);
        }
        return hasil;
    }
    public MataKuliah cariMataKuliah(String kode) {
        if (kode == null) return null;
        for (MataKuliah item : kurikulum) {
            if (item.getKode().equals(kode)) return item;
        }
        return null;
    }

    public void tambahMataKuliah(UserSession session, MataKuliah mataKuliah) throws IOException {
        requireAdmin(session);
        MataKuliah baru = validasiMataKuliah(mataKuliah);
        if (cariMataKuliah(baru.getKode()) != null) {
            throw new IllegalArgumentException("Kode mata kuliah sudah digunakan.");
        }
        kurikulum.add(baru);
        kurikulum.sort(java.util.Comparator.comparingInt(MataKuliah::getSemester)
                .thenComparing(MataKuliah::getKode));
        rekomendasi.clear();
        simpanData();
    }

    public void ubahMataKuliah(UserSession session, String kode, MataKuliah mataKuliah)
            throws IOException {
        requireAdmin(session);
        MataKuliah lama = cariMataKuliah(kode);
        if (lama == null) throw new IllegalArgumentException("Mata kuliah tidak ditemukan.");
        MataKuliah input = validasiMataKuliah(mataKuliah);
        if (!lama.getKode().equals(input.getKode())) {
            throw new IllegalArgumentException("Kode mata kuliah tidak dapat diubah.");
        }
        if (input.getSemester() > 6 && bidangPeminatan.values().stream()
                .anyMatch(bidang -> bidang.getKodeMataKuliahPendukung().contains(kode))) {
            throw new IllegalArgumentException(
                    "Hapus mata kuliah ini dari prasyarat peminatan sebelum memindahkannya ke semester 7 atau 8.");
        }
        for (NilaiMataKuliah grade : nilaiMataKuliah) {
            if (!grade.getKode().equals(kode)) continue;
            Mahasiswa student = cariMahasiswa(grade.getNim());
            if (student != null && input.getSemester() > student.getSemester()) {
                throw new IllegalArgumentException(
                        "Semester mata kuliah tidak boleh melebihi semester mahasiswa yang sudah memiliki nilai.");
            }
        }
        int index = kurikulum.indexOf(lama);
        kurikulum.set(index, new MataKuliah(input.getKode(), input.getNama(),
                input.getSemester(), input.getSks(), lama.isSimulasi(), lama.isUsulan()));
        for (int i = 0; i < nilaiMataKuliah.size(); i++) {
            NilaiMataKuliah grade = nilaiMataKuliah.get(i);
            if (grade.getKode().equals(kode)) {
                nilaiMataKuliah.set(i, new NilaiMataKuliah(grade.getNim(), kode,
                        input.getNama(), input.getSemester(), input.getSks(),
                        grade.getNilaiHuruf(), grade.isTerverifikasi()));
            }
        }
        kurikulum.sort(java.util.Comparator.comparingInt(MataKuliah::getSemester)
                .thenComparing(MataKuliah::getKode));
        rekomendasi.clear();
        simpanData();
    }

    public void hapusMataKuliah(UserSession session, String kode) throws IOException {
        requireAdmin(session);
        MataKuliah mataKuliah = cariMataKuliah(kode);
        if (mataKuliah == null) throw new IllegalArgumentException("Mata kuliah tidak ditemukan.");
        if (nilaiMataKuliah.stream().anyMatch(grade -> grade.getKode().equals(kode))) {
            throw new IllegalArgumentException(
                    "Mata kuliah yang memiliki riwayat nilai tidak dapat dihapus.");
        }
        if (bidangPeminatan.values().stream()
                .anyMatch(bidang -> bidang.getKodeMataKuliahPendukung().contains(kode))) {
            throw new IllegalArgumentException(
                    "Hapus mata kuliah ini dari prasyarat bidang peminatan sebelum menghapusnya.");
        }
        kurikulum.remove(mataKuliah);
        rekomendasi.clear();
        simpanData();
    }

    private MataKuliah validasiMataKuliah(MataKuliah data) {
        if (data == null) throw new IllegalArgumentException("Data mata kuliah wajib diisi.");
        String kode = data.getKode() == null ? "" : data.getKode().trim().toUpperCase(Locale.ROOT);
        String nama = data.getNama() == null ? "" : data.getNama().trim();
        if (!kode.matches("[A-Z0-9][A-Z0-9_-]{1,19}")) {
            throw new IllegalArgumentException(
                    "Kode mata kuliah hanya boleh berisi 2–20 huruf, angka, tanda hubung, atau garis bawah.");
        }
        if (nama.isEmpty() || nama.length() > 120) {
            throw new IllegalArgumentException("Nama mata kuliah wajib diisi dan maksimal 120 karakter.");
        }
        if (data.getSemester() < 1 || data.getSemester() > 8) {
            throw new IllegalArgumentException("Semester mata kuliah harus antara 1 dan 8.");
        }
        if (data.getSks() < 1 || data.getSks() > 24) {
            throw new IllegalArgumentException("Jumlah SKS harus antara 1 dan 24.");
        }
        return new MataKuliah(kode, nama, data.getSemester(), data.getSks(),
                data.isSimulasi(), data.isUsulan());
    }

    private void requireAdmin(UserSession session) {
        if (session == null || session.getRole() != UserSession.Role.ADMIN) {
            throw new IllegalArgumentException("Hanya Admin yang dapat mengelola kurikulum.");
        }
    }

    public String simpanFotoMahasiswa(String nim, String sumberFoto) throws IOException {
        if (sumberFoto == null || sumberFoto.trim().isEmpty()) return "";
        Path sumber = Paths.get(sumberFoto).toAbsolutePath().normalize();
        if (!Files.isRegularFile(sumber)) {
            throw new IOException("Berkas foto tidak ditemukan: " + sumberFoto);
        }
        Path direktoriFoto = FILE_DATA.getParent().resolve("photos");
        Files.createDirectories(direktoriFoto);
        String namaAman = nim.replaceAll("[^A-Za-z0-9._-]", "_");
        String namaFile = sumber.getFileName().toString();
        int titik = namaFile.lastIndexOf('.');
        String ekstensi = titik >= 0 ? namaFile.substring(titik).toLowerCase() : "";
        Path tujuan = direktoriFoto.resolve(namaAman + ekstensi).toAbsolutePath().normalize();
        if (!sumber.equals(tujuan)) {
            Files.copy(sumber, tujuan, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return tujuan.toString();
    }

    public List<RiwayatPeminatan> semuaRiwayat() {
        List<RiwayatPeminatan> hasil = new ArrayList<>(riwayat);
        hasil.addAll(riwayatSimulasi);
        return hasil;
    }
    public List<RiwayatPeminatan> riwayatMahasiswa(String nim) {
        List<RiwayatPeminatan> hasil = new ArrayList<>();
        for (RiwayatPeminatan item : riwayat) if (item.getNim().equals(nim)) hasil.add(item);
        for (RiwayatPeminatan item : riwayatSimulasi) if (item.getNim().equals(nim)) hasil.add(item);
        return hasil;
    }
    public boolean isRiwayatSimulasi(String nim, int semester) {
        for (RiwayatPeminatan item : riwayatSimulasi) {
            if (item.getNim().equals(nim) && item.getSemester() == semester) return true;
        }
        return false;
    }
    public RekomendasiService.HasilRekomendasi rekomendasiMahasiswa(String nim) {
        Mahasiswa student = cariMahasiswa(nim);
        if (student == null) return null;
        return new RekomendasiService().analisis(student, nilaiTerverifikasiMahasiswa(nim),
                semuaBidangPeminatan(), semuaMataKuliah());
    }

    public List<NilaiMataKuliah> semuaNilaiMataKuliah() {
        return new ArrayList<>(nilaiMataKuliah);
    }

    public List<NilaiMataKuliah> nilaiMahasiswa(String nim) {
        List<NilaiMataKuliah> hasil = new ArrayList<>();
        for (NilaiMataKuliah item : nilaiMataKuliah) {
            if (item.getNim().equals(nim)) hasil.add(item);
        }
        hasil.sort(java.util.Comparator.comparingInt(NilaiMataKuliah::getSemester)
                .thenComparing(NilaiMataKuliah::getKode));
        return hasil;
    }

    public List<NilaiMataKuliah> nilaiTerverifikasiMahasiswa(String nim) {
        List<NilaiMataKuliah> hasil = new ArrayList<>();
        for (NilaiMataKuliah item : nilaiMahasiswa(nim)) {
            if (item.isTerverifikasi()) hasil.add(item);
        }
        return hasil;
    }

    public Mahasiswa profilMahasiswa(UserSession session) {
        return salinMahasiswa(mahasiswaUntukSesi(session));
    }

    public List<MataKuliah> mataKuliahMahasiswa(UserSession session) {
        Mahasiswa mahasiswa = mahasiswaUntukSesi(session);
        List<MataKuliah> hasil = new ArrayList<>();
        for (MataKuliah mataKuliah : semuaMataKuliah()) {
            if (mataKuliah.getSemester() <= mahasiswa.getSemester()) hasil.add(mataKuliah);
        }
        return hasil;
    }

    public List<NilaiMataKuliah> nilaiMahasiswa(UserSession session) {
        Mahasiswa mahasiswa = mahasiswaUntukSesi(session);
        return nilaiMahasiswa(mahasiswa.getNim());
    }

    public List<RiwayatPenjurusan> riwayatPenjurusanMahasiswa(UserSession session) {
        Mahasiswa mahasiswa = mahasiswaUntukSesi(session);
        List<RiwayatPenjurusan> hasil = new ArrayList<>();
        for (RiwayatPenjurusan item : penjurusan) {
            if (item.getNim().equals(mahasiswa.getNim())) hasil.add(item);
        }
        return hasil;
    }

    public RekomendasiService.HasilRekomendasi rekomendasiMahasiswa(UserSession session) {
        Mahasiswa mahasiswa = mahasiswaUntukSesi(session);
        return new RekomendasiService().analisis(mahasiswa,
                nilaiTerverifikasiMahasiswa(mahasiswa.getNim()), semuaBidangPeminatan(),
                semuaMataKuliah());
    }

    public void ajukanNilaiMahasiswa(UserSession session, String kode, String nilaiHuruf)
            throws IOException {
        Mahasiswa mahasiswa = mahasiswaUntukSesi(session);
        MataKuliah mataKuliah = cariMataKuliah(kode);
        if (mataKuliah == null || mataKuliah.getSemester() > mahasiswa.getSemester()) {
            throw new IllegalArgumentException("Mata kuliah tidak tersedia untuk semester mahasiswa ini.");
        }
        NilaiMataKuliah pengajuan = new NilaiMataKuliah(mahasiswa.getNim(),
                mataKuliah.getKode(), mataKuliah.getNama(), mataKuliah.getSemester(),
                mataKuliah.getSks(), nilaiHuruf, false);
        int index = -1;
        for (int i = 0; i < nilaiMataKuliah.size(); i++) {
            NilaiMataKuliah existing = nilaiMataKuliah.get(i);
            if (existing.getNim().equals(mahasiswa.getNim())
                    && existing.getKode().equals(mataKuliah.getKode())
                    && existing.getSemester() == mataKuliah.getSemester()) {
                index = i;
                break;
            }
        }
        if (index < 0) nilaiMataKuliah.add(pengajuan);
        else nilaiMataKuliah.set(index, pengajuan);
        rekomendasi.remove(mahasiswa.getNim());
        simpanData();
    }

    public void pilihBidangMahasiswa(UserSession session, String bidang) throws IOException {
        Mahasiswa mahasiswa = mahasiswaUntukSesi(session);
        if (!RekomendasiService.BIDANG.contains(bidang)) {
            throw new IllegalArgumentException("Bidang peminatan tidak dikenal.");
        }
        Mahasiswa updated = salinMahasiswa(mahasiswa);
        updated.perbarui(updated.getNama(), updated.getJenisKelamin(),
                updated.getProgramStudi(), updated.getSemester(), updated.getTahunAngkatan(),
                updated.getAsalSekolah(), updated.getJurusanAsal(),
                updated.getPengalamanTeknologi(), bidang, updated.getAlasanMinat(),
                updated.getProyek(), updated.getFotoPath());
        this.mahasiswa.put(updated.getNim(), updated);
        simpanData();
    }

    public void verifikasiNilai(UserSession session, String nim, String kode, int semester)
            throws IOException {
        if (session == null || session.getRole() != UserSession.Role.ADMIN) {
            throw new IllegalArgumentException("Hanya Admin yang dapat memverifikasi nilai.");
        }
        for (int i = 0; i < nilaiMataKuliah.size(); i++) {
            NilaiMataKuliah existing = nilaiMataKuliah.get(i);
            if (existing.getNim().equals(nim) && existing.getKode().equals(kode)
                    && existing.getSemester() == semester) {
                if (existing.isTerverifikasi()) {
                    throw new IllegalArgumentException("Nilai ini sudah terverifikasi.");
                }
                nilaiMataKuliah.set(i, new NilaiMataKuliah(existing.getNim(),
                        existing.getKode(), existing.getNama(), existing.getSemester(),
                        existing.getSks(), existing.getNilaiHuruf(), true));
                rekomendasi.remove(nim);
                simpanData();
                return;
            }
        }
        throw new IllegalArgumentException("Pengajuan nilai tidak ditemukan.");
    }

    private Mahasiswa mahasiswaUntukSesi(UserSession session) {
        if (session == null || session.getRole() != UserSession.Role.MAHASISWA
                || session.getNim().trim().isEmpty()
                || !session.getNim().equals(session.getUsername())) {
            throw new IllegalArgumentException("Sesi Mahasiswa yang valid wajib disediakan.");
        }
        Mahasiswa mahasiswa = this.mahasiswa.get(session.getNim());
        if (mahasiswa == null || !"Sistem Informasi".equals(mahasiswa.getProgramStudi())) {
            throw new IllegalArgumentException("Data mahasiswa pada sesi login tidak ditemukan.");
        }
        return mahasiswa;
    }

    private Mahasiswa salinMahasiswa(Mahasiswa sumber) {
        return new Mahasiswa(sumber.getNim(), sumber.getNama(), sumber.getJenisKelamin(),
                sumber.getProgramStudi(), sumber.getSemester(), sumber.getTahunAngkatan(),
                sumber.getAsalSekolah(), sumber.getJurusanAsal(),
                sumber.getPengalamanTeknologi(), sumber.getMinatAwal(),
                sumber.getAlasanMinat(), sumber.getProyek(), sumber.getFotoPath());
    }

    public NilaiMataKuliah nilaiMahasiswa(String nim, String kode) {
        NilaiMataKuliah hasil = null;
        for (NilaiMataKuliah item : nilaiMataKuliah) {
            if (item.getNim().equals(nim) && item.getKode().equals(kode)) hasil = item;
        }
        return hasil;
    }

    public List<BidangPeminatan> semuaBidangPeminatan() {
        return new ArrayList<>(bidangPeminatan.values());
    }

    public BidangPeminatan bidangPeminatan(String nama) {
        return bidangPeminatan.get(nama);
    }

    public List<RiwayatPenjurusan> semuaPenjurusan() {
        return new ArrayList<>(penjurusan);
    }

    public RiwayatPenjurusan penjurusanMahasiswa(String nim) {
        for (RiwayatPenjurusan item : penjurusan) {
            if (item.getNim().equals(nim)) return item;
        }
        return null;
    }

    public List<String> kelayakanPenjurusan(String nim, String bidang) {
        List<String> kekurangan = new ArrayList<>();
        Mahasiswa data = cariMahasiswa(nim);
        if (data == null) {
            kekurangan.add("Data mahasiswa tidak ditemukan.");
            return kekurangan;
        }
        if (!"Sistem Informasi".equals(data.getProgramStudi())) {
            kekurangan.add("Penjurusan hanya tersedia untuk Program Studi Sistem Informasi.");
            return kekurangan;
        }
        if (data.getSemester() != 7) kekurangan.add("Penjurusan hanya dapat dicatat pada semester 7.");
        List<NilaiMataKuliah> nilai = nilaiTerverifikasiMahasiswa(nim);
        Map<String, NilaiMataKuliah> percobaanTerakhir = nilaiTerbaru(nilai);
        String normalizedBidang = RekomendasiService.normalisasiBidang(bidang);
        BidangPeminatan pilihan = bidangPeminatan.get(normalizedBidang);
        if (pilihan == null) {
            kekurangan.add("Bidang peminatan belum memiliki persyaratan.");
            return kekurangan;
        }
        if (pilihan.getKodeMataKuliahPendukung().isEmpty()) {
            kekurangan.add("Daftar mata kuliah prasyarat bidang ini masih kosong.");
            return kekurangan;
        }
        for (String kode : pilihan.getKodeMataKuliahPendukung()) {
            NilaiMataKuliah catatan = percobaanTerakhir.get(kode);
            MataKuliah course = cariMataKuliah(kode);
            String nama = course == null ? kode : course.getNama();
            if (catatan == null || catatan.getNilaiHuruf().isEmpty()) {
                kekurangan.add(kode + " — " + nama + " (belum memiliki nilai).");
            }
            else if (!"Lulus".equals(catatan.getStatusKelulusan())) {
                kekurangan.add(kode + " — " + nama + " (nilai " + catatan.getNilaiHuruf()
                        + ", harus diperbaiki/diulang).");
            }
        }
        return kekurangan;
    }

    public String statusKelayakanPenjurusan(String nim, String bidang) {
        List<String> kekurangan = kelayakanPenjurusan(nim, bidang);
        if (kekurangan.isEmpty()) return "MEMENUHI SYARAT";
        for (String alasan : kekurangan) {
            if (alasan.contains("harus diperbaiki/diulang")
                    || alasan.startsWith("Penjurusan hanya")) {
                return "TIDAK MEMENUHI SYARAT";
            }
        }
        return "BELUM LENGKAP";
    }

    public int jumlahMahasiswaLayakPenjurusan() {
        int jumlah = 0;
        for (Mahasiswa data : semuaMahasiswa()) {
            if (data.getSemester() == 7) {
                for (String bidang : RekomendasiService.BIDANG) {
                    if (kelayakanPenjurusan(data.getNim(), bidang).isEmpty()) {
                        jumlah++;
                        break;
                    }
                }
            }
        }
        return jumlah;
    }

    public int jumlahMahasiswaBelumLayakPenjurusan() {
        int jumlah = 0;
        for (Mahasiswa data : semuaMahasiswa()) {
            if (data.getSemester() == 7) {
                boolean layak = false;
                for (String bidang : RekomendasiService.BIDANG) {
                    if (kelayakanPenjurusan(data.getNim(), bidang).isEmpty()) {
                        layak = true;
                        break;
                    }
                }
                if (!layak) jumlah++;
            }
        }
        return jumlah;
    }

    public void simpanNilai(NilaiMataKuliah data) throws IOException {
        Mahasiswa student = cariMahasiswa(data.getNim());
        if (student == null) throw new IllegalArgumentException("Mahasiswa tidak ditemukan.");
        if (!"Sistem Informasi".equals(student.getProgramStudi())) {
            throw new IllegalArgumentException("Nilai hanya dapat dikelola untuk Program Studi Sistem Informasi.");
        }
        if (isMahasiswaSimulasi(data.getNim())) {
            throw new IllegalArgumentException("Nilai data simulasi hanya-baca.");
        }
        validasiNilaiKurikulum(data);
        for (NilaiMataKuliah item : nilaiMataKuliah) {
            if (item.getNim().equals(data.getNim()) && item.getKode().equals(data.getKode())
                    && item.getSemester() == data.getSemester()) {
                throw new IllegalArgumentException("Nilai mata kuliah dengan kode dan semester tersebut sudah tersedia.");
            }
        }
        nilaiMataKuliah.add(data);
        rekomendasi.remove(data.getNim());
        simpanData();
    }

    public void ubahNilai(String nimLama, String kodeLama, NilaiMataKuliah data) throws IOException {
        ubahNilai(nimLama, kodeLama, -1, data);
    }

    public void ubahNilai(String nimLama, String kodeLama, int semesterLama,
                          NilaiMataKuliah data) throws IOException {
        if (isMahasiswaSimulasi(nimLama) || isMahasiswaSimulasi(data.getNim())) {
            throw new IllegalArgumentException("Nilai data simulasi hanya-baca.");
        }
        Mahasiswa student = cariMahasiswa(data.getNim());
        if (student == null) throw new IllegalArgumentException("Mahasiswa tidak ditemukan.");
        if (!"Sistem Informasi".equals(student.getProgramStudi())) {
            throw new IllegalArgumentException("Nilai hanya dapat dikelola untuk Program Studi Sistem Informasi.");
        }
        validasiNilaiKurikulum(data);
        int index = -1;
        for (int i = 0; i < nilaiMataKuliah.size(); i++) {
            NilaiMataKuliah item = nilaiMataKuliah.get(i);
            if (item.getNim().equals(nimLama) && item.getKode().equals(kodeLama)
                    && (semesterLama < 0 || item.getSemester() == semesterLama)) index = i;
            else if (item.getNim().equals(data.getNim()) && item.getKode().equals(data.getKode())
                    && item.getSemester() == data.getSemester()) {
                throw new IllegalArgumentException("Nilai mata kuliah dengan kode dan semester tersebut sudah tersedia.");
            }
        }
        if (index < 0) throw new IllegalArgumentException("Nilai mata kuliah tidak ditemukan.");
        nilaiMataKuliah.set(index, data);
        rekomendasi.remove(nimLama);
        rekomendasi.remove(data.getNim());
        simpanData();
    }

    public void hapusNilai(String nim, String kode) throws IOException {
        hapusNilai(nim, kode, -1);
    }

    public void hapusNilai(String nim, String kode, int semester) throws IOException {
        if (isMahasiswaSimulasi(nim)) throw new IllegalArgumentException("Nilai data simulasi hanya-baca.");
        boolean removed = nilaiMataKuliah.removeIf(item -> item.getNim().equals(nim)
                && item.getKode().equals(kode) && (semester < 0 || item.getSemester() == semester));
        if (!removed) throw new IllegalArgumentException("Nilai mata kuliah tidak ditemukan.");
        rekomendasi.remove(nim);
        simpanData();
    }

    public void simpanBidangPeminatan(UserSession session, BidangPeminatan bidang)
            throws IOException {
        requireAdmin(session);
        if (!RekomendasiService.BIDANG.contains(bidang.getNama())) {
            throw new IllegalArgumentException("Bidang peminatan tidak dikenal.");
        }
        if (bidang.getKodeMataKuliahPendukung().isEmpty()) {
            throw new IllegalArgumentException("Pilih sekurang-kurangnya satu mata kuliah pendukung.");
        }
        Set<String> kodeUnik = new HashSet<>();
        for (String kode : bidang.getKodeMataKuliahPendukung()) {
            if (!kodeUnik.add(kode)) throw new IllegalArgumentException("Kode mata kuliah pendukung tidak boleh ganda.");
            MataKuliah course = cariMataKuliah(kode);
            if (course == null || course.getSemester() < 1 || course.getSemester() > 6) {
                throw new IllegalArgumentException("Prasyarat bidang harus berupa mata kuliah kurikulum semester 1–6.");
            }
        }
        bidangPeminatan.put(bidang.getNama(), new BidangPeminatan(
                bidang.getNama(), bidang.getKodeMataKuliahPendukung(), false));
        rekomendasi.clear();
        simpanData();
    }

    private void validasiNilaiKurikulum(NilaiMataKuliah data) {
        if (data.getNilaiHuruf().isEmpty()) {
            throw new IllegalArgumentException("Nilai A, B, C, D, atau E harus diisi sebelum disimpan.");
        }
        MataKuliah mataKuliah = cariMataKuliah(data.getKode());
        if (mataKuliah == null || mataKuliah.getSemester() != data.getSemester()
                || mataKuliah.getSks() != data.getSks() || !mataKuliah.getNama().equals(data.getNama())) {
            throw new IllegalArgumentException("Pilih mata kuliah dari daftar kurikulum semester 1–8.");
        }
        Mahasiswa student = cariMahasiswa(data.getNim());
        if (mataKuliah.getSemester() > student.getSemester()) {
            throw new IllegalArgumentException("Semester mata kuliah tidak boleh melebihi semester mahasiswa.");
        }
    }

    public void simpanPenjurusan(String nim, String bidang, LocalDate tanggal) throws IOException {
        if (isMahasiswaSimulasi(nim)) throw new IllegalArgumentException("Penjurusan data simulasi hanya-baca.");
        if (tanggal == null) throw new IllegalArgumentException("Tanggal penjurusan wajib diisi.");
        String normalizedBidang = RekomendasiService.normalisasiBidang(bidang);
        if (normalizedBidang.isEmpty()) throw new IllegalArgumentException("Bidang peminatan tidak dikenal.");
        List<String> kekurangan = kelayakanPenjurusan(nim, normalizedBidang);
        if (!kekurangan.isEmpty()) {
            throw new IllegalArgumentException("Mahasiswa belum memenuhi persyaratan:\n• "
                    + String.join("\n• ", kekurangan));
        }
        List<String> pendukung = new ArrayList<>();
        for (String kode : bidangPeminatan.get(normalizedBidang).getKodeMataKuliahPendukung()) {
            NilaiMataKuliah item = nilaiTerbaru(nilaiMahasiswa(nim)).get(kode);
            if (item != null) {
                pendukung.add(item.getKode() + " — " + item.getNama() + " ("
                        + item.getNilaiHuruf() + ", Lulus)");
            }
        }
        RiwayatPenjurusan record = new RiwayatPenjurusan(
                nim, normalizedBidang, tanggal, pendukung, true, false);
        penjurusan.removeIf(item -> item.getNim().equals(nim));
        penjurusan.add(record);
        simpanData();
    }

    private Map<String, NilaiMataKuliah> nilaiTerbaru(List<NilaiMataKuliah> nilai) {
        Map<String, NilaiMataKuliah> hasil = new LinkedHashMap<>();
        for (NilaiMataKuliah item : nilai) {
            NilaiMataKuliah existing = hasil.get(item.getKode());
            if (existing == null || item.getSemester() >= existing.getSemester()) {
                hasil.put(item.getKode(), item);
            }
        }
        return hasil;
    }

    public void tambahMahasiswa(Mahasiswa data) throws IOException {
        if (!"Sistem Informasi".equals(data.getProgramStudi())) {
            throw new IllegalArgumentException("Sistem ini hanya melayani Program Studi Sistem Informasi.");
        }
        if (cariMahasiswa(data.getNim()) != null) throw new IllegalArgumentException("NIM sudah terdaftar.");
        mahasiswa.put(data.getNim(), data);
        simpanData();
    }

    public void ubahMahasiswa(Mahasiswa data) throws IOException {
        if (!"Sistem Informasi".equals(data.getProgramStudi())) {
            throw new IllegalArgumentException("Sistem ini hanya melayani Program Studi Sistem Informasi.");
        }
        if (isMahasiswaSimulasi(data.getNim())) {
            throw new IllegalArgumentException("Data simulasi bersifat hanya-baca. Tambahkan data asli dengan NIM berbeda.");
        }
        if (!mahasiswa.containsKey(data.getNim())) throw new IllegalArgumentException("Mahasiswa tidak ditemukan.");
        for (NilaiMataKuliah nilai : nilaiMataKuliah) {
            if (nilai.getNim().equals(data.getNim()) && nilai.getSemester() > data.getSemester()) {
                throw new IllegalArgumentException("Semester mahasiswa tidak boleh lebih rendah dari riwayat mata kuliah yang tersimpan.");
            }
        }
        mahasiswa.put(data.getNim(), data);
        rekomendasi.remove(data.getNim());
        simpanData();
    }

    public void hapusMahasiswa(String nim) throws IOException {
        if (isMahasiswaSimulasi(nim)) {
            throw new IllegalArgumentException("Data simulasi bersifat hanya-baca dan tidak dapat dihapus.");
        }
        mahasiswa.remove(nim);
        riwayat.removeIf(item -> item.getNim().equals(nim));
        nilaiMataKuliah.removeIf(item -> item.getNim().equals(nim));
        penjurusan.removeIf(item -> item.getNim().equals(nim));
        rekomendasi.remove(nim);
        simpanData();
    }

    public void simpanRiwayat(RiwayatPeminatan data) throws IOException {
        if (isMahasiswaSimulasi(data.getNim())) {
            throw new IllegalArgumentException("Data riwayat simulasi hanya-baca. Riwayat mahasiswa simulasi tidak dapat diubah.");
        }
        for (RiwayatPeminatan item : riwayat) {
            if (item.getNim().equals(data.getNim()) && item.getSemester() == data.getSemester()) {
                item.perbarui(data.getBidang(), data.getCatatan(), data.getTanggal());
                rekomendasi.remove(data.getNim());
                simpanData();
                return;
            }
        }
        riwayat.add(data);
        rekomendasi.remove(data.getNim());
        simpanData();
    }

    public void ubahRiwayat(String nimLama, int semesterLama, RiwayatPeminatan data) throws IOException {
        if (isMahasiswaSimulasi(nimLama) || isMahasiswaSimulasi(data.getNim())) {
            throw new IllegalArgumentException("Data riwayat simulasi hanya-baca.");
        }
        int indeksLama = -1;
        for (int i = 0; i < riwayat.size(); i++) {
            RiwayatPeminatan item = riwayat.get(i);
            if (item.getNim().equals(nimLama) && item.getSemester() == semesterLama) {
                indeksLama = i;
                break;
            }
        }
        if (indeksLama < 0) throw new IllegalArgumentException("Riwayat yang akan diubah tidak ditemukan.");
        for (int i = 0; i < riwayat.size(); i++) {
            RiwayatPeminatan item = riwayat.get(i);
            if (i != indeksLama && item.getNim().equals(data.getNim())
                    && item.getSemester() == data.getSemester()) {
                throw new IllegalArgumentException("Riwayat mahasiswa pada semester tujuan sudah tersedia.");
            }
        }
        riwayat.set(indeksLama, data);
        rekomendasi.remove(nimLama);
        rekomendasi.remove(data.getNim());
        simpanData();
    }

    public void hapusRiwayat(String nim, int semester) throws IOException {
        if (isMahasiswaSimulasi(nim)) {
            throw new IllegalArgumentException("Data riwayat simulasi hanya-baca.");
        }
        riwayat.removeIf(item -> item.getNim().equals(nim) && item.getSemester() == semester);
        rekomendasi.remove(nim);
        simpanData();
    }

    public void simpanRekomendasi(String nim, RekomendasiService.HasilRekomendasi hasil) throws IOException {
        Mahasiswa student = cariMahasiswa(nim);
        if (student == null || !"Sistem Informasi".equals(student.getProgramStudi())) {
            throw new IllegalArgumentException("Rekomendasi hanya dapat disimpan untuk mahasiswa Sistem Informasi.");
        }
        if (hasil == null || !nim.equals(hasil.getNim())) {
            throw new IllegalArgumentException("Hasil rekomendasi tidak sesuai dengan NIM mahasiswa.");
        }
        rekomendasi.put(nim, hasil);
        simpanData();
    }

    private static class DataTersimpan implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Map<String, Mahasiswa> mahasiswa;
        private final Map<String, Mahasiswa> mahasiswaSimulasi;
        private final List<RiwayatPeminatan> riwayat;
        private final List<RiwayatPeminatan> riwayatSimulasi;
        private final boolean riwayatSimulasiDiinisialisasi;
        private final Map<String, RekomendasiService.HasilRekomendasi> rekomendasi;
        private final List<NilaiMataKuliah> nilaiMataKuliah;
        private final List<MataKuliah> kurikulum;
        private final Map<String, BidangPeminatan> bidangPeminatan;
        private final List<RiwayatPenjurusan> penjurusan;
        private final boolean dataAkademikDimigrasi;
        private final boolean kurikulumSimulasiDimigrasi;
        private final boolean dataDummyDiinisialisasi;
        private final boolean dataDummyRasioSemester7Dimigrasi;

        private DataTersimpan(Map<String, Mahasiswa> mahasiswa, Map<String, Mahasiswa> mahasiswaSimulasi,
                              List<RiwayatPeminatan> riwayat,
                              List<RiwayatPeminatan> riwayatSimulasi,
                              boolean riwayatSimulasiDiinisialisasi,
                              Map<String, RekomendasiService.HasilRekomendasi> rekomendasi,
                              List<NilaiMataKuliah> nilaiMataKuliah,
                              List<MataKuliah> kurikulum,
                              Map<String, BidangPeminatan> bidangPeminatan,
                              List<RiwayatPenjurusan> penjurusan,
                              boolean dataAkademikDimigrasi,
                              boolean kurikulumSimulasiDimigrasi,
                              boolean dataDummyDiinisialisasi,
                              boolean dataDummyRasioSemester7Dimigrasi) {
            this.mahasiswa = new LinkedHashMap<>(mahasiswa);
            this.mahasiswaSimulasi = new LinkedHashMap<>(mahasiswaSimulasi);
            this.riwayat = new ArrayList<>(riwayat);
            this.riwayatSimulasi = new ArrayList<>(riwayatSimulasi);
            this.riwayatSimulasiDiinisialisasi = riwayatSimulasiDiinisialisasi;
            this.rekomendasi = new LinkedHashMap<>(rekomendasi);
            this.nilaiMataKuliah = new ArrayList<>(nilaiMataKuliah);
            this.kurikulum = new ArrayList<>(kurikulum);
            this.bidangPeminatan = new LinkedHashMap<>(bidangPeminatan);
            this.penjurusan = new ArrayList<>(penjurusan);
            this.dataAkademikDimigrasi = dataAkademikDimigrasi;
            this.kurikulumSimulasiDimigrasi = kurikulumSimulasiDimigrasi;
            this.dataDummyDiinisialisasi = dataDummyDiinisialisasi;
            this.dataDummyRasioSemester7Dimigrasi = dataDummyRasioSemester7Dimigrasi;
        }
    }
}