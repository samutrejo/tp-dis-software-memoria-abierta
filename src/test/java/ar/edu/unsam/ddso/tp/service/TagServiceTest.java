package ar.edu.unsam.ddso.tp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ar.edu.unsam.ddso.tp.dto.TagCreateRequestDTO;
import ar.edu.unsam.ddso.tp.dto.TagResponseDTO;
import ar.edu.unsam.ddso.tp.exception.BadRequestException;
import ar.edu.unsam.ddso.tp.exception.NotFoundException;
import ar.edu.unsam.ddso.tp.model.Digitization;
import ar.edu.unsam.ddso.tp.model.Piece;
import ar.edu.unsam.ddso.tp.model.Tag;
import ar.edu.unsam.ddso.tp.repository.DigitizationRepository;
import ar.edu.unsam.ddso.tp.repository.TagRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private DigitizationRepository digitizationRepository;

    private TagService tagService;

    @BeforeEach
    void setUp() {
        tagService = new TagService(tagRepository, digitizationRepository);
    }

    @Test
    void createSavesTheTagAndReturnsItWithId() {
        // Given
        TagCreateRequestDTO request = new TagCreateRequestDTO("dictadura");
        when(tagRepository.existsByNameIgnoreCase("dictadura")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(new Tag(1L, "dictadura"));

        // When
        TagResponseDTO response = tagService.create(request);

        // Then
        assertEquals(1L, response.id());
        assertEquals("dictadura", response.name());
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    void createThrowsWhenTagAlreadyExists() {
        // Given
        TagCreateRequestDTO request = new TagCreateRequestDTO("dictadura");
        when(tagRepository.existsByNameIgnoreCase("dictadura")).thenReturn(true);

        // When / Then
        assertThrows(BadRequestException.class, () -> tagService.create(request));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void findByIdReturnsTheTagWhenItExists() {
        // Given
        when(tagRepository.findById(1L)).thenReturn(Optional.of(new Tag(1L, "dictadura")));

        // When
        TagResponseDTO response = tagService.findById(1L);

        // Then
        assertEquals(1L, response.id());
        assertEquals("dictadura", response.name());
    }

    @Test
    void findByIdThrowsWhenTheTagDoesNotExist() {
        // Given
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(NotFoundException.class, () -> tagService.findById(99L));
    }

    @Test
    void findAllReturnsAllTags() {
        // Given
        when(tagRepository.findAll()).thenReturn(List.of(new Tag(1L, "dictadura"), new Tag(2L, "fotografia")));

        // When
        List<TagResponseDTO> response = tagService.findAll();

        // Then
        assertEquals(2, response.size());
        assertEquals("dictadura", response.get(0).name());
        assertEquals("fotografia", response.get(1).name());
    }

    @Test
    void deleteRemovesTheTagWhenItExists() {
        // Given
        Tag tag = new Tag(1L, "dictadura");
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(digitizationRepository.findByTagsId(1L)).thenReturn(List.of());

        // When
        tagService.delete(1L);

        // Then
        verify(digitizationRepository, never()).saveAll(any());
        verify(tagRepository).delete(tag);
    }

    @Test
    void deleteUnlinksAssociatedDigitizationsBeforeRemovingTag() {
        // Given
        Tag tagToDelete = new Tag(1L, "dictadura");
        Tag otherTag = new Tag(2L, "fotografia");
        Piece piece = new Piece(10L, "Carta", "Desc", LocalDate.of(1978, 6, 25));
        Digitization digitization =
                new Digitization(
                        5L,
                        "https://archivo.org/carta.jpg",
                        "Frente",
                        piece,
                        new ArrayList<>(List.of(tagToDelete, otherTag)));

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tagToDelete));
        when(digitizationRepository.findByTagsId(1L)).thenReturn(List.of(digitization));

        // When
        tagService.delete(1L);

        // Then
        assertEquals(1, digitization.getTags().size());
        assertEquals(2L, digitization.getTags().get(0).getId());
        verify(digitizationRepository).saveAll(List.of(digitization));
        verify(tagRepository).delete(tagToDelete);
    }

    @Test
    void deleteThrowsWhenTheTagDoesNotExist() {
        // Given
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(NotFoundException.class, () -> tagService.delete(99L));
        verify(digitizationRepository, never()).findByTagsId(anyLong());
        verify(tagRepository, never()).delete(any(Tag.class));
    }
}
