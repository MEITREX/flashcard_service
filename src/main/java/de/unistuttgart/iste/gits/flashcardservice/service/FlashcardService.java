package de.unistuttgart.iste.gits.flashcardservice.service;

import de.unistuttgart.iste.gits.flashcardservice.persistence.dao.FlashcardEntity;
import de.unistuttgart.iste.gits.flashcardservice.persistence.mapper.FlashcardMapper;
import de.unistuttgart.iste.gits.flashcardservice.persistence.repository.FlashcardRepository;
import de.unistuttgart.iste.gits.flashcardservice.validation.FlashcardValidator;
import de.unistuttgart.iste.gits.generated.dto.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
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
    public Long deleteFlashcard(Long long1) {
        requireFlashcardExisting(long1);
        flashcardRepository.deleteById(long1);
        return long1;
    }
    public Flashcard createFlashcardSet(CreateFlashcardSetInput flashcardSetInput) {
        flashcardValidator.validateCreateFlashcardSetInput(flashcardSetInput);

        FlashcardEntity flashcardSetEntity = flashcardRepository.save(flashcardMapper.dtoToEntity(flashcardSetInput));

        return flashcardMapper.entityToDto(flashcardSetEntity);
    }

    public Long deleteFlashcardSet(Long long1) {
        requireFlashcardExisting(long1);
        flashcardRepository.deleteById(long1);
        return long1;
    }
    public void requireFlashcardExisting(Long id) {
        if (!flashcardRepository.existsById(id)) {
            throw new EntityNotFoundException("Flashcard with id " + id + " not found");
        }
    }
    public List<Flashcard> getFlashcardById(List<Long> ids) {
        return flashcardRepository.findById(ids).stream().map(flashcardMapper::entityToDto).toList();
    }
    public List<Flashcard> getFlashcardSetsById(List<Long> ids) {
        return flashcardRepository.findById(ids).stream().map(flashcardMapper::entityToDto).toList();
    }

    public List<Flashcard> getFlashcardSetsByAssessmentId(List<Long> ids) {
        return flashcardRepository.findById(ids).stream().map(flashcardMapper::entityToDto).toList();
    }
}
