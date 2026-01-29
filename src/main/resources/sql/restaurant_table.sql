


-- 1. Créer la table restaurant_table pour gérer les tables du restaurant
CREATE TABLE IF NOT EXISTS restaurant_table (
  id SERIAL PRIMARY KEY,
   number INTEGER NOT NULL UNIQUE CHECK (number > 0)
    );

-- 2. Modifier la table order pour ajouter les informations de table
ALTER TABLE "order" ADD COLUMN IF NOT EXISTS id_table INTEGER REFERENCES restaurant_table(id) ON DELETE RESTRICT;
ALTER TABLE "order" ADD COLUMN IF NOT EXISTS client_installation_datetime TIMESTAMP;
ALTER TABLE "order" ADD COLUMN IF NOT EXISTS client_departure_datetime TIMESTAMP;

-- Contrainte: la date d'installation doit être avant la date de départ
ALTER TABLE "order" ADD CONSTRAINT check_installation_before_departure
    CHECK (client_installation_datetime IS NULL OR client_departure_datetime IS NULL OR
           client_installation_datetime < client_departure_datetime);

-- Index pour améliorer les performances des requêtes de disponibilité
CREATE INDEX IF NOT EXISTS idx_order_table_dates ON "order"(id_table, client_installation_datetime, client_departure_datetime);

-- 3. Insérer des tables de restaurant pour les tests
INSERT INTO restaurant_table (number) VALUES
                                          (1),
                                          (2),
                                          (3),
                                          (4),
                                          (5)
    ON CONFLICT (number) DO NOTHING;

-- 4. Vérification de la structure
SELECT 'Structure de la table restaurant_table:' as info;
\d restaurant_table

SELECT 'Structure modifiée de la table order:' as info;
\d "order"

-- 5. Affichage des tables disponibles
SELECT 'Tables du restaurant:' as info;
SELECT id, number FROM restaurant_table ORDER BY number;