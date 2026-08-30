package co.edu.icesi.vista360.auditoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Una consulta a la informacion de un estudiante, permitida o denegada (Escenario 4B). */
@Entity
@Table(name = "auditoria_acceso")
public class AuditoriaAcceso {

    /** Sujeto que se registra cuando la peticion llega sin token. */
    public static final String SUJETO_ANONIMO = "anonimo";

    /** Rol que se registra cuando el token no declara ninguno conocido. */
    public static final String ROL_NINGUNO = "NINGUNO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // El DDL esta en dialecto Oracle, donde todo entero es NUMBER, y H2 lo reporta como
    // NUMERIC. Sin esto Hibernate esperaria BIGINT y la validacion del esquema fallaria.
    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "id")
    private Long id;

    @Column(name = "ocurrido_en", nullable = false)
    private Instant ocurridoEn;

    @Column(name = "sujeto_id", nullable = false, length = 50)
    private String sujetoId;

    @Column(name = "sujeto_tipo", nullable = false, length = 20)
    private String sujetoTipo;

    @Column(name = "rol", nullable = false, length = 30)
    private String rol;

    /** Nulo si la ruta no nombra a un estudiante. */
    @Column(name = "estudiante_consultado", length = 9)
    private String estudianteConsultado;

    @Column(name = "recurso", nullable = false, length = 200)
    private String recurso;

    @Column(name = "operacion", nullable = false, length = 10)
    private String operacion;

    @Column(name = "resultado", nullable = false, length = 20)
    private String resultado;

    @Column(name = "direccion_ip", length = 45)
    private String direccionIp;

    @Column(name = "agente_usuario", length = 300)
    private String agenteUsuario;

    @Column(name = "correlacion_id", nullable = false, length = 64)
    private String correlacionId;

    protected AuditoriaAcceso() {
    }

    AuditoriaAcceso(Instant ocurridoEn, String sujetoId, String sujetoTipo, String rol,
            String estudianteConsultado, String recurso, String operacion, String resultado,
            String direccionIp, String agenteUsuario, String correlacionId) {
        this.ocurridoEn = ocurridoEn;
        this.sujetoId = sujetoId;
        this.sujetoTipo = sujetoTipo;
        this.rol = rol;
        this.estudianteConsultado = estudianteConsultado;
        this.recurso = recurso;
        this.operacion = operacion;
        this.resultado = resultado;
        this.direccionIp = direccionIp;
        this.agenteUsuario = agenteUsuario;
        this.correlacionId = correlacionId;
    }

    public Long getId() {
        return id;
    }

    public Instant getOcurridoEn() {
        return ocurridoEn;
    }

    public String getSujetoId() {
        return sujetoId;
    }

    public String getRol() {
        return rol;
    }

    public String getEstudianteConsultado() {
        return estudianteConsultado;
    }

    public String getRecurso() {
        return recurso;
    }

    public String getResultado() {
        return resultado;
    }

    public String getDireccionIp() {
        return direccionIp;
    }

    public String getCorrelacionId() {
        return correlacionId;
    }

    /** Linea de log con la que se puede reponer la fila si la escritura fallo. */
    @Override
    public String toString() {
        return "auditoria_acceso[ocurridoEn=" + ocurridoEn + ", sujetoId=" + sujetoId
                + ", sujetoTipo=" + sujetoTipo + ", rol=" + rol
                + ", estudianteConsultado=" + estudianteConsultado + ", recurso=" + recurso
                + ", operacion=" + operacion + ", resultado=" + resultado
                + ", direccionIp=" + direccionIp + ", correlacionId=" + correlacionId + "]";
    }
}
