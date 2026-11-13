package com.example.oficiocr.service;

import com.example.oficiocr.model.Categoria;
import com.example.oficiocr.repository.CategoriaRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CategoriaService {
    private CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }
    public List<Categoria> findAll() {
        return categoriaRepository.findAll();
    }
    public Optional<Categoria> findByCategoria(Long id) {
        log.info("Buscando categoria por Id {}", id);
        return categoriaRepository.findById(id);
    }
    public Categoria create(Categoria categoria) {
        categoria.setId(null);
        return categoriaRepository.save(categoria);
    }
    @Transactional
    public Optional<Categoria> update(Long id,Categoria datos) {
        return categoriaRepository.findById(id).map(categoria -> {
            categoria.setNombre(datos.getNombre());
            categoria.setDescripcion(datos.getDescripcion());
            return categoriaRepository.save(categoria);
        });
    }
    @Transactional
    public boolean delete(Long id) {
        Optional<Categoria> categoriaO = categoriaRepository.findById(id);
        if (categoriaO.isEmpty()) {
            return false;
        }

        Categoria categoria = categoriaO.get();
        categoria.setActivo(false);
        categoriaRepository.save(categoria);
        return true;
    }
}
