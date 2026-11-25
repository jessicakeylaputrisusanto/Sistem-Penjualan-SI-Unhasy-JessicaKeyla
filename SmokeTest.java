import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class SmokeTest {
    public static void main(String[] args) throws Exception {
        Path home = Files.createTempDirectory("fti-si-smoke");
        try {
            System.setProperty("user.home", home.toString());
            DataService data = new DataService();
            verifikasiDataDummy(data);
            require(data.semuaRiwayat().isEmpty(), "Tidak boleh ada riwayat contoh saat mulai");
            require(data.semuaPenjurusan().size() == 15
                    && data.semuaPenjurusan().stream().filter(item ->
                            item.getNim().startsWith("7")).count() == 15,
                    "Semua mahasiswa dummy semester 7 harus memiliki bidang tetap");
            require(data.semuaBidangPeminatan().size() == 3,
                    "Tiga konfigurasi awal bidang harus tersedia");
            require(data.jumlahMahasiswaLayakPenjurusan()
                    + data.jumlahMahasiswaBelumLayakPenjurusan() == 15,
                    "Statistik penjurusan harus mencakup semua mahasiswa semester 7");
            require(data.jumlahMahasiswaLayakPenjurusan() == 12
                    && data.jumlahMahasiswaBelumLayakPenjurusan() == 3,
                    "Kelayakan 15 mahasiswa dummy semester 7 harus tepat 80:20");
            require(data.jumlahMataKuliah() == 55
                    && KurikulumService.semuaMataKuliah().stream().noneMatch(MataKuliah::isSimulasi),
                    "Kurikulum tetap tersedia tanpa mata kuliah dummy");
            require(KurikulumService.cari("SI1-01").getNama().equals("Pancasila")
                    && KurikulumService.cari("SI5-01").getSks() == 20
                    && KurikulumService.cari("SI8-07").getNama().equals("Tugas Akhir / Skripsi"),
                    "Data kurikulum harus tetap lengkap");
            verifikasiFilterMahasiswa();

            require(RekomendasiService.BIDANG.equals(Arrays.asList("UI/UX Design",
                    "Manajemen Proyek Sistem Informasi",
                    "Audit dan Tata Kelola Sistem Informasi")),
                    "Nama bidang harus sesuai dengan tiga peminatan terbaru");
            require(KurikulumService.cari("SI6-U01").isUsulan()
                    && KurikulumService.cari("SI6-U02").isUsulan(),
                    "Mata kuliah baru semester 6 harus diberi identitas sementara dan label usulan");

            String uiux = RekomendasiService.BIDANG.get(0);
            Mahasiswa student = new Mahasiswa("SI-REAL-01", "Mahasiswa Asli", "",
                    "Sistem Informasi", 7, 2022, "", "", "", uiux, "", "", "");
            data.tambahMahasiswa(student);
            AuthService authService = new AuthService(data);
            UserSession adminSession = authService.authenticate(UserSession.Role.ADMIN,
                    "admin", "admin123".toCharArray());
            require(adminSession.getRole() == UserSession.Role.ADMIN,
                    "Kredensial Admin yang ditentukan harus diterima");
            verifikasiAksesMahasiswa(data, authService, adminSession);
            UserSession studentSession = authService.authenticate(UserSession.Role.MAHASISWA,
                    student.getNim(), student.getNim().toCharArray());
            require(studentSession.getNim().equals(student.getNim())
                    && studentSession.getNama().equals(student.getNama()),
                    "Mahasiswa asli harus dapat masuk menggunakan NIM");
            for (String nim : Arrays.asList("1001", "2001", "7001")) {
                UserSession dummySession = authService.authenticate(UserSession.Role.MAHASISWA,
                        nim, nim.toCharArray());
                require(dummySession.getNim().equals(nim)
                        && dummySession.getNama().equals(data.cariMahasiswa(nim).getNama()),
                        "Akun dummy harus cocok dengan mahasiswa berdasarkan NIM: " + nim);
            }
            verifikasiLoginGagal(authService, UserSession.Role.ADMIN,
                    "admin", "salah".toCharArray(), "Password Admin yang salah harus ditolak");
            verifikasiLoginGagal(authService, UserSession.Role.MAHASISWA,
                    "SI-TIDAK-TERDAFTAR", "SI-TIDAK-TERDAFTAR".toCharArray(),
                    "NIM yang tidak ditemukan harus ditolak");
            verifikasiLoginGagal(authService, UserSession.Role.MAHASISWA,
                    student.getNim(), "password-salah".toCharArray(),
                    "Password Mahasiswa yang tidak sama dengan NIM harus ditolak");
            for (MataKuliah course : data.semuaMataKuliah()) {
                if (course.getSemester() <= 6) {
                    data.simpanNilai(new NilaiMataKuliah(student.getNim(), course.getKode(),
                            course.getNama(), course.getSemester(), course.getSks(),
                            "SI1-06".equals(course.getKode()) ? "C" : "A"));
                }
            }
            String project = RekomendasiService.BIDANG.get(1);
            String governance = RekomendasiService.BIDANG.get(2);
            Mahasiswa incomplete = new Mahasiswa("SI-REAL-02", "Mahasiswa Belum Lengkap", "",
                    "Sistem Informasi", 7, 2022, "", "", "", "", "", "", "");
            data.tambahMahasiswa(incomplete);
            require("BELUM LENGKAP".equals(data.statusKelayakanPenjurusan(incomplete.getNim(), uiux)),
                    "Nilai kosong harus berstatus belum lengkap");
            require(new RekomendasiService().analisis(incomplete,
                    data.nilaiMahasiswa(incomplete.getNim()), data.semuaBidangPeminatan())
                    .getSkor().stream().allMatch(item -> "BELUM LENGKAP".equals(item.getStatusKelayakan())),
                    "Rekomendasi harus menampilkan nilai kosong sebagai belum lengkap");
            data.hapusMahasiswa(incomplete.getNim());
            NilaiMataKuliah failedGrade = new NilaiMataKuliah(student.getNim(), "SI3-06",
                    KurikulumService.cari("SI3-06").getNama(), 3, 3, "D");
            data.ubahNilai(student.getNim(), "SI3-06", 3, failedGrade);
            require("TIDAK MEMENUHI SYARAT".equals(data.statusKelayakanPenjurusan(student.getNim(), uiux))
                    && data.kelayakanPenjurusan(student.getNim(), project).stream()
                            .anyMatch(item -> item.contains("Analisis dan Desain Sistem Informasi"))
                    && data.kelayakanPenjurusan(student.getNim(), governance).isEmpty(),
                    "Nilai D harus menggagalkan bidang pendukung terkait tanpa memblokir bidang lain");
            require(governance.equals(data.rekomendasiMahasiswa(student.getNim())
                    .getSkor().get(0).getBidang()),
                    "Rekomendasi harus menampilkan bidang lain yang memenuhi syarat");
            data.simpanBidangPeminatan(adminSession, new BidangPeminatan(uiux,
                    Arrays.asList("SI1-06"), false));
            require(data.kelayakanPenjurusan(student.getNim(), uiux).isEmpty(),
                    "Perubahan persyaratan harus langsung memengaruhi kelayakan");
            data.simpanBidangPeminatan(adminSession, new BidangPeminatan(
                    uiux, KurikulumService.bidangPendukungDefault(0), false));
            data.ubahNilai(student.getNim(), "SI3-06", 3,
                    new NilaiMataKuliah(student.getNim(), "SI3-06",
                            KurikulumService.cari("SI3-06").getNama(), 3, 3, "A"));
            require(data.kelayakanPenjurusan(student.getNim(), uiux).isEmpty(),
                    "Nilai A/B/C pada mata kuliah pendukung harus memenuhi syarat");

            RekomendasiService recommender = new RekomendasiService();
            RekomendasiService.HasilRekomendasi recommendation = recommender.analisis(
                    student, data.nilaiMahasiswa(student.getNim()), data.semuaBidangPeminatan());
            require(recommendation.getSkor().get(0).isLayak()
                    && recommendation.getSkor().get(0).getRataRata() == 4d
                    && recommendation.getSkor().stream().allMatch(RekomendasiService.SkorBidang::isLayak),
                    "Rekomendasi harus mengurutkan bidang yang lulus berdasarkan rata-rata nilai 0–4");
            require(data.rekomendasiMahasiswa(student.getNim()).getSkor().stream()
                    .allMatch(RekomendasiService.SkorBidang::isLayak),
                    "Hasil rekomendasi harus langsung mencerminkan nilai dan persyaratan terbaru");
            data.simpanRiwayat(new RiwayatPeminatan(
                    student.getNim(), 7, uiux, "Riwayat asli", LocalDate.now()));
            data.simpanPenjurusan(student.getNim(), uiux, LocalDate.now());
            data.simpanRekomendasi(student.getNim(), recommendation);

            tambahkanDataSimulasiLama(data);
            verifikasiLoginGagal(authService, UserSession.Role.MAHASISWA,
                    "SIM-SI-001", "SIM-SI-001".toCharArray(),
                    "Data mahasiswa simulasi tidak boleh dipakai untuk login");
            DataService cleaned = new DataService();
            require(cleaned.jumlahMahasiswaSimulasi() == 0
                    && cleaned.semuaMahasiswa().size() == 34
                    && cleaned.cariMahasiswa(student.getNim()) != null,
                    "Pembersihan harus mempertahankan mahasiswa dummy dan data asli");
            require(cleaned.semuaNilaiMataKuliah().size() == 1217
                    && cleaned.nilaiMahasiswa(student.getNim()).size() == 41
                    && cleaned.nilaiMahasiswa("7001").size() == 48,
                    "Nilai dummy dan nilai asli harus tetap tersimpan berdasarkan NIM (total="
                            + cleaned.semuaNilaiMataKuliah().size() + ", asli="
                            + cleaned.nilaiMahasiswa(student.getNim()).size() + ", 7001="
                            + cleaned.nilaiMahasiswa("7001").size() + ")");
            require(cleaned.semuaRiwayat().size() == 1
                    && cleaned.semuaRiwayat().get(0).getCatatan().equals("Riwayat asli")
                    && cleaned.semuaPenjurusan().size() == 16
                    && cleaned.semuaPenjurusan().stream()
                            .anyMatch(item -> item.getNim().equals(student.getNim()))
                    && cleaned.semuaPenjurusan().stream()
                            .noneMatch(RiwayatPenjurusan::isSimulasi),
                    "Riwayat simulasi harus dibersihkan tanpa menghapus data dummy dan asli");
            require(cleaned.rekomendasiMahasiswa(student.getNim()) != null
                    && cleaned.rekomendasiMahasiswa(student.getNim()).getSkor().size() == 3
                    && cleaned.rekomendasiMahasiswa("SIM-SI-001") == null
                    && cleaned.semuaBidangPeminatan().size() == 3
                    && cleaned.bidangPeminatan(uiux) != null
                    && cleaned.bidangPeminatan(project) != null
                    && cleaned.bidangPeminatan(governance) != null,
                    "Cache rekomendasi usang dibersihkan dan konfigurasi tiga bidang dipulihkan");
            int jumlahSemester7 = 0;
            int jumlahLayak = 0;
            for (Mahasiswa mahasiswa : cleaned.semuaMahasiswa()) {
                if (mahasiswa.getSemester() != 7) continue;
                jumlahSemester7++;
                if (RekomendasiService.BIDANG.stream().anyMatch(bidang ->
                        cleaned.kelayakanPenjurusan(mahasiswa.getNim(), bidang).isEmpty())) {
                    jumlahLayak++;
                }
            }
            require(cleaned.jumlahMahasiswaLayakPenjurusan() == jumlahLayak
                    && cleaned.jumlahMahasiswaBelumLayakPenjurusan()
                            == jumlahSemester7 - jumlahLayak,
                    "Statistik harus mencerminkan kelayakan mahasiswa dummy dan asli");
            verifikasiDataDummy(cleaned);
            verifikasiCrudKurikulum(data, authService, adminSession);
            System.out.println("Smoke test berhasil: data dummy, login, rekomendasi, kurikulum, dan persistensi.");
        } finally {
            try (Stream<Path> paths = Files.walk(home)) {
                paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void tambahkanDataSimulasiLama(DataService data) throws Exception {
        String nim = "SIM-SI-001";
        Mahasiswa sample = new Mahasiswa(nim, "Mahasiswa Contoh", "", "Sistem Informasi",
                7, 2022, "", "", "", "", "", "", "");
        ((Map<String, Mahasiswa>) field(data, "mahasiswaSimulasi")).put(nim, sample);
        ((List<NilaiMataKuliah>) field(data, "nilaiMataKuliah")).add(
                new NilaiMataKuliah(nim, "SI1-01", "Pancasila", 1, 2, "A"));
        ((List<RiwayatPeminatan>) field(data, "riwayatSimulasi")).add(
                new RiwayatPeminatan(nim, 7, RekomendasiService.BIDANG.get(0),
                        "Contoh", LocalDate.now()));
        ((List<RiwayatPenjurusan>) field(data, "penjurusan")).add(
                new RiwayatPenjurusan(nim, RekomendasiService.BIDANG.get(0),
                        LocalDate.now(), new ArrayList<>(), true, true));
        ((Map<String, RekomendasiService.HasilRekomendasi>) field(data, "rekomendasi")).put(
                nim, new RekomendasiService.HasilRekomendasi(nim, LocalDate.now(),
                        new ArrayList<>(), true));
        ((Map<String, BidangPeminatan>) field(data, "bidangPeminatan")).put(
                RekomendasiService.BIDANG.get(1),
                new BidangPeminatan(RekomendasiService.BIDANG.get(1), Arrays.asList("SI2-06"), true));
        Method save = DataService.class.getDeclaredMethod("simpanData");
        save.setAccessible(true);
        save.invoke(data);
    }

    private static Object field(DataService data, String name) throws Exception {
        Field field = DataService.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(data);
    }

    private static void verifikasiDataDummy(DataService data) {
        Set<String> nims = new HashSet<>();
        Set<String> names = new HashSet<>();
        Set<String> grades = new HashSet<>();
        int jumlahDummy = 0;
        int jumlahNilaiDummy = 0;
        int jumlahSemester7Layak = 0;
        int jumlahSemester7TidakLayak = 0;
        for (int semester = 1; semester <= 7; semester++) {
            for (int nomor = 1; nomor <= (semester == 7 ? 15 : 3); nomor++) {
                String nim = (semester * 1000 + nomor) + "";
                Mahasiswa mahasiswa = data.cariMahasiswa(nim);
                require(mahasiswa != null, "Mahasiswa dummy harus tersedia: " + nim);
                require(nims.add(mahasiswa.getNim()) && names.add(mahasiswa.getNama()),
                        "NIM dan nama dummy harus unik");
                require(mahasiswa.getProgramStudi().equals("Sistem Informasi")
                        && mahasiswa.getSemester() == semester,
                        "Identitas dummy harus sesuai dengan NIM dan semester");
                List<NilaiMataKuliah> nilai = data.nilaiMahasiswa(nim);
                if (semester == 7) {
                    require(RekomendasiService.BIDANG.contains(mahasiswa.getMinatAwal()),
                            "Mahasiswa semester 7 harus memiliki bidang tetap: " + nim);
                    RiwayatPenjurusan pilihan = data.penjurusanMahasiswa(nim);
                    require(pilihan != null
                            && RekomendasiService.BIDANG.contains(pilihan.getBidang())
                            && pilihan.isLayak()
                                    == data.kelayakanPenjurusan(nim, pilihan.getBidang()).isEmpty(),
                            "Bidang dan status kelayakan mahasiswa semester 7 harus sesuai nilai");
                    if (pilihan.isLayak()) jumlahSemester7Layak++;
                    else jumlahSemester7TidakLayak++;
                    Map<String, NilaiMataKuliah> nilaiPerKode = new java.util.HashMap<>();
                    for (NilaiMataKuliah item : nilai) nilaiPerKode.put(item.getKode(), item);
                    for (String kode : data.bidangPeminatan(pilihan.getBidang())
                            .getKodeMataKuliahPendukung()) {
                        NilaiMataKuliah item = nilaiPerKode.get(kode);
                        String status = item == null ? "Belum Dinilai"
                                : item.getStatusKelulusan();
                        String grade = item == null ? "" : item.getNilaiHuruf();
                        require(pilihan.getMataKuliahPendukung().stream().anyMatch(bukti ->
                                        bukti.startsWith(kode + " — ")
                                                && bukti.endsWith("(" + grade + ", " + status + ")")),
                                "Riwayat bidang harus mencerminkan status nilai mata kuliah " + kode);
                    }
                    if (nomor >= 13) {
                        RekomendasiService.HasilRekomendasi hasil =
                                data.rekomendasiMahasiswa(nim);
                        require(hasil.getSkor().stream()
                                        .noneMatch(RekomendasiService.SkorBidang::isLayak),
                                "Data 20% dummy tidak boleh memenuhi syarat pada bidang lain");
                        RekomendasiService.SkorBidang alternatif = hasil.getSkor().stream()
                                .filter(item -> !item.getBidang().equals(pilihan.getBidang()))
                                .findFirst().orElse(null);
                        require(alternatif != null
                                        && alternatif.getJumlahMataKuliahLulus() > 0
                                        && hasil.getSkor().stream()
                                                .filter(item -> !item.getBidang().equals(
                                                        pilihan.getBidang()))
                                                .allMatch(item -> alternatif
                                                        .getRasioMataKuliahLulus()
                                                        >= item.getRasioMataKuliahLulus()),
                                "Mahasiswa yang tidak memenuhi syarat harus memiliki bidang alternatif "
                                        + "berdasarkan nilai lulus");
                    }
                }
                final int currentSemester = semester;
                int expectedCourses = (int) KurikulumService.semuaMataKuliah().stream()
                        .filter(course -> course.getSemester() <= currentSemester).count();
                require(nilai.size() == expectedCourses,
                        "Nilai dummy harus tersedia untuk mata kuliah sampai semester berjalan");
                for (NilaiMataKuliah item : nilai) {
                    require(item.getSemester() <= semester && !item.getNilaiHuruf().isEmpty(),
                            "Nilai dummy harus sesuai dengan semester dan memiliki nilai huruf");
                    grades.add(item.getNilaiHuruf());
                }
                jumlahNilaiDummy += nilai.size();
                jumlahDummy++;
            }
        }
        require(jumlahDummy == 33 && jumlahNilaiDummy == 1176 && grades.size() == 5,
                "Data dummy harus berjumlah 33 mahasiswa dengan nilai bervariasi");
        require(jumlahSemester7Layak == 12 && jumlahSemester7TidakLayak == 3,
                "Persentase kelayakan mahasiswa semester 7 dummy harus tepat 80:20");
        RekomendasiService.HasilRekomendasi rekomendasi = data.rekomendasiMahasiswa("7001");
        require(rekomendasi != null && rekomendasi.getNim().equals("7001")
                && rekomendasi.getSkor().size() == 3
                && rekomendasi.getSkor().stream().allMatch(RekomendasiService.SkorBidang::isLayak),
                "Nilai mahasiswa 7001 harus terhubung ke seluruh rekomendasi bidang");
    }

    private static void verifikasiAksesMahasiswa(DataService data, AuthService authService,
                                                 UserSession adminSession) throws Exception {
        UserSession studentSession = authService.authenticate(UserSession.Role.MAHASISWA,
                "7001", "7001".toCharArray());
        Mahasiswa profile = data.profilMahasiswa(studentSession);
        require(profile.getNim().equals("7001")
                && data.mataKuliahMahasiswa(studentSession).stream()
                        .allMatch(course -> course.getSemester() <= profile.getSemester())
                && data.nilaiMahasiswa(studentSession).stream()
                        .allMatch(nilai -> nilai.getNim().equals("7001"))
                && data.riwayatPenjurusanMahasiswa(studentSession).stream()
                        .allMatch(item -> item.getNim().equals("7001")),
                "Akses mahasiswa hanya boleh menampilkan data milik NIM sesi");

        UserSession mismatchedSession = new UserSession(
                UserSession.Role.MAHASISWA, "7001", "7002", "Mahasiswa lain");
        verifikasiAksesMahasiswaDitolak(() -> data.profilMahasiswa(mismatchedSession),
                "Sesi yang mencoba membuka profil NIM lain harus ditolak");

        NilaiMataKuliah existing = data.nilaiMahasiswa("7001", "SI1-06");
        RekomendasiService.HasilRekomendasi before = data.rekomendasiMahasiswa(studentSession);
        require(before.getSkor().stream().filter(score ->
                score.getBidang().equals(RekomendasiService.BIDANG.get(0)))
                .allMatch(RekomendasiService.SkorBidang::isLayak),
                "Nilai terverifikasi 7001 harus dapat memenuhi rekomendasi UI/UX");
        data.ajukanNilaiMahasiswa(studentSession, "SI1-06", "D");
        NilaiMataKuliah pending = data.nilaiMahasiswa("7001", "SI1-06");
        require(pending != null && !pending.isTerverifikasi()
                && data.rekomendasiMahasiswa(studentSession).getSkor().stream()
                        .filter(score -> score.getBidang().equals(RekomendasiService.BIDANG.get(0)))
                        .allMatch(score -> !score.isLayak() && !score.getBelumLengkap().isEmpty()),
                "Nilai yang diajukan mahasiswa harus menunggu dan tidak memengaruhi rekomendasi");
        verifikasiAksesMahasiswaDitolak(() -> data.verifikasiNilai(
                studentSession, "7001", "SI1-06", 1),
                "Mahasiswa tidak boleh memverifikasi nilai sendiri");

        data.verifikasiNilai(adminSession, "7001", "SI1-06", 1);
        require(data.nilaiMahasiswa("7001", "SI1-06").isTerverifikasi(),
                "Admin harus dapat memverifikasi nilai mahasiswa");
        data.ubahNilai("7001", "SI1-06", 1, existing);
        data.pilihBidangMahasiswa(studentSession, RekomendasiService.BIDANG.get(1));
        require(data.profilMahasiswa(studentSession).getMinatAwal()
                        .equals(RekomendasiService.BIDANG.get(1)),
                "Pilihan bidang mahasiswa hanya boleh memperbarui minat miliknya sendiri");
    }

    private static void verifikasiCrudKurikulum(DataService data, AuthService authService,
                                                UserSession adminSession) throws Exception {
        int originalCount = data.jumlahMataKuliah();
        UserSession studentSession = authService.authenticate(UserSession.Role.MAHASISWA,
                "7001", "7001".toCharArray());
        verifikasiAksesMahasiswaDitolak(() -> data.tambahMataKuliah(studentSession,
                new MataKuliah("SI6-T99", "Topik Baru", 6, 2, false)),
                "Mahasiswa tidak boleh menambah mata kuliah kurikulum");
        verifikasiAksesMahasiswaDitolak(() -> data.simpanBidangPeminatan(studentSession,
                new BidangPeminatan(RekomendasiService.BIDANG.get(0),
                        KurikulumService.bidangPendukungDefault(0), false)),
                "Mahasiswa tidak boleh mengubah prasyarat peminatan");

        data.tambahMataKuliah(adminSession,
                new MataKuliah("SI6-T99", "Topik Baru", 6, 2, false));
        DataService persisted = new DataService();
        require(persisted.jumlahMataKuliah() == originalCount + 1
                && persisted.cariMataKuliah("SI6-T99") != null,
                "Mata kuliah baru harus tersimpan ke berkas data");
        String uiux = RekomendasiService.BIDANG.get(0);
        List<String> originalPrerequisites = persisted.bidangPeminatan(uiux)
                .getKodeMataKuliahPendukung();
        persisted.simpanBidangPeminatan(adminSession,
                new BidangPeminatan(uiux, Arrays.asList("SI6-T99"), false));
        require(persisted.rekomendasiMahasiswa("7001").getSkor().stream()
                .filter(score -> score.getBidang().equals(uiux))
                .anyMatch(score -> score.getBelumLengkap().contains("Topik Baru")),
                "Prasyarat baru harus terhubung ke kurikulum dinamis untuk rekomendasi");
        verifikasiAksesMahasiswaDitolak(() -> persisted.hapusMataKuliah(adminSession, "SI6-T99"),
                "Mata kuliah prasyarat tidak boleh dihapus sebelum konfigurasi diperbarui");
        persisted.simpanBidangPeminatan(adminSession,
                new BidangPeminatan(uiux, originalPrerequisites, false));
        persisted.ubahMataKuliah(adminSession, "SI6-T99",
                new MataKuliah("SI6-T99", "Topik Terbarui", 6, 3, false));
        DataService updated = new DataService();
        require("Topik Terbarui".equals(updated.cariMataKuliah("SI6-T99").getNama())
                && updated.cariMataKuliah("SI6-T99").getSks() == 3,
                "Admin harus dapat mengubah nama dan SKS mata kuliah");
        verifikasiAksesMahasiswaDitolak(() -> updated.hapusMataKuliah(studentSession, "SI6-T99"),
                "Mahasiswa tidak boleh menghapus mata kuliah kurikulum");
        verifikasiAksesMahasiswaDitolak(() -> updated.hapusMataKuliah(adminSession, "SI1-01"),
                "Mata kuliah yang memiliki nilai historis tidak boleh dihapus");
        updated.hapusMataKuliah(adminSession, "SI6-T99");
        DataService restored = new DataService();
        require(restored.jumlahMataKuliah() == originalCount
                && restored.cariMataKuliah("SI6-T99") == null,
                "Admin harus dapat menghapus mata kuliah tanpa riwayat atau prasyarat");
    }

    private static void verifikasiAksesMahasiswaDitolak(OperasiAkses operation, String message)
            throws Exception {
        try {
            operation.run();
            throw new AssertionError(message);
        } catch (IllegalArgumentException expected) {
            // Expected authorization rejection.
        }
    }

    private interface OperasiAkses {
        void run() throws Exception;
    }

    private static void verifikasiFilterMahasiswa() {
        Mahasiswa mahasiswa = new Mahasiswa("SI-2026-01", "Aisyah Rahma", "",
                "Sistem Informasi", 7, 2026, "", "", "", "", "", "", "");
        Mahasiswa mahasiswaLain = new Mahasiswa("SI-2026-02", "Budi Santoso", "",
                "Sistem Informasi", 3, 2026, "", "", "", "", "", "", "");
        require(SistemPeminatanFTI.cocokFilterMahasiswa(mahasiswa, "aisyah", "7"),
                "Pencarian nama dan filter semester harus dapat digunakan bersama");
        require(SistemPeminatanFTI.cocokFilterMahasiswa(mahasiswa, "2026-01", "Semua Semester"),
                "Pencarian NIM harus menerima semua semester");
        require(!SistemPeminatanFTI.cocokFilterMahasiswa(mahasiswaLain, "Budi", "7")
                && !SistemPeminatanFTI.cocokFilterMahasiswa(mahasiswa, "Budi", "Semua Semester"),
                "Pencarian dan filter semester harus menyaring secara bersamaan");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void verifikasiLoginGagal(AuthService authService, UserSession.Role role,
                                             String username, char[] password, String message) {
        try {
            authService.authenticate(role, username, password);
            throw new AssertionError(message);
        } catch (IllegalArgumentException expected) {
            // Expected authentication rejection.
        }
    }
}
