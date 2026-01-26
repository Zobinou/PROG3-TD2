import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    // ========== MÉTHODES TD3 (Dish et DishIngredient) ==========

    Dish findDishById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                    SELECT dish.id as dish_id, dish.name as dish_name, 
                           dish_type, dish.price as dish_price
                    FROM dish
                    WHERE dish.id = ?
                    """);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Dish dish = new Dish();
                dish.setId(resultSet.getInt("dish_id"));
                dish.setName(resultSet.getString("dish_name"));
                dish.setDishType(DishTypeEnum.valueOf(resultSet.getString("dish_type")));

                double priceValue = resultSet.getDouble("dish_price");
                if (resultSet.wasNull()) {
                    dish.setPrice(null);
                } else {
                    dish.setPrice(priceValue);
                }

                dish.setDishIngredients(findIngredientsByDishId(id));

                dbConnection.closeConnection(connection);
                return dish;
            }

            dbConnection.closeConnection(connection);
            throw new RuntimeException("Dish not found: " + id);
        } catch (SQLException e) {
            dbConnection.closeConnection(connection);
            throw new RuntimeException(e);
        }
    }

    private List<DishIngredient> findIngredientsByDishId(Integer idDish) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        List<DishIngredient> dishIngredients = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                    SELECT di.id, di.quantity_required, di.unit,
                           i.id as i_id, i.name as i_name, 
                           i.category as i_category, i.price as i_price
                    FROM dish_ingredient di 
                    INNER JOIN ingredient i ON i.id = di.id_ingredient
                    WHERE di.id_dish = ?
                    """);
            preparedStatement.setInt(1, idDish);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(resultSet.getInt("i_id"));
                ingredient.setName(resultSet.getString("i_name"));
                ingredient.setPrice(resultSet.getDouble("i_price"));
                ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("i_category")));

                DishIngredient dishIngredient = new DishIngredient();
                dishIngredient.setId(resultSet.getInt("id"));
                dishIngredient.setIngredient(ingredient);
                dishIngredient.setQuantity(resultSet.getDouble("quantity_required"));
                dishIngredient.setUnitType(DishIngredient.UnitType.valueOf(resultSet.getString("unit")));

                dishIngredients.add(dishIngredient);
            }

            dbConnection.closeConnection(connection);
            return dishIngredients;
        } catch (SQLException e) {
            dbConnection.closeConnection(connection);
            throw new RuntimeException(e);
        }
    }

    public double getDishCost(int id) {
        Dish dish = findDishById(id);
        return dish.getDishCost();
    }

    public double getGrossMargin(int id) {
        Dish dish = findDishById(id);
        return dish.getGrossMargin();
    }

    // ========== MÉTHODES TD4 (Stock et Ingredient) ==========

    /**
     * TD4 - Question 2a: Sauvegarde un ingrédient avec ses mouvements de stock
     */
    public Ingredient saveIngredient(Ingredient toSave) {
        String upsertIngredientSql = """
                INSERT INTO ingredient (id, name, category, price)
                VALUES (?, ?, ?::ingredient_category, ?)
                ON CONFLICT (id) DO UPDATE
                SET name = EXCLUDED.name,
                    category = EXCLUDED.category,
                    price = EXCLUDED.price
                RETURNING id
                """;

        try (Connection conn = new DBConnection().getConnection()) {
            conn.setAutoCommit(false);
            Integer ingredientId;

            try (PreparedStatement ps = conn.prepareStatement(upsertIngredientSql)) {
                if (toSave.getId() != null) {
                    ps.setInt(1, toSave.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "ingredient", "id"));
                }
                ps.setString(2, toSave.getName());
                ps.setString(3, toSave.getCategory().name());
                ps.setDouble(4, toSave.getPrice());

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    ingredientId = rs.getInt(1);
                }
            }

            // Sauvegarder les mouvements de stock
            if (toSave.getStockMovementList() != null && !toSave.getStockMovementList().isEmpty()) {
                saveStockMovements(conn, ingredientId, toSave.getStockMovementList());
            }

            conn.commit();
            return findIngredientById(ingredientId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Récupère un ingrédient par son ID avec ses mouvements de stock
     */
    public Ingredient findIngredientById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();

        try {
            PreparedStatement ps = connection.prepareStatement(
                    """
                    SELECT id, name, category, price
                    FROM ingredient
                    WHERE id = ?
                    """);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt("id"));
                ingredient.setName(rs.getString("name"));
                ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
                ingredient.setPrice(rs.getDouble("price"));

                // Charger les mouvements de stock
                ingredient.setStockMovementList(findStockMovementsByIngredientId(id));

                dbConnection.closeConnection(connection);
                return ingredient;
            }

            dbConnection.closeConnection(connection);
            throw new RuntimeException("Ingredient not found: " + id);
        } catch (SQLException e) {
            dbConnection.closeConnection(connection);
            throw new RuntimeException(e);
        }
    }

    /**
     * Sauvegarde les mouvements de stock d'un ingrédient
     * Règle: ON CONFLICT DO NOTHING si l'id existe déjà
     */
    private void saveStockMovements(Connection conn, Integer ingredientId,
                                    List<StockMovement> movements) throws SQLException {
        String insertSql = """
                INSERT INTO stock_movement (id, id_ingredient, quantity, unit, movement_date)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (id) DO NOTHING
                """;

        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            for (StockMovement movement : movements) {
                if (movement.getId() != null) {
                    ps.setInt(1, movement.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "stock_movement", "id"));
                }
                ps.setInt(2, ingredientId);
                ps.setDouble(3, movement.getQuantity());
                ps.setString(4, movement.getUnit().name());
                ps.setTimestamp(5, Timestamp.from(movement.getMovementDate()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Récupère tous les mouvements de stock d'un ingrédient
     */
    private List<StockMovement> findStockMovementsByIngredientId(Integer ingredientId) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        List<StockMovement> movements = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(
                    """
                    SELECT id, quantity, unit, movement_date
                    FROM stock_movement
                    WHERE id_ingredient = ?
                    ORDER BY movement_date
                    """);
            ps.setInt(1, ingredientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                StockMovement movement = new StockMovement();
                movement.setId(rs.getInt("id"));
                movement.setQuantity(rs.getDouble("quantity"));
                movement.setUnit(StockMovement.UnitType.valueOf(rs.getString("unit")));
                movement.setMovementDate(rs.getTimestamp("movement_date").toInstant());

                movements.add(movement);
            }

            dbConnection.closeConnection(connection);
            return movements;
        } catch (SQLException e) {
            dbConnection.closeConnection(connection);
            throw new RuntimeException(e);
        }
    }

    /**
     * TD4 - Question 3: Calcule le niveau de stock d'un ingrédient à une date donnée
     */
    public Stock getStockValueAt(Integer ingredientId, Instant t) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();

        try {
            // Récupérer le stock initial
            PreparedStatement psStock = connection.prepareStatement(
                    """
                    SELECT quantity, unit
                    FROM stock
                    WHERE id_ingredient = ?
                    """);
            psStock.setInt(1, ingredientId);
            ResultSet rsStock = psStock.executeQuery();

            if (!rsStock.next()) {
                throw new RuntimeException("Stock initial not found for ingredient: " + ingredientId);
            }

            double initialQuantity = rsStock.getDouble("quantity");
            String unit = rsStock.getString("unit");

            // Calculer la somme des mouvements jusqu'à la date t
            PreparedStatement psMovements = connection.prepareStatement(
                    """
                    SELECT COALESCE(SUM(quantity), 0) as total_movements
                    FROM stock_movement
                    WHERE id_ingredient = ? AND movement_date <= ?
                    """);
            psMovements.setInt(1, ingredientId);
            psMovements.setTimestamp(2, Timestamp.from(t));
            ResultSet rsMovements = psMovements.executeQuery();

            double totalMovements = 0.0;
            if (rsMovements.next()) {
                totalMovements = rsMovements.getDouble("total_movements");
            }

            // Créer l'objet Stock avec la quantité finale
            Ingredient ingredient = findIngredientById(ingredientId);
            Stock stock = new Stock();
            stock.setIngredient(ingredient);
            stock.setQuantity(initialQuantity + totalMovements);
            stock.setUnit(StockMovement.UnitType.valueOf(unit));

            dbConnection.closeConnection(connection);
            return stock;
        } catch (SQLException e) {
            dbConnection.closeConnection(connection);
            throw new RuntimeException(e);
        }
    }

    // ========== MÉTHODES UTILITAIRES ==========

    private String getSerialSequenceName(Connection conn, String tableName, String columnName)
            throws SQLException {
        String sql = "SELECT pg_get_serial_sequence(?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    private int getNextSerialValue(Connection conn, String tableName, String columnName)
            throws SQLException {
        String sequenceName = getSerialSequenceName(conn, tableName, columnName);
        if (sequenceName == null) {
            throw new IllegalArgumentException(
                    "No sequence found for " + tableName + "." + columnName
            );
        }
        updateSequenceNextValue(conn, tableName, columnName, sequenceName);

        String nextValSql = "SELECT nextval(?)";
        try (PreparedStatement ps = conn.prepareStatement(nextValSql)) {
            ps.setString(1, sequenceName);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void updateSequenceNextValue(Connection conn, String tableName,
                                         String columnName, String sequenceName)
            throws SQLException {
        String setValSql = String.format(
                "SELECT setval('%s', (SELECT COALESCE(MAX(%s), 0) FROM %s))",
                sequenceName, columnName, tableName
        );
        try (PreparedStatement ps = conn.prepareStatement(setValSql)) {
            ps.executeQuery();
        }
    }
}