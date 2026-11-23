package ar.edu.unsam.ddso.tp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Physical item of the archive (a photo, a letter, a document). It is what users search for. */
@Entity
@Table(name = "pieces")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Piece {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    // "date" is a reserved word in SQL, so the column gets another name.
    @Column(name = "piece_date")
    private LocalDate date;
}
