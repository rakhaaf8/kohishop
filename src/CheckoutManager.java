import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CheckoutManager {

    public void tampilkanRincianPesanan(List<OrderItem> pesananUser, boolean isFinalReceipt) {
        if (!isFinalReceipt) {
            System.out.println("\n============= RINCIAN PESANAN SEMENTARA =============");
        }
        
        List<OrderItem> listMakanan = new ArrayList<>();
        List<OrderItem> listMinuman = new ArrayList<>();
        
        for (OrderItem item : pesananUser) {
            if (item.getMenu() instanceof Makanan) listMakanan.add(item);
            else listMinuman.add(item);
        }
        
        cetakKategoriRincian("MAKANAN", listMakanan);
        cetakKategoriRincian("MINUMAN", listMinuman);
    }

    private void cetakKategoriRincian(String judul, List<OrderItem> list) {
        if (list.isEmpty()) return;
        
        System.out.println("\nKATEGORI: " + judul);
        System.out.printf("%-35s | %-12s | %-12s | %-12s\n", "[Nama+Kode+Qty+Harga/pcs]", "Total Harga", "Total Pajak", "Subtotal");
        System.out.println("----------------------------------------------------------------------------------");
        
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
            
            System.out.printf("%-35s | %-12.2f | %-12.2f | %-12.2f\n", col1, col2, col3, col4);
        }
        System.out.println("----------------------------------------------------------------------------------");
        System.out.printf("%-35s | %-12.2f | %-12.2f | %-12.2f\n", "TOTAL " + judul, sumHarga, sumPajak, sumSubtotal);
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
            if (input.equalsIgnoreCase("CC")) { System.exit(0); }
            
            try {
                int pilihan = Integer.parseInt(input);
                switch (pilihan) {
                    case 1: return new USD();
                    case 2: return new JPY();
                    case 3: return new MYR();
                    case 4: return new EUR();
                    default: System.out.println("Pilihan tidak valid.");
                }
            } catch (Exception e) { System.out.println("Error: Input harus angka!"); }
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
            if (input.equalsIgnoreCase("CC")) { System.exit(0); }
            
            try {
                int pilihan = Integer.parseInt(input);
                switch (pilihan) {
                    case 1: return new Tunai();
                    case 2: return new QRIS();
                    case 3: return new EMoney();
                    default: System.out.println("Pilihan tidak valid.");
                }
            } catch (Exception e) { System.out.println("Error: Input harus angka!"); }
        }
    }

    public void prosesTransaksiFinal(Scanner sc, List<OrderItem> pesananUser, CurrencyConverter mataUang, PaymentChannel pembayaran) {
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
            if (inputBayar.equalsIgnoreCase("CC")) { System.exit(0); }
            
            try {
                bayar = Double.parseDouble(inputBayar);
                if (bayar < tagihanValas) {
                    System.out.println("Nominal kurang!");
                    attempts++;
                } else {
                    success = true; break;
                }
            } catch (Exception e) {
                System.out.println("Error: Input harus angka!");
                attempts++;
            }
        }
        
        if (!success) {
            System.out.println("\nPesanan Dibatalkan");
            System.exit(0);
        }
        
        System.out.println("\n==================================================================================");
        System.out.println("                               PEMBAYARAN SUKSES");
        System.out.println("==================================================================================");
        tampilkanRincianPesanan(pesananUser, true); 
        
        System.out.println("\n--- RINGKASAN TRANSAKSI ---");
        System.out.printf("Total SEBELUM pajak & diskon (IDR) : %.2f\n", totalSebelumPajakDiskonIDR);
        System.out.printf("Total SETELAH pajak & diskon (IDR) : %.2f\n", totalSetelahPajakDiskonIDR);
        System.out.printf("\nMetode Pembayaran: %s\n", pembayaran.getNamaChannel());
        System.out.printf("- Diskon : IDR %.2f\n", diskon);
        System.out.printf("- Admin  : IDR %.2f\n", admin);
        System.out.println("\n--- KONVERSI MATA UANG (" + simbol + ") ---");
        System.out.printf("Total SEBELUM pajak & diskon : %.2f %s\n", mataUang.convertFromIdr(totalSebelumPajakDiskonIDR), simbol);
        System.out.printf("Total SETELAH pajak & diskon : %.2f %s\n", tagihanValas, simbol);
        
        if (bayar > tagihanValas) {
            System.out.printf("\nNOMINAL KEMBALIAN (%s): %.2f\n", simbol, (bayar - tagihanValas));
        }
        System.out.println("\nterima kasih dan silakan datang kembali");
        System.out.println("==================================================================================");
    }
}