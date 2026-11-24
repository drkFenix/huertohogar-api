package cl.duoc.huertohogar.service;

import cl.duoc.huertohogar.dto.CatalogDtos.ProductoRequest;
import cl.duoc.huertohogar.dto.CatalogDtos.ProductoResponse;
import cl.duoc.huertohogar.model.Categoria;
import cl.duoc.huertohogar.model.Producto;
import cl.duoc.huertohogar.repository.DetalleBoletaRepository;
import cl.duoc.huertohogar.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaService categoriaService;
    private final DetalleBoletaRepository detalleBoletaRepository;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaService categoriaService,
                           DetalleBoletaRepository detalleBoletaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaService = categoriaService;
        this.detalleBoletaRepository = detalleBoletaRepository;
    }

    public List<ProductoResponse> list(Long categoriaId, Boolean enOferta, String q, Boolean soloCriticos) {
        List<Producto> base = productoRepository.findByActivoTrueOrderByIdAsc();
        String query = q != null ? q.trim().toLowerCase() : "";

        return base.stream()
                .filter(p -> categoriaId == null || p.getCategoria().getId().equals(categoriaId))
                .filter(p -> enOferta == null || !enOferta || p.isEnOferta())
                .filter(p -> soloCriticos == null || !soloCriticos || (p.getStock() > 0 && p.getStock() <= 50))
                .filter(p -> query.isEmpty()
                        || p.getNombre().toLowerCase().contains(query)
                        || p.getCodigo().toLowerCase().contains(query)
                        || p.getCategoria().getNombre().toLowerCase().contains(query))
                .map(ProductoResponse::from)
                .toList();
    }

    public Producto findEntity(Long id) {
        return productoRepository.findById(id)
                .filter(Producto::isActivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    public ProductoResponse findById(Long id) {
        return ProductoResponse.from(findEntity(id));
    }

    @Transactional
    public ProductoResponse create(ProductoRequest request) {
        String codigo = request.codigo().trim().toUpperCase();
        validateOferta(request);
        Categoria categoria = categoriaService.findEntity(request.categoriaId());

        Producto existente = productoRepository.findByCodigoIgnoreCase(codigo).orElse(null);
        if (existente != null) {
            if (existente.isActivo()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con el codigo " + codigo);
            }
            applyRequest(existente, request, codigo, categoria);
            existente.setActivo(true);
            return ProductoResponse.from(productoRepository.save(existente));
        }

        Producto producto = new Producto();
        applyRequest(producto, request, codigo, categoria);
        producto.setActivo(true);
        return ProductoResponse.from(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse update(Long id, ProductoRequest request) {
        Producto producto = findEntity(id);
        String codigo = request.codigo().trim().toUpperCase();
        productoRepository.findByCodigoIgnoreCase(codigo)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe otro producto con el codigo " + codigo);
                });
        validateOferta(request);
        Categoria categoria = categoriaService.findEntity(request.categoriaId());

        applyRequest(producto, request, codigo, categoria);
        return ProductoResponse.from(productoRepository.save(producto));
    }

    @Transactional
    public void delete(Long id) {
        Producto producto = findEntity(id);
        if (detalleBoletaRepository.existsByProductoId(id)) {
            producto.setActivo(false);
            productoRepository.save(producto);
        } else {
            productoRepository.delete(producto);
        }
    }

    private void validateOferta(ProductoRequest request) {
        if (request.enOferta()) {
            if (request.precioOferta() == null || request.precioOferta() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El precio de oferta es obligatorio y debe ser mayor a 0 cuando el producto esta en oferta");
            }
            if (request.precioOferta() >= request.precio()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El precio de oferta debe ser menor al precio normal");
            }
        }
    }

    private void applyRequest(Producto producto, ProductoRequest request, String codigo, Categoria categoria) {
        producto.setCodigo(codigo);
        producto.setNombre(request.nombre().trim());
        producto.setCategoria(categoria);
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        producto.setUnidad(request.unidad().trim());
        producto.setOrigen(request.origen() != null && !request.origen().isBlank()
                ? request.origen().trim()
                : "Agricultura local de Chile");
        producto.setDescripcion(request.descripcion() != null ? request.descripcion().trim() : "");
        producto.setImagen(request.imagen() != null && !request.imagen().isBlank()
                ? request.imagen().trim()
                : "/assets/logo.png");
        producto.setEnOferta(request.enOferta());
        producto.setPrecioOferta(request.enOferta() ? request.precioOferta() : null);
    }
}
