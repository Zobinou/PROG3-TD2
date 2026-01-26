import java.util.List;
import java.util.Objects;

public class Dish {
    private Integer id;
    private Double price; // prix de vente
    private String name;
    private DishTypeEnum dishType;
    private List<DishIngredient> dishIngredients;

    public Dish() {
    }

    public Dish(Integer id, String name, DishTypeEnum dishType) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DishTypeEnum getDishType() {
        return dishType;
    }

    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }

    public List<DishIngredient> getDishIngredients() {
        return dishIngredients;
    }

    public void setDishIngredients(List<DishIngredient> dishIngredients) {
        this.dishIngredients = dishIngredients;
    }

    /**
     * Calcule le coût total du plat en fonction des ingrédients et quantités requises
     */
    public Double getDishCost() {
        if (dishIngredients == null || dishIngredients.isEmpty()) {
            return 0.0;
        }

        double totalCost = 0.0;
        for (DishIngredient dishIngredient : dishIngredients) {
            // Vérifications avec gestion correcte des null
            if (dishIngredient.getIngredient() == null) {
                throw new RuntimeException("Ingrédient null");
            }

            Double ingredientPrice = dishIngredient.getIngredient().getPrice();
            if (ingredientPrice == null) {
                throw new RuntimeException("Prix de l'ingrédient null");
            }

            Double quantity = dishIngredient.getQuantity();
            if (quantity == null) {
                throw new RuntimeException("Quantité requise null");
            }

            totalCost += ingredientPrice * quantity;
        }

        return totalCost;
    }

    /**
     * Calcule la marge brute : prix de vente - coût des ingrédients
     */
    public Double getGrossMargin() {
        if (price == null) {
            throw new IllegalStateException("Le prix de vente n'est pas défini pour ce plat");
        }
        return price - getDishCost();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Dish dish = (Dish) o;
        return Objects.equals(id, dish.id) &&
                Objects.equals(name, dish.name) &&
                dishType == dish.dishType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, dishType);
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", price=" + price +
                ", name='" + name + '\'' +
                ", dishType=" + dishType +
                ", dishIngredients=" + dishIngredients +
                '}';
    }
}