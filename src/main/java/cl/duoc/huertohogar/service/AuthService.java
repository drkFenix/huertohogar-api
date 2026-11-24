package cl.duoc.huertohogar.service;

import cl.duoc.huertohogar.dto.AuthDtos.*;
import cl.duoc.huertohogar.model.Rol;
import cl.duoc.huertohogar.model.Usuario;
import cl.duoc.huertohogar.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(AuthenticationManager authenticationManager,
                       UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenService jwtTokenService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    public AuthResponse login(LoginRequest request) {
        String correo = request.correo().trim().toLowerCase();
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(correo, request.password()));
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contrasena incorrectos");
        }

        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));

        if (!usuario.isActivo()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta se encuentra desactivada");
        }

        return buildAuthResponse(usuario);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String correo = request.correo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta registrada con ese correo");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setApellidos(request.apellidos() != null ? request.apellidos().trim() : "");
        usuario.setCorreo(correo);
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setRol(Rol.CLIENTE);
        usuario.setTelefono(trimOrNull(request.telefono()));
        usuario.setCalle(trimOrNull(request.calle()));
        usuario.setDepartamento(trimOrNull(request.departamento()));
        usuario.setRegion(trimOrNull(request.region()));
        usuario.setComuna(trimOrNull(request.comuna()));
        usuario.setActivo(true);

        Usuario guardado = usuarioRepository.save(usuario);
        return buildAuthResponse(guardado);
    }

    public AuthResponse refresh(RefreshRequest request) {
        Jwt jwt = jwtTokenService.decodeRefreshToken(request.refreshToken().trim());
        String correo = jwt.getSubject();

        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        if (!usuario.isActivo()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cuenta desactivada");
        }

        return buildAuthResponse(usuario);
    }

    public UserResponse me(String correo) {
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return UserResponse.from(usuario);
    }

    private AuthResponse buildAuthResponse(Usuario usuario) {
        String accessToken = jwtTokenService.generateAccessToken(usuario);
        String refreshToken = jwtTokenService.generateRefreshToken(usuario);
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenService.getAccessExpirationSeconds(),
                UserResponse.from(usuario)
        );
    }

    private String trimOrNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
