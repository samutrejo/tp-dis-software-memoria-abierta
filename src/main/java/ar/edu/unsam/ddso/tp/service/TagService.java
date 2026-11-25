package ar.edu.unsam.ddso.tp.service;

import ar.edu.unsam.ddso.tp.dto.TagCreateRequestDTO;
import ar.edu.unsam.ddso.tp.dto.TagResponseDTO;
import ar.edu.unsam.ddso.tp.exception.BadRequestException;
import ar.edu.unsam.ddso.tp.exception.NotFoundException;
import ar.edu.unsam.ddso.tp.model.Digitization;
import ar.edu.unsam.ddso.tp.model.Tag;
import ar.edu.unsam.ddso.tp.repository.DigitizationRepository;
import ar.edu.unsam.ddso.tp.repository.TagRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final DigitizationRepository digitizationRepository;

    public TagService(
            TagRepository tagRepository, DigitizationRepository digitizationRepository) {
        this.tagRepository = tagRepository;
        this.digitizationRepository = digitizationRepository;
    }

    // ---------- Consultas ----------

    public List<TagResponseDTO> findAll() {
        return tagRepository.findAll().stream().map(TagResponseDTO::from).toList();
    }

    public TagResponseDTO findById(Long id) {
        return TagResponseDTO.from(getTag(id));
    }

    // ---------- Operaciones ----------

    @Transactional
    public TagResponseDTO create(TagCreateRequestDTO request) {
        String tagName = request.name().trim();
        if (tagRepository.existsByNameIgnoreCase(tagName)) {
            throw new BadRequestException("Tag '" + tagName + "' already exists");
        }
        Tag tag = new Tag(null, tagName);
        return TagResponseDTO.from(tagRepository.save(tag));
    }

    @Transactional
    public void delete(Long id) {
        Tag tag = getTag(id);
        List<Digitization> associatedDigitizations = digitizationRepository.findByTagsId(id);
        for (Digitization digitization : associatedDigitizations) {
            digitization.getTags().removeIf(t -> Objects.equals(t.getId(), id));
        }
        if (!associatedDigitizations.isEmpty()) {
            digitizationRepository.saveAll(associatedDigitizations);
        }
        tagRepository.delete(tag);
    }

    // ---------- Privados ----------

    private Tag getTag(Long id) {
        return tagRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Tag " + id + " not found"));
    }
}
