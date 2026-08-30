package co.edu.icesi.vista360.matricula.model;

import co.edu.icesi.vista360.matricula.dto.MatriculaActualResponse.EscalaCalificacion;
import co.edu.icesi.vista360.matricula.dto.MatriculaActualResponse.EstadoCalificacion;
import co.edu.icesi.vista360.matricula.dto.MatriculaActualResponse.EstadoInscripcion;
import co.edu.icesi.vista360.matricula.dto.MatriculaActualResponse.ResultadoAprobacion;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Una inscripcion, no una asignatura (S-15).
 *
 * <p>Tres ejes independientes: el estado de la inscripcion, la escala con la que se califica
 * y hasta donde llego la calificacion (S-14, S-16). Las invariantes que los relacionan viven
 * en los CHECK del esquema y no aca, para que valgan tambien si el dato entra por el proceso
 * de sincronizacion y no por esta entidad.
 */
@Entity
@Table(name = "inscripcion")
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // El DDL esta en dialecto Oracle, donde todo entero es NUMBER, y H2 lo reporta como
    // NUMERIC. Sin esto Hibernate esperaria BIGINT o INTEGER y la validacion del esquema
    // fallaria al arrancar. Contra un Oracle real el dialecto ya haria esta correspondencia.
    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "matricula_id")
    private Matricula matricula;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asignatura_codigo")
    private Asignatura asignatura;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_curso_id")
    private GrupoCurso grupoCurso;

    /** Programa bajo el cual el ERP registro la inscripcion. La homologacion queda fuera. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "programa_codigo")
    private Programa programa;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_inscripcion", nullable = false, length = 10)
    private EstadoInscripcion estadoInscripcion;

    // El DATE de Oracle lleva hora, asi que H2 en MODE=Oracle lo reporta como TIMESTAMP.
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "fecha_cancelacion")
    private LocalDate fechaCancelacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "escala_calificacion", nullable = false, length = 10)
    private EscalaCalificacion escalaCalificacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_calificacion", nullable = false, length = 11)
    private EstadoCalificacion estadoCalificacion;

    @Column(name = "nota", precision = 3, scale = 2)
    private BigDecimal nota;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", length = 10)
    private ResultadoAprobacion resultado;

    @Column(name = "origen", nullable = false, length = 10)
    private String origen;

    @Column(name = "fecha_corte", nullable = false)
    private Instant fechaCorte;

    protected Inscripcion() {
    }

    public Asignatura getAsignatura() {
        return asignatura;
    }

    public GrupoCurso getGrupoCurso() {
        return grupoCurso;
    }

    public Programa getPrograma() {
        return programa;
    }

    public EstadoInscripcion getEstadoInscripcion() {
        return estadoInscripcion;
    }

    public LocalDate getFechaCancelacion() {
        return fechaCancelacion;
    }

    public EscalaCalificacion getEscalaCalificacion() {
        return escalaCalificacion;
    }

    public EstadoCalificacion getEstadoCalificacion() {
        return estadoCalificacion;
    }

    public BigDecimal getNota() {
        return nota;
    }

    public ResultadoAprobacion getResultado() {
        return resultado;
    }

    /** Hasta cuando esta al dia esta fila de la replica (S-02). */
    public Instant getFechaCorte() {
        return fechaCorte;
    }
}
