package de.unistuttgart.iste.gits.flashcard_service.service;

import de.unistuttgart.iste.gits.flashcard_service.persistence.dao.FlashcardEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.mapper.FlashcardMapper;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardRepository;
import de.unistuttgart.iste.gits.flashcard_service.validation.FlashcardValidator;
import de.unistuttgart.iste.gits.generated.dto.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final FlashcardMapper flashcardMapper;
    private final FlashcardValidator flashcardValidator;


    public Flashcard createFlashcard(CreateFlashcardInput flashcardInput) {
        flashcardValidator.validateCreateFlashcardInput(flashcardInput);

        FlashcardEntity flashcardEntity = flashcardRepository.save(flashcardMapper.dtoToEntity(flashcardInput));

        return flashcardMapper.entityToDto(flashcardEntity);
    }


    public Flashcard updateFlashcard(UpdateFlashcardInput input) {
        flashcardValidator.validateUpdateFlashcardInput(input);
        //requireFlashcardExisting(input.getId());

        FlashcardEntity updatedFlashcardEntity = flashcardRepository.save(flashcardMapper.dtoToEntity(input));

        return flashcardMapper.entityToDto(updatedFlashcardEntity);
    }

    public UUID deleteFlashcard(UUID uuid) {
        requireFlashcardExisting(uuid);
        flashcardRepository.deleteById(uuid);
        return uuid;
    }

    public Flashcard createFlashcardSet(CreateFlashcardSetInput flashcardSetInput) {
        flashcardValidator.validateCreateFlashcardSetInput(flashcardSetInput);

        FlashcardEntity flashcardSetEntity = flashcardRepository.save(flashcardMapper.dtoToEntity(flashcardSetInput));

        return flashcardMapper.entityToDto(flashcardSetEntity);
    }

    public UUID deleteFlashcardSet(UUID uuid) {
        requireFlashcardExisting(uuid);
        flashcardRepository.deleteById(uuid);
        return uuid;
    }

    public void requireFlashcardExisting(UUID id) {
        if (!flashcardRepository.existsById(id)) {
            throw new EntityNotFoundException("Flashcard with id " + id + " not found");
        }
    }

    public List<Flashcard> getFlashcardById(List<UUID> ids) {
        return flashcardRepository.findById(ids).stream().map(flashcardMapper::entityToDto).toList();
    }

    public List<Flashcard> getFlashcardSetsById(List<UUID> ids) {
        return flashcardRepository.findById(ids).stream().map(flashcardMapper::entityToDto).toList();
    }

    public List<Flashcard> getFlashcardSetsByAssessmentId(List<UUID> ids) {
        return flashcardRepository.findById(ids).stream().map(flashcardMapper::entityToDto).toList();
    }
}
