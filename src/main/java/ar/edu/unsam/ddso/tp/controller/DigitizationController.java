package ar.edu.unsam.ddso.tp.controller;

import ar.edu.unsam.ddso.tp.dto.DigitizationCreateRequestDTO;
import ar.edu.unsam.ddso.tp.dto.DigitizationResponseDTO;
import ar.edu.unsam.ddso.tp.dto.DigitizationTagsRequestDTO;
import ar.edu.unsam.ddso.tp.service.DigitizationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DigitizationController {

    private final DigitizationService digitizationService;

    public DigitizationController(DigitizationService digitizationService) {
        this.digitizationService = digitizationService;
    }

    @GetMapping("/pieces/{pieceId}/digitizations")
    public List<DigitizationResponseDTO> findByPiece(@PathVariable Long pieceId) {
        return digitizationService.findByPieceId(pieceId);
    }

    @GetMapping("/digitizations/{id}")
    public DigitizationResponseDTO findById(@PathVariable Long id) {
        return digitizationService.findById(id);
    }

    @PostMapping("/pieces/{pieceId}/digitizations")
    @ResponseStatus(HttpStatus.CREATED)
    public DigitizationResponseDTO create(
            @PathVariable Long pieceId,
            @Valid @RequestBody DigitizationCreateRequestDTO request) {
        return digitizationService.create(pieceId, request);
    }

    @PutMapping("/pieces/{pieceId}/digitizations/{digitizationId}/tags")
    public DigitizationResponseDTO replaceTags(
            @PathVariable Long pieceId,
            @PathVariable Long digitizationId,
            @Valid @RequestBody DigitizationTagsRequestDTO request) {
        return digitizationService.replaceTags(pieceId, digitizationId, request);
    }
}
