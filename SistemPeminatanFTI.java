import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.AlphaComposite;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.AbstractCellEditor;
import javax.imageio.ImageIO;

public class SistemPeminatanFTI extends JFrame {
    private static final Color NAVY = new Color(17, 39, 71);
    private static final Color BLUE = new Color(34, 111, 183);
    private static final Color LIGHT_BLUE = new Color(232, 241, 250);
    private static final Color BACKGROUND = new Color(244, 247, 251);
    private static final Color CREAM = new Color(232, 241, 250);
    private static final Color GOLD = new Color(205, 164, 52);
    private static final Color TEAL = new Color(35, 133, 129);
    private static final Color IVORY = Color.WHITE;
    private static final Color TEXT = new Color(36, 49, 66);
    private static final int MAHASISWA_PAGE_SIZE = 5;
    private static final int PAGE_HEADER_ICON_SIZE = 48;
    private static final int PAGE_HEADER_GLYPH_SIZE = 26;
    static {
        UIManager.put("Label.foreground", NAVY);
        UIManager.put("Button.foreground", NAVY);
        UIManager.put("Button.background", LIGHT_BLUE);
        UIManager.put("Button.disabledText", Color.WHITE);
        UIManager.put("TextField.foreground", NAVY);
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.selectionBackground", LIGHT_BLUE);
        UIManager.put("TextField.selectionForeground", NAVY);
        UIManager.put("TextArea.foreground", NAVY);
        UIManager.put("TextArea.background", Color.WHITE);
        UIManager.put("TextArea.selectionBackground", LIGHT_BLUE);
        UIManager.put("TextArea.selectionForeground", NAVY);
        UIManager.put("ComboBox.foreground", NAVY);
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.disabledForeground", new Color(70, 82, 96));
        UIManager.put("ComboBox.selectionBackground", BLUE);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.selectionBackground", LIGHT_BLUE);
        UIManager.put("Table.selectionForeground", NAVY);
        UIManager.put("TableHeader.background", LIGHT_BLUE);
        UIManager.put("TableHeader.foreground", NAVY);
        UIManager.put("TabbedPane.foreground", NAVY);
        UIManager.put("TabbedPane.background", LIGHT_BLUE);
        UIManager.put("TabbedPane.selected", Color.WHITE);
        UIManager.put("TabbedPane.selectedForeground", NAVY);
        UIManager.put("TabbedPane.unselectedBackground", LIGHT_BLUE);
        UIManager.put("TabbedPane.contentAreaColor", Color.WHITE);
        UIManager.put("OptionPane.messageForeground", NAVY);
    }
    private static final String[] PRODI = {"Sistem Informasi"};

    private final DataService dataService;
    private UserSession userSession;
    private final Runnable logoutHandler;
    private final RekomendasiService rekomendasiService = new RekomendasiService();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final Map<String, JButton> navButtons = new HashMap<>();
    private final BufferedImage dashboardBuildingImage = loadAsset("Kampus UNHASY di Bawah Langit Cerah.png");
    private final BufferedImage logoUnhasyImage = loadAsset("LOGO UNHASY.png");

    private final JLabel[] ringkasan = new JLabel[4];
    private final GrafikBatang grafikSemester = new GrafikBatang(BLUE);
    private final GrafikBatang grafikKelulusan = new GrafikBatang(TEAL);
    private final GrafikBatang grafikBidang = new GrafikBatang(GOLD);
    private final GrafikBatang laporanGrafikSemester = new GrafikBatang(BLUE);
    private final GrafikBatang laporanGrafikKelulusan = new GrafikBatang(TEAL);
    private final GrafikBatang laporanGrafikBidang = new GrafikBatang(GOLD);
    private final GrafikBatang laporanGrafikPenjurusan = new GrafikBatang(BLUE);

    private final JTextField nimField = new JTextField(18);
    private final JTextField namaField = new JTextField(18);
    private final JComboBox<String> genderField = new JComboBox<>(
            new String[]{"Pilih jenis kelamin", "Laki-laki", "Perempuan", "Lainnya"});
    private final JComboBox<String> prodiField = new JComboBox<>(PRODI);
    private final JComboBox<String> semesterField = semesterCombo();
    private final JTextField angkatanField = new JTextField(18);
    private final JComboBox<String> sekolahField = new JComboBox<>(
            new String[]{"SMA", "SMK", "MA", "Lainnya"});
    private final JTextField jurusanField = new JTextField(18);
    private final JTextArea pengalamanField = textArea(3);
    private final JComboBox<String> minatField = minatCombo();
    private final JTextArea alasanField = textArea(3);
    private final JTextArea proyekField = textArea(3);
    private final JLabel fotoPathLabel = new JLabel("Belum memilih foto");
    private final JLabel mahasiswaFormStatusLabel = new JLabel(" ");
    private final JLabel mahasiswaFotoPreview = fotoPreviewLabel();
    private final JComboBox<String> mahasiswaFilterSemester = new JComboBox<>(
            new String[]{"Semua Semester", "1", "2", "3", "4", "5", "6", "7"});
    private final JComboBox<String> mahasiswaFilterProdi = new JComboBox<>(
            new String[]{"Semua Program Studi", PRODI[0]});
    private final JLabel mahasiswaCountLabel = new JLabel("Menampilkan 0 data");
    private final JLabel mahasiswaPageLabel = new JLabel("1 / 1");
    private final List<JComponent> komponenFormMahasiswa = new ArrayList<>();
    private JButton simpanMahasiswaButton;
    private JButton ubahMahasiswaButton;
    private JButton hapusMahasiswaButton;
    private JButton resetMahasiswaButton;
    private JButton mahasiswaFirstPageButton;
    private JButton mahasiswaPreviousPageButton;
    private JButton mahasiswaNextPageButton;
    private JButton mahasiswaLastPageButton;
    private JPanel mahasiswaFormPanel;
    private JDialog mahasiswaFormDialog;
    private JLabel mahasiswaFormTitleLabel;
    private int currentMahasiswaPage;
    private JButton pilihFotoButton;
    private final JTextField cariMahasiswaField = new JTextField(18);
    private final DefaultTableModel mahasiswaModel = new DefaultTableModel(
            new Object[]{"No", "NIM", "Nama Mahasiswa", "Program Studi", "Semester", "Aksi"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return column == 5; }
    };
    private final JTable mahasiswaTable = createTable(mahasiswaModel);

    private final JComboBox<String> latarNimField = new JComboBox<>();
    private final JLabel latarNamaLabel = new JLabel("Pilih mahasiswa");
    private final JComboBox<String> latarSekolahField = new JComboBox<>(
            new String[]{"SMA", "SMK", "MA", "Lainnya"});
    private final JTextField latarJurusanField = new JTextField(28);
    private final JTextArea latarPengalamanField = textArea(4);
    private final JTextArea latarProyekField = textArea(4);
    private final JComboBox<String> latarMinatField = minatCombo();
    private final JTextArea latarAlasanField = textArea(4);
    private final JLabel latarFotoPreview = fotoPreviewLabel();
    private JButton latarSimpanButton;
    private final DefaultTableModel latarBelakangModel = tableModel(
            "NIM", "Nama", "Asal Sekolah", "Jurusan", "Pengalaman Teknologi",
            "Minat", "Alasan", "Proyek", "Sumber");
    private final JTable latarBelakangTable = createTable(latarBelakangModel);

    private final JComboBox<String> riwayatNimField = new JComboBox<>();
    private final JTextField riwayatCariField = new JTextField(14);
    private final JComboBox<String> riwayatFilterProdi = new JComboBox<>(
            new String[]{"Semua program studi", PRODI[0]});
    private final JComboBox<String> riwayatFilterSemester = new JComboBox<>(
            new String[]{"Semua semester", "1", "2", "3", "4", "5", "6", "7", "8"});
    private final JComboBox<String> riwayatFilterBidang = new JComboBox<>(bidangFilterOptions());
    private final JLabel riwayatNamaLabel = new JLabel("Pilih mahasiswa");
    private final JComboBox<String> riwayatSemesterField = semesterCombo();
    private final JComboBox<String> bidangField = new JComboBox<>(
            RekomendasiService.BIDANG.toArray(new String[0]));
    private final JTextArea riwayatCatatanField = textArea(4);
    private final JTextField tanggalField = new JTextField(LocalDate.now().toString(), 16);
    private final DefaultTableModel riwayatModel = tableModel(
            "NIM", "Nama", "Program Studi", "Semester", "Bidang Peminatan",
            "Tanggal", "Catatan", "Sumber");
    private final JTable riwayatTable = createTable(riwayatModel);
    private JButton riwayatSimpanButton;
    private JButton riwayatUbahButton;
    private JButton riwayatHapusButton;
    private final JLabel riwayatStatusLabel = new JLabel("Pilih mahasiswa untuk mencatat peminatan.");
    private String riwayatNimSedangDiedit;
    private int semesterRiwayatSedangDiedit = -1;

    private final JComboBox<String> rekomendasiNimField = new JComboBox<>();
    private final JTextField rekomendasiCariField = new JTextField(32);
    private final JLabel rekomendasiNamaLabel = new JLabel("Pilih mahasiswa");
    private boolean memperbaruiPilihanRekomendasi;
    private final DefaultTableModel rekomendasiModel = tableModel(
            "Peringkat", "Bidang", "Rata-rata Nilai (0–4)", "Mata Kuliah Pendukung & Nilai", "Status / Alasan");
    private final JTable rekomendasiTable = createTable(rekomendasiModel);
    private final JTextArea rekomendasiPenjelasan = textArea(6);
    private final CardLayout rekomendasiHasilLayout = new CardLayout();
    private final JPanel rekomendasiHasilContent = new JPanel(rekomendasiHasilLayout);
    private JScrollPane rekomendasiPenjelasanScroll;

    private final JComboBox<String> laporanProdiField = new JComboBox<>(
            new String[]{"Semua program studi", PRODI[0]});
    private final JComboBox<String> laporanSemesterField = new JComboBox<>(
            new String[]{"Semua Semester", "1", "2", "3", "4", "5", "6", "7"});
    private final JTextField laporanCariField = new JTextField(20);
    private final JComboBox<String> laporanJenisDataField = new JComboBox<>(
            new String[]{"Semua Data", "Data Asli", "Data Dummy"});
    private final JLabel laporanTotalMahasiswaValue = new JLabel("0");
    private final JLabel laporanTotalMataKuliahValue = new JLabel("0");
    private final JLabel laporanLayakValue = new JLabel("0");
    private final JLabel laporanBelumLayakValue = new JLabel("0");
    private final DefaultTableModel laporanMahasiswaModel = tableModel(
            "NIM", "Nama", "Program Studi", "Semester", "Angkatan", "Sumber");
    private final DefaultTableModel laporanRiwayatModel = tableModel(
            "NIM", "Nama", "Bidang Peminatan", "Tanggal", "Mata Kuliah Pendukung", "Status", "Sumber");
    private final DefaultTableModel laporanRekomendasiModel = tableModel(
            "NIM", "Nama", "Rekomendasi Utama", "Alternatif / Status", "Rata-rata Nilai (0–4)",
            "Alasan Rekomendasi", "Sumber");
    private final DefaultTableModel laporanStatistikModel = tableModel("Jenis Statistik", "Kategori", "Jumlah");
    private final JComboBox<String> kodeNilaiNimField = new JComboBox<>();
    private final JTextField kodeNilaiField = new JTextField(12);
    private final JTextField namaMataKuliahField = new JTextField(20);
    private final JComboBox<String> semesterMataKuliahField = new JComboBox<>(
            new String[]{"1", "2", "3", "4", "5", "6", "7", "8"});
    private final JTextField sksMataKuliahField = new JTextField("3", 5);
    private final JComboBox<String> nilaiHurufField = new JComboBox<>(new String[]{"A", "B", "C", "D", "E"});
    private final JTextField cariNilaiField = new JTextField(18);
    private final JLabel nilaiStatusLabel = new JLabel("Pilih mahasiswa untuk mengelola nilainya.");
    private final DefaultTableModel nilaiMataKuliahModel = tableModel(
            "No", "NIM", "Nama Mahasiswa", "Semester", "Mata Kuliah", "Nilai", "Aksi", "Kode",
            "Status Verifikasi");
    private final JTable nilaiMataKuliahTable = createTable(nilaiMataKuliahModel);
    private final JComboBox<MataKuliah> kurikulumField = new JComboBox<>();
    private final JComboBox<String> filterNilaiSemesterField = new JComboBox<>(
            new String[]{"Semua Semester", "1", "2", "3", "4", "5", "6", "7", "8"});
    private JButton nilaiTambahButton;
    private JButton nilaiUbahButton;
    private JButton nilaiHapusButton;
    private JButton nilaiVerifikasiButton;
    private JDialog nilaiFormDialog;
    private final JLabel nilaiTotalMahasiswaValue = new JLabel("0");
    private final JLabel nilaiTotalMataKuliahValue = new JLabel("0");
    private final JLabel nilaiTerinputValue = new JLabel("0%");
    private final JLabel nilaiRataRataValue = new JLabel("—");
    private final JLabel nilaiCountLabel = new JLabel("Menampilkan 0 nilai");
    private String nilaiNimSedangDiedit;
    private String kodeNilaiSedangDiedit;
    private int semesterNilaiSedangDiedit = -1;
    private final JComboBox<String> bidangKelolaField = new JComboBox<>(
            RekomendasiService.BIDANG.toArray(new String[0]));
    private final JPanel bidangMataKuliahList = new JPanel();
    private final Map<String, JCheckBox> bidangMataKuliahChecks = new LinkedHashMap<>();
    private final Map<String, Set<String>> bidangMataKuliahDrafts = new LinkedHashMap<>();
    private final Map<String, JToggleButton> bidangPilihanCards = new LinkedHashMap<>();
    private final Map<String, JLabel> bidangRingkasanCounts = new LinkedHashMap<>();
    private final Map<String, JTextArea> bidangRingkasanTexts = new LinkedHashMap<>();
    private final JLabel bidangKelolaStatus = new JLabel("0 mata kuliah dipilih.");
    private final JPanel bidangRingkasanList = new JPanel(new GridLayout(1, 3, 12, 0));
    private final JComboBox<String> penjurusanNimField = new JComboBox<>();
    private final JLabel penjurusanNamaLabel = new JLabel("Pilih mahasiswa semester 7");
    private final JComboBox<String> penjurusanBidangField = new JComboBox<>(
            RekomendasiService.BIDANG.toArray(new String[0]));
    private final JTextField penjurusanTanggalField = new JTextField(LocalDate.now().toString(), 16);
    private final JLabel penjurusanStatusLabel = new JLabel("Pilih mahasiswa dan bidang untuk memeriksa kelayakan.");
    private JButton penjurusanSimpanButton;
    private final JLabel penjurusanTotalSemester7 = new JLabel("0");
    private final JLabel penjurusanTotalLayak = new JLabel("0");
    private final JLabel penjurusanTotalBelumLayak = new JLabel("0");
    private final JLabel prasyaratTotalValue = new JLabel("0");
    private final JLabel prasyaratLulusValue = new JLabel("0");
    private final JLabel prasyaratTidakLulusValue = new JLabel("0");
    private final JLabel prasyaratStatusLabel = penjurusanStatusLabel;
    private final JTextArea rekomendasiPenjurusanArea = textArea(3);
    private final JLabel penjurusanSebelumnyaLabel = new JLabel("Belum ada penjurusan sebelumnya.");
    private final JTextField cariRiwayatPenjurusanField = new JTextField(22);
    private final DefaultTableModel pemeriksaanPrasyaratModel = tableModel(
            "Nama Mata Kuliah", "Semester", "Nilai", "Status");
    private final JTable pemeriksaanPrasyaratTable = createTable(pemeriksaanPrasyaratModel);
    private final DefaultTableModel riwayatPenjurusanModel = tableModel(
            "NIM", "Nama Mahasiswa", "Bidang Peminatan", "Tanggal Penjurusan", "Status");
    private final JTable riwayatPenjurusanTable = createTable(riwayatPenjurusanModel);
    private final JLabel riwayatPenjurusanEmptyLabel =
            new JLabel("Belum ada riwayat penjurusan", SwingConstants.CENTER);
    private final CardLayout riwayatPenjurusanLayout = new CardLayout();
    private final JPanel riwayatPenjurusanContent = new JPanel(riwayatPenjurusanLayout);
    private final CardLayout pemeriksaanLayout = new CardLayout();
    private final JPanel pemeriksaanContent = new JPanel(pemeriksaanLayout);
    private final DefaultTableModel riwayatNilaiModel = tableModel(
            "Semester", "Kode Mata Kuliah", "Nama Mata Kuliah", "Nilai Huruf", "Status");
    private final JTable riwayatNilaiTable = createTable(riwayatNilaiModel);
    private final DefaultTableModel informasiPenjurusanModel = tableModel(
            "Tanggal", "Bidang Peminatan", "Status");
    private final JTable informasiPenjurusanTable = createTable(informasiPenjurusanModel);
    private final JLabel informasiNimValue = new JLabel("—");
    private final JLabel informasiNamaValue = new JLabel("—");
    private final JLabel informasiSemesterValue = new JLabel("—");
    private final JLabel informasiKelayakanValue = new JLabel("—");
    private final JLabel jumlahMataKuliahValue = new JLabel("0");
    private final JLabel jumlahLulusValue = new JLabel("0");
    private final JLabel jumlahTidakLulusValue = new JLabel("0");
    private final CardLayout informasiAkademikLayout = new CardLayout();
    private final JPanel informasiAkademikContent = new JPanel(informasiAkademikLayout);
    private final DefaultTableModel laporanNilaiModel = tableModel(
            "NIM", "Nama", "Semester", "Kode", "Mata Kuliah", "SKS", "Nilai", "Status", "Sumber");
    private final DefaultTableModel laporanPenjurusanModel = tableModel(
            "NIM", "Nama", "Bidang Peminatan", "Tanggal", "Mata Kuliah Pendukung", "Status", "Sumber");
    private final JComboBox<String> laporanBidangField = new JComboBox<>(bidangFilterOptions());
    private final DefaultTableModel nilaiSayaModel = tableModel(
            "Semester", "Kode", "Mata Kuliah", "Nilai", "Status Verifikasi");
    private final JTable nilaiSayaTable = createTable(nilaiSayaModel);
    private final JComboBox<MataKuliah> inputNilaiMataKuliahMahasiswa = new JComboBox<>();
    private final JComboBox<String> inputNilaiHurufMahasiswa =
            new JComboBox<>(new String[]{"A", "B", "C", "D", "E"});
    private final JLabel inputNilaiStatusMahasiswa = new JLabel("Nilai baru akan menunggu verifikasi Admin.");
    private final JComboBox<String> minatMahasiswaField = new JComboBox<>(
            RekomendasiService.BIDANG.toArray(new String[0]));
    private final DefaultTableModel rekomendasiSayaModel = tableModel(
            "Peringkat", "Bidang Peminatan", "Rata-rata (0–4)", "Status");
    private final JTable rekomendasiSayaTable = createTable(rekomendasiSayaModel);
    private final JTextArea rekomendasiSayaDetail = textArea(7);
    private JLabel rekomendasiMahasiswaNama;
    private JLabel rekomendasiMahasiswaStatus;
    private JLabel rekomendasiMahasiswaSkor;
    private JLabel rekomendasiMahasiswaAlasan;
    private final DefaultTableModel riwayatSayaModel = tableModel(
            "Tanggal", "Bidang Peminatan", "Mata Kuliah Pendukung", "Status");
    private final JTable riwayatSayaTable = createTable(riwayatSayaModel);
    private JLabel riwayatMahasiswaJumlah;
    private JLabel riwayatMahasiswaStatus;
    private final Map<String, JToggleButton> pilihanBidangMahasiswaCards = new LinkedHashMap<>();
    private JLabel pilihanBidangMahasiswaStatus;
    private JLabel nilaiSayaTotalMataKuliah;
    private JLabel nilaiSayaTerverifikasi;
    private JLabel nilaiSayaMenunggu;
    private JLabel nilaiSayaBelumDiinput;
    private JLabel dashboardMahasiswaNama;
    private JLabel dashboardMahasiswaNim;
    private JLabel dashboardMahasiswaSemester;
    private JLabel dashboardMahasiswaProgram;
    private JLabel dashboardMahasiswaJumlahMataKuliah;
    private JLabel dashboardMahasiswaNilaiTerverifikasi;
    private JLabel dashboardMahasiswaIpk;
    private JLabel dashboardMahasiswaStatusAkademik;
    private JLabel dashboardMahasiswaRekomendasiNama;
    private JLabel dashboardMahasiswaRekomendasiStatus;
    private JLabel dashboardMahasiswaRekomendasiSkor;
    private JLabel dashboardMahasiswaRekomendasiAlasan;
    private JLabel dashboardMahasiswaRiwayat;
    private GrafikIpkMahasiswa dashboardMahasiswaGrafik;

    public SistemPeminatanFTI() throws IOException, ClassNotFoundException {
        this(new DataService(), null);
    }

    public SistemPeminatanFTI(DataService dataService, UserSession userSession)
            throws IOException, ClassNotFoundException {
        this(dataService, userSession, dataService == null
                ? null : () -> Main.tampilkanLogin(dataService));
    }

    public SistemPeminatanFTI(DataService dataService, UserSession userSession,
                              Runnable logoutHandler)
            throws IOException, ClassNotFoundException {
        super("Sistem Informasi Penjurusan Mata Kuliah dan Rekomendasi Peminatan SI FTI UNHASY");
        if (dataService == null) {
            throw new IllegalArgumentException("DataService wajib disediakan.");
        }
        if (userSession != null && userSession.getRole() != UserSession.Role.ADMIN
                && userSession.getRole() != UserSession.Role.MAHASISWA) {
            throw new IllegalArgumentException("Peran pengguna tidak dikenal.");
        }
        this.dataService = dataService;
        this.userSession = userSession;
        for (MataKuliah mataKuliah : dataService.semuaMataKuliah()) {
            kurikulumField.addItem(mataKuliah);
        }
        this.logoutHandler = logoutHandler == null
                ? () -> Main.tampilkanLogin(dataService) : logoutHandler;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1140, 680));
        setSize(1380, 850);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(createSidebar(), BorderLayout.WEST);
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.add(createHeader(), BorderLayout.NORTH);
        mainArea.add(content, BorderLayout.CENTER);
        add(mainArea, BorderLayout.CENTER);

        if (isAdminMode()) {
            content.add(createDashboardPage(), "Dashboard");
            content.add(createMahasiswaPage(), "Data Mahasiswa");
            content.add(createLatarPage(), "Mata Kuliah & Nilai");
            content.add(createBidangPage(), "Bidang Peminatan");
            content.add(createRiwayatPage(), "Penjurusan Semester 7");
            content.add(createRekomendasiPage(), "Rekomendasi");
            content.add(createLaporanPage(), "Laporan");
        } else {
            content.add(createDashboardMahasiswaPage(), "Dashboard");
            content.add(createProfilSayaPage(), "Profil Saya");
            content.add(createNilaiSayaPage(), "Mata Kuliah & Nilai");
            content.add(createInputNilaiMahasiswaPage(), "Input Nilai");
            content.add(createPilihBidangMahasiswaPage(), "Pilih Bidang Peminatan");
            content.add(createRekomendasiMahasiswaPage(), "Rekomendasi Peminatan");
            content.add(createRiwayatMahasiswaPage(), "Riwayat Penjurusan");
        }
        terapkanTemaKomponen(content);
        if (isAdminMode()) {
            stylePenjurusanInput(penjurusanNimField);
            stylePenjurusanInput(penjurusanBidangField);
            stylePenjurusanInput(penjurusanTanggalField);
            stylePenjurusanInput(rekomendasiCariField);
            rekomendasiPenjurusanArea.setBackground(new Color(238, 244, 255));
            rekomendasiPenjurusanArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            rekomendasiPenjelasan.setBackground(new Color(226, 237, 255));
            rekomendasiPenjelasan.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            rekomendasiPenjelasanScroll.setBorder(BorderFactory.createEmptyBorder());
            rekomendasiPenjelasanScroll.getViewport().setBackground(new Color(226, 237, 255));
            aturRendererRekomendasi();
            resetFormMahasiswa();
            muatPilihanMahasiswa();
            resetFormNilai();
            refreshSemua();
        }
        tampilkanHalaman("Dashboard");
    }

    private boolean isAdminMode() {
        return userSession == null || userSession.getRole() == UserSession.Role.ADMIN;
    }

    private JPanel createDashboardMahasiswaPage() {
        JPanel page = createContentBackgroundPage(0, 0);
        page.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        Mahasiswa mahasiswa = dataService.profilMahasiswa(userSession);

        JPanel welcome = roundedCard(new Color(230, 241, 255));
        welcome.setLayout(new BorderLayout(16, 0));
        welcome.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        welcome.setPreferredSize(new Dimension(900, 88));
        welcome.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
        JLabel graduation = new JLabel(new PenjurusanIcon(
                PenjurusanIcon.GRADUATION, new Color(20, 69, 132), 54));
        graduation.setPreferredSize(new Dimension(86, 58));
        graduation.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel welcomeText = new JPanel(new BorderLayout(0, 5));
        welcomeText.setOpaque(false);
        dashboardMahasiswaNama = new JLabel();
        dashboardMahasiswaNama.setFont(new Font("SansSerif", Font.BOLD, 21));
        dashboardMahasiswaNama.setForeground(NAVY);
        JLabel subtitle = new JLabel(
                "Semoga semangat selalu dalam menempuh pendidikan di UNHASY.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(63, 83, 112));
        welcomeText.add(dashboardMahasiswaNama, BorderLayout.NORTH);
        welcomeText.add(subtitle, BorderLayout.CENTER);

        JPanel identity = roundedCard(new Color(216, 231, 251));
        identity.setLayout(new GridLayout(1, 3, 0, 0));
        identity.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        identity.setPreferredSize(new Dimension(370, 60));
        identity.setMaximumSize(new Dimension(410, 70));
        dashboardMahasiswaNim = new JLabel();
        dashboardMahasiswaSemester = new JLabel();
        dashboardMahasiswaProgram = new JLabel();
        identity.add(dashboardIdentitasItem("NIM", dashboardMahasiswaNim));
        identity.add(dashboardIdentitasItem("Semester", dashboardMahasiswaSemester));
        identity.add(dashboardIdentitasItem("Program Studi", dashboardMahasiswaProgram));
        welcome.add(graduation, BorderLayout.WEST);
        welcome.add(welcomeText, BorderLayout.CENTER);
        welcome.add(identity, BorderLayout.EAST);

        JPanel metrics = new JPanel(new GridLayout(1, 4, 12, 0));
        metrics.setOpaque(false);
        dashboardMahasiswaJumlahMataKuliah = new JLabel("0");
        dashboardMahasiswaNilaiTerverifikasi = new JLabel("0 / 0");
        dashboardMahasiswaIpk = new JLabel("—");
        dashboardMahasiswaStatusAkademik = new JLabel("Aktif");
        metrics.add(ringkasanMahasiswa("Mata Kuliah Diambil",
                dashboardMahasiswaJumlahMataKuliah, new Color(40, 145, 226),
                PenjurusanIcon.BOOK));
        metrics.add(ringkasanMahasiswa("Nilai Terverifikasi",
                dashboardMahasiswaNilaiTerverifikasi, new Color(49, 194, 139),
                PenjurusanIcon.CHECK));
        metrics.add(ringkasanMahasiswa("IPK Kumulatif",
                dashboardMahasiswaIpk, new Color(135, 85, 197),
                PenjurusanIcon.STAR));
        metrics.add(ringkasanMahasiswa("Status Akademik",
                dashboardMahasiswaStatusAkademik, new Color(244, 171, 40),
                PenjurusanIcon.CHART));
        metrics.setPreferredSize(new Dimension(900, 94));
        metrics.setMaximumSize(new Dimension(Integer.MAX_VALUE, 94));

        dashboardMahasiswaGrafik = new GrafikIpkMahasiswa();
        JPanel chartCard = card();
        chartCard.setLayout(new BorderLayout(0, 8));
        chartCard.setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));
        chartCard.setPreferredSize(new Dimension(520, 265));
        chartCard.add(dashboardSectionHeader("Ringkasan Nilai Semester",
                PenjurusanIcon.CHART, BLUE), BorderLayout.NORTH);
        chartCard.add(dashboardMahasiswaGrafik, BorderLayout.CENTER);
        JPanel chartLegend = new JPanel(new FlowLayout(FlowLayout.CENTER, 7, 0));
        chartLegend.setOpaque(false);
        JLabel legendIcon = new JLabel();
        legendIcon.setOpaque(true);
        legendIcon.setBackground(new Color(36, 124, 208));
        legendIcon.setPreferredSize(new Dimension(12, 12));
        JLabel legendText = new JLabel("IPK Semester");
        legendText.setFont(new Font("SansSerif", Font.PLAIN, 12));
        legendText.setForeground(TEXT);
        chartLegend.add(legendIcon);
        chartLegend.add(legendText);
        chartCard.add(chartLegend, BorderLayout.SOUTH);

        JPanel recommendationCard = card();
        recommendationCard.setLayout(new BorderLayout(0, 10));
        recommendationCard.setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));
        recommendationCard.setPreferredSize(new Dimension(500, 265));
        recommendationCard.add(dashboardSectionHeader("Rekomendasi Peminatan",
                PenjurusanIcon.LIGHTBULB, GOLD), BorderLayout.NORTH);
        JPanel recommendationBody = new JPanel(new BorderLayout(0, 12));
        recommendationBody.setOpaque(false);
        JPanel best = roundedCard(new Color(239, 245, 255));
        best.setLayout(new BorderLayout(12, 0));
        best.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JLabel trophy = new JLabel(new PenjurusanIcon(PenjurusanIcon.STAR,
                new Color(233, 164, 24), 34), SwingConstants.CENTER);
        trophy.setPreferredSize(new Dimension(50, 42));
        JPanel bestText = new JPanel(new BorderLayout(0, 4));
        bestText.setOpaque(false);
        dashboardMahasiswaRekomendasiStatus = new JLabel("Bidang Peminatan Terbaik");
        dashboardMahasiswaRekomendasiStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        dashboardMahasiswaRekomendasiStatus.setForeground(new Color(63, 83, 112));
        dashboardMahasiswaRekomendasiNama = new JLabel("Belum tersedia");
        dashboardMahasiswaRekomendasiNama.setFont(new Font("SansSerif", Font.BOLD, 19));
        dashboardMahasiswaRekomendasiNama.setForeground(NAVY);
        bestText.add(dashboardMahasiswaRekomendasiStatus, BorderLayout.NORTH);
        bestText.add(dashboardMahasiswaRekomendasiNama, BorderLayout.CENTER);
        dashboardMahasiswaRekomendasiSkor = new JLabel("Skor: —");
        dashboardMahasiswaRekomendasiSkor.setFont(new Font("SansSerif", Font.BOLD, 12));
        dashboardMahasiswaRekomendasiSkor.setForeground(Color.WHITE);
        dashboardMahasiswaRekomendasiSkor.setOpaque(true);
        dashboardMahasiswaRekomendasiSkor.setBackground(new Color(17, 57, 123));
        dashboardMahasiswaRekomendasiSkor.setBorder(
                BorderFactory.createEmptyBorder(8, 11, 8, 11));
        best.add(trophy, BorderLayout.WEST);
        best.add(bestText, BorderLayout.CENTER);
        best.add(dashboardMahasiswaRekomendasiSkor, BorderLayout.EAST);
        JPanel reasonPanel = new JPanel(new BorderLayout(0, 4));
        reasonPanel.setOpaque(false);
        JLabel reasonTitle = new JLabel("Alasan Rekomendasi:");
        reasonTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        reasonTitle.setForeground(NAVY);
        dashboardMahasiswaRekomendasiAlasan = new JLabel();
        dashboardMahasiswaRekomendasiAlasan.setFont(new Font("SansSerif", Font.PLAIN, 12));
        dashboardMahasiswaRekomendasiAlasan.setForeground(TEXT);
        reasonPanel.add(reasonTitle, BorderLayout.NORTH);
        reasonPanel.add(dashboardMahasiswaRekomendasiAlasan, BorderLayout.CENTER);
        recommendationBody.add(best, BorderLayout.NORTH);
        recommendationBody.add(reasonPanel, BorderLayout.CENTER);
        JButton detail = button("Lihat Detail", new Color(17, 57, 123));
        detail.setIcon(new PenjurusanIcon(PenjurusanIcon.CHART, Color.WHITE, 18));
        detail.addActionListener(event -> tampilkanHalaman("Rekomendasi Peminatan"));
        JPanel detailBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        detailBar.setOpaque(false);
        detailBar.add(detail);
        recommendationBody.add(detailBar, BorderLayout.SOUTH);
        recommendationCard.add(recommendationBody, BorderLayout.CENTER);

        JPanel analysisRow = new JPanel(new GridLayout(1, 2, 14, 0));
        analysisRow.setOpaque(false);
        analysisRow.add(chartCard);
        analysisRow.add(recommendationCard);
        analysisRow.setPreferredSize(new Dimension(900, 265));
        analysisRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 265));

        JPanel informationCard = card();
        informationCard.setLayout(new BorderLayout(12, 0));
        informationCard.setBorder(BorderFactory.createEmptyBorder(13, 16, 13, 16));
        informationCard.setPreferredSize(new Dimension(400, 94));
        informationCard.add(new JLabel(new PenjurusanIcon(PenjurusanIcon.INFO,
                new Color(22, 73, 139), 23)), BorderLayout.WEST);
        JPanel informationText = new JPanel(new BorderLayout(0, 5));
        informationText.setOpaque(false);
        JLabel informationTitle = new JLabel("Informasi Akademik");
        informationTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        informationTitle.setForeground(NAVY);
        JLabel informationMessage = new JLabel(
                "<html>Pastikan nilai Anda sudah diverifikasi Admin untuk mendapatkan "
                        + "rekomendasi peminatan yang akurat.</html>");
        informationMessage.setFont(new Font("SansSerif", Font.PLAIN, 12));
        informationMessage.setForeground(TEXT);
        informationText.add(informationTitle, BorderLayout.NORTH);
        informationText.add(informationMessage, BorderLayout.CENTER);
        informationCard.add(informationText, BorderLayout.CENTER);

        JPanel historyCard = card();
        historyCard.setLayout(new BorderLayout(12, 0));
        historyCard.setBorder(BorderFactory.createEmptyBorder(13, 16, 13, 16));
        historyCard.setPreferredSize(new Dimension(400, 94));
        historyCard.add(new JLabel(new PenjurusanIcon(PenjurusanIcon.CLIPBOARD,
                new Color(22, 73, 139), 23)), BorderLayout.WEST);
        JPanel historyText = new JPanel(new BorderLayout(0, 5));
        historyText.setOpaque(false);
        JLabel historyTitle = new JLabel("Riwayat Penjurusan");
        historyTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        historyTitle.setForeground(NAVY);
        dashboardMahasiswaRiwayat = new JLabel("Belum ada riwayat penjurusan.");
        dashboardMahasiswaRiwayat.setFont(new Font("SansSerif", Font.PLAIN, 12));
        dashboardMahasiswaRiwayat.setForeground(TEXT);
        historyText.add(historyTitle, BorderLayout.NORTH);
        historyText.add(dashboardMahasiswaRiwayat, BorderLayout.CENTER);
        historyCard.add(historyText, BorderLayout.CENTER);

        JPanel footerRow = new JPanel(new GridLayout(1, 2, 14, 0));
        footerRow.setOpaque(false);
        footerRow.add(informationCard);
        footerRow.add(historyCard);
        footerRow.setPreferredSize(new Dimension(900, 94));
        footerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 94));

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.add(welcome);
        stack.add(Box.createVerticalStrut(12));
        stack.add(metrics);
        stack.add(Box.createVerticalStrut(14));
        stack.add(analysisRow);
        stack.add(Box.createVerticalStrut(12));
        stack.add(footerRow);
        stack.setBorder(BorderFactory.createEmptyBorder(2, 2, 8, 2));
        JScrollPane dashboardScroll = new JScrollPane(stack,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        dashboardScroll.setBorder(BorderFactory.createEmptyBorder());
        dashboardScroll.setOpaque(false);
        dashboardScroll.getViewport().setOpaque(false);
        dashboardScroll.getVerticalScrollBar().setUnitIncrement(18);
        page.add(dashboardScroll, BorderLayout.CENTER);
        refreshDashboardMahasiswa();
        return page;
    }

    private JPanel dashboardIdentitasItem(String title, JLabel value) {
        JPanel item = new JPanel(new BorderLayout(0, 2));
        item.setOpaque(false);
        item.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        label.setForeground(new Color(63, 83, 112));
        value.setFont(new Font("SansSerif", Font.BOLD, 13));
        value.setForeground(NAVY);
        value.setHorizontalAlignment(SwingConstants.CENTER);
        item.add(label, BorderLayout.NORTH);
        item.add(value, BorderLayout.CENTER);
        return item;
    }

    private JLabel dashboardSectionHeader(String title, int iconType, Color accent) {
        JLabel heading = new JLabel(title,
                new PenjurusanIcon(iconType, accent, 20), SwingConstants.LEFT);
        heading.setFont(new Font("SansSerif", Font.BOLD, 15));
        heading.setForeground(NAVY);
        heading.setIconTextGap(9);
        return heading;
    }

    private JPanel ringkasanMahasiswa(String title, JLabel value, Color accent, int iconType) {
        JPanel panel = roundedCard(Color.WHITE);
        panel.setLayout(new BorderLayout(12, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(accent.getRed(), accent.getGreen(),
                        accent.getBlue(), 80)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        panel.setPreferredSize(new Dimension(200, 94));
        JPanel icon = roundedCard(new Color(accent.getRed(), accent.getGreen(),
                accent.getBlue(), 235));
        icon.setLayout(new BorderLayout());
        icon.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        icon.add(new JLabel(new PenjurusanIcon(iconType, Color.WHITE, 28),
                SwingConstants.CENTER), BorderLayout.CENTER);
        icon.setPreferredSize(new Dimension(62, 62));
        JPanel labels = new JPanel(new BorderLayout(0, 4));
        labels.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(new Color(63, 83, 112));
        value.setFont(new Font("SansSerif", Font.BOLD, 22));
        value.setForeground(NAVY);
        if (value == dashboardMahasiswaStatusAkademik) {
            value.setOpaque(true);
            value.setBackground(new Color(224, 242, 217));
            value.setForeground(new Color(34, 119, 62));
            value.setHorizontalAlignment(SwingConstants.CENTER);
            value.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        }
        labels.add(label, BorderLayout.NORTH);
        labels.add(value, BorderLayout.CENTER);
        panel.add(icon, BorderLayout.WEST);
        panel.add(labels, BorderLayout.CENTER);
        return panel;
    }

    private JPanel halamanMahasiswa(String title, String subtitle, int iconType) {
        JPanel page = createContentBackgroundPage(0, 12);
        page.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        JPanel hero = roundedCard(new Color(230, 241, 255));
        hero.setLayout(new BorderLayout(14, 0));
        hero.setBorder(BorderFactory.createEmptyBorder(11, 16, 11, 16));
        hero.setPreferredSize(new Dimension(760, 82));
        JPanel titleText = new JPanel(new BorderLayout(0, 4));
        titleText.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 21));
        titleLabel.setForeground(NAVY);
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(63, 83, 112));
        titleText.add(titleLabel, BorderLayout.NORTH);
        titleText.add(subtitleLabel, BorderLayout.CENTER);
        hero.add(createGradientTitleIcon(iconType), BorderLayout.WEST);
        hero.add(titleText, BorderLayout.CENTER);
        page.add(hero, BorderLayout.NORTH);
        return page;
    }

    private JScrollPane gulirMahasiswa(JComponent content) {
        JScrollPane scroll = new JScrollPane(content,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    private JPanel tumpukMahasiswa(JComponent... components) {
        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        for (int i = 0; i < components.length; i++) {
            JComponent component = components[i];
            component.setAlignmentX(Component.LEFT_ALIGNMENT);
            component.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                    Math.max(component.getPreferredSize().height, 1)));
            stack.add(component);
            if (i < components.length - 1) stack.add(Box.createVerticalStrut(12));
        }
        stack.setBorder(BorderFactory.createEmptyBorder(2, 2, 8, 2));
        return stack;
    }

    private JPanel avatarMahasiswa(String nama) {
        String initial = nama == null || nama.trim().isEmpty() ? "M"
                : nama.trim().substring(0, 1).toUpperCase(java.util.Locale.ROOT);
        JPanel avatar = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int diameter = Math.min(getWidth(), getHeight());
                g.setPaint(new java.awt.GradientPaint(0, 0,
                        new Color(50, 151, 231), diameter, diameter,
                        new Color(17, 57, 123)));
                g.fillOval((getWidth() - diameter) / 2, (getHeight() - diameter) / 2,
                        diameter, diameter);
                g.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(78, 78));
        JLabel initialLabel = new JLabel(initial, SwingConstants.CENTER);
        initialLabel.setForeground(Color.WHITE);
        initialLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        avatar.add(initialLabel);
        return avatar;
    }

    private JPanel kartuDetailProfil(String title, String value, int iconType, Color accent) {
        JPanel panel = card();
        panel.setLayout(new BorderLayout(12, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        JLabel icon = new JLabel(new PenjurusanIcon(iconType, accent, 25));
        icon.setPreferredSize(new Dimension(34, 34));
        JPanel labels = new JPanel(new BorderLayout(0, 4));
        labels.setOpaque(false);
        JLabel name = new JLabel(title);
        name.setFont(new Font("SansSerif", Font.PLAIN, 12));
        name.setForeground(new Color(92, 109, 132));
        JLabel detail = new JLabel(value);
        detail.setFont(new Font("SansSerif", Font.BOLD, 14));
        detail.setForeground(NAVY);
        labels.add(name, BorderLayout.NORTH);
        labels.add(detail, BorderLayout.CENTER);
        panel.add(icon, BorderLayout.WEST);
        panel.add(labels, BorderLayout.CENTER);
        return panel;
    }

    private JPanel kartuLangkahMahasiswa(String number, String title, String description,
                                         Color accent, int iconType) {
        JPanel panel = card();
        panel.setLayout(new BorderLayout(10, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        JPanel heading = new JPanel(new BorderLayout(9, 0));
        heading.setOpaque(false);
        JLabel icon = new JLabel(new PenjurusanIcon(iconType, accent, 22));
        JLabel step = new JLabel("LANGKAH " + number);
        step.setFont(new Font("SansSerif", Font.BOLD, 10));
        step.setForeground(accent);
        heading.add(icon, BorderLayout.WEST);
        heading.add(step, BorderLayout.CENTER);
        JLabel name = new JLabel(title);
        name.setFont(new Font("SansSerif", Font.BOLD, 13));
        name.setForeground(NAVY);
        JLabel detail = new JLabel("<html><div style='width:210px'>"
                + escapeHtml(description) + "</div></html>");
        detail.setFont(new Font("SansSerif", Font.PLAIN, 11));
        detail.setForeground(new Color(82, 100, 124));
        JPanel text = new JPanel(new BorderLayout(0, 3));
        text.setOpaque(false);
        text.add(name, BorderLayout.NORTH);
        text.add(detail, BorderLayout.CENTER);
        panel.add(heading, BorderLayout.NORTH);
        panel.add(text, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(245, 104));
        return panel;
    }

    private JPanel createProfilSayaPage() {
        JPanel page = halamanMahasiswa("Profil Saya",
                "Informasi akun dan identitas akademik pribadi.", PenjurusanIcon.USER);
        Mahasiswa mahasiswa = dataService.profilMahasiswa(userSession);
        String minatAwal = mahasiswa.getMinatAwal();
        if (minatAwal == null || minatAwal.trim().isEmpty()) {
            minatAwal = "Belum memilih bidang";
        }
        String angkatan = mahasiswa.getTahunAngkatan() == 0
                ? "—" : String.valueOf(mahasiswa.getTahunAngkatan());
        JPanel summary = card();
        summary.setLayout(new BorderLayout(16, 0));
        summary.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        summary.setPreferredSize(new Dimension(760, 122));
        summary.add(avatarMahasiswa(mahasiswa.getNama()), BorderLayout.WEST);
        JPanel identity = new JPanel(new BorderLayout(0, 6));
        identity.setOpaque(false);
        JLabel name = new JLabel(mahasiswa.getNama());
        name.setFont(new Font("SansSerif", Font.BOLD, 21));
        name.setForeground(NAVY);
        JLabel nim = new JLabel(mahasiswa.getNim() + "  •  " + mahasiswa.getProgramStudi());
        nim.setFont(new Font("SansSerif", Font.PLAIN, 13));
        nim.setForeground(new Color(81, 99, 123));
        identity.add(name, BorderLayout.NORTH);
        identity.add(nim, BorderLayout.CENTER);
        summary.add(identity, BorderLayout.CENTER);
        JLabel active = new JLabel("AKTIF", SwingConstants.CENTER);
        active.setOpaque(true);
        active.setBackground(new Color(224, 242, 217));
        active.setForeground(new Color(34, 119, 62));
        active.setFont(new Font("SansSerif", Font.BOLD, 12));
        active.setBorder(BorderFactory.createEmptyBorder(7, 13, 7, 13));
        summary.add(active, BorderLayout.EAST);

        JPanel details = new JPanel(new GridLayout(0, 2, 12, 12));
        details.setOpaque(false);
        details.add(kartuDetailProfil("Nama Lengkap", mahasiswa.getNama(),
                PenjurusanIcon.USER, BLUE));
        details.add(kartuDetailProfil("NIM", mahasiswa.getNim(),
                PenjurusanIcon.DOCUMENT, TEAL));
        details.add(kartuDetailProfil("Program Studi", mahasiswa.getProgramStudi(),
                PenjurusanIcon.BOOK, new Color(136, 88, 194)));
        details.add(kartuDetailProfil("Semester", String.valueOf(mahasiswa.getSemester()),
                PenjurusanIcon.CHART, GOLD));
        details.add(kartuDetailProfil("Tahun Angkatan", angkatan,
                PenjurusanIcon.CLIPBOARD, new Color(46, 143, 111)));
        details.add(kartuDetailProfil("Minat Awal", minatAwal,
                PenjurusanIcon.STAR, new Color(136, 88, 194)));
        details.setPreferredSize(new Dimension(760, 230));

        JPanel privacyNote = roundedCard(new Color(232, 241, 255));
        privacyNote.setLayout(new BorderLayout(10, 0));
        privacyNote.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        privacyNote.setPreferredSize(new Dimension(760, 56));
        privacyNote.add(new JLabel(new PenjurusanIcon(PenjurusanIcon.INFO, BLUE, 20)),
                BorderLayout.WEST);
        JLabel privacyText = new JLabel(
                "Data profil ini hanya dapat dilihat oleh akun mahasiswa yang sedang masuk.");
        privacyText.setFont(new Font("SansSerif", Font.PLAIN, 12));
        privacyText.setForeground(TEXT);
        privacyNote.add(privacyText, BorderLayout.CENTER);

        page.add(gulirMahasiswa(tumpukMahasiswa(summary, details, privacyNote)),
                BorderLayout.CENTER);
        return page;
    }

    private JPanel createNilaiSayaPage() {
        JPanel page = halamanMahasiswa("Mata Kuliah & Nilai",
                "Pantau nilai terverifikasi dan status pengajuan setiap mata kuliah.",
                PenjurusanIcon.BOOK);
        nilaiSayaTable.setRowHeight(42);
        nilaiSayaTable.setFillsViewportHeight(true);
        nilaiSayaTable.getTableHeader().setBackground(NAVY);
        nilaiSayaTable.getTableHeader().setForeground(Color.WHITE);
        nilaiSayaTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        nilaiSayaTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        setStatusRenderer(nilaiSayaTable, 4);

        JPanel metrics = new JPanel(new GridLayout(1, 4, 12, 0));
        metrics.setOpaque(false);
        nilaiSayaTotalMataKuliah = new JLabel("0");
        nilaiSayaTerverifikasi = new JLabel("0");
        nilaiSayaMenunggu = new JLabel("0");
        nilaiSayaBelumDiinput = new JLabel("0");
        metrics.add(ringkasanMahasiswa("Mata Kuliah", nilaiSayaTotalMataKuliah,
                BLUE, PenjurusanIcon.BOOK));
        metrics.add(ringkasanMahasiswa("Terverifikasi", nilaiSayaTerverifikasi,
                new Color(49, 194, 139), PenjurusanIcon.CHECK));
        metrics.add(ringkasanMahasiswa("Menunggu Admin", nilaiSayaMenunggu,
                GOLD, PenjurusanIcon.INFO));
        metrics.add(ringkasanMahasiswa("Belum Diinput", nilaiSayaBelumDiinput,
                new Color(136, 88, 194), PenjurusanIcon.DOCUMENT));
        metrics.setPreferredSize(new Dimension(780, 94));

        JPanel tableCard = card();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        tableCard.add(dashboardSectionHeader("Daftar Mata Kuliah",
                PenjurusanIcon.BOOK, BLUE), BorderLayout.NORTH);
        tableCard.add(tableScrollPane(nilaiSayaTable), BorderLayout.CENTER);
        JLabel note = new JLabel(
                "Nilai berstatus Menunggu Admin belum digunakan dalam perhitungan rekomendasi.");
        note.setFont(new Font("SansSerif", Font.PLAIN, 12));
        note.setForeground(new Color(82, 100, 124));
        tableCard.add(note, BorderLayout.SOUTH);
        page.add(gulirMahasiswa(tumpukMahasiswa(metrics, tableCard)), BorderLayout.CENTER);
        return page;
    }

    private JPanel createInputNilaiMahasiswaPage() {
        JPanel page = halamanMahasiswa("Input Nilai",
                "Ajukan nilai pribadi untuk ditinjau dan diverifikasi Admin.",
                PenjurusanIcon.DOCUMENT);
        JPanel steps = new JPanel(new GridLayout(1, 3, 12, 0));
        steps.setOpaque(false);
        steps.add(kartuLangkahMahasiswa("01", "Pilih Mata Kuliah",
                "Pilih mata kuliah yang tersedia sesuai semester Anda.", BLUE,
                PenjurusanIcon.BOOK));
        steps.add(kartuLangkahMahasiswa("02", "Masukkan Nilai",
                "Pilih nilai huruf sesuai hasil akademik Anda.", TEAL,
                PenjurusanIcon.DOCUMENT));
        steps.add(kartuLangkahMahasiswa("03", "Menunggu Verifikasi",
                "Admin akan memeriksa nilai sebelum digunakan.", GOLD,
                PenjurusanIcon.CHECK));
        steps.setPreferredSize(new Dimension(780, 102));

        JPanel form = card();
        form.setLayout(new BorderLayout(0, 14));
        form.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        form.add(dashboardSectionHeader("Formulir Pengajuan Nilai",
                PenjurusanIcon.CLIPBOARD, BLUE), BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        int row = 0;
        row = addField(fields, row, "Mata Kuliah", inputNilaiMataKuliahMahasiswa);
        row = addField(fields, row, "Nilai Huruf", inputNilaiHurufMahasiswa);
        inputNilaiStatusMahasiswa.setForeground(TEXT);
        row = addField(fields, row, "Status", inputNilaiStatusMahasiswa);
        JLabel helper = new JLabel(
                "<html><div style='width:520px'>Setelah dikirim, nilai akan berstatus "
                        + "<b>Menunggu Admin</b>. Nilai tersebut belum memengaruhi rekomendasi "
                        + "sampai diverifikasi.</div></html>");
        helper.setFont(new Font("SansSerif", Font.PLAIN, 12));
        helper.setForeground(new Color(82, 100, 124));
        row = addField(fields, row, "Informasi", helper);
        form.add(fields, BorderLayout.CENTER);

        JButton submit = button("Kirim Pengajuan", BLUE);
        submit.setIcon(new PenjurusanIcon(PenjurusanIcon.CHECK, Color.WHITE, 18));
        submit.setIconTextGap(8);
        submit.addActionListener(event -> ajukanNilaiMahasiswa());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        actions.add(submit);
        form.add(actions, BorderLayout.SOUTH);
        page.add(gulirMahasiswa(tumpukMahasiswa(steps, form)), BorderLayout.CENTER);
        return page;
    }

    private JPanel createPilihBidangMahasiswaPage() {
        JPanel page = halamanMahasiswa("Pilih Bidang Peminatan",
                "Pilih bidang yang menarik bagi Anda sebagai preferensi pribadi.",
                PenjurusanIcon.BRIEFCASE);
        JPanel form = card();
        form.setLayout(new BorderLayout(0, 14));
        form.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        form.add(dashboardSectionHeader("Pilih Bidang yang Diminati",
                PenjurusanIcon.STAR, GOLD), BorderLayout.NORTH);
        JPanel choices = new JPanel(new GridLayout(1, 3, 12, 0));
        choices.setOpaque(false);
        Mahasiswa mahasiswa = dataService.profilMahasiswa(userSession);
        minatMahasiswaField.setSelectedItem(
                RekomendasiService.BIDANG.contains(mahasiswa.getMinatAwal())
                        ? mahasiswa.getMinatAwal() : null);
        int[] icons = {PenjurusanIcon.USER, PenjurusanIcon.BRIEFCASE,
            PenjurusanIcon.CHECKLIST};
        for (int i = 0; i < RekomendasiService.BIDANG.size(); i++) {
            String field = RekomendasiService.BIDANG.get(i);
            JToggleButton choice = new JToggleButton(
                    "<html><div style='width:175px'><b>" + escapeHtml(field)
                            + "</b><br><br><span style='font-weight:normal'>"
                            + escapeHtml(deskripsiBidang(field)) + "</span></div></html>",
                    new PenjurusanIcon(icons[i], BLUE, 28));
            choice.setHorizontalAlignment(SwingConstants.LEFT);
            choice.setVerticalAlignment(SwingConstants.TOP);
            choice.setHorizontalTextPosition(SwingConstants.RIGHT);
            choice.setVerticalTextPosition(SwingConstants.TOP);
            choice.setIconTextGap(12);
            choice.setFont(new Font("SansSerif", Font.PLAIN, 13));
            choice.setForeground(NAVY);
            choice.setBackground(Color.WHITE);
            choice.setOpaque(true);
            choice.setFocusPainted(false);
            choice.setPreferredSize(new Dimension(240, 150));
            choice.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(205, 217, 231), 1),
                    BorderFactory.createEmptyBorder(14, 14, 14, 14)));
            choice.addActionListener(event -> {
                minatMahasiswaField.setSelectedItem(field);
                refreshPilihanBidangMahasiswaCards();
                pilihanBidangMahasiswaStatus.setText("Pilihan sementara: " + field);
            });
            pilihanBidangMahasiswaCards.put(field, choice);
            choices.add(choice);
        }
        choices.setPreferredSize(new Dimension(780, 152));
        form.add(choices, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setOpaque(false);
        pilihanBidangMahasiswaStatus = new JLabel(
                "Pilihan ini adalah preferensi dan tidak mengubah hasil penjurusan.");
        pilihanBidangMahasiswaStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        pilihanBidangMahasiswaStatus.setForeground(TEXT);
        footer.add(pilihanBidangMahasiswaStatus, BorderLayout.CENTER);
        JButton save = button("Simpan Pilihan", BLUE);
        save.setIcon(new PenjurusanIcon(PenjurusanIcon.CHECK, Color.WHITE, 18));
        save.setIconTextGap(7);
        footer.add(save, BorderLayout.EAST);
        form.add(footer, BorderLayout.SOUTH);
        save.addActionListener(event -> {
            String selectedField = (String) minatMahasiswaField.getSelectedItem();
            if (!RekomendasiService.BIDANG.contains(selectedField)) {
                showWarning("Pilih bidang peminatan terlebih dahulu.");
                return;
            }
            try {
                dataService.pilihBidangMahasiswa(userSession, selectedField);
                pilihanBidangMahasiswaStatus.setText("Pilihan berhasil disimpan.");
                refreshDashboardMahasiswa();
            } catch (IllegalArgumentException ex) {
                showWarning(ex.getMessage());
            } catch (IOException ex) {
                showError("Pilihan bidang tidak dapat disimpan: " + ex.getMessage());
            }
        });
        JPanel note = roundedCard(new Color(232, 241, 255));
        note.setLayout(new BorderLayout(10, 0));
        note.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        note.setPreferredSize(new Dimension(780, 55));
        note.add(new JLabel(new PenjurusanIcon(PenjurusanIcon.INFO, BLUE, 21)),
                BorderLayout.WEST);
        JLabel noteText = new JLabel(
                "Rekomendasi tetap dihitung dari nilai terverifikasi dan prasyarat tiap bidang.");
        noteText.setFont(new Font("SansSerif", Font.PLAIN, 12));
        noteText.setForeground(TEXT);
        note.add(noteText, BorderLayout.CENTER);
        page.add(gulirMahasiswa(tumpukMahasiswa(note, form)), BorderLayout.CENTER);
        refreshPilihanBidangMahasiswaCards();
        return page;
    }

    private void refreshPilihanBidangMahasiswaCards() {
        String selected = (String) minatMahasiswaField.getSelectedItem();
        for (Map.Entry<String, JToggleButton> entry : pilihanBidangMahasiswaCards.entrySet()) {
            boolean active = entry.getKey().equals(selected);
            JToggleButton card = entry.getValue();
            card.setSelected(active);
            card.setBackground(active ? new Color(225, 239, 255) : Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(active ? BLUE : new Color(205, 217, 231),
                            active ? 2 : 1),
                    BorderFactory.createEmptyBorder(13, 13, 13, 13)));
        }
    }

    private JPanel createRekomendasiMahasiswaPage() {
        JPanel page = halamanMahasiswa("Rekomendasi Peminatan",
                "Lihat kecocokan bidang berdasarkan nilai yang sudah diverifikasi Admin.",
                PenjurusanIcon.GRADUATION);
        rekomendasiSayaTable.setRowHeight(44);
        rekomendasiSayaTable.setFillsViewportHeight(true);
        rekomendasiSayaTable.getTableHeader().setBackground(NAVY);
        rekomendasiSayaTable.getTableHeader().setForeground(Color.WHITE);
        rekomendasiSayaTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        rekomendasiSayaTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        setStatusRenderer(rekomendasiSayaTable, 3);
        rekomendasiSayaDetail.setEditable(false);
        rekomendasiSayaDetail.setLineWrap(true);
        rekomendasiSayaDetail.setWrapStyleWord(true);
        rekomendasiSayaDetail.setBackground(new Color(238, 244, 255));
        rekomendasiSayaDetail.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JPanel highlight = roundedCard(new Color(230, 241, 255));
        highlight.setLayout(new BorderLayout(14, 0));
        highlight.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        highlight.setPreferredSize(new Dimension(780, 112));
        JLabel highlightIcon = new JLabel(new PenjurusanIcon(
                PenjurusanIcon.STAR, GOLD, 42), SwingConstants.CENTER);
        highlightIcon.setPreferredSize(new Dimension(54, 54));
        JPanel highlightText = new JPanel(new BorderLayout(0, 5));
        highlightText.setOpaque(false);
        rekomendasiMahasiswaStatus = new JLabel("Rekomendasi utama");
        rekomendasiMahasiswaStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        rekomendasiMahasiswaStatus.setForeground(new Color(70, 91, 120));
        rekomendasiMahasiswaNama = new JLabel("Belum tersedia");
        rekomendasiMahasiswaNama.setFont(new Font("SansSerif", Font.BOLD, 20));
        rekomendasiMahasiswaNama.setForeground(NAVY);
        rekomendasiMahasiswaAlasan = new JLabel();
        rekomendasiMahasiswaAlasan.setFont(new Font("SansSerif", Font.PLAIN, 12));
        rekomendasiMahasiswaAlasan.setForeground(new Color(70, 91, 120));
        highlightText.add(rekomendasiMahasiswaStatus, BorderLayout.NORTH);
        highlightText.add(rekomendasiMahasiswaNama, BorderLayout.CENTER);
        highlightText.add(rekomendasiMahasiswaAlasan, BorderLayout.SOUTH);
        rekomendasiMahasiswaSkor = new JLabel("Skor: —", SwingConstants.CENTER);
        rekomendasiMahasiswaSkor.setFont(new Font("SansSerif", Font.BOLD, 13));
        rekomendasiMahasiswaSkor.setForeground(Color.WHITE);
        rekomendasiMahasiswaSkor.setOpaque(true);
        rekomendasiMahasiswaSkor.setBackground(new Color(17, 57, 123));
        rekomendasiMahasiswaSkor.setBorder(BorderFactory.createEmptyBorder(9, 13, 9, 13));
        highlight.add(highlightIcon, BorderLayout.WEST);
        highlight.add(highlightText, BorderLayout.CENTER);
        highlight.add(rekomendasiMahasiswaSkor, BorderLayout.EAST);

        JPanel results = card();
        results.setLayout(new BorderLayout(0, 10));
        results.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        results.add(dashboardSectionHeader("Peringkat Bidang Peminatan",
                PenjurusanIcon.CHART, BLUE), BorderLayout.NORTH);
        results.add(tableScrollPane(rekomendasiSayaTable), BorderLayout.CENTER);
        results.setPreferredSize(new Dimension(780, 250));
        JPanel detail = card();
        detail.setLayout(new BorderLayout(0, 9));
        detail.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        detail.add(dashboardSectionHeader("Penjelasan Hasil",
                PenjurusanIcon.INFO, TEAL), BorderLayout.NORTH);
        JScrollPane detailScroll = new JScrollPane(rekomendasiSayaDetail,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        detailScroll.setBorder(BorderFactory.createLineBorder(new Color(218, 228, 241)));
        detail.add(detailScroll, BorderLayout.CENTER);
        detail.setPreferredSize(new Dimension(780, 150));

        JButton refresh = button("Perbarui Rekomendasi", BLUE);
        refresh.setIcon(new PenjurusanIcon(PenjurusanIcon.CHART, Color.WHITE, 18));
        refresh.setIconTextGap(8);
        refresh.addActionListener(event -> refreshRekomendasiMahasiswa());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        actions.add(refresh);
        page.add(gulirMahasiswa(tumpukMahasiswa(highlight, actions, results, detail)),
                BorderLayout.CENTER);
        return page;
    }

    private JPanel createRiwayatMahasiswaPage() {
        JPanel page = halamanMahasiswa("Riwayat Penjurusan",
                "Lihat catatan penjurusan yang terhubung dengan akun Anda.",
                PenjurusanIcon.CLIPBOARD);
        riwayatSayaTable.setRowHeight(46);
        riwayatSayaTable.setFillsViewportHeight(true);
        riwayatSayaTable.getTableHeader().setBackground(NAVY);
        riwayatSayaTable.getTableHeader().setForeground(Color.WHITE);
        riwayatSayaTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        riwayatSayaTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        setStatusRenderer(riwayatSayaTable, 3);

        JPanel summary = roundedCard(new Color(230, 241, 255));
        summary.setLayout(new BorderLayout(12, 0));
        summary.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        summary.setPreferredSize(new Dimension(780, 90));
        summary.add(new JLabel(new PenjurusanIcon(PenjurusanIcon.GRADUATION,
                BLUE, 34)), BorderLayout.WEST);
        JPanel summaryText = new JPanel(new BorderLayout(0, 4));
        summaryText.setOpaque(false);
        JLabel title = new JLabel("Perjalanan Penjurusan Anda");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(NAVY);
        riwayatMahasiswaStatus = new JLabel("Belum ada riwayat penjurusan.");
        riwayatMahasiswaStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        riwayatMahasiswaStatus.setForeground(new Color(70, 91, 120));
        summaryText.add(title, BorderLayout.NORTH);
        summaryText.add(riwayatMahasiswaStatus, BorderLayout.CENTER);
        summary.add(summaryText, BorderLayout.CENTER);
        riwayatMahasiswaJumlah = new JLabel("0 Riwayat", SwingConstants.CENTER);
        riwayatMahasiswaJumlah.setFont(new Font("SansSerif", Font.BOLD, 13));
        riwayatMahasiswaJumlah.setForeground(Color.WHITE);
        riwayatMahasiswaJumlah.setOpaque(true);
        riwayatMahasiswaJumlah.setBackground(new Color(17, 57, 123));
        riwayatMahasiswaJumlah.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        summary.add(riwayatMahasiswaJumlah, BorderLayout.EAST);

        JPanel tableCard = card();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        tableCard.add(dashboardSectionHeader("Daftar Riwayat",
                PenjurusanIcon.CLIPBOARD, BLUE), BorderLayout.NORTH);
        tableCard.add(tableScrollPane(riwayatSayaTable), BorderLayout.CENTER);
        tableCard.setPreferredSize(new Dimension(780, 330));
        page.add(gulirMahasiswa(tumpukMahasiswa(summary, tableCard)),
                BorderLayout.CENTER);
        return page;
    }

    private void refreshHalamanMahasiswa(String namaHalaman) {
        switch (namaHalaman) {
            case "Dashboard":
                refreshDashboardMahasiswa();
                break;
            case "Mata Kuliah & Nilai":
                refreshNilaiSaya();
                break;
            case "Input Nilai":
                refreshFormNilaiMahasiswa();
                break;
            case "Pilih Bidang Peminatan":
                Mahasiswa mahasiswa = dataService.profilMahasiswa(userSession);
                if (RekomendasiService.BIDANG.contains(mahasiswa.getMinatAwal())) {
                    minatMahasiswaField.setSelectedItem(mahasiswa.getMinatAwal());
                } else {
                    minatMahasiswaField.setSelectedIndex(-1);
                }
                refreshPilihanBidangMahasiswaCards();
                break;
            case "Rekomendasi Peminatan":
                refreshRekomendasiMahasiswa();
                break;
            case "Riwayat Penjurusan":
                refreshRiwayatMahasiswa();
                break;
            default:
                break;
        }
    }

    private void refreshDashboardMahasiswa() {
        if (dashboardMahasiswaNama == null) return;
        Mahasiswa mahasiswa = dataService.profilMahasiswa(userSession);
        List<NilaiMataKuliah> nilai = dataService.nilaiMahasiswa(userSession);
        List<NilaiMataKuliah> verified = new ArrayList<>();
        for (NilaiMataKuliah item : nilai) {
            if (item.isTerverifikasi() && !item.getNilaiHuruf().isEmpty()) verified.add(item);
        }
        List<MataKuliah> currentCourses = new ArrayList<>();
        for (MataKuliah course : dataService.mataKuliahMahasiswa(userSession)) {
            if (course.getSemester() == mahasiswa.getSemester()) currentCourses.add(course);
        }
        long verifiedCurrentSemester = verified.stream()
                .filter(item -> item.getSemester() == mahasiswa.getSemester()).count();
        dashboardMahasiswaNama.setText("Selamat datang, " + mahasiswa.getNama() + "!");
        dashboardMahasiswaNim.setText(mahasiswa.getNim());
        dashboardMahasiswaSemester.setText(String.valueOf(mahasiswa.getSemester()));
        dashboardMahasiswaProgram.setText(mahasiswa.getProgramStudi());
        dashboardMahasiswaJumlahMataKuliah.setText(String.valueOf(currentCourses.size()));
        dashboardMahasiswaNilaiTerverifikasi.setText(verifiedCurrentSemester
                + " / " + currentCourses.size());
        int totalCredits = 0;
        int totalPoints = 0;
        Map<Integer, int[]> semesterPoints = new LinkedHashMap<>();
        for (int semester = 1; semester < mahasiswa.getSemester(); semester++) {
            semesterPoints.put(semester, new int[2]);
        }
        for (NilaiMataKuliah item : verified) {
            int points = nilaiAngkaAkademik(item.getNilaiHuruf());
            totalCredits += item.getSks();
            totalPoints += points * item.getSks();
            int[] summary = semesterPoints.get(item.getSemester());
            if (summary != null) {
                summary[0] += points * item.getSks();
                summary[1] += item.getSks();
            }
        }
        dashboardMahasiswaIpk.setText(totalCredits == 0 ? "—"
                : String.format(java.util.Locale.ROOT, "%.2f",
                        (double) totalPoints / totalCredits));
        List<String> semesterLabels = new ArrayList<>();
        List<Double> semesterIpk = new ArrayList<>();
        for (Map.Entry<Integer, int[]> entry : semesterPoints.entrySet()) {
            semesterLabels.add("Sem " + entry.getKey());
            semesterIpk.add(entry.getValue()[1] == 0 ? 0d
                    : (double) entry.getValue()[0] / entry.getValue()[1]);
        }
        dashboardMahasiswaGrafik.setData(semesterLabels, semesterIpk);

        RekomendasiService.HasilRekomendasi recommendation =
                dataService.rekomendasiMahasiswa(userSession);
        if (recommendation.getSkor().isEmpty()) {
            dashboardMahasiswaRekomendasiStatus.setText("Rekomendasi belum tersedia");
            dashboardMahasiswaRekomendasiNama.setText("Belum tersedia");
            dashboardMahasiswaRekomendasiSkor.setText("Skor: —");
            dashboardMahasiswaRekomendasiAlasan.setText(
                    "<html><div style='width:320px'>Belum ada bidang peminatan yang dapat dianalisis.</div></html>");
        } else {
            RiwayatPenjurusan pilihan = dataService.penjurusanMahasiswa(mahasiswa.getNim());
            String bidangGagal = pilihan != null && !pilihan.isLayak()
                    ? pilihan.getBidang() : "";
            RekomendasiService.SkorBidang top = recommendation.getSkor().stream()
                    .filter(item -> !item.getBidang().equals(bidangGagal))
                    .findFirst().orElse(recommendation.getSkor().get(0));
            dashboardMahasiswaRekomendasiStatus.setText(!bidangGagal.isEmpty()
                    ? "Rekomendasi Alternatif"
                    : top.isLayak() ? "Bidang Peminatan Terbaik"
                            : "Rekomendasi Teratas — belum memenuhi syarat");
            dashboardMahasiswaRekomendasiNama.setText(top.getBidang());
            dashboardMahasiswaRekomendasiSkor.setText(String.format(
                    java.util.Locale.ROOT, "Skor: %.1f%%", top.getRataRata() * 25d));
            String reason = !bidangGagal.isEmpty()
                    ? top.getJumlahMataKuliahLulus() + "/"
                            + top.getJumlahMataKuliahPendukung()
                            + " mata kuliah pendukung lulus. "
                            + String.join(" ", top.getAlasan())
                    : top.getAlasan().isEmpty()
                    ? "Lengkapi nilai yang belum diverifikasi untuk memperbarui rekomendasi."
                    : String.join(" ", top.getAlasan());
            dashboardMahasiswaRekomendasiAlasan.setText("<html><div style='width:340px'>"
                    + escapeHtml(reason) + "</div></html>");
        }

        List<RiwayatPenjurusan> history = dataService.riwayatPenjurusanMahasiswa(userSession);
        RiwayatPenjurusan latest = null;
        for (RiwayatPenjurusan item : history) {
            if (latest == null || item.getTanggal().isAfter(latest.getTanggal())) latest = item;
        }
        dashboardMahasiswaRiwayat.setText(latest == null
                ? "Belum ada riwayat penjurusan."
                : latest.getBidang() + "  •  " + latest.getTanggal());
    }

    private int nilaiAngkaAkademik(String nilaiHuruf) {
        switch (nilaiHuruf) {
            case "A": return 4;
            case "B": return 3;
            case "C": return 2;
            case "D": return 1;
            default: return 0;
        }
    }

    private void refreshNilaiSaya() {
        clearModel(nilaiSayaModel);
        Map<String, NilaiMataKuliah> nilaiByCourse = new HashMap<>();
        for (NilaiMataKuliah nilai : dataService.nilaiMahasiswa(userSession)) {
            nilaiByCourse.put(nilai.getKode(), nilai);
        }
        List<MataKuliah> courses = dataService.mataKuliahMahasiswa(userSession);
        int verified = 0;
        int pending = 0;
        int notEntered = 0;
        for (MataKuliah course : courses) {
            NilaiMataKuliah nilai = nilaiByCourse.get(course.getKode());
            String grade = nilai == null ? "—" : nilai.getNilaiHuruf();
            String status;
            if (nilai == null) {
                status = "Belum diinput";
                notEntered++;
            } else if (nilai.isTerverifikasi()) {
                status = "Terverifikasi";
                verified++;
            } else {
                status = "Menunggu Admin";
                pending++;
            }
            nilaiSayaModel.addRow(new Object[]{course.getSemester(), course.getKode(),
                course.getNama(), grade, status});
        }
        nilaiSayaTotalMataKuliah.setText(String.valueOf(courses.size()));
        nilaiSayaTerverifikasi.setText(String.valueOf(verified));
        nilaiSayaMenunggu.setText(String.valueOf(pending));
        nilaiSayaBelumDiinput.setText(String.valueOf(notEntered));
    }

    private void refreshFormNilaiMahasiswa() {
        MataKuliah selected = (MataKuliah) inputNilaiMataKuliahMahasiswa.getSelectedItem();
        inputNilaiMataKuliahMahasiswa.removeAllItems();
        for (MataKuliah course : dataService.mataKuliahMahasiswa(userSession)) {
            inputNilaiMataKuliahMahasiswa.addItem(course);
        }
        if (selected != null) inputNilaiMataKuliahMahasiswa.setSelectedItem(selected);
    }

    private void ajukanNilaiMahasiswa() {
        MataKuliah course = (MataKuliah) inputNilaiMataKuliahMahasiswa.getSelectedItem();
        String grade = (String) inputNilaiHurufMahasiswa.getSelectedItem();
        if (course == null || grade == null) {
            showWarning("Pilih mata kuliah dan nilai terlebih dahulu.");
            return;
        }
        try {
            dataService.ajukanNilaiMahasiswa(userSession, course.getKode(), grade);
            inputNilaiStatusMahasiswa.setText("Nilai diajukan dan menunggu verifikasi Admin.");
            refreshNilaiSaya();
            refreshDashboardMahasiswa();
        } catch (IllegalArgumentException ex) {
            showWarning(ex.getMessage());
        } catch (IOException ex) {
            showError("Pengajuan nilai tidak dapat disimpan: " + ex.getMessage());
        }
    }

    private void refreshRekomendasiMahasiswa() {
        clearModel(rekomendasiSayaModel);
        Mahasiswa mahasiswa = dataService.profilMahasiswa(userSession);
        RekomendasiService.HasilRekomendasi hasil =
                dataService.rekomendasiMahasiswa(userSession);
        List<RekomendasiService.SkorBidang> skor = hasil.getSkor();
        RiwayatPenjurusan pilihan = dataService.penjurusanMahasiswa(mahasiswa.getNim());
        String bidangGagal = pilihan != null && !pilihan.isLayak()
                ? pilihan.getBidang() : "";
        RekomendasiService.SkorBidang alternatifUtama = skor.stream()
                .filter(item -> !item.getBidang().equals(bidangGagal))
                .findFirst().orElse(null);
        for (int i = 0; i < skor.size(); i++) {
            RekomendasiService.SkorBidang item = skor.get(i);
            String peringkat;
            if (item.getBidang().equals(bidangGagal)) {
                peringkat = "Bidang tetap — belum memenuhi";
            } else if (item == alternatifUtama && !bidangGagal.isEmpty()) {
                peringkat = "Rekomendasi alternatif";
            } else {
                peringkat = item.isLayak()
                        ? (i == 0 ? "Utama" : "Alternatif") : "Belum memenuhi";
            }
            rekomendasiSayaModel.addRow(new Object[]{
                peringkat,
                item.getBidang(),
                String.format(java.util.Locale.ROOT, "%.2f", item.getRataRata()),
                item.getStatusKelayakan()
            });
        }
        if (skor.isEmpty()) {
            rekomendasiMahasiswaStatus.setText("Rekomendasi belum tersedia");
            rekomendasiMahasiswaNama.setText("Belum tersedia");
            rekomendasiMahasiswaSkor.setText("Skor: —");
            rekomendasiMahasiswaAlasan.setText("<html><div style='width:520px'>"
                    + "Belum ada bidang yang dapat dianalisis.</div></html>");
        } else {
            RekomendasiService.SkorBidang top = alternatifUtama == null
                    ? skor.get(0) : alternatifUtama;
            rekomendasiMahasiswaStatus.setText(!bidangGagal.isEmpty()
                    ? "Rekomendasi Alternatif"
                    : top.isLayak() ? "Bidang Peminatan Terbaik"
                            : "Rekomendasi Teratas — lengkapi persyaratan");
            rekomendasiMahasiswaNama.setText(top.getBidang());
            rekomendasiMahasiswaSkor.setText(String.format(java.util.Locale.ROOT,
                    "Skor %.1f%%", top.getRataRata() * 25d));
            String reason = top.getAlasan().isEmpty()
                    ? "Lengkapi nilai yang belum diverifikasi untuk memperbarui rekomendasi."
                    : String.join(" ", top.getAlasan());
            rekomendasiMahasiswaAlasan.setText("<html><div style='width:520px'>"
                    + escapeHtml(reason) + "</div></html>");
        }
        StringBuilder detail = new StringBuilder();
        detail.append("Mahasiswa: ").append(mahasiswa.getNama()).append(" (")
                .append(mahasiswa.getNim()).append(")\n");
        detail.append("Minat pilihan: ").append(teksTabel(mahasiswa.getMinatAwal())).append("\n\n");
        if (alternatifUtama != null && !bidangGagal.isEmpty()) {
            detail.append("Bidang tetap ").append(bidangGagal)
                    .append(" belum memenuhi syarat. Rekomendasi alternatif berdasarkan "
                            + "proporsi mata kuliah pendukung yang lulus: ")
                    .append(alternatifUtama.getBidang()).append(" (")
                    .append(alternatifUtama.getJumlahMataKuliahLulus()).append("/")
                    .append(alternatifUtama.getJumlahMataKuliahPendukung())
                    .append(" lulus, ").append(alternatifUtama.getStatusKelayakan())
                    .append(").\n\n");
        }
        for (RekomendasiService.SkorBidang item : skor) {
            detail.append(item.getBidang()).append(": ")
                    .append(item.getJumlahMataKuliahLulus()).append("/")
                    .append(item.getJumlahMataKuliahPendukung()).append(" lulus; ")
                    .append(String.join(" ", item.getAlasan())).append("\n");
        }
        detail.append("\nNilai yang menunggu verifikasi Admin belum digunakan.");
        rekomendasiSayaDetail.setText(detail.toString());
    }

    private void refreshRiwayatMahasiswa() {
        clearModel(riwayatSayaModel);
        List<RiwayatPenjurusan> history =
                dataService.riwayatPenjurusanMahasiswa(userSession);
        riwayatMahasiswaJumlah.setText(history.size() + " Riwayat");
        RiwayatPenjurusan latest = null;
        for (RiwayatPenjurusan item : history) {
            riwayatSayaModel.addRow(new Object[]{item.getTanggal(), item.getBidang(),
                String.join("; ", item.getMataKuliahPendukung()),
                item.isLayak() ? "Memenuhi syarat" : "Belum memenuhi syarat"});
            if (latest == null || item.getTanggal().isAfter(latest.getTanggal())) latest = item;
        }
        riwayatMahasiswaStatus.setText(latest == null
                ? "Belum ada riwayat penjurusan."
                : "Penjurusan terakhir: " + latest.getBidang()
                        + "  •  " + latest.getTanggal());
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 20)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setPaint(new java.awt.GradientPaint(0, 0, new Color(12, 45, 88),
                        0, getHeight(), new Color(5, 28, 57)));
                g.fillRect(0, 0, getWidth(), getHeight());
                if (dashboardBuildingImage != null) {
                    int height = Math.min(getHeight() / 3, 250);
                    int width = (int) ((double) dashboardBuildingImage.getWidth()
                            * height / dashboardBuildingImage.getHeight());
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.30f));
                    g.drawImage(dashboardBuildingImage, (getWidth() - width) / 2,
                            getHeight() - height, width, height, null);
                    g.setComposite(AlphaComposite.SrcOver);
                }
                g.setColor(new Color(8, 39, 78, 165));
                g.fillRect(0, getHeight() * 2 / 3, getWidth(), getHeight() / 3);
                g.setColor(new Color(242, 188, 42));
                g.setStroke(new java.awt.BasicStroke(5f, java.awt.BasicStroke.CAP_ROUND,
                        java.awt.BasicStroke.JOIN_ROUND));
                g.draw(new java.awt.geom.QuadCurve2D.Double(-20, getHeight() - 55,
                        getWidth() * 0.55, getHeight() - 95, getWidth() + 20, getHeight() - 28));
                g.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(26, 14, 12, 14));

        JPanel brand = new JPanel(new BorderLayout(10, 0));
        brand.setOpaque(false);
        JLabel brandLogo = new JLabel();
        if (logoUnhasyImage != null) {
            brandLogo.setIcon(new javax.swing.ImageIcon(scaleToFit(logoUnhasyImage, 88, 88)));
        }
        JLabel brandText = new JLabel("<html><b>UNHASY</b><br><font size='3'>Sistem Informasi</font></html>");
        brandText.setForeground(Color.WHITE);
        brandText.setFont(new Font("SansSerif", Font.PLAIN, 16));
        brand.add(brandLogo, BorderLayout.WEST);
        brand.add(brandText, BorderLayout.CENTER);
        brand.setBorder(BorderFactory.createEmptyBorder(0, 8, 24, 4));
        sidebar.add(brand, BorderLayout.NORTH);

        JPanel menu = new JPanel(new GridBagLayout());
        menu.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(3, 0, 3, 0);
        String[] menus = isAdminMode()
                ? new String[]{"Dashboard", "Data Mahasiswa", "Mata Kuliah & Nilai",
                    "Bidang Peminatan", "Penjurusan Semester 7", "Rekomendasi", "Laporan"}
                : new String[]{"Dashboard", "Profil Saya", "Mata Kuliah & Nilai",
                    "Input Nilai", "Pilih Bidang Peminatan", "Rekomendasi Peminatan",
                    "Riwayat Penjurusan", "Logout"};
        for (int i = 0; i < menus.length; i++) {
            String namaMenu = menus[i];
            JButton button = new JButton(namaMenu);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setForeground(new Color(220, 230, 242));
            button.setBackground(NAVY);
            button.setFont(new Font("SansSerif", Font.PLAIN, 14));
            button.setUI(new BasicButtonUI());
            button.setFocusPainted(false);
            button.setOpaque(true);
            button.setContentAreaFilled(true);
            button.setRolloverEnabled(false);
            button.setIcon(new MenuIcon(i, new Color(174, 193, 214)));
            button.setIconTextGap(14);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, NAVY),
                    BorderFactory.createEmptyBorder(13, 12, 13, 8)));
            if ("Logout".equals(namaMenu)) {
                button.addActionListener(e -> konfirmasiLogout());
            } else {
                button.addActionListener(e -> tampilkanHalaman(namaMenu));
                navButtons.put(namaMenu, button);
            }
            c.gridy = i;
            menu.add(button, c);
        }
        c.gridy = menus.length;
        c.weighty = 1;
        menu.add(new JLabel(), c);
        sidebar.add(menu, BorderLayout.CENTER);
        JPanel footer = new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(255, 255, 255, 18));
                g.setFont(new Font("SansSerif", Font.BOLD, 34));
                g.drawString("UNHASY", 8, getHeight() - 23);
                g.setColor(GOLD);
                g.setStroke(new java.awt.BasicStroke(5f, java.awt.BasicStroke.CAP_ROUND,
                        java.awt.BasicStroke.JOIN_ROUND));
                g.drawArc(-62, 18, getWidth() + 105, 78, 195, 115);
                g.dispose();
            }
        };
        footer.setOpaque(false);
        footer.setPreferredSize(new Dimension(1, 96));
        sidebar.add(footer, BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setPaint(new java.awt.GradientPaint(0, 0, Color.WHITE,
                        getWidth(), getHeight(), new Color(230, 239, 253)));
                g.fillRect(0, 0, getWidth(), getHeight());
                java.awt.geom.Path2D band = new java.awt.geom.Path2D.Float();
                band.moveTo(getWidth() * 0.61, 0);
                band.curveTo(getWidth() * 0.75, getHeight() * 0.12,
                        getWidth() * 0.83, getHeight() * 0.84, getWidth(), getHeight() * 0.74);
                band.lineTo(getWidth(), getHeight());
                band.curveTo(getWidth() * 0.80, getHeight() * 0.94,
                        getWidth() * 0.74, getHeight() * 0.22, getWidth() * 0.61, 0);
                band.closePath();
                g.setColor(new Color(10, 48, 94, 245));
                g.fill(band);
                g.setColor(new Color(243, 190, 47));
                g.setStroke(new java.awt.BasicStroke(6f));
                g.draw(new java.awt.geom.QuadCurve2D.Double(
                        getWidth() * 0.73, 0, getWidth() * 0.86, getHeight(), getWidth(), getHeight() * 0.84));
                g.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 231, 239)),
                BorderFactory.createEmptyBorder(12, 22, 12, 18)));
        JLabel logo = new JLabel();
        if (logoUnhasyImage != null) {
            logo.setIcon(new javax.swing.ImageIcon(scaleToFit(logoUnhasyImage, 88, 88)));
        } else {
            logo.setText("UNHASY");
            logo.setForeground(BLUE);
            logo.setFont(new Font("SansSerif", Font.BOLD, 12));
        }
        JPanel title = new JPanel(new BorderLayout());
        title.setOpaque(false);
        JLabel heading = new JLabel("<html>Sistem Informasi Penjurusan Mata Kuliah dan "
                + "Rekomendasi Peminatan</html>");
        heading.setFont(new Font("SansSerif", Font.BOLD, 15));
        heading.setForeground(NAVY);
        JLabel subheading = new JLabel("Program Studi Sistem Informasi • FTI UNHASY");
        subheading.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subheading.setForeground(new Color(102, 116, 134));
        title.add(heading, BorderLayout.CENTER);
        title.add(subheading, BorderLayout.SOUTH);
        header.add(logo, BorderLayout.WEST);
        header.add(title, BorderLayout.CENTER);
        JPanel profile = new JPanel(new BorderLayout(10, 0));
        profile.setOpaque(false);
        profile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(255, 255, 255, 110)),
                BorderFactory.createEmptyBorder(0, 16, 0, 4)));
        String namaProfil = isAdminMode() ? "Admin" : userSession.getNama();
        String infoProfil = isAdminMode() ? "Administrator" : userSession.getNim();
        String profileInitial = namaProfil == null || namaProfil.trim().isEmpty()
                ? "?" : namaProfil.trim().substring(0, 1).toUpperCase(java.util.Locale.ROOT);
        JPanel avatar = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int diameter = Math.min(getWidth(), getHeight());
                int x = (getWidth() - diameter) / 2;
                int y = (getHeight() - diameter) / 2;
                g.setColor(new Color(222, 235, 250));
                g.fillOval(x, y, diameter, diameter);
                g.setColor(BLUE);
                g.setFont(new Font("SansSerif", Font.BOLD, 15));
                java.awt.FontMetrics metrics = g.getFontMetrics();
                g.drawString(profileInitial,
                        (getWidth() - metrics.stringWidth(profileInitial)) / 2,
                        (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent());
                g.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(40, 40));
        JPanel avatarContainer = new JPanel(new GridBagLayout());
        avatarContainer.setOpaque(false);
        avatarContainer.add(avatar);
        JLabel profileName = new JLabel("<html><b>" + escapeHtml(namaProfil)
                + "</b><br><font size='2'>" + escapeHtml(infoProfil) + "</font></html>");
        profileName.setForeground(Color.WHITE);
        profileName.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JLabel chevron = new JLabel("⌄");
        chevron.setForeground(Color.WHITE);
        chevron.setFont(new Font("SansSerif", Font.BOLD, 16));
        profile.add(avatarContainer, BorderLayout.WEST);
        profile.add(profileName, BorderLayout.CENTER);
        profile.add(chevron, BorderLayout.EAST);
        profile.setToolTipText("Buka menu Admin");
        MouseAdapter profileClick = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                showAdminProfileMenu(profile);
            }
        };
        for (Component component : Arrays.asList(
                profile, avatarContainer, avatar, profileName, chevron)) {
            component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            component.addMouseListener(profileClick);
        }
        header.add(profile, BorderLayout.EAST);
        return header;
    }

    private void showAdminProfileMenu(Component anchor) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(BorderFactory.createLineBorder(new Color(205, 217, 231)));

        JPanel info = new JPanel(new GridLayout(isAdminMode() ? 2 : 3, 1, 0, 3));
        info.setBackground(Color.WHITE);
        info.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel name = new JLabel(isAdminMode() ? "Admin" : escapeHtml(userSession.getNama()));
        name.setFont(new Font("SansSerif", Font.BOLD, 13));
        name.setForeground(NAVY);
        JLabel role = new JLabel(isAdminMode() ? "Administrator" : "Mahasiswa");
        role.setFont(new Font("SansSerif", Font.PLAIN, 12));
        role.setForeground(new Color(102, 116, 134));
        info.add(name);
        if (!isAdminMode()) {
            JLabel nim = new JLabel("NIM: " + escapeHtml(userSession.getNim()));
            nim.setFont(new Font("SansSerif", Font.PLAIN, 12));
            nim.setForeground(new Color(102, 116, 134));
            info.add(nim);
        }
        info.add(role);
        menu.add(info);
        menu.addSeparator();

        JMenuItem logout = new JMenuItem("Logout");
        logout.setFont(new Font("SansSerif", Font.PLAIN, 13));
        logout.addActionListener(event -> konfirmasiLogout());
        menu.add(logout);
        menu.setPreferredSize(new Dimension(220, menu.getPreferredSize().height));

        Point anchorOnScreen = anchor.getLocationOnScreen();
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        Dimension menuSize = menu.getPreferredSize();
        int x = anchorOnScreen.x + anchor.getWidth() - menuSize.width;
        x = Math.max(screen.x, Math.min(x, screen.x + screen.width - menuSize.width));
        int y = anchorOnScreen.y + anchor.getHeight();
        if (y + menuSize.height > screen.y + screen.height) {
            y = anchorOnScreen.y - menuSize.height;
        }
        y = Math.max(screen.y, Math.min(y, screen.y + screen.height - menuSize.height));
        menu.show(anchor, x - anchorOnScreen.x, y - anchorOnScreen.y);
    }

    private void konfirmasiLogout() {
        int pilihan = JOptionPane.showOptionDialog(this,
                "Apakah Anda yakin ingin keluar?",
                "Konfirmasi Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new Object[]{"Ya", "Batal"},
                "Batal");
        if (pilihan != 0) return;

        userSession = null;
        dispose();
        logoutHandler.run();
    }

    private JPanel createDashboardPage() {
        JPanel page = new JPanel(new BorderLayout(0, 18)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                g.setColor(BACKGROUND);
                g.fillRect(0, 0, getWidth(), getHeight());
                if (dashboardBuildingImage != null) {
                    double scale = Math.min((double) (getWidth() * 0.72) / dashboardBuildingImage.getWidth(),
                            (double) (getHeight() * 0.48) / dashboardBuildingImage.getHeight());
                    int width = (int) (dashboardBuildingImage.getWidth() * scale);
                    int height = (int) (dashboardBuildingImage.getHeight() * scale);
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.075f));
                    g.drawImage(dashboardBuildingImage, getWidth() - width - 18,
                            getHeight() - height - 12, width, height, null);
                }
                g.dispose();
            }
        };
        page.setOpaque(false);
        page.setBorder(BorderFactory.createEmptyBorder(22, 24, 24, 24));
        JPanel heading = new JPanel(new BorderLayout(10, 0));
        heading.setOpaque(false);
        JPanel titleAccent = new JPanel();
        titleAccent.setBackground(GOLD);
        titleAccent.setPreferredSize(new Dimension(4, 38));
        JLabel title = new JLabel("Dashboard Akademik");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(NAVY);
        JLabel subtitle = new JLabel("Ringkasan data akademik dan perkembangan peminatan mahasiswa.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(TEXT);
        JPanel headingText = new JPanel(new BorderLayout(0, 3));
        headingText.setOpaque(false);
        headingText.add(title, BorderLayout.NORTH);
        headingText.add(subtitle, BorderLayout.CENTER);
        heading.add(titleAccent, BorderLayout.WEST);
        heading.add(headingText, BorderLayout.CENTER);
        page.add(heading, BorderLayout.NORTH);

        JPanel metrics = new JPanel(new java.awt.GridLayout(1, 4, 12, 12));
        metrics.setOpaque(false);
        String[] labels = {"Total Mahasiswa", "Total Mata Kuliah",
            "Memenuhi Syarat Penjurusan", "Belum Memenuhi Syarat"};
        for (int i = 0; i < labels.length; i++) {
            metrics.add(metricCard(labels[i], i));
        }

        JPanel summaryCards = new JPanel(new java.awt.GridLayout(1, 2, 16, 14));
        summaryCards.setOpaque(false);
        summaryCards.add(chartCard("Mahasiswa berdasarkan semester", grafikSemester));
        JPanel kelulusanCard = chartCard("Status kelulusan mata kuliah", grafikKelulusan);
        JButton detailButton = button("Lihat Detail", NAVY);
        detailButton.setIcon(new MenuIcon(7, Color.WHITE));
        detailButton.setIconTextGap(9);
        detailButton.addActionListener(e -> tampilkanHalaman("Mata Kuliah & Nilai"));
        kelulusanCard.add(detailButton, BorderLayout.SOUTH);
        summaryCards.add(kelulusanCard);
        summaryCards.setPreferredSize(new Dimension(760, 292));

        JPanel dashboardContent = new JPanel(new BorderLayout(0, 18));
        dashboardContent.setOpaque(false);
        dashboardContent.add(createWelcomeBanner(), BorderLayout.NORTH);
        dashboardContent.add(metrics, BorderLayout.CENTER);
        JPanel stack = new JPanel(new BorderLayout(0, 18));
        stack.setOpaque(false);
        stack.add(dashboardContent, BorderLayout.NORTH);
        stack.add(summaryCards, BorderLayout.CENTER);
        JScrollPane dashboardScroll = new JScrollPane(stack);
        dashboardScroll.setBorder(null);
        dashboardScroll.setOpaque(false);
        dashboardScroll.getViewport().setOpaque(false);
        dashboardScroll.getVerticalScrollBar().setUnitIncrement(18);
        page.add(dashboardScroll, BorderLayout.CENTER);
        return page;
    }

    private JPanel createWelcomeBanner() {
        JPanel banner = roundedCard(NAVY);
        banner.setLayout(new BorderLayout(20, 0));
        banner.setPreferredSize(new Dimension(680, 180));
        JPanel image = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                if (dashboardBuildingImage == null) return;
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                double scale = Math.max((double) getWidth() / dashboardBuildingImage.getWidth(),
                        (double) getHeight() / dashboardBuildingImage.getHeight());
                int width = (int) Math.ceil(dashboardBuildingImage.getWidth() * scale);
                int height = (int) Math.ceil(dashboardBuildingImage.getHeight() * scale);
                g.drawImage(dashboardBuildingImage, (getWidth() - width) / 2,
                        (getHeight() - height) / 2, width, height, null);
                g.setPaint(new java.awt.GradientPaint(Math.max(0, getWidth() - 115), 0,
                        new Color(NAVY.getRed(), NAVY.getGreen(), NAVY.getBlue(), 0),
                        getWidth(), 0, NAVY));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.dispose();
            }
        };
        image.setOpaque(true);
        image.setBackground(NAVY);
        image.setPreferredSize(new Dimension(285, 142));
        JPanel text = new JPanel(new BorderLayout(0, 10));
        text.setOpaque(false);
        JLabel welcomeTitle = new JLabel("<html>Selamat Datang di <font color='#D4AF37'>"
                + "Sistem Informasi Akademik</font></html>");
        welcomeTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        welcomeTitle.setForeground(Color.WHITE);
        JLabel welcomeText = new JLabel("<html>Sistem Informasi Penjurusan Mata Kuliah dan Rekomendasi Peminatan<br>"
                + "Program Studi Sistem Informasi, FTI UNHASY</html>");
        welcomeText.setFont(new Font("SansSerif", Font.PLAIN, 14));
        welcomeText.setForeground(new Color(226, 235, 246));
        JPanel textPanel = new JPanel(new BorderLayout(0, 10));
        textPanel.setOpaque(false);
        textPanel.add(welcomeTitle, BorderLayout.NORTH);
        textPanel.add(welcomeText, BorderLayout.CENTER);
        JPanel accent = roundedCard(GOLD);
        accent.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        JLabel accentText = new JLabel("UNIVERSITAS HASYIM ASY'ARI");
        accentText.setForeground(NAVY);
        accentText.setFont(new Font("SansSerif", Font.BOLD, 11));
        accent.add(accentText, BorderLayout.CENTER);
        text.add(textPanel, BorderLayout.CENTER);
        text.add(accent, BorderLayout.SOUTH);
        banner.add(image, BorderLayout.WEST);
        banner.add(text, BorderLayout.CENTER);
        return banner;
    }

    private JPanel metricCard(String labelText, int index) {
        Color[] metricColors = {BLUE, GOLD, TEAL, new Color(120, 91, 209)};
        JPanel card = card();
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, metricColors[index]),
                BorderFactory.createEmptyBorder(13, 15, 11, 15)));
        JLabel icon = new JLabel(new MetricIcon(index));
        card.add(icon, BorderLayout.WEST);
        JPanel details = new JPanel(new BorderLayout(0, 3));
        details.setOpaque(false);
        String displayLabel = labelText;
        if (index == 2) displayLabel = "Memenuhi Syarat<br>Penjurusan";
        else if (index == 3) displayLabel = "Belum Memenuhi<br>Syarat";
        JLabel label = new JLabel("<html>" + displayLabel + "</html>");
        label.setForeground(new Color(65, 81, 103));
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        ringkasan[index] = new JLabel("0");
        ringkasan[index].setForeground(NAVY);
        ringkasan[index].setFont(new Font("SansSerif", Font.BOLD, 32));
        details.add(label, BorderLayout.NORTH);
        details.add(ringkasan[index], BorderLayout.CENTER);
        card.add(details, BorderLayout.CENTER);
        return card;
    }

    private JPanel createDashboardNote() {
        JPanel note = roundedCard(LIGHT_BLUE);
        note.setLayout(new BorderLayout(0, 12));
        JLabel title = sectionTitle("Ringkasan peminatan");
        JLabel message = new JLabel("<html>Data bidang dan kelulusan diperbarui dari catatan akademik yang tersimpan."
                + "<br>Lengkapi nilai dan riwayat penjurusan untuk melihat perkembangan terbaru.</html>");
        message.setForeground(TEXT);
        message.setFont(new Font("SansSerif", Font.PLAIN, 13));
        note.add(title, BorderLayout.NORTH);
        note.add(message, BorderLayout.CENTER);
        return note;
    }

    private JPanel createMahasiswaPage() {
        JPanel page = createContentBackgroundPage(0, 14);
        page.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JPanel heading = new JPanel(new BorderLayout(14, 0));
        heading.setOpaque(false);
        heading.setPreferredSize(new Dimension(700, 96));
        JPanel titleIcon = createGradientTitleIcon(PenjurusanIcon.USERS);
        JPanel titleText = new JPanel(new BorderLayout(8, 0));
        titleText.setOpaque(false);
        JPanel titleAccent = new JPanel();
        titleAccent.setBackground(GOLD);
        titleAccent.setPreferredSize(new Dimension(4, 42));
        JPanel titleLabels = new JPanel(new BorderLayout(0, 3));
        titleLabels.setOpaque(false);
        JLabel title = new JLabel("Data Mahasiswa");
        title.setFont(new Font("SansSerif", Font.BOLD, 27));
        title.setForeground(NAVY);
        JLabel subtitle = new JLabel("Kelola data mahasiswa dengan mudah dan terintegrasi");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(TEXT);
        titleLabels.add(title, BorderLayout.NORTH);
        titleLabels.add(subtitle, BorderLayout.CENTER);
        titleText.add(titleAccent, BorderLayout.WEST);
        titleText.add(titleLabels, BorderLayout.CENTER);
        heading.add(titleIcon, BorderLayout.WEST);
        heading.add(titleText, BorderLayout.CENTER);
        page.add(heading, BorderLayout.NORTH);

        JPanel toolbar = card();
        toolbar.setLayout(new BorderLayout(14, 0));
        toolbar.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        JPanel search = new JPanel(new BorderLayout(9, 0));
        search.setOpaque(false);
        JPanel searchInput = new JPanel(new BorderLayout(8, 0));
        searchInput.setBackground(Color.WHITE);
        searchInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 222, 237)),
                BorderFactory.createEmptyBorder(0, 10, 0, 8)));
        searchInput.add(new JLabel(new MenuIcon(7, NAVY)), BorderLayout.WEST);
        cariMahasiswaField.setColumns(20);
        cariMahasiswaField.setBorder(BorderFactory.createEmptyBorder(8, 2, 8, 2));
        cariMahasiswaField.setToolTipText("Cari berdasarkan nama atau NIM");
        searchInput.add(cariMahasiswaField, BorderLayout.CENTER);
        JButton cariButton = button("Cari", NAVY);
        cariButton.addActionListener(e -> {
            currentMahasiswaPage = 0;
            refreshTabelMahasiswa();
        });
        search.add(searchInput, BorderLayout.CENTER);
        search.add(cariButton, BorderLayout.EAST);
        search.setPreferredSize(new Dimension(330, 42));

        JPanel filters = new JPanel(new GridLayout(1, 2, 12, 0));
        filters.setOpaque(false);
        filters.add(mahasiswaFilterField("Pilih Program Studi", mahasiswaFilterProdi));
        filters.add(mahasiswaFilterField("Pilih Semester", mahasiswaFilterSemester));
        JButton tambahButton = button("+  Tambah Data", NAVY);
        tambahButton.setPreferredSize(new Dimension(158, 42));
        tambahButton.addActionListener(e -> {
            resetFormMahasiswa();
            bukaFormMahasiswa(false);
        });
        toolbar.add(search, BorderLayout.WEST);
        toolbar.add(filters, BorderLayout.CENTER);
        toolbar.add(tambahButton, BorderLayout.EAST);

        JPanel tablePanel = card();
        tablePanel.setLayout(new BorderLayout(0, 10));
        tablePanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 10, 12));
        mahasiswaTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        mahasiswaTable.setRowHeight(44);
        mahasiswaTable.setSelectionBackground(new Color(231, 240, 252));
        mahasiswaTable.getTableHeader().setBackground(NAVY);
        mahasiswaTable.getTableHeader().setForeground(Color.WHITE);
        mahasiswaTable.getTableHeader().setPreferredSize(new Dimension(0, 44));
        mahasiswaTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value,
                    boolean selected, boolean focused, int row, int column) {
                super.getTableCellRendererComponent(source, value, selected, focused, row, column);
                setFont(source.getTableHeader().getFont());
                setForeground(Color.WHITE);
                setBackground(NAVY);
                setHorizontalAlignment(column == 0 || column == 4 || column == 5
                        ? SwingConstants.CENTER : SwingConstants.LEFT);
                setOpaque(true);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(56, 83, 117)),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)));
                return this;
            }
        });
        setColumnWidths(mahasiswaTable, 48, 112, 220, 190, 82, 150);
        mahasiswaTable.getColumnModel().getColumn(5).setCellRenderer(new MahasiswaActionRenderer());
        mahasiswaTable.getColumnModel().getColumn(5).setCellEditor(new MahasiswaActionEditor());
        JScrollPane mahasiswaScroll = tableScrollPane(mahasiswaTable);
        mahasiswaScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tablePanel.add(mahasiswaScroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setOpaque(false);
        mahasiswaCountLabel.setForeground(TEXT);
        mahasiswaCountLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        footer.add(mahasiswaCountLabel, BorderLayout.WEST);
        JPanel pagination = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pagination.setOpaque(false);
        mahasiswaFirstPageButton = pageButton("<<", "Halaman pertama");
        mahasiswaPreviousPageButton = pageButton("<", "Halaman sebelumnya");
        mahasiswaPageLabel.setOpaque(true);
        mahasiswaPageLabel.setBackground(new Color(231, 240, 252));
        mahasiswaPageLabel.setForeground(NAVY);
        mahasiswaPageLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        mahasiswaPageLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 222, 237)),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)));
        mahasiswaNextPageButton = pageButton(">", "Halaman berikutnya");
        mahasiswaLastPageButton = pageButton(">>", "Halaman terakhir");
        mahasiswaFirstPageButton.addActionListener(e -> ubahHalamanMahasiswa(0));
        mahasiswaPreviousPageButton.addActionListener(e -> ubahHalamanMahasiswa(currentMahasiswaPage - 1));
        mahasiswaNextPageButton.addActionListener(e -> ubahHalamanMahasiswa(currentMahasiswaPage + 1));
        mahasiswaLastPageButton.addActionListener(e ->
                ubahHalamanMahasiswa(Math.max(0, (jumlahMahasiswaTersaring() - 1) / MAHASISWA_PAGE_SIZE)));
        pagination.add(mahasiswaFirstPageButton);
        pagination.add(mahasiswaPreviousPageButton);
        pagination.add(mahasiswaPageLabel);
        pagination.add(mahasiswaNextPageButton);
        pagination.add(mahasiswaLastPageButton);
        footer.add(pagination, BorderLayout.EAST);
        tablePanel.add(footer, BorderLayout.SOUTH);

        mahasiswaFormPanel = createMahasiswaFormPanel();
        terapkanTemaKomponen(mahasiswaFormPanel);
        clearFilterMahasiswaOnChange();
        mahasiswaTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && mahasiswaTable.getSelectedRow() >= 0) isiFormMahasiswa();
        });
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setOpaque(false);
        content.add(toolbar, BorderLayout.NORTH);
        content.add(tablePanel, BorderLayout.CENTER);
        page.add(content, BorderLayout.CENTER);
        JPanel footerBand = new JPanel(new BorderLayout(18, 0));
        footerBand.setBackground(NAVY);
        footerBand.setBorder(BorderFactory.createEmptyBorder(7, 24, 7, 24));
        JLabel footerText = new JLabel("UNHASY   •   Tebuireng Jombang", SwingConstants.CENTER);
        footerText.setForeground(Color.WHITE);
        footerText.setFont(new Font("SansSerif", Font.PLAIN, 11));
        JPanel leftLine = new JPanel();
        leftLine.setBackground(GOLD);
        leftLine.setPreferredSize(new Dimension(95, 1));
        JPanel rightLine = new JPanel();
        rightLine.setBackground(GOLD);
        rightLine.setPreferredSize(new Dimension(95, 1));
        footerBand.add(leftLine, BorderLayout.WEST);
        footerBand.add(footerText, BorderLayout.CENTER);
        footerBand.add(rightLine, BorderLayout.EAST);
        footerBand.setPreferredSize(new Dimension(1, 34));
        page.add(footerBand, BorderLayout.SOUTH);
        return page;
    }

    private JPanel mahasiswaFilterField(String title, JComboBox<String> combo) {
        JPanel field = new JPanel(new BorderLayout(0, 4));
        field.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setForeground(new Color(102, 116, 134));
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        combo.setPreferredSize(new Dimension(170, 34));
        field.add(label, BorderLayout.NORTH);
        field.add(combo, BorderLayout.CENTER);
        return field;
    }

    private JButton pageButton(String text, String tooltip) {
        JButton button = button(text, new Color(231, 240, 252));
        button.setForeground(NAVY);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 222, 237)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        button.setToolTipText(tooltip);
        return button;
    }

    private JPanel createMahasiswaFormPanel() {
        JPanel form = card();
        form.setPreferredSize(new Dimension(650, 590));
        form.setLayout(new BorderLayout(0, 16));
        JPanel formHeader = new JPanel(new BorderLayout(0, 8));
        formHeader.setOpaque(false);
        mahasiswaFormTitleLabel = new JLabel("Formulir Mahasiswa", new MetricIcon(0), SwingConstants.LEFT);
        mahasiswaFormTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        mahasiswaFormTitleLabel.setForeground(NAVY);
        formHeader.add(mahasiswaFormTitleLabel, BorderLayout.NORTH);
        mahasiswaFormStatusLabel.setForeground(new Color(102, 116, 134));
        mahasiswaFormStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        formHeader.add(mahasiswaFormStatusLabel, BorderLayout.CENTER);
        form.add(formHeader, BorderLayout.NORTH);
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        fields.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        komponenFormMahasiswa.addAll(Arrays.asList(nimField, namaField, genderField, prodiField,
                semesterField, angkatanField, minatField));
        addMahasiswaField(fields, 0, "NIM *", nimField);
        addMahasiswaField(fields, 1, "Nama lengkap *", namaField);
        addMahasiswaField(fields, 2, "Jenis kelamin", genderField);
        addMahasiswaField(fields, 3, "Program studi *", prodiField);
        addMahasiswaField(fields, 4, "Semester *", semesterField);
        addMahasiswaField(fields, 5, "Tahun angkatan *", angkatanField);
        addMahasiswaField(fields, 6, "Minat peminatan", minatField);
        JScrollPane formScroll = new JScrollPane(fields,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.setBorder(null);
        formScroll.getViewport().setBackground(Color.WHITE);
        form.add(formScroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new java.awt.GridLayout(0, 3, 8, 8));
        actions.setOpaque(false);
        simpanMahasiswaButton = button("Simpan", BLUE);
        ubahMahasiswaButton = button("Ubah", TEAL);
        hapusMahasiswaButton = button("Hapus", new Color(169, 71, 71));
        actions.add(simpanMahasiswaButton);
        actions.add(ubahMahasiswaButton);
        actions.add(hapusMahasiswaButton);
        resetMahasiswaButton = button("Reset", new Color(94, 111, 132));
        JButton close = button("Tutup", new Color(94, 111, 132));
        actions.add(resetMahasiswaButton);
        actions.add(close);
        form.add(actions, BorderLayout.SOUTH);
        simpanMahasiswaButton.addActionListener(e -> {
            String nimBefore = nimField.getText().trim();
            simpanMahasiswa();
            tutupDialogSetelahBerhasil(nimBefore);
        });
        ubahMahasiswaButton.addActionListener(e -> {
            String nimBefore = nimField.getText().trim();
            ubahMahasiswa();
            tutupDialogSetelahBerhasil(nimBefore);
        });
        hapusMahasiswaButton.addActionListener(e -> {
            String nimBefore = nimField.getText().trim();
            hapusMahasiswa();
            tutupDialogSetelahBerhasil(nimBefore);
        });
        resetMahasiswaButton.addActionListener(e -> resetFormMahasiswa());
        close.addActionListener(e -> {
            if (mahasiswaFormDialog != null) mahasiswaFormDialog.dispose();
        });
        return form;
    }

    private void bukaFormMahasiswa(boolean readOnly) {
        if (mahasiswaFormDialog != null) return;
        if (readOnly) {
            aturFormMahasiswa(false, false, false);
            mahasiswaFormStatusLabel.setText("Data mahasiswa — hanya untuk dilihat.");
        }
        mahasiswaFormTitleLabel.setText(readOnly ? "Detail Mahasiswa" : "Formulir Mahasiswa");
        mahasiswaFormDialog = new JDialog(this,
                readOnly ? "Detail Mahasiswa" : "Formulir Mahasiswa", true);
        mahasiswaFormDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        mahasiswaFormDialog.setContentPane(mahasiswaFormPanel);
        mahasiswaFormDialog.pack();
        mahasiswaFormDialog.setMinimumSize(new Dimension(620, 560));
        mahasiswaFormDialog.setLocationRelativeTo(this);
        mahasiswaFormDialog.setVisible(true);
        mahasiswaFormDialog = null;
    }

    private void tutupDialogSetelahBerhasil(String nimSebelumnya) {
        if (mahasiswaFormDialog != null && nimSebelumnya != null
                && !nimSebelumnya.isEmpty() && nimField.getText().trim().isEmpty()) {
            mahasiswaFormDialog.dispose();
        }
    }

    private void clearFilterMahasiswaOnChange() {
        cariMahasiswaField.getDocument().addDocumentListener(documentListener(() -> {
            currentMahasiswaPage = 0;
            refreshTabelMahasiswa();
        }));
        mahasiswaFilterSemester.addActionListener(e -> {
            currentMahasiswaPage = 0;
            refreshTabelMahasiswa();
        });
        mahasiswaFilterProdi.addActionListener(e -> {
            currentMahasiswaPage = 0;
            refreshTabelMahasiswa();
        });
    }

    private void ubahHalamanMahasiswa(int page) {
        currentMahasiswaPage = Math.max(0, page);
        refreshTabelMahasiswa();
    }

    private void addMahasiswaField(JPanel fields, int row, String labelText, JComponent input) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.weightx = 0.40;
        labelConstraints.fill = GridBagConstraints.HORIZONTAL;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(7, 2, 7, 12);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        label.setForeground(TEXT);
        fields.add(label, labelConstraints);

        GridBagConstraints inputConstraints = new GridBagConstraints();
        inputConstraints.gridx = 1;
        inputConstraints.gridy = row;
        inputConstraints.weightx = 0.60;
        inputConstraints.fill = GridBagConstraints.HORIZONTAL;
        inputConstraints.insets = new Insets(5, 0, 5, 2);
        fields.add(input, inputConstraints);
    }

    private void setColumnWidths(JTable table, int... widths) {
        for (int column = 0; column < widths.length; column++) {
            table.getColumnModel().getColumn(column).setPreferredWidth(widths[column]);
            table.getColumnModel().getColumn(column).setMinWidth(Math.min(58, widths[column]));
        }
    }

    private JPanel createLatarPage() {
        JPanel page = createContentBackgroundPage(0, 12);
        page.setBorder(BorderFactory.createEmptyBorder(12, 16, 14, 16));

        JPanel hero = roundedCard(new Color(239, 246, 255));
        hero.setLayout(new BorderLayout(12, 0));
        hero.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        JPanel heroIcon = createGradientTitleIcon(PenjurusanIcon.DOCUMENT);
        JLabel heroTitle = new JLabel("Mata Kuliah & Nilai");
        heroTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        heroTitle.setForeground(NAVY);
        JLabel heroSubtitle = new JLabel("Kelola data mata kuliah dan nilai mahasiswa");
        heroSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        heroSubtitle.setForeground(TEXT);
        JPanel heroText = new JPanel(new BorderLayout(0, 3));
        heroText.setOpaque(false);
        heroText.add(heroTitle, BorderLayout.NORTH);
        heroText.add(heroSubtitle, BorderLayout.CENTER);
        hero.add(heroIcon, BorderLayout.WEST);
        hero.add(heroText, BorderLayout.CENTER);
        hero.setPreferredSize(new Dimension(700, 92));
        page.add(hero, BorderLayout.NORTH);

        JPanel form = card();
        form.setLayout(new BorderLayout(0, 10));
        form.add(sectionTitle("Input nilai mahasiswa"), BorderLayout.NORTH);
        kodeNilaiField.setEditable(false);
        namaMataKuliahField.setEditable(false);
        semesterMataKuliahField.setEnabled(false);
        sksMataKuliahField.setEditable(false);
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        int row = 0;
        row = addField(fields, row, "Mata Kuliah", kurikulumField);
        row = addField(fields, row, "NIM Mahasiswa", kodeNilaiNimField);
        row = addField(fields, row, "Kode Mata Kuliah", kodeNilaiField);
        row = addField(fields, row, "Nama Mata Kuliah", namaMataKuliahField);
        row = addField(fields, row, "Semester", semesterMataKuliahField);
        row = addField(fields, row, "SKS", sksMataKuliahField);
        row = addField(fields, row, "Nilai Huruf", nilaiHurufField);
        row = addField(fields, row, "Status Kelulusan", nilaiStatusLabel);
        form.add(fields, BorderLayout.CENTER);
        JPanel actions = new JPanel(new GridBagLayout());
        actions.setOpaque(false);
        nilaiTambahButton = button("Tambah", BLUE);
        nilaiUbahButton = button("Ubah", new Color(40, 133, 112));
        nilaiHapusButton = button("Hapus", new Color(190, 65, 65));
        nilaiVerifikasiButton = button("Verifikasi", BLUE);
        JButton reset = button("Reset", new Color(106, 119, 136));
        addAction(actions, nilaiTambahButton, 0); addAction(actions, nilaiUbahButton, 1);
        addAction(actions, nilaiHapusButton, 2); addAction(actions, nilaiVerifikasiButton, 3);
        addAction(actions, reset, 4);
        form.add(actions, BorderLayout.SOUTH);
        nilaiTambahButton.addActionListener(e -> tambahNilai());
        nilaiUbahButton.addActionListener(e -> ubahNilai());
        nilaiHapusButton.addActionListener(e -> hapusNilai());
        nilaiVerifikasiButton.addActionListener(e -> verifikasiNilaiTerpilih());
        reset.addActionListener(e -> resetFormNilai());
        nilaiHurufField.addActionListener(e -> perbaruiStatusNilai());
        kurikulumField.addActionListener(e -> isiMataKuliahPilihan());
        kodeNilaiNimField.addActionListener(e -> {
            if (nilaiNimSedangDiedit != null
                    && !nilaiNimSedangDiedit.equals(kodeNilaiNimField.getSelectedItem())) {
                nilaiNimSedangDiedit = null;
                kodeNilaiSedangDiedit = null;
                semesterNilaiSedangDiedit = -1;
            }
            if (nilaiNimSedangDiedit == null) {
                boolean simulasi = dataService.isMahasiswaSimulasi((String) kodeNilaiNimField.getSelectedItem());
                nilaiTambahButton.setEnabled(!simulasi);
            }
            refreshNilaiTabel();
            isiMataKuliahPilihan();
            perbaruiStatusNilai();
        });
        nilaiMataKuliahTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && nilaiMataKuliahTable.getSelectedRow() >= 0) isiFormNilai();
        });

        nilaiFormDialog = new JDialog(this, "Input Nilai Mahasiswa", true);
        nilaiFormDialog.setContentPane(form);
        nilaiFormDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        nilaiFormDialog.pack();
        nilaiFormDialog.setMinimumSize(new Dimension(460, 430));
        nilaiFormDialog.setLocationRelativeTo(this);

        JPanel filters = new JPanel(new GridBagLayout());
        filters.setOpaque(false);
        GridBagConstraints filterConstraints = new GridBagConstraints();
        filterConstraints.gridy = 0;
        filterConstraints.fill = GridBagConstraints.HORIZONTAL;
        filterConstraints.insets = new Insets(0, 0, 0, 10);
        JPanel searchField = labeledInput("Cari NIM atau Nama Mahasiswa", cariNilaiField);
        filterConstraints.gridx = 0;
        filterConstraints.weightx = 1;
        filters.add(searchField, filterConstraints);
        JPanel semesterFilter = labeledInput("Semester", filterNilaiSemesterField);
        filterConstraints.gridx = 1;
        filterConstraints.weightx = 0;
        filterConstraints.ipadx = 45;
        filters.add(semesterFilter, filterConstraints);
        JButton inputNilaiButton = button("+  Input Nilai", NAVY);
        inputNilaiButton.setPreferredSize(new Dimension(165, 42));
        inputNilaiButton.addActionListener(e -> {
            resetFormNilai();
            nilaiFormDialog.setLocationRelativeTo(this);
            nilaiFormDialog.setVisible(true);
        });
        filterConstraints.gridx = 2;
        filterConstraints.ipadx = 0;
        filterConstraints.insets = new Insets(18, 0, 0, 0);
        filters.add(inputNilaiButton, filterConstraints);
        JButton kelolaKurikulumButton = button("Kelola Kurikulum", TEAL);
        kelolaKurikulumButton.setPreferredSize(new Dimension(165, 42));
        kelolaKurikulumButton.addActionListener(e -> kelolaKurikulum());
        filterConstraints.gridx = 3;
        filters.add(kelolaKurikulumButton, filterConstraints);

        JPanel tablePanel = card();
        tablePanel.setLayout(new BorderLayout(0, 8));
        tablePanel.add(sectionTitle("Daftar Nilai Mahasiswa"), BorderLayout.NORTH);
        JScrollPane gradeScrollPane = tableScrollPane(nilaiMataKuliahTable);
        tablePanel.add(gradeScrollPane, BorderLayout.CENTER);
        nilaiCountLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        nilaiCountLabel.setForeground(new Color(86, 101, 121));
        tablePanel.add(nilaiCountLabel, BorderLayout.SOUTH);
        nilaiMataKuliahTable.setRowHeight(40);
        nilaiMataKuliahTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        setColumnWidths(nilaiMataKuliahTable, 55, 135, 210, 90, 290, 85, 112, 0, 130);
        nilaiMataKuliahTable.getTableHeader().setBackground(NAVY);
        nilaiMataKuliahTable.getTableHeader().setForeground(Color.WHITE);
        nilaiMataKuliahTable.getTableHeader().setPreferredSize(new Dimension(0, 42));
        nilaiMataKuliahTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value,
                    boolean selected, boolean focused, int row, int column) {
                super.getTableCellRendererComponent(source, value, selected, focused, row, column);
                setFont(source.getTableHeader().getFont());
                setForeground(Color.WHITE);
                setBackground(NAVY);
                setHorizontalAlignment(SwingConstants.CENTER);
                setOpaque(true);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(65, 92, 130)),
                        BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                return this;
            }
        });
        DefaultTableCellRenderer centeredCell = new DefaultTableCellRenderer();
        centeredCell.setHorizontalAlignment(SwingConstants.CENTER);
        nilaiMataKuliahTable.getColumnModel().getColumn(0).setCellRenderer(centeredCell);
        nilaiMataKuliahTable.getColumnModel().getColumn(3).setCellRenderer(centeredCell);
        nilaiMataKuliahTable.getColumnModel().getColumn(5).setCellRenderer(centeredCell);
        nilaiMataKuliahTable.getColumnModel().getColumn(8).setCellRenderer(centeredCell);
        nilaiMataKuliahTable.getColumnModel().getColumn(6).setCellRenderer(new NilaiActionRenderer());
        nilaiMataKuliahTable.getColumnModel().getColumn(6).setCellEditor(new NilaiActionEditor());
        nilaiMataKuliahTable.getColumnModel().getColumn(6).setPreferredWidth(112);
        nilaiMataKuliahTable.getColumnModel().getColumn(7).setMinWidth(0);
        nilaiMataKuliahTable.getColumnModel().getColumn(7).setMaxWidth(0);
        nilaiMataKuliahTable.getColumnModel().getColumn(7).setPreferredWidth(0);

        JPanel metrics = new JPanel(new GridLayout(1, 4, 10, 0));
        metrics.setOpaque(false);
        metrics.add(nilaiMetricCard("Total Mahasiswa", nilaiTotalMahasiswaValue, BLUE, "●"));
        metrics.add(nilaiMetricCard("Total Mata Kuliah", nilaiTotalMataKuliahValue, GOLD, "▣"));
        metrics.add(nilaiMetricCard("Nilai Terinput", nilaiTerinputValue, TEAL, "✓"));
        metrics.add(nilaiMetricCard("Rata-rata IPK", nilaiRataRataValue, new Color(112, 90, 190), "▥"));

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);
        body.add(filters, BorderLayout.NORTH);
        body.add(tablePanel, BorderLayout.CENTER);
        body.add(metrics, BorderLayout.SOUTH);
        page.add(body, BorderLayout.CENTER);

        cariNilaiField.getDocument().addDocumentListener(documentListener(this::refreshNilaiTabel));
        filterNilaiSemesterField.addActionListener(e -> refreshNilaiTabel());
        refreshLatarBelakangTabel();
        resetFormNilai();
        return page;
    }

    private JPanel nilaiMetricCard(String title, JLabel value, Color accent, String iconText) {
        JPanel panel = roundedCard(Color.WHITE);
        panel.setLayout(new BorderLayout(10, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(accent.getRed(), accent.getGreen(),
                        accent.getBlue(), 90)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel icon = new JLabel(iconText, SwingConstants.CENTER);
        icon.setForeground(Color.WHITE);
        icon.setFont(new Font("SansSerif", Font.BOLD, 17));
        icon.setOpaque(true);
        icon.setBackground(accent);
        icon.setPreferredSize(new Dimension(40, 40));
        JPanel labels = new JPanel(new BorderLayout(0, 2));
        labels.setOpaque(false);
        JLabel name = new JLabel(title);
        name.setFont(new Font("SansSerif", Font.PLAIN, 12));
        name.setForeground(new Color(86, 101, 121));
        value.setFont(new Font("SansSerif", Font.BOLD, 22));
        value.setForeground(NAVY);
        labels.add(name, BorderLayout.NORTH);
        labels.add(value, BorderLayout.CENTER);
        panel.add(icon, BorderLayout.WEST);
        panel.add(labels, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(180, 78));
        return panel;
    }

    private void kelolaKurikulum() {
        if (userSession == null || userSession.getRole() != UserSession.Role.ADMIN) {
            showWarning("Hanya Admin yang dapat mengelola kurikulum.");
            return;
        }
        DefaultTableModel model = tableModel("Kode", "Nama Mata Kuliah", "Semester", "SKS");
        JTable table = createTable(model);
        setColumnWidths(table, 100, 300, 75, 65);
        JDialog dialog = new JDialog(this, "Kelola Kurikulum", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.add(new JLabel("Daftar mata kuliah kurikulum", SwingConstants.LEFT),
                BorderLayout.NORTH);
        dialog.add(tableScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton tambah = button("Tambah", BLUE);
        JButton ubah = button("Ubah", TEAL);
        JButton hapus = button("Hapus", new Color(190, 65, 65));
        JButton tutup = button("Tutup", new Color(106, 119, 136));
        actions.add(tambah);
        actions.add(ubah);
        actions.add(hapus);
        actions.add(tutup);
        dialog.add(actions, BorderLayout.SOUTH);

        Runnable refresh = () -> {
            clearModel(model);
            for (MataKuliah course : dataService.semuaMataKuliah()) {
                model.addRow(new Object[]{course.getKode(), course.getNama(),
                    course.getSemester(), course.getSks()});
            }
        };
        refresh.run();
        tambah.addActionListener(e -> {
            MataKuliah input = dialogMataKuliah(null);
            if (input == null) return;
            try {
                dataService.tambahMataKuliah(userSession, input);
                perbaruiKurikulumSetelahPerubahan();
                refresh.run();
            } catch (IllegalArgumentException ex) {
                showWarning(ex.getMessage());
            } catch (IOException ex) {
                showError("Mata kuliah tidak dapat ditambahkan: " + ex.getMessage());
            }
        });
        ubah.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                showWarning("Pilih mata kuliah yang akan diubah.");
                return;
            }
            String code = String.valueOf(model.getValueAt(row, 0));
            MataKuliah existing = dataService.cariMataKuliah(code);
            MataKuliah input = dialogMataKuliah(existing);
            if (input == null) return;
            try {
                dataService.ubahMataKuliah(userSession, code, input);
                perbaruiKurikulumSetelahPerubahan();
                refresh.run();
            } catch (IllegalArgumentException ex) {
                showWarning(ex.getMessage());
            } catch (IOException ex) {
                showError("Mata kuliah tidak dapat diubah: " + ex.getMessage());
            }
        });
        hapus.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                showWarning("Pilih mata kuliah yang akan dihapus.");
                return;
            }
            String code = String.valueOf(model.getValueAt(row, 0));
            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Hapus mata kuliah " + code + "?", "Konfirmasi Hapus",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
            try {
                dataService.hapusMataKuliah(userSession, code);
                perbaruiKurikulumSetelahPerubahan();
                refresh.run();
            } catch (IllegalArgumentException ex) {
                showWarning(ex.getMessage());
            } catch (IOException ex) {
                showError("Mata kuliah tidak dapat dihapus: " + ex.getMessage());
            }
        });
        tutup.addActionListener(e -> dialog.dispose());
        dialog.setSize(620, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private MataKuliah dialogMataKuliah(MataKuliah existing) {
        JTextField kode = new JTextField(existing == null ? "" : existing.getKode(), 16);
        JTextField nama = new JTextField(existing == null ? "" : existing.getNama(), 24);
        JSpinner semester = new JSpinner(new SpinnerNumberModel(
                existing == null ? 1 : existing.getSemester(), 1, 8, 1));
        JSpinner sks = new JSpinner(new SpinnerNumberModel(
                existing == null ? 3 : existing.getSks(), 1, 24, 1));
        kode.setEditable(existing == null);
        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 8));
        fields.add(new JLabel("Kode Mata Kuliah"));
        fields.add(kode);
        fields.add(new JLabel("Nama Mata Kuliah"));
        fields.add(nama);
        fields.add(new JLabel("Semester"));
        fields.add(semester);
        fields.add(new JLabel("SKS"));
        fields.add(sks);
        int result = JOptionPane.showConfirmDialog(this, fields,
                existing == null ? "Tambah Mata Kuliah" : "Ubah Mata Kuliah",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return null;
        return new MataKuliah(kode.getText(), nama.getText(),
                (Integer) semester.getValue(), (Integer) sks.getValue(),
                existing != null && existing.isSimulasi(),
                existing != null && existing.isUsulan());
    }

    private void perbaruiKurikulumSetelahPerubahan() {
        kurikulumField.removeAllItems();
        for (MataKuliah course : dataService.semuaMataKuliah()) kurikulumField.addItem(course);
        bangunDaftarMataKuliahPrasyarat();
        refreshSetelahPerubahan();
    }

    private JPanel labeledInput(String title, JComponent input) {
        JPanel field = new JPanel(new BorderLayout(0, 4));
        field.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(NAVY);
        field.add(label, BorderLayout.NORTH);
        field.add(input, BorderLayout.CENTER);
        return field;
    }

    private JPanel createBidangPage() {
        JPanel page = page("Bidang Peminatan",
                "Pilih bidang, atur mata kuliah pendukung, lalu simpan pengaturannya.",
                PenjurusanIcon.BOOK);
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel selectionSection = card();
        selectionSection.setLayout(new BorderLayout(0, 10));
        selectionSection.add(sectionTitle("Pilih Bidang Peminatan"), BorderLayout.NORTH);
        JPanel selectionCards = new JPanel(new GridLayout(1, 3, 12, 0));
        selectionCards.setOpaque(false);
        for (String bidang : RekomendasiService.BIDANG) {
            JToggleButton choice = new JToggleButton(
                    "<html><div style='width:200px'><b>" + escapeHtml(bidang)
                            + "</b><br><br><span style='font-weight:normal'>"
                            + escapeHtml(deskripsiBidang(bidang)) + "</span></div></html>");
            choice.setHorizontalAlignment(SwingConstants.LEFT);
            choice.setVerticalAlignment(SwingConstants.TOP);
            choice.setFont(new Font("SansSerif", Font.PLAIN, 13));
            choice.setForeground(NAVY);
            choice.setBackground(Color.WHITE);
            choice.setOpaque(true);
            choice.setFocusPainted(false);
            choice.setPreferredSize(new Dimension(220, 92));
            choice.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(205, 217, 231), 1),
                    BorderFactory.createEmptyBorder(12, 14, 12, 14)));
            choice.addActionListener(e -> pilihBidangPeminatan(bidang));
            bidangPilihanCards.put(bidang, choice);
            selectionCards.add(choice);
        }
        selectionSection.add(selectionCards, BorderLayout.CENTER);
        content.add(selectionSection);
        content.add(Box.createVerticalStrut(12));

        JPanel coursesSection = card();
        coursesSection.setLayout(new BorderLayout(0, 10));
        JPanel coursesHeading = new JPanel(new BorderLayout(10, 0));
        coursesHeading.setOpaque(false);
        coursesHeading.add(sectionTitle("Mata Kuliah Prasyarat"), BorderLayout.WEST);
        bidangKelolaStatus.setFont(new Font("SansSerif", Font.BOLD, 13));
        bidangKelolaStatus.setForeground(BLUE);
        coursesHeading.add(bidangKelolaStatus, BorderLayout.EAST);
        JPanel coursesHeader = new JPanel(new BorderLayout(0, 5));
        coursesHeader.setOpaque(false);
        coursesHeader.add(coursesHeading, BorderLayout.NORTH);
        JLabel semesterNote = new JLabel(
                "Semester 7 ditampilkan sebagai referensi; prasyarat penjurusan diambil dari semester 1–6.");
        semesterNote.setFont(new Font("SansSerif", Font.PLAIN, 12));
        semesterNote.setForeground(new Color(95, 111, 130));
        coursesHeader.add(semesterNote, BorderLayout.SOUTH);
        coursesSection.add(coursesHeader, BorderLayout.NORTH);

        bangunDaftarMataKuliahPrasyarat();
        JScrollPane courses = new JScrollPane(bidangMataKuliahList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        courses.setBorder(BorderFactory.createEmptyBorder());
        courses.getVerticalScrollBar().setUnitIncrement(20);
        courses.setPreferredSize(new Dimension(700, 300));
        coursesSection.add(courses, BorderLayout.CENTER);
        content.add(coursesSection);
        content.add(Box.createVerticalStrut(12));

        JPanel summarySection = card();
        summarySection.setLayout(new BorderLayout(0, 10));
        summarySection.add(sectionTitle("Ringkasan Pengaturan"), BorderLayout.NORTH);
        bidangRingkasanList.setOpaque(false);
        for (String bidang : RekomendasiService.BIDANG) {
            JPanel summary = roundedCard(Color.WHITE);
            summary.setLayout(new BorderLayout(0, 7));
            summary.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(221, 230, 239)),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)));
            JLabel title = new JLabel("<html><b>" + escapeHtml(bidang) + "</b></html>");
            title.setFont(new Font("SansSerif", Font.PLAIN, 13));
            JLabel count = new JLabel("0 mata kuliah prasyarat");
            count.setFont(new Font("SansSerif", Font.BOLD, 12));
            count.setForeground(BLUE);
            JTextArea list = new JTextArea("Belum ada mata kuliah yang dipilih.", 4, 20);
            list.setEditable(false);
            list.setLineWrap(true);
            list.setWrapStyleWord(true);
            list.setOpaque(false);
            list.setFont(new Font("SansSerif", Font.PLAIN, 12));
            JPanel header = new JPanel(new BorderLayout(0, 5));
            header.setOpaque(false);
            header.add(title, BorderLayout.NORTH);
            header.add(count, BorderLayout.SOUTH);
            summary.add(header, BorderLayout.NORTH);
            JScrollPane listScroll = new JScrollPane(list,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            listScroll.setBorder(BorderFactory.createEmptyBorder());
            listScroll.setOpaque(false);
            listScroll.getViewport().setOpaque(false);
            listScroll.setPreferredSize(new Dimension(210, 78));
            summary.add(listScroll, BorderLayout.CENTER);
            bidangRingkasanCounts.put(bidang, count);
            bidangRingkasanTexts.put(bidang, list);
            bidangRingkasanList.add(summary);
        }
        summarySection.add(bidangRingkasanList, BorderLayout.CENTER);
        content.add(summarySection);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(12, 0, 4, 0));
        JButton save = button("Simpan Pengaturan", BLUE);
        save.addActionListener(e -> simpanPersyaratanBidang());
        footer.add(save);
        content.add(footer);

        JScrollPane pageScroll = new JScrollPane(content,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pageScroll.setBorder(BorderFactory.createEmptyBorder());
        pageScroll.setOpaque(false);
        pageScroll.getViewport().setOpaque(false);
        pageScroll.getVerticalScrollBar().setUnitIncrement(20);
        page.add(pageScroll, BorderLayout.CENTER);
        refreshBidangPeminatan();
        isiPersyaratanBidang();
        return page;
    }

    private void bangunDaftarMataKuliahPrasyarat() {
        simpanDraftBidangAktif();
        bidangMataKuliahList.removeAll();
        bidangMataKuliahChecks.clear();
        bidangMataKuliahList.setLayout(new GridLayout(0, 2, 12, 12));
        bidangMataKuliahList.setOpaque(false);
        for (int semester = 1; semester <= 7; semester++) {
            JPanel semesterPanel = roundedCard(Color.WHITE);
            semesterPanel.setLayout(new BorderLayout(0, 8));
            semesterPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(221, 230, 239)),
                    BorderFactory.createEmptyBorder(11, 12, 11, 12)));
            JLabel title = new JLabel("SEMESTER " + semester);
            title.setFont(new Font("SansSerif", Font.BOLD, 12));
            title.setForeground(NAVY);
            title.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, GOLD));
            JPanel courseGrid = new JPanel(new GridLayout(0, 2, 4, 5));
            courseGrid.setOpaque(false);
            for (MataKuliah course : dataService.mataKuliahSemester(semester)) {
                JCheckBox check = new JCheckBox("<html><div style='width:145px'><b>"
                        + escapeHtml(course.getKode()) + "</b><br>"
                        + escapeHtml(course.getNama())
                        + (course.isUsulan() ? " <i>[USULAN]</i>" : "")
                        + "</div></html>");
                check.setOpaque(false);
                check.setFont(new Font("SansSerif", Font.PLAIN, 12));
                check.setForeground(TEXT);
                check.setVerticalAlignment(SwingConstants.TOP);
                check.setToolTipText(course.getKode() + " — " + course.getNama()
                        + (semester == 7
                        ? " (tidak dapat dipilih sebagai prasyarat penjurusan semester 7)" : ""));
                if (semester == 7) {
                    check.setEnabled(false);
                } else {
                    check.addActionListener(e -> {
                        simpanDraftBidangAktif();
                        perbaruiJumlahPendukung();
                    });
                }
                bidangMataKuliahChecks.put(course.getKode(), check);
                courseGrid.add(check);
            }
            semesterPanel.add(title, BorderLayout.NORTH);
            semesterPanel.add(courseGrid, BorderLayout.CENTER);
            bidangMataKuliahList.add(semesterPanel);
        }
        String bidang = (String) bidangKelolaField.getSelectedItem();
        BidangPeminatan saved = dataService.bidangPeminatan(bidang);
        Set<String> selected = bidangMataKuliahDrafts.get(bidang);
        if (selected == null && saved != null) {
            selected = new java.util.LinkedHashSet<>(saved.getKodeMataKuliahPendukung());
        }
        if (selected != null) {
            for (Map.Entry<String, JCheckBox> entry : bidangMataKuliahChecks.entrySet()) {
                entry.getValue().setSelected(selected.contains(entry.getKey()));
            }
        }
        bidangMataKuliahList.revalidate();
        bidangMataKuliahList.repaint();
    }

    private String deskripsiBidang(String bidang) {
        if (RekomendasiService.BIDANG.get(0).equals(bidang)) {
            return "Fokus pada interaksi manusia, pengalaman pengguna, dan rancangan antarmuka.";
        }
        if (RekomendasiService.BIDANG.get(1).equals(bidang)) {
            return "Fokus pada perencanaan bisnis, pengembangan, dan pengelolaan proyek SI.";
        }
        return "Fokus pada keamanan, audit, serta tata kelola teknologi informasi.";
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    private JPanel createRiwayatPage() {
        JPanel page = createContentBackgroundPage(0, 0);
        page.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

        JPanel heading = new JPanel(new BorderLayout(14, 0));
        heading.setOpaque(false);
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JPanel titleIcon = createGradientTitleIcon(PenjurusanIcon.GRADUATION);

        JPanel titleText = new JPanel(new BorderLayout(0, 3));
        titleText.setOpaque(false);
        JLabel title = new JLabel("Penjurusan Semester 7");
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setForeground(NAVY);
        JLabel subtitle = new JLabel("Periksa riwayat mata kuliah dan lihat alternatif penjurusan.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(102, 116, 134));
        titleText.add(title, BorderLayout.NORTH);
        titleText.add(subtitle, BorderLayout.CENTER);

        JPanel titleAccent = new JPanel();
        titleAccent.setBackground(GOLD);
        titleAccent.setPreferredSize(new Dimension(4, 42));
        JPanel titleGroup = new JPanel(new BorderLayout(14, 0));
        titleGroup.setOpaque(false);
        titleGroup.add(titleAccent, BorderLayout.WEST);
        titleGroup.add(titleText, BorderLayout.CENTER);
        heading.add(titleIcon, BorderLayout.WEST);
        heading.add(titleGroup, BorderLayout.CENTER);
        page.add(heading, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 0));
        stats.setOpaque(false);
        stats.add(penjurusanStatCard("Total Mahasiswa Semester 7",
                penjurusanTotalSemester7, new Color(48, 132, 239), new Color(220, 235, 255),
                PenjurusanIcon.USERS));
        stats.add(penjurusanStatCard("Memenuhi Syarat Penjurusan",
                penjurusanTotalLayak, new Color(235, 168, 22), new Color(255, 239, 199),
                PenjurusanIcon.CHECKLIST));
        stats.add(penjurusanStatCard("Belum Memenuhi Syarat",
                penjurusanTotalBelumLayak, new Color(25, 169, 169), new Color(211, 245, 242),
                PenjurusanIcon.STAR));
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        stats.setMinimumSize(new Dimension(0, 102));
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 102));
        content.add(stats);
        content.add(Box.createVerticalStrut(14));

        JPanel form = new JPanel(new BorderLayout(0, 12)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Color.WHITE);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                java.awt.Shape oldClip = g.getClip();
                g.clip(new java.awt.geom.RoundRectangle2D.Float(
                        0, 0, getWidth() - 1, getHeight() - 1, 18, 18));
                int waveY = getHeight() - 55;
                java.awt.geom.Path2D wave = new java.awt.geom.Path2D.Float();
                wave.moveTo(0, waveY + 9);
                wave.curveTo(getWidth() * 0.22, waveY - 5, getWidth() * 0.48,
                        getHeight() + 10, getWidth() * 0.73, waveY + 3);
                wave.curveTo(getWidth() * 0.87, waveY - 3, getWidth() * 0.94,
                        waveY - 16, getWidth(), waveY - 24);
                wave.lineTo(getWidth(), getHeight());
                wave.lineTo(0, getHeight());
                wave.closePath();
                g.setColor(new Color(16, 63, 117));
                g.fill(wave);
                g.setColor(new Color(27, 102, 182));
                g.setStroke(new java.awt.BasicStroke(12f));
                g.draw(new java.awt.geom.QuadCurve2D.Double(0, waveY + 4,
                        getWidth() * 0.59, getHeight() + 10, getWidth(), waveY - 17));
                g.setColor(new Color(243, 190, 47));
                g.setStroke(new java.awt.BasicStroke(6f));
                g.draw(new java.awt.geom.QuadCurve2D.Double(0, waveY - 2,
                        getWidth() * 0.56, getHeight() + 7, getWidth(), waveY - 25));
                g.setClip(oldClip);
                g.setColor(new Color(193, 214, 240));
                g.setStroke(new java.awt.BasicStroke(1.2f));
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g.dispose();
            }
        };
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        form.setPreferredSize(new Dimension(0, 400));
        form.setMinimumSize(new Dimension(0, 380));
        JPanel formTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        formTitle.setOpaque(false);
        JLabel formIcon = new JLabel(new PenjurusanIcon(PenjurusanIcon.CLIPBOARD, NAVY, 20));
        JLabel formTitleLabel = new JLabel("Formulir Penjurusan");
        formTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        formTitleLabel.setForeground(NAVY);
        formTitle.add(formIcon);
        formTitle.add(formTitleLabel);
        form.add(formTitle, BorderLayout.NORTH);
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        int row = 0;
        penjurusanNimField.setEditable(true);
        penjurusanNimField.setToolTipText("Ketik untuk mencari NIM mahasiswa semester 7");
        penjurusanNimField.setMaximumRowCount(10);
        penjurusanTanggalField.setText(LocalDate.now().toString());
        penjurusanNamaLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        stylePenjurusanInput(penjurusanNimField);
        stylePenjurusanInput(penjurusanBidangField);
        stylePenjurusanInput(penjurusanTanggalField);
        penjurusanNamaLabel.setOpaque(true);
        penjurusanNamaLabel.setBackground(new Color(232, 238, 248));
        penjurusanNamaLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(207, 219, 232)),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
        row = addField(fields, row, "NIM Mahasiswa", penjurusanNimField);
        row = addField(fields, row, "Nama Mahasiswa", penjurusanNamaLabel);
        row = addField(fields, row, "Bidang Peminatan", penjurusanBidangField);
        row = addField(fields, row, "Tanggal Penjurusan", penjurusanTanggalField);
        form.add(fields, BorderLayout.CENTER);

        penjurusanSimpanButton = button("Proses Penjurusan", new Color(8, 40, 79));
        penjurusanSimpanButton.setIcon(new PenjurusanIcon(PenjurusanIcon.SEARCH, Color.WHITE, 20));
        penjurusanSimpanButton.setIconTextGap(10);
        penjurusanSimpanButton.setEnabled(false);
        penjurusanSimpanButton.addActionListener(e -> simpanPenjurusan());
        JPanel formBottom = new JPanel(new BorderLayout(0, 10));
        formBottom.setOpaque(false);
        penjurusanSimpanButton.setPreferredSize(new Dimension(280, 46));
        JPanel buttonRow = new JPanel(new BorderLayout());
        buttonRow.setOpaque(false);
        buttonRow.add(penjurusanSimpanButton, BorderLayout.EAST);
        formBottom.add(buttonRow, BorderLayout.SOUTH);
        form.add(formBottom, BorderLayout.SOUTH);
        penjurusanNimField.addActionListener(e -> perbaruiStatusPenjurusan());
        penjurusanBidangField.addActionListener(e -> perbaruiStatusPenjurusan());

        JPanel exam = card();
        exam.setLayout(new BorderLayout(0, 10));
        JPanel examTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        examTitle.setOpaque(false);
        JLabel examIcon = new JLabel(new PenjurusanIcon(PenjurusanIcon.CHART, NAVY, 20));
        JLabel examTitleLabel = new JLabel("Hasil Pemeriksaan Akademik");
        examTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        examTitleLabel.setForeground(NAVY);
        examTitle.add(examIcon);
        examTitle.add(examTitleLabel);
        exam.add(examTitle, BorderLayout.NORTH);
        JPanel examBody = new JPanel(new BorderLayout(0, 9));
        examBody.setOpaque(false);
        JPanel counts = new JPanel(new GridLayout(1, 3, 8, 0));
        counts.setOpaque(false);
        counts.add(examCountCard("Prasyarat", prasyaratTotalValue,
                new Color(49, 128, 222), PenjurusanIcon.CHECK));
        counts.add(examCountCard("Lulus", prasyaratLulusValue,
                new Color(60, 171, 63), PenjurusanIcon.CHECK));
        counts.add(examCountCard("Tidak Lulus", prasyaratTidakLulusValue,
                new Color(222, 42, 42), PenjurusanIcon.CROSS));
        examBody.add(counts, BorderLayout.NORTH);
        pemeriksaanContent.setOpaque(false);
        JPanel noSelection = new JPanel(new BorderLayout());
        noSelection.setOpaque(false);
        JLabel noSelectionLabel = new JLabel(
                "Pilih mahasiswa untuk melihat hasil pemeriksaan akademik", SwingConstants.CENTER);
        noSelectionLabel.setForeground(new Color(102, 116, 134));
        noSelection.add(noSelectionLabel, BorderLayout.CENTER);
        JScrollPane examScroll = tableScrollPane(pemeriksaanPrasyaratTable);
        examScroll.setPreferredSize(new Dimension(270, 180));
        pemeriksaanContent.add(noSelection, "empty");
        pemeriksaanContent.add(examScroll, "results");
        examBody.add(pemeriksaanContent, BorderLayout.CENTER);
        prasyaratStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        JPanel statusCard = roundedCard(LIGHT_BLUE);
        statusCard.setLayout(new BorderLayout(10, 0));
        JLabel infoIcon = new JLabel(new PenjurusanIcon(PenjurusanIcon.INFO,
                new Color(47, 134, 241), 30));
        statusCard.add(infoIcon, BorderLayout.WEST);
        statusCard.add(prasyaratStatusLabel, BorderLayout.CENTER);
        examBody.add(statusCard, BorderLayout.SOUTH);
        exam.add(examBody, BorderLayout.CENTER);
        exam.setPreferredSize(new Dimension(0, 255));
        setColumnWidths(pemeriksaanPrasyaratTable, 105, 50, 45, 80);
        pemeriksaanPrasyaratTable.setRowHeight(34);
        setStatusRenderer(pemeriksaanPrasyaratTable, 3);

        JPanel recommendations = roundedCard(Color.WHITE);
        recommendations.setLayout(new BorderLayout(0, 6));
        JPanel recommendationHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        recommendationHeader.setOpaque(false);
        recommendationHeader.add(new JLabel(new PenjurusanIcon(
                PenjurusanIcon.LIGHTBULB, new Color(237, 177, 35), 22)));
        JLabel recommendationTitle = new JLabel("Rekomendasi Bidang Lain");
        recommendationTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        recommendationTitle.setForeground(NAVY);
        recommendationHeader.add(recommendationTitle);
        recommendations.add(recommendationHeader, BorderLayout.NORTH);
        rekomendasiPenjurusanArea.setEditable(false);
        rekomendasiPenjurusanArea.setLineWrap(true);
        rekomendasiPenjurusanArea.setWrapStyleWord(true);
        rekomendasiPenjurusanArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        rekomendasiPenjurusanArea.setRows(3);
        rekomendasiPenjurusanArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        rekomendasiPenjurusanArea.setBackground(new Color(238, 244, 255));
        JScrollPane recommendationScroll = new JScrollPane(rekomendasiPenjurusanArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        recommendationScroll.setBorder(BorderFactory.createEmptyBorder());
        recommendationScroll.setPreferredSize(new Dimension(300, 86));
        JPanel recommendationBody = new JPanel(new BorderLayout(12, 0));
        recommendationBody.setOpaque(false);
        recommendationBody.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        recommendationBody.add(new JLabel(new PenjurusanIcon(
                PenjurusanIcon.DOCUMENT, new Color(57, 139, 236), 34)), BorderLayout.WEST);
        recommendationBody.add(recommendationScroll, BorderLayout.CENTER);
        recommendations.add(recommendationBody, BorderLayout.CENTER);
        recommendations.setPreferredSize(new Dimension(0, 120));
        JPanel resultPanel = new JPanel(new BorderLayout(0, 10));
        resultPanel.setOpaque(false);
        resultPanel.add(exam, BorderLayout.CENTER);
        resultPanel.add(recommendations, BorderLayout.SOUTH);

        JPanel columns = new JPanel(new GridBagLayout());
        columns.setOpaque(false);
        GridBagConstraints column = new GridBagConstraints();
        column.gridy = 0;
        column.fill = GridBagConstraints.BOTH;
        column.anchor = GridBagConstraints.NORTHWEST;
        column.insets = new Insets(0, 0, 0, 8);
        column.gridx = 0;
        column.weightx = 0.48;
        column.weighty = 1;
        columns.add(form, column);
        column.gridx = 1;
        column.weightx = 0.52;
        column.insets = new Insets(0, 4, 0, 0);
        columns.add(resultPanel, column);
        columns.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(columns);
        content.add(Box.createVerticalStrut(12));

        JPanel history = card();
        history.setLayout(new BorderLayout(0, 10));
        JPanel historyHeader = new JPanel(new BorderLayout(10, 0));
        historyHeader.setOpaque(false);
        historyHeader.add(sectionTitle("Riwayat Penjurusan Mahasiswa"), BorderLayout.WEST);
        cariRiwayatPenjurusanField.setToolTipText("Cari berdasarkan NIM atau nama mahasiswa");
        historyHeader.add(cariRiwayatPenjurusanField, BorderLayout.EAST);
        history.add(historyHeader, BorderLayout.NORTH);
        JPanel historyEmpty = new JPanel(new BorderLayout());
        historyEmpty.setOpaque(false);
        riwayatPenjurusanEmptyLabel.setForeground(new Color(102, 116, 134));
        historyEmpty.add(riwayatPenjurusanEmptyLabel, BorderLayout.CENTER);
        riwayatPenjurusanContent.add(tableScrollPane(riwayatPenjurusanTable), "results");
        riwayatPenjurusanContent.add(historyEmpty, "empty");
        history.add(riwayatPenjurusanContent, BorderLayout.CENTER);
        history.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(history);
        cariRiwayatPenjurusanField.getDocument().addDocumentListener(
                documentListener(this::refreshRiwayatPenjurusan));

        JScrollPane pageScroll = new JScrollPane(content,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pageScroll.setBorder(BorderFactory.createEmptyBorder());
        pageScroll.setOpaque(false);
        pageScroll.getViewport().setOpaque(false);
        pageScroll.getVerticalScrollBar().setUnitIncrement(18);
        page.add(pageScroll, BorderLayout.CENTER);
        setColumnWidths(riwayatPenjurusanTable, 90, 160, 220, 110, 150);
        riwayatPenjurusanTable.setRowHeight(36);
        setStatusRenderer(riwayatPenjurusanTable, 4);
        perbaruiStatusPenjurusan();
        refreshRiwayatPenjurusan();
        refreshStatistikPenjurusan();
        return page;
    }

    private JPanel statCard(String title, JLabel value, Color accent, String iconText) {
        JPanel panel = roundedCard(Color.WHITE);
        panel.setLayout(new BorderLayout(12, 0));
        JLabel icon = new JLabel(iconText, SwingConstants.CENTER);
        icon.setForeground(Color.WHITE);
        icon.setFont(new Font("SansSerif", Font.BOLD, 16));
        icon.setOpaque(true);
        icon.setBackground(accent);
        icon.setPreferredSize(new Dimension(42, 42));
        panel.add(icon, BorderLayout.WEST);
        JPanel details = new JPanel(new BorderLayout(0, 3));
        details.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setForeground(new Color(102, 116, 134));
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        value.setForeground(accent);
        value.setFont(new Font("SansSerif", Font.BOLD, 23));
        details.add(label, BorderLayout.NORTH);
        details.add(value, BorderLayout.CENTER);
        panel.add(details, BorderLayout.CENTER);
        return panel;
    }

    private JPanel penjurusanStatCard(String title, JLabel value, Color accent,
                                      Color tint, int iconType) {
        JPanel panel = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Color.WHITE);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g.setColor(accent);
                g.fillArc(getWidth() - 135, getHeight() - 64, 150, 85, 12, 155);
                g.setColor(new Color(221, 230, 239));
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        panel.setPreferredSize(new Dimension(300, 102));

        JPanel badge = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(tint);
                g.fillOval(0, 0, getWidth(), getHeight());
                g.setColor(Color.WHITE);
                g.fillOval(7, 7, getWidth() - 14, getHeight() - 14);
                g.setColor(accent);
                g.fillOval(12, 12, getWidth() - 24, getHeight() - 24);
                g.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(70, 70));
        badge.add(new JLabel(new PenjurusanIcon(iconType, Color.WHITE, 30)));
        panel.add(badge, BorderLayout.WEST);

        JPanel details = new JPanel(new BorderLayout(0, 2));
        details.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setForeground(NAVY);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        value.setForeground(NAVY);
        value.setFont(new Font("SansSerif", Font.BOLD, 24));
        details.add(label, BorderLayout.NORTH);
        details.add(value, BorderLayout.CENTER);
        panel.add(details, BorderLayout.CENTER);
        return panel;
    }

    private JPanel examCountCard(String title, JLabel value, Color accent, int iconType) {
        JPanel panel = new JPanel(new BorderLayout(0, 3));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 214, 233)));
        JPanel badge = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(accent);
                g.fillOval(0, 0, getWidth(), getHeight());
                g.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(38, 38));
        badge.add(new JLabel(new PenjurusanIcon(iconType, Color.WHITE, 23)));
        value.setForeground(NAVY);
        value.setFont(new Font("SansSerif", Font.BOLD, 23));
        value.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setForeground(NAVY);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JPanel details = new JPanel(new BorderLayout(0, 0));
        details.setOpaque(false);
        details.add(value, BorderLayout.CENTER);
        details.add(label, BorderLayout.SOUTH);
        panel.add(badge, BorderLayout.NORTH);
        panel.add(details, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAcademicSummary() {
        JPanel summary = roundedCard(LIGHT_BLUE);
        summary.setLayout(new BorderLayout(0, 12));
        JPanel identity = new JPanel(new java.awt.GridLayout(2, 2, 12, 8));
        identity.setOpaque(false);
        identity.add(summaryItem("NIM", informasiNimValue));
        identity.add(summaryItem("Nama Mahasiswa", informasiNamaValue));
        identity.add(summaryItem("Semester", informasiSemesterValue));
        identity.add(summaryItem("Kelayakan", informasiKelayakanValue));
        JPanel totals = new JPanel(new java.awt.GridLayout(1, 3, 10, 0));
        totals.setOpaque(false);
        totals.add(summaryCount("Mata Kuliah", jumlahMataKuliahValue, NAVY));
        totals.add(summaryCount("Lulus", jumlahLulusValue, new Color(31, 126, 78)));
        totals.add(summaryCount("Tidak Lulus", jumlahTidakLulusValue, new Color(180, 62, 62)));
        summary.add(identity, BorderLayout.CENTER);
        summary.add(totals, BorderLayout.SOUTH);
        return summary;
    }

    private JPanel summaryItem(String title, JLabel value) {
        JPanel item = new JPanel(new BorderLayout(0, 3));
        item.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        label.setForeground(new Color(102, 116, 134));
        value.setFont(new Font("SansSerif", Font.BOLD, 13));
        value.setForeground(NAVY);
        item.add(label, BorderLayout.NORTH);
        item.add(value, BorderLayout.CENTER);
        return item;
    }

    private JPanel summaryCount(String title, JLabel value, Color color) {
        JPanel item = new JPanel(new BorderLayout(0, 2));
        item.setOpaque(false);
        value.setFont(new Font("SansSerif", Font.BOLD, 17));
        value.setForeground(color);
        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        label.setForeground(new Color(102, 116, 134));
        item.add(value, BorderLayout.NORTH);
        item.add(label, BorderLayout.CENTER);
        return item;
    }

    private void setStatusRenderer(JTable table, int statusColumn) {
        table.getColumnModel().getColumn(statusColumn).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value,
                    boolean selected, boolean focused, int row, int column) {
                super.getTableCellRendererComponent(source, value, selected, focused, row, column);
                String status = value == null ? "" : value.toString();
                boolean passed = status.startsWith("Lulus")
                        || status.startsWith("Terverifikasi")
                        || status.startsWith("Memenuhi syarat")
                        || status.startsWith("MEMENUHI SYARAT");
                boolean pending = status.startsWith("Menunggu Admin");
                boolean failed = status.startsWith("Tidak Lulus")
                        || status.startsWith("Belum memenuhi")
                        || status.startsWith("Tidak memenuhi syarat")
                        || status.startsWith("TIDAK MEMENUHI SYARAT");
                setForeground(passed ? new Color(31, 126, 78)
                        : failed ? new Color(180, 62, 62)
                        : pending ? new Color(166, 112, 16) : selected ? NAVY : TEXT);
                setBackground(selected ? new Color(222, 237, 250)
                        : row % 2 == 0 ? Color.WHITE : new Color(243, 247, 251));
                setFont(source.getFont().deriveFont(Font.BOLD));
                setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
                setOpaque(true);
                return this;
            }
        });
    }

    private JPanel createRekomendasiPage() {
        JPanel page = createContentBackgroundPage(0, 12);
        page.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel heading = new JPanel(new BorderLayout(12, 0));
        heading.setOpaque(false);
        heading.setBorder(BorderFactory.createEmptyBorder(0, 4, 2, 0));
        JPanel titleIcon = createGradientTitleIcon(PenjurusanIcon.GRADUATION);
        JPanel titleAccent = new JPanel();
        titleAccent.setBackground(GOLD);
        titleAccent.setPreferredSize(new Dimension(3, 42));
        JPanel titleText = new JPanel(new BorderLayout(0, 3));
        titleText.setOpaque(false);
        JLabel title = new JLabel("Rekomendasi Peminatan");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(NAVY);
        JLabel subtitle = new JLabel(
                "Penilaian berdasarkan nilai mata kuliah pendukung pada setiap bidang.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(67, 87, 115));
        titleText.add(title, BorderLayout.NORTH);
        titleText.add(subtitle, BorderLayout.CENTER);
        JPanel titleGroup = new JPanel(new BorderLayout(12, 0));
        titleGroup.setOpaque(false);
        titleGroup.add(titleAccent, BorderLayout.WEST);
        titleGroup.add(titleText, BorderLayout.CENTER);
        heading.add(titleIcon, BorderLayout.WEST);
        heading.add(titleGroup, BorderLayout.CENTER);
        page.add(heading, BorderLayout.NORTH);

        JPanel top = card();
        top.setLayout(new GridBagLayout());
        top.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        GridBagConstraints row = new GridBagConstraints();
        row.gridy = 0;
        row.insets = new Insets(3, 4, 7, 4);
        row.anchor = GridBagConstraints.CENTER;
        row.fill = GridBagConstraints.HORIZONTAL;
        JLabel searchIcon = new JLabel(new PenjurusanIcon(PenjurusanIcon.USER, NAVY, 20));
        row.gridx = 0;
        row.weightx = 0;
        top.add(searchIcon, row);
        JLabel searchLabel = new JLabel("Cari mahasiswa berdasarkan NIM atau nama");
        searchLabel.setForeground(NAVY);
        searchLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        searchLabel.setPreferredSize(new Dimension(260, 28));
        row.gridx = 1;
        top.add(searchLabel, row);
        rekomendasiCariField.setColumns(20);
        rekomendasiCariField.setToolTipText("Ketik NIM atau nama untuk menyaring pilihan mahasiswa");
        stylePenjurusanInput(rekomendasiCariField);
        row.gridx = 2;
        row.weightx = 1;
        top.add(rekomendasiCariField, row);
        JButton searchButton = button("Cari", new Color(8, 40, 79));
        searchButton.setIcon(new PenjurusanIcon(PenjurusanIcon.SEARCH, Color.WHITE, 18));
        searchButton.setIconTextGap(8);
        searchButton.setPreferredSize(new Dimension(130, 38));
        row.gridx = 3;
        row.weightx = 0;
        top.add(searchButton, row);
        searchButton.addActionListener(e -> filterPilihanRekomendasi());

        row.gridy = 1;
        row.insets = new Insets(4, 4, 2, 4);
        JLabel selectionIcon = new JLabel(new PenjurusanIcon(PenjurusanIcon.BRIEFCASE, NAVY, 20));
        row.gridx = 0;
        top.add(selectionIcon, row);
        JLabel selectionLabel = new JLabel("Pilih mahasiswa");
        selectionLabel.setForeground(NAVY);
        selectionLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        selectionLabel.setPreferredSize(new Dimension(260, 28));
        row.gridx = 1;
        top.add(selectionLabel, row);
        rekomendasiNimField.setPreferredSize(new Dimension(0, 38));
        rekomendasiNimField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        rekomendasiNimField.setBackground(Color.WHITE);
        rekomendasiNimField.setForeground(NAVY);
        rekomendasiNimField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 216, 235)),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        JPanel selectedStudent = new JPanel(new BorderLayout(0, 2));
        selectedStudent.setOpaque(false);
        selectedStudent.add(rekomendasiNimField, BorderLayout.CENTER);
        rekomendasiNamaLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        rekomendasiNamaLabel.setForeground(new Color(94, 111, 134));
        selectedStudent.add(rekomendasiNamaLabel, BorderLayout.SOUTH);
        row.gridx = 2;
        row.weightx = 1;
        top.add(selectedStudent, row);
        JButton analyze = button("Hitung Rekomendasi", new Color(8, 40, 79));
        analyze.setIcon(new PenjurusanIcon(PenjurusanIcon.CHART, Color.WHITE, 19));
        analyze.setIconTextGap(8);
        analyze.setPreferredSize(new Dimension(205, 40));
        row.gridx = 3;
        row.weightx = 0;
        top.add(analyze, row);
        analyze.addActionListener(e -> analisisRekomendasi());
        rekomendasiNimField.addActionListener(e -> {
            if (!memperbaruiPilihanRekomendasi) isiNamaRekomendasi();
        });
        rekomendasiCariField.getDocument().addDocumentListener(
                documentListener(this::filterPilihanRekomendasi));

        JPanel results = card();
        results.setLayout(new BorderLayout(0, 10));
        results.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        JPanel resultHeading = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));
        resultHeading.setOpaque(false);
        resultHeading.add(new JLabel(new PenjurusanIcon(PenjurusanIcon.BOOK, NAVY, 22)));
        JLabel resultTitle = sectionTitle("Bidang peminatan dan skor");
        resultTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        resultHeading.add(resultTitle);
        results.add(resultHeading, BorderLayout.NORTH);
        JScrollPane tableScroll = tableScrollPane(rekomendasiTable);
        tableScroll.setPreferredSize(new Dimension(0, 180));
        tableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(207, 220, 241)));
        tableScroll.getViewport().setBackground(Color.WHITE);
        setColumnWidths(rekomendasiTable, 100, 170, 175, 330, 250);
        rekomendasiTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        rekomendasiTable.setRowHeight(48);
        rekomendasiTable.getTableHeader().setBackground(NAVY);
        rekomendasiTable.getTableHeader().setForeground(Color.WHITE);
        rekomendasiTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        rekomendasiTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        rekomendasiTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value,
                    boolean selected, boolean focused, int headerRow, int column) {
                super.getTableCellRendererComponent(source, value, selected, focused, headerRow, column);
                setFont(source.getTableHeader().getFont());
                setForeground(Color.WHITE);
                setBackground(NAVY);
                setHorizontalAlignment(SwingConstants.CENTER);
                setOpaque(true);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(65, 92, 130)),
                        BorderFactory.createEmptyBorder(5, 6, 5, 6)));
                return this;
            }
        });
        JPanel emptyResults = new JPanel(new GridBagLayout());
        emptyResults.setBackground(Color.WHITE);
        JPanel emptyMessage = new JPanel(new BorderLayout(0, 5));
        emptyMessage.setOpaque(false);
        JPanel emptyIllustration = new JPanel(new GridBagLayout());
        emptyIllustration.setOpaque(false);
        emptyIllustration.add(new JLabel(new PenjurusanIcon(
                PenjurusanIcon.DOCUMENT, new Color(58, 123, 207), 42)));
        JLabel emptyTitle = new JLabel("Belum ada data", SwingConstants.CENTER);
        emptyTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        emptyTitle.setForeground(NAVY);
        JLabel emptyHint = new JLabel("Silakan pilih mahasiswa terlebih dahulu", SwingConstants.CENTER);
        emptyHint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        emptyHint.setForeground(new Color(89, 106, 130));
        JPanel emptyText = new JPanel(new GridLayout(2, 1, 0, 2));
        emptyText.setOpaque(false);
        emptyText.add(emptyTitle);
        emptyText.add(emptyHint);
        emptyMessage.add(emptyIllustration, BorderLayout.NORTH);
        emptyMessage.add(emptyText, BorderLayout.CENTER);
        emptyResults.add(emptyMessage);
        rekomendasiHasilContent.setOpaque(false);
        rekomendasiHasilContent.add(emptyResults, "empty");
        rekomendasiHasilContent.add(tableScroll, "table");
        rekomendasiModel.addTableModelListener(e -> {
            rekomendasiHasilLayout.show(rekomendasiHasilContent,
                    rekomendasiModel.getRowCount() == 0 ? "empty" : "table");
        });
        rekomendasiHasilLayout.show(rekomendasiHasilContent,
                rekomendasiModel.getRowCount() == 0 ? "empty" : "table");
        results.add(rekomendasiHasilContent, BorderLayout.CENTER);
        results.setPreferredSize(new Dimension(0, 245));

        JPanel detail = card();
        detail.setLayout(new BorderLayout(0, 8));
        detail.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JPanel detailHeading = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));
        detailHeading.setOpaque(false);
        detailHeading.add(new JLabel(new PenjurusanIcon(
                PenjurusanIcon.LIGHTBULB, new Color(238, 177, 38), 22)));
        JLabel detailTitle = sectionTitle("Ringkasan analisis dan alasan");
        detailTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        detailHeading.add(detailTitle);
        detail.add(detailHeading, BorderLayout.NORTH);
        rekomendasiPenjelasan.setEditable(false);
        rekomendasiPenjelasan.setLineWrap(true);
        rekomendasiPenjelasan.setWrapStyleWord(true);
        rekomendasiPenjelasan.setFont(new Font("SansSerif", Font.PLAIN, 12));
        rekomendasiPenjelasan.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        rekomendasiPenjelasan.setBackground(new Color(226, 237, 255));
        if (rekomendasiPenjelasan.getText().trim().isEmpty()) {
            rekomendasiPenjelasan.setText(
                    "Hasil analisis akan ditampilkan di sini setelah proses perhitungan.");
        }
        rekomendasiPenjelasanScroll = new JScrollPane(rekomendasiPenjelasan);
        rekomendasiPenjelasanScroll.setBorder(BorderFactory.createEmptyBorder());
        rekomendasiPenjelasanScroll.getViewport().setBackground(new Color(226, 237, 255));
        JPanel explanationBox = new JPanel(new BorderLayout(12, 0));
        explanationBox.setBackground(new Color(226, 237, 255));
        explanationBox.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
        explanationBox.add(new JLabel(new PenjurusanIcon(
                PenjurusanIcon.DOCUMENT, new Color(24, 78, 148), 24)), BorderLayout.WEST);
        explanationBox.add(rekomendasiPenjelasanScroll, BorderLayout.CENTER);
        JPanel detailBody = new JPanel(new BorderLayout(0, 5));
        detailBody.setOpaque(false);
        detailBody.add(explanationBox, BorderLayout.CENTER);
        JLabel eligibilityNote = new JLabel(
                "Rekomendasi mensyaratkan seluruh mata kuliah pendukung bernilai minimal C.");
        eligibilityNote.setForeground(new Color(80, 98, 124));
        eligibilityNote.setFont(new Font("SansSerif", Font.PLAIN, 11));
        detailBody.add(eligibilityNote, BorderLayout.SOUTH);
        detail.add(detailBody, BorderLayout.CENTER);
        detail.setPreferredSize(new Dimension(0, 126));

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        top.setAlignmentX(Component.LEFT_ALIGNMENT);
        results.setAlignmentX(Component.LEFT_ALIGNMENT);
        detail.setAlignmentX(Component.LEFT_ALIGNMENT);
        stack.add(top);
        stack.add(Box.createVerticalStrut(10));
        stack.add(results);
        stack.add(Box.createVerticalStrut(10));
        stack.add(detail);
        JScrollPane pageScroll = new JScrollPane(stack,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pageScroll.setBorder(BorderFactory.createEmptyBorder());
        pageScroll.setOpaque(false);
        pageScroll.getViewport().setOpaque(false);
        pageScroll.getVerticalScrollBar().setUnitIncrement(18);
        page.add(pageScroll, BorderLayout.CENTER);
        return page;
    }

    private JPanel createLaporanPage() {
        JPanel page = page("Laporan",
                "Ringkasan akademik, nilai, penjurusan, dan rekomendasi dari data asli serta simulasi.",
                PenjurusanIcon.CHART);
        JPanel filter = card();
        filter.setLayout(new BorderLayout(0, 10));
        JPanel fields = new JPanel(new GridLayout(1, 4, 10, 0));
        fields.setOpaque(false);
        fields.add(reportFilterField("Semester", laporanSemesterField));
        fields.add(reportFilterField("Bidang Peminatan", laporanBidangField));
        fields.add(reportFilterField("Jenis Data", laporanJenisDataField));
        fields.add(reportFilterField("Cari NIM atau Nama", laporanCariField));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton reset = button("Reset Filter", new Color(102, 116, 134));
        JButton apply = button("Terapkan", BLUE);
        JButton export = button("Ekspor Excel (CSV)", new Color(40, 133, 112));
        JButton print = button("Cetak", NAVY);
        JButton pdf = button("Simpan PDF", GOLD);
        actions.add(reset);
        actions.add(apply);
        actions.add(export);
        actions.add(print);
        actions.add(pdf);
        filter.add(fields, BorderLayout.CENTER);
        filter.add(actions, BorderLayout.SOUTH);
        apply.addActionListener(e -> refreshLaporan());
        reset.addActionListener(e -> {
            laporanSemesterField.setSelectedIndex(0);
            laporanBidangField.setSelectedIndex(0);
            laporanJenisDataField.setSelectedIndex(0);
            laporanCariField.setText("");
            refreshLaporan();
        });
        export.addActionListener(e -> eksporLaporan());
        print.addActionListener(e -> cetakLaporan());
        pdf.addActionListener(e -> simpanLaporanPdf());
        laporanSemesterField.addActionListener(e -> refreshLaporan());
        laporanBidangField.addActionListener(e -> refreshLaporan());
        laporanJenisDataField.addActionListener(e -> refreshLaporan());
        laporanCariField.getDocument().addDocumentListener(documentListener(this::refreshLaporan));

        JPanel reportStats = new JPanel(new GridLayout(1, 4, 12, 0));
        reportStats.setOpaque(false);
        reportStats.add(statCard("Total Mahasiswa", laporanTotalMahasiswaValue, BLUE, "01"));
        reportStats.add(statCard("Total Mata Kuliah", laporanTotalMataKuliahValue, TEAL, "02"));
        reportStats.add(statCard("Memenuhi Syarat", laporanLayakValue, new Color(31, 126, 78), "✓"));
        reportStats.add(statCard("Belum Memenuhi", laporanBelumLayakValue, new Color(180, 62, 62), "!"));

        JTabbedPane tabs = new JTabbedPane();
        JPanel summary = new JPanel(new BorderLayout(0, 12));
        summary.setOpaque(false);
        JPanel charts = new JPanel(new GridLayout(2, 2, 12, 12));
        charts.setOpaque(false);
        charts.add(chartCard("Mahasiswa berdasarkan semester", laporanGrafikSemester));
        charts.add(chartCard("Kelulusan mata kuliah", laporanGrafikKelulusan));
        charts.add(chartCard("Mahasiswa berdasarkan bidang peminatan", laporanGrafikBidang));
        charts.add(chartCard("Hasil pemeriksaan penjurusan", laporanGrafikPenjurusan));
        summary.add(charts, BorderLayout.CENTER);
        summary.add(tableScrollPane(createTable(laporanStatistikModel)), BorderLayout.SOUTH);
        summary.setPreferredSize(new Dimension(900, 560));
        tabs.addTab("Ringkasan Statistik", new JScrollPane(summary));
        tabs.addTab("Data Mahasiswa", tableScrollPane(createTable(laporanMahasiswaModel)));
        tabs.addTab("Nilai Mata Kuliah", tableScrollPane(createTable(laporanNilaiModel)));
        tabs.addTab("Riwayat Penjurusan", tableScrollPane(createTable(laporanPenjurusanModel)));
        tabs.addTab("Hasil Rekomendasi", tableScrollPane(createTable(laporanRekomendasiModel)));

        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.add(filter);
        controls.add(Box.createVerticalStrut(12));
        controls.add(reportStats);
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(controls, BorderLayout.NORTH);
        body.add(tabs, BorderLayout.CENTER);
        page.add(body, BorderLayout.CENTER);
        return page;
    }

    private JPanel reportFilterField(String title, JComponent input) {
        JPanel field = new JPanel(new BorderLayout(0, 5));
        field.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setForeground(NAVY);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        field.add(label, BorderLayout.NORTH);
        field.add(input, BorderLayout.CENTER);
        return field;
    }

    private JPanel page(String title, String subtitle) {
        return page(title, subtitle, -1);
    }

    private JPanel page(String title, String subtitle, int iconType) {
        JPanel panel = createContentBackgroundPage(0, 18);
        panel.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        JPanel heading = new JPanel(new BorderLayout(12, 0));
        heading.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 23));
        titleLabel.setForeground(NAVY);
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(102, 116, 134));
        JPanel titleText = new JPanel(new BorderLayout(0, 4));
        titleText.setOpaque(false);
        titleText.add(titleLabel, BorderLayout.NORTH);
        titleText.add(subtitleLabel, BorderLayout.CENTER);
        if (iconType >= 0) {
            JPanel icon = createGradientTitleIcon(iconType);
            JPanel accent = new JPanel();
            accent.setBackground(GOLD);
            accent.setPreferredSize(new Dimension(3, 40));
            JPanel titleGroup = new JPanel(new BorderLayout(12, 0));
            titleGroup.setOpaque(false);
            titleGroup.add(accent, BorderLayout.WEST);
            titleGroup.add(titleText, BorderLayout.CENTER);
            heading.add(icon, BorderLayout.WEST);
            heading.add(titleGroup, BorderLayout.CENTER);
        } else {
            heading.add(titleText, BorderLayout.CENTER);
        }
        panel.add(heading, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createGradientTitleIcon(int iconType) {
        javax.swing.Icon glyph = new PenjurusanIcon(
                iconType, Color.WHITE, PAGE_HEADER_GLYPH_SIZE);
        JPanel icon = new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int diameter = Math.min(PAGE_HEADER_ICON_SIZE,
                        Math.min(getWidth(), getHeight()));
                if (diameter > 0) {
                    int circleX = (getWidth() - diameter) / 2;
                    int circleY = (getHeight() - diameter) / 2;
                    g.setPaint(new java.awt.GradientPaint(circleX, circleY,
                            new Color(41, 143, 226), circleX + diameter, circleY + diameter,
                            new Color(8, 41, 83)));
                    g.fillOval(circleX, circleY, diameter, diameter);
                    glyph.paintIcon(this, g,
                            circleX + (diameter - glyph.getIconWidth()) / 2,
                            circleY + (diameter - glyph.getIconHeight()) / 2);
                }
                g.dispose();
            }
        };
        icon.setOpaque(false);
        icon.setPreferredSize(new Dimension(PAGE_HEADER_ICON_SIZE, PAGE_HEADER_ICON_SIZE));
        return icon;
    }

    private JPanel createContentBackgroundPage(int horizontalGap, int verticalGap) {
        JPanel panel = new JPanel(new BorderLayout(horizontalGap, verticalGap)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setPaint(new java.awt.GradientPaint(0, 0, new Color(249, 251, 255),
                        getWidth(), getHeight(), new Color(232, 241, 252)));
                g.fillRect(0, 0, getWidth(), getHeight());
                if (dashboardBuildingImage != null && getWidth() > 0 && getHeight() > 0) {
                    double scale = Math.min((double) (getWidth() * 0.42)
                                    / dashboardBuildingImage.getWidth(),
                            (double) (getHeight() * 0.58)
                                    / dashboardBuildingImage.getHeight());
                    int imageWidth = (int) Math.round(dashboardBuildingImage.getWidth() * scale);
                    int imageHeight = (int) Math.round(dashboardBuildingImage.getHeight() * scale);
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
                    g.drawImage(dashboardBuildingImage,
                            getWidth() - imageWidth - 16, getHeight() - imageHeight - 12,
                            imageWidth, imageHeight, null);
                }
                g.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    private JPanel chartCard(String title, GrafikBatang chart) {
        JPanel panel = card();
        panel.setLayout(new BorderLayout(0, 12));
        panel.add(sectionTitle(title), BorderLayout.NORTH);
        panel.add(chart, BorderLayout.CENTER);
        return panel;
    }

    private JPanel card() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Color.WHITE);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g.setColor(new Color(222, 230, 239));
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        return panel;
    }

    private JPanel roundedCard(Color fill) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(new Color(221, 225, 229));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        return panel;
    }

    private static BufferedImage loadAsset(String fileName) {
        File file = new File("asset" + File.separator + fileName);
        if (!file.isFile()) {
            System.err.println("Aset gambar tidak ditemukan: " + file.getPath());
            return null;
        }
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                System.err.println("Format gambar tidak dapat dibaca: " + file.getPath());
            }
            return image;
        } catch (IOException e) {
            System.err.println("Gagal membaca gambar " + file.getPath() + ": " + e.getMessage());
            return null;
        }
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 15));
        label.setForeground(NAVY);
        return label;
    }

    private JButton button(String text, Color background) {
        JButton button = new JButton(text);
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setUI(new BasicButtonUI());
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setRolloverEnabled(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.darker()),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return button;
    }

    private int addField(JPanel panel, int row, String label, java.awt.Component component) {
        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0; left.gridy = row; left.anchor = GridBagConstraints.NORTHWEST;
        left.insets = new Insets(5, 3, 5, 12);
        left.fill = GridBagConstraints.HORIZONTAL;
        left.weightx = 0.25;
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setForeground(TEXT);
        panel.add(fieldLabel, left);
        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1; right.gridy = row; right.fill = GridBagConstraints.HORIZONTAL;
        right.anchor = GridBagConstraints.NORTHWEST;
        right.insets = new Insets(4, 3, 4, 3);
        right.weightx = 0.75;
        if (component instanceof JScrollPane) ((JScrollPane) component).setPreferredSize(new Dimension(220, 68));
        panel.add(component, right);
        return row + 1;
    }

    private void addAction(JPanel panel, JButton button, int x) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x; c.gridy = 0; c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1; c.insets = new Insets(4, 3, 4, 3);
        panel.add(button, c);
    }

    private static JComboBox<String> semesterCombo() {
        return new JComboBox<>(new String[]{"1", "2", "3", "4", "5", "6", "7", "8"});
    }

    private static JComboBox<String> minatCombo() {
        JComboBox<String> combo = new JComboBox<>(
                RekomendasiService.BIDANG.toArray(new String[0]));
        combo.setEditable(false);
        return combo;
    }

    private static String nilaiCombo(JComboBox<String> combo) {
        Object value = combo.isEditable() ? combo.getEditor().getItem() : combo.getSelectedItem();
        return value == null ? "" : value.toString().trim();
    }

    private static String[] bidangFilterOptions() {
        List<String> options = new ArrayList<>();
        options.add("Semua bidang");
        options.addAll(RekomendasiService.BIDANG);
        return options.toArray(new String[0]);
    }

    private BufferedImage scaleToFit(BufferedImage source, int width, int height) {
        double scale = Math.min((double) width / source.getWidth(),
                (double) height / source.getHeight());
        int scaledWidth = (int) Math.round(source.getWidth() * scale);
        int scaledHeight = (int) Math.round(source.getHeight() * scale);
        int x = (width - scaledWidth) / 2;
        int y = (height - scaledHeight) / 2;

        BufferedImage scaled = source;
        int currentWidth = source.getWidth();
        int currentHeight = source.getHeight();
        while (currentWidth / 2 >= scaledWidth && currentHeight / 2 >= scaledHeight) {
            currentWidth = Math.max(scaledWidth, currentWidth / 2);
            currentHeight = Math.max(scaledHeight, currentHeight / 2);
            scaled = resizeImage(scaled, currentWidth, currentHeight);
        }
        if (currentWidth != scaledWidth || currentHeight != scaledHeight) {
            scaled = resizeImage(scaled, scaledWidth, scaledHeight);
        }

        BufferedImage fitted = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = fitted.createGraphics();
        graphics.drawImage(scaled, x, y, null);
        graphics.dispose();
        return fitted;
    }

    private BufferedImage resizeImage(BufferedImage source, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resized.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        return resized;
    }

    private static JTextArea textArea(int rows) {
        JTextArea area = new JTextArea(rows, 20);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("SansSerif", Font.PLAIN, 13));
        area.setForeground(NAVY);
        area.setBackground(Color.WHITE);
        area.setCaretColor(NAVY);
        area.setSelectionColor(LIGHT_BLUE);
        area.setSelectedTextColor(NAVY);
        area.setMargin(new Insets(6, 8, 6, 8));
        return area;
    }

    private static JLabel fotoPreviewLabel() {
        JLabel label = new JLabel("Foto belum tersedia", SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setForeground(new Color(102, 116, 134));
        label.setBorder(BorderFactory.createLineBorder(new Color(210, 220, 230)));
        return label;
    }

    private void tampilkanFoto(JLabel target, Mahasiswa mahasiswa) {
        tampilkanFotoPath(target, mahasiswa == null ? "" : mahasiswa.getFotoPath());
    }

    private void tampilkanFotoPath(JLabel target, String path) {
        target.setIcon(null);
        target.setText("Foto belum tersedia");
        if (path == null || path.trim().isEmpty()) return;
        try {
            BufferedImage image = ImageIO.read(new File(path));
            if (image == null) {
                target.setText("Berkas foto tidak valid");
                return;
            }
            Dimension preferred = target.getPreferredSize();
            int width = Math.max(50, preferred.width - 12);
            int height = Math.max(50, preferred.height - 12);
            target.setText("");
            target.setIcon(new javax.swing.ImageIcon(image.getScaledInstance(
                    width, height, java.awt.Image.SCALE_SMOOTH)));
        } catch (IOException | IllegalArgumentException ex) {
            target.setText("Foto tidak dapat dibaca");
        }
    }

    private static DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private JScrollPane tableScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setWheelScrollingEnabled(true);
        return scrollPane;
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                component.setSize(getColumnModel().getColumn(column).getWidth(), 1000);
                int height = Math.max(36, component.getPreferredSize().height + 6);
                if (getRowHeight(row) != height) setRowHeight(row, height);
                return component;
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getRowCount() == 0) {
                    g.setColor(new Color(83, 98, 116));
                    g.setFont(new Font("SansSerif", Font.PLAIN, 13));
                    String message = "Belum ada data";
                    int x = Math.max(8, (getWidth() - g.getFontMetrics().stringWidth(message)) / 2);
                    int y = Math.max(24, getHeight() / 2);
                    g.drawString(message, x, y);
                }
            }
        };
        table.setRowHeight(36);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setBackground(Color.WHITE);
        table.setForeground(TEXT);
        table.setSelectionBackground(LIGHT_BLUE);
        table.setSelectionForeground(NAVY);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);
        table.setShowGrid(true);
        table.setGridColor(new Color(226, 232, 239));
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setRowMargin(1);
        table.setDefaultRenderer(Object.class, new WrappingTableCellRenderer());
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setOpaque(true);
        table.getTableHeader().setBackground(LIGHT_BLUE);
        table.getTableHeader().setForeground(NAVY);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 42));
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value,
                    boolean selected, boolean focused, int row, int column) {
                super.getTableCellRendererComponent(source, value, selected, focused, row, column);
                setFont(source.getTableHeader().getFont());
                setForeground(NAVY);
                setBackground(LIGHT_BLUE);
                setHorizontalAlignment(SwingConstants.LEFT);
                setOpaque(true);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(204, 218, 232)),
                        BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                setToolTipText(value == null ? null : value.toString());
                return this;
            }
        });
        aturLebarKolom(table);
        return table;
    }

    private void aturLebarKolom(JTable table) {
        for (int column = 0; column < table.getColumnModel().getColumnCount(); column++) {
            String title = table.getColumnName(column).toLowerCase();
            int width = 145;
            if (title.contains("nim")) width = 135;
            else if (title.contains("nama")) width = 175;
            else if (title.contains("program studi")) width = 190;
            else if (title.contains("semester")) width = 88;
            else if (title.contains("angkatan")) width = 95;
            else if (title.contains("jenis kelamin")) width = 125;
            else if (title.contains("sumber")) width = 150;
            else if (title.contains("tanggal")) width = 115;
            else if (title.contains("kode")) width = 105;
            else if (title.contains("mata kuliah")) width = 280;
            else if (title.contains("sks")) width = 65;
            else if (title.equals("nilai") || title.contains("skor")) width = 85;
            else if (title.contains("status") || title.contains("kelayakan")) width = 145;
            else if (title.contains("asal sekolah")) width = 135;
            else if (title.contains("jurusan")) width = 180;
            else if (title.contains("pengalaman")) width = 280;
            else if (title.contains("alasan")) width = 300;
            else if (title.contains("proyek")) width = 280;
            else if (title.contains("minat")) width = 210;
            else if (title.contains("bidang")) width = 210;
            else if (title.contains("skor")) width = 130;
            else if (title.contains("peringkat")) width = 105;
            else if (title.contains("faktor") || title.contains("catatan")) width = 330;
            table.getColumnModel().getColumn(column).setPreferredWidth(width);
        }
    }

    private void terapkanTemaKomponen(Component component) {
        if (component instanceof JLabel) {
            JLabel label = (JLabel) component;
            if (label.getFont().getSize() < 12) {
                label.setFont(label.getFont().deriveFont(12f));
            }
        } else if (component instanceof JTextField) {
            JTextField field = (JTextField) component;
            field.setForeground(NAVY);
            field.setBackground(Color.WHITE);
            field.setCaretColor(NAVY);
            field.setSelectionColor(LIGHT_BLUE);
            field.setSelectedTextColor(NAVY);
            field.setDisabledTextColor(new Color(70, 82, 96));
            field.setFont(new Font("SansSerif", Font.PLAIN, 13));
            field.setMargin(new Insets(5, 8, 5, 8));
            aturTinggi(component, 34);
        } else if (component instanceof JTextArea) {
            JTextArea area = (JTextArea) component;
            area.setForeground(NAVY);
            area.setBackground(Color.WHITE);
            area.setCaretColor(NAVY);
            area.setSelectionColor(LIGHT_BLUE);
            area.setSelectedTextColor(NAVY);
            area.setDisabledTextColor(new Color(70, 82, 96));
            area.setFont(new Font("SansSerif", Font.PLAIN, 13));
            area.setMargin(new Insets(6, 8, 6, 8));
        } else if (component instanceof JComboBox<?>) {
            aturRendererCombo((JComboBox<?>) component);
            JComboBox<?> combo = (JComboBox<?>) component;
            combo.setForeground(NAVY);
            combo.setBackground(Color.WHITE);
            combo.setFont(new Font("SansSerif", Font.PLAIN, 13));
            combo.setOpaque(true);
            aturTinggi(component, 34);
        } else if (component instanceof JTabbedPane) {
            JTabbedPane tabs = (JTabbedPane) component;
            tabs.setForeground(NAVY);
            tabs.setBackground(LIGHT_BLUE);
            tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
            tabs.setOpaque(true);
        } else if (component instanceof JScrollPane) {
            JScrollPane scroll = (JScrollPane) component;
            scroll.getViewport().setBackground(Color.WHITE);
            scroll.setBackground(Color.WHITE);
            scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 228, 237)));
        }
        if (component instanceof java.awt.Container) {
            for (Component child : ((java.awt.Container) component).getComponents()) {
                terapkanTemaKomponen(child);
            }
        }
    }

    private void aturTinggi(Component component, int height) {
        Dimension preferred = component.getPreferredSize();
        component.setPreferredSize(new Dimension(preferred.width, height));
    }

    private <T> void aturRendererCombo(JComboBox<T> combo) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list,
                    Object value, int index, boolean selected, boolean focused) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, selected, focused);
                label.setForeground(selected ? Color.WHITE : NAVY);
                label.setBackground(selected ? BLUE : Color.WHITE);
                label.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
                return label;
            }
        });
    }

    private DocumentListener documentListener(Runnable action) {
        return new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { action.run(); }
            @Override public void removeUpdate(DocumentEvent e) { action.run(); }
            @Override public void changedUpdate(DocumentEvent e) { action.run(); }
        };
    }

    private void tampilkanHalaman(String nama) {
        if (!isAdminMode() && !Arrays.asList("Dashboard", "Profil Saya",
                "Mata Kuliah & Nilai", "Input Nilai", "Pilih Bidang Peminatan",
                "Rekomendasi Peminatan", "Riwayat Penjurusan").contains(nama)) {
            throw new IllegalArgumentException("Menu tidak tersedia untuk akun Mahasiswa.");
        }
        cardLayout.show(content, nama);
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            boolean active = entry.getKey().equals(nama);
            JButton button = entry.getValue();
            button.setBackground(active ? new Color(31, 62, 101) : NAVY);
            button.setForeground(active ? Color.WHITE : new Color(220, 230, 242));
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, active ? GOLD : NAVY),
                    BorderFactory.createEmptyBorder(12, 12, 12, 8)));
            if (button.getIcon() instanceof MenuIcon) {
                ((MenuIcon) button.getIcon()).setColor(active ? GOLD : new Color(174, 193, 214));
            }
        }
        if (!isAdminMode()) refreshHalamanMahasiswa(nama);
    }

    private void pilihFoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Gambar (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                if (javax.imageio.ImageIO.read(file) == null) {
                    showWarning("Berkas yang dipilih bukan gambar yang dapat dibaca.");
                    return;
                }
                fotoPathLabel.setText(file.getAbsolutePath());
                tampilkanFotoPath(mahasiswaFotoPreview, file.getAbsolutePath());
            } catch (IOException | IllegalArgumentException ex) {
                showError("Foto tidak dapat dibaca: " + ex.getMessage());
            }
        }
    }

    private Mahasiswa bacaFormMahasiswa() {
        String nim = nimField.getText().trim();
        String nama = namaField.getText().trim();
        if (nim.isEmpty() || nama.isEmpty()) throw new IllegalArgumentException("NIM dan nama lengkap wajib diisi.");
        if (prodiField.getSelectedItem() == null) throw new IllegalArgumentException("Program studi wajib dipilih.");
        int semester;
        try {
            semester = Integer.parseInt((String) semesterField.getSelectedItem());
        } catch (NumberFormatException | NullPointerException ex) {
            throw new IllegalArgumentException("Pilih semester antara 1 sampai 8.");
        }
        if (semester < 1 || semester > 8) throw new IllegalArgumentException("Semester harus berada pada rentang 1–8.");
        int angkatan;
        try {
            angkatan = Integer.parseInt(angkatanField.getText().trim());
            if (angkatan < 1900 || angkatan > 2200) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Tahun angkatan wajib diisi dengan tahun yang valid.");
        }
        String gender = genderField.getSelectedIndex() <= 0 ? "" : (String) genderField.getSelectedItem();
        String foto = fotoPathLabel.getText().equals("Belum memilih foto") ? "" : fotoPathLabel.getText();
        Mahasiswa existing = dataService.cariMahasiswa(nim);
        String minat = nilaiCombo(minatField);
        if (minat.isEmpty() && existing != null) minat = existing.getMinatAwal();
        return new Mahasiswa(nim, nama, gender, (String) prodiField.getSelectedItem(), semester,
                angkatan, existing == null ? "" : existing.getAsalSekolah(),
                existing == null ? "" : existing.getJurusanAsal(),
                existing == null ? "" : existing.getPengalamanTeknologi(), minat,
                existing == null ? "" : existing.getAlasanMinat(),
                existing == null ? "" : existing.getProyek(), foto);
    }

    private void wajibDiisi(String value, String namaKolom) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(namaKolom + " wajib diisi.");
        }
    }

    private Mahasiswa denganFotoTersimpan(Mahasiswa data) throws IOException {
        String fotoPersisten = dataService.simpanFotoMahasiswa(data.getNim(), data.getFotoPath());
        data.perbarui(data.getNama(), data.getJenisKelamin(), data.getProgramStudi(),
                data.getSemester(), data.getTahunAngkatan(), data.getAsalSekolah(),
                data.getJurusanAsal(), data.getPengalamanTeknologi(), data.getMinatAwal(),
                data.getAlasanMinat(), data.getProyek(), fotoPersisten);
        return data;
    }

    private void simpanMahasiswa() {
        try {
            Mahasiswa dataBaru = bacaFormMahasiswa();
            if (dataService.cariMahasiswa(dataBaru.getNim()) != null) {
                throw new IllegalArgumentException("NIM sudah terdaftar.");
            }
            dataService.tambahMahasiswa(denganFotoTersimpan(dataBaru));
            JOptionPane.showMessageDialog(this, "Data mahasiswa berhasil disimpan.");
            resetFormMahasiswa();
            refreshSetelahPerubahan();
        } catch (IllegalArgumentException ex) {
            showWarning(ex.getMessage());
        } catch (IOException ex) {
            showError("Data mahasiswa tidak dapat disimpan: " + ex.getMessage());
        }
    }

    private void ubahMahasiswa() {
        try {
            dataService.ubahMahasiswa(denganFotoTersimpan(bacaFormMahasiswa()));
            JOptionPane.showMessageDialog(this, "Data mahasiswa berhasil diperbarui.");
            resetFormMahasiswa();
            refreshSetelahPerubahan();
        } catch (IllegalArgumentException ex) {
            showWarning(ex.getMessage());
        } catch (IOException ex) {
            showError("Perubahan tidak dapat disimpan: " + ex.getMessage());
        }
    }

    private void hapusMahasiswa() {
        String nim = nimField.getText().trim();
        if (nim.isEmpty() || dataService.cariMahasiswa(nim) == null) {
            showWarning("Pilih mahasiswa yang akan dihapus dari tabel.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Hapus data mahasiswa " + nim + " beserta riwayat dan rekomendasinya?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;
        try {
            dataService.hapusMahasiswa(nim);
            JOptionPane.showMessageDialog(this, "Data mahasiswa dan data terkait berhasil dihapus.");
            resetFormMahasiswa();
            refreshSetelahPerubahan();
        } catch (IOException ex) {
            showError("Data tidak dapat dihapus: " + ex.getMessage());
        }
    }

    private void resetFormMahasiswa() {
        nimField.setText(""); nimField.setEditable(true);
        namaField.setText(""); genderField.setSelectedIndex(0);
        prodiField.setSelectedIndex(0); semesterField.setSelectedIndex(0);
        angkatanField.setText(""); sekolahField.setSelectedIndex(0);
        jurusanField.setText(""); pengalamanField.setText("");
        minatField.setSelectedIndex(-1); alasanField.setText(""); proyekField.setText("");
        fotoPathLabel.setText("Belum memilih foto");
        mahasiswaTable.clearSelection();
        aturFormMahasiswa(true, true, false);
        tampilkanFoto(mahasiswaFotoPreview, null);
    }

    private void isiFormMahasiswa() {
        int row = mahasiswaTable.convertRowIndexToModel(mahasiswaTable.getSelectedRow());
        String nim = mahasiswaModel.getValueAt(row, 1).toString();
        Mahasiswa m = dataService.cariMahasiswa(nim);
        if (m == null) return;
        nimField.setText(m.getNim()); nimField.setEditable(false);
        namaField.setText(m.getNama());
        genderField.setSelectedItem(m.getJenisKelamin().isEmpty() ? "Pilih jenis kelamin" : m.getJenisKelamin());
        prodiField.setSelectedItem(m.getProgramStudi());
        semesterField.setSelectedItem(String.valueOf(m.getSemester()));
        angkatanField.setText(m.getTahunAngkatan() == 0 ? "" : String.valueOf(m.getTahunAngkatan()));
        sekolahField.setSelectedItem(m.getAsalSekolah());
        jurusanField.setText(m.getJurusanAsal());
        pengalamanField.setText(m.getPengalamanTeknologi());
        minatField.setSelectedItem(RekomendasiService.normalisasiBidang(m.getMinatAwal()));
        alasanField.setText(m.getAlasanMinat());
        proyekField.setText(m.getProyek());
        fotoPathLabel.setText(m.getFotoPath().isEmpty() ? "Belum memilih foto" : m.getFotoPath());
        boolean simulasi = dataService.isMahasiswaSimulasi(m.getNim());
        aturFormMahasiswa(!simulasi, false, !simulasi);
        tampilkanFoto(mahasiswaFotoPreview, m);
    }

    private void aturFormMahasiswa(boolean editable, boolean canSave, boolean canEdit) {
        for (JComponent component : komponenFormMahasiswa) component.setEnabled(editable);
        simpanMahasiswaButton.setEnabled(canSave);
        ubahMahasiswaButton.setEnabled(canEdit);
        hapusMahasiswaButton.setEnabled(canEdit);
        resetMahasiswaButton.setEnabled(editable);
        mahasiswaFormStatusLabel.setText(canEdit ? "Data asli — dapat diubah."
                : editable ? "Formulir siap untuk menambahkan data asli."
                : "Data tidak tersedia untuk diedit.");
    }

    private void refreshTabelMahasiswa() {
        clearModel(mahasiswaModel);
        List<Mahasiswa> filtered = mahasiswaTersaring();
        int pageCount = Math.max(1, (filtered.size() + MAHASISWA_PAGE_SIZE - 1) / MAHASISWA_PAGE_SIZE);
        currentMahasiswaPage = Math.min(currentMahasiswaPage, pageCount - 1);
        int start = currentMahasiswaPage * MAHASISWA_PAGE_SIZE;
        int end = Math.min(start + MAHASISWA_PAGE_SIZE, filtered.size());
        for (int i = start; i < end; i++) {
            Mahasiswa m = filtered.get(i);
            mahasiswaModel.addRow(new Object[]{i + 1, m.getNim(), m.getNama(),
                m.getProgramStudi(), m.getSemester(), ""});
        }
        mahasiswaCountLabel.setText(filtered.isEmpty() ? "Menampilkan 0 dari 0 data"
                : "Menampilkan " + (start + 1) + " - " + end + " dari " + filtered.size() + " data");
        mahasiswaPageLabel.setText((currentMahasiswaPage + 1) + " / " + pageCount);
        mahasiswaFirstPageButton.setEnabled(currentMahasiswaPage > 0);
        mahasiswaPreviousPageButton.setEnabled(currentMahasiswaPage > 0);
        mahasiswaNextPageButton.setEnabled(currentMahasiswaPage < pageCount - 1);
        mahasiswaLastPageButton.setEnabled(currentMahasiswaPage < pageCount - 1);
    }

    private List<Mahasiswa> mahasiswaTersaring() {
        String filter = cariMahasiswaField.getText();
        String semester = (String) mahasiswaFilterSemester.getSelectedItem();
        String prodi = (String) mahasiswaFilterProdi.getSelectedItem();
        List<Mahasiswa> filtered = new ArrayList<>();
        for (Mahasiswa mahasiswa : dataService.semuaMahasiswa()) {
            boolean cocokProdi = prodi == null || prodi.trim().isEmpty()
                    || "Semua Program Studi".equalsIgnoreCase(prodi.trim())
                    || mahasiswa.getProgramStudi().equalsIgnoreCase(prodi.trim());
            if (cocokProdi && cocokFilterMahasiswa(mahasiswa, filter, semester)) {
                filtered.add(mahasiswa);
            }
        }
        return filtered;
    }

    private int jumlahMahasiswaTersaring() {
        return mahasiswaTersaring().size();
    }

    private void jalankanAksiMahasiswa(int modelRow, int action) {
        if (modelRow < 0 || modelRow >= mahasiswaModel.getRowCount()) return;
        int viewRow = mahasiswaTable.convertRowIndexToView(modelRow);
        if (viewRow < 0) return;
        mahasiswaTable.setRowSelectionInterval(viewRow, viewRow);
        if (action == 0) {
            bukaFormMahasiswa(true);
        } else if (action == 1) {
            bukaFormMahasiswa(false);
        } else {
            hapusMahasiswa();
        }
    }

    static boolean cocokFilterMahasiswa(Mahasiswa mahasiswa, String pencarian, String semesterFilter) {
        String query = pencarian == null ? "" : pencarian.trim().toLowerCase(java.util.Locale.ROOT);
        boolean cocokSemester = semesterFilter == null || semesterFilter.trim().isEmpty()
                || "semua semester".equalsIgnoreCase(semesterFilter.trim())
                || String.valueOf(mahasiswa.getSemester()).equals(semesterFilter.trim());
        return cocokSemester && (query.isEmpty()
                || mahasiswa.getNim().toLowerCase(java.util.Locale.ROOT).contains(query)
                || mahasiswa.getNama().toLowerCase(java.util.Locale.ROOT).contains(query));
    }

    private void isiLatarBelakang() {
        Mahasiswa m = dataService.cariMahasiswa((String) latarNimField.getSelectedItem());
        if (m == null) {
            latarNamaLabel.setText("Pilih mahasiswa");
            latarJurusanField.setText(""); latarPengalamanField.setText("");
            latarProyekField.setText(""); latarMinatField.setSelectedItem(""); latarAlasanField.setText("");
            tampilkanFoto(latarFotoPreview, null);
            if (latarSimpanButton != null) latarSimpanButton.setEnabled(false);
            return;
        }
        latarNamaLabel.setText(m.getNama());
        latarSekolahField.setSelectedItem(m.getAsalSekolah());
        latarJurusanField.setText(m.getJurusanAsal());
        latarPengalamanField.setText(m.getPengalamanTeknologi());
        latarProyekField.setText(m.getProyek());
        latarMinatField.setSelectedItem(RekomendasiService.normalisasiBidang(m.getMinatAwal()));
        latarAlasanField.setText(m.getAlasanMinat());
        tampilkanFoto(latarFotoPreview, m);
        latarSimpanButton.setEnabled(!dataService.isMahasiswaSimulasi(m.getNim()));
    }

    private void refreshLatarBelakangTabel() {
        refreshNilaiTabel();
    }

    private void refreshNilaiTabel() {
        clearModel(nilaiMataKuliahModel);
        String search = cariNilaiField.getText().trim().toLowerCase();
        String semesterFilter = (String) filterNilaiSemesterField.getSelectedItem();
        Map<String, MataKuliah> coursesByCode = new HashMap<>();
        for (MataKuliah course : dataService.semuaMataKuliah()) {
            coursesByCode.put(course.getKode(), course);
        }
        int rowNumber = 0;
        for (NilaiMataKuliah grade : dataService.semuaNilaiMataKuliah()) {
            Mahasiswa student = dataService.cariMahasiswa(grade.getNim());
            if (student == null) continue;
            if (semesterFilter != null && !semesterFilter.equals("Semua Semester")
                    && !semesterFilter.equals(String.valueOf(grade.getSemester()))) continue;
            if (!search.isEmpty()
                    && !(student.getNim() + " " + student.getNama()).toLowerCase().contains(search)) continue;
            MataKuliah course = coursesByCode.get(grade.getKode());
            String courseName = course == null ? grade.getNama() : course.getNama();
            if (course != null && course.isUsulan()) courseName += " [USULAN]";
            nilaiMataKuliahModel.addRow(new Object[]{++rowNumber, student.getNim(), student.getNama(),
                grade.getSemester(), courseName, grade.getNilaiHuruf(), "", grade.getKode(),
                grade.isTerverifikasi() ? "Terverifikasi" : "Menunggu Admin"});
        }
        nilaiCountLabel.setText("Menampilkan " + nilaiMataKuliahModel.getRowCount() + " nilai");
        refreshNilaiMetrics();
    }

    private void isiFormNilai() {
        int selectedRow = nilaiMataKuliahTable.getSelectedRow();
        if (selectedRow < 0) return;
        int row = nilaiMataKuliahTable.convertRowIndexToModel(selectedRow);
        String nim = nilaiMataKuliahModel.getValueAt(row, 1).toString();
        String kode = nilaiMataKuliahModel.getValueAt(row, 7).toString();
        isiFormNilai(nim, kode);
    }

    private void isiFormNilai(String nim, String kode) {
        MataKuliah course = dataService.cariMataKuliah(kode);
        if (course == null) return;
        kodeNilaiNimField.setSelectedItem(nim);
        kurikulumField.setSelectedItem(course);
        NilaiMataKuliah nilai = dataService.nilaiMahasiswa(nim, kode);
        boolean asli = !dataService.isMahasiswaSimulasi(nim);
        boolean semesterBerjalan = course.getSemester() <= dataService.cariMahasiswa(nim).getSemester();
        nilaiNimSedangDiedit = nilai == null ? null : nim;
        kodeNilaiSedangDiedit = nilai == null ? null : kode;
        semesterNilaiSedangDiedit = nilai == null ? -1 : course.getSemester();
        nilaiHurufField.setSelectedItem(nilai == null ? null : nilai.getNilaiHuruf());
        nilaiTambahButton.setEnabled(asli && semesterBerjalan && nilai == null);
        nilaiUbahButton.setEnabled(asli && nilai != null);
        nilaiHapusButton.setEnabled(asli && nilai != null);
        nilaiVerifikasiButton.setEnabled(nilai != null && !nilai.isTerverifikasi());
        kodeNilaiNimField.setEnabled(asli);
        kurikulumField.setEnabled(asli);
        nilaiHurufField.setEnabled(asli && semesterBerjalan);
        nilaiStatusLabel.setText(nilai == null ? "Belum Dinilai"
                : nilai.getNilaiHuruf() + " — " + nilai.getStatusKelulusan()
                        + (nilai.isTerverifikasi() ? " — Terverifikasi" : " — Menunggu Admin"));
    }

    private void refreshNilaiMetrics() {
        List<Mahasiswa> students = dataService.semuaMahasiswa();
        List<MataKuliah> courses = dataService.semuaMataKuliah();
        List<NilaiMataKuliah> grades = dataService.semuaNilaiMataKuliah();
        int totalPossible = 0;
        for (Mahasiswa student : students) {
            for (MataKuliah course : courses) {
                if (course.getSemester() <= student.getSemester()) totalPossible++;
            }
        }
        double weightedPoints = 0;
        int weightedCredits = 0;
        for (NilaiMataKuliah grade : grades) {
            if (!grade.isTerverifikasi()) continue;
            int points;
            switch (grade.getNilaiHuruf()) {
                case "A": points = 4; break;
                case "B": points = 3; break;
                case "C": points = 2; break;
                case "D": points = 1; break;
                case "E": points = 0; break;
                default: continue;
            }
            weightedPoints += points * grade.getSks();
            weightedCredits += grade.getSks();
        }
        nilaiTotalMahasiswaValue.setText(String.valueOf(students.size()));
        nilaiTotalMataKuliahValue.setText(String.valueOf(courses.size()));
        nilaiTerinputValue.setText(totalPossible == 0 ? "0%"
                : Math.round((double) grades.size() * 100 / totalPossible) + "%");
        nilaiRataRataValue.setText(weightedCredits == 0 ? "—"
                : String.format(java.util.Locale.ROOT, "%.2f", weightedPoints / weightedCredits));
    }

    private void jalankanAksiNilai(int modelRow, int action) {
        String nim = nilaiMataKuliahModel.getValueAt(modelRow, 1).toString();
        String kode = nilaiMataKuliahModel.getValueAt(modelRow, 7).toString();
        isiFormNilai(nim, kode);
        int viewRow = nilaiMataKuliahTable.convertRowIndexToView(modelRow);
        if (viewRow < 0) return;
        nilaiMataKuliahTable.setRowSelectionInterval(viewRow, viewRow);
        if (action == 0) {
            nilaiFormDialog.setTitle("Ubah Nilai Mahasiswa");
            nilaiFormDialog.setLocationRelativeTo(this);
            nilaiFormDialog.setVisible(true);
        } else {
            hapusNilai();
        }
    }

    private NilaiMataKuliah bacaFormNilai() {
        String nim = (String) kodeNilaiNimField.getSelectedItem();
        if (nim == null) throw new IllegalArgumentException("Pilih mahasiswa.");
        MataKuliah course = (MataKuliah) kurikulumField.getSelectedItem();
        if (course == null) throw new IllegalArgumentException("Pilih mata kuliah.");
        return new NilaiMataKuliah(nim, course.getKode(), course.getNama(),
                course.getSemester(), course.getSks(), (String) nilaiHurufField.getSelectedItem());
    }

    private void isiMataKuliahPilihan() {
        MataKuliah course = (MataKuliah) kurikulumField.getSelectedItem();
        if (course == null) return;
        kodeNilaiField.setText(course.getKode());
        namaMataKuliahField.setText(course.getNama());
        semesterMataKuliahField.setSelectedItem(String.valueOf(course.getSemester()));
        sksMataKuliahField.setText(String.valueOf(course.getSks()));
        NilaiMataKuliah grade = dataService.nilaiMahasiswa(
                (String) kodeNilaiNimField.getSelectedItem(), course.getKode());
        nilaiHurufField.setSelectedItem(grade == null ? null : grade.getNilaiHuruf());
        if (grade == null) nilaiStatusLabel.setText("Belum Dinilai");
        else nilaiStatusLabel.setText(grade.getNilaiHuruf() + " — " + grade.getStatusKelulusan());
        if (nilaiNimSedangDiedit == null) {
            Mahasiswa student = dataService.cariMahasiswa((String) kodeNilaiNimField.getSelectedItem());
            nilaiTambahButton.setEnabled(student != null && !dataService.isMahasiswaSimulasi(student.getNim())
                    && course.getSemester() <= student.getSemester() && grade == null);
            nilaiUbahButton.setEnabled(false);
            nilaiHapusButton.setEnabled(false);
        }
    }

    private void tambahNilai() {
        try {
            dataService.simpanNilai(bacaFormNilai());
            JOptionPane.showMessageDialog(this, "Nilai mata kuliah berhasil disimpan.");
            nilaiFormDialog.setVisible(false);
            resetFormNilai();
            refreshSetelahPerubahan();
        } catch (IllegalArgumentException ex) {
            showWarning(ex.getMessage());
        } catch (IOException ex) {
            showError("Nilai mata kuliah tidak dapat disimpan: " + ex.getMessage());
        }
    }

    private void ubahNilai() {
        try {
            dataService.ubahNilai(nilaiNimSedangDiedit, kodeNilaiSedangDiedit,
                    semesterNilaiSedangDiedit, bacaFormNilai());
            JOptionPane.showMessageDialog(this, "Nilai mata kuliah berhasil diperbarui.");
            nilaiFormDialog.setVisible(false);
            resetFormNilai();
            refreshSetelahPerubahan();
        } catch (IllegalArgumentException ex) {
            showWarning(ex.getMessage());
        } catch (IOException ex) {
            showError("Perubahan nilai tidak dapat disimpan: " + ex.getMessage());
        }
    }

    private void hapusNilai() {
        if (nilaiNimSedangDiedit == null || kodeNilaiSedangDiedit == null) {
            showWarning("Pilih nilai mata kuliah yang akan dihapus.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hapus nilai " + kodeNilaiSedangDiedit + "?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            dataService.hapusNilai(nilaiNimSedangDiedit, kodeNilaiSedangDiedit,
                    semesterNilaiSedangDiedit);
            resetFormNilai();
            refreshSetelahPerubahan();
        } catch (IllegalArgumentException ex) {
            showWarning(ex.getMessage());
        } catch (IOException ex) {
            showError("Nilai mata kuliah tidak dapat dihapus: " + ex.getMessage());
        }
    }

    private void verifikasiNilaiTerpilih() {
        if (nilaiNimSedangDiedit == null || kodeNilaiSedangDiedit == null) {
            showWarning("Pilih pengajuan nilai yang akan diverifikasi.");
            return;
        }
        try {
            dataService.verifikasiNilai(sesiAdmin(), nilaiNimSedangDiedit,
                    kodeNilaiSedangDiedit, semesterNilaiSedangDiedit);
            JOptionPane.showMessageDialog(this, "Nilai berhasil diverifikasi.");
            resetFormNilai();
            refreshSetelahPerubahan();
        } catch (IllegalArgumentException ex) {
            showWarning(ex.getMessage());
        } catch (IOException ex) {
            showError("Nilai tidak dapat diverifikasi: " + ex.getMessage());
        }
    }

    private UserSession sesiAdmin() {
        return userSession == null ? new UserSession(
                UserSession.Role.ADMIN, "admin", "", "Administrator") : userSession;
    }

    private void resetFormNilai() {
        nilaiNimSedangDiedit = null;
        kodeNilaiSedangDiedit = null;
        semesterNilaiSedangDiedit = -1;
        kurikulumField.setEnabled(true);
        kodeNilaiNimField.setEnabled(true);
        kurikulumField.setSelectedIndex(0);
        nilaiHurufField.setSelectedIndex(-1);
        nilaiMataKuliahTable.clearSelection();
        nilaiHurufField.setEnabled(true);
        boolean simulasi = dataService.isMahasiswaSimulasi((String) kodeNilaiNimField.getSelectedItem());
        MataKuliah course = (MataKuliah) kurikulumField.getSelectedItem();
        Mahasiswa student = dataService.cariMahasiswa((String) kodeNilaiNimField.getSelectedItem());
        nilaiTambahButton.setEnabled(!simulasi && student != null
                && course.getSemester() <= student.getSemester()
                && dataService.nilaiMahasiswa(student.getNim(), course.getKode()) == null);
        nilaiUbahButton.setEnabled(false);
        nilaiHapusButton.setEnabled(false);
        nilaiVerifikasiButton.setEnabled(false);
        nilaiStatusLabel.setText(simulasi ? "Data tidak tersedia untuk diedit." : "Data asli dapat ditambah.");
        perbaruiStatusNilai();
        refreshNilaiTabel();
    }

    private void perbaruiStatusNilai() {
        String grade = (String) nilaiHurufField.getSelectedItem();
        if (grade == null) {
            nilaiStatusLabel.setText("Belum Dinilai");
            return;
        }
        String kelulusan = "A".equals(grade) || "B".equals(grade) || "C".equals(grade)
                ? "Lulus" : "Tidak Lulus";
        String nim = (String) kodeNilaiNimField.getSelectedItem();
        nilaiStatusLabel.setText(kelulusan + (dataService.isMahasiswaSimulasi(nim)
                ? " — hanya-baca" : " — data asli"));
    }

    private void refreshBidangPeminatan() {
        for (String bidang : RekomendasiService.BIDANG) {
            Set<String> selected = bidangMataKuliahDrafts.get(bidang);
            if (selected == null) {
                BidangPeminatan saved = dataService.bidangPeminatan(bidang);
                selected = saved == null ? new java.util.LinkedHashSet<>()
                        : new java.util.LinkedHashSet<>(saved.getKodeMataKuliahPendukung());
            }
            JLabel count = bidangRingkasanCounts.get(bidang);
            JTextArea list = bidangRingkasanTexts.get(bidang);
            if (count == null || list == null) continue;
            count.setText(selected.size() + " mata kuliah prasyarat");
            if (selected.isEmpty()) {
                list.setText("Belum ada mata kuliah yang dipilih.");
                continue;
            }
            List<String> labels = new ArrayList<>();
            for (String kode : selected) {
                MataKuliah course = dataService.cariMataKuliah(kode);
                labels.add(course == null ? kode : "• " + course.getKode() + " — "
                        + course.getNama() + (course.isUsulan() ? " [USULAN]" : ""));
            }
            list.setText(String.join("\n", labels));
            list.setCaretPosition(0);
        }
    }

    private void isiPersyaratanBidang() {
        String nama = (String) bidangKelolaField.getSelectedItem();
        BidangPeminatan bidang = dataService.bidangPeminatan(nama);
        if (bidang == null) return;
        Set<String> selected = bidangMataKuliahDrafts.get(nama);
        if (selected == null) {
            selected = new java.util.LinkedHashSet<>(bidang.getKodeMataKuliahPendukung());
            bidangMataKuliahDrafts.put(nama, selected);
        }
        for (Map.Entry<String, JCheckBox> entry : bidangMataKuliahChecks.entrySet()) {
            entry.getValue().setSelected(selected.contains(entry.getKey()));
        }
        perbaruiKartuBidangAktif();
        perbaruiJumlahPendukung();
        refreshBidangPeminatan();
    }

    private void pilihBidangPeminatan(String bidang) {
        simpanDraftBidangAktif();
        bidangKelolaField.setSelectedItem(bidang);
        isiPersyaratanBidang();
    }

    private void simpanDraftBidangAktif() {
        String bidang = (String) bidangKelolaField.getSelectedItem();
        if (bidang == null || bidangMataKuliahChecks.isEmpty()) return;
        Set<String> selected = new java.util.LinkedHashSet<>();
        for (Map.Entry<String, JCheckBox> entry : bidangMataKuliahChecks.entrySet()) {
            if (entry.getValue().isSelected()) selected.add(entry.getKey());
        }
        bidangMataKuliahDrafts.put(bidang, selected);
    }

    private void perbaruiKartuBidangAktif() {
        String bidangAktif = (String) bidangKelolaField.getSelectedItem();
        for (Map.Entry<String, JToggleButton> entry : bidangPilihanCards.entrySet()) {
            boolean selected = entry.getKey().equals(bidangAktif);
            JToggleButton card = entry.getValue();
            card.setSelected(selected);
            card.setBackground(selected ? LIGHT_BLUE : Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(selected ? GOLD : new Color(205, 217, 231),
                            selected ? 2 : 1),
                    BorderFactory.createEmptyBorder(11, 13, 11, 13)));
        }
    }

    private void perbaruiJumlahPendukung() {
        int selected = 0;
        for (JCheckBox check : bidangMataKuliahChecks.values()) {
            if (check.isSelected()) selected++;
        }
        bidangKelolaStatus.setText(selected + " mata kuliah dipilih.");
        simpanDraftBidangAktif();
        refreshBidangPeminatan();
    }

    private void simpanPersyaratanBidang() {
        String bidang = (String) bidangKelolaField.getSelectedItem();
        List<String> kode = new ArrayList<>();
        for (Map.Entry<String, JCheckBox> entry : bidangMataKuliahChecks.entrySet()) {
            if (entry.getValue().isSelected()) kode.add(entry.getKey());
        }
        if (kode.isEmpty()) {
            showWarning("Pilih sekurang-kurangnya satu mata kuliah pendukung.");
            return;
        }
        try {
            bidangMataKuliahDrafts.put(bidang, new java.util.LinkedHashSet<>(kode));
            dataService.simpanBidangPeminatan(userSession,
                    new BidangPeminatan(bidang, kode, false));
            refreshSetelahPerubahan();
            JOptionPane.showMessageDialog(this, "Pengaturan mata kuliah pendukung berhasil disimpan.");
        } catch (IllegalArgumentException ex) {
            showWarning(ex.getMessage());
        } catch (IOException ex) {
            showError("Persyaratan bidang tidak dapat disimpan: " + ex.getMessage());
        }
    }

    private void stylePenjurusanInput(JComponent input) {
        input.setFont(new Font("SansSerif", Font.PLAIN, 13));
        input.setForeground(NAVY);
        input.setBackground(Color.WHITE);
        input.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 216, 235)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        input.setPreferredSize(new Dimension(0, 38));
        if (input instanceof JComboBox) {
            ((JComboBox<?>) input).setToolTipText("Pilih atau masukkan pilihan");
        }
    }

    private void perbaruiStatusPenjurusan() {
        String nim = (String) penjurusanNimField.getSelectedItem();
        Mahasiswa student = dataService.cariMahasiswa(nim);
        penjurusanNamaLabel.setText(student == null ? "—" : student.getNama());
        if (student == null) {
            penjurusanStatusLabel.setText("<html><div style='width:250px'>"
                    + "Pilih mahasiswa untuk melihat hasil pemeriksaan akademik</div></html>");
            penjurusanStatusLabel.setForeground(new Color(102, 116, 134));
            penjurusanSimpanButton.setEnabled(false);
            penjurusanSebelumnyaLabel.setText("Belum ada penjurusan sebelumnya.");
            prasyaratTotalValue.setText("0");
            prasyaratLulusValue.setText("0");
            prasyaratTidakLulusValue.setText("0");
            pemeriksaanPrasyaratModel.setRowCount(0);
            pemeriksaanLayout.show(pemeriksaanContent, "empty");
            rekomendasiPenjurusanArea.setText("Pilih mahasiswa untuk melihat rekomendasi.");
        } else {
            String bidang = (String) penjurusanBidangField.getSelectedItem();
            BidangPeminatan requirement = dataService.bidangPeminatan(bidang);
            List<String> prerequisites = requirement == null
                    ? new ArrayList<>() : requirement.getKodeMataKuliahPendukung();
            Map<String, NilaiMataKuliah> grades = new LinkedHashMap<>();
            for (NilaiMataKuliah grade : dataService.nilaiMahasiswa(nim)) {
                NilaiMataKuliah existing = grades.get(grade.getKode());
                if (existing == null || grade.getSemester() >= existing.getSemester()) {
                    grades.put(grade.getKode(), grade);
                }
            }
            pemeriksaanPrasyaratModel.setRowCount(0);
            int lulus = 0;
            int tidakLulus = 0;
            for (String kode : prerequisites) {
                MataKuliah course = dataService.cariMataKuliah(kode);
                NilaiMataKuliah grade = grades.get(kode);
                String gradeValue = grade == null || grade.getNilaiHuruf().isEmpty()
                        ? "—" : grade.getNilaiHuruf();
                String statusValue = grade == null || grade.getNilaiHuruf().isEmpty()
                        ? "Belum Dinilai" : grade.getStatusKelulusan();
                if ("Lulus".equals(statusValue)) lulus++;
                if ("Tidak Lulus".equals(statusValue)) tidakLulus++;
                pemeriksaanPrasyaratModel.addRow(new Object[]{
                    course == null ? kode : course.getNama(),
                    course == null ? "—" : course.getSemester(),
                    gradeValue,
                    statusValue
                });
            }
            prasyaratTotalValue.setText(String.valueOf(prerequisites.size()));
            prasyaratLulusValue.setText(String.valueOf(lulus));
            prasyaratTidakLulusValue.setText(String.valueOf(tidakLulus));
            pemeriksaanLayout.show(pemeriksaanContent, "results");

            String status = dataService.statusKelayakanPenjurusan(nim, bidang);
            if ("MEMENUHI SYARAT".equals(status)) {
                penjurusanStatusLabel.setText("<html><div style='width:250px'>"
                        + "Memenuhi Syarat Penjurusan</div></html>");
                penjurusanStatusLabel.setForeground(new Color(31, 126, 78));
            } else if ("TIDAK MEMENUHI SYARAT".equals(status)) {
                penjurusanStatusLabel.setText("<html><div style='width:250px'>"
                        + "Tidak Memenuhi Syarat Penjurusan</div></html>");
                penjurusanStatusLabel.setForeground(new Color(180, 62, 62));
            } else {
                penjurusanStatusLabel.setText("<html><div style='width:250px'>"
                        + "Belum Lengkap — lengkapi nilai mata kuliah prasyarat</div></html>");
                penjurusanStatusLabel.setForeground(new Color(163, 111, 21));
            }
            RiwayatPenjurusan previous = dataService.penjurusanMahasiswa(nim);
            if (previous == null) {
                penjurusanSebelumnyaLabel.setText("Belum ada penjurusan sebelumnya.");
            } else {
                penjurusanSebelumnyaLabel.setText("<html>" + escapeHtml(previous.getBidang())
                        + " — " + previous.getTanggal() + "<br>Status: "
                        + (previous.isLayak() ? "Memenuhi syarat" : "Tidak memenuhi syarat")
                        + "</html>");
            }
            penjurusanSimpanButton.setEnabled(!dataService.isMahasiswaSimulasi(nim)
                    && "MEMENUHI SYARAT".equals(status) && previous == null);
            perbaruiRekomendasiPenjurusan(nim, bidang, status);
        }
        refreshStatistikPenjurusan();
    }

    private void perbaruiRekomendasiPenjurusan(String nim, String bidangPilihan, String statusPilihan) {
        rekomendasiPenjurusanArea.setText("");
        Mahasiswa student = dataService.cariMahasiswa(nim);
        if (student == null) return;
        RekomendasiService.HasilRekomendasi analysis = rekomendasiService.analisis(
                student, dataService.nilaiTerverifikasiMahasiswa(nim),
                dataService.semuaBidangPeminatan(), dataService.semuaMataKuliah());
        List<String> alternatives = new ArrayList<>();
        for (RekomendasiService.SkorBidang item : analysis.getSkor()) {
            boolean isChosen = item.getBidang().equals(bidangPilihan);
            if ("MEMENUHI SYARAT".equals(statusPilihan)) {
                if (item.isLayak()) {
                    alternatives.add(item.getBidang() + (isChosen ? " (Pilihan)" : "")
                            + " — rata-rata " + String.format(java.util.Locale.ROOT,
                                    "%.2f", item.getRataRata()) + "/4");
                }
            } else if (!isChosen) {
                alternatives.add(item.getBidang() + " — "
                        + item.getJumlahMataKuliahLulus() + "/"
                        + item.getJumlahMataKuliahPendukung() + " mata kuliah pendukung lulus, "
                        + item.getStatusKelayakan() + ", rata-rata "
                        + String.format(java.util.Locale.ROOT, "%.2f", item.getRataRata()) + "/4");
            }
        }
        if (alternatives.isEmpty()) {
            rekomendasiPenjurusanArea.setText("Belum ada bidang alternatif yang dapat dianalisis.");
        } else if ("MEMENUHI SYARAT".equals(statusPilihan)) {
            rekomendasiPenjurusanArea.setText("Bidang yang memenuhi syarat:\n• "
                    + String.join("\n• ", alternatives));
        } else {
            rekomendasiPenjurusanArea.setText("Rekomendasi bidang alternatif, diurutkan "
                    + "berdasarkan proporsi mata kuliah pendukung yang lulus:\n• "
                    + String.join("\n• ", alternatives));
        }
    }

    private void refreshStatistikPenjurusan() {
        int total = 0;
        for (Mahasiswa mahasiswa : dataService.semuaMahasiswa()) {
            if (mahasiswa.getSemester() == 7) total++;
        }
        penjurusanTotalSemester7.setText(String.valueOf(total));
        penjurusanTotalLayak.setText(String.valueOf(dataService.jumlahMahasiswaLayakPenjurusan()));
        penjurusanTotalBelumLayak.setText(String.valueOf(dataService.jumlahMahasiswaBelumLayakPenjurusan()));
    }

    private void simpanPenjurusan() {
        String nim = (String) penjurusanNimField.getSelectedItem();
        if (nim == null || dataService.cariMahasiswa(nim) == null) {
            showWarning("Pilih NIM mahasiswa semester 7 yang terdaftar.");
            return;
        }
        if (dataService.penjurusanMahasiswa(nim) != null) {
            showWarning("Mahasiswa ini sudah memiliki riwayat penjurusan.");
            perbaruiStatusPenjurusan();
            return;
        }
        LocalDate tanggal;
        try {
            tanggal = LocalDate.parse(penjurusanTanggalField.getText().trim());
            dataService.simpanPenjurusan(nim, (String) penjurusanBidangField.getSelectedItem(), tanggal);
            JOptionPane.showMessageDialog(this, "Riwayat penjurusan semester 7 berhasil dicatat.");
            refreshSetelahPerubahan();
        } catch (DateTimeParseException ex) {
            showWarning("Tanggal harus memakai format YYYY-MM-DD.");
        } catch (IllegalArgumentException ex) {
            showWarning(ex.getMessage());
        } catch (IOException ex) {
            showError("Riwayat penjurusan tidak dapat disimpan: " + ex.getMessage());
        }
    }

    private void refreshRiwayatNilai() {
        perbaruiStatusPenjurusan();
    }

    private void refreshPenjurusanTable() {
        refreshRiwayatPenjurusan();
    }

    private void refreshRiwayatPenjurusan() {
        riwayatPenjurusanModel.setRowCount(0);
        String query = cariRiwayatPenjurusanField.getText().trim().toLowerCase(java.util.Locale.ROOT);
        for (RiwayatPenjurusan placement : dataService.semuaPenjurusan()) {
            Mahasiswa student = dataService.cariMahasiswa(placement.getNim());
            if (student == null) continue;
            String searchable = (student.getNim() + " " + student.getNama())
                    .toLowerCase(java.util.Locale.ROOT);
            if (!searchable.contains(query)) continue;
            riwayatPenjurusanModel.addRow(new Object[]{student.getNim(), student.getNama(),
                placement.getBidang(), placement.getTanggal(),
                placement.isLayak() ? "Memenuhi syarat" : "Tidak memenuhi syarat"});
        }
        if (riwayatPenjurusanModel.getRowCount() == 0) {
            riwayatPenjurusanEmptyLabel.setText(query.isEmpty()
                    ? "Belum ada riwayat penjurusan" : "Tidak ada riwayat yang cocok dengan pencarian.");
            riwayatPenjurusanLayout.show(riwayatPenjurusanContent, "empty");
        } else {
            riwayatPenjurusanLayout.show(riwayatPenjurusanContent, "results");
        }
    }

    private String teksTabel(String value) {
        return value == null || value.trim().isEmpty() ? "Data belum diisi" : value.trim();
    }

    private void simpanLatarBelakang() {
        Mahasiswa m = dataService.cariMahasiswa((String) latarNimField.getSelectedItem());
        if (m == null) { showWarning("Pilih mahasiswa yang sudah terdaftar."); return; }
        m.perbarui(m.getNama(), m.getJenisKelamin(), m.getProgramStudi(), m.getSemester(),
                m.getTahunAngkatan(), (String) latarSekolahField.getSelectedItem(),
                latarJurusanField.getText().trim(), latarPengalamanField.getText().trim(),
                nilaiCombo(latarMinatField), latarAlasanField.getText().trim(),
                latarProyekField.getText().trim(), m.getFotoPath());
        try {
            dataService.ubahMahasiswa(m);
            JOptionPane.showMessageDialog(this, "Latar belakang mahasiswa berhasil disimpan.");
            refreshSetelahPerubahan();
            isiLatarBelakang();
        } catch (IOException ex) {
            showError("Latar belakang tidak dapat disimpan: " + ex.getMessage());
        }
    }

    private void isiNamaRiwayat() {
        Mahasiswa m = dataService.cariMahasiswa((String) riwayatNimField.getSelectedItem());
        riwayatNamaLabel.setText(m == null ? "Pilih mahasiswa" : m.getNama());
    }

    private void simpanRiwayat() {
        String nim = (String) riwayatNimField.getSelectedItem();
        if (nim == null || dataService.cariMahasiswa(nim) == null) {
            showWarning("Pilih mahasiswa yang sudah terdaftar.");
            return;
        }
        if (dataService.isMahasiswaSimulasi(nim)) {
            showWarning("Riwayat ini tidak dapat diubah. Pilih mahasiswa yang terdaftar.");
            return;
        }
        LocalDate tanggal;
        try {
            tanggal = LocalDate.parse(tanggalField.getText().trim());
        } catch (DateTimeParseException ex) {
            showWarning("Tanggal harus memakai format YYYY-MM-DD, contoh: 2026-10-25.");
            return;
        }
        int semester = Integer.parseInt((String) riwayatSemesterField.getSelectedItem());
        String catatan = riwayatCatatanField.getText().trim();
        if (catatan.isEmpty()) {
            showWarning("Catatan perkembangan atau alasan pemilihan bidang wajib diisi.");
            return;
        }
        try {
            dataService.simpanRiwayat(new RiwayatPeminatan(nim, semester,
                    (String) bidangField.getSelectedItem(), catatan, tanggal));
            JOptionPane.showMessageDialog(this, "Riwayat semester tersimpan. Data semester lain tetap dipertahankan.");
            resetFormRiwayat();
            refreshSetelahPerubahan();
        } catch (IOException ex) {
            showError("Riwayat tidak dapat disimpan: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            showWarning(ex.getMessage());
        }
    }

    private void hapusRiwayat() {
        int row = riwayatTable.getSelectedRow();
        if (row < 0) { showWarning("Pilih riwayat pada tabel terlebih dahulu."); return; }
        int modelRow = riwayatTable.convertRowIndexToModel(row);
        String nim = riwayatModel.getValueAt(modelRow, 0).toString();
        int semester = Integer.parseInt(riwayatModel.getValueAt(modelRow, 3).toString());
        if (dataService.isRiwayatSimulasi(nim, semester)) {
            showWarning("Riwayat ini tidak dapat dihapus.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, "Hapus riwayat NIM " + nim
                + " semester " + semester + "?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) return;
        try {
            dataService.hapusRiwayat(nim, semester);
            resetFormRiwayat();
            refreshSetelahPerubahan();
        } catch (IOException ex) {
            showError("Riwayat tidak dapat dihapus: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            showWarning(ex.getMessage());
        }
    }

    private void perbaruiRiwayat() {
        if (riwayatNimSedangDiedit == null || semesterRiwayatSedangDiedit < 1) {
            showWarning("Pilih riwayat pada tabel yang akan diubah terlebih dahulu.");
            return;
        }
        if (dataService.isRiwayatSimulasi(riwayatNimSedangDiedit, semesterRiwayatSedangDiedit)) {
            showWarning("Riwayat ini tidak dapat diubah.");
            return;
        }
        String nim = (String) riwayatNimField.getSelectedItem();
        if (nim == null || dataService.cariMahasiswa(nim) == null) {
            showWarning("Pilih mahasiswa yang sudah terdaftar.");
            return;
        }
        LocalDate tanggal;
        try {
            tanggal = LocalDate.parse(tanggalField.getText().trim());
        } catch (DateTimeParseException ex) {
            showWarning("Tanggal harus memakai format YYYY-MM-DD, contoh: 2026-10-25.");
            return;
        }
        int semester = Integer.parseInt((String) riwayatSemesterField.getSelectedItem());
        String catatan = riwayatCatatanField.getText().trim();
        if (catatan.isEmpty()) {
            showWarning("Catatan perkembangan atau alasan pemilihan bidang wajib diisi.");
            return;
        }
        try {
            dataService.ubahRiwayat(riwayatNimSedangDiedit, semesterRiwayatSedangDiedit,
                    new RiwayatPeminatan(nim, semester, (String) bidangField.getSelectedItem(),
                            catatan, tanggal));
            JOptionPane.showMessageDialog(this, "Riwayat peminatan berhasil diperbarui.");
            resetFormRiwayat();
            refreshSetelahPerubahan();
        } catch (IllegalArgumentException ex) {
            showWarning(ex.getMessage());
        } catch (IOException ex) {
            showError("Riwayat tidak dapat diperbarui: " + ex.getMessage());
        }
    }

    private void isiFormRiwayat() {
        int row = riwayatTable.convertRowIndexToModel(riwayatTable.getSelectedRow());
        riwayatNimSedangDiedit = riwayatModel.getValueAt(row, 0).toString();
        semesterRiwayatSedangDiedit = Integer.parseInt(riwayatModel.getValueAt(row, 3).toString());
        riwayatNimField.setSelectedItem(riwayatNimSedangDiedit);
        riwayatSemesterField.setSelectedItem(String.valueOf(semesterRiwayatSedangDiedit));
        bidangField.setSelectedItem(RekomendasiService.normalisasiBidang(
                riwayatModel.getValueAt(row, 4).toString()));
        tanggalField.setText(riwayatModel.getValueAt(row, 5).toString());
        riwayatCatatanField.setText(riwayatModel.getValueAt(row, 6).toString());
        aturStatusRiwayat(true, dataService.isRiwayatSimulasi(
                riwayatNimSedangDiedit, semesterRiwayatSedangDiedit));
        isiNamaRiwayat();
    }

    private void aturStatusRiwayat(boolean selected, boolean simulasi) {
        boolean canModify = selected && !simulasi;
        riwayatSimpanButton.setEnabled(!simulasi);
        riwayatUbahButton.setEnabled(canModify);
        riwayatHapusButton.setEnabled(canModify);
        riwayatStatusLabel.setText(simulasi
                ? "Data riwayat tidak tersedia untuk diedit."
                : selected ? "Riwayat data asli — dapat diubah."
                : "Catat riwayat peminatan mahasiswa pada semester yang relevan.");
    }

    private void resetFormRiwayat() {
        riwayatTable.clearSelection();
        riwayatNimSedangDiedit = null;
        semesterRiwayatSedangDiedit = -1;
        riwayatNimField.setSelectedIndex(-1);
        riwayatSemesterField.setSelectedIndex(0);
        bidangField.setSelectedIndex(0);
        riwayatCatatanField.setText("");
        tanggalField.setText(LocalDate.now().toString());
        aturStatusRiwayat(false, false);
        isiNamaRiwayat();
    }

    private void isiNamaRekomendasi() {
        Mahasiswa m = dataService.cariMahasiswa((String) rekomendasiNimField.getSelectedItem());
        rekomendasiNamaLabel.setText(m == null ? "Pilih mahasiswa" : m.getNama());
    }

    private void aturRendererRekomendasi() {
        rekomendasiNimField.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list,
                    Object value, int index, boolean selected, boolean focused) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, selected, focused);
                Mahasiswa mahasiswa = value == null ? null : dataService.cariMahasiswa(value.toString());
                label.setText(mahasiswa == null ? "" : mahasiswa.getNim() + "  —  " + mahasiswa.getNama());
                label.setForeground(selected ? Color.WHITE : NAVY);
                label.setBackground(selected ? BLUE : Color.WHITE);
                label.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
                return label;
            }
        });
    }

    private void filterPilihanRekomendasi() {
        String query = rekomendasiCariField.getText().trim().toLowerCase(java.util.Locale.ROOT);
        String current = (String) rekomendasiNimField.getSelectedItem();
        memperbaruiPilihanRekomendasi = true;
        try {
            rekomendasiNimField.removeAllItems();
            for (Mahasiswa mahasiswa : dataService.semuaMahasiswa()) {
                boolean matches = query.isEmpty()
                        || mahasiswa.getNim().toLowerCase(java.util.Locale.ROOT).contains(query)
                        || mahasiswa.getNama().toLowerCase(java.util.Locale.ROOT).contains(query);
                if (matches) rekomendasiNimField.addItem(mahasiswa.getNim());
            }
            if (current != null) rekomendasiNimField.setSelectedItem(current);
            if (rekomendasiNimField.getSelectedItem() == null
                    && rekomendasiNimField.getItemCount() > 0) rekomendasiNimField.setSelectedIndex(0);
        } finally {
            memperbaruiPilihanRekomendasi = false;
        }
        isiNamaRekomendasi();
        if (rekomendasiNimField.getItemCount() == 0) {
            rekomendasiNamaLabel.setText("Tidak ada mahasiswa sesuai pencarian");
        }
    }

    private void analisisRekomendasi() {
        String nim = (String) rekomendasiNimField.getSelectedItem();
        Mahasiswa m = dataService.cariMahasiswa(nim);
        if (m == null) { showWarning("Pilih mahasiswa yang sudah terdaftar."); return; }
        RekomendasiService.HasilRekomendasi hasil = rekomendasiService.analisis(
                m, dataService.nilaiTerverifikasiMahasiswa(nim),
                dataService.semuaBidangPeminatan(), dataService.semuaMataKuliah());
        try {
            dataService.simpanRekomendasi(nim, hasil);
            tampilkanHasilRekomendasi(m, hasil);
            refreshLaporan();
            refreshDashboard();
        } catch (IOException ex) {
            showError("Hasil analisis tidak dapat disimpan: " + ex.getMessage());
        }
    }

    private void tampilkanPenjurusanTerpilih(Mahasiswa mahasiswa) {
        clearModel(rekomendasiModel);
        RiwayatPenjurusan pilihan = dataService.penjurusanMahasiswa(mahasiswa.getNim());
        if (pilihan == null) {
            rekomendasiPenjelasan.setText("Belum ada bidang peminatan yang dicatat untuk mahasiswa ini. "
                    + "Periksa persyaratan dan pilih bidang pada menu Penjurusan Semester 7.");
            return;
        }
        rekomendasiModel.addRow(new Object[]{"Bidang dipilih", pilihan.getBidang(), "-",
            String.join("; ", pilihan.getMataKuliahPendukung()),
            pilihan.isLayak() ? "Tercatat layak pada " + pilihan.getTanggal()
                    : "Riwayat tercatat belum memenuhi syarat."});
        rekomendasiPenjelasan.setText("Pilihan penjurusan semester 7: " + pilihan.getBidang()
                + "\nTanggal: " + pilihan.getTanggal()
                + "\nStatus: " + (pilihan.isLayak() ? "Memenuhi syarat" : "Belum memenuhi syarat")
                + "\n\nBidang yang tercatat merupakan pilihan mahasiswa, bukan rekomendasi sistem.");
    }

    private void tampilkanHasilRekomendasi(Mahasiswa m, RekomendasiService.HasilRekomendasi hasil) {
        clearModel(rekomendasiModel);
        List<RekomendasiService.SkorBidang> skor = hasil.getSkor();
        List<RekomendasiService.SkorBidang> layak = new ArrayList<>();
        for (RekomendasiService.SkorBidang item : skor) if (item.isLayak()) layak.add(item);
        for (int i = 0; i < Math.min(3, skor.size()); i++) {
            RekomendasiService.SkorBidang item = skor.get(i);
            String rank = item.isLayak() ? i < layak.size()
                    ? (layak.indexOf(item) == 0 ? "Utama" : "Alternatif " + layak.indexOf(item))
                    : "Tidak direkomendasikan" : "Tidak direkomendasikan";
            rekomendasiModel.addRow(new Object[]{rank,
                RekomendasiService.normalisasiBidang(item.getBidang()),
                String.format(java.util.Locale.ROOT, "%.2f", item.getRataRata()),
                item.getMataKuliahPendukung().isEmpty() ? "Belum ada nilai relevan"
                        : String.join("; ", item.getMataKuliahPendukung()),
                item.getStatusKelayakan() + ". "
                        + String.join(" ", item.getAlasan())});
        }
        StringBuilder text = new StringBuilder();
        text.append("Mahasiswa: ").append(m.getNama()).append(" (").append(m.getNim()).append(")\n")
                .append("Tanggal analisis: ").append(hasil.getTanggal()).append("\n\n");
        if (!layak.isEmpty()) {
            text.append("Rekomendasi utama: ").append(layak.get(0).getBidang())
                    .append(" — rata-rata nilai ")
                    .append(String.format(java.util.Locale.ROOT, "%.2f", layak.get(0).getRataRata()))
                    .append(" dari 4.\n");
            if (layak.size() > 1) {
                text.append("Alternatif: ");
                for (int i = 1; i < layak.size(); i++) {
                    if (i > 1) text.append(", ");
                    text.append(layak.get(i).getBidang());
                }
                text.append(".\n");
            }
        } else {
            text.append("Mahasiswa belum memenuhi syarat penjurusan pada bidang mana pun.\n")
                    .append("Daftar mata kuliah yang perlu diperbaiki atau dilengkapi:\n");
            for (RekomendasiService.SkorBidang item : skor) {
                text.append("• ").append(item.getBidang()).append(": ")
                        .append(String.join(" ", item.getAlasan())).append("\n");
            }
        }
        text.append("\nRata-rata dihitung dengan konversi A=4, B=3, C=2, D=1, dan E=0. "
                + "Nilai kosong ditampilkan sebagai BELUM LENGKAP.");
        rekomendasiPenjelasan.setText(text.toString());
    }

    private void muatPilihanMahasiswa() {
        String selectedLatar = (String) latarNimField.getSelectedItem();
        String selectedRiwayat = (String) riwayatNimField.getSelectedItem();
        String selectedRecommendation = (String) rekomendasiNimField.getSelectedItem();
        String selectedNilai = (String) kodeNilaiNimField.getSelectedItem();
        String selectedPenjurusan = (String) penjurusanNimField.getSelectedItem();
        latarNimField.removeAllItems();
        riwayatNimField.removeAllItems();
        kodeNilaiNimField.removeAllItems();
        penjurusanNimField.removeAllItems();
        if (penjurusanNimField.isEditable()) {
            penjurusanNimField.getEditor().setItem("");
        }
        penjurusanNimField.setSelectedIndex(-1);
        boolean selectedPenjurusanExists = false;
        List<Mahasiswa> daftarMahasiswa = dataService.semuaMahasiswa();
        for (Mahasiswa m : daftarMahasiswa) {
            latarNimField.addItem(m.getNim());
            riwayatNimField.addItem(m.getNim());
            kodeNilaiNimField.addItem(m.getNim());
            if (m.getSemester() == 7) {
                penjurusanNimField.addItem(m.getNim());
                if (m.getNim().equals(selectedPenjurusan)) selectedPenjurusanExists = true;
            }
        }
        if (selectedLatar != null) latarNimField.setSelectedItem(selectedLatar);
        if (selectedRiwayat != null) riwayatNimField.setSelectedItem(selectedRiwayat);
        if (selectedNilai != null) kodeNilaiNimField.setSelectedItem(selectedNilai);
        if (selectedPenjurusanExists) penjurusanNimField.setSelectedItem(selectedPenjurusan);
        else if (penjurusanNimField.getItemCount() > 0) penjurusanNimField.setSelectedIndex(0);
        filterPilihanRekomendasi();
        if (selectedRecommendation != null) rekomendasiNimField.setSelectedItem(selectedRecommendation);
        isiNamaRekomendasi();
        isiMataKuliahPilihan();
        perbaruiStatusPenjurusan();
        refreshRiwayatNilai();
    }

    private void refreshSetelahPerubahan() {
        muatPilihanMahasiswa();
        refreshDashboard();
        refreshTabelMahasiswa();
        refreshLatarBelakangTabel();
        refreshRiwayat();
        refreshBidangPeminatan();
        refreshRiwayatNilai();
        String nimRekomendasi = (String) rekomendasiNimField.getSelectedItem();
        Mahasiswa mahasiswaRekomendasi = dataService.cariMahasiswa(nimRekomendasi);
        if (mahasiswaRekomendasi != null) {
            RekomendasiService.HasilRekomendasi hasil = rekomendasiService.analisis(
                    mahasiswaRekomendasi, dataService.nilaiTerverifikasiMahasiswa(nimRekomendasi),
                    dataService.semuaBidangPeminatan(), dataService.semuaMataKuliah());
            try {
                dataService.simpanRekomendasi(nimRekomendasi, hasil);
                tampilkanHasilRekomendasi(mahasiswaRekomendasi, hasil);
            } catch (IOException ex) {
                showError("Rekomendasi terbaru tidak dapat disimpan: " + ex.getMessage());
            }
        } else {
            clearModel(rekomendasiModel);
            rekomendasiPenjelasan.setText("Pilih mahasiswa untuk menghitung rekomendasi.");
        }
        refreshLaporan();
    }

    private void refreshSemua() {
        refreshDashboard();
        refreshTabelMahasiswa();
        refreshNilaiTabel();
        refreshRiwayat();
        refreshBidangPeminatan();
        refreshLaporan();
    }

    private void refreshDashboard() {
        List<Mahasiswa> list = dataService.semuaMahasiswa();
        ringkasan[0].setText(String.valueOf(list.size()));
        ringkasan[1].setText(String.valueOf(dataService.jumlahMataKuliah()));
        ringkasan[2].setText(String.valueOf(dataService.jumlahMahasiswaLayakPenjurusan()));
        ringkasan[3].setText(String.valueOf(dataService.jumlahMahasiswaBelumLayakPenjurusan()));
        List<String> semesterLabels = new ArrayList<>();
        List<Integer> semesterCounts = new ArrayList<>();
        for (int semester = 1; semester <= 8; semester++) {
            final int current = semester;
            semesterLabels.add("Sem. " + semester);
            semesterCounts.add((int) list.stream().filter(m -> m.getSemester() == current).count());
        }
        grafikSemester.setData(semesterLabels, semesterCounts);

        int lulus = 0;
        int tidakLulus = 0;
        int belumDinilai = 0;
        for (Mahasiswa mahasiswa : list) {
            for (MataKuliah course : dataService.semuaMataKuliah()) {
                if (course.getSemester() > mahasiswa.getSemester()) continue;
                NilaiMataKuliah nilai = dataService.nilaiMahasiswa(mahasiswa.getNim(), course.getKode());
                if (nilai == null || !nilai.isTerverifikasi()) belumDinilai++;
                else if ("Lulus".equals(nilai.getStatusKelulusan())) lulus++;
                else tidakLulus++;
            }
        }
        grafikKelulusan.setData(Arrays.asList("Lulus", "Tidak lulus", "Belum dinilai"),
                Arrays.asList(lulus, tidakLulus, belumDinilai));

        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String bidang : RekomendasiService.BIDANG) counts.put(bidang, 0);
        for (RiwayatPenjurusan item : dataService.semuaPenjurusan()) {
            Mahasiswa student = dataService.cariMahasiswa(item.getNim());
            if (item.isLayak() && student != null
                    && "Sistem Informasi".equals(student.getProgramStudi())) {
                counts.put(item.getBidang(), counts.getOrDefault(item.getBidang(), 0) + 1);
            }
        }
        List<String> categoryLabels = new ArrayList<>();
        List<Integer> categoryCounts = new ArrayList<>();
        for (String bidang : RekomendasiService.BIDANG) {
            int count = counts.getOrDefault(bidang, 0);
            categoryLabels.add(labelRingkasBidang(bidang));
            categoryCounts.add(count);
        }
        grafikBidang.setData(categoryLabels, categoryCounts);
    }

    private String labelRingkasBidang(String bidang) {
        if (bidang.startsWith("UI/UX")) return "UI/UX";
        if (bidang.contains("(CRM)")) return "CRM";
        if (bidang.startsWith("Perencanaan")) return "Strategi SI/TI";
        if (bidang.startsWith("Pengukuran")) return "Kinerja TI";
        if (bidang.startsWith("Manajemen Proyek")) return "Proyek SI";
        return "Audit SI";
    }

    private void refreshRiwayat() {
        refreshPenjurusanTable();
    }

    private void refreshLaporan() {
        clearModel(laporanMahasiswaModel);
        clearModel(laporanRiwayatModel);
        clearModel(laporanRekomendasiModel);
        clearModel(laporanStatistikModel);
        clearModel(laporanNilaiModel);
        clearModel(laporanPenjurusanModel);
        String semester = (String) laporanSemesterField.getSelectedItem();
        String bidangFilter = (String) laporanBidangField.getSelectedItem();
        String jenisData = (String) laporanJenisDataField.getSelectedItem();
        String search = laporanCariField.getText().trim().toLowerCase(java.util.Locale.ROOT);
        List<LaporanMahasiswa> terpilih = new ArrayList<>();
        Map<String, Integer> bidangCounts = new LinkedHashMap<>();
        for (String bidang : RekomendasiService.BIDANG) bidangCounts.put(bidang, 0);
        int asli = 0;
        int dummy = 0;
        int lulus = 0;
        int tidakLulus = 0;
        int belumDinilai = 0;
        int memenuhi = 0;
        int belumMemenuhi = 0;
        int tercatat = 0;

        for (LaporanMahasiswa reportStudent : sumberDataLaporan()) {
            Mahasiswa m = reportStudent.mahasiswa;
            boolean matches = (semester == null || semester.equals("Semua Semester")
                    || semester.equals(String.valueOf(m.getSemester())))
                    && (m.getNim().toLowerCase(java.util.Locale.ROOT).contains(search)
                    || m.getNama().toLowerCase(java.util.Locale.ROOT).contains(search))
                    && (jenisData == null || jenisData.equals("Semua Data")
                    || (jenisData.equals("Data Asli") && "Data Asli".equals(reportStudent.sumber))
                    || (jenisData.equals("Data Dummy")
                    && reportStudent.sumber.startsWith("Data Dummy")));
            boolean bidangCocok = bidangFilter == null || bidangFilter.equals("Semua bidang")
                    || bidangFilter.equals("Semua Bidang")
                    || (reportStudent.penjurusan != null
                    && bidangFilter.equals(reportStudent.penjurusan.getBidang()))
                    || reportStudent.rekomendasi.getSkor().stream().anyMatch(score ->
                            score.isLayak() && score.getBidang().equals(bidangFilter));
            matches = matches && bidangCocok;
            if (!matches) continue;
            terpilih.add(reportStudent);
            if ("Data Asli".equals(reportStudent.sumber)) asli++;
            else dummy++;
            laporanMahasiswaModel.addRow(new Object[]{m.getNim(), m.getNama(), m.getProgramStudi(),
                m.getSemester(), m.getTahunAngkatan() == 0 ? "" : m.getTahunAngkatan(),
                reportStudent.sumber});
            Map<String, NilaiMataKuliah> grades = new LinkedHashMap<>();
            for (NilaiMataKuliah grade : reportStudent.nilai) grades.put(grade.getKode(), grade);
            List<MataKuliah> reportCourses = new ArrayList<>(dataService.semuaMataKuliah());
            Set<String> knownCourseCodes = new HashSet<>();
            for (MataKuliah course : reportCourses) knownCourseCodes.add(course.getKode());
            for (NilaiMataKuliah grade : reportStudent.nilai) {
                if (!knownCourseCodes.contains(grade.getKode())) {
                    reportCourses.add(new MataKuliah(grade.getKode(), grade.getNama(),
                            grade.getSemester(), grade.getSks(),
                            "Data Dummy/Simulasi".equals(reportStudent.sumber)));
                    knownCourseCodes.add(grade.getKode());
                }
            }
            for (MataKuliah course : reportCourses) {
                if (course.getSemester() > m.getSemester() && !grades.containsKey(course.getKode())) continue;
                NilaiMataKuliah nilai = grades.get(course.getKode());
                String gradeStatus = nilai == null ? "Belum Dinilai"
                        : nilai.isTerverifikasi() ? nilai.getStatusKelulusan()
                        : "Menunggu Verifikasi Admin";
                if ("Lulus".equals(gradeStatus)) lulus++;
                else if ("Tidak Lulus".equals(gradeStatus)) tidakLulus++;
                else belumDinilai++;
                laporanNilaiModel.addRow(new Object[]{m.getNim(), m.getNama(), course.getSemester(),
                    course.getKode(), course.getNama(),
                    course.getSks(), nilai == null ? "Belum Diisi" : nilai.getNilaiHuruf(),
                    gradeStatus, reportStudent.sumber});
            }
            RiwayatPenjurusan placement = reportStudent.penjurusan;
            if (placement != null) {
                bidangCounts.put(placement.getBidang(),
                        bidangCounts.getOrDefault(placement.getBidang(), 0) + 1);
                String supporting = String.join(", ", placement.getMataKuliahPendukung());
                String status = placement.isLayak() ? "Memenuhi Syarat" : "Belum Memenuhi";
                Object[] placementRow = {m.getNim(), m.getNama(), placement.getBidang(),
                    placement.getTanggal(), supporting, status, reportStudent.sumber};
                laporanRiwayatModel.addRow(placementRow);
                laporanPenjurusanModel.addRow(placementRow);
                tercatat++;
            }
            RekomendasiService.HasilRekomendasi recommendation = reportStudent.rekomendasi;
            if (!recommendation.getSkor().isEmpty()) {
                List<RekomendasiService.SkorBidang> qualifying = new ArrayList<>();
                for (RekomendasiService.SkorBidang item : recommendation.getSkor()) {
                    if (item.isLayak()) qualifying.add(item);
                }
                RekomendasiService.SkorBidang top = qualifying.isEmpty()
                        ? recommendation.getSkor().get(0) : qualifying.get(0);
                String alternatives = qualifying.stream().skip(1)
                        .map(RekomendasiService.SkorBidang::getBidang)
                        .collect(java.util.stream.Collectors.joining(", "));
                String alasan = qualifying.isEmpty()
                        ? String.join(" ", top.getAlasan())
                        : "Rata-rata nilai pendukung tertinggi.";
                laporanRekomendasiModel.addRow(new Object[]{m.getNim(), m.getNama(),
                    qualifying.isEmpty() ? "Belum memenuhi syarat" : top.getBidang(),
                    alternatives.isEmpty() ? "—" : alternatives,
                    String.format(java.util.Locale.ROOT, "%.2f / 4", top.getRataRata()),
                    qualifying.isEmpty() ? alasan : "Memenuhi seluruh prasyarat.",
                    reportStudent.sumber});
            }
            if (m.getSemester() == 7) {
                boolean eligible = recommendation.getSkor().stream()
                        .anyMatch(RekomendasiService.SkorBidang::isLayak);
                if (eligible) memenuhi++;
                else belumMemenuhi++;
            }
        }

        laporanTotalMahasiswaValue.setText(String.valueOf(terpilih.size()));
        laporanTotalMataKuliahValue.setText(String.valueOf(dataService.semuaMataKuliah().size()));
        laporanLayakValue.setText(String.valueOf(memenuhi));
        laporanBelumLayakValue.setText(String.valueOf(belumMemenuhi));
        laporanStatistikModel.addRow(new Object[]{"Total mahasiswa", "Sesuai filter", terpilih.size()});
        laporanStatistikModel.addRow(new Object[]{"Sumber data", "Data Asli", asli});
        laporanStatistikModel.addRow(new Object[]{"Sumber data", "Data Dummy/Simulasi", dummy});
        laporanStatistikModel.addRow(new Object[]{"Mata kuliah", "Jumlah tersedia", dataService.semuaMataKuliah().size()});
        laporanStatistikModel.addRow(new Object[]{"Nilai mata kuliah", "Lulus", lulus});
        laporanStatistikModel.addRow(new Object[]{"Nilai mata kuliah", "Tidak Lulus", tidakLulus});
        laporanStatistikModel.addRow(new Object[]{"Nilai mata kuliah", "Belum Dinilai", belumDinilai});
        laporanStatistikModel.addRow(new Object[]{"Penjurusan semester 7", "Memenuhi syarat", memenuhi});
        laporanStatistikModel.addRow(new Object[]{"Penjurusan semester 7", "Belum memenuhi syarat", belumMemenuhi});
        laporanStatistikModel.addRow(new Object[]{"Penjurusan", "Sudah tercatat", tercatat});
        List<String> semesterLabels = new ArrayList<>();
        List<Integer> semesterCounts = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            int jumlah = 0;
            for (LaporanMahasiswa item : terpilih) if (item.mahasiswa.getSemester() == i) jumlah++;
            laporanStatistikModel.addRow(new Object[]{"Semester", String.valueOf(i), jumlah});
            semesterLabels.add("Semester " + i);
            semesterCounts.add(jumlah);
        }
        laporanGrafikSemester.setData(semesterLabels, semesterCounts);
        laporanGrafikKelulusan.setData(Arrays.asList("Lulus", "Tidak Lulus"),
                Arrays.asList(lulus, tidakLulus), Arrays.asList(new Color(36, 143, 92), new Color(195, 72, 72)));
        List<String> fieldLabels = new ArrayList<>();
        List<Integer> fieldValues = new ArrayList<>();
        for (String bidang : RekomendasiService.BIDANG) {
            int count = bidangCounts.getOrDefault(bidang, 0);
            laporanStatistikModel.addRow(new Object[]{"Bidang peminatan tercatat", bidang, count});
            fieldLabels.add(labelRingkasBidang(bidang));
            fieldValues.add(count);
        }
        laporanGrafikBidang.setData(fieldLabels, fieldValues,
                Arrays.asList(BLUE, TEAL, GOLD));
        laporanGrafikPenjurusan.setData(Arrays.asList("Memenuhi", "Belum Memenuhi", "Tercatat"),
                Arrays.asList(memenuhi, belumMemenuhi, tercatat),
                Arrays.asList(new Color(36, 143, 92), new Color(195, 72, 72), GOLD));
        laporanStatistikModel.addRow(new Object[]{"Hasil rekomendasi", "Sudah dianalisis",
                laporanRekomendasiModel.getRowCount()});
    }

    private List<LaporanMahasiswa> sumberDataLaporan() {
        List<LaporanMahasiswa> result = new ArrayList<>();
        Set<String> nims = new HashSet<>();
        List<BidangPeminatan> fields = dataService.semuaBidangPeminatan();
        for (Mahasiswa student : dataService.semuaMahasiswa()) {
            nims.add(student.getNim());
            List<NilaiMataKuliah> grades = dataService.nilaiMahasiswa(student.getNim());
            String source = dataService.isMahasiswaSimulasi(student.getNim())
                    ? "Data Dummy/Simulasi" : "Data Asli";
            RekomendasiService.HasilRekomendasi recommendation =
                    rekomendasiService.analisis(student,
                            dataService.nilaiTerverifikasiMahasiswa(student.getNim()), fields,
                            dataService.semuaMataKuliah());
            result.add(new LaporanMahasiswa(student, source, grades,
                    dataService.penjurusanMahasiswa(student.getNim()), recommendation));
        }
        List<MataKuliah> courses = dataService.semuaMataKuliah();
        Set<String> requiredCourseCodes = new HashSet<>();
        for (BidangPeminatan field : fields) requiredCourseCodes.addAll(field.getKodeMataKuliahPendukung());
        for (int i = 1; i <= 11; i++) {
            String nim = String.format(java.util.Locale.ROOT, "SIM-RPT-%04d", i);
            while (nims.contains(nim)) nim = "S" + nim;
            nims.add(nim);
            Mahasiswa student = new Mahasiswa(nim, String.format("Simulasi Laporan %02d", i),
                    i % 2 == 0 ? "Perempuan" : "Laki-laki", "Sistem Informasi",
                    Math.min(i, 7), 2021 + (7 - Math.min(i, 7)), "Simulasi", "Simulasi",
                    "Data simulasi untuk tampilan laporan", "—",
                    "Data sintetis khusus laporan", "—", "");
            List<NilaiMataKuliah> grades = new ArrayList<>();
            for (MataKuliah course : courses) {
                if (course.getSemester() > student.getSemester()) continue;
                String grade = "A";
                if (i == 4 && !fields.isEmpty()
                        && !fields.get(0).getKodeMataKuliahPendukung().isEmpty()
                        && fields.get(0).getKodeMataKuliahPendukung().get(0).equals(course.getKode())) {
                    grade = "D";
                }
                if (i == 5 && fields.size() > 2
                        && !fields.get(2).getKodeMataKuliahPendukung().isEmpty()
                        && fields.get(2).getKodeMataKuliahPendukung().get(0).equals(course.getKode())) {
                    grade = "E";
                }
                if (i == 11 && requiredCourseCodes.contains(course.getKode())) grade = "D";
                grades.add(new NilaiMataKuliah(nim, course.getKode(), course.getNama(),
                        course.getSemester(), course.getSks(), grade));
            }
            RekomendasiService.HasilRekomendasi recommendation =
                    rekomendasiService.analisis(student, grades, fields,
                            dataService.semuaMataKuliah());
            RiwayatPenjurusan placement = null;
            if (i >= 8 && i <= 10) {
                List<RekomendasiService.SkorBidang> qualified = recommendation.getSkor().stream()
                        .filter(RekomendasiService.SkorBidang::isLayak)
                        .collect(java.util.stream.Collectors.toList());
                String preferredField = i - 8 < RekomendasiService.BIDANG.size()
                        ? RekomendasiService.BIDANG.get(i - 8) : "";
                RekomendasiService.SkorBidang selected = qualified.stream()
                        .filter(score -> score.getBidang().equals(preferredField))
                        .findFirst().orElse(qualified.isEmpty() ? null : qualified.get(0));
                if (selected != null) {
                    placement = new RiwayatPenjurusan(nim, selected.getBidang(),
                            LocalDate.now().minusDays(7),
                            selected.getMataKuliahPendukung(), true, true);
                }
            }
            result.add(new LaporanMahasiswa(student, "Data Dummy/Simulasi",
                    grades, placement, recommendation));
        }
        return result;
    }

    private void eksporLaporan() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Ekspor laporan akademik UNHASY");
        chooser.setSelectedFile(new File("laporan-akademik-unhasy.csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV (dapat dibuka dengan Excel)", "csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File selected = chooser.getSelectedFile();
        String fileName = selected.getName().toLowerCase(java.util.Locale.ROOT);
        Path tujuan = selected.toPath();
        if (!fileName.endsWith(".csv")) tujuan = tujuan.resolveSibling(selected.getName() + ".csv");

        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append(escapeCsv("LAPORAN AKADEMIK - SISTEM INFORMASI UNHASY")).append('\n');
        csv.append(escapeCsv("Semester: " + laporanSemesterField.getSelectedItem()
                + " | Bidang: " + laporanBidangField.getSelectedItem()
                + " | Jenis Data: " + laporanJenisDataField.getSelectedItem()
                + " | Pencarian: " + (laporanCariField.getText().trim().isEmpty()
                        ? "Semua" : laporanCariField.getText().trim()))).append("\n\n");
        tambahTabelCsv(csv, "Ringkasan Statistik", laporanStatistikModel);
        tambahTabelCsv(csv, "Daftar Mahasiswa", laporanMahasiswaModel);
        tambahTabelCsv(csv, "Nilai Mata Kuliah Semester 1-7", laporanNilaiModel);
        tambahTabelCsv(csv, "Hasil Rekomendasi", laporanRekomendasiModel);
        tambahTabelCsv(csv, "Riwayat Penjurusan Semester 7", laporanPenjurusanModel);
        try {
            Files.write(tujuan, csv.toString().getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this, "Laporan berhasil diekspor ke:\n" + tujuan);
        } catch (IOException ex) {
            showError("Laporan tidak dapat diekspor: " + ex.getMessage());
        }
    }

    private void tambahTabelCsv(StringBuilder csv, String judul, DefaultTableModel model) {
        csv.append(escapeCsv(judul)).append('\n');
        for (int col = 0; col < model.getColumnCount(); col++) {
            if (col > 0) csv.append(',');
            csv.append(escapeCsv(model.getColumnName(col)));
        }
        csv.append('\n');
        for (int row = 0; row < model.getRowCount(); row++) {
            for (int col = 0; col < model.getColumnCount(); col++) {
                if (col > 0) csv.append(',');
                Object value = model.getValueAt(row, col);
                csv.append(escapeCsv(value == null ? "" : value.toString()));
            }
            csv.append('\n');
        }
        csv.append('\n');
    }

    private String escapeCsv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private void cetakLaporan() {
        StringBuilder report = new StringBuilder("LAPORAN AKADEMIK - SISTEM INFORMASI UNHASY\n")
                .append("Semester: ").append(laporanSemesterField.getSelectedItem())
                .append(" | Bidang: ").append(laporanBidangField.getSelectedItem())
                .append(" | Jenis Data: ").append(laporanJenisDataField.getSelectedItem()).append('\n')
                .append("Pencarian: ").append(laporanCariField.getText().trim().isEmpty()
                        ? "Semua" : laporanCariField.getText().trim())
                .append(" | Tanggal: ").append(LocalDate.now()).append("\n\n");
        tambahModelCetak(report, "Ringkasan Statistik", laporanStatistikModel);
        tambahModelCetak(report, "Data Mahasiswa", laporanMahasiswaModel);
        tambahModelCetak(report, "Nilai Mata Kuliah", laporanNilaiModel);
        tambahModelCetak(report, "Riwayat Penjurusan", laporanPenjurusanModel);
        tambahModelCetak(report, "Hasil Rekomendasi", laporanRekomendasiModel);
        JTextArea printable = new JTextArea(report.toString());
        printable.setFont(new Font("Monospaced", Font.PLAIN, 9));
        printable.setLineWrap(true);
        printable.setWrapStyleWord(true);
        try {
            printable.print(
                    new java.text.MessageFormat("Laporan Akademik UNHASY"),
                    new java.text.MessageFormat("Halaman {0}"));
        } catch (java.awt.print.PrinterException ex) {
            showError("Laporan tidak dapat dicetak: " + ex.getMessage());
        }
    }

    private void tambahModelCetak(StringBuilder report, String title, DefaultTableModel model) {
        report.append(title).append('\n');
        for (int col = 0; col < model.getColumnCount(); col++) {
            if (col > 0) report.append(" | ");
            report.append(model.getColumnName(col));
        }
        report.append('\n');
        for (int row = 0; row < model.getRowCount(); row++) {
            for (int col = 0; col < model.getColumnCount(); col++) {
                if (col > 0) report.append(" | ");
                Object value = model.getValueAt(row, col);
                report.append(value == null ? "" : value.toString());
            }
            report.append('\n');
        }
        report.append('\n');
    }

    private void simpanLaporanPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan laporan akademik sebagai PDF");
        chooser.setSelectedFile(new File("laporan-akademik-unhasy.pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("Dokumen PDF", "pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File selected = chooser.getSelectedFile();
        Path destination = selected.toPath();
        if (!selected.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")) {
            destination = destination.resolveSibling(selected.getName() + ".pdf");
        }
        List<String> lines = new ArrayList<>();
        lines.add("LAPORAN AKADEMIK - SISTEM INFORMASI UNHASY");
        lines.add("Semester: " + laporanSemesterField.getSelectedItem()
                + " | Bidang: " + laporanBidangField.getSelectedItem()
                + " | Jenis data: " + laporanJenisDataField.getSelectedItem());
        lines.add("Pencarian: " + (laporanCariField.getText().trim().isEmpty()
                ? "Semua" : laporanCariField.getText().trim()));
        lines.add("Dibuat: " + LocalDate.now());
        lines.add("");
        tambahModelPdf(lines, "Ringkasan Statistik", laporanStatistikModel);
        tambahModelPdf(lines, "Data Mahasiswa", laporanMahasiswaModel);
        tambahModelPdf(lines, "Nilai Mata Kuliah", laporanNilaiModel);
        tambahModelPdf(lines, "Riwayat Penjurusan", laporanPenjurusanModel);
        tambahModelPdf(lines, "Hasil Rekomendasi", laporanRekomendasiModel);
        try {
            Files.write(destination, buatPdf(lines));
            JOptionPane.showMessageDialog(this, "Laporan PDF berhasil disimpan ke:\n" + destination);
        } catch (IOException ex) {
            showError("Laporan PDF tidak dapat disimpan: " + ex.getMessage());
        }
    }

    private void tambahModelPdf(List<String> lines, String title, DefaultTableModel model) {
        lines.add(title);
        List<String> headings = new ArrayList<>();
        for (int col = 0; col < model.getColumnCount(); col++) headings.add(model.getColumnName(col));
        tambahBarisPdf(lines, headings);
        for (int row = 0; row < model.getRowCount(); row++) {
            List<String> values = new ArrayList<>();
            for (int col = 0; col < model.getColumnCount(); col++) {
                Object value = model.getValueAt(row, col);
                values.add(value == null ? "" : value.toString());
            }
            tambahBarisPdf(lines, values);
        }
        lines.add("");
    }

    private void tambahBarisPdf(List<String> lines, List<String> values) {
        StringBuilder row = new StringBuilder();
        for (String value : values) {
            if (row.length() > 0) row.append(" | ");
            row.append(value);
        }
        String remaining = row.toString();
        while (remaining.length() > 100) {
            int split = remaining.lastIndexOf(' ', 100);
            if (split < 1) split = 100;
            lines.add(remaining.substring(0, split));
            remaining = remaining.substring(split).trim();
        }
        lines.add(remaining);
    }

    private byte[] buatPdf(List<String> lines) throws IOException {
        List<List<String>> pages = new ArrayList<>();
        for (int start = 0; start < lines.size(); start += 55) {
            pages.add(new ArrayList<>(lines.subList(start, Math.min(start + 55, lines.size()))));
        }
        if (pages.isEmpty()) pages.add(new ArrayList<>());
        List<byte[]> objects = new ArrayList<>();
        objects.add(null);
        objects.add(null);
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"
                .getBytes(StandardCharsets.US_ASCII));
        for (int i = 0; i < pages.size(); i++) {
            int pageObject = 4 + i * 2;
            int contentObject = pageObject + 1;
            String page = "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] "
                    + "/Resources << /Font << /F1 3 0 R >> >> /Contents "
                    + contentObject + " 0 R >>";
            objects.add(page.getBytes(StandardCharsets.US_ASCII));
            StringBuilder content = new StringBuilder("BT\n/F1 9 Tf\n40 752 Td\n12 TL\n");
            for (String line : pages.get(i)) {
                content.append('(').append(escapePdf(line)).append(") Tj\nT*\n");
            }
            content.append("ET");
            byte[] stream = content.toString().getBytes(StandardCharsets.US_ASCII);
            String streamHeader = "<< /Length " + stream.length + " >>\nstream\n";
            byte[] header = streamHeader.getBytes(StandardCharsets.US_ASCII);
            byte[] tail = "\nendstream".getBytes(StandardCharsets.US_ASCII);
            byte[] streamObject = new byte[header.length + stream.length + tail.length];
            System.arraycopy(header, 0, streamObject, 0, header.length);
            System.arraycopy(stream, 0, streamObject, header.length, stream.length);
            System.arraycopy(tail, 0, streamObject, header.length + stream.length, tail.length);
            objects.add(streamObject);
        }
        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < pages.size(); i++) kids.append(4 + i * 2).append(" 0 R ");
        objects.set(0, "<< /Type /Catalog /Pages 2 0 R >>".getBytes(StandardCharsets.US_ASCII));
        objects.set(1, ("<< /Type /Pages /Kids [" + kids + "] /Count " + pages.size() + " >>")
                .getBytes(StandardCharsets.US_ASCII));

        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        output.write("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(output.size());
            output.write((i + 1 + " 0 obj\n").getBytes(StandardCharsets.US_ASCII));
            output.write(objects.get(i));
            output.write("\nendobj\n".getBytes(StandardCharsets.US_ASCII));
        }
        int xref = output.size();
        output.write(("xref\n0 " + (objects.size() + 1) + "\n").getBytes(StandardCharsets.US_ASCII));
        output.write("0000000000 65535 f \n".getBytes(StandardCharsets.US_ASCII));
        for (int i = 1; i < offsets.size(); i++) {
            output.write(String.format(java.util.Locale.ROOT, "%010d 00000 n \n", offsets.get(i))
                    .getBytes(StandardCharsets.US_ASCII));
        }
        output.write(("trailer\n<< /Size " + (objects.size() + 1)
                + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF")
                .getBytes(StandardCharsets.US_ASCII));
        return output.toByteArray();
    }

    private String escapePdf(String value) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character < 32 || character > 126) character = '?';
            if (character == '(' || character == ')' || character == '\\') result.append('\\');
            result.append(character);
        }
        return result.toString();
    }

    private static void clearModel(DefaultTableModel model) {
        model.setRowCount(0);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Periksa data", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Terjadi kesalahan", JOptionPane.ERROR_MESSAGE);
    }

    private JButton tombolAksiMahasiswa(int action) {
        Color background = action == 2 ? new Color(202, 55, 68) : BLUE;
        JButton button = new JButton(new MahasiswaActionIcon(action, Color.WHITE));
        button.setUI(new BasicButtonUI());
        button.setBackground(background);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.darker()),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        button.setPreferredSize(new Dimension(38, 30));
        button.setToolTipText(action == 0 ? "Lihat detail"
                : action == 1 ? "Ubah data" : "Hapus data");
        return button;
    }

    private class MahasiswaActionRenderer extends JPanel implements TableCellRenderer {
        private MahasiswaActionRenderer() {
            super(new GridLayout(1, 3, 5, 0));
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(4, 5, 4, 5));
            add(tombolAksiMahasiswa(0));
            add(tombolAksiMahasiswa(1));
            add(tombolAksiMahasiswa(2));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focused, int row, int column) {
            setBackground(selected ? new Color(231, 240, 252)
                    : row % 2 == 0 ? Color.WHITE : new Color(243, 247, 251));
            return this;
        }
    }

    private class MahasiswaActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new GridLayout(1, 3, 5, 0));
        private int modelRow = -1;

        private MahasiswaActionEditor() {
            panel.setOpaque(true);
            panel.setBorder(BorderFactory.createEmptyBorder(4, 5, 4, 5));
            for (int i = 0; i < 3; i++) {
                final int action = i;
                JButton button = tombolAksiMahasiswa(action);
                button.addActionListener(e -> {
                    int row = modelRow;
                    fireEditingStopped();
                    jalankanAksiMahasiswa(row, action);
                });
                panel.add(button);
            }
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean selected, int row, int column) {
            modelRow = table.convertRowIndexToModel(row);
            panel.setBackground(selected ? new Color(231, 240, 252)
                    : row % 2 == 0 ? Color.WHITE : new Color(243, 247, 251));
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    private class NilaiActionRenderer extends JPanel implements TableCellRenderer {
        private NilaiActionRenderer() {
            super(new GridLayout(1, 2, 5, 0));
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(4, 5, 4, 5));
            add(tombolAksiMahasiswa(1));
            add(tombolAksiMahasiswa(2));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focused, int row, int column) {
            setBackground(selected ? new Color(231, 240, 252)
                    : row % 2 == 0 ? Color.WHITE : new Color(243, 247, 251));
            return this;
        }
    }

    private class NilaiActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new GridLayout(1, 2, 5, 0));
        private int modelRow = -1;

        private NilaiActionEditor() {
            panel.setOpaque(true);
            panel.setBorder(BorderFactory.createEmptyBorder(4, 5, 4, 5));
            for (int action = 0; action < 2; action++) {
                final int selectedAction = action;
                JButton button = tombolAksiMahasiswa(action + 1);
                button.addActionListener(e -> {
                    int row = modelRow;
                    fireEditingStopped();
                    jalankanAksiNilai(row, selectedAction);
                });
                panel.add(button);
            }
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean selected, int row, int column) {
            modelRow = table.convertRowIndexToModel(row);
            panel.setBackground(selected ? new Color(231, 240, 252)
                    : row % 2 == 0 ? Color.WHITE : new Color(243, 247, 251));
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    private static class MahasiswaActionIcon implements javax.swing.Icon {
        private final int type;
        private final Color color;

        private MahasiswaActionIcon(int type, Color color) {
            this.type = type;
            this.color = color;
        }

        @Override public int getIconWidth() { return 18; }
        @Override public int getIconHeight() { return 18; }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.translate(x, y);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);
            g.setStroke(new java.awt.BasicStroke(1.7f,
                    java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            if (type == 0) {
                g.drawOval(1, 4, 16, 10);
                g.fillOval(7, 7, 5, 5);
            } else if (type == 1) {
                g.drawLine(4, 14, 14, 4);
                g.drawLine(12, 3, 15, 6);
                g.drawLine(3, 15, 7, 14);
                g.drawLine(3, 15, 4, 11);
            } else {
                g.drawLine(4, 5, 14, 5);
                g.drawLine(6, 3, 12, 3);
                g.drawRect(5, 6, 8, 10);
                g.drawLine(8, 8, 8, 14);
                g.drawLine(10, 8, 10, 14);
            }
            g.dispose();
        }
    }

    private static class MenuIcon implements javax.swing.Icon {
        private final int type;
        private Color color;

        private MenuIcon(int type, Color color) {
            this.type = type;
            this.color = color;
        }

        private void setColor(Color color) {
            this.color = color;
        }

        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.translate(x, y);
            g.setColor(color);
            g.setStroke(new java.awt.BasicStroke(1.8f,
                    java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            switch (type) {
                case 0:
                    g.drawLine(2, 9, 10, 3); g.drawLine(10, 3, 18, 9);
                    g.drawRect(4, 8, 12, 9); g.drawRect(8, 12, 4, 5);
                    break;
                case 1:
                    g.drawOval(7, 3, 6, 6); g.drawArc(4, 10, 12, 10, 0, 180);
                    break;
                case 2:
                case 6:
                    g.drawRoundRect(3, 3, 14, 15, 2, 2);
                    g.drawLine(6, 7, 14, 7); g.drawLine(6, 10, 14, 10);
                    g.drawLine(6, 13, 12, 13);
                    break;
                case 3:
                    int[] px = {10, 12, 18, 13, 14, 10, 6, 7, 2, 8};
                    int[] py = {2, 7, 8, 11, 17, 14, 18, 12, 8, 7};
                    g.drawPolygon(px, py, px.length);
                    break;
                case 4:
                    g.drawLine(2, 7, 10, 3); g.drawLine(10, 3, 18, 7);
                    g.drawLine(2, 7, 10, 11); g.drawLine(10, 11, 18, 7);
                    g.drawLine(5, 9, 5, 14); g.drawLine(15, 9, 15, 14);
                    g.drawLine(10, 11, 10, 17); g.drawLine(15, 14, 17, 15);
                    break;
                case 5:
                    g.drawLine(11, 2, 12, 7); g.drawLine(12, 7, 17, 9);
                    g.drawLine(17, 9, 12, 11); g.drawLine(12, 11, 10, 17);
                    g.drawLine(10, 17, 8, 11); g.drawLine(8, 11, 3, 9);
                    g.drawLine(3, 9, 8, 7); g.drawLine(8, 7, 11, 2);
                    break;
                case 7:
                    g.drawOval(2, 2, 11, 11);
                    g.drawLine(11, 11, 18, 18);
                    break;
                default:
                    g.drawLine(3, 17, 3, 3); g.drawLine(3, 17, 18, 17);
                    g.drawLine(6, 14, 6, 10); g.drawLine(10, 14, 10, 6);
                    g.drawLine(14, 14, 14, 8); g.drawLine(18, 14, 18, 3);
                    break;
            }
            g.dispose();
        }
    }

    private static class PenjurusanIcon implements javax.swing.Icon {
        private static final int GRADUATION = 0;
        private static final int USERS = 1;
        private static final int CHECKLIST = 2;
        private static final int STAR = 3;
        private static final int CLIPBOARD = 4;
        private static final int CHART = 5;
        private static final int CHECK = 6;
        private static final int CROSS = 7;
        private static final int INFO = 8;
        private static final int LIGHTBULB = 9;
        private static final int DOCUMENT = 10;
        private static final int SEARCH = 11;
        private static final int BOOK = 12;
        private static final int BRIEFCASE = 13;
        private static final int USER = 14;

        private final int type;
        private final Color color;
        private final int size;

        private PenjurusanIcon(int type, Color color, int size) {
            this.type = type;
            this.color = color;
            this.size = size;
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.translate(x, y);
            g.scale(size / 24.0, size / 24.0);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);
            g.setStroke(new java.awt.BasicStroke(2.2f,
                    java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            switch (type) {
                case GRADUATION:
                    g.drawLine(2, 8, 12, 3);
                    g.drawLine(12, 3, 22, 8);
                    g.drawLine(2, 8, 12, 13);
                    g.drawLine(12, 13, 22, 8);
                    g.drawLine(5, 10, 5, 16);
                    g.drawArc(5, 10, 14, 9, 185, 170);
                    g.drawLine(21, 8, 21, 15);
                    g.fillOval(20, 15, 3, 3);
                    break;
                case USERS:
                    g.fillOval(9, 3, 6, 6);
                    g.fillOval(2, 6, 5, 5);
                    g.fillOval(17, 6, 5, 5);
                    g.fillRoundRect(6, 11, 12, 8, 5, 5);
                    g.fillRoundRect(0, 13, 7, 6, 4, 4);
                    g.fillRoundRect(17, 13, 7, 6, 4, 4);
                    break;
                case CHECKLIST:
                    g.drawRoundRect(5, 3, 16, 19, 2, 2);
                    g.drawRoundRect(9, 1, 8, 5, 2, 2);
                    g.drawLine(8, 10, 10, 12);
                    g.drawLine(10, 12, 13, 8);
                    g.drawLine(15, 10, 18, 10);
                    g.drawLine(8, 16, 10, 18);
                    g.drawLine(10, 18, 13, 14);
                    g.drawLine(15, 16, 18, 16);
                    break;
                case STAR:
                    int[] sx = {12, 15, 22, 17, 18, 12, 6, 7, 2, 9};
                    int[] sy = {2, 8, 8, 12, 20, 16, 20, 12, 8, 8};
                    g.fillPolygon(sx, sy, sx.length);
                    break;
                case CLIPBOARD:
                    g.drawRoundRect(5, 4, 14, 18, 2, 2);
                    g.drawRoundRect(9, 2, 6, 5, 2, 2);
                    g.drawLine(8, 11, 16, 11);
                    g.drawLine(8, 15, 16, 15);
                    g.drawLine(8, 19, 13, 19);
                    break;
                case CHART:
                    g.fillRoundRect(3, 13, 4, 8, 1, 1);
                    g.fillRoundRect(10, 8, 4, 13, 1, 1);
                    g.fillRoundRect(17, 3, 4, 18, 1, 1);
                    break;
                case CHECK:
                    g.setStroke(new java.awt.BasicStroke(3f,
                            java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                    g.drawLine(5, 12, 10, 17);
                    g.drawLine(10, 17, 20, 6);
                    break;
                case CROSS:
                    g.setStroke(new java.awt.BasicStroke(3f,
                            java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                    g.drawLine(6, 6, 18, 18);
                    g.drawLine(18, 6, 6, 18);
                    break;
                case INFO:
                    g.fillOval(1, 1, 22, 22);
                    g.setColor(Color.WHITE);
                    g.fillOval(11, 6, 2, 2);
                    g.fillRoundRect(10, 10, 4, 8, 2, 2);
                    break;
                case LIGHTBULB:
                    g.drawOval(5, 2, 14, 14);
                    g.drawArc(8, 12, 8, 8, 180, 180);
                    g.drawLine(9, 20, 15, 20);
                    g.drawLine(10, 22, 14, 22);
                    g.drawLine(12, 0, 12, 1);
                    g.drawLine(2, 7, 4, 8);
                    g.drawLine(20, 8, 22, 7);
                    break;
                case DOCUMENT:
                    g.drawRoundRect(4, 2, 15, 20, 2, 2);
                    g.drawLine(8, 8, 15, 8);
                    g.drawLine(8, 12, 15, 12);
                    g.drawLine(8, 16, 13, 16);
                    g.drawLine(16, 2, 19, 6);
                    break;
                case SEARCH:
                    g.drawOval(3, 3, 12, 12);
                    g.drawLine(13, 13, 21, 21);
                    break;
                case BOOK:
                    g.drawRoundRect(2, 4, 9, 17, 1, 1);
                    g.drawRoundRect(13, 4, 9, 17, 1, 1);
                    g.drawLine(11, 5, 11, 21);
                    g.drawLine(4, 8, 8, 8);
                    g.drawLine(16, 8, 20, 8);
                    break;
                case BRIEFCASE:
                    g.drawRoundRect(2, 7, 20, 14, 2, 2);
                    g.drawRoundRect(8, 3, 8, 5, 1, 1);
                    g.drawLine(2, 12, 22, 12);
                    g.drawLine(10, 11, 14, 11);
                    g.drawLine(10, 13, 14, 13);
                    break;
                case USER:
                    g.fillOval(8, 2, 8, 8);
                    g.fillRoundRect(4, 12, 16, 10, 6, 6);
                    break;
                default:
                    throw new IllegalStateException("Jenis ikon penjurusan tidak dikenal: " + type);
            }
            g.dispose();
        }
    }

    private static class MetricIcon implements javax.swing.Icon {
        private final int type;

        private MetricIcon(int type) {
            this.type = type;
        }

        @Override public int getIconWidth() { return 42; }
        @Override public int getIconHeight() { return 42; }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Color[] colors = {BLUE, GOLD, TEAL, new Color(120, 91, 209)};
            Graphics2D g = (Graphics2D) graphics.create();
            g.translate(x, y);
            g.scale(1.4, 1.4);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(colors[type].getRed(), colors[type].getGreen(),
                    colors[type].getBlue(), 28));
            g.fillRoundRect(0, 0, 30, 30, 10, 10);
            g.setColor(colors[type]);
            g.setStroke(new java.awt.BasicStroke(2f,
                    java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            switch (type) {
                case 0:
                    g.drawOval(11, 5, 8, 8);
                    g.drawArc(7, 15, 16, 12, 0, 180);
                    break;
                case 1:
                    g.drawRoundRect(6, 5, 18, 20, 2, 2);
                    g.drawLine(10, 11, 20, 11); g.drawLine(10, 15, 20, 15);
                    g.drawLine(10, 19, 17, 19);
                    break;
                case 2:
                    g.drawOval(5, 5, 20, 20);
                    g.drawLine(10, 15, 14, 19); g.drawLine(14, 19, 21, 10);
                    break;
                default:
                    g.drawLine(15, 5, 15, 25);
                    g.drawLine(8, 8, 22, 8); g.drawLine(9, 8, 13, 14);
                    g.drawLine(21, 8, 17, 14); g.drawLine(13, 14, 17, 14);
                    g.drawLine(10, 23, 20, 23);
                    break;
            }
            g.dispose();
        }
    }

    private static class LaporanMahasiswa {
        private final Mahasiswa mahasiswa;
        private final String sumber;
        private final List<NilaiMataKuliah> nilai;
        private final RiwayatPenjurusan penjurusan;
        private final RekomendasiService.HasilRekomendasi rekomendasi;

        private LaporanMahasiswa(Mahasiswa mahasiswa, String sumber, List<NilaiMataKuliah> nilai,
                                 RiwayatPenjurusan penjurusan,
                                 RekomendasiService.HasilRekomendasi rekomendasi) {
            this.mahasiswa = mahasiswa;
            this.sumber = sumber;
            this.nilai = new ArrayList<>(nilai);
            this.penjurusan = penjurusan;
            this.rekomendasi = rekomendasi;
        }
    }

    private static class GrafikIpkMahasiswa extends JPanel {
        private List<String> label = new ArrayList<>();
        private List<Double> nilai = new ArrayList<>();

        private GrafikIpkMahasiswa() {
            setOpaque(false);
            setPreferredSize(new Dimension(440, 175));
        }

        private void setData(List<String> labels, List<Double> values) {
            label = new ArrayList<>(labels);
            nilai = new ArrayList<>(values);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int left = 38;
            int right = 12;
            int top = 18;
            int bottom = 32;
            int plotHeight = Math.max(1, getHeight() - top - bottom);
            int plotWidth = Math.max(1, getWidth() - left - right);
            g.setFont(new Font("SansSerif", Font.PLAIN, 10));
            for (int level = 0; level <= 4; level++) {
                int y = top + plotHeight - (int) Math.round(plotHeight * level / 4d);
                g.setColor(new Color(225, 233, 243));
                g.drawLine(left, y, left + plotWidth, y);
                g.setColor(new Color(88, 106, 130));
                String tick = String.valueOf(level);
                g.drawString(tick, left - 17, y + 4);
            }
            if (label.isEmpty()) {
                g.setColor(new Color(102, 116, 134));
                g.setFont(new Font("SansSerif", Font.PLAIN, 12));
                String message = "Belum ada nilai terverifikasi untuk ditampilkan.";
                g.drawString(message, Math.max(left, (getWidth()
                        - g.getFontMetrics().stringWidth(message)) / 2), getHeight() / 2);
                g.dispose();
                return;
            }
            int slot = Math.max(1, plotWidth / label.size());
            int barWidth = Math.max(14, Math.min(42, (int) (slot * 0.52)));
            for (int i = 0; i < label.size(); i++) {
                double gpa = Math.max(0d, Math.min(4d, nilai.get(i)));
                int barHeight = (int) Math.round(plotHeight * gpa / 4d);
                int x = left + i * slot + (slot - barWidth) / 2;
                int y = top + plotHeight - barHeight;
                if (barHeight > 0) {
                    g.setPaint(new java.awt.GradientPaint(x, y,
                            new Color(79, 164, 236), x, top + plotHeight,
                            new Color(22, 105, 197)));
                    g.fillRoundRect(x, y, barWidth, barHeight, 7, 7);
                }
                g.setColor(NAVY);
                g.setFont(new Font("SansSerif", Font.BOLD, 10));
                String value = nilai.get(i) <= 0d ? "—"
                        : String.format(java.util.Locale.ROOT, "%.2f", gpa);
                int valueX = x + (barWidth - g.getFontMetrics().stringWidth(value)) / 2;
                g.drawString(value, valueX, Math.max(top - 3, y - 5));
                g.setFont(new Font("SansSerif", Font.PLAIN, 10));
                int labelX = left + i * slot
                        + (slot - g.getFontMetrics().stringWidth(label.get(i))) / 2;
                g.setColor(new Color(63, 83, 112));
                g.drawString(label.get(i), labelX, top + plotHeight + 19);
            }
            g.dispose();
        }
    }

    private static class GrafikBatang extends JPanel {
        private final Color warna;
        private List<String> label = new ArrayList<>();
        private List<Integer> nilai = new ArrayList<>();
        private List<Color> warnaBatang = new ArrayList<>();

        private GrafikBatang(Color warna) {
            this.warna = warna;
            setOpaque(false);
            setPreferredSize(new Dimension(300, 150));
        }

        private void setData(List<String> labels, List<Integer> values) {
            setData(labels, values, new ArrayList<>());
        }

        private void setData(List<String> labels, List<Integer> values, List<Color> colors) {
            label = new ArrayList<>(labels);
            nilai = new ArrayList<>(values);
            warnaBatang = new ArrayList<>(colors);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean adaData = nilai.stream().anyMatch(count -> count > 0);
            if (!adaData) {
                String message = "Belum ada data";
                g.setColor(new Color(102, 116, 134));
                g.setFont(new Font("SansSerif", Font.PLAIN, 13));
                g.drawString(message,
                        Math.max(0, (getWidth() - g.getFontMetrics().stringWidth(message)) / 2),
                        Math.max(18, getHeight() / 2));
                g.dispose();
                return;
            }
            int barX = Math.max(82, Math.min(132, getWidth() / 3));
            int barWidth = Math.max(12, getWidth() - barX - 38);
            int rowHeight = Math.max(18, (getHeight() - 10) / Math.max(1, label.size()));
            int max = 1;
            for (Integer count : nilai) max = Math.max(max, count);
            for (int i = 0; i < label.size(); i++) {
                int y = 7 + i * rowHeight;
                int count = nilai.get(i);
                g.setColor(TEXT);
                g.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g.drawString(label.get(i), 2, y + Math.min(rowHeight - 3, 14));
                g.setColor(new Color(230, 236, 243));
                g.fillRoundRect(barX, y + 2, barWidth, Math.max(8, rowHeight - 7), 8, 8);
                int filled = count == 0 ? 0 : Math.max(4, barWidth * count / max);
                g.setColor(i < warnaBatang.size() ? warnaBatang.get(i) : warna);
                g.fillRoundRect(barX, y + 2, filled, Math.max(8, rowHeight - 7), 8, 8);
                g.setColor(NAVY);
                g.drawString(String.valueOf(count), barX + barWidth + 7, y + Math.min(rowHeight - 3, 14));
            }
            g.dispose();
        }

    }

    private static class WrappingTableCellRenderer extends JTextArea implements TableCellRenderer {
        private WrappingTableCellRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focused, int row, int column) {
            setText(value == null ? "" : value.toString());
            setFont(table.getFont());
            setForeground(selected ? NAVY : TEXT);
            setBackground(selected ? new Color(222, 237, 250)
                    : (row % 2 == 0 ? Color.WHITE : new Color(243, 247, 251)));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, selected ? 3 : 0, 1, 0,
                            selected ? GOLD : new Color(232, 237, 243)),
                    BorderFactory.createEmptyBorder(7, 10, 7, 10)));
            setToolTipText(value == null ? null : value.toString());
            return this;
        }
    }
}
