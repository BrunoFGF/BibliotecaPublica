-- Crear base de datos (ejecutar primero)
CREATE DATABASE BibliotecaDB;
GO

USE BibliotecaDB;
GO

USE master;
GO

-- Crear un login
CREATE LOGIN biblioteca_user WITH PASSWORD = 'biblioteca123';
GO

ALTER SERVER ROLE sysadmin ADD MEMBER biblioteca_user;
GO

-- Usar la base de datos
USE BibliotecaDB;
GO

-- Crear usuario en la base de datos
CREATE USER biblioteca_user FOR LOGIN biblioteca_user;
GO

-- Hacer al usuario propietario de la base de datos (db_owner)
ALTER ROLE db_owner ADD MEMBER biblioteca_user;
GO

-- Permisos adicionales explícitos (aunque db_owner ya los incluye)
GRANT CONTROL ON DATABASE::BibliotecaDB TO biblioteca_user;
GO