package co.edu.icesi.vista360.matricula.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * El acto semestral.
 *
 * <p>Sin esta entidad no se distingue el estudiante matriculado que todavia no inscribe, que
 * responde 200 con lista vacia, del que no tiene ninguna matricula (S-14, S-17).
 */
@Entity
@Table(name = "matricula")
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // El DDL esta en dialecto Oracle, donde todo entero es NUMBER, y H2 lo reporta como
    // NUMERIC. Sin esto Hibernate esperaria BIGINT o INTEGER y la validacion del esquema
    // fallaria al arrancar. Contra un Oracle real el dialecto ya haria esta correspondencia.
    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_codigo")
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "periodo_codigo")
    private PeriodoAcademico periodo;

    /** Ordenados por orden ascendente: el primero es el programa principal (S-15). */
    @OneToMany(mappedBy = "matricula", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<MatriculaPrograma> programas = new ArrayList<>();

    @OneToMany(mappedBy = "matricula", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inscripcion> inscripciones = new ArrayList<>();

    @Column(name = "origen", nullable = false, length = 10)
    private String origen;

    @Column(name = "fecha_corte", nullable = false)
    private Instant fechaCorte;

    protected Matricula() {
    }

    public Long getId() {
        return id;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public PeriodoAcademico getPeriodo() {
        return periodo;
    }

    public List<MatriculaPrograma> getProgramas() {
        return programas;
    }

    public List<Inscripcion> getInscripciones() {
        return inscripciones;
    }

    public Instant getFechaCorte() {
        return fechaCorte;
    }
}
