ALTER TABLE users ADD COLUMN is_mail_confirmed BOOLEAN;

UPDATE users SET is_mail_confirmed = false;

ALTER TABLE users ALTER COLUMN is_mail_confirmed SET NOT NULL;