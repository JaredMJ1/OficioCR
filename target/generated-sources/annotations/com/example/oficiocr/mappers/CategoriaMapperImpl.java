package com.example.oficiocr.mappers;

import com.example.oficiocr.dto.CategoriaDTO;
import com.example.oficiocr.model.Categoria;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-11-12T23:04:29-0600",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class CategoriaMapperImpl implements CategoriaMapper {

    @Override
    public CategoriaDTO toDto(Categoria categoria) {
        if ( categoria == null ) {
            return null;
        }

        Long id = null;
        String nombre = null;
        String descripcion = null;
        Boolean activo = null;

        id = categoria.getId();
        nombre = categoria.getNombre();
        descripcion = categoria.getDescripcion();
        activo = categoria.getActivo();

        CategoriaDTO categoriaDTO = new CategoriaDTO( id, nombre, descripcion, activo );

        return categoriaDTO;
    }

    @Override
    public List<CategoriaDTO> toDtoList(List<Categoria> categorias) {
        if ( categorias == null ) {
            return null;
        }

        List<CategoriaDTO> list = new ArrayList<CategoriaDTO>( categorias.size() );
        for ( Categoria categoria : categorias ) {
            list.add( toDto( categoria ) );
        }

        return list;
    }

    @Override
    public Categoria toEntity(CategoriaDTO categoriaDTO) {
        if ( categoriaDTO == null ) {
            return null;
        }

        Categoria categoria = new Categoria();

        categoria.setId( categoriaDTO.id() );
        categoria.setNombre( categoriaDTO.nombre() );
        categoria.setDescripcion( categoriaDTO.descripcion() );
        categoria.setActivo( categoriaDTO.activo() );

        return categoria;
    }
}
