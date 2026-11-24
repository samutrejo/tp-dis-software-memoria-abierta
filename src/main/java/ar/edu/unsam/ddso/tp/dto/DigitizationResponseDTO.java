package ar.edu.unsam.ddso.tp.dto;

import ar.edu.unsam.ddso.tp.model.Digitization;

public record DigitizationResponseDTO(Long id, Long pieceId, String url, String description) {

    public static DigitizationResponseDTO from(Digitization digitization) {
        return new DigitizationResponseDTO(
                digitization.getId(),
                digitization.getPiece().getId(),
                digitization.getUrl(),
                digitization.getDescription());
    }
}
