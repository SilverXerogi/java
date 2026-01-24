
DROP TABLE IF EXISTS PC;
DROP TABLE IF EXISTS Laptop;
DROP TABLE IF EXISTS Printer;
DROP TABLE IF EXISTS Product;


CREATE TABLE Product (
    maker   VARCHAR(10) NOT NULL,
    model   INTEGER PRIMARY KEY,
    type    VARCHAR(7) CHECK (type IN ('PC', 'Laptop', 'Printer'))
);


CREATE TABLE PC (
    code    INTEGER PRIMARY KEY,
    model   INTEGER NOT NULL REFERENCES Product(model),
    speed   INTEGER NOT NULL,
    ram     INTEGER NOT NULL,
    hd      INTEGER NOT NULL,
    cd      VARCHAR(10) NOT NULL,
    price   NUMERIC(10,2) NOT NULL
);
CREATE TABLE Laptop (
    code    INTEGER PRIMARY KEY,
    model   INTEGER NOT NULL REFERENCES Product(model),
    speed   INTEGER NOT NULL,
    ram     INTEGER NOT NULL,
    hd      INTEGER NOT NULL,
    screen  NUMERIC(3,1) NOT NULL,
    price   NUMERIC(10,2) NOT NULL
);
CREATE TABLE Printer (
    code    INTEGER PRIMARY KEY,
    model   INTEGER NOT NULL REFERENCES Product(model),
    color   CHAR(1) CHECK (color IN ('y', 'n')),
    type    VARCHAR(10) CHECK (type IN ('Laser', 'Jet', 'Matrix')),
    price   NUMERIC(10,2) NOT NULL
);