
package ar.edu.unsam.ddso.tp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.unsam.ddso.tp.dto.PieceCreateRequestDTO;
import ar.edu.unsam.ddso.tp.dto.PieceResponseDTO;
import ar.edu.unsam.ddso.tp.exception.NotFoundException;
import ar.edu.unsam.ddso.tp.model.Piece;
import ar.edu.unsam.ddso.tp.repository.PieceRepository;

@ExtendWith(MockitoExtension.class)
class PieceServiceTest {

    @Mock
    private PieceRepository pieceRepository;

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

    @Test
    void searchReturnsMatchingPieces() {
        // Given
        LocalDate date = LocalDate.of(1978, 6, 25);
        Piece piece = new Piece(1L, "Letter", "Handwritten", date);
        when(pieceRepository.search("Letter", null, null))
                .thenReturn(List.of(piece));

        // When
        List<PieceResponseDTO> response = pieceService.search("Letter", null, null, null);

        // Then
        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).id());
        assertEquals("Letter", response.get(0).name());
        assertEquals("Handwritten", response.get(0).description());
        assertEquals(date, response.get(0).date());
        verify(pieceRepository).search("Letter", null, null);
        verify(pieceRepository, never()).searchByTags(any(), any(), any(), any());
    }

    @Test
    void searchReturnsEmptyListWhenNoPiecesMatch() {
        // Given
        when(pieceRepository.search("Unknown", null, null))
                .thenReturn(List.of());

        // When
        List<PieceResponseDTO> response = pieceService.search("Unknown", null, null, null);

        // Then
        assertTrue(response.isEmpty());
        verify(pieceRepository).search("Unknown", null, null);
        verify(pieceRepository, never()).searchByTags(any(), any(), any(), any());
    }

    @Test
    void searchByTagsNormalizesTagsAndReturnsMatchingPieces() {
        // Given
        LocalDate date = LocalDate.of(1978, 6, 25);
        Piece piece = new Piece(1L, "Letter", "Handwritten", date);
        List<String> tagsNormalizados = List.of("fotografia", "carta");

        when(pieceRepository.searchByTags(null, null, null, tagsNormalizados))
                .thenReturn(List.of(piece));

        // When
        List<PieceResponseDTO> response = pieceService.search(
                null,
                null,
                null,
                Arrays.asList(" Fotografia ", "carta", "FOTOGRAFIA", "", null));

        // Then
        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).id());
        assertEquals("Letter", response.get(0).name());
        assertEquals(date, response.get(0).date());

        verify(pieceRepository).searchByTags(null, null, null, tagsNormalizados);
        verify(pieceRepository, never()).search(any(), any(), any());
    }
}
