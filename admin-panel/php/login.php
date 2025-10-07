<?php
require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);

    $email = $data['email'] ?? '';
    $password = $data['password'] ?? '';

    // Query the usuarios table for an admin user
    $stmt = $pdo->prepare("SELECT * FROM usuarios WHERE email = ? AND is_admin = TRUE");
    $stmt->execute([$email]);
    $admin = $stmt->fetch(PDO::FETCH_ASSOC);

    // Verify password and that the user is indeed an admin
    if ($admin && $password === $admin['password']) {
        session_start();
        $_SESSION['admin_id'] = $admin['id'];
        $_SESSION['admin_name'] = $admin['nombre'];

        // Return a consistent response, using 'admin' key for frontend compatibility
        echo json_encode([
            'success' => true,
            'message' => 'Login de administrador exitoso',
            'admin' => $admin
        ]);
    } else {
        echo json_encode([
            'success' => false,
            'message' => 'Credenciales incorrectas o no es un administrador'
        ]);
    }
}
?>