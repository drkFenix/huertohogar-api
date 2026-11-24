package cl.duoc.huertohogar.service;

import cl.duoc.huertohogar.dto.AuthDtos.UserResponse;
import cl.duoc.huertohogar.dto.OperationsDtos.AdminCreateUserRequest;
import cl.duoc.huertohogar.dto.OperationsDtos.AdminUpdateUserRequest;
import cl.duoc.huertohogar.dto.OperationsDtos.UpdateProfileRequest;
import cl.duoc.huertohogar.model.Usuario;
import cl.duoc.huertohogar.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> listAll() {
        return usuarioRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse findById(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return UserResponse.from(u);
    }

    public UserResponse getProfile(String correo) {
        Usuario u = usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return UserResponse.from(u);
    }

    @Transactional
    public UserResponse updateProfile(String correo, UpdateProfileRequest request) {
        Usuario u = usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        u.setNombre(request.nombre().trim());
        u.setApellidos(request.apellidos() != null ? request.apellidos().trim() : "");
        u.setTelefono(trimOrNull(request.telefono()));
        u.setCalle(trimOrNull(request.calle()));
        u.setDepartamento(trimOrNull(request.departamento()));
        u.setRegion(trimOrNull(request.region()));
        u.setComuna(trimOrNull(request.comuna()));
        return UserResponse.from(usuarioRepository.save(u));
    }

    @Transactional
    public UserResponse adminCreate(AdminCreateUserRequest request) {
        String correo = request.correo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo");
        }

        Usuario u = new Usuario();
        u.setNombre(request.nombre().trim());
        u.setApellidos(request.apellidos() != null ? request.apellidos().trim() : "");
        u.setCorreo(correo);
        u.setPasswordHash(passwordEncoder.encode(request.password()));
        u.setRol(request.rol());
        u.setTelefono(trimOrNull(request.telefono()));
        u.setCalle(trimOrNull(request.calle()));
        u.setDepartamento(trimOrNull(request.departamento()));
        u.setRegion(trimOrNull(request.region()));
        u.setComuna(trimOrNull(request.comuna()));
        u.setActivo(true);
        return UserResponse.from(usuarioRepository.save(u));
    }

    @Transactional
    public UserResponse adminUpdate(Long id, AdminUpdateUserRequest request) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        u.setNombre(request.nombre().trim());
        u.setApellidos(request.apellidos() != null ? request.apellidos().trim() : "");
        u.setRol(request.rol());
        u.setActivo(request.activo());
        if (request.telefono() != null) {
            u.setTelefono(trimOrNull(request.telefono()));
        }
        if (request.calle() != null) {
            u.setCalle(trimOrNull(request.calle()));
        }
        if (request.departamento() != null) {
            u.setDepartamento(trimOrNull(request.departamento()));
        }
        if (request.region() != null) {
            u.setRegion(trimOrNull(request.region()));
        }
        if (request.comuna() != null) {
            u.setComuna(trimOrNull(request.comuna()));
        }

        if (request.password() != null && !request.password().isBlank()) {
            if (request.password().length() < 8) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La nueva contrasena debe tener al menos 8 caracteres");
            }
            u.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        return UserResponse.from(usuarioRepository.save(u));
    }

    @Transactional
    public void adminDelete(Long id, String currentAdminEmail) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if (u.getCorreo().equalsIgnoreCase(currentAdminEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No puedes eliminar tu propia cuenta de administrador activa");
        }
        u.setActivo(false);
        usuarioRepository.save(u);
    }

    private String trimOrNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
