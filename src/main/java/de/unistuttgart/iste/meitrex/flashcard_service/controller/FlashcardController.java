package de.unistuttgart.iste.meitrex.flashcard_service.controller;

import de.unistuttgart.iste.meitrex.generated.dto.Flashcard;
import de.unistuttgart.iste.meitrex.generated.dto.FlashcardSet;

import de.unistuttgart.iste.meitrex.common.exception.NoAccessToCourseException;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser.UserRoleInCourse;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity.FlashcardSetEntity;
import de.unistuttgart.iste.meitrex.flashcard_service.service.FlashcardService;
import de.unistuttgart.iste.meitrex.flashcard_service.service.FlashcardUserProgressDataService;
import de.unistuttgart.iste.meitrex.generated.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

import static de.unistuttgart.iste.meitrex.common.user_handling.UserCourseAccessValidator.validateUserHasAccessToCourse;
import static de.unistuttgart.iste.meitrex.common.user_handling.UserCourseAccessValidator.validateUserHasAccessToCourses;


@Slf4j
@Controller
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;
    private final FlashcardUserProgressDataService progressDataService;

    @QueryMapping
    public List<Flashcard> flashcardsByIds(@Argument(name = "itemIds") final List<UUID> ids,
                                           @ContextValue final LoggedInUser currentUser) {
        final List<UUID> courseIds = flashcardService.getCourseIdsForFlashcardIds(ids);

        validateUserHasAccessToCourses(currentUser, UserRoleInCourse.STUDENT, courseIds);

        return flashcardService.getFlashcardsByIds(ids);
    }

    @QueryMapping
    public List<FlashcardSet> findFlashcardSetsByAssessmentIds(@Argument(name = "assessmentIds") final List<UUID> ids,
                                                               @ContextValue final LoggedInUser currentUser) {

        return flashcardService.findFlashcardSetsByAssessmentIds(ids).stream()
                .map(set -> {
                    if (set == null) {
                        return null;
                    }
                    try {
                        // check if the user has access to the course, otherwise return null
                        validateUserHasAccessToCourse(currentUser, UserRoleInCourse.STUDENT, set.getCourseId());
                        return set;
                    } catch (NoAccessToCourseException ex) {
                        return null;
                    }
                })
                .toList();
    }

    @QueryMapping
    public List<Flashcard> dueFlashcardsByCourseId(@Argument final UUID courseId,
                                                   @ContextValue final LoggedInUser currentUser) {
        validateUserHasAccessToCourse(currentUser, UserRoleInCourse.STUDENT, courseId);
        return progressDataService.getAllDueFlashcardsOfCourse(courseId, currentUser.getId());
    }

    @SchemaMapping(typeName = "Flashcard", field = "userProgressData")
    public FlashcardProgressData flashcardUserProgressData(final Flashcard flashcard,
                                                           @ContextValue final LoggedInUser currentUser) {
        return progressDataService.getProgressData(flashcard.getItemId(), currentUser.getId());
    }

    @MutationMapping
    public FlashcardSetMutation mutateFlashcardSet(@Argument final UUID assessmentId,
                                                   @ContextValue final LoggedInUser currentUser) {
        final FlashcardSetEntity flashcardSet = flashcardService.requireFlashcardSetExisting(assessmentId);

        validateUserHasAccessToCourse(currentUser, UserRoleInCourse.ADMINISTRATOR, flashcardSet.getCourseId());

        // this is basically an empty object, only serving as a parent for the nested mutations
        return new FlashcardSetMutation(assessmentId);
    }

    @SchemaMapping(typeName = "FlashcardSetMutation")
    public Flashcard _internal_noauth_createFlashcard(@Argument(name = "input") final CreateFlashcardInput input,
                                                      final FlashcardSetMutation mutation) {
        return flashcardService.createFlashcard(mutation.getAssessmentId(), input);
    }

    @SchemaMapping(typeName = "FlashcardSetMutation")
    public Flashcard _internal_noauth_updateFlashcard(@Argument(name = "input") final UpdateFlashcardInput input) {
        return flashcardService.updateFlashcard(input);
    }

    @SchemaMapping(typeName = "FlashcardSetMutation")
    public Flashcard updateFlashcard(@Argument(name = "input") final UpdateFlashcardInput input) {
        return flashcardService.updateFlashcard(input);
    }

    @SchemaMapping(typeName = "FlashcardSetMutation")
    public UUID deleteFlashcard(@Argument final UUID id, final FlashcardSetMutation mutation) {
        return flashcardService.deleteFlashcard(mutation.getAssessmentId(), id);
    }

    @MutationMapping(name = "_internal_noauth_createFlashcardSet")
    public FlashcardSet createFlashcardSet(@Argument final UUID courseId,
                                           @Argument final UUID assessmentId,
                                           @Argument final CreateFlashcardSetInput input) {
        return flashcardService.createFlashcardSet(courseId, assessmentId, input);
    }

    @MutationMapping
    public UUID deleteFlashcardSet(@Argument final UUID assessmentId) {
        return flashcardService.deleteFlashcardSet(assessmentId);
    }

    @MutationMapping
    public FlashcardLearnedFeedback logFlashcardLearned(@Argument("input") final LogFlashcardLearnedInput input,
                                                        @ContextValue final LoggedInUser currentUser) {
        final UUID courseId = flashcardService.getCourseIdsForFlashcardIds(List.of(input.getFlashcardId())).get(0);

        validateUserHasAccessToCourse(currentUser, UserRoleInCourse.STUDENT, courseId);

        return progressDataService.logFlashcardLearned(input.getFlashcardId(),
                currentUser.getId(),
                input.getSuccessful());
    }


}
