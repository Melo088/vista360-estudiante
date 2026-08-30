package co.edu.icesi.vista360.matricula.model;

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
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * El grupo como fila, no como atributo repetido en cada inscripcion.
 *
 * <p>La restriccion unica sobre (nrc, periodo) del esquema es lo que
 * sostiene la promesa del contrato de que el NRC identifica el grupo dentro del periodo.
 */
@Entity
@Table(name = "grupo_curso")
public class GrupoCurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // El DDL esta en dialecto Oracle, donde todo entero es NUMBER, y H2 lo reporta como
    // NUMERIC. Sin esto Hibernate esperaria BIGINT o INTEGER y la validacion del esquema
    // fallaria al arrancar. Contra un Oracle real el dialecto ya haria esta correspondencia.
    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "id")
    private Long id;

    @Column(name = "nrc", nullable = false, length = 5)
    private String nrc;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "periodo_codigo")
    private PeriodoAcademico periodo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asignatura_codigo")
    private Asignatura asignatura;

    @Column(name = "grupo", nullable = false, length = 3)
    private String grupo;

    @Column(name = "docente", nullable = false, length = 150)
    private String docente;

    @Column(name = "origen", nullable = false, length = 10)
    private String origen;

    @Column(name = "fecha_corte", nullable = false)
    private Instant fechaCorte;

    protected GrupoCurso() {
    }

    public Long getId() {
        return id;
    }

    public String getNrc() {
        return nrc;
    }

    public PeriodoAcademico getPeriodo() {
        return periodo;
    }

    public Asignatura getAsignatura() {
        return asignatura;
    }

    public String getGrupo() {
        return grupo;
    }

    public String getDocente() {
        return docente;
    }
}
