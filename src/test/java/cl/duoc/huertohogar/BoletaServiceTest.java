package cl.duoc.huertohogar;

import cl.duoc.huertohogar.dto.OperationsDtos.BoletaItemRequest;
import cl.duoc.huertohogar.dto.OperationsDtos.BoletaResponse;
import cl.duoc.huertohogar.dto.OperationsDtos.CreateBoletaRequest;
import cl.duoc.huertohogar.model.*;
import cl.duoc.huertohogar.repository.BoletaRepository;
import cl.duoc.huertohogar.repository.ProductoRepository;
import cl.duoc.huertohogar.repository.UsuarioRepository;
import cl.duoc.huertohogar.service.BoletaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoletaServiceTest {

    @Mock
    private BoletaRepository boletaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private BoletaService boletaService;
    private Producto manzanas;

    @BeforeEach
    void setUp() {
        boletaService = new BoletaService(boletaRepository, productoRepository, usuarioRepository);

        Categoria cat = new Categoria("Frutas Frescas", "Desc");
        cat.setId(1L);

        manzanas = new Producto();
        manzanas.setId(1L);
        manzanas.setCodigo("FR001");
        manzanas.setNombre("Manzanas Fuji");
        manzanas.setCategoria(cat);
        manzanas.setPrecio(1200);
        manzanas.setEnOferta(true);
        manzanas.setPrecioOferta(990);
        manzanas.setStock(10);
        manzanas.setUnidad("kilo");
        manzanas.setActivo(true);
    }

    @Test
    void crearBoletaCalculaPrecioOfertaDesdeBdYDescuentaStock() {
        when(boletaRepository.existsByNumeroOrden(anyString())).thenReturn(false);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(manzanas));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(inv -> {
            Boleta b = inv.getArgument(0);
            b.setId(100L);
            return b;
        });

        CreateBoletaRequest req = new CreateBoletaRequest(
                "Bryan", "Rodriguez", "bryan@duocuc.cl",
                "Av. Central 100", "", "Región Metropolitana de Santiago", "Santiago Centro",
                "", false, List.of(new BoletaItemRequest(1L, 3))
        );

        BoletaResponse res = boletaService.create(req, null);

        assertThat(res.estado()).isEqualTo(EstadoBoleta.COMPLETADA);
        assertThat(res.total()).isEqualTo(2970);
        assertThat(manzanas.getStock()).isEqualTo(7);
        assertThat(res.items()).hasSize(1);
        assertThat(res.items().get(0).precio()).isEqualTo(990);
    }

    @Test
    void crearBoletaRechazaCuandoStockEsInsuficiente() {
        when(boletaRepository.existsByNumeroOrden(anyString())).thenReturn(false);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(manzanas));

        CreateBoletaRequest req = new CreateBoletaRequest(
                "Bryan", "Rodriguez", "bryan@duocuc.cl",
                "Av. Central 100", "", "Región Metropolitana de Santiago", "Santiago Centro",
                "", false, List.of(new BoletaItemRequest(1L, 15))
        );

        assertThatThrownBy(() -> boletaService.create(req, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Stock insuficiente");
        assertThat(manzanas.getStock()).isEqualTo(10);
    }

    @Test
    void crearBoletaSimulandoRechazoNoDescuentaStock() {
        when(boletaRepository.existsByNumeroOrden(anyString())).thenReturn(false);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(manzanas));
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateBoletaRequest req = new CreateBoletaRequest(
                "Bryan", "Rodriguez", "bryan@duocuc.cl",
                "Av. Central 100", "", "Región Metropolitana de Santiago", "Santiago Centro",
                "", true, List.of(new BoletaItemRequest(1L, 4))
        );

        BoletaResponse res = boletaService.create(req, null);

        assertThat(res.estado()).isEqualTo(EstadoBoleta.RECHAZADA);
        assertThat(manzanas.getStock()).isEqualTo(10);
    }
}
