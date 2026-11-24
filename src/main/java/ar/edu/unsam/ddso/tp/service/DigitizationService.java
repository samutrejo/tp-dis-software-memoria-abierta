package ar.edu.unsam.ddso.tp.service;

import ar.edu.unsam.ddso.tp.dto.DigitizationCreateRequestDTO;
import ar.edu.unsam.ddso.tp.dto.DigitizationResponseDTO;
import ar.edu.unsam.ddso.tp.exception.NotFoundException;
import ar.edu.unsam.ddso.tp.model.Digitization;
import ar.edu.unsam.ddso.tp.model.Piece;
import ar.edu.unsam.ddso.tp.repository.DigitizationRepository;
import ar.edu.unsam.ddso.tp.repository.PieceRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DigitizationService {

    private final DigitizationRepository digitizationRepository;
    private final PieceRepository pieceRepository;

    public DigitizationService(
            DigitizationRepository digitizationRepository, PieceRepository pieceRepository) {
        this.digitizationRepository = digitizationRepository;
        this.pieceRepository = pieceRepository;
    }

    // ---------- Consultas ----------

    public List<DigitizationResponseDTO> findByPieceId(Long pieceId) {
        getPiece(pieceId);
        return digitizationRepository.findByPieceId(pieceId).stream()
                .map(DigitizationResponseDTO::from)
                .toList();
    }

    public DigitizationResponseDTO findById(Long id) {
        return DigitizationResponseDTO.from(getDigitization(id));
    }

    // ---------- Operaciones ----------

    @Transactional
    public DigitizationResponseDTO create(Long pieceId, DigitizationCreateRequestDTO request) {
        Piece piece = getPiece(pieceId);
        Digitization digitization =
                new Digitization(null, request.url().trim(), request.description(), piece);
        return DigitizationResponseDTO.from(digitizationRepository.save(digitization));
    }

    // ---------- Privados ----------

    private Piece getPiece(Long pieceId) {
        return pieceRepository
                .findById(pieceId)
                .orElseThrow(() -> new NotFoundException("Piece " + pieceId + " not found"));
    }

    private Digitization getDigitization(Long id) {
        return digitizationRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Digitization " + id + " not found"));
    }
}
