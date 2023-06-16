package de.unistuttgart.iste.gits.flashcardservice.controller;
import de.unistuttgart.iste.gits.flashcardservice.service.FlashcardService;
import de.unistuttgart.iste.gits.generated.dto.CreateFlashcardInput;
import de.unistuttgart.iste.gits.generated.dto.UpdateFlashcardInput;
import de.unistuttgart.iste.gits.generated.dto.CreateFlashcardSetInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;


@Slf4j
@Controller
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;


    public FlashcardController(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
    }
    @QueryMapping
    public List<Flashcard> flashcardByIds(@Argument(name = "ids") List<Long> ids) {
        log.info("Request flashcard by Ids");
        return flashcardService.getFlashcardById(ids);
    }
    @QueryMapping
    public List<FlashcardSet> flashcardSetsByIds(@Argument(name = "ids") List<Long> ids) {
        log.info("Request flashcard by Ids");
        return flashcardService.getFlashcardSetsById(ids);
    }
    @QueryMapping
    public List<FlashcardSet> flashcardSetsByAssessmentIds(@Argument(name = "ids") List<Long> ids) {
        log.info("Request flashcard by Ids");
        return flashcardService.getFlashcardSetsByAssessmentId(ids);
    }

    @MutationMapping
    public Flashcard createFlashcard(@Argument(name = "input") CreateFlashcardInput input) {
        return flashcardService.createFlashcard(input);
    }

    @MutationMapping
    public Flashcard updateFlashcard(@Argument(name = "input") UpdateFlashcardInput input) {
        return flashcardService.updateFlashcard(input);
    }
    @MutationMapping
    public Long deleteFlashcard(@Argument(name = "id") Long id) {
        return flashcardService.deleteFlashcard(id);
    }

    @MutationMapping
    public Flashcard createFlashcardSet(@Argument(name = "input") CreateFlashcardSetInput input) {
        return flashcardService.createFlashcardSet(input);
    }
    @MutationMapping
    public Long deleteFlashcardSet(@Argument(name = "id") Long id) {
        return flashcardService.deleteFlashcardSet(id);
    }


}
