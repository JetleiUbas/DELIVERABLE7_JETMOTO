package model;

/** A motorcycle part sold in the JetMotoHub shop. */
public record Part(int partId, String partName, String category, String brand,
                   double price, int stock, String description) {
}
