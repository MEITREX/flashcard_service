package de.unistuttgart.iste.gits.flashcard_service.api;

import de.unistuttgart.iste.gits.common.event.UserProgressLogEvent;
import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.common.testutil.TablesToDelete;
import de.unistuttgart.iste.gits.flashcard_service.dapr.TopicPublisher;
import de.unistuttgart.iste.gits.flashcard_service.persistence.entity.FlashcardSetEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardSetRepository;
import de.unistuttgart.iste.gits.flashcard_service.test_config.MockTopicPublisherConfiguration;
import de.unistuttgart.iste.gits.flashcard_service.test_utils.TestUtils;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@GraphQlApiTest
@ContextConfiguration(classes = MockTopicPublisherConfiguration.class)
@TablesToDelete({"flashcard_progress_data_log", "flashcard_progress_data", "flashcard_side", "flashcard", "flashcard_set"})
class MutationLogFlashcardProgressTest {

    @Autowired
    private FlashcardSetRepository flashcardSetRepository;
    @Autowired
    private TopicPublisher topicPublisher;

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
    void testLogFlashcardProgress(HttpGraphQlTester graphQlTester) {
        List<FlashcardSetEntity> flashcardSet = new TestUtils().populateFlashcardSetRepository(flashcardSetRepository);

        UUID userId1 = UUID.randomUUID();
        FlashcardSetEntity flashcardSetEntity = flashcardSet.get(0);
        UUID flashcardSetId = flashcardSetEntity.getAssessmentId();
        UUID flashcardId1 = flashcardSetEntity.getFlashcards().get(0).getId();
        UUID flashcardId2 = flashcardSetEntity.getFlashcards().get(1).getId();

        String currentUser = """
                {
                    "id": "%s",
                    "userName": "MyUserName",
                    "firstName": "John",
                    "lastName": "Doe",
                    "courseMemberships": []
                }
                """.formatted(userId1.toString());

        runMutationLogFlashcardLearned(graphQlTester, currentUser, flashcardId1, true)
                .path("logFlashcardLearned.success").entity(Boolean.class).isEqualTo(true)
                .path("logFlashcardLearned.flashcardSetProgress.correctness").entity(Float.class).isEqualTo(1.0f)
                .path("logFlashcardLearned.flashcardSetProgress.percentageLearned").entity(Float.class).isEqualTo(0.5f);

        verify(topicPublisher, never()).notifyFlashcardSetLearned(any());

        flashcardSetEntity = flashcardSetRepository.findById(flashcardSetId).orElseThrow();
        assertThat(flashcardSetEntity.getLastLearned().isPresent(), is(false));

        runMutationLogFlashcardLearned(graphQlTester, currentUser, flashcardId2, false)
                .path("logFlashcardLearned.success").entity(Boolean.class).isEqualTo(false)
                .path("logFlashcardLearned.flashcardSetProgress.correctness").entity(Float.class).isEqualTo(0.5f)
                .path("logFlashcardLearned.flashcardSetProgress.percentageLearned").entity(Float.class).isEqualTo(1.0f);

        UserProgressLogEvent expectedEvent = UserProgressLogEvent.builder()
                .userId(userId1)
                .contentId(flashcardSetId)
                .correctness(0.5)
                .success(true)
                .timeToComplete(null)
                .hintsUsed(0)
                .build();

        verify(topicPublisher).notifyFlashcardSetLearned(expectedEvent);
        flashcardSetEntity = flashcardSetRepository.findById(flashcardSetId).orElseThrow();
        assertThat(flashcardSetEntity.getLastLearned().isPresent(), is(true));

        // do another run of the same flashcard set, with 100% correctness
        reset(topicPublisher);
        expectedEvent.setCorrectness(1.0);

        runMutationLogFlashcardLearned(graphQlTester, currentUser, flashcardId1, true)
                .errors().verify();

        verify(topicPublisher, never()).notifyFlashcardSetLearned(any());

        runMutationLogFlashcardLearned(graphQlTester, currentUser, flashcardId2, true)
                .errors().verify();

        verify(topicPublisher).notifyFlashcardSetLearned(expectedEvent);
    }

    @NotNull
    private static GraphQlTester.Response runMutationLogFlashcardLearned(HttpGraphQlTester graphQlTester,
                                                                         String currentUser,
                                                                         UUID flashcardId,
                                                                         boolean success) {
        return graphQlTester.mutate()
                .header("CurrentUser", currentUser)
                .build()
                .document(mutation)
                .variable("id", flashcardId)
                .variable("successful", success)
                .execute();
    }
}
