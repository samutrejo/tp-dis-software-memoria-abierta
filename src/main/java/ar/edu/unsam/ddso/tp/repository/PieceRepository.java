
package ar.edu.unsam.ddso.tp.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ar.edu.unsam.ddso.tp.model.Piece;

public interface PieceRepository extends JpaRepository<Piece, Long> {

    @Query("""
        SELECT p FROM Piece p
        WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
        AND (:description IS NULL OR LOWER(p.description) LIKE LOWER(CONCAT('%', :description, '%')))
        AND (:date IS NULL OR p.date = :date)
        AND (
            :tag IS NULL OR EXISTS (
                SELECT d.id FROM Digitization d
                JOIN d.tags t
                WHERE d.piece = p
                AND LOWER(t.name) = LOWER(:tag)
            )
        )
        """)
    List<Piece> search(
            @Param("name") String name,
            @Param("description") String description,
            @Param("date") LocalDate date,
            @Param("tag") String tag
    );
}