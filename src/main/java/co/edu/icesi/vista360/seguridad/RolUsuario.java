package co.edu.icesi.vista360.seguridad;

import java.util.Optional;

/** Roles que la plataforma de identidad declara en el token (S-18). */
public enum RolUsuario {

    /** Consulta unicamente su propia informacion. */
    ESTUDIANTE,

    /** Consulta a los estudiantes que tiene asignados con vigencia abierta (S-05). */
    ACOMPANAMIENTO;

    /** Vacio si el token trae un rol que esta plataforma no conoce. */
    public static Optional<RolUsuario> desde(String valor) {
        if (valor == null) {
            return Optional.empty();
        }
        for (RolUsuario rol : values()) {
            if (rol.name().equals(valor)) {
                return Optional.of(rol);
            }
        }
        return Optional.empty();
    }
}
