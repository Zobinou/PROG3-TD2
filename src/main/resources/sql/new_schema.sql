-- 1. Ajouter la colonne price (prix de vente) à la table dish
ALTER TABLE dish ADD COLUMN IF NOT EXISTS price NUMERIC(12, 2);

-- 2. Supprimer la colonne id_dish de la table ingredient
ALTER TABLE ingredient DROP COLUMN IF EXISTS id_dish;

-- 3. Supprimer la colonne required_quantity si elle existe
ALTER TABLE ingredient DROP COLUMN IF EXISTS required_quantity;

-- 4. Créer la table de jointure dish_ingredient
DROP TABLE IF EXISTS dish_ingredient CASCADE;

CREATE TABLE dish_ingredient(
    id SERIAL PRIMARY KEY,
    id_dish INTEGER NOT NULL REFERENCES dish(id) ON DELETE CASCADE,
    id_ingredient INTEGER NOT NULL REFERENCES ingredient(id) ON DELETE CASCADE,
    quantity_required NUMERIC(10,3) NOT NULL CHECK (quantity_required > 0),
    unit VARCHAR(10) NOT NULL CHECK (unit IN ('KG','L','PCS')),
    CONSTRAINT unique_dish_ingredient UNIQUE (id_dish, id_ingredient)
);

-- 5. Mettre à jour les prix de vente des plats
UPDATE dish SET price = 3500.00 WHERE id = 1;
UPDATE dish SET price = 12000.00 WHERE id = 2;
UPDATE dish SET price = NULL WHERE id = 3;
UPDATE dish SET price = 8000.00 WHERE id = 4;
UPDATE dish SET price = NULL WHERE id = 5;

-- 6. Insérer les associations dans dish_ingredient
INSERT INTO dish_ingredient (id_dish, id_ingredient, quantity_required, unit)
VALUES (1, 1, 0.20, 'KG');

INSERT INTO dish_ingredient (id_dish, id_ingredient, quantity_required, unit)
VALUES (1, 2, 0.15, 'KG');

INSERT INTO dish_ingredient (id_dish, id_ingredient, quantity_required, unit)
VALUES (2, 3, 1.00, 'KG');

INSERT INTO dish_ingredient (id_dish, id_ingredient, quantity_required, unit)
VALUES (4, 4, 0.30, 'KG');

INSERT INTO dish_ingredient (id_dish, id_ingredient, quantity_required, unit)
VALUES (4, 5, 0.20, 'KG');

-- 7. Vérification des données insérées
SELECT 'Verification des plats:' as info;
SELECT id, name, dish_type, price FROM dish ORDER BY id;

SELECT 'Verification des ingredients:' as info;
SELECT id, name, category, price FROM ingredient ORDER BY id;

SELECT 'Verification des associations dish_ingredient:' as info;
SELECT di.id, d.name as plat, i.name as ingredient, di.quantity_required, di.unit
FROM dish_ingredient di
         JOIN dish d ON di.id_dish = d.id
         JOIN ingredient i ON di.id_ingredient = i.id
ORDER BY di.id;