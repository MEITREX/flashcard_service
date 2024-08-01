package de.unistuttgart.iste.meitrex.flashcard_service.persistence.repository;

import de.unistuttgart.iste.meitrex.common.persistence.MeitrexRepository;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity.FlashcardSetEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FlashcardSetRepository extends MeitrexRepository<FlashcardSetEntity, UUID>,
        JpaSpecificationExecutor<FlashcardSetEntity> {

    /**
     * Find all flashcard sets by course id.
     *
     * @param courseId the course id
     * @return the list of flashcard sets
     */
    List<FlashcardSetEntity> findAllByCourseId(UUID courseId);

}
