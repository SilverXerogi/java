-- Таблица книг
CREATE TABLE IF NOT EXISTS books (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    publication_date DATE,
    arrival_date DATE,
    description TEXT
);

-- Таблица заказов
CREATE TABLE IF NOT EXISTS orders (
    id SERIAL PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_price NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP NULL
);

-- Связь заказов и книг
CREATE TABLE IF NOT EXISTS order_items (
    order_id INTEGER,
    book_id INTEGER,
    quantity INTEGER NOT NULL,
    PRIMARY KEY (order_id, book_id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);

-- Таблица заявок
CREATE TABLE IF NOT EXISTS book_requests (
    id SERIAL PRIMARY KEY,
    book_id INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    request_count INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP NULL,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);