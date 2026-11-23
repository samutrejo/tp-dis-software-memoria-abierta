package ar.edu.unsam.ddso.tp.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record PieceCreateRequestDTO(@NotBlank String name, String description, LocalDate date) {}
