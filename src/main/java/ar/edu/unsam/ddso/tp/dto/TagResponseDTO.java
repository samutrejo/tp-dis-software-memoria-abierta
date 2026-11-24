package ar.edu.unsam.ddso.tp.dto;

import ar.edu.unsam.ddso.tp.model.Tag;

public record TagResponseDTO(Long id, String name) {

    public static TagResponseDTO from(Tag tag) {
        return new TagResponseDTO(tag.getId(), tag.getName());
    }
}
