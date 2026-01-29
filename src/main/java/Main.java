import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Main {
    public static void main(String[] args) {
        DataRetriever dr = new DataRetriever();


        try {
            Thread.sleep(3000); // Pause de 3 secondes pour lire l'avertissement
        } catch (InterruptedException e) {
            // Ignorer
        }


        // ═══════════════════════════════════════════════════════════════
        // TD3: NORMALISATION ET GESTION MANYTOMANY
        // ═══════════════════════════════════════════════════════════════

        System.out.println("\n\n┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
        System.out.println("┃ TD3: NORMALISATION ET GESTION MANYTOMANY                       ┃");
        System.out.println("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛\n");


        // Question 4: getDishCost()
        System.out.println("Question 4: Calcul du coût des plats (getDishCost)");
        System.out.println("─────────────────────────────────────────────────────────────────");

        try {
            int[] dishIds = {1, 2, 4};
            String[] dishNames = {"Salade fraîche", "Poulet grillé", "Gâteau au chocolat"};
            double[] expectedCosts = {310.0, 4500.0, 1400.0};

            int passedTests = 0;
            for (int i = 0; i < dishIds.length; i++) {
                int id = dishIds[i];
                String name = dishNames[i];
                double expected = expectedCosts[i];

                Dish dish = dr.findDishById(id);
                System.out.printf("\n%s (id=%d):%n", name, id);
                System.out.println("  Ingrédients:");

                for (DishIngredient di : dish.getDishIngredients()) {
                    System.out.printf("    • %s: %.2f %s × %.0f Ar/KG = %.0f Ar%n",
                            di.getIngredient().getName(),
                            di.getQuantity(),
                            di.getUnitType(),
                            di.getIngredient().getPrice(),
                            di.getQuantity() * di.getIngredient().getPrice());
                }

                double cost = dr.getDishCost(id);
                System.out.printf("  Coût total: %.0f Ar (attendu: %.0f Ar)%n", cost, expected);

                if (Math.abs(cost - expected) < 0.01) {
                    System.out.println("  ✓ TEST RÉUSSI");
                    passedTests++;
                } else {
                    System.out.println("  ✗ TEST ÉCHOUÉ");
                }
            }
            System.out.printf("\nRésultat: %d/%d tests réussis%n", passedTests, dishIds.length);

        } catch (Exception e) {
            System.err.println("✗ ERREUR: " + e.getMessage());
            e.printStackTrace();
        }

        // Question 5: getGrossMargin()
        System.out.println("\n\nQuestion 5: Calcul de la marge brute (getGrossMargin)");
        System.out.println("─────────────────────────────────────────────────────────────────");

        try {
            int[] dishIds = {1, 2, 3, 4, 5};
            String[] dishNames = {"Salade fraîche", "Poulet grillé", "Riz aux légumes",
                    "Gâteau au chocolat", "Salade de fruits"};
            Double[] expectedMargins = {3190.0, 7500.0, null, 6600.0, null};

            int passedTests = 0;
            for (int i = 0; i < dishIds.length; i++) {
                int id = dishIds[i];
                String name = dishNames[i];
                Double expected = expectedMargins[i];

                Dish dish = dr.findDishById(id);
                System.out.printf("\n%s (id=%d):%n", name, id);
                System.out.printf("  Prix de vente: %s%n",
                        dish.getPrice() != null ? dish.getPrice() + " Ar" : "NULL");

                try {
                    double margin = dr.getGrossMargin(id);
                    System.out.printf("  Coût: %.0f Ar%n", dr.getDishCost(id));
                    System.out.printf("  Marge brute: %.0f Ar (attendu: %.0f Ar)%n",
                            margin, expected);

                    if (expected != null && Math.abs(margin - expected) < 0.01) {
                        System.out.println("  ✓ TEST RÉUSSI");
                        passedTests++;
                    } else {
                        System.out.println("  ✗ TEST ÉCHOUÉ");
                    }
                } catch (IllegalStateException e) {
                    System.out.println("  Exception: " + e.getMessage());
                    if (expected == null) {
                        System.out.println("  ✓ TEST RÉUSSI (exception attendue)");
                        passedTests++;
                    } else {
                        System.out.println("  ✗ TEST ÉCHOUÉ (exception non attendue)");
                    }
                }
            }
            System.out.printf("\nRésultat: %d/%d tests réussis%n", passedTests, dishIds.length);

        } catch (Exception e) {
            System.err.println("✗ ERREUR: " + e.getMessage());
            e.printStackTrace();
        }

        // ═══════════════════════════════════════════════════════════════
        // TD4: GESTION DES STOCKS
        // ═══════════════════════════════════════════════════════════════
        System.out.println("\n\n┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
        System.out.println("┃ TD4: GESTION DES STOCKS                                        ┃");
        System.out.println("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛\n");

        // Question 2a: saveIngredient()
        System.out.println("Question 2a: Sauvegarde d'un ingrédient (saveIngredient)");
        System.out.println("─────────────────────────────────────────────────────────────────");

        try {
            Ingredient newIngredient = new Ingredient();
            newIngredient.setName("Riz");
            newIngredient.setCategory(CategoryEnum.OTHER);
            newIngredient.setPrice(1500.0);

            StockMovement movement1 = new StockMovement();
            movement1.setQuantity(20.0);
            movement1.setUnit(StockMovement.UnitType.KG);
            movement1.setMovementDate(Instant.parse("2024-01-01T10:00:00Z"));

            StockMovement movement2 = new StockMovement();
            movement2.setQuantity(-5.0);
            movement2.setUnit(StockMovement.UnitType.KG);
            movement2.setMovementDate(Instant.parse("2024-01-03T14:00:00Z"));

            newIngredient.getStockMovementList().add(movement1);
            newIngredient.getStockMovementList().add(movement2);

            System.out.println("\nCréation d'un nouvel ingrédient:");
            System.out.println("  • Nom: " + newIngredient.getName());
            System.out.println("  • Prix: " + newIngredient.getPrice() + " Ar");
            System.out.println("  • Mouvements: " + newIngredient.getStockMovementList().size());

            Ingredient saved = dr.saveIngredient(newIngredient);

            System.out.println("\nIngrédient sauvegardé:");
            System.out.println("  • ID: " + saved.getId());
            System.out.println("  • Nom: " + saved.getName());
            System.out.println("  • Mouvements enregistrés: " + saved.getStockMovementList().size());
            System.out.println("  ✓ TEST RÉUSSI");

        } catch (Exception e) {
            System.err.println("✗ ERREUR: " + e.getMessage());
            e.printStackTrace();
        }

        // Question 3: getStockValueAt()
        System.out.println("\n\nQuestion 3: Calcul du stock à une date (getStockValueAt)");
        System.out.println("─────────────────────────────────────────────────────────────────");
        System.out.println("Date de test: 2024-01-06 12:00:00");

        try {
            Instant testDate = Instant.parse("2024-01-06T12:00:00Z");

            int[] ingredientIds = {1, 2, 3, 4, 5};
            String[] ingredientNames = {"Laitue", "Tomate", "Poulet", "Chocolat", "Beurre"};
            double[] initialStocks = {10.0, 15.0, 20.0, 5.0, 8.0};
            double[] expectedStocks = {12.0, 20.0, 30.0, 8.0, 10.0};

            int passedTests = 0;
            for (int i = 0; i < ingredientIds.length; i++) {
                int id = ingredientIds[i];
                String name = ingredientNames[i];
                double initial = initialStocks[i];
                double expected = expectedStocks[i];

                Stock stock = dr.getStockValueAt(id, testDate);

                System.out.printf("\n%s (id=%d):%n", name, id);
                System.out.printf("  Stock initial: %.0f KG%n", initial);
                System.out.printf("  Stock calculé: %.0f KG%n", stock.getQuantity());
                System.out.printf("  Stock attendu: %.0f KG%n", expected);
                System.out.printf("  Différence: %+.0f KG%n", stock.getQuantity() - initial);

                if (Math.abs(stock.getQuantity() - expected) < 0.01) {
                    System.out.println("  ✓ TEST RÉUSSI");
                    passedTests++;
                } else {
                    System.out.println("  ✗ TEST ÉCHOUÉ");
                }
            }
            System.out.printf("\nRésultat: %d/%d tests réussis%n", passedTests, ingredientIds.length);

        } catch (Exception e) {
            System.err.println("✗ ERREUR: " + e.getMessage());
            e.printStackTrace();
        }

        // ═══════════════════════════════════════════════════════════════
        // TD ORDERS: GESTION DES COMMANDES
        // ═══════════════════════════════════════════════════════════════
        System.out.println("\n\n┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
        System.out.println("┃ TD ORDERS: GESTION DES COMMANDES                               ┃");
        System.out.println("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛\n");

        // Test 1: saveOrder() avec vérification de stock
        System.out.println("Question c-i: Sauvegarde de commande (saveOrder)");
        System.out.println("─────────────────────────────────────────────────────────────────");

        try {
            System.out.println("\nTest 1: Commande avec stock suffisant");

            // Vérifier stock avant
            Instant before = Instant.now();
            Stock laitueAvant = dr.getStockValueAt(1, before);
            Stock tomateAvant = dr.getStockValueAt(2, before);

            System.out.printf("  Stock AVANT commande:%n");
            System.out.printf("    Laitue: %.2f KG%n", laitueAvant.getQuantity());
            System.out.printf("    Tomate: %.2f KG%n", tomateAvant.getQuantity());

            Order order1 = new Order();
            order1.setReference("ORD00001");
            order1.setCreationDateTime(Instant.now());

            // Ajouter une table pour les tests TD Orders
            Table table1 = dr.findTableById(1);
            order1.setTable(table1);
            order1.setClientInstallationDateTime(Instant.now());
            order1.setClientDepartureDateTime(Instant.now().plus(1, ChronoUnit.HOURS));

            DishOrder dishOrder1 = new DishOrder();
            dishOrder1.setDish(dr.findDishById(1)); // Salade fraîche
            dishOrder1.setQuantity(2);
            order1.getDishOrders().add(dishOrder1);

            System.out.printf("\n  Commande: %s%n", order1.getReference());
            System.out.printf("    • 2 × Salade fraîche%n");
            System.out.printf("    • Laitue nécessaire: 0.20 × 2 = 0.40 KG%n");
            System.out.printf("    • Tomate nécessaire: 0.15 × 2 = 0.30 KG%n");

            Order saved = dr.saveOrder(order1);

            System.out.printf("\n  Commande créée avec succès!%n");
            System.out.printf("    Montant HT: %.0f Ar%n", saved.getTotalAmountExcludingTax());

            // Vérifier stock après
            try {
                Thread.sleep(100); // Petit délai pour garantir l'ordre des timestamps
            } catch (InterruptedException e) {
                // Ignorer
            }

            Instant after = Instant.now();
            Stock laitueApres = dr.getStockValueAt(1, after);
            Stock tomateApres = dr.getStockValueAt(2, after);

            System.out.printf("\n  Stock APRÈS commande:%n");
            System.out.printf("    Laitue: %.2f KG (-%+.2f KG)%n",
                    laitueApres.getQuantity(),
                    laitueAvant.getQuantity() - laitueApres.getQuantity());
            System.out.printf("    Tomate: %.2f KG (-%+.2f KG)%n",
                    tomateApres.getQuantity(),
                    tomateAvant.getQuantity() - tomateApres.getQuantity());

            System.out.println("\n  ✓ TEST RÉUSSI");

        } catch (Exception e) {
            System.err.println("  ✗ TEST ÉCHOUÉ: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            System.out.println("\n\nTest 2: Commande avec stock insuffisant");

            Order order2 = new Order();
            order2.setReference("ORD00002");
            order2.setCreationDateTime(Instant.now());

            // Ajouter une table pour les tests TD Orders
            Table table2 = dr.findTableById(2);
            order2.setTable(table2);
            order2.setClientInstallationDateTime(Instant.now());
            order2.setClientDepartureDateTime(Instant.now().plus(1, ChronoUnit.HOURS));

            DishOrder dishOrder2 = new DishOrder();
            dishOrder2.setDish(dr.findDishById(2)); // Poulet grillé
            dishOrder2.setQuantity(100);
            order2.getDishOrders().add(dishOrder2);

            System.out.printf("  Commande: %s%n", order2.getReference());
            System.out.printf("    • 100 × Poulet grillé%n");
            System.out.printf("    • Poulet nécessaire: 1.00 × 100 = 100 KG%n");

            Stock poulet = dr.getStockValueAt(3, Instant.now());
            System.out.printf("    • Poulet disponible: %.0f KG%n", poulet.getQuantity());

            try {
                dr.saveOrder(order2);
                System.out.println("\n  ✗ TEST ÉCHOUÉ: La commande aurait dû être refusée!");
            } catch (InsufficientStockException e) {
                System.out.println("\n  Exception levée: " + e.getMessage());
                System.out.println("  ✓ TEST RÉUSSI (exception attendue)");
            }

        } catch (Exception e) {
            System.err.println("  ✗ TEST ÉCHOUÉ: " + e.getMessage());
            e.printStackTrace();
        }

        // Test 2: findOrderByReference()
        System.out.println("\n\nQuestion c-ii: Récupération de commande (findOrderByReference)");
        System.out.println("─────────────────────────────────────────────────────────────────");

        try {
            Order retrieved = dr.findOrderByReference("ORD00001");

            System.out.printf("\nCommande trouvée: %s%n", retrieved.getReference());
            System.out.printf("  Date: %s%n", retrieved.getCreationDateTime());
            System.out.printf("  Plats:%n");

            for (DishOrder dishOrder : retrieved.getDishOrders()) {
                System.out.printf("    • %d × %s (%.0f Ar)%n",
                        dishOrder.getQuantity(),
                        dishOrder.getDish().getName(),
                        dishOrder.getDish().getPrice());
            }

            System.out.printf("  Montant total: %.0f Ar%n", retrieved.getTotalAmountExcludingTax());
            System.out.println("\n  ✓ TEST RÉUSSI");

        } catch (Exception e) {
            System.err.println("  ✗ TEST ÉCHOUÉ: " + e.getMessage());
            e.printStackTrace();
        }

        // Test complémentaire: Commande multiple plats
        System.out.println("\n\nTest complémentaire: Commande avec plusieurs plats différents");
        System.out.println("─────────────────────────────────────────────────────────────────");

        try {
            Order order3 = new Order();
            order3.setReference("ORD00003");
            order3.setCreationDateTime(Instant.now());

            // Ajouter une table pour les tests TD Orders
            Table table3 = dr.findTableById(3);
            order3.setTable(table3);
            order3.setClientInstallationDateTime(Instant.now());
            order3.setClientDepartureDateTime(Instant.now().plus(1, ChronoUnit.HOURS));

            DishOrder do1 = new DishOrder();
            do1.setDish(dr.findDishById(1)); // Salade fraîche
            do1.setQuantity(1);
            order3.getDishOrders().add(do1);

            DishOrder do2 = new DishOrder();
            do2.setDish(dr.findDishById(4)); // Gâteau au chocolat
            do2.setQuantity(2);
            order3.getDishOrders().add(do2);

            System.out.printf("\nCommande: %s%n", order3.getReference());
            System.out.printf("  • 1 × Salade fraîche (3500 Ar)%n");
            System.out.printf("  • 2 × Gâteau au chocolat (8000 Ar × 2)%n");

            Order saved = dr.saveOrder(order3);

            double expectedTotal = 3500.0 + (8000.0 * 2);
            System.out.printf("\n  Montant calculé: %.0f Ar%n", saved.getTotalAmountExcludingTax());
            System.out.printf("  Montant attendu: %.0f Ar%n", expectedTotal);

            if (Math.abs(saved.getTotalAmountExcludingTax() - expectedTotal) < 0.01) {
                System.out.println("  ✓ TEST RÉUSSI");
            } else {
                System.out.println("  ✗ TEST ÉCHOUÉ");
            }

        } catch (Exception e) {
            System.err.println("  ✗ TEST ÉCHOUÉ: " + e.getMessage());
            e.printStackTrace();
        }


        // ═══════════════════════════════════════════════════════════════
        // EXAMEN: GESTION DES CONTRAINTES D'ESPACE (TABLES)
        // ═══════════════════════════════════════════════════════════════
        System.out.println("\n\n┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
        System.out.println("┃ EXAMEN: GESTION DES CONTRAINTES D'ESPACE (TABLES)             ┃");
        System.out.println("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛\n");

        // Test 1: Commande avec table disponible
        System.out.println("Test 1: Commande avec table disponible");
        System.out.println("─────────────────────────────────────────────────────────────────");

        try {
            // Créer une commande avec une table
            Order examOrder1 = new Order();
            examOrder1.setReference("EXAM_ORD_001");
            examOrder1.setCreationDateTime(Instant.now());

            // Associer la table 1
            Table table1 = dr.findTableById(1);
            examOrder1.setTable(table1);

            // Date d'installation: maintenant
            Instant installation = Instant.now();
            examOrder1.setClientInstallationDateTime(installation);

            // Date de départ: dans 2 heures
            Instant departure = installation.plus(2, ChronoUnit.HOURS);
            examOrder1.setClientDepartureDateTime(departure);

            // Ajouter un plat
            DishOrder examDishOrder1 = new DishOrder();
            examDishOrder1.setDish(dr.findDishById(1)); // Salade fraîche
            examDishOrder1.setQuantity(2);
            examOrder1.getDishOrders().add(examDishOrder1);

            System.out.printf("\nCréation de la commande:%n");
            System.out.printf("  • Référence: %s%n", examOrder1.getReference());
            System.out.printf("  • Table: %d%n", table1.getNumber());
            System.out.printf("  • Installation: %s%n", installation);
            System.out.printf("  • Départ prévu: %s%n", departure);
            System.out.printf("  • Plat: 2 × Salade fraîche%n");

            // Sauvegarder la commande
            Order savedExam = dr.saveOrder(examOrder1);

            System.out.printf("\n✓ Commande créée avec succès!%n");
            System.out.printf("  • ID: %d%n", savedExam.getId());
            System.out.printf("  • Montant: %.0f Ar%n", savedExam.getTotalAmountExcludingTax());
            System.out.println("\n  ✓ TEST EXAMEN 1 RÉUSSI");

        } catch (Exception e) {
            System.err.println("\n✗ TEST EXAMEN 1 ÉCHOUÉ: " + e.getMessage());
            e.printStackTrace();
        }

        // Test 2: Commande avec table déjà occupée (AVEC suggestion)
        System.out.println("\n\nTest 2: Commande avec table déjà occupée (vérification suggestions)");
        System.out.println("─────────────────────────────────────────────────────────────────");

        try {
            // Essayer de réserver la même table au même moment
            Order examOrder2 = new Order();
            examOrder2.setReference("EXAM_ORD_002");
            examOrder2.setCreationDateTime(Instant.now());

            // Même table que la commande précédente
            Table table1 = dr.findTableById(1);
            examOrder2.setTable(table1);

            // Même période (dans l'heure qui suit)
            Instant installation2 = Instant.now().plus(30, ChronoUnit.MINUTES);
            examOrder2.setClientInstallationDateTime(installation2);
            examOrder2.setClientDepartureDateTime(installation2.plus(2, ChronoUnit.HOURS));

            DishOrder examDishOrder2 = new DishOrder();
            examDishOrder2.setDish(dr.findDishById(2)); // Poulet grillé
            examDishOrder2.setQuantity(1);
            examOrder2.getDishOrders().add(examDishOrder2);

            System.out.printf("\nTentative de réservation:%n");
            System.out.printf("  • Table: %d (déjà occupée)%n", table1.getNumber());
            System.out.printf("  • Installation prévue: %s%n", installation2);

            // Ceci devrait échouer
            dr.saveOrder(examOrder2);

            System.out.println("\n✗ TEST EXAMEN 2 ÉCHOUÉ: La commande aurait dû être refusée!");

        } catch (TableNotAvailableException e) {
            System.out.printf("\n✓ Exception attendue levée:%n");
            System.out.printf("  Message: %s%n", e.getMessage());

            if (e.getAvailableTables() != null && !e.getAvailableTables().isEmpty()) {
                System.out.printf("  Tables suggérées: %d table(s)%n", e.getAvailableTables().size());
                for (Table t : e.getAvailableTables()) {
                    System.out.printf("    • Table %d%n", t.getNumber());
                }
            }

            System.out.println("\n  ✓ TEST EXAMEN 2 RÉUSSI");

        } catch (Exception e) {
            System.err.println("\n✗ TEST EXAMEN 2 ÉCHOUÉ (autre erreur): " + e.getMessage());
            e.printStackTrace();
        }

        // Test 3: Commande avec table disponible (différente période)
        System.out.println("\n\nTest 3: Commande sur même table mais période différente");
        System.out.println("─────────────────────────────────────────────────────────────────");

        try {
            // Réserver la table 1 mais pour plus tard (après le départ)
            Order examOrder3 = new Order();
            examOrder3.setReference("EXAM_ORD_003");
            examOrder3.setCreationDateTime(Instant.now());

            Table table1 = dr.findTableById(1);
            examOrder3.setTable(table1);

            // Installation dans 3 heures (après le départ de la première commande)
            Instant installation3 = Instant.now().plus(3, ChronoUnit.HOURS);
            examOrder3.setClientInstallationDateTime(installation3);
            examOrder3.setClientDepartureDateTime(installation3.plus(1, ChronoUnit.HOURS));

            DishOrder examDishOrder3 = new DishOrder();
            examDishOrder3.setDish(dr.findDishById(4)); // Gâteau au chocolat
            examDishOrder3.setQuantity(1);
            examOrder3.getDishOrders().add(examDishOrder3);

            System.out.printf("\nRéservation future:%n");
            System.out.printf("  • Table: %d%n", table1.getNumber());
            System.out.printf("  • Installation: %s (dans 3h)%n", installation3);

            Order savedExam3 = dr.saveOrder(examOrder3);

            System.out.printf("\n✓ Commande créée avec succès!%n");
            System.out.printf("  • ID: %d%n", savedExam3.getId());
            System.out.println("\n  ✓ TEST EXAMEN 3 RÉUSSI");

        } catch (Exception e) {
            System.err.println("\n✗ TEST EXAMEN 3 ÉCHOUÉ: " + e.getMessage());
            e.printStackTrace();
        }

        // Test 4: Vérification de récupération des commandes avec tables
        System.out.println("\n\nTest 4: Récupération d'une commande avec table");
        System.out.println("─────────────────────────────────────────────────────────────────");

        try {
            Order retrievedExam = dr.findOrderByReference("EXAM_ORD_001");

            System.out.printf("\nCommande récupérée:%n");
            System.out.printf("  • Référence: %s%n", retrievedExam.getReference());
            System.out.printf("  • Table: %d%n", retrievedExam.getTable().getNumber());
            System.out.printf("  • Installation: %s%n", retrievedExam.getClientInstallationDateTime());
            System.out.printf("  • Départ: %s%n", retrievedExam.getClientDepartureDateTime());
            System.out.printf("  • Plats: %d%n", retrievedExam.getDishOrders().size());
            System.out.printf("  • Montant: %.0f Ar%n", retrievedExam.getTotalAmountExcludingTax());

            System.out.println("\n  ✓ TEST EXAMEN 4 RÉUSSI");

        } catch (Exception e) {
            System.err.println("\n✗ TEST EXAMEN 4 ÉCHOUÉ: " + e.getMessage());
            e.printStackTrace();
        }

    }
}