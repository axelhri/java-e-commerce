-- USERS
INSERT INTO users (id, email, password)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'alice@example.com', '$2a$10$w7ty1skmOPy.5T5PZ1dK5ePoqcWq1bZr4N7DlD/J.0AibFtB7DC8G'),
    ('22222222-2222-2222-2222-222222222222', 'bob@example.com', '$2a$10$w7ty1skmOPy.5T5PZ1dK5ePoqcWq1bZr4N7DlD/J.0AibFtB7DC8G'),
    ('33333333-3333-3333-3333-333333333333', 'charlie@example.com', '$2a$10$w7ty1skmOPy.5T5PZ1dK5ePoqcWq1bZr4N7DlD/J.0AibFtB7DC8G');

-- USER ROLES
INSERT INTO user_roles (user_id, role)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'ADMIN'),
    ('22222222-2222-2222-2222-222222222222', 'USER'),
    ('33333333-3333-3333-3333-333333333333', 'USER');

-- VENDORS
INSERT INTO vendors (id, name)
VALUES
    ('aaaa1111-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'TechZone'),
    ('aaaa2222-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'GreenMarket');

-- PRODUCTS
INSERT INTO products (id, name, price, description, vendor_id)
VALUES
    ('11111111-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Wireless Mouse', 2500, 'Ergonomic and rechargeable wireless mouse', 'aaaa1111-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
    ('22222222-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Mechanical Keyboard', 8000, 'RGB backlit keyboard with blue switches', 'aaaa1111-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
    ('33333333-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Organic Coffee Beans', 1500, 'Freshly roasted 500g coffee beans', 'aaaa2222-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
    ('44444444-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Reusable Water Bottle', 1200, 'Stainless steel eco-friendly bottle', 'aaaa2222-aaaa-aaaa-aaaa-aaaaaaaaaaaa');

-- CARTS
INSERT INTO carts (id, user_id)
VALUES
    ('11111111-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222'),
    ('22222222-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '33333333-3333-3333-3333-333333333333');

-- CART ITEMS
INSERT INTO cart_items (id, cart_id, product_id, quantity)
VALUES
    ('11111111-cccc-cccc-cccc-cccccccccccc', '11111111-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '11111111-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 1),
    ('22222222-cccc-cccc-cccc-cccccccccccc', '11111111-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '33333333-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 2),
    ('33333333-cccc-cccc-cccc-cccccccccccc', '22222222-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '44444444-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 1);

-- ORDERS
INSERT INTO orders (id, user_id)
VALUES
    ('11111111-dddd-dddd-dddd-dddddddddddd', '22222222-2222-2222-2222-222222222222'),
    ('22222222-dddd-dddd-dddd-dddddddddddd', '33333333-3333-3333-3333-333333333333');

-- ORDER ITEMS
INSERT INTO order_items (id, order_id, product_id, quantity)
VALUES
    ('11111111-eeee-eeee-eeee-eeeeeeeeeeee', '11111111-dddd-dddd-dddd-dddddddddddd', '11111111-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 1),
    ('22222222-eeee-eeee-eeee-eeeeeeeeeeee', '11111111-dddd-dddd-dddd-dddddddddddd', '33333333-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 1),
    ('33333333-eeee-eeee-eeee-eeeeeeeeeeee', '22222222-dddd-dddd-dddd-dddddddddddd', '44444444-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 2);

-- VENDOR RATINGS
INSERT INTO vendors_ratings (id, vendor_id, user_id, rating)
VALUES
    ('11111111-ffff-ffff-ffff-ffffffffffff', 'aaaa1111-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222', 5),
    ('22222222-ffff-ffff-ffff-ffffffffffff', 'aaaa2222-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '33333333-3333-3333-3333-333333333333', 4);

-- PRODUCT RATINGS
INSERT INTO products_ratings (id, product_id, user_id, rating)
VALUES
    ('11111111-9999-9999-9999-999999999999', '11111111-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222', 5),
    ('22222222-9999-9999-9999-999999999999', '33333333-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '33333333-3333-3333-3333-333333333333', 4);
