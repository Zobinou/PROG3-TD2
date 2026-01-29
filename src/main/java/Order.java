import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Order {
    private Integer id;
    private String reference;
    private Instant creationDateTime;
    private List<DishOrder> dishOrders;
    private Table table;
    private Instant clientInstallationDateTime;
    private Instant clientDepartureDateTime;

    public Order() {
        this.dishOrders = new ArrayList<>();
    }

    public Order(Integer id, String reference, Instant creationDateTime) {
        this.id = id;
        this.reference = reference;
        this.creationDateTime = creationDateTime;
        this.dishOrders = new ArrayList<>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Instant getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(Instant creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public List<DishOrder> getDishOrders() {
        return dishOrders;
    }

    public void setDishOrders(List<DishOrder> dishOrders) {
        this.dishOrders = dishOrders;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Instant getClientInstallationDateTime() {
        return clientInstallationDateTime;
    }

    public void setClientInstallationDateTime(Instant clientInstallationDateTime) {
        this.clientInstallationDateTime = clientInstallationDateTime;
    }

    public Instant getClientDepartureDateTime() {
        return clientDepartureDateTime;
    }

    public void setClientDepartureDateTime(Instant clientDepartureDateTime) {
        this.clientDepartureDateTime = clientDepartureDateTime;
    }

    /**
     * Calcule le montant total HT de la commande
     */
    public Double getTotalAmountExcludingTax() {
        if (dishOrders == null || dishOrders.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        for (DishOrder dishOrder : dishOrders) {
            if (dishOrder.getDish() != null && dishOrder.getDish().getPrice() != null) {
                total += dishOrder.getDish().getPrice() * dishOrder.getQuantity();
            }
        }
        return total;
    }

    /**
     * Calcule le montant TTC de la commande
     */
    public Double getTotalAmountIncludingTax() {
        // Pour le moment, on retourne le même montant (pas de TVA)
        return getTotalAmountExcludingTax();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id) &&
                Objects.equals(reference, order.reference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reference);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", creationDateTime=" + creationDateTime +
                ", table=" + (table != null ? "Table " + table.getNumber() : "null") +
                ", clientInstallationDateTime=" + clientInstallationDateTime +
                ", clientDepartureDateTime=" + clientDepartureDateTime +
                ", dishOrders=" + (dishOrders != null ? dishOrders.size() : 0) + " plat(s)" +
                '}';
    }
}