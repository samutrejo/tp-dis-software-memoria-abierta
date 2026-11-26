package ar.edu.unsam.ddso.tp.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record DigitizationCreateRequestDTO(@NotBlank @URL String url, String description) {}
