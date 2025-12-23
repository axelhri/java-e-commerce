-- USERS
INSERT INTO users (id, email, password)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'alice@example.com', '$2a$10$kP9bVDWjeSjhJhqhuRPlmOG8.YmC9uFWBMhuLWfcEIUQVv0LB7R62'),
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

-- CATEGORIES
INSERT INTO categories (id, name)
VALUES
    ('a7a4a8eb-4bc7-4056-a6e0-e13c241ea1e5', 'Homme'),
    ('e0c417f7-83ce-440c-97c1-d5b0e9dbe810', 'Femme'),
    ('046a27cd-4810-4db6-aca3-7ca4336166e7', 'Gaming'),
    ('ba0c5d03-e301-4dbf-b203-d96cda30dec9', 'Sport'),
    ('96ad91c0-c2ad-49c8-aee7-aea5e25d190a', 'Hat'),
    ('84d0b829-ba3f-4c07-8441-279bc706b161', 'Cap'),
    ('f2f4fd38-7dc6-4b2d-b860-958fcdbe68a8', 'Keyboard'),
    ('6edead47-2858-436d-aa86-f8ce1f6496b6', 'Tennis');

-- CATEGORY_PARENT
INSERT INTO category_parent (child_id, parent_id)
VALUES
    ('96ad91c0-c2ad-49c8-aee7-aea5e25d190a', 'a7a4a8eb-4bc7-4056-a6e0-e13c241ea1e5'),
    ('96ad91c0-c2ad-49c8-aee7-aea5e25d190a', 'e0c417f7-83ce-440c-97c1-d5b0e9dbe810'),
    ('6edead47-2858-436d-aa86-f8ce1f6496b6', 'ba0c5d03-e301-4dbf-b203-d96cda30dec9'),
    ('f2f4fd38-7dc6-4b2d-b860-958fcdbe68a8', '046a27cd-4810-4db6-aca3-7ca4336166e7'),
    ('84d0b829-ba3f-4c07-8441-279bc706b161', '96ad91c0-c2ad-49c8-aee7-aea5e25d190a');

-- PRODUCTS
INSERT INTO products (id, name, price, description, vendor_id, category_id)
VALUES
    ('11111111-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Tennis Racket', 15000, 'Professional lightweight tennis racket', 'aaaa1111-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '6edead47-2858-436d-aa86-f8ce1f6496b6'),
    ('22222222-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Mechanical Keyboard', 8000, 'RGB backlit keyboard with blue switches', 'aaaa1111-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'f2f4fd38-7dc6-4b2d-b860-958fcdbe68a8'),
    ('33333333-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Baseball Cap', 1500, 'Comfortable cotton cap', 'aaaa2222-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '84d0b829-ba3f-4c07-8441-279bc706b161');

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
    ('33333333-cccc-cccc-cccc-cccccccccccc', '22222222-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 1);

-- ORDERS
INSERT INTO orders (id, user_id, status)
VALUES
    ('11111111-dddd-dddd-dddd-dddddddddddd', '22222222-2222-2222-2222-222222222222', 'PENDING'),
    ('22222222-dddd-dddd-dddd-dddddddddddd', '33333333-3333-3333-3333-333333333333', 'PENDING');

-- ORDER ITEMS
INSERT INTO order_items (id, order_id, product_id, quantity)
VALUES
    ('11111111-eeee-eeee-eeee-eeeeeeeeeeee', '11111111-dddd-dddd-dddd-dddddddddddd', '11111111-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 1),
    ('22222222-eeee-eeee-eeee-eeeeeeeeeeee', '11111111-dddd-dddd-dddd-dddddddddddd', '33333333-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 1),
    ('33333333-eeee-eeee-eeee-eeeeeeeeeeee', '22222222-dddd-dddd-dddd-dddddddddddd', '22222222-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 2);

-- PRODUCT RATINGS
INSERT INTO products_ratings (id, product_id, user_id, rating)
VALUES
    ('11111111-9999-9999-9999-999999999999', '11111111-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222', 5),
    ('22222222-9999-9999-9999-999999999999', '33333333-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '33333333-3333-3333-3333-333333333333', 4);

-- PRODUCT IMAGES
INSERT INTO products_images (id, image_url, cloudinary_image_id, display_order, product_id)
VALUES
    (gen_random_uuid(), 'mock_image', 'mock_id', 0, '11111111-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
    (gen_random_uuid(), 'mock_image', 'mock_id', 0, '22222222-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
    (gen_random_uuid(), 'mock_image', 'mock_id', 0, '33333333-aaaa-aaaa-aaaa-aaaaaaaaaaaa');

-- VENDOR IMAGES
INSERT INTO vendors_images (id, image_url, cloudinary_image_id, vendor_id)
VALUES
    (gen_random_uuid(), 'mock_image', 'mock_id',  'aaaa1111-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
    (gen_random_uuid(), 'mock_image', 'mock_id',  'aaaa2222-aaaa-aaaa-aaaa-aaaaaaaaaaaa');
