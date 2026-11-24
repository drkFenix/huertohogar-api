package cl.duoc.huertohogar.service;

import cl.duoc.huertohogar.dto.OperationsDtos.BoletaItemRequest;
import cl.duoc.huertohogar.dto.OperationsDtos.BoletaResponse;
import cl.duoc.huertohogar.dto.OperationsDtos.CreateBoletaRequest;
import cl.duoc.huertohogar.model.*;
import cl.duoc.huertohogar.repository.BoletaRepository;
import cl.duoc.huertohogar.repository.ProductoRepository;
import cl.duoc.huertohogar.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class BoletaService {

    private final BoletaRepository boletaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public BoletaService(BoletaRepository boletaRepository,
                         ProductoRepository productoRepository,
                         UsuarioRepository usuarioRepository) {
        this.boletaRepository = boletaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public BoletaResponse create(CreateBoletaRequest request, String authenticatedEmail) {
        Boleta boleta = new Boleta();
        boleta.setNumeroOrden(generarNumeroOrden());
        boleta.setFecha(LocalDateTime.now());
        boleta.setClienteNombre(request.nombre().trim());
        boleta.setClienteApellidos(request.apellidos().trim());
        boleta.setClienteCorreo(request.correo().trim().toLowerCase());
        boleta.setCalle(request.calle().trim());
        boleta.setDepartamento(request.departamento() != null ? request.departamento().trim() : "");
        boleta.setRegion(request.region().trim());
        boleta.setComuna(request.comuna().trim());
        boleta.setIndicaciones(request.indicaciones() != null ? request.indicaciones().trim() : "");
        boleta.setEstado(request.simularRechazo() ? EstadoBoleta.RECHAZADA : EstadoBoleta.COMPLETADA);

        if (authenticatedEmail != null) {
            usuarioRepository.findByCorreoIgnoreCase(authenticatedEmail).ifPresent(boleta::setUsuario);
        }

        int totalCalculado = 0;

        for (BoletaItemRequest itemReq : request.items()) {
            Producto producto = productoRepository.findById(itemReq.productoId())
                    .filter(Producto::isActivo)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Producto no encontrado con id " + itemReq.productoId()));

            if (boleta.getEstado() == EstadoBoleta.COMPLETADA && producto.getStock() < itemReq.cantidad()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Stock insuficiente para " + producto.getNombre() + " (disponible: " + producto.getStock() + ")");
            }

            int precioVigente = producto.calcularPrecioVigente();
            int subtotal = precioVigente * itemReq.cantidad();
            totalCalculado += subtotal;

            DetalleBoleta detalle = new DetalleBoleta();
            detalle.setProducto(producto);
            detalle.setCodigoProducto(producto.getCodigo());
            detalle.setNombreProducto(producto.getNombre());
            detalle.setPrecioUnitario(precioVigente);
            detalle.setCantidad(itemReq.cantidad());
            detalle.setSubtotal(subtotal);
            boleta.agregarDetalle(detalle);

            if (boleta.getEstado() == EstadoBoleta.COMPLETADA) {
                producto.setStock(producto.getStock() - itemReq.cantidad());
                productoRepository.save(producto);
            }
        }

        boleta.setTotal(totalCalculado);
        return BoletaResponse.from(boletaRepository.save(boleta));
    }

    @Transactional(readOnly = true)
    public List<BoletaResponse> listAll() {
        return boletaRepository.findAllByOrderByFechaDesc()
                .stream()
                .map(BoletaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BoletaResponse> listMine(String authenticatedEmail) {
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        List<Boleta> byUser = boletaRepository.findByUsuarioIdOrderByFechaDesc(usuario.getId());
        if (!byUser.isEmpty()) {
            return byUser.stream().map(BoletaResponse::from).toList();
        }
        return boletaRepository.findByClienteCorreoIgnoreCaseOrderByFechaDesc(usuario.getCorreo())
                .stream()
                .map(BoletaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BoletaResponse findById(Long id, String authenticatedEmail, boolean isStaff) {
        Boleta boleta = boletaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Boleta no encontrada"));

        if (!isStaff) {
            boolean ownsByUser = boleta.getUsuario() != null
                    && boleta.getUsuario().getCorreo().equalsIgnoreCase(authenticatedEmail);
            boolean ownsByEmail = boleta.getClienteCorreo().equalsIgnoreCase(authenticatedEmail);
            if (!ownsByUser && !ownsByEmail) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "No tienes permiso para ver esta boleta");
            }
        }
        return BoletaResponse.from(boleta);
    }

    @Transactional
    public BoletaResponse updateEstado(Long id, EstadoBoleta nuevoEstado) {
        Boleta boleta = boletaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Boleta no encontrada"));

        EstadoBoleta anterior = boleta.getEstado();
        if (anterior == EstadoBoleta.COMPLETADA && nuevoEstado == EstadoBoleta.ANULADA) {
            for (DetalleBoleta d : boleta.getDetalles()) {
                Producto p = d.getProducto();
                p.setStock(p.getStock() + d.getCantidad());
                productoRepository.save(p);
            }
        }

        boleta.setEstado(nuevoEstado);
        return BoletaResponse.from(boletaRepository.save(boleta));
    }

    private String generarNumeroOrden() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        for (int i = 0; i < 10; i++) {
            int rand = ThreadLocalRandom.current().nextInt(1000, 9999);
            String candidate = "HH-" + datePart + "-" + rand;
            if (!boletaRepository.existsByNumeroOrden(candidate)) {
                return candidate;
            }
        }
        return "HH-" + System.currentTimeMillis();
    }
}
