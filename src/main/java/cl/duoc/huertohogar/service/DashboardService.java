package cl.duoc.huertohogar.service;

import cl.duoc.huertohogar.dto.OperationsDtos.DashboardResumenResponse;
import cl.duoc.huertohogar.model.EstadoBoleta;
import cl.duoc.huertohogar.model.Rol;
import cl.duoc.huertohogar.repository.BoletaRepository;
import cl.duoc.huertohogar.repository.ProductoRepository;
import cl.duoc.huertohogar.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final BoletaRepository boletaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public DashboardService(BoletaRepository boletaRepository,
                            ProductoRepository productoRepository,
                            UsuarioRepository usuarioRepository) {
        this.boletaRepository = boletaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public DashboardResumenResponse obtenerResumen() {
        long totalVentas = boletaRepository.sumTotalByEstado(EstadoBoleta.COMPLETADA);
        long totalOrdenes = boletaRepository.count();
        long ordenesCompletadas = boletaRepository.countByEstado(EstadoBoleta.COMPLETADA);
        long ordenesRechazadas = boletaRepository.countByEstado(EstadoBoleta.RECHAZADA);
        long productosActivos = productoRepository.countByActivoTrue();
        long stockCritico = productoRepository.countByActivoTrueAndStockLessThanEqual(50);
        long clientesRegistrados = usuarioRepository.countByRol(Rol.CLIENTE);
        long totalUsuarios = usuarioRepository.count();

        return new DashboardResumenResponse(
                totalVentas,
                totalOrdenes,
                ordenesCompletadas,
                ordenesRechazadas,
                productosActivos,
                stockCritico,
                clientesRegistrados,
                totalUsuarios
        );
    }
}
