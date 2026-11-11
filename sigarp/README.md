# SIGARP — Sistema Integral de Gestión del Acervo de Residencias Profesionales

NetBeans · Java with Ant · Enterprise Application · Payara 7 (Jakarta EE 11) · JDK 21 · Jakarta Faces 4 · PrimeFaces 16 · MySQL 8

## Estructura (UI / Control / Entidad)

```
sigarp            EAR  → src/conf: glassfish-resources.xml (conexión BD), glassfish-application.xml (roles)
sigarp-ejb        EJB  → entidades/ (JPA), negocio/ (servicios @Stateless), seguridad/Roles
sigarp-war        WAR  → control/ (beans), seguridad/ (login por roles), web/ (páginas)
bd/sigarp_bd.sql  script de la base de datos
```

Páginas del WAR:

| Ruta | Quién entra |
|---|---|
| `index`, `explorar`, `continuacion`, `acerca`, `login` | Público |
| `panel.xhtml` | Cualquier usuario con sesión (enrutador por rol) |
| `/estudiante/*` | ESTUDIANTE |
| `/docente/*` | DOCENTE y ADMIN |
| `/admin/*` | ADMIN |

Las plantillas están en `WEB-INF/plantillas/` (no se pueden abrir directo desde el navegador).

## Puesta en marcha

1. **Proyecto**: en NetBeans, File → New Project → Java with Ant → Java Enterprise → Enterprise Application.
   Nombre **`sigarp`**, Server **Payara Server (7)**, **Jakarta EE 11**, con módulos EJB y Web. Finish.
2. Cerrar NetBeans y **descomprimir este zip encima** de la carpeta del proyecto (reemplazar archivos).
   Borrar `sigarp-war/web/index.html` si NetBeans lo creó.
3. **PrimeFaces 16**: en `sigarp-war` → Properties → Libraries → Add JAR/Folder → `primefaces-16.x.jar` (Package ✔).
   Recomendado: guardar el jar en una carpeta `lib/` del repositorio.
4. **BD**: ejecutar `bd/sigarp_bd.sql` en MySQL como root. Crea la base `sigarp`, el usuario `sigarp/sigarp123` y 3 cuentas de prueba.
5. **Driver**: copiar `mysql-connector-j-9.x.jar` a `payara7/glassfish/domains/domain1/lib/` y reiniciar Payara.
6. Clean and Build de `sigarp` → Run → `http://localhost:8080/sigarp-war/`

### Cuentas de prueba (contraseña `Sigarp2026`)

| Correo | Rol |
|---|---|
| admin@sigarp.mx | ADMIN |
| docente@sigarp.mx | DOCENTE |
| estudiante@sigarp.mx | ESTUDIANTE |

## Cómo funciona la autenticación

Se usa **Jakarta Security**, la forma estándar en Jakarta EE:

1. `ConfiguracionSeguridad` declara el mecanismo de formulario (`login.xhtml`) y los roles.
2. `LoginBean` entrega correo y contraseña al contenedor (`SecurityContext.authenticate`).
3. `SigarpIdentityStore` busca al usuario, verifica el hash PBKDF2 y devuelve su rol como grupo.
4. Payara asigna ese grupo al rol (`glassfish-web.xml` / `glassfish-application.xml`).
5. `web.xml` protege cada carpeta por rol. Sin sesión → login y luego regreso a la página pedida.
   Con sesión pero sin el rol → `error/403.xhtml`.
6. `panel.xhtml` + `RuteoBean` mandan a cada quien a su área (enrutamiento interno).
7. En los EJB, `@RolesAllowed` protege también la lógica de negocio.

Para crear usuarios nuevos desde la app, generar el hash con
`@Inject Pbkdf2PasswordHash hash; hash.generate(password.toCharArray())`.
Nunca guardar contraseñas en texto plano.
