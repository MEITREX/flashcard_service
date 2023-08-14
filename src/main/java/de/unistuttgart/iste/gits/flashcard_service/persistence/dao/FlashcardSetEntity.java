package de.unistuttgart.iste.gits.flashcard_service.persistence.dao;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
    private UUID assessmentId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "parentSet")
    private List<FlashcardEntity> flashcards;
}
