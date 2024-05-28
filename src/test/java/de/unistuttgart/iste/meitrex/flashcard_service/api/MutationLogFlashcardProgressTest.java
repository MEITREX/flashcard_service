package de.unistuttgart.iste.meitrex.flashcard_service.api;

import de.unistuttgart.iste.meitrex.common.dapr.TopicPublisher;
import de.unistuttgart.iste.meitrex.common.event.ContentProgressedEvent;
import de.unistuttgart.iste.meitrex.common.testutil.*;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.common.event.Response;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity.FlashcardSetEntity;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.repository.FlashcardSetRepository;
import de.unistuttgart.iste.meitrex.flashcard_service.test_utils.TestUtils;
import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.UUID;

import static de.unistuttgart.iste.meitrex.common.testutil.TestUsers.userWithMembershipInCourseWithId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@GraphQlApiTest
@ContextConfiguration(classes = MockTestPublisherConfiguration.class)
@TablesToDelete({"flashcard_progress_data_log", "flashcard_progress_data", "flashcard_side", "flashcard", "flashcard_set"})
class MutationLogFlashcardProgressTest {

    @Autowired
    private FlashcardSetRepository flashcardSetRepository;
    @Autowired
    private TopicPublisher topicPublisher;

    private final UUID courseId = UUID.randomUUID();

    @InjectCurrentUserHeader
    private final LoggedInUser loggedInUser = userWithMembershipInCourseWithId(courseId, LoggedInUser.UserRoleInCourse.ADMINISTRATOR);

    private static final String mutation = """
                mutation logFlashcardProgress($id: UUID!, $successful: Boolean!) {
                    logFlashcardLearned(
                       input: {
                        flashcardId: $id,
                        successful: $successful
                       }
                    ) {
                        success
                        nextLearnDate
                        flashcardSetProgress {
                            correctness
                            percentageLearned
                        }
                    }
                    
                }
                """;

    /**
     * Tests that the mutation "logFlashcardProgress" works as expected
     */
    @Test
    @Transactional
    @Commit
    void testLogFlashcardProgress(final HttpGraphQlTester graphQlTester) {
        final List<FlashcardSetEntity> flashcardSet = new TestUtils().populateFlashcardSetRepository(flashcardSetRepository, courseId);

        FlashcardSetEntity flashcardSetEntity = flashcardSet.get(0);
        final UUID flashcardSetId = flashcardSetEntity.getAssessmentId();
        final UUID flashcardId1 = flashcardSetEntity.getFlashcards().get(0).getItemId();
        final UUID flashcardId2 = flashcardSetEntity.getFlashcards().get(1).getItemId();

        runMutationLogFlashcardLearned(graphQlTester, flashcardId1, true)
                .path("logFlashcardLearned.success").entity(Boolean.class).isEqualTo(true)
                .path("logFlashcardLearned.flashcardSetProgress.correctness").entity(Float.class).isEqualTo(1.0f)
                .path("logFlashcardLearned.flashcardSetProgress.percentageLearned").entity(Float.class).isEqualTo(0.5f);

        verify(topicPublisher, never()).notifyUserWorkedOnContent(any());

        flashcardSetEntity = flashcardSetRepository.findById(flashcardSetId).orElseThrow();
        assertThat(flashcardSetEntity.getLastLearned().isPresent(), is(false));

        runMutationLogFlashcardLearned(graphQlTester, flashcardId2, false)
                .path("logFlashcardLearned.success").entity(Boolean.class).isEqualTo(false)
                .path("logFlashcardLearned.flashcardSetProgress.correctness").entity(Float.class).isEqualTo(0.5f)
                .path("logFlashcardLearned.flashcardSetProgress.percentageLearned").entity(Float.class).isEqualTo(1.0f);

        final ContentProgressedEvent expectedEvent = ContentProgressedEvent.builder()
                .userId(loggedInUser.getId())
                .contentId(flashcardSetId)
                .correctness(0.5)
                .success(true)
                .timeToComplete(null)
                .hintsUsed(0)
                .responses(List.of(Response.builder().itemId(flashcardId1).response(1).build(),Response.builder().itemId(flashcardId2).response(0).build()))
                .build();

        verify(topicPublisher).notifyUserWorkedOnContent(expectedEvent);
        flashcardSetEntity = flashcardSetRepository.findById(flashcardSetId).orElseThrow();
        assertThat(flashcardSetEntity.getLastLearned().isPresent(), is(true));

        // do another run of the same flashcard set, with 100% correctness
        reset(topicPublisher);
        expectedEvent.setCorrectness(1.0);

        runMutationLogFlashcardLearned(graphQlTester, flashcardId1, true)
                .errors().verify();

        verify(topicPublisher, never()).notifyUserWorkedOnContent(any());

        runMutationLogFlashcardLearned(graphQlTester, flashcardId2, true)
                .errors().verify();
        final ContentProgressedEvent expectedEvent2 = ContentProgressedEvent.builder()
                .userId(loggedInUser.getId())
                .contentId(flashcardSetId)
                .correctness(1.0)
                .success(true)
                .timeToComplete(null)
                .hintsUsed(0)
                .responses(List.of(Response.builder().itemId(flashcardId1).response(1).build(),Response.builder().itemId(flashcardId2).response(1).build()))
                .build();

        verify(topicPublisher).notifyUserWorkedOnContent(expectedEvent2);
    }

    @NotNull
    private static GraphQlTester.Response runMutationLogFlashcardLearned(final HttpGraphQlTester graphQlTester,
                                                                         final UUID flashcardId,
                                                                         final boolean success) {
        return graphQlTester
                .document(mutation)
                .variable("id", flashcardId)
                .variable("successful", success)
                .execute();
    }
}
