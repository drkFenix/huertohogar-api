package cl.duoc.huertohogar.controller;

import cl.duoc.huertohogar.dto.CatalogDtos.ProductoRequest;
import cl.duoc.huertohogar.dto.CatalogDtos.ProductoResponse;
import cl.duoc.huertohogar.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "Catálogo público con filtros y gestión CRUD (ADMIN) de productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Listar productos", description = "Soporta filtros opcionales por categoriaId, enOferta, q (texto) y soloCriticos")
    public List<ProductoResponse> list(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Boolean enOferta,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean soloCriticos) {
        return productoService.list(categoriaId, enOferta, q, soloCriticos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar producto por ID")
    public ProductoResponse findById(@PathVariable Long id) {
        return productoService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Crear producto (Solo ADMIN)")
    public ResponseEntity<ProductoResponse> create(@Valid @RequestBody ProductoRequest request) {
        ProductoResponse created = productoService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/productos/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto (Solo ADMIN)")
    public ProductoResponse update(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
        return productoService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto (Solo ADMIN)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
