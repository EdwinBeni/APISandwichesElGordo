package com.sandwicheselgordo.api;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recompensas")
public class RecompensaController {

    private final JdbcTemplate jdbcTemplate;

    public RecompensaController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<?> listarRecompensas() {
        String sql = """
                SELECT ID_RECOMPENSA,
                       CODIGO,
                       NOMBRE,
                       PUNTOS_NECESARIOS
                FROM RECOMPENSA
                WHERE ESTADO = 'A'
                ORDER BY PUNTOS_NECESARIOS, NOMBRE
                """;

        try {
            List<RecompensaResumen> recompensas =
                    jdbcTemplate.query(
                            sql,
                            (rs, fila) -> new RecompensaResumen(
                                    rs.getInt("ID_RECOMPENSA"),
                                    rs.getString("CODIGO"),
                                    rs.getString("NOMBRE"),
                                    rs.getInt("PUNTOS_NECESARIOS")
                            )
                    );

            return ResponseEntity.ok(recompensas);

        } catch (DataAccessException exception) {
            return ResponseEntity.status(503).body(
                    Map.of(
                            "mensaje",
                            "No se pudieron consultar las recompensas"
                    )
            );
        }
    }

    public record RecompensaResumen(
            int idRecompensa,
            String codigo,
            String nombre,
            int puntosNecesarios
    ) {
    }
}