package ar.edu.unsam.ddso.tp.service;

import ar.edu.unsam.ddso.tp.dto.PieceCreateRequestDTO;
import ar.edu.unsam.ddso.tp.dto.PieceResponseDTO;
import ar.edu.unsam.ddso.tp.exception.NotFoundException;
import ar.edu.unsam.ddso.tp.model.Piece;
import ar.edu.unsam.ddso.tp.repository.PieceRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PieceService {

    private final PieceRepository pieceRepository;

    public PieceService(PieceRepository pieceRepository) {
        this.pieceRepository = pieceRepository;
    }

    // ---------- Consultas ----------

    public List<PieceResponseDTO> findAll() {
        return pieceRepository.findAll().stream().map(PieceResponseDTO::from).toList();
    }

    public PieceResponseDTO findById(Long id) {
        return PieceResponseDTO.from(getPiece(id));
    }

    // ---------- Operaciones ----------

    @Transactional
    public PieceResponseDTO create(PieceCreateRequestDTO request) {
        Piece piece = new Piece(null, request.name(), request.description(), request.date());
        return PieceResponseDTO.from(pieceRepository.save(piece));
    }

    // ---------- Privados ----------

    private Piece getPiece(Long id) {
        return pieceRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Piece " + id + " not found"));
    }
}
