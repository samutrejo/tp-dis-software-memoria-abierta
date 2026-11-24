package ar.edu.unsam.ddso.tp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ar.edu.unsam.ddso.tp.dto.DigitizationCreateRequestDTO;
import ar.edu.unsam.ddso.tp.dto.DigitizationResponseDTO;
import ar.edu.unsam.ddso.tp.exception.NotFoundException;
import ar.edu.unsam.ddso.tp.model.Digitization;
import ar.edu.unsam.ddso.tp.model.Piece;
import ar.edu.unsam.ddso.tp.repository.DigitizationRepository;
import ar.edu.unsam.ddso.tp.repository.PieceRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

    private DigitizationService digitizationService;

    @BeforeEach
    void setUp() {
        digitizationService = new DigitizationService(digitizationRepository, pieceRepository);
    }

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
                                piece));

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
                                        10L, "https://archivo.org/frente.jpg", "Frente", piece),
                                new Digitization(
                                        11L, "https://archivo.org/dorso.jpg", "Dorso", piece)));

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
                new Digitization(10L, "https://archivo.org/frente.jpg", "Frente", piece);
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
}
