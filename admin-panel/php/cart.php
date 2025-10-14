<?php
require_once 'config.php';
session_start();

// Verify that a user is logged in (any user, not just admin)
if (!isset($_SESSION['user_id'])) {
    header('Content-Type: application/json');
    http_response_code(403); // Forbidden
    echo json_encode(['success' => false, 'message' => 'Acceso no autorizado. Por favor, inicie sesión.']);
    exit;
}

$userId = $_SESSION['user_id'];
$method = $_SERVER['REQUEST_METHOD'];

// GET: Obtener el contenido del carrito
if ($method === 'GET') {
    try {
        $stmt = $pdo->prepare("
            SELECT 
                c.id as cart_item_id, 
                p.id as product_id, 
                p.nombre, 
                p.precio, 
                p.imagen_url, 
                c.cantidad,
                p.stock
            FROM carrito c
            JOIN productos p ON c.producto_id = p.id
            WHERE c.usuario_id = ?
            ORDER BY c.fecha_agregado DESC
        ");
        $stmt->execute([$userId]);
        $cartItems = $stmt->fetchAll(PDO::FETCH_ASSOC);

        echo json_encode(['success' => true, 'data' => $cartItems]);

    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Error al obtener el carrito: ' . $e->getMessage()]);
    }
}

// POST: Añadir producto al carrito o actualizar cantidad
if ($method === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);
    $productId = $data['product_id'] ?? null;
    $quantity = $data['quantity'] ?? 1;

    if (!$productId) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'ID de producto requerido.']);
        exit;
    }

    try {
        $sql = "
            INSERT INTO carrito (usuario_id, producto_id, cantidad) 
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE cantidad = cantidad + VALUES(cantidad);
        ";
        $stmt = $pdo->prepare($sql);
        
        if ($stmt->execute([$userId, $productId, $quantity])) {
            echo json_encode(['success' => true, 'message' => 'Producto añadido al carrito.']);
        } else {
            http_response_code(500);
            echo json_encode(['success' => false, 'message' => 'Error al añadir el producto al carrito.']);
        }

    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Error de base de datos: ' . $e->getMessage()]);
    }
}

// PUT: Actualizar la cantidad de un producto en el carrito
if ($method === 'PUT') {
    $data = json_decode(file_get_contents('php://input'), true);
    $cartItemId = $data['cart_item_id'] ?? null;
    $quantity = $data['quantity'] ?? null;

    if (!$cartItemId || $quantity === null) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'ID de ítem y cantidad requeridos.']);
        exit;
    }

    // Si la cantidad es 0, lo eliminamos
    if ($quantity <= 0) {
        $sql = "DELETE FROM carrito WHERE id = ? AND usuario_id = ?";
        $stmt = $pdo->prepare($sql);
        $stmt->execute([$cartItemId, $userId]);
    } else {
        $sql = "UPDATE carrito SET cantidad = ? WHERE id = ? AND usuario_id = ?";
        $stmt = $pdo->prepare($sql);
        $stmt->execute([$quantity, $cartItemId, $userId]);
    }

    echo json_encode(['success' => true, 'message' => 'Carrito actualizado.']);
}

// DELETE: Eliminar un producto del carrito
if ($method === 'DELETE') {
    $data = json_decode(file_get_contents('php://input'), true);
    $cartItemId = $data['cart_item_id'] ?? null;

    if (!$cartItemId) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'ID de ítem requerido.']);
        exit;
    }

    $sql = "DELETE FROM carrito WHERE id = ? AND usuario_id = ?";
    $stmt = $pdo->prepare($sql);
    
    if ($stmt->execute([$cartItemId, $userId])) {
        echo json_encode(['success' => true, 'message' => 'Producto eliminado del carrito.']);
    } else {
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Error al eliminar el producto.']);
    }
}


