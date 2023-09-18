package de.unistuttgart.iste.gits.flashcard_service.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.*;

@Entity(name = "FlashcardSet")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardSetEntity {
    @Id
    private UUID assessmentId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "parentSet")
    private List<FlashcardEntity> flashcards;

    @Column(nullable = true)
    private OffsetDateTime lastLearned;

    public Optional<OffsetDateTime> getLastLearned() {
        return Optional.ofNullable(lastLearned);
    }
}
