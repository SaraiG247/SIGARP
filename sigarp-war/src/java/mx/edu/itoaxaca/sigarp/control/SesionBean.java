package mx.edu.itoaxaca.sigarp.control;

import java.io.Serializable;
import java.security.Principal;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import mx.edu.itoaxaca.sigarp.entidades.Usuario;
import mx.edu.itoaxaca.sigarp.negocio.UsuarioService;
import mx.edu.itoaxaca.sigarp.seguridad.Roles;

/**
 * Datos del usuario con sesión iniciada, para mostrarlos en las páginas
 * (nombre, rol) y decidir qué opciones del menú se ven.
 */
@Named
@SessionScoped
public class SesionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private UsuarioService usuarioService;

    private Usuario usuario;

    public boolean isAutenticado() {
        return principal() != null;
    }

    public boolean isAdmin() {
        return contexto().isUserInRole(Roles.ADMIN);
    }

    public boolean isDocente() {
        return contexto().isUserInRole(Roles.DOCENTE);
    }

    public boolean isEstudiante() {
        return contexto().isUserInRole(Roles.ESTUDIANTE);
    }

    /** Usuario de la BD; se carga una vez por sesión. */
    public Usuario getUsuario() {
        Principal principal = principal();
        if (principal == null) {
            usuario = null;
        } else if (usuario == null || !usuario.getCorreo().equalsIgnoreCase(principal.getName())) {
            usuario = usuarioService.buscarActivoPorCorreo(principal.getName());
        }
        return usuario;
    }

    public String getNombre() {
        Usuario u = getUsuario();
        return u != null ? u.getNombre() : "";
    }

    public String getRolEtiqueta() {
        Usuario u = getUsuario();
        return u != null ? u.getRol().getEtiqueta() : "";
    }

    public String cerrarSesion() throws ServletException {
        ExternalContext ec = contexto();
        ((HttpServletRequest) ec.getRequest()).logout();
        ec.invalidateSession();
        return "/index?faces-redirect=true";
    }

    private Principal principal() {
        return contexto().getUserPrincipal();
    }

    private ExternalContext contexto() {
        return FacesContext.getCurrentInstance().getExternalContext();
    }
}
