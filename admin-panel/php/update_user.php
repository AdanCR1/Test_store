<?php
require_once 'config.php';
session_start();

// Verificar autenticación y si es super admin
if (!isset($_SESSION['user_id']) || !isset($_SESSION['is_super_admin']) || !$_SESSION['is_super_admin']) {
    echo json_encode(['success' => false, 'message' => 'No autorizado']);
    exit;
}

// PUT - Actualizar usuario
if ($_SERVER['REQUEST_METHOD'] === 'PUT') {
    $data = json_decode(file_get_contents('php://input'), true);

    $id = $data['id'] ?? 0;
    $is_admin = $data['is_admin'] ?? 0;
    $is_super_admin = $data['is_super_admin'] ?? 0;
    $is_active = $data['is_active'] ?? 0;

    $stmt = $pdo->prepare("UPDATE usuarios SET is_admin = ?, is_super_admin = ?, is_active = ? WHERE id = ?");

    if ($stmt->execute([$is_admin, $is_super_admin, $is_active, $id])) {
        echo json_encode(['success' => true, 'message' => 'Usuario actualizado exitosamente']);
    } else {
        echo json_encode(['success' => false, 'message' => 'Error al actualizar usuario']);
    }
}
?>