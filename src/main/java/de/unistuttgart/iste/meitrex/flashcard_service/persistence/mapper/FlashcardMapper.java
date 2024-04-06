package de.unistuttgart.iste.meitrex.flashcard_service.persistence.mapper;

import de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity.*;
import de.unistuttgart.iste.meitrex.generated.dto.*;
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
            side.setIsAnswer(sideEntity.isAnswer());
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
        if (flashcardSetEntity == null) {
            return null;
        }
        var result = modelMapper.map(flashcardSetEntity, FlashcardSet.class);
        result.setFlashcards(flashcardSetEntity.getFlashcards().stream().map(this::entityToDto).toList());
        return result;
    }
}
