package co.edu.icesi.vista360.matricula.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "periodo_academico")
public class PeriodoAcademico {

    @Id
    @Column(name = "codigo", length = 6)
    private String codigo;

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
