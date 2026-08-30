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

@Entity
@Table(name = "matricula_programa")
public class MatriculaPrograma {

    public static final int ORDEN_PRINCIPAL = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "matricula_id")
    private Matricula matricula;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "programa_codigo")
    private Programa programa;

    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "orden", nullable = false)
    private int orden;

    protected MatriculaPrograma() {
    }

    public Programa getPrograma() {
        return programa;
    }

    public int getOrden() {
        return orden;
    }

    public boolean esPrincipal() {
        return orden == ORDEN_PRINCIPAL;
    }
}
