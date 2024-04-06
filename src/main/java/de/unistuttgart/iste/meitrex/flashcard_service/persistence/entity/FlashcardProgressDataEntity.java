package de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity(name = "FlashcardProgressData")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardProgressDataEntity {

    @EmbeddedId
    private PrimaryKey primaryKey;

    @Column
    private OffsetDateTime lastLearned;

    @Column
    private int learningInterval;

    @Column
    private OffsetDateTime nextLearn;

    @OneToMany(mappedBy = "flashcardProgressData", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FlashcardProgressDataLogEntity> flashcardProgressDataLogs;

    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrimaryKey implements Serializable {
        private UUID flashcardID;
        private UUID userId;

    }
}
