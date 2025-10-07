-- LIMPIAR BASE DE DATOS
DROP TABLE IF EXISTS detalle_pedidos;
DROP TABLE IF EXISTS pedidos;
DROP TABLE IF EXISTS carrito;
DROP TABLE IF EXISTS productos;
DROP TABLE IF EXISTS usuarios;
DROP TABLE IF EXISTS categorias;
DROP DATABASE IF EXISTS tienda_rog;

-- Crear la base de datos
CREATE DATABASE tienda_rog;
USE tienda_rog;

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
    stock INT DEFAULT 0,
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
    direccion TEXT,
    telefono VARCHAR(20),
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_admin BOOLEAN NOT NULL DEFAULT FALSE
);

-- Crear tabla carrito de compras
CREATE TABLE carrito (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    fecha_agregado DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE,
    UNIQUE KEY carrito_unico (usuario_id, producto_id)
);

-- crear tabla pedidos
CREATE TABLE pedidos (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    estado VARCHAR(50) DEFAULT 'pendiente',
    direccion_envio TEXT NOT NULL,
    fecha_pedido DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- crear tabla  detalle de pedidos
CREATE TABLE detalle_pedidos (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    pedido_id INT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES productos(id)
);

-- Insertar datos en categorias
INSERT INTO categorias (id, nombre) VALUES
(1, 'Laptops Gaming'),
(2, 'Tarjetas Gráficas'),
(3, 'Monitores Gaming'),
(4, 'Periféricos'),
(5, 'Teléfonos Gaming');

-- Insertar datos en productos (CON STOCK)
INSERT INTO productos (id, nombre, descripción, precio, stock, categoria_id, imagen_url) VALUES
(1, 'ASUS ROG Strix G15', 'Laptop gaming con procesador Intel Core i7, 16GB RAM, NVIDIA GeForce RTX 3060.', 1299.99, 15, 1, 'https://rog.asus.com/media/1610273282904.jpg'),
(2, 'ASUS ROG Zephyrus G14', 'Laptop gaming ultradelgada con procesador AMD Ryzen 9, 16GB RAM, NVIDIA GeForce RTX 3050.', 1499.99, 10, 1, 'https://sm.mashable.com/mashable_sea/photo/default/images-3_5gq8.jpg'),
(3, 'ASUS ROG Strix GeForce RTX 4070', 'Tarjeta gráfica de alto rendimiento para gaming extremo.', 699.99, 25, 2, 'https://bgamer.pro/wp-content/uploads/2025/01/asus-rogstrix-4070-1.jpg'),
(4, 'ASUS ROG Swift PG279QM', 'Monitor gaming de 27 pulgadas, QHD, 240Hz, G-Sync.', 799.99, 12, 3, 'https://i.rtings.com/assets/products/muhDpBhD/asus-rog-swift-pg279qm/design-medium.jpg?format=auto'),
(5, 'ASUS ROG Claymore II', 'Teclado mecánico gaming modular con switches ROG RX ópticos.', 199.99, 30, 4, 'https://dlcdnwebimgs.asus.com/files/media/F23D0485-8CB9-4CBE-AE6A-23E6B83E8AF2/v1/img/bg/new_kv.png'),
(6, 'ASUS ROG Gladius III Wireless', 'Ratón gaming inalámbrico ligero con sensor de 19,000 dpi.', 99.99, 40, 4, 'https://i.rtings.com/assets/products/1RBzU9HN/asus-rog-gladius-iii-wireless/design-large.jpg?format=auto'),
(7, 'ASUS ROG Strix Scar 17', 'Laptop gaming de 17 pulgadas con Intel Core i9, 32GB RAM, NVIDIA GeForce RTX 4080.', 2499.99, 8, 1, 'https://cdn.mos.cms.futurecdn.net/uxst8zpkMv7eCgW6feemZG.jpg'),
(8, 'ASUS ROG Flow X13', 'Laptop gaming convertible con AMD Ryzen 9, 16GB RAM, NVIDIA GeForce RTX 3050 Ti.', 1799.99, 12, 1, 'https://i.rtings.com/assets/products/XiiQ8wbP/asus-rog-flow-x13-2023/design-medium.jpg?format=auto'),
(9, 'ASUS ROG Strix GeForce RTX 4090', 'Tarjeta gráfica flagship para gaming y creación de contenido.', 1599.99, 5, 2, 'https://m.media-amazon.com/images/I/71Bi6UsIoIL.jpg'),
(10, 'ASUS ROG Swift OLED PG27AQDM', 'Monitor OLED gaming de 27 pulgadas, 240Hz, HDR.', 899.99, 18, 3, 'https://rog.asus.com/media/1672711969695.jpg'),
(11, 'ASUS ROG Azoth', 'Teclado mecánico gaming inalámbrico con switches gasket mount.', 249.99, 22, 4, 'https://rog.asus.com/media/1683597558743.jpg'),
(12, 'ASUS ROG Chakram X', 'Ratón gaming inalámbrico con joystick y carga inalámbrica Qi.', 149.99, 35, 4, 'https://dlcdnwebimgs.asus.com/files/media/13D84A7F-F078-4A7F-95B5-2A6B2834B460/v1/img/aura/bg-aura.png'),
(13, 'ASUS ROG Phone 8 Pro', 'Teléfono gaming con Snapdragon 8 Gen 3, 24GB RAM, pantalla de 165Hz.', 1299.99, 20, 5, 'https://i.blogs.es/106bc5/asus-rog-phone-8-02/840_560.jpeg'),
(14, 'ASUS ROG Phone 7 Ultimate', 'Teléfono gaming con Snapdragon 8 Gen 2, 16GB RAM, sistema de refrigeración avanzado.', 999.99, 15, 5, 'https://www.stuff.tv/wp-content/uploads/sites/2/2023/04/Asus-ROG-Phone-7-Ultimate-rear.jpg');

-- Insertar datos en usuarios
INSERT INTO usuarios (nombre, email, password, direccion, telefono, is_admin) VALUES
('Admin', 'admin@admin.com', 'admin123', 'Oficina Central', '66666666', TRUE),
('Marshel', 'marshel@tecba.com', 'marshel123', 'Av. Principal 123, La Paz', '77123456', FALSE),
('Adán', 'adan@tecba.com', 'adan123', 'Calle Comercio 456, La Paz', '77234567', FALSE),
('Rommel', 'rommel@tecba.com', 'rommel123', 'Zona Central 789, La Paz', '77345678', FALSE);

-- Insertar datos de ejemplo en carrito
INSERT INTO carrito (usuario_id, producto_id, cantidad) VALUES
(2, 1, 1),
(2, 5, 2),
(3, 3, 1);

-- Insertar pedidos de ejemplo
INSERT INTO pedidos (usuario_id, total, estado, direccion_envio) VALUES
(2, 1699.97, 'completado', 'Av. Principal 123, La Paz'),
(3, 699.99, 'pendiente', 'Calle Comercio 456, La Paz');

-- Insertar detalle de pedidos
INSERT INTO detalle_pedidos (pedido_id, producto_id, cantidad, precio_unitario, subtotal) VALUES
(1, 1, 1, 1299.99, 1299.99),
(1, 5, 2, 199.99, 399.98),
(2, 3, 1, 699.99, 699.99);
