-- Expense Tracker — optional database setup.
-- The app creates the database and table automatically on first run
-- (createDatabaseIfNotExist=true + ddl-auto=update), so this script is only
-- needed if you want to set up the schema (and some sample data) by hand.

CREATE DATABASE IF NOT EXISTS expense_db;
USE expense_db;

CREATE TABLE IF NOT EXISTS expenses (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    description VARCHAR(255) NOT NULL,
    amount      DECIMAL(10,2) NOT NULL,
    category    VARCHAR(20)  NOT NULL,
    date        DATE         NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT expenses_amount_chk CHECK (amount >= 0.01)
);

-- Sample data (safe to delete)
INSERT INTO expenses (description, amount, category, date) VALUES
    ('Groceries at Big Bazaar', 49.99, 'FOOD', '2026-11-13'),
    ('Metro card top-up', 20.00, 'TRANSPORT', '2026-11-12'),
    ('Electricity bill', 85.50, 'UTILITIES', '2026-11-10'),
    ('Movie tickets', 15.00, 'ENTERTAINMENT', '2026-11-09');
