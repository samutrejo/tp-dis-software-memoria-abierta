package ar.edu.unsam.ddso.tp.dto;

import ar.edu.unsam.ddso.tp.model.Piece;
import java.time.LocalDate;

public record PieceResponseDTO(Long id, String name, String description, LocalDate date) {

    public static PieceResponseDTO from(Piece piece) {
        return new PieceResponseDTO(
                piece.getId(), piece.getName(), piece.getDescription(), piece.getDate());
    }
}
