<?php
require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);

    $email = $data['email'] ?? '';
    $password = $data['password'] ?? '';

    $stmt = $pdo->prepare("SELECT * FROM administradores WHERE email = ?");
    $stmt->execute([$email]);
    $admin = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($admin && $password === $admin['password']) {
        session_start();
        $_SESSION['admin_id'] = $admin['id'];
        $_SESSION['admin_name'] = $admin['nombre'];

        echo json_encode([
            'success' => true,
            'message' => 'Login exitoso',
            'admin' => $admin
        ]);
    } else {
        echo json_encode([
            'success' => false,
            'message' => 'Credenciales incorrectas'
        ]);
    }
}
?>