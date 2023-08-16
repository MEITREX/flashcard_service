package de.unistuttgart.iste.gits.flashcard_service.persistence.mapper;

import de.unistuttgart.iste.gits.flashcard_service.persistence.dao.FlashcardEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.dao.FlashcardSetEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.dao.FlashcardSideEntity;
import de.unistuttgart.iste.gits.generated.dto.*;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class FlashcardMapper {

    private final ModelMapper modelMapper;

    public FlashcardMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public Flashcard entityToDto(FlashcardEntity flashcardEntity) {
        Flashcard result = modelMapper.map(flashcardEntity, Flashcard.class);
        for (int i = 0; i < flashcardEntity.getSides().size(); i++) {
            FlashcardSideEntity sideEntity = flashcardEntity.getSides().get(i);
            FlashcardSide side = result.getSides().get(i);
            side.setIsQuestion(sideEntity.isQuestion()); // manual mapping necessary because of naming difference
            result.getSides().set(i, side);
        }
        return result;
    }

    public FlashcardEntity dtoToEntity(UpdateFlashcardInput input) {
        FlashcardEntity flashcardEntity = modelMapper.map(input, FlashcardEntity.class);

        for (FlashcardSideEntity side : flashcardEntity.getSides()) {
            side.setFlashcard(flashcardEntity);
        }

        return flashcardEntity;
    }

    public FlashcardEntity dtoToEntity(CreateFlashcardInput flashcardInput) {
        FlashcardEntity flashcardEntity = modelMapper.map(flashcardInput, FlashcardEntity.class);

        for (FlashcardSideEntity side : flashcardEntity.getSides()) {
            side.setFlashcard(flashcardEntity);
        }

        return flashcardEntity;
    }

    public FlashcardSetEntity flashcardSetDtoToEntity(CreateFlashcardSetInput flashcardSetInput) {
        FlashcardSetEntity flashcardSetEntity = modelMapper.map(flashcardSetInput, FlashcardSetEntity.class);

        for (FlashcardEntity flashcard : flashcardSetEntity.getFlashcards()) {
            flashcard.setParentSet(flashcardSetEntity);
            for (FlashcardSideEntity side : flashcard.getSides()) {
                side.setFlashcard(flashcard);
            }
        }

        return flashcardSetEntity;
    }

    public FlashcardSet flashcardSetEntityToDto(FlashcardSetEntity flashcardSetEntity) {
        var result = modelMapper.map(flashcardSetEntity, FlashcardSet.class);
        result.setFlashcards(flashcardSetEntity.getFlashcards().stream().map(this::entityToDto).toList());
        return result;
    }
}
