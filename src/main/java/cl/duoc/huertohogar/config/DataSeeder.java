package cl.duoc.huertohogar.config;

import cl.duoc.huertohogar.model.*;
import cl.duoc.huertohogar.repository.BoletaRepository;
import cl.duoc.huertohogar.repository.CategoriaRepository;
import cl.duoc.huertohogar.repository.ProductoRepository;
import cl.duoc.huertohogar.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final BoletaRepository boletaRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.admin-password:AdminHuerto2026}")
    private String adminPassword;

    @Value("${app.seed.vendedor-password:VendedorHuerto2026}")
    private String vendedorPassword;

    @Value("${app.seed.cliente-password:ClienteHuerto2026}")
    private String clientePassword;

    public DataSeeder(CategoriaRepository categoriaRepository,
                      ProductoRepository productoRepository,
                      UsuarioRepository usuarioRepository,
                      BoletaRepository boletaRepository,
                      PasswordEncoder passwordEncoder) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.boletaRepository = boletaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }

        Categoria frutas = ensureCategoria("Frutas Frescas",
                "Nuestra seleccion de frutas frescas ofrece una experiencia directa del campo a tu hogar, cosechadas en el punto optimo de madurez.");
        Categoria verduras = ensureCategoria("Verduras Orgánicas",
                "Verduras organicas cultivadas sin pesticidas ni quimicos, garantizando sabor autentico, vitaminas, minerales y fibra.");
        Categoria organicos = ensureCategoria("Productos Orgánicos",
                "Productos elaborados con ingredientes naturales y procesados de manera responsable: miel, quinua y granos saludables.");
        Categoria lacteos = ensureCategoria("Productos Lácteos",
                "Lacteos provenientes de granjas locales dedicadas a la produccion responsable y de calidad, ricos en calcio y nutrientes.");

        Producto manzanas = ensureProducto("FR001", "Manzanas Fuji", frutas, 1200, 150, "kilo",
                "Valle del Maule",
                "Manzanas Fuji crujientes y dulces, cultivadas en el Valle del Maule. Perfectas para meriendas saludables o postres.",
                "/assets/manzanas.jpg", true, 990);
        ensureProducto("FR002", "Naranjas Valencia", frutas, 1000, 200, "kilo",
                "Región de Coquimbo",
                "Jugosas y ricas en vitamina C, ideales para zumos frescos y refrescantes cultivadas en condiciones climaticas optimas.",
                "/assets/naranjas.png", false, null);
        ensureProducto("FR003", "Plátanos Cavendish", frutas, 800, 250, "kilo",
                "Selección Tropical Certificada",
                "Platanos maduros y dulces, perfectos para el desayuno o snack energetico ricos en potasio y vitaminas.",
                "/assets/platanos.png", false, null);
        ensureProducto("VR001", "Zanahorias Orgánicas", verduras, 900, 100, "kilo",
                "Región de O'Higgins",
                "Zanahorias crujientes cultivadas sin pesticidas en la Region de O'Higgins. Excelente fuente de vitamina A y fibra.",
                "/assets/zanahorias.jpg", true, 750);
        ensureProducto("VR002", "Espinacas Frescas", verduras, 700, 80, "bolsa 500g",
                "Valle Central de Chile",
                "Espinacas frescas y nutritivas cultivadas bajo practicas organicas, ideales para ensaladas y batidos verdes.",
                "/assets/espinacas.jpg", false, null);
        ensureProducto("VR003", "Pimientos Tricolores", verduras, 1500, 120, "kilo",
                "Región Metropolitana",
                "Pimientos rojos, amarillos y verdes ricos en antioxidantes y vitaminas para salteados y platos coloridos.",
                "/assets/pimientos.jpg", false, null);
        Producto miel = ensureProducto("PO001", "Miel Orgánica", organicos, 5000, 50, "frasco 500g",
                "Apicultores del Sur de Chile",
                "Miel pura y organica producida por apicultores locales, rica en antioxidantes y de sabor natural inigualable.",
                "/assets/miel.jpg", true, 4290);
        ensureProducto("PO003", "Quinua Orgánica", organicos, 3200, 70, "bolsa 1kg",
                "Altiplano Chileno",
                "Superalimento andino libre de gluten, rico en proteinas vegetales, fibra y minerales esenciales.",
                "/assets/quinua.png", false, null);
        ensureProducto("PL001", "Leche Entera", lacteos, 1100, 120, "litro",
                "Granjas del Sur de Chile",
                "Leche fresca de pastoreo natural del sur de Chile, pasteurizada y con sabor autentico.",
                "/assets/leche.jpg", false, null);

        ensureUsuario("Administrador", "HuertoHogar", "admin@huertohogar.cl", adminPassword, Rol.ADMIN,
                "+56 9 8000 1001", "Av. Providencia 1234", "Oficina 402", "Región Metropolitana de Santiago", "Providencia");
        ensureUsuario("Camila", "Vendedora", "vendedor@huertohogar.cl", vendedorPassword, Rol.VENDEDOR,
                "+56 9 8000 1002", "Av. Libertad 450", "Local 2", "Región de Valparaíso", "Viña del Mar");
        Usuario cliente = ensureUsuario("Bryan", "Rodríguez", "cliente@huertohogar.cl", clientePassword, Rol.CLIENTE,
                "+56 9 8000 1003", "Av. Apoquindo 4500", "Depto 302", "Región Metropolitana de Santiago", "Las Condes");

        if (boletaRepository.count() == 0) {
            Boleta b = new Boleta();
            b.setNumeroOrden("HH-20261101");
            b.setFecha(LocalDateTime.now().minusDays(1));
            b.setUsuario(cliente);
            b.setClienteNombre(cliente.getNombre());
            b.setClienteApellidos(cliente.getApellidos());
            b.setClienteCorreo(cliente.getCorreo());
            b.setCalle(cliente.getCalle());
            b.setDepartamento(cliente.getDepartamento());
            b.setRegion(cliente.getRegion());
            b.setComuna(cliente.getComuna());
            b.setIndicaciones("Entregar en conserjeria");
            b.setEstado(EstadoBoleta.COMPLETADA);

            DetalleBoleta d1 = new DetalleBoleta();
            d1.setProducto(manzanas);
            d1.setCodigoProducto(manzanas.getCodigo());
            d1.setNombreProducto(manzanas.getNombre());
            d1.setPrecioUnitario(manzanas.calcularPrecioVigente());
            d1.setCantidad(2);
            d1.setSubtotal(d1.getPrecioUnitario() * 2);
            b.agregarDetalle(d1);

            DetalleBoleta d2 = new DetalleBoleta();
            d2.setProducto(miel);
            d2.setCodigoProducto(miel.getCodigo());
            d2.setNombreProducto(miel.getNombre());
            d2.setPrecioUnitario(miel.calcularPrecioVigente());
            d2.setCantidad(1);
            d2.setSubtotal(d2.getPrecioUnitario());
            b.agregarDetalle(d2);

            b.setTotal(d1.getSubtotal() + d2.getSubtotal());
            boletaRepository.save(b);
        }
    }

    private Categoria ensureCategoria(String nombre, String descripcion) {
        return categoriaRepository.findByNombreIgnoreCase(nombre)
                .orElseGet(() -> categoriaRepository.save(new Categoria(nombre, descripcion)));
    }

    private Producto ensureProducto(String codigo, String nombre, Categoria categoria, int precio, int stock,
                                    String unidad, String origen, String descripcion, String imagen,
                                    boolean enOferta, Integer precioOferta) {
        return productoRepository.findByCodigoIgnoreCase(codigo).orElseGet(() -> {
            Producto p = new Producto();
            p.setCodigo(codigo);
            p.setNombre(nombre);
            p.setCategoria(categoria);
            p.setPrecio(precio);
            p.setStock(stock);
            p.setUnidad(unidad);
            p.setOrigen(origen);
            p.setDescripcion(descripcion);
            p.setImagen(imagen);
            p.setEnOferta(enOferta);
            p.setPrecioOferta(precioOferta);
            p.setActivo(true);
            return productoRepository.save(p);
        });
    }

    private Usuario ensureUsuario(String nombre, String apellidos, String correo, String rawPassword, Rol rol,
                                  String telefono, String calle, String departamento, String region, String comuna) {
        return usuarioRepository.findByCorreoIgnoreCase(correo).orElseGet(() -> {
            Usuario u = new Usuario();
            u.setNombre(nombre);
            u.setApellidos(apellidos);
            u.setCorreo(correo.toLowerCase());
            u.setPasswordHash(passwordEncoder.encode(rawPassword));
            u.setRol(rol);
            u.setTelefono(telefono);
            u.setCalle(calle);
            u.setDepartamento(departamento);
            u.setRegion(region);
            u.setComuna(comuna);
            u.setActivo(true);
            return usuarioRepository.save(u);
        });
    }
}
