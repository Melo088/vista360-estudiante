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

@Entity
@Table(name = "inscripcion")
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "programa_codigo")
    private Programa programa;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_inscripcion", nullable = false, length = 10)
    private EstadoInscripcion estadoInscripcion;

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
}
