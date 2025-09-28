<?php
require_once 'config.php';
session_start();

// Verificar autenticación
if (!isset($_SESSION['admin_id'])) {
    echo json_encode(['success' => false, 'message' => 'No autorizado']);
    exit;
}

// GET - Obtener productos
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $stmt = $pdo->query("
        SELECT p.*, c.nombre as categoria_nombre
        FROM productos p
        LEFT JOIN categorias c ON p.categoria_id = c.id
        ORDER BY p.id DESC
    ");
    $products = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode(['success' => true, 'data' => $products]);
}

// POST - Crear producto
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);

    $nombre = $data['nombre'] ?? '';
    $precio = $data['precio'] ?? 0;
    $descripcion = $data['descripcion'] ?? '';
    $categoria_id = $data['categoria_id'] ?? 1;

    $stmt = $pdo->prepare("INSERT INTO productos (nombre, precio, descripción, categoria_id) VALUES (?, ?, ?, ?)");

    if ($stmt->execute([$nombre, $precio, $descripcion, $categoria_id])) {
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
    $categoria_id = $data['categoria_id'] ?? 1;

    $stmt = $pdo->prepare("UPDATE productos SET nombre = ?, precio = ?, descripción = ?, categoria_id = ? WHERE id = ?");

    if ($stmt->execute([$nombre, $precio, $descripcion, $categoria_id, $id])) {
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