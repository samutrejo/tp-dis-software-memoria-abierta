package ar.edu.unsam.ddso.tp.bot.port;

import java.time.LocalDate;
import java.util.List;

import ar.edu.unsam.ddso.tp.dto.PieceResponseDTO;

public interface PieceSearchPort {

    List<PieceResponseDTO> search(
            String name,
            String description,
            LocalDate date,
            List<String> tags);
}
