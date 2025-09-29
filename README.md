# Test_store - Guía rápida de configuración

## 1. Requisitos previos

Asegúrate de tener instalado en tu sistema:

- **PHP**  
- **php-mysql** (extensión para conectar PHP con MySQL)
- **MySQL Server**  

En Ubuntu/Linux puedes instalar todo con:

```sh
sudo apt update
sudo apt install php php-mysql mysql-server
```

## 2. ¿Cómo obtener tu IP local?

En Linux, abre una terminal y ejecuta:

```sh
hostname -I
```
La IP que aparece (por ejemplo, `192.168.1.3`) es la que debes usar para acceder al backend desde la app móvil.

## 3. Cambiar la IP en la app (MainActivity.kt)

Abre el archivo:

```
app/src/main/java/com/example/test_store/MainActivity.kt
```

Busca las líneas donde se usa la IP para las peticiones HTTP, por ejemplo:

```kotlin
val url = "http://192.168.1.3:8000/admin-panel/php/products.php"
```

Reemplaza `192.168.1.3` por tu IP local obtenida en el paso 2.

Haz esto en todas las funciones que hagan peticiones al backend (login, productos, etc).

## 4. Ejecutar las queries de la base de datos

Antes de usar la app, asegúrate de crear las tablas y datos necesarios en MySQL. Ejecuta las queries de estructura y datos que se encuentran en el archivo de instalación o documentación del proyecto.

Por ejemplo, en MySQL puedes ejecutar:

```sh
mysql -u <tu_usuario> -p tienda_rog < queries.sql
```

Donde `queries.sql` contiene las sentencias `CREATE TABLE`, `INSERT`, etc.

## 5. Ejecutar el panel web (backend PHP)

Para iniciar el servidor embebido de PHP y exponer el panel web en tu red local, ejecuta desde la carpeta raíz del proyecto:

```sh
php -S 0.0.0.0:8000
```

Esto hará que el backend esté disponible en tu IP local y puerto 8000.

## 6. Notas importantes

- La IP local puede cambiar si reinicias el router o tu PC. Si la app deja de funcionar, revisa la IP y actualízala en el código.
- El archivo `config.php` puede mantener `127.0.0.1:3306` si la base de datos está en la misma máquina que el backend.
- Si usas Android, el dispositivo debe estar en la misma red WiFi que tu PC.
- Asegúrate que el firewall permita conexiones al puerto 8000 (o el que uses para el servidor PHP).

---