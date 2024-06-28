package de.unistuttgart.iste.gits.flashcard_service.persistence.entity;

import de.unistuttgart.iste.gits.common.persistence.IWithId;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.*;

@Entity(name = "FlashcardSet")
@Table(indexes = {
        @Index(name = "idx_flashcard_set_course_id", columnList = "course_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardSetEntity implements IWithId<UUID> {
    @Id
    private UUID assessmentId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "parentSet")
    private List<FlashcardEntity> flashcards;

    @Column(nullable = true)
    private OffsetDateTime lastLearned;

    public Optional<OffsetDateTime> getLastLearned() {
        return Optional.ofNullable(lastLearned);
    }

    @Override
    public UUID getId() {
        return assessmentId;
    }
}
