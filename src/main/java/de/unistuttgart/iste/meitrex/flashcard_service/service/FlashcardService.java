package de.unistuttgart.iste.meitrex.flashcard_service.service;

import de.unistuttgart.iste.gits.common.event.ContentChangeEvent;
import de.unistuttgart.iste.gits.common.event.CrudOperation;
import de.unistuttgart.iste.gits.common.exception.IncompleteEventMessageException;
import de.unistuttgart.iste.gits.generated.dto.*;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity.FlashcardEntity;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity.FlashcardSetEntity;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.mapper.FlashcardMapper;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.repository.FlashcardRepository;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.repository.FlashcardSetRepository;
import de.unistuttgart.iste.meitrex.flashcard_service.validation.FlashcardValidator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
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

    public Flashcard createFlashcard(final UUID assessmentId, final CreateFlashcardInput flashcardInput) {
        flashcardValidator.validateCreateFlashcardInput(flashcardInput);

        final FlashcardSetEntity set = requireFlashcardSetExisting(assessmentId);

        FlashcardEntity flashcard = flashcardMapper.dtoToEntity(flashcardInput);
        flashcard.setParentSet(set);

        flashcard = flashcardRepository.save(flashcard);

        set.getFlashcards().add(flashcard);

        return flashcardMapper.entityToDto(flashcard);
    }


    public Flashcard updateFlashcard(final UpdateFlashcardInput input) {
        flashcardValidator.validateUpdateFlashcardInput(input);

        final FlashcardEntity oldFlashcard = flashcardRepository.findById(input.getId())
                .orElseThrow(() -> new EntityNotFoundException("Flashcard with id %s not found.".formatted(input.getId())));

        FlashcardEntity updatedFlashcard = flashcardMapper.dtoToEntity(input);
        updatedFlashcard.setParentSet(oldFlashcard.getParentSet());

        updatedFlashcard = flashcardRepository.save(updatedFlashcard);

        return flashcardMapper.entityToDto(updatedFlashcard);
    }

    public UUID deleteFlashcard(final UUID assessmentId, final UUID flashcardId) {
        final FlashcardSetEntity set = requireFlashcardSetExisting(assessmentId);
        if (!set.getFlashcards().removeIf(x -> x.getId().equals(flashcardId))) {
            throw new EntityNotFoundException("Flashcard with id %s not found.".formatted(flashcardId));
        }
        flashcardSetRepository.save(set);
        return flashcardId;
    }

    public FlashcardSet createFlashcardSet(final UUID courseId, final UUID assessmentId, final CreateFlashcardSetInput flashcardSetInput) {
        flashcardValidator.validateCreateFlashcardSetInput(flashcardSetInput);

        final FlashcardSetEntity mappedEntity = flashcardMapper.flashcardSetDtoToEntity(flashcardSetInput);
        mappedEntity.setAssessmentId(assessmentId);
        mappedEntity.setCourseId(courseId);
        final FlashcardSetEntity flashcardSetEntity = flashcardSetRepository.save(mappedEntity);
        return flashcardMapper.flashcardSetEntityToDto(flashcardSetEntity);
    }

    public UUID deleteFlashcardSet(final UUID uuid) {
        requireFlashcardSetExisting(uuid);
        flashcardSetRepository.deleteById(uuid);
        return uuid;
    }

    public FlashcardSetEntity requireFlashcardSetExisting(final UUID uuid) {
        return flashcardSetRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Flashcard set with id %s not found".formatted(uuid)));
    }

    public List<Flashcard> getFlashcardsByIds(final List<UUID> ids) {
        final List<FlashcardEntity> entities = flashcardRepository.findByIdIn(ids);

        ids.removeAll(entities.stream().map(FlashcardEntity::getId).toList());
        if(!ids.isEmpty()) {
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
}
