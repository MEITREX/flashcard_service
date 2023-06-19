package de.unistuttgart.iste.gits.flashcard_service.persistence.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity(name = "Flashcard")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToMany(mappedBy = "flashcard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FlashcardSideEntity> sides;

    @ManyToOne
    private FlashcardSetEntity flashcardSet;

}


