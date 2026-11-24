package cl.duoc.huertohogar.repository;

import cl.duoc.huertohogar.model.Rol;
import cl.duoc.huertohogar.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
    boolean existsByCorreoIgnoreCase(String correo);
    long countByRol(Rol rol);
}
