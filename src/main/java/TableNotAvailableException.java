
import java.util.List;

public class TableNotAvailableException extends Exception {
    private List<Table> availableTables;

    public TableNotAvailableException(String message) {
        super(message);
    }

    public TableNotAvailableException(String message, List<Table> availableTables) {
        super(message);
        this.availableTables = availableTables;
    }

    public List<Table> getAvailableTables() {
        return availableTables;
    }

    public void setAvailableTables(List<Table> availableTables) {
        this.availableTables = availableTables;
    }
}