package co.edu.icesi.vista360.matricula.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Periodo academico.
 *
 * <p>Vigente es el que contiene la fecha de hoy y, si no hay ninguno porque el calendario
 * esta entre semestres, el mas reciente (S-14, S-17).
 */
@Entity
@Table(name = "periodo_academico")
public class PeriodoAcademico {

    @Id
    @Column(name = "codigo", length = 6)
    private String codigo;

    // El DATE de Oracle lleva hora, asi que H2 en MODE=Oracle lo reporta como TIMESTAMP.
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "origen", nullable = false, length = 10)
    private String origen;

    @Column(name = "fecha_corte", nullable = false)
    private Instant fechaCorte;

    protected PeriodoAcademico() {
    }

    public String getCodigo() {
        return codigo;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public Instant getFechaCorte() {
        return fechaCorte;
    }
}
