package de.unistuttgart.iste.gits.flashcard_service.service;

import de.unistuttgart.iste.gits.flashcard_service.persistence.dao.FlashcardEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.dao.FlashcardSetEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.dao.FlashcardSideEntity;
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

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final FlashcardSetRepository flashcardSetRepository;
    private final FlashcardMapper flashcardMapper;
    private final FlashcardValidator flashcardValidator;


    public Flashcard createFlashcard(CreateFlashcardInput flashcardInput) {
        flashcardValidator.validateCreateFlashcardInput(flashcardInput);

        FlashcardEntity mappedEntity = flashcardMapper.dtoToEntity(flashcardInput);
        for (FlashcardSideEntity flashcardSideEntity : mappedEntity.getSides()) {
            flashcardSideEntity.setFlashcard(mappedEntity);
        }
        mappedEntity.setSetId(flashcardInput.getSetId());
        FlashcardEntity flashcardEntity = flashcardRepository.save(mappedEntity);
        return flashcardMapper.entityToDto(flashcardEntity);
    }


    public Flashcard updateFlashcard(UpdateFlashcardInput input) {
        flashcardValidator.validateUpdateFlashcardInput(input);

        FlashcardEntity oldFlashcardEntity = flashcardRepository.getReferenceById(input.getId());
        FlashcardEntity mappedEntity = flashcardMapper.dtoToEntity(input);
        for (FlashcardSideEntity flashcardSideEntity : mappedEntity.getSides()) {
            flashcardSideEntity.setFlashcard(mappedEntity);
        }
        mappedEntity.setSetId(oldFlashcardEntity.getSetId());
        FlashcardEntity updatedFlashcardEntity = flashcardRepository.save(mappedEntity);

        return flashcardMapper.entityToDto(updatedFlashcardEntity);
    }

    public UUID deleteFlashcard(UUID uuid) {
        requireFlashcardExisting(uuid);
        flashcardRepository.deleteById(uuid);
        return uuid;
    }

    public FlashcardSet createFlashcardSet(CreateFlashcardSetInput flashcardSetInput) {
        flashcardValidator.validateCreateFlashcardSetInput(flashcardSetInput);

        FlashcardSetEntity mappedEntity = flashcardMapper.flashcardSetDtoToEntity(flashcardSetInput);
        for (FlashcardEntity flashcardEntity : mappedEntity.getFlashcards()) {
            flashcardEntity.setSetId(mappedEntity.getAssessmentId());

            for (FlashcardSideEntity flashcardSideEntity : flashcardEntity.getSides()) {
                flashcardSideEntity.setFlashcard(flashcardEntity);
            }
        }
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

    public void requireFlashcardExisting(UUID id) {
        if (!flashcardRepository.existsById(id)) {
            throw new EntityNotFoundException("Flashcard with id " + id + " not found");
        }
    }

    public List<Flashcard> getFlashcardsById(List<UUID> ids) {
        var entities = flashcardRepository.findByIdIn(ids);
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
