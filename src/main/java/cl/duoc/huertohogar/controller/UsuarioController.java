package cl.duoc.huertohogar.controller;

import cl.duoc.huertohogar.dto.AuthDtos.UserResponse;
import cl.duoc.huertohogar.dto.OperationsDtos.AdminCreateUserRequest;
import cl.duoc.huertohogar.dto.OperationsDtos.AdminUpdateUserRequest;
import cl.duoc.huertohogar.dto.OperationsDtos.UpdateProfileRequest;
import cl.duoc.huertohogar.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Gestión de perfil propio y administración de cuentas y roles (ADMIN)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener perfil propio")
    public UserResponse getProfile(Authentication authentication) {
        return usuarioService.getProfile(authentication.getName());
    }

    @PutMapping("/me")
    @Operation(summary = "Actualizar perfil y dirección de entrega propia")
    public UserResponse updateProfile(Authentication authentication,
                                      @Valid @RequestBody UpdateProfileRequest request) {
        return usuarioService.updateProfile(authentication.getName(), request);
    }

    @GetMapping
    @Operation(summary = "Listar todos los usuarios (Solo ADMIN)")
    public List<UserResponse> listAll() {
        return usuarioService.listAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID (Solo ADMIN)")
    public UserResponse findById(@PathVariable Long id) {
        return usuarioService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Crear usuario con rol específico (Solo ADMIN)")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody AdminCreateUserRequest request) {
        UserResponse created = usuarioService.adminCreate(request);
        return ResponseEntity.created(URI.create("/api/v1/usuarios/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario y rol (Solo ADMIN)")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody AdminUpdateUserRequest request) {
        return usuarioService.adminUpdate(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar usuario (Solo ADMIN)")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        usuarioService.adminDelete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
