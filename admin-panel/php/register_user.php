<?php
header('Content-Type: application/json');
require_once 'config.php';

$response = ['success' => false, 'message' => ''];

$input = file_get_contents('php://input');
$data = json_decode($input, true);

if ($data === null) {
    $response['message'] = 'Entrada JSON inválida.';
    echo json_encode($response);
    exit();
}

$nombre = $data['nombre'] ?? '';
$email = $data['email'] ?? '';
$password = $data['password'] ?? '';
$direccion = $data['direccion'] ?? null;
$telefono = $data['telefono'] ?? null;

if (empty($nombre) || empty($email) || empty($password)) {
    $response['message'] = 'Nombre, email y contraseña son campos obligatorios.';
    echo json_encode($response);
    exit();
}

$hashed_password = md5($password);

try {
    global $pdo;

    // Verificar si el email ya existe
    $stmt = $pdo->prepare("SELECT id FROM usuarios WHERE email = ?");
    $stmt->execute([$email]);
    if ($stmt->fetch()) {
        $response['message'] = 'El email ya está registrado. Por favor, usa otro.';
        echo json_encode($response);
        exit();
    }

    $stmt = $pdo->prepare(
        "INSERT INTO usuarios (nombre, email, password, direccion, telefono) VALUES (?, ?, ?, ?, ?)"
    );
    $stmt->execute([$nombre, $email, $hashed_password, $direccion, $telefono]);

    $response['success'] = true;
    $response['message'] = 'Usuario registrado exitosamente.';

} catch (PDOException $e) {
    $response['message'] = 'Error en la base de datos: ' . $e->getMessage();
}

echo json_encode($response);
?>