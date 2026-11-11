-- =====================================================================
-- SIGARP - Sistema Integral de Gestión del Acervo de Residencias Profesionales
-- Script inicial de base de datos (MySQL 8)
-- Cubre: HU1 (registro/expediente), HU2 (búsqueda/etiquetas), HU3 (estadísticas),
--        HU4 (valoraciones moderadas), HU5 (contacto con autorización),
--        HU6 (ficha PDF), HU7 (panel admin + bitácora), banco de continuación.
-- =====================================================================
DROP DATABASE IF EXISTS sigarp;
CREATE DATABASE sigarp CHARACTER SET utf8mb4 COLLATE utf8mb4_spanish_ci;
USE sigarp;

-- ---------- Usuarios del sistema (quien inicia sesión) ----------
CREATE TABLE usuario (
  id_usuario      INT AUTO_INCREMENT PRIMARY KEY,
  nombre          VARCHAR(120) NOT NULL,
  correo          VARCHAR(120) NOT NULL UNIQUE,
  password_hash   VARCHAR(255) NOT NULL,           -- hash PBKDF2, nunca texto plano
  rol             ENUM('ESTUDIANTE','DOCENTE','ADMIN') NOT NULL,
  num_control     VARCHAR(12)  NULL UNIQUE,        -- solo estudiantes/egresados
  activo          TINYINT(1)   NOT NULL DEFAULT 1,
  fecha_alta      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------- Catálogos ----------
CREATE TABLE empresa (
  id_empresa  INT AUTO_INCREMENT PRIMARY KEY,
  nombre      VARCHAR(150) NOT NULL UNIQUE,
  sector      VARCHAR(100) NULL
);

CREATE TABLE area (
  id_area  INT AUTO_INCREMENT PRIMARY KEY,
  nombre   VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE docente (                              -- asesores internos (pueden no tener cuenta)
  id_docente  INT AUTO_INCREMENT PRIMARY KEY,
  nombre      VARCHAR(120) NOT NULL,
  correo      VARCHAR(120) NULL,
  id_usuario  INT NULL UNIQUE,                      -- si el docente también usa el sistema
  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

CREATE TABLE tecnologia (
  id_tecnologia  INT AUTO_INCREMENT PRIMARY KEY,
  nombre         VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE etiqueta (
  id_etiqueta  INT AUTO_INCREMENT PRIMARY KEY,
  nombre       VARCHAR(60) NOT NULL UNIQUE
);

-- ---------- Proyecto (centro del acervo) ----------
CREATE TABLE proyecto (
  id_proyecto               INT AUTO_INCREMENT PRIMARY KEY,
  titulo                    VARCHAR(250) NOT NULL,
  resumen                   TEXT NOT NULL,
  anio                      SMALLINT NOT NULL,
  periodo                   ENUM('ENE-JUN','AGO-DIC') NULL,
  metodologia               VARCHAR(100) NULL,
  id_empresa                INT NOT NULL,
  id_area                   INT NOT NULL,
  id_docente_asesor         INT NULL,
  nivel_confidencialidad    ENUM('PUBLICO','RESTRINGIDO','CONFIDENCIAL') NOT NULL DEFAULT 'PUBLICO',
  disponible_continuacion   TINYINT(1) NOT NULL DEFAULT 0,      -- banco de propuestas de extensión
  propuesta_continuacion    TEXT NULL,
  registrado_por            INT NOT NULL,
  fecha_registro            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_empresa)        REFERENCES empresa(id_empresa),
  FOREIGN KEY (id_area)           REFERENCES area(id_area),
  FOREIGN KEY (id_docente_asesor) REFERENCES docente(id_docente),
  FOREIGN KEY (registrado_por)    REFERENCES usuario(id_usuario),
  FULLTEXT KEY ft_proyecto (titulo, resumen)          -- búsqueda por texto libre (HU2)
);

-- Autores del proyecto. Egresados de años anteriores pueden no tener cuenta,
-- por eso se guardan sus datos aquí y la cuenta es opcional.
CREATE TABLE autor (
  id_autor            INT AUTO_INCREMENT PRIMARY KEY,
  id_proyecto         INT NOT NULL,
  nombre              VARCHAR(120) NOT NULL,
  num_control         VARCHAR(12) NULL,
  correo              VARCHAR(120) NULL,
  telefono            VARCHAR(20)  NULL,
  autoriza_contacto   TINYINT(1) NOT NULL DEFAULT 0,      -- HU5: solo se muestra si autoriza
  id_usuario          INT NULL,
  FOREIGN KEY (id_proyecto) REFERENCES proyecto(id_proyecto) ON DELETE CASCADE,
  FOREIGN KEY (id_usuario)  REFERENCES usuario(id_usuario)
);

CREATE TABLE proyecto_tecnologia (
  id_proyecto    INT NOT NULL,
  id_tecnologia  INT NOT NULL,
  PRIMARY KEY (id_proyecto, id_tecnologia),
  FOREIGN KEY (id_proyecto)   REFERENCES proyecto(id_proyecto) ON DELETE CASCADE,
  FOREIGN KEY (id_tecnologia) REFERENCES tecnologia(id_tecnologia)
);

CREATE TABLE proyecto_etiqueta (
  id_proyecto  INT NOT NULL,
  id_etiqueta  INT NOT NULL,
  PRIMARY KEY (id_proyecto, id_etiqueta),
  FOREIGN KEY (id_proyecto) REFERENCES proyecto(id_proyecto) ON DELETE CASCADE,
  FOREIGN KEY (id_etiqueta) REFERENCES etiqueta(id_etiqueta)
);

-- ---------- Expediente final (archivos) ----------
-- Los archivos se guardan en una carpeta del servidor; aquí solo la ruta.
CREATE TABLE archivo (
  id_archivo       INT AUTO_INCREMENT PRIMARY KEY,
  id_proyecto      INT NOT NULL,
  tipo             ENUM('INFORME','MANUAL','SOFTWARE','OTRO') NOT NULL,
  nombre_original  VARCHAR(255) NOT NULL,
  ruta             VARCHAR(500) NOT NULL,
  tamano_bytes     BIGINT NOT NULL,
  fecha_subida     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_proyecto) REFERENCES proyecto(id_proyecto) ON DELETE CASCADE
);

-- ---------- Valoraciones y comentarios moderados (HU4) ----------
CREATE TABLE valoracion (
  id_valoracion  INT AUTO_INCREMENT PRIMARY KEY,
  id_proyecto    INT NOT NULL,
  id_usuario     INT NOT NULL,
  estrellas      TINYINT NOT NULL CHECK (estrellas BETWEEN 1 AND 5),
  comentario     VARCHAR(1000) NULL,
  estado         ENUM('PENDIENTE','APROBADO','RECHAZADO') NOT NULL DEFAULT 'PENDIENTE',
  fecha          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_valoracion (id_proyecto, id_usuario),   -- una valoración por usuario
  FOREIGN KEY (id_proyecto) REFERENCES proyecto(id_proyecto) ON DELETE CASCADE,
  FOREIGN KEY (id_usuario)  REFERENCES usuario(id_usuario)
);

-- ---------- Bitácora de cambios del administrador (HU7) ----------
CREATE TABLE bitacora (
  id_bitacora  INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario   INT NOT NULL,
  accion       ENUM('CREAR','EDITAR','ELIMINAR','MODERAR') NOT NULL,
  entidad      VARCHAR(50) NOT NULL,       -- 'proyecto', 'etiqueta', 'valoracion'...
  id_entidad   INT NOT NULL,
  detalle      VARCHAR(500) NULL,
  fecha        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

-- ---------- Vista de apoyo para estadísticas (HU3) ----------
CREATE VIEW v_tecnologias_por_anio AS
SELECT p.anio, t.nombre AS tecnologia, COUNT(*) AS total
FROM proyecto p
JOIN proyecto_tecnologia pt ON pt.id_proyecto = p.id_proyecto
JOIN tecnologia t ON t.id_tecnologia = pt.id_tecnologia
GROUP BY p.anio, t.nombre;

-- ---------- Usuario de conexión para la aplicación ----------
-- Debe coincidir con src/conf/glassfish-resources.xml del EAR
CREATE USER IF NOT EXISTS 'sigarp'@'localhost' IDENTIFIED BY 'sigarp123';
GRANT SELECT, INSERT, UPDATE, DELETE ON sigarp.* TO 'sigarp'@'localhost';
FLUSH PRIVILEGES;

-- ---------- Datos de prueba ----------
-- Las contraseñas se guardan con PBKDF2 (formato de javax.security.enterprise Pbkdf2PasswordHash).
-- Los tres usuarios tienen la contraseña: Sigarp2026   (cámbiala antes de entregar)
INSERT INTO usuario (nombre, correo, password_hash, rol, num_control) VALUES
 ('Oficina de Vinculación', 'admin@sigarp.mx',
  'PBKDF2WithHmacSHA256:2048:/eouQPn2I3Pan9oILAhELt0Ijvq0T9Y7VgAbkaIlhIk=:X0hdFr5/1sCghyTGaLtHBlRG1lGpGW9L4s74GIMkr9Y=', 'ADMIN', NULL),
 ('Docente de prueba', 'docente@sigarp.mx',
  'PBKDF2WithHmacSHA256:2048:s6m2mRbAhz7BOVqMj3E03egDKRaal5pArjhZ+FdLn8k=:NBiysThRl6EWi2aXgdvqUf8K6nr+Rbrbip7PbBq3Jz8=', 'DOCENTE', NULL),
 ('Estudiante de prueba', 'estudiante@sigarp.mx',
  'PBKDF2WithHmacSHA256:2048:wtbbEUhheZOphhmOP6p6vcIbcUHdZYBnYw9H1j+EtkY=:SJXaNLQNHAS4gc1kT1+II3tEPCIRHi0iAeETIGCKA4Q=', 'ESTUDIANTE', '21160000');

INSERT INTO area (nombre) VALUES ('Desarrollo de software'), ('Redes'), ('Bases de datos');
INSERT INTO empresa (nombre, sector) VALUES ('Instituto Tecnológico de Oaxaca', 'Educación');
INSERT INTO tecnologia (nombre) VALUES ('Java'), ('PrimeFaces'), ('MySQL'), ('React'), ('Python');
INSERT INTO etiqueta (nombre) VALUES ('web'), ('gestión escolar'), ('inventario');
