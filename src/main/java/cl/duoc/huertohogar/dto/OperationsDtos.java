package cl.duoc.huertohogar.dto;

import cl.duoc.huertohogar.model.Boleta;
import cl.duoc.huertohogar.model.DetalleBoleta;
import cl.duoc.huertohogar.model.EstadoBoleta;
import cl.duoc.huertohogar.model.Rol;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class OperationsDtos {

    private OperationsDtos() {
    }

    public record BoletaItemRequest(
            @NotNull(message = "El productoId es obligatorio")
            Long productoId,

            @NotNull(message = "La cantidad es obligatoria")
            @Min(value = 1, message = "La cantidad debe ser al menos 1")
            Integer cantidad
    ) {
    }

    public record CreateBoletaRequest(
            @NotBlank(message = "El nombre del cliente es obligatorio")
            String nombre,

            @NotBlank(message = "Los apellidos del cliente son obligatorios")
            String apellidos,

            @NotBlank(message = "El correo del cliente es obligatorio")
            @Email(message = "El correo no es valido")
            String correo,

            @NotBlank(message = "La calle y numero son obligatorios")
            String calle,

            String departamento,

            @NotBlank(message = "La region es obligatoria")
            String region,

            @NotBlank(message = "La comuna es obligatoria")
            String comuna,

            String indicaciones,
            boolean simularRechazo,

            @NotEmpty(message = "La boleta debe incluir al menos un producto")
            @Valid
            List<BoletaItemRequest> items
    ) {
    }

    public record UpdateEstadoBoletaRequest(
            @NotNull(message = "El estado es obligatorio")
            EstadoBoleta estado
    ) {
    }

    public record DetalleBoletaResponse(
            Long id,
            Long productoId,
            String codigoProducto,
            String nombre,
            String imagen,
            Integer precio,
            Integer cantidad,
            Integer subtotal
    ) {
        public static DetalleBoletaResponse from(DetalleBoleta d) {
            return new DetalleBoletaResponse(
                    d.getId(),
                    d.getProducto().getId(),
                    d.getCodigoProducto(),
                    d.getNombreProducto(),
                    d.getProducto().getImagen(),
                    d.getPrecioUnitario(),
                    d.getCantidad(),
                    d.getSubtotal()
            );
        }
    }

    public record ClienteResumen(
            String nombre,
            String apellidos,
            String correo,
            String calle,
            String departamento,
            String region,
            String comuna,
            String indicaciones
    ) {
    }

    public record BoletaResponse(
            Long id,
            String numeroOrden,
            LocalDateTime fecha,
            Long usuarioId,
            ClienteResumen cliente,
            List<DetalleBoletaResponse> items,
            Integer total,
            EstadoBoleta estado
    ) {
        public static BoletaResponse from(Boleta b) {
            return new BoletaResponse(
                    b.getId(),
                    b.getNumeroOrden(),
                    b.getFecha(),
                    b.getUsuario() != null ? b.getUsuario().getId() : null,
                    new ClienteResumen(
                            b.getClienteNombre(),
                            b.getClienteApellidos(),
                            b.getClienteCorreo(),
                            b.getCalle(),
                            b.getDepartamento(),
                            b.getRegion(),
                            b.getComuna(),
                            b.getIndicaciones()
                    ),
                    b.getDetalles().stream().map(DetalleBoletaResponse::from).toList(),
                    b.getTotal(),
                    b.getEstado()
            );
        }
    }

    public record UpdateProfileRequest(
            @NotBlank(message = "El nombre es obligatorio")
            String nombre,
            String apellidos,
            String telefono,
            String calle,
            String departamento,
            String region,
            String comuna
    ) {
    }

    public record AdminCreateUserRequest(
            @NotBlank(message = "El nombre es obligatorio")
            String nombre,
            String apellidos,
            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "Correo invalido")
            String correo,
            @NotBlank(message = "La contrasena es obligatoria")
            @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
            String password,
            @NotNull(message = "El rol es obligatorio")
            Rol rol,
            String telefono,
            String calle,
            String departamento,
            String region,
            String comuna
    ) {
    }

    public record AdminUpdateUserRequest(
            @NotBlank(message = "El nombre es obligatorio")
            String nombre,
            String apellidos,
            @NotNull(message = "El rol es obligatorio")
            Rol rol,
            boolean activo,
            String password,
            String telefono,
            String calle,
            String departamento,
            String region,
            String comuna
    ) {
    }

    public record DashboardResumenResponse(
            long totalVentas,
            long totalOrdenes,
            long ordenesCompletadas,
            long ordenesRechazadas,
            long productosActivos,
            long stockCritico,
            long clientesRegistrados,
            long totalUsuarios
    ) {
    }
}
