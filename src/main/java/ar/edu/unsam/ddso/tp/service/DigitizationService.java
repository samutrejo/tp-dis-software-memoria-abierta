package ar.edu.unsam.ddso.tp.service;

import ar.edu.unsam.ddso.tp.dto.DigitizationCreateRequestDTO;
import ar.edu.unsam.ddso.tp.dto.DigitizationResponseDTO;
import ar.edu.unsam.ddso.tp.dto.DigitizationTagsRequestDTO;
import ar.edu.unsam.ddso.tp.exception.NotFoundException;
import ar.edu.unsam.ddso.tp.model.Digitization;
import ar.edu.unsam.ddso.tp.model.Piece;
import ar.edu.unsam.ddso.tp.model.Tag;
import ar.edu.unsam.ddso.tp.repository.DigitizationRepository;
import ar.edu.unsam.ddso.tp.repository.PieceRepository;
import ar.edu.unsam.ddso.tp.repository.TagRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DigitizationService {

    private final DigitizationRepository digitizationRepository;
    private final PieceRepository pieceRepository;
    private final TagRepository tagRepository;

    public DigitizationService(
            DigitizationRepository digitizationRepository,
            PieceRepository pieceRepository,
            TagRepository tagRepository) {
        this.digitizationRepository = digitizationRepository;
        this.pieceRepository = pieceRepository;
        this.tagRepository = tagRepository;
    }

    // ---------- Consultas ----------

    @Transactional(readOnly = true)
    public List<DigitizationResponseDTO> findByPieceId(Long pieceId) {
        getPiece(pieceId);
        return digitizationRepository.findByPieceId(pieceId).stream()
                .map(DigitizationResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
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

    @Transactional
    public DigitizationResponseDTO replaceTags(Long pieceId, Long digitizationId, DigitizationTagsRequestDTO request) {
        Digitization digitization = getDigitization(digitizationId);

        // Verificar que la digitalización pertenezca a la pieza indicada
        if (!digitization.getPiece().getId().equals(pieceId)) {
            throw new NotFoundException("Digitization " + digitizationId + " not found for piece " + pieceId);
        }

        List<Long> tagIds = request.tagIds();

        // Validar que todos los tags existan antes de modificar
        List<Tag> tags = tagIds.stream()
                .map(this::getTag)
                .toList();

        // Reemplazar la colección de tags
        digitization.setTags(tags);

        return DigitizationResponseDTO.from(digitizationRepository.save(digitization));
    }

    // ---------- Privados ----------

    private Piece getPiece(Long pieceId) {
        return pieceRepository
                .findById(pieceId)
                .orElseThrow(() -> new NotFoundException("Piece " + pieceId + " not found"));
    }

    private Tag getTag(Long id) {
        return tagRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Tag " + id + " not found"));
    }

    private Digitization getDigitization(Long id) {
        return digitizationRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Digitization " + id + " not found"));
    }
}
