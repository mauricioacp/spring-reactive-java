package com.learning.reactor.app.models;

import java.util.ArrayList;
import java.util.List;

public class Comentarios {
    List<String>comentarios;

    public Comentarios() {
        this.comentarios = new ArrayList<>();
    }

    public List<String> getComentarios() {
        return comentarios;
    }

    public void addComentario(String comentario) {
        this.comentarios.add(comentario);
    }

    @Override
    public String toString() {
        return "Comentarios " +
                 comentarios;
    }
}
