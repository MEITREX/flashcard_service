package de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity;

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
    private UUID itemId;

    @OneToMany(mappedBy = "flashcard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FlashcardSideEntity> sides;

    @ManyToOne
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FlashcardSetEntity parentSet;

}


