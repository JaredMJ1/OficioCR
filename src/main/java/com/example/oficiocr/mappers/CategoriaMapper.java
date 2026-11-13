package com.example.oficiocr.mappers;
import com.example.oficiocr.dto.CategoriaDTO;
import com.example.oficiocr.model.Categoria;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {
    CategoriaDTO toDto(Categoria categoria);

    List<CategoriaDTO> toDtoList(List<Categoria> categorias);

    Categoria toEntity(CategoriaDTO categoriaDTO);
}
