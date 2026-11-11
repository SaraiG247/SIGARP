package mx.edu.itoaxaca.sigarp.control;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.credential.Password;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Control de la página login.xhtml.
 * No compara contraseñas aquí: le pasa la credencial al contenedor
 * (SecurityContext) y este la valida con SigarpIdentityStore.
 */
@Named
@RequestScoped
public class LoginBean {

    @Inject
    private SecurityContext securityContext;

    @Inject
    private FacesContext facesContext;

    @Inject
    private ExternalContext externalContext;

    private String correo;
    private String password;

    public void entrar() throws IOException {
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

        UsernamePasswordCredential credencial =
                new UsernamePasswordCredential(correo.trim().toLowerCase(), new Password(password));

        AuthenticationStatus estado = securityContext.authenticate(request, response,
                AuthenticationParameters.withParams()
                        .credential(credencial)
                        .newAuthentication(true));

        switch (estado) {
            case SEND_CONTINUE:
                // Venía de una página protegida: el servidor ya lo está regresando a ella.
                facesContext.responseComplete();
                break;
            case SUCCESS:
                // Entró directo al login: lo mandamos al enrutador por rol.
                externalContext.redirect(externalContext.getRequestContextPath() + "/panel.xhtml");
                break;
            default:
                // SEND_FAILURE / NOT_DONE: mensaje genérico (no revelar si el correo existe)
                password = null;
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Correo o contraseña incorrectos.", null));
                break;
        }
    }

    /** viewAction de login.xhtml: si ya hay sesión, no mostrar el formulario. */
    public String redirigirSiYaTieneSesion() {
        if (externalContext.getUserPrincipal() != null) {
            return "/panel?faces-redirect=true";
        }
        return null;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
