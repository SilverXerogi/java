-- Вставка книг
INSERT INTO books (title, status, price, publication_date, arrival_date, description) VALUES
('Война и мир', 'AVAILABLE', 500.00, '1869-01-01', '2025-01-01', 'Эпопея Л.Н. Толстого'),
('Преступление и наказание', 'AVAILABLE', 400.00, '1866-01-01', '2025-01-01', 'Роман Ф.М. Достоевского'),
('Мастер и Маргарита', 'ABSENT', 450.00, '1967-01-01', '2025-01-01', 'Роман М.А. Булгакова');

-- Вставка заказов
INSERT INTO orders (customer_name, status, total_price) VALUES
('Иван Иванов', 'CREATED', 900.00),
('Петр Петров', 'PAID', 400.00);

-- Вставка связей заказов и книг
INSERT INTO order_items (order_id, book_id, quantity) VALUES
(1, 1, 1), -- заказ 1: 1 шт "Война и мир"
(1, 2, 1), -- заказ 1: 1 шт "Преступление и наказание"
(2, 2, 1); -- заказ 2: 1 шт "Преступление и наказание"

-- Вставка заявок
INSERT INTO book_requests (book_id, status, request_count) VALUES
(3, 'OPEN', 2); -- заявка на "Мастер и Маргарита"