package ar.edu.unsam.ddso.tp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ar.edu.unsam.ddso.tp.dto.DigitizationCreateRequestDTO;
import ar.edu.unsam.ddso.tp.dto.DigitizationResponseDTO;
import ar.edu.unsam.ddso.tp.dto.DigitizationTagsRequestDTO;
import ar.edu.unsam.ddso.tp.exception.NotFoundException;
import ar.edu.unsam.ddso.tp.model.Digitization;
import ar.edu.unsam.ddso.tp.model.Piece;
import ar.edu.unsam.ddso.tp.model.Tag;
import ar.edu.unsam.ddso.tp.repository.DigitizationRepository;
import ar.edu.unsam.ddso.tp.repository.PieceRepository;
import ar.edu.unsam.ddso.tp.repository.TagRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DigitizationServiceTest {

    @Mock
    private DigitizationRepository digitizationRepository;

    @Mock
    private PieceRepository pieceRepository;

    @Mock
    private TagRepository tagRepository;

    private DigitizationService digitizationService;
    private Validator validator;

    @BeforeEach
    void setUp() {
        digitizationService = new DigitizationService(digitizationRepository, pieceRepository, tagRepository);
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // ---------- Tests DS-14 (existentes) ----------

    @Test
    void createSavesTheDigitizationWhenPieceExists() {
        // Given
        Piece piece = new Piece(1L, "Carta de 1978", "Manuscrita", LocalDate.of(1978, 6, 25));
        DigitizationCreateRequestDTO request =
                new DigitizationCreateRequestDTO(
                        "https://archivo.org/carta-frente.jpg", "Escaneo frente");
        when(pieceRepository.findById(1L)).thenReturn(Optional.of(piece));
        when(digitizationRepository.save(any(Digitization.class)))
                .thenReturn(
                        new Digitization(
                                10L,
                                "https://archivo.org/carta-frente.jpg",
                                "Escaneo frente",
                                piece,
                                List.of()));

        // When
        DigitizationResponseDTO response = digitizationService.create(1L, request);

        // Then
        assertEquals(10L, response.id());
        assertEquals(1L, response.pieceId());
        assertEquals("https://archivo.org/carta-frente.jpg", response.url());
        assertEquals("Escaneo frente", response.description());
        verify(digitizationRepository).save(any(Digitization.class));
    }

    @Test
    void createThrowsWhenPieceDoesNotExist() {
        // Given
        DigitizationCreateRequestDTO request =
                new DigitizationCreateRequestDTO(
                        "https://archivo.org/carta-frente.jpg", "Escaneo frente");
        when(pieceRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(NotFoundException.class, () -> digitizationService.create(99L, request));
        verify(digitizationRepository, never()).save(any(Digitization.class));
    }

    @Test
    void findByPieceIdReturnsDigitizationsWhenPieceExists() {
        // Given
        Piece piece = new Piece(1L, "Carta de 1978", "Manuscrita", LocalDate.of(1978, 6, 25));
        when(pieceRepository.findById(1L)).thenReturn(Optional.of(piece));
        when(digitizationRepository.findByPieceId(1L))
                .thenReturn(
                        List.of(
                                new Digitization(
                                        10L, "https://archivo.org/frente.jpg", "Frente", piece, List.of()),
                                new Digitization(
                                        11L, "https://archivo.org/dorso.jpg", "Dorso", piece, List.of())));

        // When
        List<DigitizationResponseDTO> response = digitizationService.findByPieceId(1L);

        // Then
        assertEquals(2, response.size());
        assertEquals("https://archivo.org/frente.jpg", response.get(0).url());
        assertEquals("https://archivo.org/dorso.jpg", response.get(1).url());
    }

    @Test
    void findByPieceIdThrowsWhenPieceDoesNotExist() {
        // Given
        when(pieceRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(NotFoundException.class, () -> digitizationService.findByPieceId(99L));
        verify(digitizationRepository, never()).findByPieceId(anyLong());
    }

    @Test
    void findByIdReturnsTheDigitizationWhenItExists() {
        // Given
        Piece piece = new Piece(1L, "Carta de 1978", "Manuscrita", LocalDate.of(1978, 6, 25));
        Digitization digitization =
                new Digitization(10L, "https://archivo.org/frente.jpg", "Frente", piece, List.of());
        when(digitizationRepository.findById(10L)).thenReturn(Optional.of(digitization));

        // When
        DigitizationResponseDTO response = digitizationService.findById(10L);

        // Then
        assertEquals(10L, response.id());
        assertEquals(1L, response.pieceId());
        assertEquals("https://archivo.org/frente.jpg", response.url());
    }

    @Test
    void findByIdThrowsWhenDigitizationDoesNotExist() {
        // Given
        when(digitizationRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(NotFoundException.class, () -> digitizationService.findById(99L));
    }

    // ---------- Tests DS-17 (nuevos) ----------

    @Test
    void replaceTagsAssociatesMultipleTagsSuccessfully() {
        // Given
        Long pieceId = 1L;
        Long digitizationId = 10L;
        Piece piece = new Piece(pieceId, "Carta", "Desc", LocalDate.of(1978, 6, 25));
        Tag tag1 = new Tag(1L, "dictadura");
        Tag tag2 = new Tag(2L, "fotografia");
        Digitization digitization = new Digitization(digitizationId, "url", "desc", piece, List.of());

        when(digitizationRepository.findById(digitizationId)).thenReturn(Optional.of(digitization));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag1));
        when(tagRepository.findById(2L)).thenReturn(Optional.of(tag2));
        when(digitizationRepository.save(any(Digitization.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        DigitizationTagsRequestDTO request = new DigitizationTagsRequestDTO(List.of(1L, 2L));

        // When
        DigitizationResponseDTO response = digitizationService.replaceTags(pieceId, digitizationId, request);

        // Then
        assertEquals(digitizationId, response.id());
        assertEquals(2, response.tags().size());
        assertEquals("dictadura", response.tags().get(0).name());
        assertEquals("fotografia", response.tags().get(1).name());
        verify(digitizationRepository).save(any(Digitization.class));
    }

    @Test
    void replaceTagsReplacesExistingTags() {
        // Given
        Long pieceId = 1L;
        Long digitizationId = 10L;
        Piece piece = new Piece(pieceId, "Carta", "Desc", LocalDate.of(1978, 6, 25));
        Tag oldTag = new Tag(1L, "dictadura");
        Tag newTag1 = new Tag(2L, "fotografia");
        Tag newTag2 = new Tag(3L, "derechos-humanos");
        Digitization digitization = new Digitization(digitizationId, "url", "desc", piece, List.of(oldTag));

        when(digitizationRepository.findById(digitizationId)).thenReturn(Optional.of(digitization));
        when(tagRepository.findById(2L)).thenReturn(Optional.of(newTag1));
        when(tagRepository.findById(3L)).thenReturn(Optional.of(newTag2));
        when(digitizationRepository.save(any(Digitization.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        DigitizationTagsRequestDTO request = new DigitizationTagsRequestDTO(List.of(2L, 3L));

        // When
        DigitizationResponseDTO response = digitizationService.replaceTags(pieceId, digitizationId, request);

        // Then
        assertEquals(2, response.tags().size());
        assertEquals("fotografia", response.tags().get(0).name());
        assertEquals("derechos-humanos", response.tags().get(1).name());
        verify(digitizationRepository).save(any(Digitization.class));
    }

    @Test
    void replaceTagsWithEmptyListRemovesAllTags() {
        // Given
        Long pieceId = 1L;
        Long digitizationId = 10L;
        Piece piece = new Piece(pieceId, "Carta", "Desc", LocalDate.of(1978, 6, 25));
        Tag oldTag = new Tag(1L, "dictadura");
        Digitization digitization = new Digitization(digitizationId, "url", "desc", piece, List.of(oldTag));

        when(digitizationRepository.findById(digitizationId)).thenReturn(Optional.of(digitization));
        when(digitizationRepository.save(any(Digitization.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        DigitizationTagsRequestDTO request = new DigitizationTagsRequestDTO(List.of());

        // When
        DigitizationResponseDTO response = digitizationService.replaceTags(pieceId, digitizationId, request);

        // Then
        assertTrue(response.tags().isEmpty());
        verify(digitizationRepository).save(any(Digitization.class));
    }

    @Test
    void replaceTagsThrowsWhenTagDoesNotExist() {
        // Given
        Long pieceId = 1L;
        Long digitizationId = 10L;
        Piece piece = new Piece(pieceId, "Carta", "Desc", LocalDate.of(1978, 6, 25));
        Digitization digitization = new Digitization(digitizationId, "url", "desc", piece, List.of());

        when(digitizationRepository.findById(digitizationId)).thenReturn(Optional.of(digitization));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(new Tag(1L, "dictadura")));
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        DigitizationTagsRequestDTO request = new DigitizationTagsRequestDTO(List.of(1L, 99L));

        // When / Then
        assertThrows(NotFoundException.class, () -> digitizationService.replaceTags(pieceId, digitizationId, request));
        verify(digitizationRepository, never()).save(any(Digitization.class));
    }

    @Test
    void replaceTagsThrowsWhenDigitizationDoesNotExist() {
        // Given
        Long pieceId = 1L;
        Long digitizationId = 99L;
        when(digitizationRepository.findById(digitizationId)).thenReturn(Optional.empty());

        DigitizationTagsRequestDTO request = new DigitizationTagsRequestDTO(List.of(1L));

        // When / Then
        assertThrows(NotFoundException.class, () -> digitizationService.replaceTags(pieceId, digitizationId, request));
        verify(digitizationRepository, never()).save(any(Digitization.class));
    }

    @Test
    void replaceTagsThrowsWhenDigitizationBelongsToOtherPiece() {
        // Given
        Long pieceId = 1L;
        Long otherPieceId = 2L;
        Long digitizationId = 10L;
        Piece otherPiece = new Piece(otherPieceId, "Otra pieza", "Desc", LocalDate.of(1978, 6, 25));
        Digitization digitization = new Digitization(digitizationId, "url", "desc", otherPiece, List.of());

        when(digitizationRepository.findById(digitizationId)).thenReturn(Optional.of(digitization));

        DigitizationTagsRequestDTO request = new DigitizationTagsRequestDTO(List.of(1L));

        // When / Then
        assertThrows(NotFoundException.class, () -> digitizationService.replaceTags(pieceId, digitizationId, request));
        verify(digitizationRepository, never()).save(any(Digitization.class));
    }

    // ---------- Tests de validación Jakarta (DS-17) ----------

    @Test
    void dtoValidationRejectsNullElementInTagIds() {
        // Given
        List<Long> tagIdsWithNull = new java.util.ArrayList<>(List.of(1L));
        tagIdsWithNull.add(null);
        DigitizationTagsRequestDTO dto = new DigitizationTagsRequestDTO(tagIdsWithNull);

        // When
        Set<ConstraintViolation<DigitizationTagsRequestDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<DigitizationTagsRequestDTO> violation = violations.iterator().next();
        assertTrue(violation.getPropertyPath().toString().contains("tagIds"));
    }

    @Test
    void dtoValidationRejectsNullTagIds() {
        // Given
        DigitizationTagsRequestDTO dto = new DigitizationTagsRequestDTO(null);

        // When
        Set<ConstraintViolation<DigitizationTagsRequestDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<DigitizationTagsRequestDTO> violation = violations.iterator().next();
        assertEquals("tagIds", violation.getPropertyPath().toString());
    }

    @Test
    void dtoValidationAcceptsValidTagIds() {
        // Given
        DigitizationTagsRequestDTO dto = new DigitizationTagsRequestDTO(List.of(1L, 2L, 3L));

        // When
        Set<ConstraintViolation<DigitizationTagsRequestDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void dtoValidationAcceptsEmptyTagIds() {
        // Given
        DigitizationTagsRequestDTO dto = new DigitizationTagsRequestDTO(List.of());

        // When
        Set<ConstraintViolation<DigitizationTagsRequestDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty());
    }
}
