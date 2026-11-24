package ar.edu.unsam.ddso.tp.dto;

import jakarta.validation.constraints.NotBlank;

public record TagCreateRequestDTO(@NotBlank String name) {}
