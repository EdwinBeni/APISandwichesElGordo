/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sandwicheselgordo.api;

import java.math.BigDecimal;
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
public class PedidoController {

    private final JdbcTemplate jdbcTemplate;

    public PedidoController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/codigo/{codigo}/pedidos")
    public ResponseEntity<?> consultarHistorial(
            @PathVariable("codigo") String codigo) {

        String codigoCliente = codigo.trim()
                .toUpperCase(Locale.ROOT);

        if (codigoCliente.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje", "Ingresa el código del cliente")
            );
        }

        String sqlCliente = """
                SELECT ID_CLIENTE, CODIGO_CLIENTE, ESTADO
                FROM CLIENTE
                WHERE UPPER(TRIM(CODIGO_CLIENTE)) = ?
                """;

        String sqlPedidos = """
                SELECT ID_PEDIDO,
                       TO_CHAR(FECHA, 'YYYY-MM-DD HH24:MI') AS FECHA_TEXTO,
                       ESTADO,
                       TOTAL,
                       PUNTOS_GENERADOS
                FROM PEDIDO
                WHERE ID_CLIENTE = ?
                ORDER BY FECHA DESC, ID_PEDIDO DESC
                """;

        try {
            List<ClienteHistorial> clientes = jdbcTemplate.query(
                    sqlCliente,
                    (rs, fila) -> new ClienteHistorial(
                            rs.getInt("ID_CLIENTE"),
                            rs.getString("CODIGO_CLIENTE"),
                            rs.getString("ESTADO")
                    ),
                    codigoCliente
            );

            if (clientes.isEmpty()) {
                return ResponseEntity.status(404).body(
                        Map.of("mensaje", "El cliente no existe")
                );
            }

            if (clientes.size() > 1) {
                return ResponseEntity.status(409).body(
                        Map.of(
                                "mensaje",
                                "El código está duplicado. "
                                        + "Contacta al administrador."
                        )
                );
            }

            ClienteHistorial cliente = clientes.get(0);

            if (!"A".equalsIgnoreCase(cliente.estado())) {
                return ResponseEntity.status(409).body(
                        Map.of("mensaje", "El cliente está inactivo")
                );
            }

            List<PedidoResumen> pedidos = jdbcTemplate.query(
                    sqlPedidos,
                    (rs, fila) -> new PedidoResumen(
                            rs.getInt("ID_PEDIDO"),
                            rs.getString("FECHA_TEXTO"),
                            rs.getString("ESTADO"),
                            rs.getBigDecimal("TOTAL"),
                            rs.getInt("PUNTOS_GENERADOS")
                    ),
                    cliente.idCliente()
            );

            return ResponseEntity.ok(
                    new HistorialRespuesta(
                            cliente.codigoCliente(),
                            pedidos
                    )
            );

        } catch (DataAccessException exception) {
            return ResponseEntity.status(503).body(
                    Map.of(
                            "mensaje",
                            "No se pudo consultar el historial"
                    )
            );
        }
    }

    private record ClienteHistorial(
            int idCliente,
            String codigoCliente,
            String estado
    ) {
    }

    public record PedidoResumen(
            int idPedido,
            String fecha,
            String estado,
            BigDecimal total,
            int puntosGenerados
    ) {
    }

    public record HistorialRespuesta(
            String codigoCliente,
            List<PedidoResumen> pedidos
    ) {
    }
}