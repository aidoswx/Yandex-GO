package yandexgo;

public enum Tariff {
    ECONOMY(500, 100), COMFORT(800, 150), BUSINESS(1500, 250);

    private final int basePrice;
    private final int pricePerKm;

    Tariff(int basePrice, int pricePerKm) {
        this.basePrice = basePrice;
        this.pricePerKm = pricePerKm;
    }

    public int getBasePrice() { return basePrice; }
    public int getPricePerKm() { return pricePerKm; }
}
