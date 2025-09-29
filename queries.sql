-- LIMPIAR BASE DE DATOS (para re-seedear)
DROP TABLE IF EXISTS productos;
DROP TABLE IF EXISTS usuarios;
DROP TABLE IF EXISTS administradores;
DROP TABLE IF EXISTS categorias;
DROP DATABASE IF EXISTS tienda_rog;

-- Crear la base de datos
CREATE DATABASE tienda_rog;
USE tienda_rog;

-- Crear tabla administradores
CREATE TABLE administradores (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL
);

-- Crear tabla categorias
CREATE TABLE categorias (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL UNIQUE
);

-- Crear tabla productos
CREATE TABLE productos (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255),
    descripción TEXT,
    precio DECIMAL(10,2),
    categoria_id INT,
    imagen_url TEXT,
    FOREIGN KEY (categoria_id) REFERENCES categorias(id)
);

-- Crear tabla usuarios
CREATE TABLE usuarios (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Insertar datos en administradores
INSERT INTO administradores (email, password, rol) VALUES
('admin@admin.com', 'admin123', 'admin');

-- Insertar datos en categorias
INSERT INTO categorias (id, nombre) VALUES
(1, 'Laptops Gaming'),
(2, 'Tarjetas Gráficas'),
(3, 'Monitores Gaming'),
(4, 'Periféricos'),
(5, 'Teléfonos Gaming');

-- Insertar datos en productos
INSERT INTO productos (id, nombre, descripción, precio, categoria_id, imagen_url) VALUES
(1, 'ASUS ROG Strix G15', 'Laptop gaming con procesador Intel Core i7, 16GB RAM, NVIDIA GeForce RTX 3060.', 1299.99, 1, 'https://rog.asus.com/media/1610273282904.jpg'),
(2, 'ASUS ROG Zephyrus G14', 'Laptop gaming ultradelgada con procesador AMD Ryzen 9, 16GB RAM, NVIDIA GeForce RTX 3050.', 1499.99, 1, 'https://sm.mashable.com/mashable_sea/photo/default/images-3_5gq8.jpg'),
(3, 'ASUS ROG Strix GeForce RTX 4070', 'Tarjeta gráfica de alto rendimiento para gaming extremo.', 699.99, 2, 'https://bgamer.pro/wp-content/uploads/2025/01/asus-rogstrix-4070-1.jpg'),
(4, 'ASUS ROG Swift PG279QM', 'Monitor gaming de 27 pulgadas, QHD, 240Hz, G-Sync.', 799.99, 3, 'https://i.rtings.com/assets/products/muhDpBhD/asus-rog-swift-pg279qm/design-medium.jpg?format=auto'),
(5, 'ASUS ROG Claymore II', 'Teclado mecánico gaming modular con switches ROG RX ópticos.', 199.99, 4, 'https://dlcdnwebimgs.asus.com/files/media/F23D0485-8CB9-4CBE-AE6A-23E6B83E8AF2/v1/img/bg/new_kv.png'),
(6, 'ASUS ROG Gladius III Wireless', 'Ratón gaming inalámbrico ligero con sensor de 19,000 dpi.', 99.99, 4, 'https://i.rtings.com/assets/products/1RBzU9HN/asus-rog-gladius-iii-wireless/design-large.jpg?format=auto'),
-- NUEVOS PRODUCTOS CON URLs DE IMÁGENES
(7, 'ASUS ROG Strix Scar 17', 'Laptop gaming de 17 pulgadas con Intel Core i9, 32GB RAM, NVIDIA GeForce RTX 4080.', 2499.99, 1, 'https://cdn.mos.cms.futurecdn.net/uxst8zpkMv7eCgW6feemZG.jpg'),
(8, 'ASUS ROG Flow X13', 'Laptop gaming convertible con AMD Ryzen 9, 16GB RAM, NVIDIA GeForce RTX 3050 Ti.', 1799.99, 1, 'https://i.rtings.com/assets/products/XiiQ8wbP/asus-rog-flow-x13-2023/design-medium.jpg?format=auto'),
(9, 'ASUS ROG Strix GeForce RTX 4090', 'Tarjeta gráfica flagship para gaming y creación de contenido.', 1599.99, 2, 'https://m.media-amazon.com/images/I/71Bi6UsIoIL.jpg'),
(10, 'ASUS ROG Swift OLED PG27AQDM', 'Monitor OLED gaming de 27 pulgadas, 240Hz, HDR.', 899.99, 3, 'https://rog.asus.com/media/1672711969695.jpg'),
(11, 'ASUS ROG Azoth', 'Teclado mecánico gaming inalámbrico con switches gasket mount.', 249.99, 4, 'https://rog.asus.com/media/1683597558743.jpg'),
(12, 'ASUS ROG Chakram X', 'Ratón gaming inalámbrico con joystick y carga inalámbrica Qi.', 149.99, 4, 'https://dlcdnwebimgs.asus.com/files/media/13D84A7F-F078-4A7F-95B5-2A6B2834B460/v1/img/aura/bg-aura.png'),
(13, 'ASUS ROG Phone 8 Pro', 'Teléfono gaming con Snapdragon 8 Gen 3, 24GB RAM, pantalla de 165Hz.', 1299.99, 5, 'https://i.blogs.es/106bc5/asus-rog-phone-8-02/840_560.jpeg'),
(14, 'ASUS ROG Phone 7 Ultimate', 'Teléfono gaming con Snapdragon 8 Gen 2, 16GB RAM, sistema de refrigeración avanzado.', 999.99, 5, 'https://www.stuff.tv/wp-content/uploads/sites/2/2023/04/Asus-ROG-Phone-7-Ultimate-rear.jpg');

-- Insertar datos en usuarios
INSERT INTO usuarios (nombre, email, password) VALUES
('Marshel', 'marshel@tecba.com', 'marshel123'),
('Adán', 'adan@tecba.com', 'adan123'),
('Rommel', 'rommel@tecba.com', 'rommel123');