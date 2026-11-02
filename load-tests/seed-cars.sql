-- Adds available cars :from..:to of the seeded BMW model with predictable ids, so every
-- k6 iteration can order its own car: cccccccc-0000-4000-8000-<12-digit number>.
-- The schema of `cars` is the same in the monolith and in storage-service.
INSERT INTO cars (id, created_at, updated_at, removed, car_model_id, vin, color, price,
                  available, available_for_test_drive)
SELECT ('cccccccc-0000-4000-8000-' || lpad(n::text, 12, '0'))::uuid,
       now(), now(), false,
       'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1',
       'VIN-LOAD-' || n, 'BLACK', 3500000, true, false
FROM generate_series(:from, :to) AS n
ON CONFLICT DO NOTHING;
