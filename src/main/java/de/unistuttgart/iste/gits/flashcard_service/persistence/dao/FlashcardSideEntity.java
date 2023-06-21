package de.unistuttgart.iste.gits.flashcard_service.persistence.dao;

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

    @Column(nullable = false, length = 3000)
    private String text;

    @Column(nullable = false, length = 255)
    private String label;

    @Column(nullable = false)
    private boolean isQuestion;

    @ManyToOne
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private FlashcardEntity flashcard;




}
