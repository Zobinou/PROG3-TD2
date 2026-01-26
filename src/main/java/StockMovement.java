import java.time.Instant;
import java.util.Objects;

public class StockMovement {
    private Integer id;
    private Ingredient ingredient;
    private Double quantity;
    private UnitType unit;
    private Instant movementDate;

    public StockMovement() {
    }

    public StockMovement(Integer id, Ingredient ingredient, Double quantity, UnitType unit, Instant movementDate) {
        this.id = id;
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.unit = unit;
        this.movementDate = movementDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public UnitType getUnit() {
        return unit;
    }

    public void setUnit(UnitType unit) {
        this.unit = unit;
    }

    public Instant getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(Instant movementDate) {
        this.movementDate = movementDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockMovement that = (StockMovement) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "StockMovement{" +
                "id=" + id +
                ", ingredient=" + (ingredient != null ? ingredient.getName() : "null") +
                ", quantity=" + quantity +
                ", unit=" + unit +
                ", movementDate=" + movementDate +
                '}';
    }

    public enum UnitType {
        KG, L, PCS
    }
}