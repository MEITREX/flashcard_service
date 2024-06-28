package de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity(name = "FlashcardProgressDataLog")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardProgressDataLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private boolean success;

    @OrderColumn(nullable = false)
    private OffsetDateTime learnedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "flashcard_id", referencedColumnName = "flashcardID", nullable = false),
            @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false)
    })
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FlashcardProgressDataEntity flashcardProgressData;
}
