package de.unistuttgart.iste.gits.flashcard_service.persistence.repository;

import de.unistuttgart.iste.gits.flashcard_service.persistence.dao.FlashcardProgressDataLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FlashCardProgressDataLogRepository extends JpaRepository<FlashcardProgressDataLogEntity, UUID> {

    @Query("""
            SELECT log FROM FlashcardProgressDataLog log
            JOIN FETCH log.flashcardProgressData progressData
            WHERE log.learnedAt = (
               SELECT MAX(latestLog.learnedAt)
               FROM FlashcardProgressDataLog latestLog
               WHERE latestLog.flashcardProgressData = log.flashcardProgressData
            )
            """)
    List<FlashcardProgressDataLogEntity> findLatestLogsPerFlashcardProgressData();
}
