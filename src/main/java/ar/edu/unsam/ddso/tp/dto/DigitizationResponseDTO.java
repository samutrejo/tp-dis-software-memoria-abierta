package ar.edu.unsam.ddso.tp.dto;

import ar.edu.unsam.ddso.tp.model.Digitization;
import java.util.List;

public record DigitizationResponseDTO(
        Long id,
        Long pieceId,
        String url,
        String description,
        List<TagSummaryDTO> tags) {

    public record TagSummaryDTO(Long id, String name) {}

    public static DigitizationResponseDTO from(Digitization digitization) {
        List<TagSummaryDTO> tagDTOs = digitization.getTags().stream()
                .map(t -> new TagSummaryDTO(t.getId(), t.getName()))
                .toList();

        return new DigitizationResponseDTO(
                digitization.getId(),
                digitization.getPiece().getId(),
                digitization.getUrl(),
                digitization.getDescription(),
                tagDTOs);
    }
}
