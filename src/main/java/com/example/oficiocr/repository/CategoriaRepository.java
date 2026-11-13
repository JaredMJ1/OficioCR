package com.example.oficiocr.repository;

import com.example.oficiocr.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    @Override
    List<Categoria> findAll();

    @Override
    Optional<Categoria> findById(Long id);
}
