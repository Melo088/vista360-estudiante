package co.edu.icesi.vista360.matricula.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;

/**
 * Replica de identidad academica.
 *
 * <p>La llave es el codigo institucional y no el documento, que es credencial de acceso y no
 * llave de negocio, y no debe viajar en las URL (S-01, S-11).
 */
@Entity
@Table(name = "estudiante")
public class Estudiante {

    @Id
    @Column(name = "codigo_institucional", length = 9)
    private String codigoInstitucional;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "correo", length = 150)
    private String correo;

    @Column(name = "origen", nullable = false, length = 10)
    private String origen;

    @Column(name = "fecha_corte", nullable = false)
    private Instant fechaCorte;

    protected Estudiante() {
    }

    public String getCodigoInstitucional() {
        return codigoInstitucional;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getCorreo() {
        return correo;
    }

    public Instant getFechaCorte() {
        return fechaCorte;
    }
}
