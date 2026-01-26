import java.util.Objects;

public class Stock {
    private Integer id;
    private Ingredient ingredient;
    private Double quantity;
    private StockMovement.UnitType unit;

    public Stock() {
    }

    public Stock(Integer id, Ingredient ingredient, Double quantity, StockMovement.UnitType unit) {
        this.id = id;
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.unit = unit;
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

    public StockMovement.UnitType getUnit() {
        return unit;
    }

    public void setUnit(StockMovement.UnitType unit) {
        this.unit = unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock stock = (Stock) o;
        return Objects.equals(id, stock.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Stock{" +
                "id=" + id +
                ", ingredient=" + (ingredient != null ? ingredient.getName() : "null") +
                ", quantity=" + quantity +
                ", unit=" + unit +
                '}';
    }
}