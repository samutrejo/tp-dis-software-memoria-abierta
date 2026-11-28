package ar.edu.unsam.ddso.tp.bot.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import ar.edu.unsam.ddso.tp.bot.port.PieceSearchPort;
import ar.edu.unsam.ddso.tp.dto.PieceResponseDTO;

@Service
public class BotSearchService {

    private final PieceSearchPort pieceSearchPort;

    public BotSearchService(PieceSearchPort pieceSearchPort) {
        this.pieceSearchPort = pieceSearchPort;
    }

    public String search(
            String name,
            String description,
            LocalDate date,
            List<String> tags) {

        List<PieceResponseDTO> pieces =
                pieceSearchPort.search(name, description, date, tags);

        if (pieces.isEmpty()) {
            return "No se encontraron piezas con esos criterios de búsqueda.";
        }

        StringBuilder response = new StringBuilder();
        response.append("Se encontraron ")
                .append(pieces.size())
                .append(" pieza(s):\n\n");

        for (PieceResponseDTO piece : pieces) {
            response.append("• ")
                    .append(piece.name())
                    .append(" (ID: ")
                    .append(piece.id())
                    .append(")\n");

            response.append("  Descripción: ")
                    .append(piece.description() == null
                            ? "Sin descripción"
                            : piece.description())
                    .append("\n");

            response.append("  Fecha: ")
                    .append(piece.date() == null
                            ? "Sin fecha"
                            : piece.date())
                    .append("\n\n");
        }

        return response.toString();
    }
}
