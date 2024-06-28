package de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity(name = "FlashcardSide")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardSideEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(nullable = false, length = 255)
    private String label;

    @Column(nullable = false)
    private boolean isQuestion;

    @Column(nullable = false)
    private boolean isAnswer;

    @ManyToOne
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private FlashcardEntity flashcard;
}
