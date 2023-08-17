package de.unistuttgart.iste.gits.flashcard_service.service;

import de.unistuttgart.iste.gits.flashcard_service.persistence.dao.FlashcardEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.dao.FlashcardSetEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.mapper.FlashcardMapper;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardRepository;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardSetRepository;
import de.unistuttgart.iste.gits.flashcard_service.validation.FlashcardValidator;
import de.unistuttgart.iste.gits.generated.dto.*;
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

    public Flashcard createFlashcard(UUID assessmentId, CreateFlashcardInput flashcardInput) {
        flashcardValidator.validateCreateFlashcardInput(flashcardInput);

        FlashcardSetEntity set = flashcardSetRepository.findById(assessmentId)
                .orElseThrow(() -> new EntityNotFoundException("FlashcardSet with id " + assessmentId
                        + " not found while trying to create a new flashcard for it."));

        FlashcardEntity flashcard = flashcardMapper.dtoToEntity(flashcardInput);
        flashcard.setParentSet(set);

        flashcard = flashcardRepository.save(flashcard);

        set.getFlashcards().add(flashcard);

        return flashcardMapper.entityToDto(flashcard);
    }


    public Flashcard updateFlashcard(UpdateFlashcardInput input) {
        flashcardValidator.validateUpdateFlashcardInput(input);

        FlashcardEntity oldFlashcard = flashcardRepository.findById(input.getId())
                .orElseThrow(() -> new EntityNotFoundException("Flashcard with id " + input.getId() + " not found."));

        FlashcardEntity updatedFlashcard = flashcardMapper.dtoToEntity(input);
        updatedFlashcard.setParentSet(oldFlashcard.getParentSet());

        updatedFlashcard = flashcardRepository.save(updatedFlashcard);

        return flashcardMapper.entityToDto(updatedFlashcard);
    }

    public UUID deleteFlashcard(UUID assessmentId, UUID flashcardId) {
        FlashcardSetEntity set = flashcardSetRepository.getReferenceById(assessmentId);
        if(!set.getFlashcards().removeIf(x -> x.getId().equals(flashcardId))) {
            throw new EntityNotFoundException("Flashcard with id " + flashcardId + " not found.");
        }
        flashcardSetRepository.save(set);
        return flashcardId;
    }

    public FlashcardSet createFlashcardSet(UUID assessmentId, CreateFlashcardSetInput flashcardSetInput) {
        flashcardValidator.validateCreateFlashcardSetInput(flashcardSetInput);

        FlashcardSetEntity mappedEntity = flashcardMapper.flashcardSetDtoToEntity(flashcardSetInput);
        mappedEntity.setAssessmentId(assessmentId);
        FlashcardSetEntity flashcardSetEntity = flashcardSetRepository.save(mappedEntity);
        return flashcardMapper.flashcardSetEntityToDto(flashcardSetEntity);
    }

    public UUID deleteFlashcardSet(UUID uuid) {
        requireFlashcardSetExisting(uuid);
        flashcardSetRepository.deleteById(uuid);
        return uuid;
    }

    private void requireFlashcardSetExisting(UUID uuid) {
        if (!flashcardSetRepository.existsById(uuid)) {
            throw new EntityNotFoundException("Flashcard set with id " + uuid + " not found");
        }
    }

    public Flashcard getFlashcardById(UUID flashcardId) {
        return flashcardMapper.entityToDto(flashcardRepository.getReferenceById(flashcardId));
    }

    public List<Flashcard> getFlashcardsByIds(List<UUID> ids) {
        List<FlashcardEntity> entities = flashcardRepository.findByIdIn(ids);

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

    public List<FlashcardSet> getFlashcardSetsByAssessmentId(List<UUID> ids) {
        return flashcardSetRepository.findByAssessmentIdIn(ids)
                .stream()
                .map(flashcardMapper::flashcardSetEntityToDto)
                .toList();
    }

}
