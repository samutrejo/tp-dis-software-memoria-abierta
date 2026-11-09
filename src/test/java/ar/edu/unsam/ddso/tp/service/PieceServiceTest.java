package ar.edu.unsam.ddso.tp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ar.edu.unsam.ddso.tp.dto.PieceCreateRequestDTO;
import ar.edu.unsam.ddso.tp.dto.PieceResponseDTO;
import ar.edu.unsam.ddso.tp.exception.NotFoundException;
import ar.edu.unsam.ddso.tp.model.Piece;
import ar.edu.unsam.ddso.tp.repository.PieceRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PieceServiceTest {

    @Mock private PieceRepository pieceRepository;

    private PieceService pieceService;

    @BeforeEach
    void setUp() {
        pieceService = new PieceService(pieceRepository);
    }

    @Test
    void createSavesThePieceAndReturnsItWithId() {
        // Given
        LocalDate date = LocalDate.of(1978, 6, 25);
        PieceCreateRequestDTO request = new PieceCreateRequestDTO("Letter", "Handwritten", date);
        when(pieceRepository.save(any(Piece.class)))
                .thenReturn(new Piece(1L, "Letter", "Handwritten", date));

        // When
        PieceResponseDTO response = pieceService.create(request);

        // Then
        assertEquals(1L, response.id());
        assertEquals("Letter", response.name());
        assertEquals("Handwritten", response.description());
        assertEquals(date, response.date());
        verify(pieceRepository).save(any(Piece.class));
    }

    @Test
    void findByIdThrowsWhenThePieceDoesNotExist() {
        // Given
        when(pieceRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(NotFoundException.class, () -> pieceService.findById(99L));
    }
}
