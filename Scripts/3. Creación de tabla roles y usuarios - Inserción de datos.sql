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
        email_verificado BIT DEFAULT 1,
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
        usuario_id BIGINT NOT NULL,
        rol_id BIGINT NOT NULL,
        PRIMARY KEY (usuario_id, rol_id),
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
        FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE
    );
    PRINT 'Tabla usuario_roles creada exitosamente';
END
ELSE
BEGIN
    PRINT 'Tabla usuario_roles ya existe';
END
GO

-- Crear índices básicos
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_usuarios_email')
BEGIN
    CREATE INDEX IX_usuarios_email ON usuarios(email);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_usuarios_activo')
BEGIN
    CREATE INDEX IX_usuarios_activo ON usuarios(activo);
END
GO

-- Trigger para fecha de actualización
IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'TR_usuarios_update')
BEGIN
    DROP TRIGGER TR_usuarios_update;
END
GO

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

-- Insertar roles básicos
IF NOT EXISTS (SELECT * FROM roles WHERE nombre = 'BIBLIOTECARIO')
BEGIN
    INSERT INTO roles (nombre, descripcion) VALUES 
    ('BIBLIOTECARIO', 'Administrador del sistema con acceso completo');
END

IF NOT EXISTS (SELECT * FROM roles WHERE nombre = 'USUARIO')
BEGIN
    INSERT INTO roles (nombre, descripcion) VALUES 
    ('USUARIO', 'Usuario regular del sistema');
END
GO

-- Insertar usuario administrador (Contraseña: admin123)
IF NOT EXISTS (SELECT * FROM usuarios WHERE email = 'admin@biblioteca.com')
BEGIN
    INSERT INTO usuarios (cedula, nombre, email, password, telefono, direccion, activo, email_verificado) 
    VALUES ('1234567890', 'Administrador', 'admin@biblioteca.com', 'admin123', '0999999999', 'Guayaquil, Ecuador', 1, 1);
    
    -- Asignar rol de bibliotecario
    DECLARE @admin_id BIGINT = SCOPE_IDENTITY();
    DECLARE @bibliotecario_rol_id BIGINT = (SELECT id FROM roles WHERE nombre = 'BIBLIOTECARIO');
    
    INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (@admin_id, @bibliotecario_rol_id);
    
    PRINT 'Usuario administrador creado con email: admin@biblioteca.com y contraseña: admin123';
END
ELSE
BEGIN
    PRINT 'Usuario administrador ya existe';
END
GO

-- Insertar algunos usuarios de prueba
IF NOT EXISTS (SELECT * FROM usuarios WHERE email = 'juan.perez@email.com')
BEGIN
    INSERT INTO usuarios (cedula, nombre, email, password, telefono, direccion, activo, email_verificado) 
    VALUES ('0987654321', 'Juan Pérez', 'juan.perez@email.com', 'juan123', '0987654321', 'Quito, Ecuador', 1, 1);
    
    DECLARE @juan_id BIGINT = SCOPE_IDENTITY();
    DECLARE @usuario_rol_id BIGINT = (SELECT id FROM roles WHERE nombre = 'USUARIO');
    
    INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (@juan_id, @usuario_rol_id);
END

IF NOT EXISTS (SELECT * FROM usuarios WHERE email = 'maria.gonzalez@email.com')
BEGIN
    INSERT INTO usuarios (cedula, nombre, email, password, telefono, direccion, activo, email_verificado) 
    VALUES ('1122334455', 'María González', 'maria.gonzalez@email.com', 'maria123', '0123456789', 'Cuenca, Ecuador', 1, 1);
    
    DECLARE @maria_id BIGINT = SCOPE_IDENTITY();
    DECLARE @usuario_rol_id2 BIGINT = (SELECT id FROM roles WHERE nombre = 'USUARIO');
    
    INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (@maria_id, @usuario_rol_id2);
END
GO

-- Verificar la creación
SELECT 'ROLES CREADOS:' as Info;
SELECT * FROM roles;

SELECT 'USUARIOS CREADOS:' as Info;
SELECT u.id, u.nombre, u.email, u.password, u.activo, 
       STRING_AGG(r.nombre, ', ') as roles
FROM usuarios u
LEFT JOIN usuario_roles ur ON u.id = ur.usuario_id
LEFT JOIN roles r ON ur.rol_id = r.id
GROUP BY u.id, u.nombre, u.email, u.password, u.activo
ORDER BY u.id;

PRINT 'Script ejecutado exitosamente. Usuario admin: admin@biblioteca.com / admin123';
GO