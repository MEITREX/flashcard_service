package de.unistuttgart.iste.gits.flashcard_service.controller;

import de.unistuttgart.iste.gits.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.gits.flashcard_service.service.FlashcardService;
import de.unistuttgart.iste.gits.flashcard_service.service.FlashcardUserProgressDataService;
import de.unistuttgart.iste.gits.generated.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;


@Slf4j
@Controller
public class FlashcardController {

    private final FlashcardService flashcardService;
    private final FlashcardUserProgressDataService progressDataService;


    public FlashcardController(FlashcardService flashcardService, FlashcardUserProgressDataService progressDataService) {
        this.flashcardService = flashcardService;
        this.progressDataService = progressDataService;
    }

    @QueryMapping
    public List<Flashcard> flashcardsByIds(@Argument(name = "ids") List<UUID> ids) {
        return flashcardService.getFlashcardsByIds(ids);
    }

    @QueryMapping
    public List<FlashcardSet> findFlashcardSetsByAssessmentIds(@Argument(name = "assessmentIds") List<UUID> ids) {
        return flashcardService.findFlashcardSetsByAssessmentId(ids);
    }

    @SchemaMapping(typeName = "Flashcard", field = "userProgressData")
    public FlashcardProgressData flashcardUserProgressData(Flashcard flashcard, @ContextValue LoggedInUser currentUser) {
        return progressDataService.getProgressData(flashcard.getId(), currentUser.getId());
    }

    @MutationMapping
    public FlashcardSetMutation mutateFlashcardSet(@Argument UUID assessmentId) {
        // this is basically an empty object, only serving as a parent for the nested mutations
        return new FlashcardSetMutation(assessmentId);
    }

    @SchemaMapping(typeName = "FlashcardSetMutation")
    public Flashcard createFlashcard(@Argument(name = "input") CreateFlashcardInput input, FlashcardSetMutation mutation) {
        return flashcardService.createFlashcard(mutation.getAssessmentId(), input);
    }

    @SchemaMapping(typeName = "FlashcardSetMutation")
    public Flashcard updateFlashcard(@Argument(name = "input") UpdateFlashcardInput input) {
        return flashcardService.updateFlashcard(input);
    }

    @SchemaMapping(typeName = "FlashcardSetMutation")
    public UUID deleteFlashcard(@Argument UUID id, FlashcardSetMutation mutation) {
        return flashcardService.deleteFlashcard(mutation.getAssessmentId(), id);
    }

    @MutationMapping
    public FlashcardSet _internal_createFlashcardSet(@Argument UUID courseId,
                                                     @Argument UUID assessmentId,
                                                     @Argument CreateFlashcardSetInput input) {
        return flashcardService.createFlashcardSet(courseId, assessmentId, input);
    }

    @MutationMapping
    public UUID deleteFlashcardSet(@Argument(name = "input") UUID id) {
        return flashcardService.deleteFlashcardSet(id);
    }

   @MutationMapping
    public FlashcardLearnedFeedback logFlashcardLearned(@Argument("input") LogFlashcardLearnedInput input, @ContextValue LoggedInUser currentUser) {
        UUID flashcardId = input.getFlashcardId();
        boolean successful = input.getSuccessful();
        UUID authenticatedUserId = currentUser.getId(); // Use the authenticated user's ID
        return progressDataService.logFlashcardLearned(flashcardId, authenticatedUserId, successful);
    }


}
