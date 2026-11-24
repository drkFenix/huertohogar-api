package cl.duoc.huertohogar.repository;

import cl.duoc.huertohogar.model.DetalleBoleta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleBoletaRepository extends JpaRepository<DetalleBoleta, Long> {
    boolean existsByProductoId(Long productoId);
}
