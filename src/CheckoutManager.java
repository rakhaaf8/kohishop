import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CheckoutManager implements WarnaTerminal {
    public void tampilkanRincianPesanan(List<OrderItem> pesananUser, boolean isFinalReceipt) {
        String formatKotak = "%-82s";

        if (!isFinalReceipt) {
            System.out.println();
            String judul = "                      --- RINCIAN PESANAN SEMENTARA ---";
            System.out.printf(BG_WHITE + BLACK_TEXT + String.format(formatKotak, judul) + RESET + "\n");
        }

        List<OrderItem> listMakanan = new ArrayList<>();
        List<OrderItem> listMinuman = new ArrayList<>();

        for (OrderItem item : pesananUser) {
            if (item.getMenu() instanceof Makanan)
                listMakanan.add(item);
            else
                listMinuman.add(item);
        }

        cetakKategoriRincian("MAKANAN", listMakanan, isFinalReceipt);
        cetakKategoriRincian("MINUMAN", listMinuman, isFinalReceipt);
    }

    private void cetakKategoriRincian(String judul, List<OrderItem> list, boolean isFinalReceipt) {
        if (list.isEmpty())
            return;

        String formatKotak = "%-82s";

        String bgList = isFinalReceipt ? BG_WHITE : BG_MGM_YELLOW;

        String katHeader = "KATEGORI: " + judul;
        System.out.printf(BG_WHITE + BLACK_TEXT + String.format(formatKotak, katHeader) + RESET + "\n");

        String colHeader = String.format("%-35s | %-12s | %-12s | %-12s", "[Nama+Kode+Qty+Harga/pcs]", "Total Harga",
                "Total Pajak", "Subtotal");
        System.out.printf(BG_WHITE + BLACK_TEXT + String.format(formatKotak, colHeader) + RESET + "\n");

        String garis = "----------------------------------------------------------------------------------";
        System.out.printf(bgList + BLACK_TEXT + String.format(formatKotak, garis) + RESET + "\n");

        double sumHarga = 0, sumPajak = 0, sumSubtotal = 0;

        for (OrderItem item : list) {
            Menu m = item.getMenu();
            String col1 = String.format("%s (%s) x%d @%.0f", m.getNama(), m.getKode(), item.getQty(), m.getHarga());
            double col2 = item.getHargaNormalTotal();
            double col3 = item.getPajakTotal();
            double col4 = item.getSubtotal();

            sumHarga += col2;
            sumPajak += col3;
            sumSubtotal += col4;

            String rowData = String.format("%-35s | %-12.2f | %-12.2f | %-12.2f", col1, col2, col3, col4);
            System.out.printf(bgList + BLACK_TEXT + String.format(formatKotak, rowData) + RESET + "\n");
        }

        System.out.printf(bgList + BLACK_TEXT + String.format(formatKotak, garis) + RESET + "\n");
        String totalRow = String.format("%-35s | %-12.2f | %-12.2f | %-12.2f", "TOTAL " + judul, sumHarga, sumPajak,
                sumSubtotal);
        System.out.printf(bgList + BLACK_TEXT + String.format(formatKotak, totalRow) + RESET + "\n");
    }

    public CurrencyConverter pilihMataUang(Scanner sc) {
        System.out.println("\n--- Pilih Mata Uang Pembayaran ---");
        System.out.println("1. USD (1 USD = 15 IDR)");
        System.out.println("2. JPY (10 JPY = 1 IDR)");
        System.out.println("3. MYR (1 MYR = 4 IDR)");
        System.out.println("4. EUR (1 EUR = 14 IDR)");

        while (true) {
            System.out.print("Masukkan pilihan (1-4, 'CC' batal): ");
            String input = sc.nextLine().trim();
            if (input.equalsIgnoreCase("CC")) {
                System.exit(0);
            }

            try {
                int pilihan = Integer.parseInt(input);
                switch (pilihan) {
                    case 1:
                        return new USD();
                    case 2:
                        return new JPY();
                    case 3:
                        return new MYR();
                    case 4:
                        return new EUR();
                    default:
                        System.out.println(RED + "Pilihan tidak valid." + RESET);
                }
            } catch (Exception e) {
                System.out.println(RED + "Error: Input harus angka!" + RESET);
            }
        }
    }

    public PaymentChannel pilihChannelPembayaran(Scanner sc) {
        System.out.println("\n--- Pilih Channel Pembayaran ---");
        System.out.println("1. Tunai (Diskon 0%)");
        System.out.println("2. QRIS (Diskon 5%)");
        System.out.println("3. eMoney (Diskon 7%, Admin 20 IDR)");

        while (true) {
            System.out.print("Masukkan pilihan (1-3, 'CC' batal): ");
            String input = sc.nextLine().trim();
            if (input.equalsIgnoreCase("CC")) {
                System.exit(0);
            }

            try {
                int pilihan = Integer.parseInt(input);
                switch (pilihan) {
                    case 1:
                        return new Tunai();
                    case 2:
                        return new QRIS();
                    case 3:
                        return new EMoney();
                    default:
                        System.out.println(RED + "Pilihan tidak valid." + RESET);
                }
            } catch (Exception e) {
                System.out.println(RED + "Error: Input harus angka!" + RESET);
            }
        }
    }

    public void prosesTransaksiFinal(Scanner sc, List<OrderItem> pesananUser, CurrencyConverter mataUang,
            PaymentChannel pembayaran) {
        double totalSebelumPajakDiskonIDR = 0, totalPajakIDR = 0;

        for (OrderItem item : pesananUser) {
            totalSebelumPajakDiskonIDR += item.getHargaNormalTotal();
            totalPajakIDR += item.getPajakTotal();
        }

        double subtotalDenganPajakIDR = totalSebelumPajakDiskonIDR + totalPajakIDR;
        double diskon = pembayaran.hitungDiskon(subtotalDenganPajakIDR);
        double admin = pembayaran.getAdminFee();
        double totalSetelahPajakDiskonIDR = subtotalDenganPajakIDR - diskon + admin;

        double tagihanValas = mataUang.convertFromIdr(totalSetelahPajakDiskonIDR);
        String simbol = mataUang.getSymbol();

        System.out.println("\n=========================================");
        System.out.printf("TOTAL TAGIHAN SEMENTARA: %.2f %s\n", tagihanValas, simbol);
        System.out.println("=========================================");

        int attempts = 0;
        double bayar = 0;
        boolean success = false;

        while (attempts < 3) {
            System.out.printf("Masukkan nominal pembayaran (%s) atau 'CC' untuk batal: ", simbol);
            String inputBayar = sc.nextLine().trim();
            if (inputBayar.equalsIgnoreCase("CC")) {
                System.exit(0);
            }

            try {
                bayar = Double.parseDouble(inputBayar);
                if (bayar < tagihanValas) {
                    System.out.println(RED + "Nominal kurang!" + RESET);
                    attempts++;
                } else {
                    success = true;
                    break;
                }
            } catch (Exception e) {
                System.out.println(RED + "Error: Input harus angka!" + RESET);
                attempts++;
            }
        }

        if (!success) {
            System.out.println("\nPesanan Dibatalkan");
            System.exit(0);
        }

        String formatKotak82 = "%-82s";
        String garisBatas = "==================================================================================";

        System.out.println();
        System.out.printf(BG_WHITE + GREEN + String.format(formatKotak82, garisBatas) + RESET + "\n");
        String headerSukses = "                               PEMBAYARAN SUKSES";
        System.out.printf(BG_WHITE + GREEN + String.format(formatKotak82, headerSukses) + RESET + "\n");
        System.out.printf(BG_WHITE + GREEN + String.format(formatKotak82, garisBatas) + RESET + "\n");

        tampilkanRincianPesanan(pesananUser, true);

        System.out.printf(BG_WHITE + BLACK_TEXT + String.format(formatKotak82, "") + RESET + "\n");
        System.out.printf(
                BG_WHITE + BLACK_TEXT + String.format(formatKotak82, "--- RINGKASAN TRANSAKSI ---") + RESET + "\n");

        String strTotalSblm = String.format("Total SEBELUM pajak & diskon (IDR) : %.2f", totalSebelumPajakDiskonIDR);
        System.out.printf(BG_WHITE + BLACK_TEXT + String.format(formatKotak82, strTotalSblm) + RESET + "\n");

        String strTotalStlh = String.format("Total SETELAH pajak & diskon (IDR) : %.2f", totalSetelahPajakDiskonIDR);
        System.out.printf(BG_WHITE + BLACK_TEXT + String.format(formatKotak82, strTotalStlh) + RESET + "\n");

        System.out.printf(BG_WHITE + BLACK_TEXT + String.format(formatKotak82, "") + RESET + "\n");
        System.out.printf(BG_WHITE + BLACK_TEXT
                + String.format(formatKotak82, "Metode Pembayaran: " + pembayaran.getNamaChannel()) + RESET + "\n");

        String strDiskon = String.format("- Diskon : IDR %.2f", diskon);
        System.out.printf(BG_WHITE + BLACK_TEXT + String.format(formatKotak82, strDiskon) + RESET + "\n");

        String strAdmin = String.format("- Admin  : IDR %.2f", admin);
        System.out.printf(BG_WHITE + BLACK_TEXT + String.format(formatKotak82, strAdmin) + RESET + "\n");

        System.out.printf(BG_WHITE + BLACK_TEXT + String.format(formatKotak82, "") + RESET + "\n");
        System.out.printf(BG_WHITE + BLACK_TEXT
                + String.format(formatKotak82, "--- KONVERSI MATA UANG (" + simbol + ") ---") + RESET + "\n");

        String strKonvSblm = String.format("Total SEBELUM pajak & diskon : %.2f %s",
                mataUang.convertFromIdr(totalSebelumPajakDiskonIDR), simbol);
        System.out.printf(BG_WHITE + BLACK_TEXT + String.format(formatKotak82, strKonvSblm) + RESET + "\n");

        String strKonvStlh = String.format("Total SETELAH pajak & diskon : %.2f %s", tagihanValas, simbol);
        System.out.printf(BG_WHITE + BLACK_TEXT + String.format(formatKotak82, strKonvStlh) + RESET + "\n");

        System.out.printf(BG_WHITE + BLACK_TEXT + String.format(formatKotak82, "") + RESET + "\n");

        if (bayar > tagihanValas) {
            String strKembali = String.format("NOMINAL KEMBALIAN (%s): %.2f", simbol, (bayar - tagihanValas));
            System.out.printf(BG_WHITE + BLACK_TEXT + String.format(formatKotak82, strKembali) + RESET + "\n");
        }

        System.out.printf(BG_WHITE + BLACK_TEXT + String.format(formatKotak82, "") + RESET + "\n");

        String thanks = "TERIMA KASIH DAN SILAHKAN DATANG KEMBALI";
        System.out.printf(BG_WHITE + GREEN + String.format(formatKotak82, thanks) + RESET + "\n");
        System.out.printf(BG_WHITE + GREEN + String.format(formatKotak82, garisBatas) + RESET + "\n");
    }
}