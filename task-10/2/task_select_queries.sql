
-- 1. Найти номер модели, скорость и размер жесткого диска для всех ПК стоимостью менее 500 долларов.
SELECT model, speed, hd
FROM PC
WHERE price < 500;

-- 2. Найти производителей принтеров. Вывести поля: maker.
SELECT DISTINCT maker
FROM Product
WHERE type = 'Printer';

-- 3. Найти номер модели, объем памяти и размеры экранов ноутбуков, цена которых превышает 1000 долларов.
SELECT model, ram, screen
FROM Laptop
WHERE price > 1000;

-- 4. Найти все записи таблицы Printer для цветных принтеров.
SELECT *
FROM Printer
WHERE color = 'y';

-- 5. Найти номер модели, скорость и размер жесткого диска для ПК, имеющих скорость cd 12x или 24x и цену менее 600 долларов.
SELECT model, speed, hd
FROM PC
WHERE cd IN ('12x', '24x') AND price < 600;

-- 6. Указать производителя и скорость для тех ноутбуков, которые имеют жесткий диск объемом не менее 100 Гбайт.
SELECT p.maker, l.speed
FROM Laptop l
JOIN Product p ON l.model = p.model
WHERE l.hd >= 100;

-- 7. Найти номера моделей и цены всех продуктов (любого типа), выпущенных производителем B.
SELECT p.model, pc.price
FROM Product p
JOIN PC pc ON p.model = pc.model
WHERE p.maker = 'B'

UNION

SELECT p.model, l.price
FROM Product p
JOIN Laptop l ON p.model = l.model
WHERE p.maker = 'B'

UNION

SELECT p.model, pr.price
FROM Product p
JOIN Printer pr ON p.model = pr.model
WHERE p.maker = 'B';

-- 8. Найти производителя, выпускающего ПК, но не ноутбуки.
SELECT DISTINCT maker
FROM Product
WHERE type = 'PC'
EXCEPT
SELECT DISTINCT maker
FROM Product
WHERE type = 'Laptop';

-- 9. Найти производителей ПК с процессором не менее 450 Мгц. Вывести поля: maker.
SELECT DISTINCT p.maker
FROM Product p
JOIN PC pc ON p.model = pc.model
WHERE pc.speed >= 450;

-- 10. Найти принтеры, имеющие самую высокую цену. Вывести поля: model, price.
SELECT model, price
FROM Printer
WHERE price = (SELECT MAX(price) FROM Printer);

-- 11. Найти среднюю скорость ПК.
SELECT AVG(speed) AS avg_speed
FROM PC;

-- 12. Найти среднюю скорость ноутбуков, цена которых превышает 1000 долларов.
SELECT AVG(speed) AS avg_speed
FROM Laptop
WHERE price > 1000;

-- 13. Найти среднюю скорость ПК, выпущенных производителем A.
SELECT AVG(pc.speed) AS avg_speed
FROM PC pc
JOIN Product p ON pc.model = p.model
WHERE p.maker = 'A';

-- 14. Для каждого значения скорости процессора найти среднюю стоимость ПК с такой же скоростью.
SELECT speed, AVG(price) AS avg_price
FROM PC
GROUP BY speed;

-- 15. Найти размеры жестких дисков, совпадающих у двух и более PC.
SELECT hd
FROM PC
GROUP BY hd
HAVING COUNT(*) >= 2;

-- 16. Найти пары моделей PC с одинаковыми speed и ram (каждая пара один раз).
SELECT p1.model AS model_high, p2.model AS model_low, p1.speed, p1.ram
FROM PC p1
JOIN PC p2 ON p1.speed = p2.speed AND p1.ram = p2.ram AND p1.model > p2.model
ORDER BY model_high, model_low;

-- 17. Найти модели ноутбуков, скорость которых меньше скорости любого из ПК.
SELECT 'Laptop' AS type, model, speed
FROM Laptop
WHERE speed < (SELECT MIN(speed) FROM PC);

-- 18. Найти производителей самых дешевых цветных принтеров.
SELECT p.maker, pr.price
FROM Printer pr
JOIN Product p ON pr.model = p.model
WHERE pr.color = 'y'
  AND pr.price = (
    SELECT MIN(price)
    FROM Printer
    WHERE color = 'y'
  );

-- 19. Для каждого производителя — средний размер экрана его ноутбуков.
SELECT p.maker, AVG(l.screen) AS avg_screen
FROM Product p
JOIN Laptop l ON p.model = l.model
GROUP BY p.maker;

-- 20. Производители, выпускающие ≥3 моделей ПК.
SELECT p.maker, COUNT(p.model) AS model_count
FROM Product p
WHERE p.type = 'PC'
GROUP BY p.maker
HAVING COUNT(p.model) >= 3;

-- 21. Максимальная цена ПК для каждого производителя.
SELECT p.maker, MAX(pc.price) AS max_price
FROM Product p
JOIN PC pc ON p.model = pc.model
WHERE p.type = 'PC'
GROUP BY p.maker;

-- 22. Средняя цена ПК с speed > 600, сгруппированная по speed.
SELECT speed, AVG(price) AS avg_price
FROM PC
WHERE speed > 600
GROUP BY speed;

-- 23. Производители, выпускающие и ПК, и ноутбуки со speed ≥ 750.
SELECT DISTINCT p1.maker
FROM Product p1
JOIN PC pc ON p1.model = pc.model
WHERE p1.type = 'PC'
  AND pc.speed >= 750
  AND p1.maker IN (
    SELECT p2.maker
    FROM Product p2
    JOIN Laptop l ON p2.model = l.model
    WHERE p2.type = 'Laptop' AND l.speed >= 750
  );

-- 24. Модели (любого типа) с самой высокой ценой в базе.
WITH all_products AS (
  SELECT model, price FROM PC
  UNION ALL
  SELECT model, price FROM Laptop
  UNION ALL
  SELECT model, price FROM Printer
)
SELECT model
FROM all_products
WHERE price = (SELECT MAX(price) FROM all_products);

-- 25. Производители принтеров, которые делают ПК с минимальным RAM и максимальной speed среди таких.
WITH min_ram AS (
  SELECT MIN(ram) AS ram_val FROM PC
),
target_pc AS (
  SELECT pc.model
  FROM PC pc, min_ram mr
  WHERE pc.ram = mr.ram_val
    AND pc.speed = (
      SELECT MAX(speed)
      FROM PC
      WHERE ram = mr.ram_val
    )
),
makers_of_target_pc AS (
  SELECT DISTINCT pr.maker
  FROM Product pr
  JOIN target_pc t ON pr.model = t.model
)
SELECT DISTINCT m.maker
FROM makers_of_target_pc m
WHERE m.maker IN (
  SELECT DISTINCT maker
  FROM Product
  WHERE type = 'Printer'
);