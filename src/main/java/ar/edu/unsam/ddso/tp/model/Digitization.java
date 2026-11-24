package ar.edu.unsam.ddso.tp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Digital resource (URL and description) associated with a physical {@link Piece} of the archive.
 */
@Entity
@Table(name = "digitizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Digitization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "piece_id", nullable = false)
    private Piece piece;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "digitization_tags",
            joinColumns = @JoinColumn(name = "digitization_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags = new ArrayList<>();

    public Digitization(Long id, String url, String description, Piece piece) {
        this.id = id;
        this.url = url;
        this.description = description;
        this.piece = piece;
        this.tags = new ArrayList<>();
    }
}
