package cl.duoc.huertohogar.dto;

import cl.duoc.huertohogar.model.Categoria;
import cl.duoc.huertohogar.model.Producto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class CatalogDtos {

    private CatalogDtos() {
    }

    public record CategoriaRequest(
            @NotBlank(message = "El nombre de la categoria es obligatorio")
            @Size(max = 100, message = "El nombre de la categoria no puede superar 100 caracteres")
            String nombre,

            @Size(max = 600, message = "La descripcion no puede superar 600 caracteres")
            String descripcion
    ) {
    }

    public record CategoriaResponse(
            Long id,
            String nombre,
            String descripcion
    ) {
        public static CategoriaResponse from(Categoria c) {
            return new CategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion());
        }
    }

    public record ProductoRequest(
            @NotBlank(message = "El codigo del producto es obligatorio")
            @Size(max = 20, message = "El codigo no puede superar 20 caracteres")
            String codigo,

            @NotBlank(message = "El nombre del producto es obligatorio")
            @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
            String nombre,

            @NotNull(message = "La categoriaId es obligatoria")
            Long categoriaId,

            @NotNull(message = "El precio es obligatorio")
            @Min(value = 1, message = "El precio debe ser mayor a 0")
            Integer precio,

            @NotNull(message = "El stock es obligatorio")
            @Min(value = 0, message = "El stock no puede ser negativo")
            Integer stock,

            @NotBlank(message = "La unidad de medida es obligatoria")
            String unidad,

            String origen,
            String descripcion,
            String imagen,
            boolean enOferta,
            Integer precioOferta
    ) {
    }

    public record ProductoResponse(
            Long id,
            String codigo,
            String nombre,
            Long categoriaId,
            String categoria,
            Integer precio,
            Integer stock,
            String unidad,
            String origen,
            String descripcion,
            String imagen,
            boolean enOferta,
            Integer precioOferta,
            Integer precioVigente,
            boolean activo
    ) {
        public static ProductoResponse from(Producto p) {
            return new ProductoResponse(
                    p.getId(),
                    p.getCodigo(),
                    p.getNombre(),
                    p.getCategoria().getId(),
                    p.getCategoria().getNombre(),
                    p.getPrecio(),
                    p.getStock(),
                    p.getUnidad(),
                    p.getOrigen(),
                    p.getDescripcion(),
                    p.getImagen(),
                    p.isEnOferta(),
                    p.getPrecioOferta(),
                    p.calcularPrecioVigente(),
                    p.isActivo()
            );
        }
    }
}
