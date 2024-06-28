package de.unistuttgart.iste.gits.flashcard_service.persistence.repository;

import de.unistuttgart.iste.gits.flashcard_service.persistence.entity.FlashcardProgressDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlashcardProgressDataRepository extends JpaRepository<FlashcardProgressDataEntity, FlashcardProgressDataEntity.PrimaryKey> {

}
