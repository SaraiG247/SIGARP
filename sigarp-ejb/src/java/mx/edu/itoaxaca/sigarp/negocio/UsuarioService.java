package mx.edu.itoaxaca.sigarp.negocio;

import java.util.List;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import mx.edu.itoaxaca.sigarp.entidades.Usuario;
import mx.edu.itoaxaca.sigarp.seguridad.Roles;

/**
 * Lógica de negocio de usuarios (capa :Control del lado del servidor).
 *
 * Cada método declara quién puede llamarlo. Aunque alguien se saltara la
 * interfaz web, el contenedor rechaza la llamada si no tiene el rol.
 */
@Stateless
public class UsuarioService {

    @PersistenceContext(unitName = "sigarpPU")
    private EntityManager em;

    /**
     * Busca un usuario activo por correo. Se usa durante el inicio de sesión,
     * cuando todavía no hay nadie autenticado; por eso es @PermitAll.
     *
     * @return el usuario, o null si no existe o está desactivado
     */
    @PermitAll
    public Usuario buscarActivoPorCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return null;
        }
        List<Usuario> encontrados = em.createNamedQuery(Usuario.POR_CORREO, Usuario.class)
                .setParameter("correo", correo.trim().toLowerCase())
                .setMaxResults(1)
                .getResultList();
        if (encontrados.isEmpty() || !encontrados.get(0).isActivo()) {
            return null;
        }
        return encontrados.get(0);
    }

    /** Ejemplo de método restringido: solo el administrador puede listar cuentas. */
    @RolesAllowed(Roles.ADMIN)
    public List<Usuario> listar() {
        return em.createQuery("SELECT u FROM Usuario u ORDER BY u.nombre", Usuario.class)
                .getResultList();
    }
}
