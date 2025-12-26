
CREATE DATABASE CharityDB;
USE CharityDB;

CREATE TABLE persons (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    address VARCHAR(150),
    needs VARCHAR(150),
    age INT,
    contact VARCHAR(50),
    category VARCHAR(50)
);

CREATE TABLE donations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    donor VARCHAR(100),
    amount DOUBLE,
    description VARCHAR(150),
    type VARCHAR(50)
);
