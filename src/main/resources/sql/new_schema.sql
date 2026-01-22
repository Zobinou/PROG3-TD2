/*ajoute du prix de vente a la table dish */
ALTER TABLE dish ADD COLUMN IF NOT EXISTS selling_price NUMERIC(12, 2);

/*supprime de la  colonne id_dish*/
ALTER TABLE  ingredient DROP COLUMN IF EXISTS id_dish;

/*creation de la table de jointure */
CREATE TABLE IF NOT EXISTS dish_ingredient(
    id SERIAL PRIMARY key,
    id_dish INTEGER NOT NULL REFERENCES  dish(id) ON DELETE CASCADE,
    id_ingredient INTEGER NOT NULL REFERENCES  ingredient(id) ON DELETE CASCADE,
    quantity_required NUMERIC(10,3) NOT NULL CHECK (quantity_required > 0),
    unit VARCHAR (10) NOT NULL CHECK (unit IN ('KG','L','PCS')),

    CONSTRAINT unique_dish_ingredient UNIQUE (id_dish,id_ingredient)
);

/*Insertion/ mise a jours des plats avec prix de ventes */
INSERT INTO  dish (id,name,dish_type,selling-price)VALUES
     (1,'Salade fraiche','STARTER',3500.00),
     (2,'Poulet grille','MAIN',12000.00),
     (3,'Riz aux legumes','MAIN',NULL),
     (4,'Gateau aux chocolat','DESSERT',8000.00),
     (5,'Salade de fruits','DESSERT',NULL)
ON CONFLICT (id) DO UPDATE SET
        name = EXCLUDED.name,
        dish_type = EXCLUDED.dish_type,
        selling_price = EXCLUDED.selling_price;

/*insertion des associations dans dish_ingredient*/
INSERT INTO  dish_ingredient (id,id_dish,id_ingredient,quantity_required,unit)VALUES
    (1, 1, 1, 0.20, 'KG'),   -- Salade fraîche → 0.20 kg Laitue
    (2, 1, 2, 0.15, 'KG'),   -- Salade fraîche → 0.15 kg Tomate
    (3, 2, 3, 1.00, 'KG'),   -- Poulet grillé   → 1.00 kg Poulet
    (4, 4, 4, 0.30, 'KG'),   -- Gâteau chocolat → 0.30 kg Chocolat
    (5, 4, 5, 0.20, 'KG')    -- Gâteau chocolat → 0.20 kg Fruits
    ON CONFLICT (id) DO NOTHING;