package ar.edu.unsam.ddso.tp.repository;

import ar.edu.unsam.ddso.tp.model.Digitization;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DigitizationRepository extends JpaRepository<Digitization, Long> {

    List<Digitization> findByPieceId(Long pieceId);

    List<Digitization> findByTagsId(Long tagId);
}
