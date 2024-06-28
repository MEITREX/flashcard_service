package de.unistuttgart.iste.gits.flashcard_service.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity(name = "Flashcard")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToMany(mappedBy = "flashcard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FlashcardSideEntity> sides;

    @ManyToOne
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FlashcardSetEntity parentSet;

}


