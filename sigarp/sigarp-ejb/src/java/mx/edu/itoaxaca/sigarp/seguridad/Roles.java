package mx.edu.itoaxaca.sigarp.seguridad;

/**
 * Nombres de los roles del sistema.
 * Son constantes para poder usarlas dentro de anotaciones
 * (@RolesAllowed, @DeclareRoles) y en web.xml deben escribirse igual.
 */
public final class Roles {

    public static final String ADMIN = "ADMIN";
    public static final String DOCENTE = "DOCENTE";
    public static final String ESTUDIANTE = "ESTUDIANTE";

    private Roles() {
    }
}
