import java.util.Scanner;
public class KohiSopApp implements WarnaTerminal {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        MenuManager menuManager = new MenuManager();
        OrderManager orderManager = new OrderManager(menuManager);
        CheckoutManager checkoutManager = new CheckoutManager();

        System.out.println(CYAN+"====================================================="+RESET);
        System.out.printf(YELLOW+"%43s\n", "SELAMAT DATANG DI KOHISOP"+RESET);
        System.out.println(CYAN+"====================================================="+RESET);

        menuManager.tampilkanTabelMenu();
        
        orderManager.prosesPemesanan(sc);

        if (orderManager.isPesananKosong()) {
            System.out.println("Pesanan kosong. Program selesai.");
            return;
        }

        checkoutManager.tampilkanRincianPesanan(orderManager.getPesananUser(), false);

        CurrencyConverter mataUang = checkoutManager.pilihMataUang(sc);
        PaymentChannel pembayaran = checkoutManager.pilihChannelPembayaran(sc);

        checkoutManager.prosesTransaksiFinal(sc, orderManager.getPesananUser(), mataUang, pembayaran);
        
        sc.close();
    }
}