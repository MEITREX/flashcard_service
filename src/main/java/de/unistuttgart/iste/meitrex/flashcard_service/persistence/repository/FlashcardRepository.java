package de.unistuttgart.iste.gits.flashcard_service.persistence.repository;

import de.unistuttgart.iste.gits.flashcard_service.persistence.entity.FlashcardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


/**
 * Repository for {@link FlashcardEntity}.
 */
@Repository
public interface FlashcardRepository extends JpaRepository<FlashcardEntity, UUID>, JpaSpecificationExecutor<FlashcardEntity> {
    List<FlashcardEntity> findByIdIn(List<UUID> ids);


}
