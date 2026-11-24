package cl.duoc.huertohogar.repository;

import cl.duoc.huertohogar.model.Boleta;
import cl.duoc.huertohogar.model.EstadoBoleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BoletaRepository extends JpaRepository<Boleta, Long> {
    List<Boleta> findAllByOrderByFechaDesc();
    List<Boleta> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
    List<Boleta> findByClienteCorreoIgnoreCaseOrderByFechaDesc(String correo);
    boolean existsByNumeroOrden(String numeroOrden);

    @Query("SELECT COALESCE(SUM(b.total), 0) FROM Boleta b WHERE b.estado = :estado")
    long sumTotalByEstado(EstadoBoleta estado);

    long countByEstado(EstadoBoleta estado);
}
