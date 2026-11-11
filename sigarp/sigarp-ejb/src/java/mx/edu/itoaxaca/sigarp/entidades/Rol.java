package mx.edu.itoaxaca.sigarp.entidades;

/**
 * Rol de un usuario. Se guarda como texto en la columna usuario.rol
 * y su nombre coincide con las constantes de {@link mx.edu.itoaxaca.sigarp.seguridad.Roles}.
 */
public enum Rol {

    ADMIN("Administrador"),
    DOCENTE("Docente"),
    ESTUDIANTE("Estudiante");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
