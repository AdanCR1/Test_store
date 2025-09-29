<?php
require_once 'config.php';

// GET - Obtener productos (todos o por ID)
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    if (isset($_GET['id'])) {
        // Obtener un solo producto por ID
        $id = $_GET['id'];
        $stmt = $pdo->prepare("
            SELECT p.*, c.nombre as categoria_nombre
            FROM productos p
            LEFT JOIN categorias c ON p.categoria_id = c.id
            WHERE p.id = ?
        ");
        $stmt->execute([$id]);
        $product = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($product) {
            echo json_encode(['success' => true, 'data' => $product]);
        } else {
            echo json_encode(['success' => false, 'message' => 'Producto no encontrado']);
        }
    } else {
        // Obtener todos los productos
        $stmt = $pdo->query("
            SELECT p.*, c.nombre as categoria_nombre
            FROM productos p
            LEFT JOIN categorias c ON p.categoria_id = c.id
            ORDER BY p.id DESC
        ");
        $products = $stmt->fetchAll(PDO::FETCH_ASSOC);
        echo json_encode(['success' => true, 'data' => $products]);
    }
}

// POST - Crear producto
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);

    $nombre = $data['nombre'] ?? '';
    $precio = $data['precio'] ?? 0;
    $descripcion = $data['descripcion'] ?? '';
    $imagen_url = $data['imagen_url'] ?? null;
    $categoria_nombre = $data['categoria_id'] ?? 'General'; // Recibimos un nombre de categoría

    // Buscar el ID de la categoría. Si no existe, la creamos.
    $stmt_cat = $pdo->prepare("SELECT id FROM categorias WHERE nombre = ?");
    $stmt_cat->execute([$categoria_nombre]);
    $categoria = $stmt_cat->fetch(PDO::FETCH_ASSOC);

    if ($categoria) {
        $categoria_id_final = $categoria['id'];
    } else {
        $stmt_new_cat = $pdo->prepare("INSERT INTO categorias (nombre) VALUES (?)");
        $stmt_new_cat->execute([$categoria_nombre]);
        $categoria_id_final = $pdo->lastInsertId();
    }

    $stmt = $pdo->prepare("INSERT INTO productos (nombre, precio, descripción, imagen_url, categoria_id) VALUES (?, ?, ?, ?, ?)");

    if ($stmt->execute([$nombre, $precio, $descripcion, $imagen_url, $categoria_id_final])) {
        echo json_encode(['success' => true, 'message' => 'Producto creado exitosamente']);
    } else {
        echo json_encode(['success' => false, 'message' => 'Error al crear producto']);
    }
}

// PUT - Actualizar producto
if ($_SERVER['REQUEST_METHOD'] === 'PUT') {
    $data = json_decode(file_get_contents('php://input'), true);

    $id = $data['id'] ?? 0;
    $nombre = $data['nombre'] ?? '';
    $precio = $data['precio'] ?? 0;
    $descripcion = $data['descripcion'] ?? '';
    $imagen_url = $data['imagen_url'] ?? null;
    $categoria_nombre = $data['categoria_id'] ?? 'General'; // Usar nombre como en POST

    // Buscar el ID de la categoría. Si no existe, la creamos.
    $stmt_cat = $pdo->prepare("SELECT id FROM categorias WHERE nombre = ?");
    $stmt_cat->execute([$categoria_nombre]);
    $categoria = $stmt_cat->fetch(PDO::FETCH_ASSOC);

    if ($categoria) {
        $categoria_id_final = $categoria['id'];
    } else {
        $stmt_new_cat = $pdo->prepare("INSERT INTO categorias (nombre) VALUES (?)");
        $stmt_new_cat->execute([$categoria_nombre]);
        $categoria_id_final = $pdo->lastInsertId();
    }

    $stmt = $pdo->prepare("UPDATE productos SET nombre = ?, precio = ?, descripción = ?, imagen_url = ?, categoria_id = ? WHERE id = ?");

    if ($stmt->execute([$nombre, $precio, $descripcion, $imagen_url, $categoria_id_final, $id])) {
        echo json_encode(['success' => true, 'message' => 'Producto actualizado exitosamente']);
    } else {
        echo json_encode(['success' => false, 'message' => 'Error al actualizar producto']);
    }
}

// DELETE - Eliminar producto
if ($_SERVER['REQUEST_METHOD'] === 'DELETE') {
    $id = $_GET['id'] ?? 0;

    $stmt = $pdo->prepare("DELETE FROM productos WHERE id = ?");

    if ($stmt->execute([$id])) {
        echo json_encode(['success' => true, 'message' => 'Producto eliminado exitosamente']);
    } else {
        echo json_encode(['success' => false, 'message' => 'Error al eliminar producto']);
    }
}
?>