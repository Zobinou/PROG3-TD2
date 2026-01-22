//public class Main {
//    public static void main(String[] args) {
//        // Log before changes
//        DataRetriever dataRetriever = new DataRetriever();
//        Dish dish = dataRetriever.findDishById(4
//
//        );
//        System.out.println(dish);
//
//        // Log after changes
////        dish.setIngredients(List.of(new Ingredient(1), new Ingredient(2)));
////        Dish newDish = dataRetriever.saveDish(dish);
////        System.out.println(newDish);
//
//        // Ingredient creations
//        //List<Ingredient> createdIngredients = dataRetriever.createIngredients(List.of(new Ingredient(null, "Fromage", CategoryEnum.DAIRY, 1200.0)));
//        //System.out.println(createdIngredients);
//    }
//}

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
    }
}