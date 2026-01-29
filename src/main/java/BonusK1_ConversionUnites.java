
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public class BonusK1_ConversionUnites {


    public static class UnitConversion {
        private Integer ingredientId;
        private String ingredientName;
        private DishIngredient.UnitType fromUnit;
        private DishIngredient.UnitType toUnit;
        private Double conversionFactor; // null signifie "impossible"

        public UnitConversion(Integer ingredientId, String ingredientName,
                              DishIngredient.UnitType fromUnit,
                              DishIngredient.UnitType toUnit,
                              Double conversionFactor) {
            this.ingredientId = ingredientId;
            this.ingredientName = ingredientName;
            this.fromUnit = fromUnit;
            this.toUnit = toUnit;
            this.conversionFactor = conversionFactor;
        }

        public boolean isConversionPossible() {
            return conversionFactor != null;
        }

        public Double convert(Double quantity) {
            if (!isConversionPossible()) {
                throw new IllegalStateException(
                        String.format("Conversion impossible de %s vers %s pour %s",
                                fromUnit, toUnit, ingredientName));
            }
            return quantity * conversionFactor;
        }

        // Getters
        public Integer getIngredientId() {
            return ingredientId;
        }
        public String getIngredientName() {
            return ingredientName;
        }
        public DishIngredient.UnitType getFromUnit() {
            return fromUnit;
        }
        public DishIngredient.UnitType getToUnit() {
            return toUnit;
        }
        public Double getConversionFactor() {
            return conversionFactor;

        }
    }

    /**
     * Base de données en mémoire des conversions d'unités
     * Selon le tableau du sujet:
     * - Tomate: 1 KG = 10 PCS, KG vers L impossible
     * - Laitue: 1 KG = 2 PCS, KG vers L impossible
     * - Chocolat: 1 KG = 10 PCS = 2,5 L
     * - Poulet: 1 KG = 8 PCS, KG vers L impossible
     * - Beurre: 1 KG = 4 PCS = 5 L
     */
    private static final List<UnitConversion> CONVERSION_TABLE = new ArrayList<>();

    static {
        // TOMATE (id=2): 1 KG = 10 PCS, KG↔L impossible
        CONVERSION_TABLE.add(new UnitConversion(2, "Tomate",
                DishIngredient.UnitType.KG, DishIngredient.UnitType.PCS, 10.0));
        CONVERSION_TABLE.add(new UnitConversion(2, "Tomate",
                DishIngredient.UnitType.PCS, DishIngredient.UnitType.KG, 0.1));
        CONVERSION_TABLE.add(new UnitConversion(2, "Tomate",
                DishIngredient.UnitType.KG, DishIngredient.UnitType.L, null)); // Impossible
        CONVERSION_TABLE.add(new UnitConversion(2, "Tomate",
                DishIngredient.UnitType.L, DishIngredient.UnitType.KG, null)); // Impossible
        CONVERSION_TABLE.add(new UnitConversion(2, "Tomate",
                DishIngredient.UnitType.PCS, DishIngredient.UnitType.L, null)); // Impossible
        CONVERSION_TABLE.add(new UnitConversion(2, "Tomate",
                DishIngredient.UnitType.L, DishIngredient.UnitType.PCS, null)); // Impossible

        // LAITUE (id=1): 1 KG = 2 PCS, KG↔L impossible
        CONVERSION_TABLE.add(new UnitConversion(1, "Laitue",
                DishIngredient.UnitType.KG, DishIngredient.UnitType.PCS, 2.0));
        CONVERSION_TABLE.add(new UnitConversion(1, "Laitue",
                DishIngredient.UnitType.PCS, DishIngredient.UnitType.KG, 0.5));
        CONVERSION_TABLE.add(new UnitConversion(1, "Laitue",
                DishIngredient.UnitType.KG, DishIngredient.UnitType.L, null)); // Impossible
        CONVERSION_TABLE.add(new UnitConversion(1, "Laitue",
                DishIngredient.UnitType.L, DishIngredient.UnitType.KG, null)); // Impossible
        CONVERSION_TABLE.add(new UnitConversion(1, "Laitue",
                DishIngredient.UnitType.PCS, DishIngredient.UnitType.L, null)); // Impossible
        CONVERSION_TABLE.add(new UnitConversion(1, "Laitue",
                DishIngredient.UnitType.L, DishIngredient.UnitType.PCS, null)); // Impossible

        // CHOCOLAT (id=4): 1 KG = 10 PCS = 2,5 L (toutes conversions possibles)
        CONVERSION_TABLE.add(new UnitConversion(4, "Chocolat",
                DishIngredient.UnitType.KG, DishIngredient.UnitType.PCS, 10.0));
        CONVERSION_TABLE.add(new UnitConversion(4, "Chocolat",
                DishIngredient.UnitType.PCS, DishIngredient.UnitType.KG, 0.1));
        CONVERSION_TABLE.add(new UnitConversion(4, "Chocolat",
                DishIngredient.UnitType.KG, DishIngredient.UnitType.L, 2.5));
        CONVERSION_TABLE.add(new UnitConversion(4, "Chocolat",
                DishIngredient.UnitType.L, DishIngredient.UnitType.KG, 0.4));
        CONVERSION_TABLE.add(new UnitConversion(4, "Chocolat",
                DishIngredient.UnitType.PCS, DishIngredient.UnitType.L, 0.25)); // 10 PCS = 2.5 L → 1 PCS = 0.25 L
        CONVERSION_TABLE.add(new UnitConversion(4, "Chocolat",
                DishIngredient.UnitType.L, DishIngredient.UnitType.PCS, 4.0)); // 1 L = 4 PCS

        // POULET (id=3): 1 KG = 8 PCS, KG↔L impossible
        CONVERSION_TABLE.add(new UnitConversion(3, "Poulet",
                DishIngredient.UnitType.KG, DishIngredient.UnitType.PCS, 8.0));
        CONVERSION_TABLE.add(new UnitConversion(3, "Poulet",
                DishIngredient.UnitType.PCS, DishIngredient.UnitType.KG, 0.125));
        CONVERSION_TABLE.add(new UnitConversion(3, "Poulet",
                DishIngredient.UnitType.KG, DishIngredient.UnitType.L, null)); // Impossible
        CONVERSION_TABLE.add(new UnitConversion(3, "Poulet",
                DishIngredient.UnitType.L, DishIngredient.UnitType.KG, null)); // Impossible
        CONVERSION_TABLE.add(new UnitConversion(3, "Poulet",
                DishIngredient.UnitType.PCS, DishIngredient.UnitType.L, null)); // Impossible
        CONVERSION_TABLE.add(new UnitConversion(3, "Poulet",
                DishIngredient.UnitType.L, DishIngredient.UnitType.PCS, null)); // Impossible

        // BEURRE (id=5): 1 KG = 4 PCS = 5 L (toutes conversions possibles)
        CONVERSION_TABLE.add(new UnitConversion(5, "Beurre",
                DishIngredient.UnitType.KG, DishIngredient.UnitType.PCS, 4.0));
        CONVERSION_TABLE.add(new UnitConversion(5, "Beurre",
                DishIngredient.UnitType.PCS, DishIngredient.UnitType.KG, 0.25));
        CONVERSION_TABLE.add(new UnitConversion(5, "Beurre",
                DishIngredient.UnitType.KG, DishIngredient.UnitType.L, 5.0));
        CONVERSION_TABLE.add(new UnitConversion(5, "Beurre",
                DishIngredient.UnitType.L, DishIngredient.UnitType.KG, 0.2));
        CONVERSION_TABLE.add(new UnitConversion(5, "Beurre",
                DishIngredient.UnitType.PCS, DishIngredient.UnitType.L, 1.25)); // 4 PCS = 5 L → 1 PCS = 1.25 L
        CONVERSION_TABLE.add(new UnitConversion(5, "Beurre",
                DishIngredient.UnitType.L, DishIngredient.UnitType.PCS, 0.8)); // 1 L = 0.8 PCS
    }


    public static UnitConversion findConversion(Integer ingredientId,
                                                DishIngredient.UnitType fromUnit,
                                                DishIngredient.UnitType toUnit) {
        // Si même unité, pas de conversion nécessaire
        if (fromUnit == toUnit) {
            return new UnitConversion(ingredientId, "Same unit", fromUnit, toUnit, 1.0);
        }

        for (UnitConversion conversion : CONVERSION_TABLE) {
            if (conversion.getIngredientId().equals(ingredientId) &&
                    conversion.getFromUnit() == fromUnit &&
                    conversion.getToUnit() == toUnit) {
                return conversion;
            }
        }

        throw new IllegalArgumentException(
                String.format("Aucune règle de conversion trouvée pour l'ingrédient %d de %s vers %s",
                        ingredientId, fromUnit, toUnit));
    }


    public static class TestData {
        public static class OrderItem {
            String ingredient;
            int quantity;
            DishIngredient.UnitType unit;
            String type; // "OUT" pour sortie
            String comment;

            public OrderItem(String ingredient, int quantity, DishIngredient.UnitType unit,
                             String type, String comment) {
                this.ingredient = ingredient;
                this.quantity = quantity;
                this.unit = unit;
                this.type = type;
                this.comment = comment;
            }
        }

        // Données du tableau dans l'image
        public static List<OrderItem> getTestOrders() {
            List<OrderItem> orders = new ArrayList<>();
            orders.add(new OrderItem("Tomate", 5, DishIngredient.UnitType.PCS, "OUT", "Préparation salade"));
            orders.add(new OrderItem("Laitue", 2, DishIngredient.UnitType.PCS, "OUT", "Préparation salade"));
            orders.add(new OrderItem("Chocolat", 1, DishIngredient.UnitType.L, "OUT", "Dessert"));
            orders.add(new OrderItem("Poulet", 4, DishIngredient.UnitType.PCS, "OUT", "Plat principal"));
            orders.add(new OrderItem("Beurre", 1, DishIngredient.UnitType.L, "OUT", "Pâtisserie"));
            return orders;
        }
    }


    public static class StockResult {
        String ingredient;
        double stockAvant;
        double sortie;
        double stockFinal;

        public StockResult(String ingredient, double stockAvant, double sortie) {
            this.ingredient = ingredient;
            this.stockAvant = stockAvant;
            this.sortie = sortie;
            this.stockFinal = stockAvant - sortie;
        }

        @Override
        public String toString() {
            return String.format("%-10s | Stock avant: %.1f KG | Sortie: %.1f KG | Stock final: %.1f KG",
                    ingredient, stockAvant, sortie, stockFinal);
        }
    }


    public static void demonstrateConversions() {


        // Stock initial (en KG, comme dans la base de données)
        double stockLaitue = 5.0;
        double stockTomate = 4.0;
        double stockPoulet = 10.0;
        double stockChocolat = 3.0;
        double stockBeurre = 2.5;

        System.out.println("Stock initial (rappel du stock_schema.sql):");
        System.out.println("  Laitue:   " + stockLaitue + " KG");
        System.out.println("  Tomate:   " + stockTomate + " KG");
        System.out.println("  Poulet:   " + stockPoulet + " KG");
        System.out.println("  Chocolat: " + stockChocolat + " KG");
        System.out.println("  Beurre:   " + stockBeurre + " KG");



        List<StockResult> results = new ArrayList<>();

        // Traiter chaque commande
        for (TestData.OrderItem order : TestData.getTestOrders()) {
            System.out.println("Commande: " + order.quantity + " " + order.unit + " de " + order.ingredient);
            System.out.println("  Type: " + order.type + " - " + order.comment);

            // Récupérer l'ID de l'ingrédient (basé sur les données existantes)
            Integer ingredientId = getIngredientId(order.ingredient);

            // Trouver la règle de conversion vers KG (notre unité de référence)
            UnitConversion conversion = findConversion(ingredientId, order.unit, DishIngredient.UnitType.KG);

            if (!conversion.isConversionPossible()) {
                System.out.println("  ✗ ERREUR: Conversion impossible de " + order.unit + " vers KG pour " + order.ingredient);
                continue;
            }

            // Convertir la quantité commandée en KG
            double quantityInKG = conversion.convert((double) order.quantity);

            System.out.println("  Conversion: " + order.quantity + " " + order.unit +
                    " = " + String.format("%.1f", quantityInKG) + " KG");
            System.out.println("  (Facteur de conversion: " + conversion.getConversionFactor() + ")");

            // Enregistrer le résultat selon l'ingrédient
            switch (order.ingredient) {
                case "Tomate":
                    results.add(new StockResult("Tomate", stockTomate, quantityInKG));
                    stockTomate -= quantityInKG;
                    break;
                case "Laitue":
                    results.add(new StockResult("Laitue", stockLaitue, quantityInKG));
                    stockLaitue -= quantityInKG;
                    break;
                case "Chocolat":
                    results.add(new StockResult("Chocolat", stockChocolat, quantityInKG));
                    stockChocolat -= quantityInKG;
                    break;
                case "Poulet":
                    results.add(new StockResult("Poulet", stockPoulet, quantityInKG));
                    stockPoulet -= quantityInKG;
                    break;
                case "Beurre":
                    results.add(new StockResult("Beurre", stockBeurre, quantityInKG));
                    stockBeurre -= quantityInKG;
                    break;
            }
            System.out.println();
        }


        for (StockResult result : results) {
            System.out.println(result);
        }



        // Valeurs attendues selon l'image 2
        double[][] expectedResults = {
                {5.0, 1.0, 4.0},  // Laitue: avant, sortie, après
                {4.0, 0.5, 3.5},  // Tomate
                {10.0, 0.5, 9.5}, // Poulet
                {3.0, 0.4, 2.6},  // Chocolat
                {2.5, 0.2, 2.3}   // Beurre
        };

        String[] ingredientNames = {"Laitue", "Tomate", "Poulet", "Chocolat", "Beurre"};
        boolean allTestsPassed = true;

        for (int i = 0; i < results.size(); i++) {
            StockResult result = results.get(i);
            double[] expected = expectedResults[i];

            boolean sortieOK = Math.abs(result.sortie - expected[1]) < 0.01;
            boolean finalOK = Math.abs(result.stockFinal - expected[2]) < 0.01;

            System.out.printf("%s:%n", ingredientNames[i]);
            System.out.printf("  Sortie attendue: %.1f KG, Obtenue: %.1f KG %s%n",
                    expected[1], result.sortie, sortieOK ? "✓" : "✗");
            System.out.printf("  Stock final attendu: %.1f KG, Obtenu: %.1f KG %s%n",
                    expected[2], result.stockFinal, finalOK ? "✓" : "✗");

            if (!sortieOK || !finalOK) {
                allTestsPassed = false;
            }
        }


        if (allTestsPassed) {
            System.out.println("✓ TEST PASSED");
        } else {
            System.out.println("✗ TESTS FAILED");
        }

    }

    /**
     * Méthode utilitaire pour obtenir l'ID d'un ingrédient
     */
    private static Integer getIngredientId(String ingredientName) {
        switch (ingredientName) {
            case "Laitue": return 1;
            case "Tomate": return 2;
            case "Poulet": return 3;
            case "Chocolat": return 4;
            case "Beurre": return 5;
            default: throw new IllegalArgumentException("Ingrédient inconnu: " + ingredientName);
        }
    }

    /**
     * MÉTHODE PRINCIPALE POUR DÉMONSTRATION
     */
    public static void main(String[] args) {
        demonstrateConversions();


        // Tests de conversions impossibles
        System.out.println("Test 1: Conversion impossible (Tomate KG → L)");
        try {
            UnitConversion conv = findConversion(2, DishIngredient.UnitType.KG, DishIngredient.UnitType.L);
            if (!conv.isConversionPossible()) {
                System.out.println("  ✓ Conversion correctement identifiée comme impossible");
            }
            conv.convert(5.0); // Devrait lancer une exception
            System.out.println("  ✗ ERREUR: L'exception n'a pas été levée");
        } catch (IllegalStateException e) {
            System.out.println("  ✓ Exception levée: " + e.getMessage());
        }

        // Test de conversion possible
        System.out.println("\nTest 2: Conversion possible (Chocolat L → KG)");
        UnitConversion conv = findConversion(4, DishIngredient.UnitType.L, DishIngredient.UnitType.KG);
        double result = conv.convert(2.5);
        System.out.println("  2.5 L de Chocolat = " + result + " KG");
        System.out.println("  ✓ Résultat attendu: 1.0 KG (car 2.5 L × 0.4 = 1.0)");

        // Test de conversion PCS → L pour le Beurre
        System.out.println("\nTest 3: Conversion PCS → L (Beurre)");
        UnitConversion conv2 = findConversion(5, DishIngredient.UnitType.PCS, DishIngredient.UnitType.L);
        double result2 = conv2.convert(4.0);
        System.out.println("  4 PCS de Beurre = " + result2 + " L");
        System.out.println("  ✓ Résultat attendu: 5.0 L (car 4 PCS × 1.25 = 5.0)");
    }
}

/**
 * ════════════════════════════════════════════════════════════════════════════
 * INSTRUCTIONS D'INTÉGRATION DANS DataRetriever.java
 * ════════════════════════════════════════════════════════════════════════════
 *
 * Pour intégrer cette fonctionnalité dans votre code existant:
 *
 * 1. Copiez la classe UnitConversion et la table CONVERSION_TABLE dans DataRetriever
 *
 * 2. Modifiez la méthode updateStockForOrder() pour utiliser les conversions:
 *
 *    private void updateStockForOrder(Connection conn, Order order) throws SQLException {
 *        for (DishOrder dishOrder : order.getDishOrders()) {
 *            List<DishIngredient> ingredients = findIngredientsByDishId(dishOrder.getDish().getId());
 *
 *            for (DishIngredient di : ingredients) {
 *                double quantityRequired = di.getQuantity() * dishOrder.getQuantity();
 *
 *                // NOUVELLE LOGIQUE: Convertir l'unité si nécessaire
 *                DishIngredient.UnitType dishUnit = di.getUnitType();
 *                double quantityInKG;
 *
 *                if (dishUnit == DishIngredient.UnitType.KG) {
 *                    quantityInKG = quantityRequired;
 *                } else {
 *                    // Trouver la conversion vers KG
 *                    UnitConversion conversion = BonusK1_ConversionUnites.findConversion(
 *                        di.getIngredient().getId(), dishUnit, DishIngredient.UnitType.KG);
 *
 *                    if (!conversion.isConversionPossible()) {
 *                        throw new IllegalStateException(
 *                            "Impossible de convertir " + dishUnit + " en KG pour " +
 *                            di.getIngredient().getName());
 *                    }
 *
 *                    quantityInKG = conversion.convert(quantityRequired);
 *                }
 *
 *                // Créer un mouvement de stock négatif en KG
 *                String insertMovement = """
 *                    INSERT INTO stock_movement (id_ingredient, quantity, unit, movement_date)
 *                    VALUES (?, ?, 'KG', ?)
 *                    """;
 *
 *                try (PreparedStatement ps = conn.prepareStatement(insertMovement)) {
 *                    ps.setInt(1, di.getIngredient().getId());
 *                    ps.setDouble(2, -quantityInKG);  // Négatif car c'est une sortie
 *                    ps.setTimestamp(3, Timestamp.from(order.getCreationDateTime()));
 *                    ps.executeUpdate();
 *                }
 *            }
 *        }
 *    }
 *
 * 3. De même pour checkStockAvailability(), convertir en KG avant de vérifier:
 *
 *    private void checkStockAvailability(Connection conn, Order order)
 *            throws SQLException, InsufficientStockException {
 *        for (DishOrder dishOrder : order.getDishOrders()) {
 *            Dish dish = dishOrder.getDish();
 *            int quantityOrdered = dishOrder.getQuantity();
 *
 *            List<DishIngredient> ingredients = findIngredientsByDishId(dish.getId());
 *            for (DishIngredient di : ingredients) {
 *                double requiredQuantity = di.getQuantity() * quantityOrdered;
 *
 *                // Convertir en KG si nécessaire
 *                double requiredQuantityInKG;
 *                if (di.getUnitType() == DishIngredient.UnitType.KG) {
 *                    requiredQuantityInKG = requiredQuantity;
 *                } else {
 *                    UnitConversion conversion = BonusK1_ConversionUnites.findConversion(
 *                        di.getIngredient().getId(),
 *                        di.getUnitType(),
 *                        DishIngredient.UnitType.KG);
 *                    requiredQuantityInKG = conversion.convert(requiredQuantity);
 *                }
 *
 *                // Vérifier le stock actuel (toujours en KG dans la base)
 *                double currentStock = getCurrentStock(conn, di.getIngredient().getId());
 *
 *                if (currentStock < requiredQuantityInKG) {
 *                    throw new InsufficientStockException(
 *                        String.format("Stock insuffisant pour %s. Requis: %.2f KG, Disponible: %.2f KG",
 *                                     di.getIngredient().getName(),
 *                                     requiredQuantityInKG,
 *                                     currentStock));
 *                }
 *            }
 *        }
 *    }
 */