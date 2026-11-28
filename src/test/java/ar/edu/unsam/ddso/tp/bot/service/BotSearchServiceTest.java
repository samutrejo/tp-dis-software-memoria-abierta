
package ar.edu.unsam.ddso.tp.bot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.unsam.ddso.tp.bot.port.PieceSearchPort;
import ar.edu.unsam.ddso.tp.dto.PieceResponseDTO;

@ExtendWith(MockitoExtension.class)
class BotSearchServiceTest {

    @Mock private PieceSearchPort pieceSearchPort;

    private BotSearchService botSearchService;

    @BeforeEach
    void setUp() {
        botSearchService = new BotSearchService(pieceSearchPort);
    }

    @Test
    void searchReturnsMessageWhenNoPiecesAreFound() {
        // Given
        when(pieceSearchPort.search(null, null, null, List.of()))
                .thenReturn(List.of());

        // When
        String response = botSearchService.search(null, null, null, List.of());

        // Then
        assertEquals(
                "No se encontraron piezas con esos criterios de búsqueda.",
                response);
        verify(pieceSearchPort).search(null, null, null, List.of());
    }

    @Test
    void searchReturnsFormattedResultsWhenPiecesAreFound() {
        // Given
        LocalDate date = LocalDate.of(1978, 6, 25);
        PieceResponseDTO piece =
                new PieceResponseDTO(1L, "Carta familiar", "Carta manuscrita", date);

        when(pieceSearchPort.search("carta", null, null, List.of()))
                .thenReturn(List.of(piece));

        // When
        String response =
                botSearchService.search("carta", null, null, List.of());

        // Then
        assertTrue(response.contains("Se encontraron 1 pieza(s):"));
        assertTrue(response.contains("Carta familiar"));
        assertTrue(response.contains("ID: 1"));
        assertTrue(response.contains("Descripción: Carta manuscrita"));
        assertTrue(response.contains("Fecha: 1978-06-25"));
        verify(pieceSearchPort).search("carta", null, null, List.of());
    }

    @Test
    void searchShowsFallbackTextWhenPieceHasNoDescriptionOrDate() {
        // Given
        PieceResponseDTO piece =
                new PieceResponseDTO(2L, "Documento", null, null);

        when(pieceSearchPort.search(null, null, null, List.of()))
                .thenReturn(List.of(piece));

        // When
        String response = botSearchService.search(null, null, null, List.of());

        // Then
        assertTrue(response.contains("Descripción: Sin descripción"));
        assertTrue(response.contains("Fecha: Sin fecha"));
    }
}