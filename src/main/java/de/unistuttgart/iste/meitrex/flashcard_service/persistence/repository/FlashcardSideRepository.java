package de.unistuttgart.iste.gits.flashcard_service.persistence.repository;


import de.unistuttgart.iste.gits.flashcard_service.persistence.entity.FlashcardSideEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface FlashcardSideRepository extends JpaRepository<FlashcardSideEntity, UUID>,
        JpaSpecificationExecutor<FlashcardSideEntity> {
}
