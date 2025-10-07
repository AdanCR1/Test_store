<?php
require_once 'config.php'; // Para la conexión a la BD ($pdo)

// Asegurarse de que la respuesta sea JSON
header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Leer el JSON enviado desde la app Android
    $data = json_decode(file_get_contents('php://input'), true);

    $email = $data['email'] ?? '';
    $password = $data['password'] ?? '';

    if (empty($email) || empty($password)) {
        echo json_encode(['success' => false, 'message' => 'Email y contraseña son requeridos']);
        exit;
    }

    // Preparar la consulta a la tabla de usuarios
    $stmt = $pdo->prepare("SELECT * FROM usuarios WHERE email = ?");
    $stmt->execute([$email]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    // Verificar si el usuario existe y la contraseña coincide
    // NOTA: La comparación de contraseñas en texto plano es insegura.
    // Esto se hace así para ser consistente con el resto del código del proyecto.
    if ($user && $password === $user['password']) {
        // En una app real, aquí se generaría un token de sesión (ej. JWT)
        echo json_encode([
            'success' => true,
            'message' => 'Login de usuario exitoso',
                            'user' => [ // No enviar la contraseña de vuelta al cliente
                                'id' => $user['id'],
                                'nombre' => $user['nombre'],
                                'email' => $user['email'],
                                'fecha_registro' => $user['fecha_registro'],
                                'is_admin' => (bool)($user['is_admin'] ?? false)
                            ]        ]);
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
