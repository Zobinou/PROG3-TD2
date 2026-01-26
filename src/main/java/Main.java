import java.time.Instant;

public class Main {
    public static void main(String[] args) {
        DataRetriever dr = new DataRetriever();

        try {
            int[] ids = {1, 2, 3, 4, 5};
            String[] noms = {"Salade fraîche", "Poulet grillé", "Riz aux légumes", "Gâteau au chocolat", "Salade de fruits"};

            for (int i = 0; i < ids.length; i++) {
                int id = ids[i];
                String nom = noms[i];

                System.out.println("\n=== " + nom + " (id " + id + ") ===");
                double cout = dr.getDishCost(id);
                System.out.println("Coût : " + cout);

                try {
                    double marge = dr.getGrossMargin(id);
                    System.out.println("Marge brute : " + marge);
                } catch (IllegalStateException e) {
                    System.out.println("→ Exception : " + e.getMessage());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("=== TD4: Tests de gestion des stocks ===\n");

        // Date de test: 2024-01-06 12:00:00
        Instant testDate = Instant.parse("2024-01-06T12:00:00Z");

        // IDs et noms des ingrédients
        int[] ingredientIds = {1, 2, 3, 4, 5};
        String[] ingredientNames = {"Laitue", "Tomate", "Poulet", "Chocolat", "Beurre"};

        // Valeurs attendues selon le TD4
        // Stock initial + mouvements cumulés jusqu'au 2024-01-06 12:00
        double[] expectedStocks = {
                12.0,  // Laitue: 10 + 5 - 2 - 1 = 12
                20.0,  // Tomate: 15 + 10 - 3 - 2 = 20
                30.0,  // Poulet: 20 + 15 - 5 = 30
                8.0,   // Chocolat: 5 + 3 = 8
                10.0   // Beurre: 8 + 2 = 10
        };

        try {
            System.out.println("Test de getStockValueAt() au " + testDate);
            System.out.println("=" .repeat(60) + "\n");

            for (int i = 0; i < ingredientIds.length; i++) {
                int id = ingredientIds[i];
                String name = ingredientNames[i];
                double expected = expectedStocks[i];

                System.out.println("Ingrédient: " + name + " (id " + id + ")");

                Stock stock = dr.getStockValueAt(id, testDate);
                System.out.println("  Quantité en stock: " + stock.getQuantity() + " " + stock.getUnit());
                System.out.println("  Quantité attendue: " + expected + " KG");

                if (Math.abs(stock.getQuantity() - expected) < 0.01) {
                    System.out.println("  ✓ Stock correct");
                } else {
                    System.out.println("  ✗ Stock incorrect!");
                }
                System.out.println();
            }

            // Test supplémentaire: afficher les mouvements d'un ingrédient
            System.out.println("\n=== Détails des mouvements pour la Laitue ===");
            Ingredient laitue = dr.findIngredientById(1);
            System.out.println("Ingrédient: " + laitue.getName());
            System.out.println("Prix: " + laitue.getPrice());
            System.out.println("Mouvements de stock:");

            if (laitue.getStockMovementList() != null) {
                for (StockMovement mv : laitue.getStockMovementList()) {
                    String type = mv.getQuantity() > 0 ? "ACHAT" : "VENTE";
                    System.out.println("  - " + mv.getMovementDate() + " : " +
                            mv.getQuantity() + " " + mv.getUnit() +
                            " (" + type + ")");
                }
            }

            // Test de saveIngredient avec des mouvements
            System.out.println("\n=== Test de sauvegarde d'un nouvel ingrédient ===");
            Ingredient newIngredient = new Ingredient();
            newIngredient.setName("Huile d'olive");
            newIngredient.setCategory(CategoryEnum.OTHER);
            newIngredient.setPrice(2000.0);

            // Ajouter un mouvement de stock
            StockMovement movement = new StockMovement();
            movement.setQuantity(5.0);
            movement.setUnit(StockMovement.UnitType.L);
            movement.setMovementDate(Instant.now());

            newIngredient.getStockMovementList().add(movement);

            Ingredient saved = dr.saveIngredient(newIngredient);
            System.out.println("Ingrédient sauvegardé: " + saved);
            System.out.println("Nombre de mouvements: " + saved.getStockMovementList().size());

        } catch (Exception e) {
            System.err.println("Erreur lors des tests:");
            e.printStackTrace();
        }
    }
}