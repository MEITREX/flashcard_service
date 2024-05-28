package de.unistuttgart.iste.meitrex.flashcard_service.service;

import de.unistuttgart.iste.meitrex.common.dapr.TopicPublisher;
import de.unistuttgart.iste.meitrex.common.event.ContentProgressedEvent;
import de.unistuttgart.iste.meitrex.common.event.Response;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity.*;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.mapper.FlashcardMapper;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.repository.*;
import de.unistuttgart.iste.meitrex.generated.dto.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FlashcardUserProgressDataService {

    private final FlashcardProgressDataRepository flashcardProgressDataRepository;
    private final FlashcardProgressDataLogRepository flashCardProgressDataLogRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardSetRepository flashcardSetRepository;
    private final ModelMapper modelMapper;
    private final FlashcardMapper flashcardMapper;
    private final TopicPublisher topicPublisher;

    /**
     * Get the progress data for a flashcard
     *
     * @param flashcardId the id of the flashcard
     * @param userId      the id of the user
     * @return the progress data
     */
    public FlashcardProgressData getProgressData(final UUID flashcardId, final UUID userId) {
        final var entity = getProgressDataEntity(flashcardId, userId);
        return mapProgressDataEntityToDto(entity);
    }

    /**
     * Get the progress data for a flashcard set or initialize it if it does not exist yet.
     *
     * @param flashcardId the id of the flashcard
     * @param userId      the id of the user
     * @return the progress data
     */
    private FlashcardProgressDataEntity getProgressDataEntity(final UUID flashcardId, final UUID userId) {
        final var primaryKey = new FlashcardProgressDataEntity.PrimaryKey(flashcardId, userId);
        return flashcardProgressDataRepository.findById(primaryKey)
                .orElseGet(() -> initializeProgressData(flashcardId, userId));
    }

    private FlashcardProgressDataEntity initializeProgressData(final UUID flashcardId, final UUID userId) {
        final var primaryKey = new FlashcardProgressDataEntity.PrimaryKey(flashcardId, userId);
        final var progressData = FlashcardProgressDataEntity.builder()
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
     * @param userId      the id of the user
     * @param successful  whether the flashcard was learned successfully
     * @return the feedback for the flashcard
     */
    public FlashcardLearnedFeedback logFlashcardLearned(final UUID flashcardId, final UUID userId, final boolean successful) {
        final var progressData = getProgressDataEntity(flashcardId, userId);
        final var logData = new FlashcardProgressDataLogEntity();
        logData.setSuccess(successful);
        logData.setFlashcardProgressData(progressData);
        logData.setLearnedAt(OffsetDateTime.now());
        if (progressData.getFlashcardProgressDataLogs() == null) {
            progressData.setFlashcardProgressDataLogs(new ArrayList<>());
        }
        progressData.getFlashcardProgressDataLogs().add(logData);

        updateProgressDataEntity(progressData, successful);
        flashcardProgressDataRepository.save(progressData);

        final FlashcardSetEntity flashcardSetEntity = getFlashcardSetForFlashcard(flashcardId);
        publishFlashcardSetLearned(userId, flashcardSetEntity.getAssessmentId(),flashcardId,successful);

        return createFeedback(progressData, successful, flashcardSetEntity, userId);
    }

    /**
     * Get all flashcards of a course that are due to be learned for the given user.
     *
     * @param courseId the id of the course
     * @param userId   the id of the user
     * @return the flashcards
     */
    public List<Flashcard> getAllDueFlashcardsOfCourse(UUID courseId, UUID userId) {
        List<FlashcardSetEntity> flashcardSets = flashcardSetRepository.findAllByCourseId(courseId);

        return flashcardSets.stream()
                .flatMap(flashcardSetEntity -> getDueFlashcardsOfFlashcardSet(flashcardSetEntity, userId))
                .map(flashcardMapper::entityToDto)
                .toList();
    }

    private Stream<FlashcardEntity> getDueFlashcardsOfFlashcardSet(final FlashcardSetEntity flashcardSetEntity,
                                                                   final UUID userId) {
        final var flashcards = flashcardSetEntity.getFlashcards();
        final OffsetDateTime now = OffsetDateTime.now();
        return flashcards.stream()
                .filter(flashcardEntity -> {
                    final var progressData = getProgressDataEntity(flashcardEntity.getItemId(), userId);
                    if (progressData.getNextLearn() == null) {
                        // if the flashcard has never been learned, it's not due for review
                        return false;
                    }
                    return progressData.getNextLearn().isBefore(now);
                });
    }

    private FlashcardLearnedFeedback createFeedback(
            final FlashcardProgressDataEntity progressData,
            final boolean successful,
            final FlashcardSetEntity flashcardSetEntity,
            final UUID userId) {
        final var result = new FlashcardLearnedFeedback();
        result.setSuccess(successful);
        result.setNextLearnDate(progressData.getNextLearn());

        // calculate progress for the whole set and correctness
        final var flashcards = flashcardSetEntity.getFlashcards();
        int numberOfFlashcardsNotLearnedInSet = 0;
        int correctlyLearnedFlashcards = 0;

        for (final FlashcardEntity flashcardEntity : flashcards) {
            final FlashcardProgressDataEntity progressDataOfCurrentCard = getProgressDataEntity(flashcardEntity.getItemId(), userId);
            if (wasNotLearnedInCurrentIteration(flashcardSetEntity, progressDataOfCurrentCard)) {
                numberOfFlashcardsNotLearnedInSet++;
            } else if (wasLearnedSuccessful(progressDataOfCurrentCard)) {
                correctlyLearnedFlashcards++;
            }
        }

        final int flashcardsLearnedInSet = flashcards.size() - numberOfFlashcardsNotLearnedInSet;

        final FlashcardSetProgress flashcardSetProgress = FlashcardSetProgress.builder()
                .setPercentageLearned((double) flashcardsLearnedInSet / flashcards.size())
                .setCorrectness((double) correctlyLearnedFlashcards / flashcardsLearnedInSet)
                .build();
        result.setFlashcardSetProgress(flashcardSetProgress);
        return result;
    }

    private static boolean wasLearnedSuccessful(final FlashcardProgressDataEntity progressData) {
      List<FlashcardProgressDataLogEntity> progress=  progressData.getFlashcardProgressDataLogs();
      boolean success=progress.get(0).getSuccess();
      return success;
    }

    private static boolean wasNotLearnedInCurrentIteration(final FlashcardSetEntity flashcardSetEntity, final FlashcardProgressDataEntity progressData) {
        return progressData.getLastLearned() == null
               // if the flashcard was learned before the last time the set was learned, it was not learned in the current iteration
               || progressData.getNextLearn().isBefore(flashcardSetEntity.getLastLearned().orElse(OffsetDateTime.MIN));
    }

    private void updateProgressDataEntity(final FlashcardProgressDataEntity progressData, final boolean success) {
        final var lastLearn = OffsetDateTime.now();
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


    private FlashcardProgressData mapProgressDataEntityToDto(final FlashcardProgressDataEntity entity) {
        return modelMapper.map(entity, FlashcardProgressData.class);
    }

    private FlashcardProgressDataLog mapLogEntityToDto(final FlashcardProgressDataLogEntity entity) {
        return modelMapper.map(entity, FlashcardProgressDataLog.class);
    }

    private FlashcardSetEntity getFlashcardSetForFlashcard(final UUID flashcardId) {
        final FlashcardEntity flashcardEntity = this.flashcardRepository.getReferenceById(flashcardId);
        return this.flashcardSetRepository.getReferenceById(flashcardEntity.getParentSet().getAssessmentId());
    }

    private void publishFlashcardSetLearned(final UUID userId, final UUID flashcardSetId, final UUID itemId,final boolean successful) {
        final FlashcardSetEntity flashcardSetEntity = flashcardSetRepository.getReferenceById(flashcardSetId);
        final List<FlashcardProgressDataLogEntity> dataLogEntities = flashCardProgressDataLogRepository
                .findLatestLogsPerFlashcardProgressData(userId);

        if (flashcardSetEntity.getLastLearned().isPresent()) {
            // exclude all logs that have been learned before the last time the set was learned
            dataLogEntities.removeIf(log -> log.getLearnedAt().isBefore(flashcardSetEntity.getLastLearned().get()));
        }

        if (dataLogEntities.size() < flashcardSetEntity.getFlashcards().size()) {
            // not all flashcards have been learned yet

            return;
        }
        List<Response>responses=new ArrayList<>();
        for(FlashcardProgressDataLogEntity log:dataLogEntities){
            FlashcardProgressDataEntity progressDataEntity = log.getFlashcardProgressData();
            UUID flashcardID = progressDataEntity.getPrimaryKey().getFlashcardID();
            Response response=new Response(flashcardID,log.getSuccess()?1:0);
            responses.add(response);
        }
        final List<FlashcardProgressDataLog> dataLogs = dataLogEntities
                .stream()
                .map(this::mapLogEntityToDto)
                .toList();
        final int total = dataLogs.size();
        final int correct = dataLogs.stream().mapToInt(log -> log.getSuccess() ? 1 : 0).sum();

        final float correctness = (float) correct / total;

        flashcardSetEntity.setLastLearned(OffsetDateTime.now());
        flashcardSetRepository.save(flashcardSetEntity);
        publishUserProgressEvent(userId, flashcardSetId, correctness,responses);
    }

    private void publishUserProgressEvent(final UUID userId, final UUID assessmentId, final float correctness, List<Response> responses) {
        topicPublisher.notifyUserWorkedOnContent(
                ContentProgressedEvent.builder()
                        .contentId(assessmentId)
                        .userId(userId)
                        .hintsUsed(0)
                        .success(true)
                        .timeToComplete(null)
                        .correctness(correctness)
                        .responses(responses)
                        .build()
        );
    }
}
