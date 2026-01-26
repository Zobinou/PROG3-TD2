
-- 1. Créer la table stock pour le stock initial de chaque ingrédient
CREATE TABLE IF NOT EXISTS stock (
id SERIAL PRIMARY KEY,
id_ingredient INTEGER NOT NULL UNIQUE REFERENCES ingredient(id) ON DELETE CASCADE,
    quantity NUMERIC(10, 3) NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    unit VARCHAR(10) NOT NULL CHECK (unit IN ('KG', 'L', 'PCS'))
    );

-- 2. Créer la table stock_movement pour les mouvements de stock
CREATE TABLE IF NOT EXISTS stock_movement (
id SERIAL PRIMARY KEY,
id_ingredient INTEGER NOT NULL REFERENCES ingredient(id) ON DELETE CASCADE,
    quantity NUMERIC(10, 3) NOT NULL,
    unit VARCHAR(10) NOT NULL CHECK (unit IN ('KG', 'L', 'PCS')),
    movement_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Index pour améliorer les performances des requêtes
CREATE INDEX IF NOT EXISTS idx_stock_movement_ingredient ON stock_movement(id_ingredient);
CREATE INDEX IF NOT EXISTS idx_stock_movement_date ON stock_movement(movement_date);

-- 3. Insérer les stocks initiaux (niveau de stock de départ)
INSERT INTO stock (id_ingredient, quantity, unit) VALUES
(1, 10.0, 'KG'),   -- Laitue: 10 KG
(2, 15.0, 'KG'),   -- Tomate: 15 KG
(3, 20.0, 'KG'),   -- Poulet: 20 KG
(4, 5.0, 'KG'),    -- Chocolat: 5 KG
(5, 8.0, 'KG')     -- Beurre: 8 KG
    ON CONFLICT (id_ingredient) DO NOTHING;

-- 4. Insérer les mouvements de stock pour les tests
-- Mouvements avant 2024-01-06 12:00
INSERT INTO stock_movement (id_ingredient, quantity, unit, movement_date) VALUES
  -- Achats (quantités positives)
  (1, 5.0, 'KG', '2024-01-01 08:00:00'),    -- Laitue +5 KG
  (2, 10.0, 'KG', '2024-01-01 08:00:00'),   -- Tomate +10 KG
  (3, 15.0, 'KG', '2024-01-02 09:00:00'),   -- Poulet +15 KG

   -- Ventes (quantités négatives)
  (1, -2.0, 'KG', '2024-01-03 12:00:00'),   -- Laitue -2 KG (vente)
  (2, -3.0, 'KG', '2024-01-03 12:30:00'),   -- Tomate -3 KG (vente)
  (3, -5.0, 'KG', '2024-01-04 14:00:00'),   -- Poulet -5 KG (vente)

 -- Autres achats
  (4, 3.0, 'KG', '2024-01-05 10:00:00'),    -- Chocolat +3 KG
  (5, 2.0, 'KG', '2024-01-05 10:00:00'),    -- Beurre +2 KG

  -- Ventes supplémentaires
  (1, -1.0, 'KG', '2024-01-06 11:00:00'),   -- Laitue -1 KG
  (2, -2.0, 'KG', '2024-01-06 11:30:00')    -- Tomate -2 KG
    ON CONFLICT DO NOTHING;

-- 5. Vérification des données
SELECT 'Stock initial:' as info;
SELECT s.id, i.name as ingredient, s.quantity, s.unit
FROM stock s
         JOIN ingredient i ON s.id_ingredient = i.id
ORDER BY i.id;

SELECT 'Mouvements de stock:' as info;
SELECT sm.id, i.name as ingredient, sm.quantity, sm.unit, sm.movement_date
FROM stock_movement sm
         JOIN ingredient i ON sm.id_ingredient = i.id
ORDER BY sm.movement_date, i.id;

-- 6. Calcul du stock à une date donnée (2024-01-06 12:00)
SELECT 'Stock au 2024-01-06 12:00:' as info;
SELECT
    i.id,
    i.name as ingredient,
    s.quantity as stock_initial,
    COALESCE(SUM(sm.quantity), 0) as mouvements_cumules,
    s.quantity + COALESCE(SUM(sm.quantity), 0) as stock_final
FROM ingredient i
         JOIN stock s ON i.id = s.id_ingredient
         LEFT JOIN stock_movement sm ON i.id = sm.id_ingredient
    AND sm.movement_date <= '2024-01-06 12:00:00'
GROUP BY i.id, i.name, s.quantity
ORDER BY i.id;