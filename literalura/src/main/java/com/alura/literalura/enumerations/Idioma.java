package com.alura.literalura.enumerations;

import java.util.Arrays;
import java.util.Optional;

public enum Idioma {

    EN("Inglés"),
    ES("Español"),
    FR("Francés"),
    DE("Alemán"),
    IT("Italiano");

    private final String nombreCompleto;

    Idioma(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public static Optional<Idioma> desdeNombre(String nombre) {
        return Arrays.stream(values())
                .filter(idioma -> idioma.name().equalsIgnoreCase(nombre) || idioma.getNombreCompleto().equalsIgnoreCase(nombre))
                .findFirst();
    }

}
