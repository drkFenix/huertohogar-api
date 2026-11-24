package cl.duoc.huertohogar.dto;

import cl.duoc.huertohogar.model.Rol;
import cl.duoc.huertohogar.model.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no tiene un formato valido")
            String correo,

            @NotBlank(message = "La contrasena es obligatoria")
            String password
    ) {
    }

    public record RegisterRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
            String nombre,

            @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres")
            String apellidos,

            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no tiene un formato valido")
            String correo,

            @NotBlank(message = "La contrasena es obligatoria")
            @Size(min = 8, max = 100, message = "La contrasena debe tener al menos 8 caracteres")
            String password,

            String telefono,
            String calle,
            String departamento,
            String region,
            String comuna
    ) {
    }

    public record RefreshRequest(
            @NotBlank(message = "El refreshToken es obligatorio")
            String refreshToken
    ) {
    }

    public record UserResponse(
            Long id,
            String nombre,
            String apellidos,
            String correo,
            Rol rol,
            String telefono,
            String calle,
            String departamento,
            String region,
            String comuna,
            boolean activo,
            LocalDateTime fechaCreacion
    ) {
        public static UserResponse from(Usuario u) {
            return new UserResponse(
                    u.getId(),
                    u.getNombre(),
                    u.getApellidos(),
                    u.getCorreo(),
                    u.getRol(),
                    u.getTelefono(),
                    u.getCalle(),
                    u.getDepartamento(),
                    u.getRegion(),
                    u.getComuna(),
                    u.isActivo(),
                    u.getFechaCreacion()
            );
        }
    }

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn,
            UserResponse user
    ) {
    }
}
