package de.unistuttgart.iste.meitrex.flashcard_service.service;

import de.unistuttgart.iste.meitrex.common.event.AssessmentContentMutatedEvent;
import de.unistuttgart.iste.meitrex.common.event.AssessmentType;
import de.unistuttgart.iste.meitrex.common.event.ContentChangeEvent;
import de.unistuttgart.iste.meitrex.common.event.CrudOperation;
import de.unistuttgart.iste.meitrex.common.exception.IncompleteEventMessageException;

import de.unistuttgart.iste.meitrex.common.dapr.TopicPublisher;

import de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity.FlashcardEntity;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity.FlashcardSetEntity;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity.FlashcardSideEntity;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.mapper.FlashcardMapper;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.repository.FlashcardRepository;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.repository.FlashcardSetRepository;
import de.unistuttgart.iste.meitrex.flashcard_service.validation.FlashcardValidator;
import de.unistuttgart.iste.meitrex.generated.dto.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final FlashcardSetRepository flashcardSetRepository;
    private final FlashcardMapper flashcardMapper;
    private final FlashcardValidator flashcardValidator;

    private final TopicPublisher topicPublisher;

    public Flashcard createFlashcard(final UUID assessmentId, final CreateFlashcardInput flashcardInput) {
        flashcardValidator.validateCreateFlashcardInput(flashcardInput);

        final FlashcardSetEntity set = requireFlashcardSetExisting(assessmentId);

        FlashcardEntity flashcard = flashcardMapper.dtoToEntity(flashcardInput);
        flashcard.setParentSet(set);

        flashcard = flashcardRepository.save(flashcard);

        set.getFlashcards().add(flashcard);

        publishAssessmentContentMutatedEvent(set);

        return flashcardMapper.entityToDto(flashcard);
    }


    public Flashcard updateFlashcard(final UpdateFlashcardInput input) {
        flashcardValidator.validateUpdateFlashcardInput(input);

        final FlashcardEntity oldFlashcard = flashcardRepository.findById(input.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("Flashcard with id %s not found.".formatted(input.getItemId())));

        FlashcardEntity updatedFlashcard = flashcardMapper.dtoToEntity(input);
        updatedFlashcard.setParentSet(oldFlashcard.getParentSet());

        updatedFlashcard = flashcardRepository.save(updatedFlashcard);

        publishAssessmentContentMutatedEvent(updatedFlashcard.getParentSet());

        return flashcardMapper.entityToDto(updatedFlashcard);
    }

    public UUID deleteFlashcard(final UUID assessmentId, final UUID flashcardId) {
        final FlashcardSetEntity set = requireFlashcardSetExisting(assessmentId);
        if (!set.getFlashcards().removeIf(x -> x.getItemId().equals(flashcardId))) {
            throw new EntityNotFoundException("Flashcard with id %s not found.".formatted(flashcardId));
        }
        flashcardSetRepository.save(set);
        publishItemChangeEvent(flashcardId);

        publishAssessmentContentMutatedEvent(set);

        return flashcardId;
    }

    public FlashcardSet createFlashcardSet(final UUID courseId, final UUID assessmentId, final CreateFlashcardSetInput flashcardSetInput) {
        flashcardValidator.validateCreateFlashcardSetInput(flashcardSetInput);

        final FlashcardSetEntity mappedEntity = flashcardMapper.flashcardSetDtoToEntity(flashcardSetInput);
        mappedEntity.setAssessmentId(assessmentId);
        mappedEntity.setCourseId(courseId);
        final FlashcardSetEntity flashcardSetEntity = flashcardSetRepository.save(mappedEntity);

        publishAssessmentContentMutatedEvent(flashcardSetEntity);

        return flashcardMapper.flashcardSetEntityToDto(flashcardSetEntity);
    }

    public UUID deleteFlashcardSet(final UUID assessmentId) {
        requireFlashcardSetExisting(assessmentId);
        publishDeletedFlashcardSet(assessmentId);
        flashcardSetRepository.deleteById(assessmentId);

        return assessmentId;
    }

    public FlashcardSetEntity requireFlashcardSetExisting(final UUID uuid) {
        return flashcardSetRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Flashcard set with id %s not found".formatted(uuid)));
    }

    public List<Flashcard> getFlashcardsByIds(final List<UUID> ids) {
        final List<FlashcardEntity> entities = flashcardRepository.findByItemIdIn(ids);

        ids.removeAll(entities.stream().map(FlashcardEntity::getItemId).toList());
        if (!ids.isEmpty()) {
            throw new EntityNotFoundException("Flashcards with ids "
                    + ids.stream().map(UUID::toString).collect(Collectors.joining(", "))
                    + " not found.");
        }

        return entities.stream()
                .map(flashcardMapper::entityToDto)
                .toList();
    }

    /**
     * Returns the id of the course that the flashcards with the given ids belong to.
     *
     * @param flashcardIds list of flashcard ids
     * @return list of course ids, in the same order as the flashcard ids.
     */
    public List<UUID> getCourseIdsForFlashcardIds(final List<UUID> flashcardIds) {
        return flashcardRepository.findAllById(flashcardIds).stream()
                .map(FlashcardEntity::getParentSet)
                .map(FlashcardSetEntity::getCourseId)
                .toList();
    }

    /**
     * Returns all flashcard sets that are linked to the given assessment ids
     *
     * @param ids list of assessment ids
     * @return list of flashcard sets, an element is null if the corresponding assessment id was not found
     */
    public List<FlashcardSet> findFlashcardSetsByAssessmentIds(final List<UUID> ids) {
        return flashcardSetRepository.findAllByIdPreservingOrder(ids).stream()
                .map(flashcardMapper::flashcardSetEntityToDto)
                .toList();
    }

    /**
     * removes all flashcards when linked Content gets deleted
     *
     * @param dto event object containing changes to content
     */
    public void deleteFlashcardSetIfContentIsDeleted(final ContentChangeEvent dto) throws IncompleteEventMessageException {

        // validate event message
        checkCompletenessOfDto(dto);

        // only consider DELETE Operations
        if (!dto.getOperation().equals(CrudOperation.DELETE) || dto.getContentIds().isEmpty()) {
            return;
        }

        flashcardSetRepository.deleteAllById(dto.getContentIds());
    }

    /**
     * helper function to make sure received event message is complete
     *
     * @param dto event message under evaluation
     * @throws IncompleteEventMessageException if any of the fields are null
     */
    private void checkCompletenessOfDto(final ContentChangeEvent dto) throws IncompleteEventMessageException {
        if (dto.getOperation() == null || dto.getContentIds() == null) {
            throw new IncompleteEventMessageException(IncompleteEventMessageException.ERROR_INCOMPLETE_MESSAGE);
        }
    }

    /***
     * helper function, that creates a ItemChange Event and publish it, when a flashcard was deleted
     * @param itemId the id of the item
     */
    private void publishItemChangeEvent(final UUID itemId) {
        topicPublisher.notifyItemChanges(itemId, CrudOperation.DELETE);

    }

    /**
     * Helper method to raise an AssessmentContentMutatedEvent dapr event for the specified flashcard set.
     * @param flashcardSetEntity The flashcard set for which to raise the event for.
     */
    private void publishAssessmentContentMutatedEvent(final FlashcardSetEntity flashcardSetEntity) {
        topicPublisher.notifyAssessmentContentMutated(new AssessmentContentMutatedEvent(
                flashcardSetEntity.getCourseId(),
                flashcardSetEntity.getAssessmentId(),
                AssessmentType.FLASHCARDS,
                generateTaskInformation(flashcardSetEntity)
        ));
    }

    /**
     * for each flashcard of the deleted flashcard set publish a itemchanged event
     *
     * @param flashcardSetId the id of the flashcardset to delete
     */
    private void publishDeletedFlashcardSet(UUID flashcardSetId) {
        Optional<FlashcardSetEntity> flashcardSetOptional = flashcardSetRepository.findById(flashcardSetId);
        if(flashcardSetOptional.isPresent()){
            FlashcardSetEntity flashcardSet=flashcardSetOptional.get();
            for (FlashcardEntity flashcard : flashcardSet.getFlashcards()) {
                publishItemChangeEvent(flashcard.getItemId());
            }
        }
    }

    /**
     * Helper method to generate TaskInformation objects for a given flashcard set.
     * @param flashcardSet The flashcard set for which to generate the task information.
     * @return List containing the task information.
     */
    private List<AssessmentContentMutatedEvent.TaskInformation> generateTaskInformation(
            final FlashcardSetEntity flashcardSet) {
        final List<AssessmentContentMutatedEvent.TaskInformation> results =
                new ArrayList<>(flashcardSet.getFlashcards().size());

        for(final FlashcardEntity flashcard : flashcardSet.getFlashcards()) {
            final StringBuilder sb = new StringBuilder();

            sb.append("Flashcard\n\n");
            // sort the flashcard sides such that questions appear before answers
            List<FlashcardSideEntity> sortedSides = flashcard.getSides().stream()
                    .sorted(Comparator.comparing(x -> x.isQuestion() ? 0 : 1))
                    .toList();
            for(FlashcardSideEntity side : sortedSides) {
                if(side.isQuestion())
                    sb.append("Question Side:\n");
                else if(side.isAnswer())
                    sb.append("Answer Side:\n");

                sb.append(side.getLabel());
                sb.append(": ");
                sb.append(side.getText());
                sb.append("\n\n");
            }
            results.add(new AssessmentContentMutatedEvent.TaskInformation(
                    flashcard.getItemId(),
                    sb.toString().trim()));
        }

        return results;
    }
}
