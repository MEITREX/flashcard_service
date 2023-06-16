package de.unistuttgart.iste.gits.flashcardservice.persistence.repository;

import de.unistuttgart.iste.gits.flashcardservice.persistence.dao.FlashcardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;



/**
 * Repository for {@link FlashcardEntity}.
 */
@Repository
public interface FlashcardRepository extends JpaRepository<FlashcardEntity, Long>, JpaSpecificationExecutor<FlashcardEntity> {
    @Query("select flashcard from Flashcard flashcard where flashcard.id in (:ids)")
    List<FlashcardEntity> findById(List<Long> ids);
}
