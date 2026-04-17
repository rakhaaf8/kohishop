import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class OrderManager {
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
                System.out.println("Pesanan Dibatalkan. Program Berhenti.");
                System.exit(0);
            }
            if (inputKode.equalsIgnoreCase("DONE")) break;
            
            Menu menuPilihan = menuManager.cariMenu(inputKode);
            if (menuPilihan == null) {
                System.out.println("Error: Kode menu tidak ditemukan!");
                continue;
            }
            
            int qty = 0;
            while (true) {
                System.out.print("Kuantitas untuk " + menuPilihan.getNama() + " (Enter=1, 'S'/'0'=skip, 'CC'=batal): ");
                String qtyInput = sc.nextLine().trim();
                
                if (qtyInput.equalsIgnoreCase("CC")) {
                    System.out.println("Pesanan Dibatalkan. Program Berhenti.");
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
                        System.out.println("Error: Input harus berupa angka!");
                        continue;
                    }
                }
                
                if (qty < 0) { System.out.println("Error: Tidak boleh negatif."); continue; }
                if (menuPilihan instanceof Minuman && qty > 3) { System.out.println("Error: Minuman maksimal 3!"); continue; }
                if (menuPilihan instanceof Makanan && qty > 2) { System.out.println("Error: Makanan maksimal 2!"); continue; }
                
                break; 
            }
            
            if (qty > 0) {
                pesananUser.add(new OrderItem(menuPilihan, qty));
            }
            
            System.out.println("\n--- Pesanan Sementara Saat Ini ---");
            System.out.printf("%-5s | %-35s | %s\n", "KODE", "NAMA", "QTY");
            for (OrderItem item : pesananUser) {
                System.out.printf("%-5s | %-35s | %d\n", item.getMenu().getKode(), item.getMenu().getNama(), item.getQty());
            }
        }
    }
}