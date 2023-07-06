package de.unistuttgart.iste.gits.flashcard_service.service;

import de.unistuttgart.iste.gits.common.event.UserProgressLogEvent;
import de.unistuttgart.iste.gits.flashcard_service.dapr.TopicPublisher;
import de.unistuttgart.iste.gits.flashcard_service.persistence.dao.FlashcardEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.dao.FlashcardProgressDataEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.dao.FlashcardProgressDataLogEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.dao.FlashcardSetEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashCardProgressDataLogRepository;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardProgressDataRepository;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardRepository;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardSetRepository;
import de.unistuttgart.iste.gits.generated.dto.Flashcard;
import de.unistuttgart.iste.gits.generated.dto.FlashcardProgressData;
import de.unistuttgart.iste.gits.generated.dto.FlashcardProgressDataLog;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FlashcardUserProgressDataService {

    private final FlashcardProgressDataRepository flashcardProgressDataRepository;
    private final FlashCardProgressDataLogRepository flashCardProgressDataLogRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardSetRepository flashcardSetRepository;
    private final ModelMapper modelMapper;
    private final FlashcardService flashcardService;
    private final TopicPublisher topicPublisher;

    public FlashcardProgressData getProgressData(UUID flashcardId, UUID userId) {
        var entity = getProgressDataEntity(flashcardId, userId);
        return mapProgressDataEntityToDto(entity);
    }

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

    public Flashcard logFlashCardLearned(UUID flashcardId, UUID userId, boolean successful) {
        var flashcard = flashcardService.getFlashCardById(flashcardId);
        var progressData = getProgressDataEntity(flashcardId, userId);
        var logData = new FlashcardProgressDataLogEntity();
        logData.setSuccess(successful);
        logData.setFlashcardProgressData(progressData);
        logData.setLearnedAt(OffsetDateTime.now());

        updateProgressDataEntity(progressData, successful);
        flashCardProgressDataLogRepository.save(logData);

        publishFlashcardSetLearned(userId, getFlashCardSetAssessmentId(flashcardId));

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

    private UUID getFlashCardSetAssessmentId(UUID flashcardId) {
        FlashcardEntity flashcardEntity = this.flashcardRepository.getReferenceById(flashcardId);
        FlashcardSetEntity flashcardSetEntity = this.flashcardSetRepository.getReferenceById(flashcardEntity.getSetId());
        return flashcardSetEntity.getAssessmentId();
    }

    private void publishFlashcardSetLearned(UUID userId, UUID flashcardSetId) {
        List<FlashcardProgressDataLogEntity> dataLogEntities = flashCardProgressDataLogRepository.findLatestLogsPerFlashcardProgressData();
        List<FlashcardProgressDataLog> dataLogs = new ArrayList<>();

        for (var datalogEntity : dataLogEntities) {
            dataLogs.add(mapLogEntityToDto(datalogEntity));
        }

        int total = 0;
        int correct = 0;

        for (var datalog : dataLogs) {
            total++;
            if (datalog.getSuccess()) {
                correct++;
            }
        }

        float correctness = (float) correct / total;

        publishUserProgressEvent(userId, flashcardSetId, correctness);
    }

    private void publishUserProgressEvent(UUID userId, UUID assessmentId, float correctness) {
        topicPublisher.notifyFlashcardSetLearned(
                UserProgressLogEvent.builder()
                        .userId(userId)
                        .contentId(assessmentId)
                        .hintsUsed(0)
                        .success(true)
                        .timeToComplete(null)
                        .correctness(correctness)
                        .build()
        );
    }
}
