//import java.sql.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class DataRetriever {
//
//    private final Connection connection;
//
//    public DataRetriever() {
//        DBConnection dbConnection = new DBConnection();
//        this.connection = dbConnection.getConnection();
//    }
//
//
//    public Dish findDishById(Integer id) {
//        try (PreparedStatement ps = connection.prepareStatement(
//                """
//                    SELECT id, name, dish_type, selling_price
//                    FROM dish
//                    WHERE id = ?
//                """)) {
//            ps.setInt(1, id);
//            try (ResultSet rs = ps.executeQuery()) {
//                if (rs.next()) {
//                    Dish dish = new Dish();
//                    dish.setId(rs.getInt("id"));
//                    dish.setName(rs.getString("name"));
//                    dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
//                    dish.setSellingPrice(rs.getObject("selling_price") == null ? null : rs.getDouble("selling_price"));
//                    return dish;
//                }
//                throw new RuntimeException("Dish not found: " + id);
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//
//    public List<DishIngredient> findDishIngredientsByDishId(int dishId) throws SQLException {
//        List<DishIngredient> result = new ArrayList<>();
//
//        String sql = """
//            SELECT
//                di.id,
//                di.id_dish,
//                di.id_ingredient,
//                di.quantity_required,
//                di.unit,
//                i.id AS ingredient_id,
//                i.name AS ingredient_name,
//                i.price AS ingredient_price,
//                i.category AS ingredient_category
//            FROM dish_ingredient di
//            JOIN ingredient i ON di.id_ingredient = i.id
//            WHERE di.id_dish = ?
//            """;
//
//        try (PreparedStatement ps = connection.prepareStatement(sql)) {
//            ps.setInt(1, dishId);
//            try (ResultSet rs = ps.executeQuery()) {
//                while (rs.next()) {
//                    Ingredient ingredient = new Ingredient();
//                    ingredient.setId(rs.getInt("ingredient_id"));
//                    ingredient.setName(rs.getString("ingredient_name"));
//                    ingredient.setPrice(rs.getDouble("ingredient_price"));
//                    ingredient.setCategory(CategoryEnum.valueOf(rs.getString("ingredient_category")));
//
//                    DishIngredient di = new DishIngredient();
//                    di.setId(rs.getInt("id"));
//                    di.setDish(findDishById(dishId));
//                    di.setIngredient(ingredient);
//                    di.setQuantityRequired(rs.getDouble("quantity_required"));
//                    di.setUnitType(DishIngredient.UnitType.valueOf(rs.getString("unit")));
//
//                    result.add(di);
//                }
//            }
//        }
//        return result;
//    }
//
//
//    public double getDishCost(int dishId) throws SQLException {
//        List<DishIngredient> ingredients = findDishIngredientsByDishId(dishId);
//        double totalCost = 0.0;
//
//        for (DishIngredient di : ingredients) {
//            Double price =di.getIngredient().getPrice();
//            double quantity= (double) di.getQuantityRequired();
//
//            totalCost +=(price !=null ? price.doubleValue():0.0)* quantity;
//        }
//
//        return totalCost;
//    }
//
//
//    public double getGrossMargin(int dishId) throws SQLException {
//        Dish dish = findDishById(dishId);
//        if (dish == null) {
//            throw new IllegalArgumentException("Plat non trouvé : id " + dishId);
//        }
//
//        Double sellingPrice = dish.getSellingPrice();
//        if (sellingPrice == null) {
//            throw new IllegalStateException(
//                    "Le prix de vente du plat '" + dish.getName() + "' n'est pas défini (NULL)"
//            );
//        }
//
//        double cost = getDishCost(dishId);
//        return sellingPrice - cost;
//    }
//
//
//    public Dish saveDish(Dish toSave) {
//        String upsertSql = """
//            INSERT INTO dish (id, name, dish_type, selling_price)
//            VALUES (?, ?, ?::dish_type, ?)
//            ON CONFLICT (id) DO UPDATE
//            SET name = EXCLUDED.name,
//                dish_type = EXCLUDED.dish_type,
//                selling_price = EXCLUDED.selling_price
//            RETURNING id
//        """;
//
//        try (Connection conn = new DBConnection().getConnection()) {
//            conn.setAutoCommit(false);
//            Integer dishId;
//
//            try (PreparedStatement ps = conn.prepareStatement(upsertSql)) {
//                if (toSave.getId() != null) {
//                    ps.setInt(1, toSave.getId());
//                } else {
//                    ps.setInt(1, getNextSerialValue(conn, "dish", "id"));
//                }
//                ps.setString(2, toSave.getName());
//                ps.setString(3, toSave.getDishType().name());
//                Double sellingPrice = 0.0;
//                if (sellingPrice != null) {
//                    ps.setDouble(4, sellingPrice);  // ← .doubleValue() pour convertir Double en double
//                } else {
//                    ps.setNull(4, Types.DOUBLE);
//                }
//
//                try (ResultSet rs = ps.executeQuery()) {
//                    rs.next();
//                    dishId = rs.getInt(1);
//                }
//            }
//
//            detachIngredients(conn, dishId);
//            attachIngredients(conn, dishId, toSave.getDishIngredients());
//
//            conn.commit();
//            return findDishById(dishId);
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private void detachIngredients(Connection conn, Integer dishId) throws SQLException {
//        try (PreparedStatement ps = conn.prepareStatement(
//                "DELETE FROM dish_ingredient WHERE id_dish = ?")) {
//            ps.setInt(1, dishId);
//            ps.executeUpdate();
//        }
//    }
//
//    private void attachIngredients(Connection conn, Integer dishId, List<DishIngredient> dishIngredients)
//            throws SQLException {
//        if (dishIngredients == null || dishIngredients.isEmpty()) return;
//
//        String sql = """
//            INSERT INTO dish_ingredient (id_dish, id_ingredient, quantity_required, unit)
//            VALUES (?, ?, ?, ?)
//        """;
//
//        try (PreparedStatement ps = conn.prepareStatement(sql)) {
//            for (DishIngredient di : dishIngredients) {
//                ps.setInt(1, dishId);
//                ps.setInt(2, di.getIngredient().getId());
//                ps.setDouble(3, (Double) di.getQuantityRequired());
//                ps.setString(4, di.getUnitType().name());
//                ps.addBatch();
//            }
//            ps.executeBatch();
//        }
//    }
//
//
//    private String getSerialSequenceName(Connection conn, String tableName, String columnName)
//            throws SQLException {
//        String sql = "SELECT pg_get_serial_sequence(?, ?)";
//
//        try (PreparedStatement ps = conn.prepareStatement(sql)) {
//            ps.setString(1, tableName);
//            ps.setString(2, columnName);
//
//            try (ResultSet rs = ps.executeQuery()) {
//                if (rs.next()) {
//                    return rs.getString(1);
//                }
//            }
//        }
//        return null;
//    }
//
//    private int getNextSerialValue(Connection conn, String tableName, String columnName)
//            throws SQLException {
//        String sequenceName = getSerialSequenceName(conn, tableName, columnName);
//        if (sequenceName == null) {
//            throw new IllegalArgumentException(
//                    "Any sequence found for " + tableName + "." + columnName
//            );
//        }
//        updateSequenceNextValue(conn, tableName, columnName, sequenceName);
//
//        String nextValSql = "SELECT nextval(?)";
//
//        try (PreparedStatement ps = conn.prepareStatement(nextValSql)) {
//            ps.setString(1, sequenceName);
//            try (ResultSet rs = ps.executeQuery()) {
//                rs.next();
//                return rs.getInt(1);
//            }
//        }
//    }
//
//    private void updateSequenceNextValue(Connection conn, String tableName, String columnName, String sequenceName)
//            throws SQLException {
//        String setValSql = String.format(
//                "SELECT setval('%s', (SELECT COALESCE(MAX(%s), 0) FROM %s))",
//                sequenceName, columnName, tableName
//        );
//
//        try (PreparedStatement ps = conn.prepareStatement(setValSql)) {
//            ps.executeQuery();
//        }
//    }
//}


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    /**
     * Récupère un plat par son ID avec tous ses ingrédients
     */
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

                Object priceObj = resultSet.getObject("dish_price");
                dish.setPrice(priceObj == null ? null : resultSet.getDouble("dish_price"));

                // Récupère les ingrédients associés
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

    /**
     * Sauvegarde ou met à jour un plat
     */
    Dish saveDish(Dish toSave) {
        String upsertDishSql = """
                INSERT INTO dish (id, price, name, dish_type)
                VALUES (?, ?, ?, ?::dish_type)
                ON CONFLICT (id) DO UPDATE
                SET name = EXCLUDED.name,
                    dish_type = EXCLUDED.dish_type,
                    price = EXCLUDED.price
                RETURNING id
                """;

        try (Connection conn = new DBConnection().getConnection()) {
            conn.setAutoCommit(false);
            Integer dishId;

            try (PreparedStatement ps = conn.prepareStatement(upsertDishSql)) {
                if (toSave.getId() != null) {
                    ps.setInt(1, toSave.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "dish", "id"));
                }

                if (toSave.getPrice() != null) {
                    ps.setDouble(2, toSave.getPrice());
                } else {
                    ps.setNull(2, Types.DOUBLE);
                }

                ps.setString(3, toSave.getName());
                ps.setString(4, toSave.getDishType().name());

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    dishId = rs.getInt(1);
                }
            }

            List<DishIngredient> newDishIngredients = toSave.getDishIngredients();
            detachIngredients(conn, dishId);
            attachIngredients(conn, dishId, newDishIngredients);

            conn.commit();
            return findDishById(dishId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Crée plusieurs ingrédients
     */
    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        if (newIngredients == null || newIngredients.isEmpty()) {
            return List.of();
        }

        List<Ingredient> savedIngredients = new ArrayList<>();
        DBConnection dbConnection = new DBConnection();
        Connection conn = dbConnection.getConnection();

        try {
            conn.setAutoCommit(false);
            String insertSql = """
                    INSERT INTO ingredient (id, name, category, price)
                    VALUES (?, ?, ?::ingredient_category, ?)
                    RETURNING id
                    """;

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (Ingredient ingredient : newIngredients) {
                    if (ingredient.getId() != null) {
                        ps.setInt(1, ingredient.getId());
                    } else {
                        ps.setInt(1, getNextSerialValue(conn, "ingredient", "id"));
                    }
                    ps.setString(2, ingredient.getName());
                    ps.setString(3, ingredient.getCategory().name());
                    ps.setDouble(4, ingredient.getPrice());

                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        int generatedId = rs.getInt(1);
                        ingredient.setId(generatedId);
                        savedIngredients.add(ingredient);
                    }
                }
                conn.commit();
                return savedIngredients;
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    /**
     * Détache tous les ingrédients d'un plat
     */
    private void detachIngredients(Connection conn, Integer dishId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM dish_ingredient WHERE id_dish = ?")) {
            ps.setInt(1, dishId);
            ps.executeUpdate();
        }
    }

    /**
     * Attache des ingrédients à un plat avec quantités
     */
    private void attachIngredients(Connection conn, Integer dishId,
                                   List<DishIngredient> dishIngredients) throws SQLException {
        if (dishIngredients == null || dishIngredients.isEmpty()) {
            return;
        }

        String attachSql = """
                INSERT INTO dish_ingredient (id_dish, id_ingredient, quantity_required, unit) 
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(attachSql)) {
            for (DishIngredient dishIngredient : dishIngredients) {
                ps.setInt(1, dishId);
                ps.setInt(2, dishIngredient.getIngredient().getId());
                ps.setDouble(3, dishIngredient.getQuantity());
                ps.setString(4, dishIngredient.getUnitType().name());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Récupère tous les ingrédients d'un plat avec leurs quantités
     */
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

    /**
     * Calcule le coût d'un plat
     */
    public double getDishCost(int id) {
        Dish dish = findDishById(id);
        return dish.getDishCost();
    }

    /**
     * Calcule la marge brute d'un plat
     */
    public double getGrossMargin(int id) {
        Dish dish = findDishById(id);
        return dish.getGrossMargin();
    }

    // ============ Méthodes utilitaires ============

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