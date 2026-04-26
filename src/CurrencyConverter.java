public interface CurrencyConverter {
    double convertFromIdr(double idrAmount);
    String getSymbol();
}

class USD implements CurrencyConverter {
    @Override
    public double convertFromIdr(double idrAmount) { return idrAmount / 15.0; }
    @Override
    public String getSymbol() { return "USD"; }
}

class JPY implements CurrencyConverter {
    @Override
    public double convertFromIdr(double idrAmount) { return idrAmount * 10.0; }
    @Override
    public String getSymbol() { return "JPY"; }
}

class MYR implements CurrencyConverter {
    @Override
    public double convertFromIdr(double idrAmount) { return idrAmount / 4.0; }
    @Override
    public String getSymbol() { return "MYR"; }
}

class EUR implements CurrencyConverter {
    @Override
    public double convertFromIdr(double idrAmount) { return idrAmount / 14.0; }
    @Override
    public String getSymbol() { return "EUR"; }
}