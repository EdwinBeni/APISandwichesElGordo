package com.sandwicheselgordo.api;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final JdbcTemplate jdbcTemplate;

    public ClienteController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/codigo/{codigo}/puntos")
    public ResponseEntity<?> consultarPorCodigo(
            @PathVariable("codigo") String codigo) {

        String codigoCliente = codigo.trim()
                .toUpperCase(Locale.ROOT);

        if (codigoCliente.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje", "Ingresa el código del cliente")
            );
        }

        String sql = """
                SELECT ID_CLIENTE,
                       CODIGO_CLIENTE,
                       PUNTOS,
                       ESTADO
                FROM CLIENTE
                WHERE UPPER(TRIM(CODIGO_CLIENTE)) = ?
                """;

        return consultarCliente(sql, codigoCliente);
    }

    @GetMapping("/{id}/puntos")
    public ResponseEntity<?> consultarPorId(
            @PathVariable("id") int id) {

        if (id <= 0) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "mensaje",
                            "El ID del cliente debe ser mayor que cero"
                    )
            );
        }

        String sql = """
                SELECT ID_CLIENTE,
                       CODIGO_CLIENTE,
                       PUNTOS,
                       ESTADO
                FROM CLIENTE
                WHERE ID_CLIENTE = ?
                """;

        return consultarCliente(sql, id);
    }

    private ResponseEntity<?> consultarCliente(
            String sql,
            Object parametro) {

        try {
            List<ClienteEncontrado> resultados = jdbcTemplate.query(
                    sql,
                    (rs, fila) -> new ClienteEncontrado(
                            rs.getInt("ID_CLIENTE"),
                            rs.getString("CODIGO_CLIENTE"),
                            rs.getInt("PUNTOS"),
                            rs.getString("ESTADO")
                    ),
                    parametro
            );

            if (resultados.isEmpty()) {
                return ResponseEntity.status(404).body(
                        Map.of("mensaje", "El cliente no existe")
                );
            }

            if (resultados.size() > 1) {
                return ResponseEntity.status(409).body(
                        Map.of(
                                "mensaje",
                                "El código está duplicado. "
                                        + "Contacta al administrador."
                        )
                );
            }

            ClienteEncontrado cliente = resultados.get(0);

            if (!"A".equalsIgnoreCase(cliente.estado())) {
                return ResponseEntity.status(409).body(
                        Map.of("mensaje", "El cliente está inactivo")
                );
            }

            return ResponseEntity.ok(
                    new SaldoCliente(
                            cliente.idCliente(),
                            cliente.codigoCliente(),
                            cliente.puntos()
                    )
            );

        } catch (DataAccessException exception) {
            return ResponseEntity.status(503).body(
                    Map.of(
                            "mensaje",
                            "No se pudo consultar la base de datos"
                    )
            );
        }
    }

    private record ClienteEncontrado(
            int idCliente,
            String codigoCliente,
            int puntos,
            String estado
    ) {
    }

    public record SaldoCliente(
            int idCliente,
            String codigoCliente,
            int puntos
    ) {
    }
}