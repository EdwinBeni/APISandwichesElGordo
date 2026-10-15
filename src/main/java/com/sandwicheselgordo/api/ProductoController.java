package com.sandwicheselgordo.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final JdbcTemplate jdbcTemplate;

    public ProductoController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<?> listarProductos() {
        String sql = """
                SELECT P.ID_PRODUCTO,
                       P.CODIGO,
                       P.NOMBRE,
                       P.PRECIO,
                       P.EXISTENCIA,
                       C.NOMBRE AS CATEGORIA
                FROM PRODUCTO P
                INNER JOIN CATEGORIA C
                    ON P.ID_CATEGORIA = C.ID_CATEGORIA
                WHERE P.ESTADO = 'A'
                ORDER BY C.NOMBRE, P.NOMBRE
                """;

        try {
            List<ProductoCatalogo> productos = jdbcTemplate.query(
                    sql,
                    (rs, fila) -> new ProductoCatalogo(
                            rs.getInt("ID_PRODUCTO"),
                            rs.getString("CODIGO"),
                            rs.getString("NOMBRE"),
                            rs.getString("CATEGORIA"),
                            rs.getBigDecimal("PRECIO"),
                            rs.getInt("EXISTENCIA") > 0
                    )
            );

            return ResponseEntity.ok(productos);

        } catch (DataAccessException exception) {
            return ResponseEntity.status(503).body(
                    Map.of(
                            "mensaje",
                            "No se pudo consultar el catálogo"
                    )
            );
        }
    }

    public record ProductoCatalogo(
            int idProducto,
            String codigo,
            String nombre,
            String categoria,
            BigDecimal precio,
            boolean disponible
    ) {
    }
}