INSERT INTO users (email, password) VALUES
    ('user@example.com', '$2a$10$w7ty1skmOPy.5T5PZ1dK5ePoqcWq1bZr4N7DlD/J.0AibFtB7DC8G'),
    ('admin@example.com', '$2a$10$w7ty1skmOPy.5T5PZ1dK5ePoqcWq1bZr4N7DlD/J.0AibFtB7DC8G');

INSERT INTO user_roles (user_id, role)
SELECT id, 'USER' FROM users;

INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE email = 'admin@example.com';
