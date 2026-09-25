-- Sample data for local development. Run with `npm run db:seed` (Postgres must be running and migrated).
-- Replaces every product, so ids always start at 1 and the result is the same on every run.
-- Not a Flyway migration on purpose: migrations run in every environment, sample data must not.

BEGIN;

TRUNCATE products RESTART IDENTITY;

-- Realistic names, including characters a name must be allowed to contain.
INSERT INTO products (name) VALUES
    ('Cordless Drill 18V'),
    ('Claw Hammer 16 oz'),
    ('Adjustable Wrench 10"'),
    ('3/4" Hex Bolt, Grade 8'),
    ('Nut & Washer Assortment Kit'),
    ('O''Brien Needle-Nose Pliers'),
    ('Safety Glasses (Clear)'),
    ('Nitrile Gloves, Size L — Box of 100'),
    ('M8×1.25 Socket Head Cap Screw'),
    ('Café Break Room Mug'),
    ('LED Shop Light 4 ft'),
    ('Extension Cord 50 ft, 12/3 AWG'),
    ('Utility Knife + 10 Blades'),
    ('Digital Multimeter'),
    ('Pipe Wrench 14"'),
    ('WD-40 Multi-Use 12 oz'),
    ('Duct Tape, Silver, 2" × 60 yd'),
    ('Ladder, Fiberglass, 6 ft'),
    ('Shop Vacuum 5 gal'),
    ('Torque Wrench ½" Drive'),
    -- Longest allowed name (256 characters), to exercise truncation and expand-on-click.
    (rpad('Heavy-Duty Industrial Storage Cabinet with Locking Doors', 256, ', and more'));

-- Bulk rows up to 250 products in total, so every page size (10, 50, 100) has several pages.
-- Counting from the rows above keeps the total at 250 when names are added to or removed from the list.
INSERT INTO products (name)
SELECT 'Sample product ' || n FROM generate_series((SELECT count(*) FROM products) + 1, 250) AS n;

COMMIT;
