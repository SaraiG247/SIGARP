package mx.edu.itoaxaca.sigarp.control;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import mx.edu.itoaxaca.sigarp.seguridad.Roles;

/**
 * Enrutamiento interno por rol. Lo usa panel.xhtml: según el rol que el
 * servidor le asignó al usuario al iniciar sesión, lo redirige a su área.
 *
 * Esto solo decide A DÓNDE ir; quién PUEDE entrar a cada área lo
 * garantiza web.xml, aunque alguien escriba la URL a mano.
 */
@Named
@RequestScoped
public class RuteoBean {

    @Inject
    private ExternalContext externalContext;

    public String irAInicioSegunRol() {
        if (externalContext.isUserInRole(Roles.ADMIN)) {
            return "/admin/inicio?faces-redirect=true";
        }
        if (externalContext.isUserInRole(Roles.DOCENTE)) {
            return "/docente/inicio?faces-redirect=true";
        }
        if (externalContext.isUserInRole(Roles.ESTUDIANTE)) {
            return "/estudiante/inicio?faces-redirect=true";
        }
        return "/error/403?faces-redirect=true";
    }
}
