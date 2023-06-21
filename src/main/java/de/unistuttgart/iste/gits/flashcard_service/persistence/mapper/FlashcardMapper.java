package de.unistuttgart.iste.gits.flashcard_service.persistence.mapper;

import de.unistuttgart.iste.gits.generated.dto.Flashcard;
import de.unistuttgart.iste.gits.generated.dto.CreateFlashcardInput;
import de.unistuttgart.iste.gits.generated.dto.UpdateFlashcardInput;
import de.unistuttgart.iste.gits.generated.dto.CreateFlashcardSetInput;
import de.unistuttgart.iste.gits.flashcard_service.persistence.dao.FlashcardEntity;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component

public class FlashcardMapper {

    private final ModelMapper modelMapper;

    public FlashcardMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public Flashcard entityToDto(FlashcardEntity flashcardEntity) {
        return modelMapper.map(flashcardEntity, Flashcard.class);
    }

    public FlashcardEntity dtoToEntity(UpdateFlashcardInput input) {
        return modelMapper.map(input, FlashcardEntity.class);
    }

    public FlashcardEntity dtoToEntity(CreateFlashcardSetInput flashcardSetInput) {
        return modelMapper.map(flashcardSetInput, FlashcardEntity.class);
    }

    public FlashcardEntity dtoToEntity(CreateFlashcardInput flashcardInput) {
        return modelMapper.map(flashcardInput, FlashcardEntity.class);
    }


}
