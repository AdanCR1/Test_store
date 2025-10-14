# ASUS ROG Store - Guía rápida de configuración

## 1. Requisitos previos

Asegúrate de tener instalado en tu sistema o usar `xampp` para correr php y mysql:

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

## 3. Configurar la URL de la API

La URL base de la API ahora se gestiona de forma centralizada como una variable de entorno, para evitar tener que cambiarla en múltiples sitios.

Abre el archivo:

```
app/build.gradle.kts
```

Busca el bloque `buildTypes` y cambia el valor de `API_BASE_URL` por la URL de tu backend (obtenida en el paso 2).

```kotlin
    buildTypes {
        release {
            // ...
            // URL para la versión de producción de la app
            buildConfigField("String", "API_BASE_URL", "\"https://tu-servidor.com/api\"")
        }
        getByName("debug") {
            // URL para desarrollo y pruebas locales
            buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.7:8000/admin-panel/php\"")
        }
    }
```

**Importante:** Después de modificar este archivo, Android Studio te pedirá sincronizar el proyecto. Haz clic en **"Sync Now"**. Este paso es obligatorio para que la app pueda usar la nueva URL.

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
