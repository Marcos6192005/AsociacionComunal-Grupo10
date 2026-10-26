# Asociación Comunal - Grupo 10

Sistema para la administración de la ADESCO Residencial Los Maquilishuats (San Salvador). Es el proyecto final de Programación II de la Universidad Evangélica de El Salvador.

El sistema cubre usuarios y roles de la junta directiva, proyectos comunitarios con votos y comentarios, secretaría (solicitudes, actas, acuerdos y comunicados), tesorería (cuotas y movimientos) y una bitácora de correos. Todos los módulos persisten en PostgreSQL mediante JDBC y un pool de conexiones Tomcat.

Repositorio: https://github.com/Marcos6192005/AsociacionComunal-Grupo10

## Integrantes

| Nombre | CIF | Rol |
|---|---|---|
| Marcos Armando Vásquez González | 2026011588 | Product Owner / Analista |
| Roberto De Jesus Erazo Ramirez | 2026010073 | Analista de negocio |
| Luis Gerardo Ramos Martínez | 2026010771 | Arquitecto |
| Anderson Steven Flores Pérez | 2026011705 | Desarrollador |

## Tecnologías

| Capa | Tecnología |
|---|---|
| Frontend | Next.js 16, React 19, TypeScript 5, Tailwind CSS 4, shadcn/ui |
| Backend | Java 21, Spring Boot 4, Spring Security, Spring Mail, springdoc (Swagger) |
| Persistencia | PostgreSQL 16, JDBC con DAOs propios, pool Tomcat JDBC |
| Pruebas | JUnit 5, Mockito, MockMvc |
| Infraestructura | Docker y Docker Compose, Gradle, bun |

## Estructura

```text
web-asociacion-comunal/       Frontend Next.js. Puerto 3000.
backend-asociacion-comunal/   API REST Spring Boot. Puerto 8081.
docker-compose.yml            PostgreSQL 16 y pgAdmin.
avances/                      Documentos de cada entrega.
```

El backend sigue una arquitectura en capas: `controllers` → `services` → `dao` (interfaces) → `dao/impl` (JDBC) → `DataSource`. La configuración del pool está en `config/DataSourceConfig.java`.

## Requisitos

- Docker Desktop
- Java 21
- bun (o Node.js 20+) para el frontend

El puerto publicado de Postgres es **5433**, no 5432, para no chocar con un Postgres instalado en la máquina.

## 1. Clonar

```bash
git clone https://github.com/Marcos6192005/AsociacionComunal-Grupo10.git
cd AsociacionComunal-Grupo10
```

## 2. Base de datos

Desde la raíz del repo:

```bash
docker compose up -d
```

| Servicio | URL | Para qué |
|---|---|---|
| Postgres | `localhost:5433` | Base `asociacion_comunal` |
| pgAdmin | http://localhost:5050 | Ver las tablas en el navegador |

En un volumen nuevo, Docker ejecuta `backend-asociacion-comunal/src/main/resources/schema.sql` y crea las 11 tablas. Si el volumen ya existía y faltan tablas, aplicalo a mano:

```powershell
Get-Content -Raw .\backend-asociacion-comunal\src\main\resources\schema.sql | docker compose exec -T db psql -U admin_asociacion_comunal -d asociacion_comunal
```

Credenciales de Postgres (las mismas en `docker-compose.yml` y en `application.properties`, claves `app.db.*`):

| Campo | Valor |
|---|---|
| Base | `asociacion_comunal` |
| Usuario | `admin_asociacion_comunal` |
| Contraseña | `asociacioncomunal_2K26Proyecto` |

pgAdmin usa otro login: `admin@asociacion-comunal.com` / `asociacioncomunal_2K26Proyecto`. Para registrar el servidor dentro de pgAdmin, el host es `db` y el puerto `5432` (el puerto interno de la red de Docker).

## 3. Backend

Con los contenedores arriba, desde `backend-asociacion-comunal`:

```bash
# Windows
.\gradlew bootRun

# Linux y macOS
./gradlew bootRun
```

La API queda en http://localhost:8081 y Swagger en http://localhost:8081/swagger-ui.html.

Al arrancar, cada DAO revisa si su tabla está vacía y, si lo está, carga datos de ejemplo: 21 usuarios, 6 proyectos, votos, comentarios, solicitudes, actas, comunicados, cuotas y movimientos. Las contraseñas se guardan con BCrypt.

### Backend con Docker (opcional)

El backend también tiene un `Dockerfile`. Con Postgres corriendo por compose:

```bash
docker build -t adesco-backend backend-asociacion-comunal
docker run --rm -p 8081:8081 -e APP_DB_URL=jdbc:postgresql://host.docker.internal:5433/asociacion_comunal adesco-backend
```

## 4. Frontend

Desde `web-asociacion-comunal`:

```bash
bun install
bun dev
```

Con npm: `npm install` y `npm run dev`. La aplicación queda en http://localhost:3000 y consume la API en `localhost:8081`.

## 5. Pruebas

Desde `backend-asociacion-comunal`, con Postgres arriba:

```bash
.\gradlew test
```

Son 57 pruebas: unitarias de servicios con Mockito, pruebas de controller con MockMvc e integración de los DAO contra PostgreSQL. El reporte queda en `backend-asociacion-comunal/build/reports/tests/test/index.html`.

Las pruebas de los DAO escriben en la misma base de desarrollo y no revierten sus cambios. Si después de correrlas querés volver a los datos de ejemplo, vaciá las tablas (menos `usuario`) y reiniciá el backend:

```powershell
"TRUNCATE movimiento, cuota, acuerdo_acta, acta, comunicado, solicitud, comentario, voto, proyecto, registro_email;" | docker compose exec -T db psql -U admin_asociacion_comunal -d asociacion_comunal
```

## Cuentas de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| Presidente | `presidente@adesco.com` | `presi123` |
| Secretario | `secretario@adesco.com` | `secre123` |
| Tesorero | `tesorero@adesco.com` | `teso123` |
| Vecino | `vecino@adesco.com` | `vecino123` |

El resto de vecinos usa `vecino123`.

## Concurrencia

El sistema usa concurrencia en dos lugares:

- **Generación de cuotas del mes** (`TesoreriaServiceImpl.generarCuotasDelMes`). El tesorero genera las cuotas de un periodo para todos los vecinos. Un `ExecutorService` de hasta 4 hilos crea una cuota por vecino en paralelo, cada tarea con su propia conexión del pool. Los resultados se cuentan con `AtomicInteger`. La restricción `UNIQUE (correo_vecino, periodo)` de la base evita duplicados aunque dos tareas o dos tesoreros corran a la vez: la violación (SQLState `23505`) se reporta como "ya existía" y no como error.
- **Envío de correos** (`EmailServiceImpl`). Los avisos se envían en un pool de 3 hilos (`app.mail.hilos`) con `CompletableFuture`, hasta 3 intentos con espera creciente. Cada envío queda registrado en la tabla `registro_email` con su estado y el hilo que lo procesó. Un fallo de correo nunca revierte la operación que lo originó.

## Documentación

Los documentos de cada entrega están en `avances/`. La entrega final está en `avances/avance-3/AVANCE3_DOCUMENTO_GRUPO_10.docx` (y su PDF).
