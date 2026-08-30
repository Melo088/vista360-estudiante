package co.edu.icesi.vista360.matricula.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;

/**
 * Catalogo de asignaturas.
 */
@Entity
@Table(name = "asignatura")
public class Asignatura {

    @Id
    @Column(name = "codigo", length = 5)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "creditos", nullable = false)
    private int creditos;

    @Column(name = "origen", nullable = false, length = 10)
    private String origen;

    @Column(name = "fecha_corte", nullable = false)
    private Instant fechaCorte;

    protected Asignatura() {
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCreditos() {
        return creditos;
    }
}
