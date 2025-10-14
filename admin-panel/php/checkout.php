<?php
require_once 'config.php';
session_start();

header('Content-Type: application/json');

if (!isset($_SESSION['user_id'])) {
    http_response_code(403);
    echo json_encode(['success' => false, 'message' => 'No autorizado.']);
    exit;
}

$userId = $_SESSION['user_id'];
$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);
    $cartItems = $data['cart_items'] ?? [];
    $address = $data['address'] ?? 'Dirección no proporcionada';

    if (empty($cartItems)) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'El carrito está vacío.']);
        exit;
    }

    try {
        $pdo->beginTransaction();

        // 1. Calcular el total y verificar stock
        $total = 0;
        foreach ($cartItems as $item) {
            $stmt = $pdo->prepare("SELECT precio, stock FROM productos WHERE id = ? FOR UPDATE");
            $stmt->execute([$item['product_id']]);
            $product = $stmt->fetch(PDO::FETCH_ASSOC);

            if (!$product || $product['stock'] < $item['cantidad']) {
                throw new Exception('Stock insuficiente para el producto: ' . $item['nombre']);
            }
            $total += $product['precio'] * $item['cantidad'];
        }

        // 2. Crear el pedido
        $stmt = $pdo->prepare("INSERT INTO pedidos (usuario_id, total, direccion_envio) VALUES (?, ?, ?)");
        $stmt->execute([$userId, $total, $address]);
        $pedidoId = $pdo->lastInsertId();

        // 3. Mover ítems a detalle_pedidos y actualizar stock
        foreach ($cartItems as $item) {
            $stmt = $pdo->prepare("INSERT INTO detalle_pedidos (pedido_id, producto_id, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)");
            $stmt->execute([$pedidoId, $item['product_id'], $item['cantidad'], $item['precio'], $item['precio'] * $item['cantidad']]);

            $stmt = $pdo->prepare("UPDATE productos SET stock = stock - ? WHERE id = ?");
            $stmt->execute([$item['cantidad'], $item['product_id']]);
        }

        // 4. Vaciar el carrito
        $stmt = $pdo->prepare("DELETE FROM carrito WHERE usuario_id = ?");
        $stmt->execute([$userId]);

        $pdo->commit();

        echo json_encode(['success' => true, 'message' => '¡Compra realizada con éxito!', 'pedido_id' => $pedidoId]);

    } catch (Exception $e) {
        $pdo->rollBack();
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => $e->getMessage()]);
    }
}
?>
