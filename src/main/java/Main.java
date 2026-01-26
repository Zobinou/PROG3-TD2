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