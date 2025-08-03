package com.alura.literalura.repository;

import com.alura.literalura.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro,Long> {

    @Query("SELECT COUNT(l) > 0 FROM Libro l WHERE l.titulo = :titulo")
    boolean existsByTitulo(@Param("titulo") String titulo);


    Optional<Libro> findByTitulo(String titulo);

    List<Libro> findByAutorNombre(@Param("nombre") String nombre);

}
