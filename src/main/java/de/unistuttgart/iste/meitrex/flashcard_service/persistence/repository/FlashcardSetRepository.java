package de.unistuttgart.iste.gits.flashcard_service.persistence.repository;

import de.unistuttgart.iste.gits.common.persistence.GitsRepository;
import de.unistuttgart.iste.gits.flashcard_service.persistence.entity.FlashcardSetEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FlashcardSetRepository extends GitsRepository<FlashcardSetEntity, UUID>,
        JpaSpecificationExecutor<FlashcardSetEntity> {

    /**
     * Find all flashcard sets by course id.
     * @param courseId the course id
     * @return the list of flashcard sets
     */
    List<FlashcardSetEntity> findAllByCourseId(UUID courseId);

}
