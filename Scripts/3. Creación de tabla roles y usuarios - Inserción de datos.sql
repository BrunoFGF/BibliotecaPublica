USE BibliotecaDB;
GO

-- Crear tabla de roles
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='roles' AND xtype='U')
BEGIN
    CREATE TABLE roles (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        nombre NVARCHAR(50) NOT NULL UNIQUE,
        descripcion NVARCHAR(200),
        fecha_creacion DATETIME2 DEFAULT GETDATE()
    );
    PRINT 'Tabla roles creada exitosamente';
END
ELSE
BEGIN
    PRINT 'Tabla roles ya existe';
END
GO

-- Crear tabla de usuarios
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='usuarios' AND xtype='U')
BEGIN
    CREATE TABLE usuarios (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
		cedula NVARCHAR(10) NOT NULL,
        nombre NVARCHAR(30) NOT NULL,
        email NVARCHAR(60) NOT NULL UNIQUE,
        password NVARCHAR(255) NOT NULL,
        telefono NVARCHAR(10),
        direccion NVARCHAR(60),
        activo BIT DEFAULT 1,
        email_verificado BIT DEFAULT 0,
        fecha_creacion DATETIME2 DEFAULT GETDATE(),
        fecha_actualizacion DATETIME2 DEFAULT GETDATE()
    );
    PRINT 'Tabla usuarios creada exitosamente';
END
ELSE
BEGIN
    PRINT 'Tabla usuarios ya existe';
END
GO

-- Crear tabla intermedia usuario_roles
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='usuario_roles' AND xtype='U')
BEGIN
    CREATE TABLE usuario_roles (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        usuario_id BIGINT NOT NULL,
        rol_id BIGINT NOT NULL,
        fecha_asignacion DATETIME2 DEFAULT GETDATE(),
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
        FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE,
        UNIQUE(usuario_id, rol_id)
    );
    PRINT 'Tabla usuario_roles creada exitosamente';
END
ELSE
BEGIN
    PRINT 'Tabla usuario_roles ya existe';
END
GO

-- Índices para usuarios
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_usuarios_email')
BEGIN
    CREATE INDEX IX_usuarios_email ON usuarios(email);
    PRINT 'Índice IX_usuarios_email creado';
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_usuarios_activo')
BEGIN
    CREATE INDEX IX_usuarios_activo ON usuarios(activo);
    PRINT 'Índice IX_usuarios_activo creado';
END

-- Índices para usuario_roles
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_usuario_roles_usuario')
BEGIN
    CREATE INDEX IX_usuario_roles_usuario ON usuario_roles(usuario_id);
    PRINT 'Índice IX_usuario_roles_usuario creado';
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_usuario_roles_rol')
BEGIN
    CREATE INDEX IX_usuario_roles_rol ON usuario_roles(rol_id);
    PRINT 'Índice IX_usuario_roles_rol creado';
END
GO

-- Verificar si el trigger ya existe y eliminarlo
IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'TR_usuarios_update')
BEGIN
    DROP TRIGGER TR_usuarios_update;
    PRINT 'Trigger anterior eliminado';
END
GO

-- Crear trigger para actualizar fecha de modificación de usuarios
CREATE TRIGGER TR_usuarios_update 
ON usuarios
AFTER UPDATE
AS
BEGIN
    UPDATE usuarios 
    SET fecha_actualizacion = GETDATE()
    FROM usuarios u
    INNER JOIN inserted i ON u.id = i.id;
END;
GO
PRINT 'Trigger TR_usuarios_update creado exitosamente';
GO

-- Insertar roles si no existen
IF NOT EXISTS (SELECT * FROM roles WHERE nombre = 'BIBLIOTECARIO')
BEGIN
    INSERT INTO roles (nombre, descripcion) VALUES ('BIBLIOTECARIO', 'Administrador del sistema con acceso completo');
    PRINT 'Rol BIBLIOTECARIO creado';
END
ELSE
BEGIN
    PRINT 'Rol BIBLIOTECARIO ya existe';
END

IF NOT EXISTS (SELECT * FROM roles WHERE nombre = 'USUARIO')
BEGIN
    INSERT INTO roles (nombre, descripcion) VALUES ('USUARIO', 'Usuario regular');
    PRINT 'Rol USUARIO creado';
END
ELSE
BEGIN
    PRINT 'Rol USUARIO ya existe';
END
GO

-- Insertar usuarios de prueba si no existen (contraseña: password123)
DECLARE @password_hash NVARCHAR(255) = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P2.nRZ.Ud5zdtS';

-- Usuario Bibliotecario Principal
IF NOT EXISTS (SELECT * FROM usuarios WHERE email = 'admin@biblioteca.com')
BEGIN
    INSERT INTO usuarios (cedula, nombre, email, password, telefono, direccion, activo, email_verificado) 
    VALUES ('0382763976', 'Admin', 'admin@biblioteca.com', @password_hash, '0999999999', 'Guayaquil, Ecuador', 1, 1);
    PRINT 'Usuario admin@biblioteca.com creado';
END
ELSE
BEGIN
    PRINT 'Usuario admin@biblioteca.com ya existe';
END

-- Usuarios regulares
IF NOT EXISTS (SELECT * FROM usuarios WHERE email = 'juan.perez@email.com')
BEGIN
    INSERT INTO usuarios (cedula, nombre, email, password, telefono, direccion, activo, email_verificado) 
    VALUES ('0382763976', 'Juan', 'juan.perez@email.com', @password_hash, '0987654321', 'Quito, Ecuador', 1, 1);
    PRINT 'Usuario juan.perez@email.com creado';
END

IF NOT EXISTS (SELECT * FROM usuarios WHERE email = 'maria.gonzalez@email.com')
BEGIN
    INSERT INTO usuarios (cedula, nombre, email, password, telefono, direccion, activo, email_verificado) 
    VALUES ('0382763976', 'María', 'maria.gonzalez@email.com', @password_hash, '0123456789', 'Cuenca, Ecuador', 1, 1);
    PRINT 'Usuario maria.gonzalez@email.com creado';
END

IF NOT EXISTS (SELECT * FROM usuarios WHERE email = 'ana.rodriguez@email.com')
BEGIN
    INSERT INTO usuarios (cedula, nombre, email, password, telefono, direccion, activo, email_verificado) 
    VALUES ('0382763976', 'Ana', 'ana.rodriguez@email.com', @password_hash, '0987654322', 'Ambato, Ecuador', 1, 1);
    PRINT 'Usuario ana.rodriguez@email.com creado';
END

IF NOT EXISTS (SELECT * FROM usuarios WHERE email = 'carlos.mendoza@email.com')
BEGIN
    INSERT INTO usuarios (cedula, nombre, email, password, telefono, direccion, activo, email_verificado) 
    VALUES ('0382763976', 'Carlos', 'carlos.mendoza@email.com', @password_hash, '0123456780', 'Loja, Ecuador', 1, 1);
    PRINT 'Usuario carlos.mendoza@email.com creado';
END

IF NOT EXISTS (SELECT * FROM usuarios WHERE email = 'lucia.vargas@email.com')
BEGIN
    INSERT INTO usuarios (cedula, nombre, email, password, telefono, direccion, activo, email_verificado) 
    VALUES ('0382763976', 'Lucia', 'lucia.vargas@email.com', @password_hash, '0999888777', 'Machala, Ecuador', 0, 1);
    PRINT 'Usuario lucia.vargas@email.com creado (INACTIVO)';
END
GO

-- Obtener IDs de roles
DECLARE @bibliotecario_rol_id BIGINT = (SELECT id FROM roles WHERE nombre = 'BIBLIOTECARIO');
DECLARE @usuario_rol_id BIGINT = (SELECT id FROM roles WHERE nombre = 'USUARIO');

-- Asignar rol de bibliotecario al admin
DECLARE @admin_id BIGINT = (SELECT id FROM usuarios WHERE email = 'admin@biblioteca.com');
IF NOT EXISTS (SELECT * FROM usuario_roles WHERE usuario_id = @admin_id AND rol_id = @bibliotecario_rol_id)
BEGIN
    INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (@admin_id, @bibliotecario_rol_id);
    PRINT 'Rol BIBLIOTECARIO asignado a admin@biblioteca.com';
END

-- Asignar rol de usuario a los demás
DECLARE @juan_id BIGINT = (SELECT id FROM usuarios WHERE email = 'juan.perez@email.com');
IF NOT EXISTS (SELECT * FROM usuario_roles WHERE usuario_id = @juan_id AND rol_id = @usuario_rol_id)
BEGIN
    INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (@juan_id, @usuario_rol_id);
    PRINT 'Rol USUARIO asignado a juan.perez@email.com';
END

DECLARE @maria_id BIGINT = (SELECT id FROM usuarios WHERE email = 'maria.gonzalez@email.com');
IF NOT EXISTS (SELECT * FROM usuario_roles WHERE usuario_id = @maria_id AND rol_id = @usuario_rol_id)
BEGIN
    INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (@maria_id, @usuario_rol_id);
    PRINT 'Rol USUARIO asignado a maria.gonzalez@email.com';
END

DECLARE @ana_id BIGINT = (SELECT id FROM usuarios WHERE email = 'ana.rodriguez@email.com');
IF NOT EXISTS (SELECT * FROM usuario_roles WHERE usuario_id = @ana_id AND rol_id = @usuario_rol_id)
BEGIN
    INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (@ana_id, @usuario_rol_id);
    PRINT 'Rol USUARIO asignado a ana.rodriguez@email.com';
END

DECLARE @carlos_id BIGINT = (SELECT id FROM usuarios WHERE email = 'carlos.mendoza@email.com');
IF NOT EXISTS (SELECT * FROM usuario_roles WHERE usuario_id = @carlos_id AND rol_id = @usuario_rol_id)
BEGIN
    INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (@carlos_id, @usuario_rol_id);
    PRINT 'Rol USUARIO asignado a carlos.mendoza@email.com';
END

DECLARE @lucia_id BIGINT = (SELECT id FROM usuarios WHERE email = 'lucia.vargas@email.com');
IF NOT EXISTS (SELECT * FROM usuario_roles WHERE usuario_id = @lucia_id AND rol_id = @usuario_rol_id)
BEGIN
    INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (@lucia_id, @usuario_rol_id);
    PRINT 'Rol USUARIO asignado a lucia.vargas@email.com';
END
GO


DECLARE @new_password_hash NVARCHAR(255) = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.';

-- Actualizar la contraseña del admin
UPDATE usuarios 
SET password = @new_password_hash
WHERE email = 'admin@biblioteca.com';

-- Verificar que el usuario está activo y tiene email verificado
UPDATE usuarios 
SET activo = 1, email_verificado = 1
WHERE email = 'admin@biblioteca.com';

-- Verificar los datos
SELECT u.id, u.email, u.nombre, u.activo, u.email_verificado, u.password,
       STRING_AGG(r.nombre, ', ') as roles
FROM usuarios u
LEFT JOIN usuario_roles ur ON u.id = ur.usuario_id
LEFT JOIN roles r ON ur.rol_id = r.id
WHERE u.email = 'admin@biblioteca.com'
GROUP BY u.id, u.email, u.nombre, u.activo, u.email_verificado, u.password;

PRINT 'Usuario admin actualizado correctamente';
GO


-- Verificar todos los roles
SELECT * FROM roles;
GO

-- Verificar relación usuario-roles para admin
SELECT u.email, r.nombre as rol
FROM usuarios u
INNER JOIN usuario_roles ur ON u.id = ur.usuario_id
INNER JOIN roles r ON ur.rol_id = r.id
WHERE u.email = 'admin@biblioteca.com';
GO