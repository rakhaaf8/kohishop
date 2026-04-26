import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class OrderManager implements WarnaTerminal {
    private List<OrderItem> pesananUser;
    private MenuManager menuManager;

    public OrderManager(MenuManager menuManager) {
        this.pesananUser = new ArrayList<>();
        this.menuManager = menuManager;
    }

    public List<OrderItem> getPesananUser() {
        return pesananUser;
    }

    public boolean isPesananKosong() {
        return pesananUser.isEmpty();
    }

    public void prosesPemesanan(Scanner sc) {
        while (true) {
            System.out.print("\nMasukkan Kode Menu (ketik 'DONE' jika selesai, 'CC' batal): ");
            String inputKode = sc.nextLine().trim();
            
            if (inputKode.equalsIgnoreCase("CC")) {
                System.out.println(RED+"Pesanan Dibatalkan. Program Berhenti."+RESET);
                System.exit(0);
            }
            if (inputKode.equalsIgnoreCase("DONE")) break;
            
            Menu menuPilihan = menuManager.cariMenu(inputKode);
            if (menuPilihan == null) {
                System.out.println(RED+"Error: Kode menu tidak ditemukan!"+RESET);
                continue;
            }

            OrderItem itemExist = null;
            int countMinuman = 0;
            int countMakanan = 0;

            for (OrderItem item : pesananUser) {
                if (item.getMenu().getKode().equalsIgnoreCase(menuPilihan.getKode())) {
                    itemExist = item;
                }
                
                if (item.getMenu() instanceof Minuman) countMinuman++;
                if (item.getMenu() instanceof Makanan) countMakanan++;
            }

            if (itemExist == null) {
                if (menuPilihan instanceof Minuman && countMinuman >= 5) {
                    System.out.println(RED+"Error: Anda sudah memesan 5 jenis minuman yang berbeda! (Maks 5 jenis)"+RESET);
                    continue;
                }
                if (menuPilihan instanceof Makanan && countMakanan >= 5) {
                    System.out.println(RED+"Error: Anda sudah memesan 5 jenis makanan yang berbeda! (Maks 5 jenis)"+RESET);
                    continue;
                }
            }

            int currentQty = (itemExist != null) ? itemExist.getQty() : 0;
            int maxPorsi = (menuPilihan instanceof Minuman) ? 3 : 2;
            int sisaKuota = maxPorsi - currentQty;

            if (sisaKuota <= 0) {
                System.out.println(RED+"Error: Kuantitas untuk " + menuPilihan.getNama() + " sudah mencapai batas maksimal (" + maxPorsi + " porsi)."+RESET);
                continue;
            }

            int qty = 0;
            while (true) {
                System.out.print("Kuantitas untuk " + menuPilihan.getNama() + " (Maks " + sisaKuota + ") (Enter=1, 'S'/'0'=skip, 'CC'=batal): ");
                String qtyInput = sc.nextLine().trim();
                
                if (qtyInput.equalsIgnoreCase("CC")) {
                    System.out.println(RED+"Pesanan Dibatalkan. Program Berhenti."+RESET);
                    System.exit(0);
                }
                
                if (qtyInput.equalsIgnoreCase("S") || qtyInput.equals("0")) {
                    qty = 0; break;
                }
                
                if (qtyInput.isEmpty()) {
                    qty = 1;
                } else {
                    try {
                        qty = Integer.parseInt(qtyInput);
                    } catch (Exception e) {
                        System.out.println(RED+"Error: Input harus berupa angka!"+RESET);
                        continue;
                    }
                }
                
                if (qty < 0) { 
                    System.out.println(RED+"Error: Tidak boleh negatif."+RESET); 
                    continue; 
                }
                
                if (qty > sisaKuota) {
                    System.out.println(RED+"Error: Kuantitas melebihi batas! Sisa kuota Anda untuk menu ini adalah: " + sisaKuota+RESET);
                    
                    continue;
                }
                
                break; 
            }
            
            if (qty > 0) {
                if (itemExist != null) {
                    itemExist.tambahQty(qty);
                } else {
                    pesananUser.add(new OrderItem(menuPilihan, qty));
                }
            }

            String formatKotak = "%-55s";
            System.out.println();
            
            String judulPesanan = "         --- PESANAN SEMENTARA SAAT INI ---" ;
            System.out.printf(BG_WHITE + BLACK_TEXT + String.format(formatKotak, judulPesanan) + RESET + "\n");
            

            String headerPesanan = String.format("%-5s | %-35s | %-5s", "KODE", "NAMA", "QTY");
            System.out.printf(BG_WHITE + BLACK_TEXT + String.format(formatKotak, headerPesanan) + RESET + "\n");
            
            for (OrderItem item : pesananUser) {
                String barisPesanan = String.format("%-5s | %-35s | %-5d", item.getMenu().getKode(), item.getMenu().getNama(), item.getQty());
                System.out.printf(BG_MGM_YELLOW + BLACK_TEXT + String.format(formatKotak, barisPesanan) + RESET + "\n");
            }
        }
    }
}