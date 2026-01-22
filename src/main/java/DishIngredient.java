import java.net.ProtocolFamily;

public class DishIngredient {
    private Integer id;
    private Dish dish;
    private Integer ingredient;
    private double quantityRequired;
    private UnitType unitType;

    public void setIngredient(Ingredient ingredient) {

    }

    public void setDish(Dish dish) {

    }

    public void setId(int id) {

    }

    public Dish getIngredient() {
        return null;
    }

    public double getQuantity() {
        return 0;
    }

    public ProtocolFamily getUnitType() {
        return null;
    }

    public void setQuantity(int quantityRequire) {

    }

    public void setUnitType(UnitType unit) {

    }

    public enum UnitType {
        KG,L,PCS
    }
}
