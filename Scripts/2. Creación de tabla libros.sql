USE BibliotecaDB;
GO

-- Crear tabla libros
CREATE TABLE libros (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    codigo_libro NVARCHAR(20) NOT NULL UNIQUE,
    titulo NVARCHAR(100) NOT NULL,
    autor NVARCHAR(60) NOT NULL,
    editorial NVARCHAR(60) NOT NULL,
    anio_publicacion INT NOT NULL CHECK (anio_publicacion >= 1000 AND anio_publicacion <= 9999),
    cantidad_disponible INT NOT NULL DEFAULT 0 CHECK (cantidad_disponible >= 0),
    fecha_creacion DATETIME2 DEFAULT GETDATE(),
    fecha_actualizacion DATETIME2 DEFAULT GETDATE()
);
GO

-- Crear índices para optimizar búsquedas
CREATE INDEX IX_libros_codigo ON libros(codigo_libro);
CREATE INDEX IX_libros_titulo ON libros(titulo);
CREATE INDEX IX_libros_autor ON libros(autor);
CREATE INDEX IX_libros_editorial ON libros(editorial);
CREATE INDEX IX_libros_anio ON libros(anio_publicacion);
GO

-- Trigger para actualizar fecha de modificación
CREATE TRIGGER TR_libros_update 
ON libros
AFTER UPDATE
AS
BEGIN
    UPDATE libros 
    SET fecha_actualizacion = GETDATE()
    FROM libros l
    INNER JOIN inserted i ON l.id = i.id;
END;
GO

-- Insertar datos de prueba
INSERT INTO libros (codigo_libro, titulo, autor, editorial, anio_publicacion, cantidad_disponible) VALUES
('LIB0011', 'Cien años de soledad', 'Gabriel García Márquez', 'Sudamericana', 1967, 5),
('LIB0012', 'Don Quijote de la Mancha', 'Miguel de Cervantes', 'Francisco de Robles', 1605, 3),
('LIB0013', '1984', 'George Orwell', 'Secker & Warburg', 1949, 8),
('LIB0014', 'El amor en los tiempos del cólera', 'Gabriel García Márquez', 'Oveja Negra', 1985, 4),
('LIB0015', 'Rayuela', 'Julio Cortázar', 'Sudamericana', 1963, 2),
('LIB006', 'La casa de los espíritus', 'Isabel Allende', 'Plaza & Janés', 1982, 6),
('LIB007', 'Crónica de una muerte anunciada', 'Gabriel García Márquez', 'La Oveja Negra', 1981, 3),
('LIB008', 'El túnel', 'Ernesto Sabato', 'Sur', 1948, 4),
('LIB009', 'Pedro Páramo', 'Juan Rulfo', 'Fondo de Cultura Económica', 1955, 5),
('LIB010', 'La ciudad y los perros', 'Mario Vargas Llosa', 'Seix Barral', 1963, 2);
GO