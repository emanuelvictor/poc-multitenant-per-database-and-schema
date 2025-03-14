CREATE TABLE tenant
(
    schema   VARCHAR(255) PRIMARY KEY,
    database VARCHAR(255) NOT NULL,
    address  VARCHAR(255) NOT NULL
);