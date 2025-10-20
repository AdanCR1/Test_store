<?php
require_once 'config.php'; // Para la conexión a la BD ($pdo)

// APP

// Asegurarse de que la respuesta sea JSON
header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);

    $email = $data['email'] ?? '';
    $password = $data['password'] ?? '';

    if (empty($email) || empty($password)) {
        echo json_encode(['success' => false, 'message' => 'Email y contraseña son requeridos']);
        exit;
    }

    // Preparar la consulta a la tabla de usuarios
    $stmt = $pdo->prepare("SELECT id, nombre, email, password, fecha_registro, is_admin, is_super_admin, is_active FROM usuarios WHERE email = ?");
    $stmt->execute([$email]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    // Verificar si el usuario existe, la contraseña coincide y la cuenta está activa
    if ($user && md5($password) === $user['password']) {
        if (!$user['is_active']) {
            echo json_encode([
                'success' => false,
                'message' => 'La cuenta de usuario ha sido desactivada. Contacte con el administrador.'
            ]);
            exit;
        }

        session_start();
        $_SESSION['user_id'] = $user['id'];
        $_SESSION['is_admin'] = (bool)($user['is_admin'] ?? false);
        $_SESSION['is_super_admin'] = (bool)($user['is_super_admin'] ?? false);



        echo json_encode([
            'success' => true,
            'message' => 'Login de usuario exitoso',
            'user' => [
                'id' => $user['id'],
                'nombre' => $user['nombre'],
                'email' => $user['email'],
                'fecha_registro' => $user['fecha_registro'],
                'is_admin' => (bool)($user['is_admin'] ?? false),
                'is_super_admin' => (bool)($user['is_super_admin'] ?? false)
            ]
        ]);
    } else {
        echo json_encode([
            'success' => false,
            'message' => 'Credenciales de usuario incorrectas'
        ]);
    }
} else {
    echo json_encode(['success' => false, 'message' => 'Método no permitido']);
}
?>
