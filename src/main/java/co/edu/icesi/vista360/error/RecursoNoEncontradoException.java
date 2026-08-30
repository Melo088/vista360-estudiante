package co.edu.icesi.vista360.error;

/**
 * Lo que se pidio no existe. El manejador de errores la traduce a 404 en problem+json.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    private RecursoNoEncontradoException(String detalle) {
        super(detalle);
    }

    /** El codigo institucional no corresponde a ningun estudiante. */
    public static RecursoNoEncontradoException estudianteInexistente(String estudianteId) {
        return new RecursoNoEncontradoException(
                "No existe un estudiante con código " + estudianteId);
    }

    /**
     * El estudiante existe y no tiene ninguna matricula registrada.
     */
    public static RecursoNoEncontradoException sinNingunaMatricula(String estudianteId) {
        return new RecursoNoEncontradoException(
                "El estudiante " + estudianteId + " no tiene ninguna matrícula registrada");
    }
}
