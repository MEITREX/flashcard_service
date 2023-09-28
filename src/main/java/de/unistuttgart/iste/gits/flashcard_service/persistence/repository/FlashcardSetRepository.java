package de.unistuttgart.iste.gits.flashcard_service.persistence.repository;

import de.unistuttgart.iste.gits.flashcard_service.persistence.entity.FlashcardSetEntity;
import de.unistuttgart.iste.gits.generated.dto.Flashcard;
import de.unistuttgart.iste.gits.generated.dto.FlashcardSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FlashcardSetRepository extends JpaRepository<FlashcardSetEntity, UUID>,
        JpaSpecificationExecutor<FlashcardSetEntity> {

    List<FlashcardSet> getFlashcardSetEntityByFlashcardsContains(Flashcard flashcard);
}
