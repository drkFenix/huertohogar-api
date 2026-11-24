package cl.duoc.huertohogar.controller;

import cl.duoc.huertohogar.dto.OperationsDtos.DashboardResumenResponse;
import cl.duoc.huertohogar.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Métricas reales de negocio para el Administrador")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/resumen")
    @Operation(summary = "Resumen de ventas, inventario y usuarios (Solo ADMIN)")
    public DashboardResumenResponse resumen() {
        return dashboardService.obtenerResumen();
    }
}
