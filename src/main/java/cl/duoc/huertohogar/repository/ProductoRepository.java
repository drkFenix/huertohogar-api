package cl.duoc.huertohogar.repository;

import cl.duoc.huertohogar.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByCodigoIgnoreCase(String codigo);
    boolean existsByCodigoIgnoreCase(String codigo);
    boolean existsByCategoriaId(Long categoriaId);
    List<Producto> findByActivoTrueOrderByIdAsc();
    List<Producto> findByCategoriaIdAndActivoTrueOrderByIdAsc(Long categoriaId);
    List<Producto> findByEnOfertaTrueAndActivoTrueOrderByIdAsc();
    long countByActivoTrue();
    long countByActivoTrueAndStockLessThanEqual(Integer stock);
}
