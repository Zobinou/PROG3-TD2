
-- TD Orders: Création des tables pour la gestion des commandes

-- 1. Créer la table order pour les commandes
CREATE TABLE IF NOT EXISTS "order" (
id SERIAL PRIMARY KEY,
reference VARCHAR(255) NOT NULL UNIQUE,
    creation_datetime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- 2. Créer la table dish_order (table de jointure)
CREATE TABLE IF NOT EXISTS dish_order (
id SERIAL PRIMARY KEY,
id_order INTEGER NOT NULL REFERENCES "order"(id) ON DELETE CASCADE,
    id_dish INTEGER NOT NULL REFERENCES dish(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL CHECK (quantity > 0)
    );

-- Index pour améliorer les performances
CREATE INDEX IF NOT EXISTS idx_dish_order_order ON dish_order(id_order);
CREATE INDEX IF NOT EXISTS idx_dish_order_dish ON dish_order(id_dish);

-- 3. Vérification de la structure
SELECT 'Structure de la table order:' as info;
\d "order"

SELECT 'Structure de la table dish_order:' as info;
\d dish_order;