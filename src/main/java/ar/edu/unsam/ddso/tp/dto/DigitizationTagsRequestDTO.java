package ar.edu.unsam.ddso.tp.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record DigitizationTagsRequestDTO(@NotNull List<@NotNull Long> tagIds) {}