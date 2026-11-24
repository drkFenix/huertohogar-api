package cl.duoc.huertohogar.controller;

import cl.duoc.huertohogar.dto.OperationsDtos.BoletaResponse;
import cl.duoc.huertohogar.dto.OperationsDtos.CreateBoletaRequest;
import cl.duoc.huertohogar.dto.OperationsDtos.UpdateEstadoBoletaRequest;
import cl.duoc.huertohogar.service.BoletaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/boletas")
@Tag(name = "Boletas y Órdenes", description = "Creación transaccional de compras con control de stock y consulta por rol")
public class BoletaController {

    private final BoletaService boletaService;

    public BoletaController(BoletaService boletaService) {
        this.boletaService = boletaService;
    }

    @PostMapping
    @Operation(summary = "Crear boleta / procesar compra", description = "Recalcula precios desde BD, valida stock y descuenta inventario en transacción")
    public ResponseEntity<BoletaResponse> create(@Valid @RequestBody CreateBoletaRequest request,
                                                 Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        BoletaResponse created = boletaService.create(request, email);
        return ResponseEntity.created(URI.create("/api/v1/boletas/" + created.id())).body(created);
    }

    @GetMapping
    @Operation(summary = "Listar todas las boletas (ADMIN y VENDEDOR)")
    public List<BoletaResponse> listAll() {
        return boletaService.listAll();
    }

    @GetMapping("/mis-ordenes")
    @Operation(summary = "Listar órdenes del usuario autenticado")
    public List<BoletaResponse> listMine(Authentication authentication) {
        return boletaService.listMine(authentication.getName());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar detalle de boleta", description = "ADMIN y VENDEDOR ven cualquiera; CLIENTE solo sus propias boletas")
    public BoletaResponse findById(@PathVariable Long id, Authentication authentication) {
        boolean isStaff = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN") || a.equals("ROLE_VENDEDOR"));
        return boletaService.findById(id, authentication.getName(), isStaff);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de boleta (Solo ADMIN)", description = "Si pasa de COMPLETADA a ANULADA restaura el stock")
    public BoletaResponse updateEstado(@PathVariable Long id,
                                       @Valid @RequestBody UpdateEstadoBoletaRequest request) {
        return boletaService.updateEstado(id, request.estado());
    }
}
