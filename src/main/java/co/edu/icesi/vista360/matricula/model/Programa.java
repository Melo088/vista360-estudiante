package co.edu.icesi.vista360.matricula.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "programa")
public class Programa {

    @Id
    @Column(name = "codigo", length = 10)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "origen", nullable = false, length = 10)
    private String origen;

    @Column(name = "fecha_corte", nullable = false)
    private Instant fechaCorte;

    protected Programa() {
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getOrigen() {
        return origen;
    }

    public Instant getFechaCorte() {
        return fechaCorte;
    }
}
