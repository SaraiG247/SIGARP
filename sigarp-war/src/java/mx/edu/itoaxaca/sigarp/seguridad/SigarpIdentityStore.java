package mx.edu.itoaxaca.sigarp.seguridad;

import java.util.Collections;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import mx.edu.itoaxaca.sigarp.entidades.Usuario;
import mx.edu.itoaxaca.sigarp.negocio.UsuarioService;

/**
 * Valida correo + contraseña contra la tabla usuario y le dice al servidor
 * qué rol (grupo) tiene la persona. El servidor usa ese rol para aplicar
 * las reglas de web.xml y las anotaciones @RolesAllowed.
 */
@ApplicationScoped
public class SigarpIdentityStore implements IdentityStore {

    /**
     * Hash de relleno: si el correo no existe se verifica contra este valor
     * para que la respuesta tarde lo mismo y no se pueda adivinar qué correos existen.
     */
    private static final String HASH_RELLENO =
            "PBKDF2WithHmacSHA256:2048:m9VbR3m1l4XjXeEXBI66wHpk4jb8N6kZjFPaG22xxgk=:6qUofk8cQ76AIyXR3HxzFAZCnmiD/SiMgA/w44iykEo=";

    @EJB
    private UsuarioService usuarioService;

    @Inject
    private Pbkdf2PasswordHash passwordHash;

    /**
     * El IdentityStore llama a este método cuando la credencial es de tipo
     * usuario/contraseña (lo que manda LoginBean).
     */
    public CredentialValidationResult validate(UsernamePasswordCredential credencial) {
        Usuario usuario = usuarioService.buscarActivoPorCorreo(credencial.getCaller());
        char[] password = credencial.getPassword().getValue();

        if (usuario == null) {
            verificarSinExcepcion(password, HASH_RELLENO);
            return CredentialValidationResult.INVALID_RESULT;
        }
        if (!verificarSinExcepcion(password, usuario.getPasswordHash())) {
            return CredentialValidationResult.INVALID_RESULT;
        }

        // Nombre del "caller" = correo; grupo = rol (ADMIN, DOCENTE o ESTUDIANTE)
        return new CredentialValidationResult(
                usuario.getCorreo(),
                Collections.singleton(usuario.getRol().name()));
    }

    private boolean verificarSinExcepcion(char[] password, String hash) {
        try {
            return passwordHash.verify(password, hash);
        } catch (IllegalArgumentException e) {
            // Hash mal formado en la BD (p. ej. contraseña guardada en texto plano)
            return false;
        }
    }
}
