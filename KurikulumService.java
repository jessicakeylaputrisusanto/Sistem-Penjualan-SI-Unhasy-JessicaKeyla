import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class KurikulumService {
    private static final List<MataKuliah> MATA_KULIAH;

    static {
        List<MataKuliah> courses = new ArrayList<>();
        tambah(courses, 1, new String[][]{
            {"Pancasila", "2"}, {"Bahasa Indonesia", "2"}, {"Pengantar Teknologi Informasi", "2"},
            {"Teori Kewirausahaan", "2"}, {"Matematika Diskrit", "3"},
            {"Interaksi Manusia dan Komputer", "3"}, {"Algoritma dan Struktur Data", "3"},
            {"Sistem Operasi", "3"}
        });
        tambah(courses, 2, new String[][]{
            {"Kepemimpinan dan Manajemen Organisasi", "2"}, {"Agama", "2"}, {"Literasi Digital", "2"},
            {"Pemikiran Hadratus Syaikh K.H. M. Hasyim Asy'ari", "4"}, {"Kewarganegaraan", "2"},
            {"Sistem Informasi Manajemen", "3"}, {"Studi Al-Qur'an dan Hadist", "2"}, {"Struktur Data", "3"}
        });
        tambah(courses, 3, new String[][]{
            {"Basis Data", "3"}, {"Rekayasa Perangkat Lunak", "3"},
            {"Pemikiran Hadratus Syaikh K.H. M. Hasyim Asy'ari", "2"}, {"Bahasa Inggris", "2"},
            {"Perencanaan Bisnis", "3"}, {"Analisis dan Desain Sistem Informasi", "3"},
            {"Statistik", "3"}, {"Desain dan Manajemen Jaringan", "3"}
        });
        tambah(courses, 4, new String[][]{
            {"Pemrograman Web", "3"}, {"Pemrograman Berorientasi Objek", "3"},
            {"Manajemen Basis Data", "3"}, {"Analisis Proses Bisnis", "3"},
            {"Sistem Informasi Akuntansi", "3"}, {"Keamanan Sistem Informasi", "3"},
            {"Metode Penelitian Sistem Informasi", "2"}
        });
        tambah(courses, 5, new String[][]{{"Magang", "20"}});
        tambah(courses, 6, new String[][]{
            {"Data Mining", "3"}, {"Business Intelligence", "3"},
            {"Tata Kelola Teknologi Informasi", "3"}, {"Manajemen Risiko Sistem Informasi", "3"},
            {"E-Commerce", "3"}, {"Interaksi dan Pengalaman Pengguna", "3"},
            {"Kewirausahaan Digital", "2"}
        });
        courses.add(new MataKuliah("SI6-U01", "Manajemen Proyek Sistem Informasi", 6, 3, false, true));
        courses.add(new MataKuliah("SI6-U02", "Audit Sistem Informasi", 6, 3, false, true));
        tambah(courses, 7, new String[][]{
            {"Analisis dan Desain Sistem Informasi", "3"}, {"Ekonomi Digital", "3"},
            {"Pemikiran Hadratus Syaikh K.H. M. Hasyim Asy'ari", "2"}, {"Metodologi Penelitian", "3"},
            {"Aplikasi Bisnis", "3"}, {"Manajemen Proyek", "3"}, {"Skripsi (Pilihan)", "6"}
        });
        tambah(courses, 8, new String[][]{
            {"Audit Sistem Informasi", "3"}, {"Strategi Sistem Informasi", "3"},
            {"Manajemen Layanan TI", "3"}, {"Analisis Data Bisnis", "3"},
            {"Pengembangan Sistem Informasi Enterprise", "3"},
            {"Etika Profesi dan Hukum TI", "2"}, {"Tugas Akhir / Skripsi", "6"}
        });
        MATA_KULIAH = Collections.unmodifiableList(courses);
    }

    private KurikulumService() {
    }

    private static void tambah(List<MataKuliah> target, int semester, String[][] daftar) {
        for (int i = 0; i < daftar.length; i++) {
            target.add(new MataKuliah(String.format(java.util.Locale.ROOT, "SI%d-%02d", semester, i + 1),
                    daftar[i][0], semester, Integer.parseInt(daftar[i][1]), false));
        }
    }

    public static List<MataKuliah> semuaMataKuliah() {
        return MATA_KULIAH;
    }

    public static MataKuliah cari(String kode) {
        for (MataKuliah item : MATA_KULIAH) {
            if (item.getKode().equals(kode)) return item;
        }
        return null;
    }

    public static List<MataKuliah> mataKuliahSemester(int semester) {
        List<MataKuliah> hasil = new ArrayList<>();
        for (MataKuliah item : MATA_KULIAH) {
            if (item.getSemester() == semester) hasil.add(item);
        }
        return hasil;
    }

    public static int jumlahMataKuliah() {
        return MATA_KULIAH.size();
    }

    public static List<String> bidangPendukungDefault(int index) {
        switch (index) {
            case 0: return java.util.Arrays.asList("SI1-06", "SI3-02", "SI3-06", "SI4-01");
            case 1: return java.util.Arrays.asList(
                    "SI1-04", "SI3-05", "SI3-02", "SI3-06", "SI4-04", "SI5-01", "SI6-U01");
            case 2: return java.util.Arrays.asList(
                    "SI1-08", "SI3-01", "SI3-08", "SI4-06", "SI6-03", "SI6-U02");
            default: throw new IllegalArgumentException("Indeks bidang tidak valid.");
        }
    }
}
