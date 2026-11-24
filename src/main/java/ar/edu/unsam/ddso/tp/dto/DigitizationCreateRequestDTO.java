package ar.edu.unsam.ddso.tp.dto;

import jakarta.validation.constraints.NotBlank;

public record DigitizationCreateRequestDTO(@NotBlank String url, String description) {}
