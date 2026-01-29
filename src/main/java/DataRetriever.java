import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    //  MÉTHODES Dish et DishIngredient

    public Dish findDishById(Integer id) {
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

    // MÉTHODES Stock et Ingredient

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

            if (toSave.getStockMovementList() != null && !toSave.getStockMovementList().isEmpty()) {
                saveStockMovements(conn, ingredientId, toSave.getStockMovementList());
            }

            conn.commit();
            return findIngredientById(ingredientId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

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

    public Stock getStockValueAt(Integer ingredientId, Instant t) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();

        try {
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

    // MÉTHODES POUR LES TABLES DE RESTAURANT

    /**
     * Trouve une table par son ID
     */
    public Table findTableById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();

        try {
            PreparedStatement ps = connection.prepareStatement(
                    """
                    SELECT id, number
                    FROM restaurant_table
                    WHERE id = ?
                    """);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Table table = new Table();
                table.setId(rs.getInt("id"));
                table.setNumber(rs.getInt("number"));

                dbConnection.closeConnection(connection);
                return table;
            }

            dbConnection.closeConnection(connection);
            throw new RuntimeException("Table not found: " + id);
        } catch (SQLException e) {
            dbConnection.closeConnection(connection);
            throw new RuntimeException(e);
        }
    }

    /**
     * Vérifie si une table est disponible à une date et heure donnée
     * Une table est considérée comme occupée si elle a une commande active
     * (date d'installation <= date demandée < date de départ)
     */
    public boolean isTableAvailable(Connection conn, Integer tableId, Instant requestedDateTime)
            throws SQLException {
        String sql = """
                SELECT COUNT(*) as occupied_count
                FROM "order"
                WHERE id_table = ?
                AND client_installation_datetime <= ?
                AND (client_departure_datetime IS NULL OR client_departure_datetime > ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tableId);
            ps.setTimestamp(2, Timestamp.from(requestedDateTime));
            ps.setTimestamp(3, Timestamp.from(requestedDateTime));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("occupied_count") == 0;
                }
            }
        }
        return false;
    }

    /**
     * Récupère toutes les tables disponibles à une date et heure donnée
     */
    public List<Table> getAvailableTables(Connection conn, Instant requestedDateTime)
            throws SQLException {
        String sql = """
                SELECT rt.id, rt.number
                FROM restaurant_table rt
                WHERE rt.id NOT IN (
                    SELECT id_table
                    FROM "order"
                    WHERE client_installation_datetime <= ?
                    AND (client_departure_datetime IS NULL OR client_departure_datetime > ?)
                )
                ORDER BY rt.number
                """;

        List<Table> availableTables = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.from(requestedDateTime));
            ps.setTimestamp(2, Timestamp.from(requestedDateTime));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Table table = new Table();
                    table.setId(rs.getInt("id"));
                    table.setNumber(rs.getInt("number"));
                    availableTables.add(table);
                }
            }
        }

        return availableTables;
    }

    // MÉTHODES POUR LES COMMANDES Orders

    public Order saveOrder(Order orderToSave)
            throws InsufficientStockException, TableNotAvailableException {
        try (Connection conn = new DBConnection().getConnection()) {
            conn.setAutoCommit(false);

            // 1. Vérifier que la table est spécifiée
            if (orderToSave.getTable() == null || orderToSave.getTable().getId() == null) {
                conn.rollback();
                throw new RuntimeException("La table doit être spécifiée pour la commande");
            }

            // 2. Vérifier que la date d'installation est spécifiée
            if (orderToSave.getClientInstallationDateTime() == null) {
                conn.rollback();
                throw new RuntimeException("La date d'installation du client doit être spécifiée");
            }

            // 3. Vérifier la disponibilité de la table
            if (!isTableAvailable(conn, orderToSave.getTable().getId(),
                    orderToSave.getClientInstallationDateTime())) {

                // Récupérer les tables disponibles pour proposer des alternatives
                List<Table> availableTables = getAvailableTables(conn,
                        orderToSave.getClientInstallationDateTime());

                StringBuilder message = new StringBuilder();
                message.append("La table numéro ")
                        .append(findTableById(orderToSave.getTable().getId()).getNumber())
                        .append(" n'est pas disponible à la date et heure demandée.");

                if (availableTables.isEmpty()) {
                    message.append(" Aucune autre table n'est actuellement disponible.");
                } else {
                    message.append(" Tables disponibles: ");
                    for (int i = 0; i < availableTables.size(); i++) {
                        if (i > 0) {
                            message.append(", ");
                        }
                        message.append("Table ").append(availableTables.get(i).getNumber());
                    }
                }

                conn.rollback();
                throw new TableNotAvailableException(message.toString(), availableTables);
            }

            // 4. Vérifier le stock pour chaque plat de la commande
            checkStockAvailability(conn, orderToSave);

            // 5. Insérer la commande
            String insertOrderSql = """
                    INSERT INTO "order" (id, reference, creation_datetime, id_table, 
                                        client_installation_datetime, client_departure_datetime)
                    VALUES (?, ?, ?, ?, ?, ?)
                    RETURNING id
                    """;

            Integer orderId;
            try (PreparedStatement ps = conn.prepareStatement(insertOrderSql)) {
                if (orderToSave.getId() != null) {
                    ps.setInt(1, orderToSave.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "order", "id"));
                }
                ps.setString(2, orderToSave.getReference());
                ps.setTimestamp(3, Timestamp.from(orderToSave.getCreationDateTime()));
                ps.setInt(4, orderToSave.getTable().getId());
                ps.setTimestamp(5, Timestamp.from(orderToSave.getClientInstallationDateTime()));

                if (orderToSave.getClientDepartureDateTime() != null) {
                    ps.setTimestamp(6, Timestamp.from(orderToSave.getClientDepartureDateTime()));
                } else {
                    ps.setNull(6, Types.TIMESTAMP);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    orderId = rs.getInt(1);
                }
            }

            // 6. Insérer les DishOrder
            saveDishOrders(conn, orderId, orderToSave.getDishOrders());

            // 7. Mettre à jour les stocks (créer des mouvements négatifs)
            updateStockForOrder(conn, orderToSave);

            conn.commit();
            return findOrderByReference(orderToSave.getReference());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkStockAvailability(Connection conn, Order order)
            throws SQLException, InsufficientStockException {
        for (DishOrder dishOrder : order.getDishOrders()) {
            Dish dish = dishOrder.getDish();
            int quantityOrdered = dishOrder.getQuantity();

            // Pour chaque ingrédient du plat
            List<DishIngredient> ingredients = findIngredientsByDishId(dish.getId());
            for (DishIngredient di : ingredients) {
                double requiredQuantity = di.getQuantity() * quantityOrdered;

                // Vérifier le stock actuel
                double currentStock = getCurrentStock(conn, di.getIngredient().getId());

                if (currentStock < requiredQuantity) {
                    throw new InsufficientStockException(
                            "Stock insuffisant pour " + di.getIngredient().getName() +
                                    ". Requis: " + requiredQuantity + " KG, Disponible: " + currentStock + " KG"
                    );
                }
            }
        }
    }

    private double getCurrentStock(Connection conn, Integer ingredientId) throws SQLException {
        String sql = """
                SELECT s.quantity + COALESCE(SUM(sm.quantity), 0) as current_stock
                FROM stock s
                LEFT JOIN stock_movement sm ON s.id_ingredient = sm.id_ingredient
                WHERE s.id_ingredient = ?
                GROUP BY s.quantity
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ingredientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("current_stock");
                }
            }
        }
        return 0.0;
    }

    private void updateStockForOrder(Connection conn, Order order) throws SQLException {
        for (DishOrder dishOrder : order.getDishOrders()) {
            List<DishIngredient> ingredients = findIngredientsByDishId(dishOrder.getDish().getId());

            for (DishIngredient di : ingredients) {
                double quantityToDeduct = di.getQuantity() * dishOrder.getQuantity();

                // Créer un mouvement de stock négatif
                String insertMovement = """
                        INSERT INTO stock_movement (id_ingredient, quantity, unit, movement_date)
                        VALUES (?, ?, 'KG', ?)
                        """;

                try (PreparedStatement ps = conn.prepareStatement(insertMovement)) {
                    ps.setInt(1, di.getIngredient().getId());
                    ps.setDouble(2, -quantityToDeduct);
                    ps.setTimestamp(3, Timestamp.from(order.getCreationDateTime()));
                    ps.executeUpdate();
                }
            }
        }
    }

    private void saveDishOrders(Connection conn, Integer orderId,
                                List<DishOrder> dishOrders) throws SQLException {
        String insertSql = """
                INSERT INTO dish_order (id_order, id_dish, quantity)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            for (DishOrder dishOrder : dishOrders) {
                ps.setInt(1, orderId);
                ps.setInt(2, dishOrder.getDish().getId());
                ps.setInt(3, dishOrder.getQuantity());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public Order findOrderByReference(String reference) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();

        try {
            PreparedStatement ps = connection.prepareStatement(
                    """
                    SELECT id, reference, creation_datetime, id_table,
                           client_installation_datetime, client_departure_datetime
                    FROM "order"
                    WHERE reference = ?
                    """);
            ps.setString(1, reference);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setReference(rs.getString("reference"));
                order.setCreationDateTime(rs.getTimestamp("creation_datetime").toInstant());

                // Charger la table si présente
                int tableId = rs.getInt("id_table");
                if (!rs.wasNull()) {
                    order.setTable(findTableById(tableId));
                }

                // Charger les dates d'installation et de départ
                Timestamp installation = rs.getTimestamp("client_installation_datetime");
                if (installation != null) {
                    order.setClientInstallationDateTime(installation.toInstant());
                }

                Timestamp departure = rs.getTimestamp("client_departure_datetime");
                if (departure != null) {
                    order.setClientDepartureDateTime(departure.toInstant());
                }

                // Charger les DishOrder
                order.setDishOrders(findDishOrdersByOrderId(order.getId()));

                dbConnection.closeConnection(connection);
                return order;
            }

            dbConnection.closeConnection(connection);
            throw new RuntimeException("Order not found: " + reference);
        } catch (SQLException e) {
            dbConnection.closeConnection(connection);
            throw new RuntimeException(e);
        }
    }

    private List<DishOrder> findDishOrdersByOrderId(Integer orderId) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        List<DishOrder> dishOrders = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(
                    """
                    SELECT id, id_dish, quantity
                    FROM dish_order
                    WHERE id_order = ?
                    """);
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DishOrder dishOrder = new DishOrder();
                dishOrder.setId(rs.getInt("id"));
                dishOrder.setDish(findDishById(rs.getInt("id_dish")));
                dishOrder.setQuantity(rs.getInt("quantity"));
                dishOrders.add(dishOrder);
            }

            dbConnection.closeConnection(connection);
            return dishOrders;
        } catch (SQLException e) {
            dbConnection.closeConnection(connection);
            throw new RuntimeException(e);
        }
    }

    //  MÉTHODES UTILITAIRES

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