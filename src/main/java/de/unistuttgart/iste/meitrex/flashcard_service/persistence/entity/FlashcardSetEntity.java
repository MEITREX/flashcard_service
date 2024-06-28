package de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity;

import de.unistuttgart.iste.gits.common.persistence.IWithId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
