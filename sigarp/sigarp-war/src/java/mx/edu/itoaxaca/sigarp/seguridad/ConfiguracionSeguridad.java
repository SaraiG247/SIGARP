package mx.edu.itoaxaca.sigarp.seguridad;

import jakarta.annotation.security.DeclareRoles;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;

/**
 * Configuración central de seguridad (Jakarta Security).
 *
 * - Mecanismo: formulario propio (login.xhtml hecho con JSF/PrimeFaces).
 * - LoginToContinue: si alguien sin sesión pide una URL protegida, el servidor
 *   lo manda a login.xhtml y, al autenticarse, lo regresa a esa URL.
 * - Los usuarios y sus roles los valida {@link SigarpIdentityStore}.
 * - Qué URL requiere qué rol se define en WEB-INF/web.xml (security-constraint).
 *
 * En Jakarta Faces 4 ya no hace falta @FacesConfig: FacesContext y
 * ExternalContext se pueden inyectar con @Inject directamente.
 */
@CustomFormAuthenticationMechanismDefinition(
        loginToContinue = @LoginToContinue(
                loginPage = "/login.xhtml",
                useForwardToLogin = false,
                errorPage = ""
        )
)
@DeclareRoles({Roles.ADMIN, Roles.DOCENTE, Roles.ESTUDIANTE})
@ApplicationScoped
public class ConfiguracionSeguridad {
}
