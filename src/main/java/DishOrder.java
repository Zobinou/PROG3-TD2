
import java.util.Objects;

public class DishOrder {
    private Integer id;
    private Order order;
    private Dish dish;
    private Integer quantity;

    public DishOrder() {
    }

    public DishOrder(Integer id, Order order, Dish dish, Integer quantity) {
        this.id = id;
        this.order = order;
        this.dish = dish;
        this.quantity = quantity;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishOrder dishOrder = (DishOrder) o;
        return Objects.equals(id, dishOrder.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DishOrder{" +
                "id=" + id +
                ", dish=" + (dish != null ? dish.getName() : "null") +
                ", quantity=" + quantity +
                '}';
    }
}