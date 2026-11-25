import java.io.Serializable;

public class Mahasiswa implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nim;
    private String nama;
    private String jenisKelamin;
    private String programStudi;
    private int semester;
    private int tahunAngkatan;
    private String asalSekolah;
    private String jurusanAsal;
    private String pengalamanTeknologi;
    private String minatAwal;
    private String alasanMinat;
    private String proyek;
    private String fotoPath;

    public Mahasiswa(String nim, String nama, String jenisKelamin, String programStudi,
                     int semester, int tahunAngkatan, String asalSekolah, String jurusanAsal,
                     String pengalamanTeknologi, String minatAwal, String alasanMinat,
                     String proyek, String fotoPath) {
        this.nim = nim;
        this.nama = nama;
        this.jenisKelamin = jenisKelamin;
        this.programStudi = programStudi;
        this.semester = semester;
        this.tahunAngkatan = tahunAngkatan;
        this.asalSekolah = asalSekolah;
        this.jurusanAsal = jurusanAsal;
        this.pengalamanTeknologi = pengalamanTeknologi;
        this.minatAwal = minatAwal;
        this.alasanMinat = alasanMinat;
        this.proyek = proyek;
        this.fotoPath = fotoPath;
    }

    public String getNim() { return nim; }
    public String getNama() { return nama; }
    public String getJenisKelamin() { return jenisKelamin; }
    public String getProgramStudi() { return programStudi; }
    public int getSemester() { return semester; }
    public int getTahunAngkatan() { return tahunAngkatan; }
    public String getAsalSekolah() { return asalSekolah; }
    public String getJurusanAsal() { return jurusanAsal; }
    public String getPengalamanTeknologi() { return pengalamanTeknologi; }
    public String getMinatAwal() { return minatAwal; }
    public String getAlasanMinat() { return alasanMinat; }
    public String getProyek() { return proyek; }
    public String getFotoPath() { return fotoPath; }

    public void perbarui(String nama, String jenisKelamin, String programStudi,
                         int semester, int tahunAngkatan, String asalSekolah, String jurusanAsal,
                         String pengalamanTeknologi, String minatAwal, String alasanMinat,
                         String proyek, String fotoPath) {
        this.nama = nama;
        this.jenisKelamin = jenisKelamin;
        this.programStudi = programStudi;
        this.semester = semester;
        this.tahunAngkatan = tahunAngkatan;
        this.asalSekolah = asalSekolah;
        this.jurusanAsal = jurusanAsal;
        this.pengalamanTeknologi = pengalamanTeknologi;
        this.minatAwal = minatAwal;
        this.alasanMinat = alasanMinat;
        this.proyek = proyek;
        this.fotoPath = fotoPath;
    }
}
