// Variables globales
let currentSection = 'dashboard';
let products = [];
let users = [];

// Inicialización
document.addEventListener('DOMContentLoaded', function() {
    checkAuth();
    setupEventListeners();
});

// Verificar autenticación
function checkAuth() {
    // Mostrar modal de login al cargar
    const loginModal = new bootstrap.Modal(document.getElementById('loginModal'));
    loginModal.show();
}

// Configurar event listeners
function setupEventListeners() {
    // Navegación
    document.querySelectorAll('[data-section]').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            showSection(this.getAttribute('data-section'));
        });
    });
}

// Mostrar sección
function showSection(section) {
    // Ocultar todas las secciones
    document.querySelectorAll('.content-section').forEach(sec => {
        sec.style.display = 'none';
    });

    // Mostrar sección seleccionada
    document.getElementById(section + '-section').style.display = 'block';

    // Actualizar navegación activa
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });
    document.querySelectorAll(`[data-section="${section}"]`).forEach(link => {
        link.classList.add('active');
    });

    currentSection = section;

    // Cargar datos según la sección
    switch(section) {
        case 'dashboard':
            loadDashboardData();
            break;
        case 'products':
            loadProducts();
            break;
        case 'users':
            loadUsers();
            break;
    }
}

// Login de administrador
function adminLogin() {
    const email = document.getElementById('adminEmail').value;
    const password = document.getElementById('adminPassword').value;

    fetch('php/login.php', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            bootstrap.Modal.getInstance(document.getElementById('loginModal')).hide();
            loadDashboardData();
        } else {
            alert('Error: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Error de conexión');
    });
}

// Cerrar sesión
function logout() {
    if (confirm('¿Estás seguro de que quieres cerrar sesión?')) {
        window.location.reload();
    }
}

// Cargar datos del dashboard
function loadDashboardData() {
    // Cargar productos
    fetch('php/products.php')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                document.getElementById('total-products').textContent = data.data.length;
                updateRecentOrders(data.data.slice(0, 5));
            }
        });

    // Cargar usuarios
    fetch('php/users.php')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                document.getElementById('total-users').textContent = data.data.length;
            }
        });
}

// Cargar productos
function loadProducts() {
    fetch('php/products.php')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                products = data.data;
                renderProductsTable();
            }
        });
}

// Renderizar tabla de productos
function renderProductsTable() {
    const tbody = document.querySelector('#products-table tbody');
    tbody.innerHTML = '';

    products.forEach(product => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${product.id}</td>
            <td>-</td>
            <td>${product.nombre}</td>
            <td>${product.categoria_nombre || 'Sin categoría'}</td>
            <td>$${product.precio}</td>
            <td>-</td>
            <td>
                <button class="btn btn-sm btn-warning me-1" onclick="editProduct(${product.id})">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-sm btn-danger" onclick="deleteProduct(${product.id})">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Cargar usuarios
function loadUsers() {
    fetch('php/users.php')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                users = data.data;
                renderUsersTable();
            }
        });
}

// Renderizar tabla de usuarios
function renderUsersTable() {
    const tbody = document.querySelector('#users-table tbody');
    tbody.innerHTML = '';

    users.forEach(user => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${user.id}</td>
            <td>${user.nombre}</td>
            <td>${user.email}</td>
            <td>Usuario</td>
            <td>${user.fecha_registro}</td>
            <td>
                <button class="btn btn-sm btn-warning me-1" onclick="editUser(${user.id})">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-sm btn-danger" onclick="deleteUser(${user.id})">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Mostrar modal para agregar producto
function showAddProductModal() {
    document.getElementById('addProductForm').reset();
    const modal = new bootstrap.Modal(document.getElementById('addProductModal'));
    modal.show();
}

// Agregar producto
function addProduct() {
    const formData = {
        nombre: document.getElementById('productName').value,
        precio: document.getElementById('productPrice').value,
        descripcion: document.getElementById('productDescription').value,
        categoria_id: document.getElementById('productCategory').value
    };

    fetch('php/products.php', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            bootstrap.Modal.getInstance(document.getElementById('addProductModal')).hide();
            alert('Producto agregado exitosamente');
            loadProducts();
        } else {
            alert('Error: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Error de conexión');
    });
}

// Eliminar producto
function deleteProduct(id) {
    if (confirm('¿Estás seguro de que quieres eliminar este producto?')) {
        fetch(`php/products.php?id=${id}`, {
            method: 'DELETE'
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('Producto eliminado exitosamente');
                loadProducts();
            } else {
                alert('Error: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error de conexión');
        });
    }
}

// Eliminar usuario
function deleteUser(id) {
    if (confirm('¿Estás seguro de que quieres eliminar este usuario?')) {
        fetch(`php/users.php?id=${id}`, {
            method: 'DELETE'
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('Usuario eliminado exitosamente');
                loadUsers();
            } else {
                alert('Error: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error de conexión');
        });
    }
}

// Actualizar pedidos recientes (placeholder)
function updateRecentOrders(orders) {
    const tbody = document.querySelector('#recent-orders-table tbody');
    tbody.innerHTML = '';

    orders.forEach(order => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${order.id}</td>
            <td>Cliente ${order.id}</td>
            <td>$${order.precio}</td>
            <td><span class="badge badge-pendiente">Pendiente</span></td>
            <td>${new Date().toLocaleDateString()}</td>
        `;
        tbody.appendChild(row);
    });
}

// Funciones placeholder para edición (puedes expandirlas)
function editProduct(id) {
    alert(`Editar producto ${id} - Esta funcionalidad se expandirá`);
}

function editUser(id) {
    alert(`Editar usuario ${id} - Esta funcionalidad se expandirá`);
}

function showAddStaffModal() {
    alert('Agregar personal - Esta funcionalidad se expandirá');
}