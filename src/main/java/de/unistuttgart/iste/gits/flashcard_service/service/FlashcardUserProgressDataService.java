package de.unistuttgart.iste.gits.flashcard_service.service;

import de.unistuttgart.iste.gits.common.event.UserProgressLogEvent;
import de.unistuttgart.iste.gits.flashcard_service.dapr.TopicPublisher;
import de.unistuttgart.iste.gits.flashcard_service.persistence.entity.*;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.*;
import de.unistuttgart.iste.gits.generated.dto.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FlashcardUserProgressDataService {

    private final FlashcardProgressDataRepository flashcardProgressDataRepository;
    private final FlashcardProgressDataLogRepository flashCardProgressDataLogRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardSetRepository flashcardSetRepository;
    private final ModelMapper modelMapper;
    private final FlashcardService flashcardService;
    private final TopicPublisher topicPublisher;

    /**
     * Get the progress data for a flashcard
     *
     * @param flashcardId the id of the flashcard
     * @param userId      the id of the user
     * @return the progress data
     */
    public FlashcardProgressData getProgressData(UUID flashcardId, UUID userId) {
        var entity = getProgressDataEntity(flashcardId, userId);
        return mapProgressDataEntityToDto(entity);
    }

    /**
     * Get the progress data for a flashcard set or initialize it if it does not exist yet.
     *
     * @param flashcardId the id of the flashcard
     * @param userId      the id of the user
     * @return the progress data
     */
    private FlashcardProgressDataEntity getProgressDataEntity(UUID flashcardId, UUID userId) {
        var primaryKey = new FlashcardProgressDataEntity.PrimaryKey(flashcardId, userId);
        return flashcardProgressDataRepository.findById(primaryKey)
                .orElseGet(() -> initializeProgressData(flashcardId, userId));
    }

    private FlashcardProgressDataEntity initializeProgressData(UUID flashcardId, UUID userId) {
        var primaryKey = new FlashcardProgressDataEntity.PrimaryKey(flashcardId, userId);
        var progressData = FlashcardProgressDataEntity.builder()
                .primaryKey(primaryKey)
                .learningInterval(1)
                .lastLearned(null)
                .nextLearn(null)
                .build();
        return flashcardProgressDataRepository.save(progressData);
    }

    /**
     * Logs that a flashcard has been learned.
     * If this was the last flashcard of a set, the set is marked as learned
     * and a UserProgressLogEvent is published.
     * This will also update the learning interval of the flashcard, depending on whether it was learned successfully.
     *
     * @param flashcardId the id of the flashcard
     * @param userId the id of the user
     * @param successful whether the flashcard was learned successfully
     * @return the flashcard
     */
    public Flashcard logFlashcardLearned(UUID flashcardId, UUID userId, boolean successful) {
        var flashcard = flashcardService.getFlashcardById(flashcardId);
        var progressData = getProgressDataEntity(flashcardId, userId);
        var logData = new FlashcardProgressDataLogEntity();
        logData.setSuccess(successful);
        logData.setFlashcardProgressData(progressData);
        logData.setLearnedAt(OffsetDateTime.now());
        if (progressData.getFlashcardProgressDataLogs() == null) {
            progressData.setFlashcardProgressDataLogs(new ArrayList<>());
        }
        progressData.getFlashcardProgressDataLogs().add(logData);

        updateProgressDataEntity(progressData, successful);
        flashcardProgressDataRepository.save(progressData);

        publishFlashcardSetLearned(userId, getFlashcardSetIdForFlashcard(flashcardId));

        return flashcard;
    }

    private void updateProgressDataEntity(FlashcardProgressDataEntity progressData, boolean success) {
        var lastLearn = OffsetDateTime.now();
        progressData.setLastLearned(lastLearn);
        var learningInterval = progressData.getLearningInterval();
        if (success) {
            learningInterval *= 2;
        } else {
            learningInterval /= 2;
        }
        if (learningInterval < 1) {
            learningInterval = 1;
        }
        progressData.setLearningInterval(learningInterval);
        progressData.setNextLearn(lastLearn.plusDays(learningInterval));

        flashcardProgressDataRepository.save(progressData);
    }


    private FlashcardProgressData mapProgressDataEntityToDto(FlashcardProgressDataEntity entity) {
        return modelMapper.map(entity, FlashcardProgressData.class);
    }

    private FlashcardProgressDataLog mapLogEntityToDto(FlashcardProgressDataLogEntity entity) {
        return modelMapper.map(entity, FlashcardProgressDataLog.class);
    }

    private UUID getFlashcardSetIdForFlashcard(UUID flashcardId) {
        FlashcardEntity flashcardEntity = this.flashcardRepository.getReferenceById(flashcardId);
        FlashcardSetEntity flashcardSetEntity = this.flashcardSetRepository.getReferenceById(flashcardEntity.getParentSet().getAssessmentId());
        return flashcardSetEntity.getAssessmentId();
    }

    private void publishFlashcardSetLearned(UUID userId, UUID flashcardSetId) {
        FlashcardSetEntity flashcardSetEntity = flashcardSetRepository.getReferenceById(flashcardSetId);
        List<FlashcardProgressDataLogEntity> dataLogEntities = flashCardProgressDataLogRepository
                .findLatestLogsPerFlashcardProgressData(userId);

        if (flashcardSetEntity.getLastLearned().isPresent()) {
            // exclude all logs that have been learned before the last time the set was learned
            dataLogEntities.removeIf(log -> log.getLearnedAt().isBefore(flashcardSetEntity.getLastLearned().get()));
        }

        if (dataLogEntities.size() < flashcardSetEntity.getFlashcards().size()) {
            // not all flashcards have been learned yet
            return;
        }

        List<FlashcardProgressDataLog> dataLogs = dataLogEntities
                .stream()
                .map(this::mapLogEntityToDto)
                .toList();

        int total = dataLogs.size();
        int correct = dataLogs.stream().mapToInt(log -> log.getSuccess() ? 1 : 0).sum();

        float correctness = (float) correct / total;

        flashcardSetEntity.setLastLearned(OffsetDateTime.now());
        flashcardSetRepository.save(flashcardSetEntity);

        publishUserProgressEvent(userId, flashcardSetId, correctness);
    }

    private void publishUserProgressEvent(UUID userId, UUID assessmentId, float correctness) {
        topicPublisher.notifyFlashcardSetLearned(
                UserProgressLogEvent.builder()
                        .contentId(assessmentId)
                        .userId(userId)
                        .hintsUsed(0)
                        .success(true)
                        .timeToComplete(null)
                        .correctness(correctness)
                        .build()
        );
    }
}
