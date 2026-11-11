package mx.edu.itoaxaca.sigarp.entidades;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuenta con la que una persona inicia sesión en SIGARP (tabla usuario).
 */
@Entity
@Table(name = "usuario")
@NamedQuery(name = Usuario.POR_CORREO,
        query = "SELECT u FROM Usuario u WHERE LOWER(u.correo) = :correo")
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String POR_CORREO = "Usuario.porCorreo";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @NotNull
    @Size(max = 120)
    @Column(name = "nombre")
    private String nombre;

    @NotNull
    @Size(max = 120)
    @Column(name = "correo", unique = true)
    private String correo;

    /** Hash PBKDF2 generado con Pbkdf2PasswordHash. Nunca la contraseña en texto plano. */
    @NotNull
    @Column(name = "password_hash")
    private String passwordHash;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "rol")
    private Rol rol;

    @Size(max = 12)
    @Column(name = "num_control")
    private String numControl;

    @Column(name = "activo")
    private boolean activo = true;

    @Column(name = "fecha_alta", insertable = false, updatable = false)
    private LocalDateTime fechaAlta;

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getNumControl() {
        return numControl;
    }

    public void setNumControl(String numControl) {
        this.numControl = numControl;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Usuario)) {
            return false;
        }
        Usuario otro = (Usuario) o;
        return idUsuario != null && idUsuario.equals(otro.idUsuario);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idUsuario);
    }

    @Override
    public String toString() {
        return "Usuario[" + idUsuario + ", " + correo + ", " + rol + "]";
    }
}
