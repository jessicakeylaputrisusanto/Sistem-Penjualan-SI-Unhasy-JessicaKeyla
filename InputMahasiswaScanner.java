import java.util.Scanner;

public class InputMahasiswaScanner {
    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

        System.out.println("=== DATA MAHASISWA ===");

        System.out.print("Nama     : ");
        String nama = input.nextLine();

        System.out.print("NIM      : ");
        String nim = input.nextLine();

        System.out.print("Prodi    : ");
        String prodi = input.nextLine();

        System.out.print("Semester : ");
        int semester = input.nextInt();

        System.out.print("Nilai    : ");
        double nilai = input.nextDouble();

        System.out.println("\n=== DATA MAHASISWA ===");
        System.out.println("Nama     : " + nama);
        System.out.println("NIM      : " + nim);
        System.out.println("Prodi    : " + prodi);
        System.out.println("Semester : " + semester);
        System.out.println("Nilai    : " + nilai);

        input.close();
    }
}