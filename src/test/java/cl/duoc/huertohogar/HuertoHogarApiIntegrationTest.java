package cl.duoc.huertohogar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HuertoHogarApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private JsonNode login(String correo, String password) throws Exception {
        String body = """
                {"correo":"%s","password":"%s"}
                """.formatted(correo, password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    @Test
    void catalogoYSwaggerSonPublicos() throws Exception {
        mockMvc.perform(get("/api/v1/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));

        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("FR001"))
                .andExpect(jsonPath("$[0].origen").value("Valle del Maule"));

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("HuertoHogar API REST v1"));
    }

    @Test
    void endpointsProtegidosRetornan401SinToken() throws Exception {
        mockMvc.perform(get("/api/v1/boletas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").isNotEmpty());

        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/dashboard/resumen"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshTokenRenuevaSesionYNoSirveComoAccessToken() throws Exception {
        JsonNode session = login("cliente@huertohogar.cl", "ClienteHuerto2026");
        String accessToken = session.get("accessToken").asText();
        String refreshToken = session.get("refreshToken").asText();

        // Un refreshToken no debe ser aceptado como Bearer en endpoints protegidos
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized());

        // Un accessToken no debe ser aceptado en /refresh
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + accessToken + "\"}"))
                .andExpect(status().isUnauthorized());

        // Un refreshToken valido emite nuevos tokens
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.rol").value("CLIENTE"));
    }

    @Test
    void matrizDeRolesRestringeCorrectamenteClienteVendedorYAdmin() throws Exception {
        String clienteToken = login("cliente@huertohogar.cl", "ClienteHuerto2026").get("accessToken").asText();
        String vendedorToken = login("vendedor@huertohogar.cl", "VendedorHuerto2026").get("accessToken").asText();
        String adminToken = login("admin@huertohogar.cl", "AdminHuerto2026").get("accessToken").asText();

        // CLIENTE: puede ver su perfil y sus ordenes, pero 403 en boletas globales, dashboard y CRUD
        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
        mockMvc.perform(get("/api/v1/boletas/mis-ordenes").header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/boletas").header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/dashboard/resumen").header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isForbidden());

        // VENDEDOR: puede ver productos y boletas, pero 403 en CRUD productos, usuarios y dashboard
        mockMvc.perform(get("/api/v1/boletas").header("Authorization", "Bearer " + vendedorToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/productos").header("Authorization", "Bearer " + vendedorToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/productos")
                        .header("Authorization", "Bearer " + vendedorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"TEST99","nombre":"Pera","categoriaId":1,"precio":1000,"stock":10,"unidad":"kilo"}
                                """))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + vendedorToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/dashboard/resumen").header("Authorization", "Bearer " + vendedorToken))
                .andExpect(status().isForbidden());

        // ADMIN: acceso total a dashboard, usuarios y CRUD productos
        mockMvc.perform(get("/api/v1/dashboard/resumen").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productosActivos").isNumber())
                .andExpect(jsonPath("$.clientesRegistrados").isNumber());

        MvcResult createdProd = mockMvc.perform(post("/api/v1/productos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"FR099","nombre":"Peras Packham","categoriaId":1,"precio":1300,"stock":40,"unidad":"kilo","origen":"Valle del Maule","descripcion":"Peras frescas"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("FR099"))
                .andReturn();

        long prodId = objectMapper.readTree(createdProd.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/api/v1/productos/" + prodId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"FR099","nombre":"Peras Packham Premium","categoriaId":1,"precio":1400,"stock":35,"unidad":"kilo","origen":"Valle del Maule","descripcion":"Actualizadas"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Peras Packham Premium"));

        mockMvc.perform(delete("/api/v1/productos/" + prodId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/productos/" + prodId))
                .andExpect(status().isNotFound());
    }

    @Test
    void registroYCompraRealDescuentanStockYCalculanTotalEnServidor() throws Exception {
        MvcResult reg = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Sofia","apellidos":"Lagos","correo":"sofia.test@huertohogar.cl","password":"Password123","region":"Región Metropolitana de Santiago","comuna":"Providencia"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.rol").value("CLIENTE"))
                .andReturn();

        String token = objectMapper.readTree(reg.getResponse().getContentAsString()).get("accessToken").asText();

        // Consultar stock previo de producto 2 (Naranjas Valencia, $1000, stock 200)
        MvcResult beforeProd = mockMvc.perform(get("/api/v1/productos/2"))
                .andExpect(status().isOk())
                .andReturn();
        int stockAntes = objectMapper.readTree(beforeProd.getResponse().getContentAsString()).get("stock").asInt();

        MvcResult boletaRes = mockMvc.perform(post("/api/v1/boletas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre":"Sofia",
                                  "apellidos":"Lagos",
                                  "correo":"sofia.test@huertohogar.cl",
                                  "calle":"Av. Providencia 900",
                                  "departamento":"Depto 12",
                                  "region":"Región Metropolitana de Santiago",
                                  "comuna":"Providencia",
                                  "indicaciones":"Llamar al llegar",
                                  "simularRechazo":false,
                                  "items":[{"productoId":2,"cantidad":3}]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(3000))
                .andExpect(jsonPath("$.estado").value("COMPLETADA"))
                .andReturn();

        assertThat(objectMapper.readTree(boletaRes.getResponse().getContentAsString()).get("numeroOrden").asText())
                .startsWith("HH-");

        MvcResult afterProd = mockMvc.perform(get("/api/v1/productos/2"))
                .andExpect(status().isOk())
                .andReturn();
        int stockDespues = objectMapper.readTree(afterProd.getResponse().getContentAsString()).get("stock").asInt();
        assertThat(stockDespues).isEqualTo(stockAntes - 3);
    }

    @Test
    void adminActualizaUsuarioSinPerderDireccionYReactivaProductoDesactivado() throws Exception {
        String adminToken = login("admin@huertohogar.cl", "AdminHuerto2026").get("accessToken").asText();

        MvcResult usersRes = mockMvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        long clienteId = -1;
        for (JsonNode u : objectMapper.readTree(usersRes.getResponse().getContentAsString())) {
            if ("cliente@huertohogar.cl".equals(u.get("correo").asText())) {
                clienteId = u.get("id").asLong();
                break;
            }
        }
        assertThat(clienteId).isPositive();

        mockMvc.perform(put("/api/v1/usuarios/" + clienteId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Bryan","apellidos":"Rodríguez","rol":"CLIENTE","activo":true,"comuna":"Las Condes"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calle").value("Av. Apoquindo 4500"))
                .andExpect(jsonPath("$.telefono").value("+56 9 8000 1003"))
                .andExpect(jsonPath("$.region").value("Región Metropolitana de Santiago"));

        mockMvc.perform(delete("/api/v1/productos/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/productos/1"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/productos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"FR001","nombre":"Manzanas Fuji","categoriaId":1,"precio":1200,"stock":150,"unidad":"kilo","origen":"Valle del Maule","descripcion":"Crujientes y dulces","imagen":"/assets/manzanas.jpg","enOferta":true,"precioOferta":990}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.codigo").value("FR001"));
    }
}
