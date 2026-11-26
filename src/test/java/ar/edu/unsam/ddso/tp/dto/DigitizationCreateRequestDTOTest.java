package ar.edu.unsam.ddso.tp.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DigitizationCreateRequestDTOTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void validUrlPassesValidation() {
        // Given
        DigitizationCreateRequestDTO request =
                new DigitizationCreateRequestDTO(
                        "https://archivo.org/carta-frente.jpg", "Escaneo frente");

        // When
        Set<ConstraintViolation<DigitizationCreateRequestDTO>> violations =
                validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankUrlFailsValidation() {
        // Given
        DigitizationCreateRequestDTO request =
                new DigitizationCreateRequestDTO("   ", "Escaneo frente");

        // When
        Set<ConstraintViolation<DigitizationCreateRequestDTO>> violations =
                validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
    }

    @Test
    void invalidUrlFormatFailsValidation() {
        // Given
        DigitizationCreateRequestDTO request =
                new DigitizationCreateRequestDTO("cualquier-cosa-no-url", "Escaneo frente");

        // When
        Set<ConstraintViolation<DigitizationCreateRequestDTO>> violations =
                validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
    }
}
