public class OrderItem {
    private Menu menu;
    private int qty;

    public OrderItem(Menu menu, int qty) {
        this.menu = menu;
        this.qty = qty;
    }

    public Menu getMenu() { return menu; }
    public int getQty() { return qty; }
    
    public double getHargaNormalTotal() { return menu.getHarga() * qty; }
    public double getPajakTotal() { return menu.hitungPajak() * qty; }
    public double getSubtotal() { return getHargaNormalTotal() + getPajakTotal(); }
}