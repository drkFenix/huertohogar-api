package cl.duoc.huertohogar.controller;

import cl.duoc.huertohogar.dto.CatalogDtos.CategoriaRequest;
import cl.duoc.huertohogar.dto.CatalogDtos.CategoriaResponse;
import cl.duoc.huertohogar.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
@Tag(name = "Categorías", description = "Consulta pública y administración CRUD (ADMIN) de categorías")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @Operation(summary = "Listar categorías")
    public List<CategoriaResponse> list() {
        return categoriaService.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar categoría por ID")
    public CategoriaResponse findById(@PathVariable Long id) {
        return categoriaService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Crear categoría (Solo ADMIN)")
    public ResponseEntity<CategoriaResponse> create(@Valid @RequestBody CategoriaRequest request) {
        CategoriaResponse created = categoriaService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/categorias/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría (Solo ADMIN)")
    public CategoriaResponse update(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        return categoriaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría (Solo ADMIN)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoriaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
