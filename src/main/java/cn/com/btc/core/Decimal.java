package cn.com.btc.core;

public class Decimal {
    private final int price_decimal;
    private final int amount_decimal;

    public Decimal(int price_decimal, int amount_decimal) {
        this.price_decimal = price_decimal;
        this.amount_decimal = amount_decimal;
    }

    public int getPrice_decimal() {
        return price_decimal;
    }

    public int getAmount_decimal() {
        return amount_decimal;
    }

    @Override
    public String toString() {
        return "Decimal{" +
                "price_decimal=" + price_decimal +
                ", amount_decimal=" + amount_decimal +
                '}';
    }
}
