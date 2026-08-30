package co.edu.icesi.vista360.acompanamiento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Vinculo entre un estudiante y su acompanante, con vigencia (S-05). */
@Entity
@Table(name = "asignacion_acompanamiento")
public class AsignacionAcompanamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // El DDL esta en dialecto Oracle, donde todo entero es NUMBER, y H2 lo reporta como
    // NUMERIC. Sin esto Hibernate esperaria BIGINT y la validacion del esquema fallaria.
    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "id")
    private Long id;

    @Column(name = "estudiante_codigo", nullable = false, length = 9)
    private String estudianteCodigo;

    /** Sujeto que emite la plataforma de identidad para el acompanante (S-10). */
    @Column(name = "acompanante_id", nullable = false, length = 50)
    private String acompananteId;

    // El DATE de Oracle lleva hora, asi que H2 en MODE=Oracle lo reporta como TIMESTAMP.
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "vigente_desde", nullable = false)
    private LocalDate vigenteDesde;

    /** Nulo mientras la asignacion siga abierta. */
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "vigente_hasta")
    private LocalDate vigenteHasta;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    protected AsignacionAcompanamiento() {
    }

    public String getEstudianteCodigo() {
        return estudianteCodigo;
    }

    public String getAcompananteId() {
        return acompananteId;
    }

    public LocalDate getVigenteDesde() {
        return vigenteDesde;
    }

    public LocalDate getVigenteHasta() {
        return vigenteHasta;
    }
}
