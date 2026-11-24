package cl.duoc.huertohogar.service;

import cl.duoc.huertohogar.dto.CatalogDtos.CategoriaRequest;
import cl.duoc.huertohogar.dto.CatalogDtos.CategoriaResponse;
import cl.duoc.huertohogar.model.Categoria;
import cl.duoc.huertohogar.repository.CategoriaRepository;
import cl.duoc.huertohogar.repository.ProductoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository,
                            ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    public List<CategoriaResponse> list() {
        return categoriaRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(CategoriaResponse::from)
                .toList();
    }

    public Categoria findEntity(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria no encontrada"));
    }

    public CategoriaResponse findById(Long id) {
        return CategoriaResponse.from(findEntity(id));
    }

    @Transactional
    public CategoriaResponse create(CategoriaRequest request) {
        String nombre = request.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una categoria con ese nombre");
        }
        Categoria categoria = new Categoria(
                nombre,
                request.descripcion() != null ? request.descripcion().trim() : ""
        );
        return CategoriaResponse.from(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponse update(Long id, CategoriaRequest request) {
        Categoria categoria = findEntity(id);
        String nombre = request.nombre().trim();
        categoriaRepository.findByNombreIgnoreCase(nombre)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe otra categoria con ese nombre");
                });

        categoria.setNombre(nombre);
        categoria.setDescripcion(request.descripcion() != null ? request.descripcion().trim() : "");
        return CategoriaResponse.from(categoriaRepository.save(categoria));
    }

    @Transactional
    public void delete(Long id) {
        Categoria categoria = findEntity(id);
        if (productoRepository.existsByCategoriaId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar la categoria porque tiene productos asociados");
        }
        categoriaRepository.delete(categoria);
    }
}
