package com.example.oficiocr.controllers;

import com.example.oficiocr.dto.CategoriaDTO;
import com.example.oficiocr.mappers.CategoriaMapper;
import com.example.oficiocr.model.Categoria;
import com.example.oficiocr.service.CategoriaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
@Slf4j
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final CategoriaMapper categoriaMapper;

    public CategoriaController(CategoriaService categoriaService, CategoriaMapper categoriaMapper) {
        this.categoriaService = categoriaService;
        this.categoriaMapper = categoriaMapper;
    }

    @GetMapping
    public List<CategoriaDTO> findAll() {
        log.info("Iniciando lista de categorias");
        return categoriaMapper.toDtoList(categoriaService.findAll());
    }
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> findById(@PathVariable Long id) {
        log.info("Iniciando categoria por id: {}", id);
        return ResponseEntity.of(
                categoriaService.findByCategoria(id)
                        .map(categoriaMapper::toDto)
        );
    }
    @PostMapping()
    public ResponseEntity<CategoriaDTO> create(@RequestBody CategoriaDTO categoriaDTO) {
        Categoria creada = categoriaService.create(categoriaMapper.toEntity(categoriaDTO));
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(creada.getId()).toUri();
        return ResponseEntity.created(uri).body(categoriaMapper.toDto(creada));
    }
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDTO> update(@PathVariable Long id,@RequestBody CategoriaDTO categoriaDTO) {
        return ResponseEntity.of(
                categoriaService.update(id,categoriaMapper.toEntity(categoriaDTO)).map(categoriaMapper::toDto)
        );
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return categoriaService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
