# Asociación Comunal - Grupo 10

Monorepo del proyecto universitario de la ADESCO Residencial Los Maquilishuats (San Salvador). El frontend es Next.js y el backend es una API REST en Spring Boot.

Los usuarios se guardan en PostgreSQL con JDBC y un pool Tomcat. El resto de módulos (proyectos, secretaría, tesorería y la bitácora de correos) sigue en archivos `.dat` mientras se migran.

## Estructura

```text
web-asociacion-comunal/       Frontend Next.js y shadcn/ui. Corre en el puerto 3000.
backend-asociacion-comunal/   API Spring Boot, Spring Security y JDBC. Corre en el puerto 8081.
docker-compose.yml            PostgreSQL 16 y pgAdmin.
```

## Requisitos

- Docker Desktop
- Java 21
- Node.js (o bun) para el frontend

El puerto publicado de Postgres es **5433**, no 5432. Así no choca con un Postgres instalado en la máquina.

## 1. Clonar

```bash
git clone https://github.com/Marcos6192005/AsociacionComunal-Grupo10-priv.git
cd AsociacionComunal-Grupo10-priv
```

## 2. Base de datos

Desde la raíz del repo:

```bash
docker compose up -d
```

Eso levanta dos contenedores:

| Servicio | URL | Para qué |
|---|---|---|
| Postgres | `localhost:5433` | Base `asociacion_comunal` |
| pgAdmin | http://localhost:5050 | Ver las tablas en el navegador |

En un volumen nuevo, Docker ejecuta `backend-asociacion-comunal/src/main/resources/schema.sql` al crear la base. Si el volumen ya existía y faltan las tablas, aplicalo a mano:

```powershell
Get-Content -Raw .\backend-asociacion-comunal\src\main\resources\schema.sql | docker compose exec -T db psql -U admin_asociacion_comunal -d asociacion_comunal
```

Credenciales de Postgres (las mismas en `docker-compose.yml` y en `application.properties`):

| Campo | Valor |
|---|---|
| Base | `asociacion_comunal` |
| Usuario | `admin_asociacion_comunal` |
| Contraseña | `asociacioncomunal_2K26Proyecto` |

pgAdmin pide otro login, el de la página: `admin@asociacion-comunal.com` / `asociacioncomunal_2K26Proyecto`.

Para registrar el servidor dentro de pgAdmin, el host es `db` y el puerto es `5432`. Ese puerto es el de adentro de la red de Docker. Desde Windows la base se alcanza por `localhost:5433`.

## 3. Backend

Con los contenedores arriba, en otra terminal:

```bash
cd backend-asociacion-comunal
```

Windows:

```bash
.\gradlew bootRun
```

Linux y macOS:

```bash
./gradlew bootRun
```

La API queda en http://localhost:8081. Swagger queda en http://localhost:8081/swagger-ui.html.

La primera vez que la tabla `usuario` está vacía, el backend siembra la junta y los vecinos. Las contraseñas se guardan con BCrypt. En la pantalla de login se escribe la clave en texto plano.

## 4. Frontend

En otra terminal, desde `web-asociacion-comunal`:

```bash
npm install
npm run dev
```

La aplicación queda en http://localhost:3000.

## 5. Pruebas

Desde `backend-asociacion-comunal`, con Postgres arriba (`contextLoads` levanta la aplicación completa):

```bash
.\gradlew test
```

El reporte queda en `backend-asociacion-comunal/build/reports/tests/test/index.html`.

## Cuentas de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| Presidente | `presidente@adesco.com` | `presi123` |
| Secretario | `secretario@adesco.com` | `secre123` |
| Tesorero | `tesorero@adesco.com` | `teso123` |
| Vecino | `vecino@adesco.com` | `vecino123` |

El resto de vecinos usa `vecino123`.
