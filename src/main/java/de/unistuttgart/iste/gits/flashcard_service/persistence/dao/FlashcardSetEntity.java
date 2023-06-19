package de.unistuttgart.iste.gits.flashcard_service.persistence.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity(name = "FlashcardSet")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardSetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID assessmentId;

    @OneToMany(mappedBy = "flashcardSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FlashcardEntity> flashcards;

}
