<?php
require_once 'config.php';
session_start();

// Verificar autenticación y si es super admin
if (!isset($_SESSION['user_id']) || !isset($_SESSION['is_super_admin']) || !$_SESSION['is_super_admin']) {
    http_response_code(401); // Unauthorized
    echo json_encode(['success' => false, 'message' => 'No autorizado']);
    exit;
}


// GET - Obtener usuarios
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $current_user_id = $_SESSION['user_id'];
    $stmt = $pdo->prepare("SELECT id, nombre, email, is_admin, is_super_admin, is_active FROM usuarios WHERE is_super_admin = false AND id != ? ORDER BY id DESC");
    $stmt->execute([$current_user_id]);
    $users = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Cast boolean values
    foreach ($users as &$user) {
        $user['is_admin'] = (bool)$user['is_admin'];
        $user['is_super_admin'] = (bool)$user['is_super_admin'];
        $user['is_active'] = (bool)$user['is_active'];
    }

    echo json_encode(['success' => true, 'data' => $users]);
}
?>