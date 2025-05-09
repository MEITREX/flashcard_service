package de.unistuttgart.iste.meitrex.flashcard_service.api;

import de.unistuttgart.iste.meitrex.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.meitrex.common.testutil.InjectCurrentUserHeader;
import de.unistuttgart.iste.meitrex.common.testutil.TablesToDelete;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity.*;

import de.unistuttgart.iste.meitrex.flashcard_service.persistence.repository.FlashcardRepository;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.repository.FlashcardSetRepository;
import de.unistuttgart.iste.meitrex.flashcard_service.test_utils.TestUtils;
import de.unistuttgart.iste.meitrex.generated.dto.Flashcard;
import de.unistuttgart.iste.meitrex.generated.dto.FlashcardSide;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ContextConfiguration;
import de.unistuttgart.iste.meitrex.common.testutil.MockTestPublisherConfiguration;

import java.util.List;
import java.util.UUID;


import static org.assertj.core.api.Assertions.assertThat;
import static de.unistuttgart.iste.meitrex.common.testutil.TestUsers.userWithMembershipInCourseWithId;


@GraphQlApiTest
@ContextConfiguration(classes = MockTestPublisherConfiguration.class)
@TablesToDelete({"flashcard_side", "flashcard", "flashcard_set"})
class MutationCreateFlashcardTest {

    @Autowired
    private FlashcardSetRepository flashcardSetRepository;

    @Autowired
    private FlashcardRepository flashcardRepository;
    private final UUID courseId = UUID.randomUUID();

    @InjectCurrentUserHeader
    private final LoggedInUser loggedInUser = userWithMembershipInCourseWithId(courseId, LoggedInUser.UserRoleInCourse.ADMINISTRATOR);

    @Autowired
    private TestUtils testUtils;

    @Test
    @Transactional
    @Commit
    void testCreateFlashcard(final GraphQlTester graphQlTester) {
        final List<FlashcardSetEntity> sets = testUtils.populateFlashcardSetRepository(flashcardSetRepository, courseId);
        final UUID itemId = UUID.randomUUID();
        final String query = """
                mutation ($setId: UUID!,$itemId:UUID!) {
                  mutateFlashcardSet(assessmentId: $setId) {
                    _internal_noauth_createFlashcard(input: {
                      itemId:$itemId,
                      sides: [
                      {
                        label: "Side 11",
                        isQuestion: true,
                        isAnswer: false,
                        text: "Question 1"
                      },
                      {
                        label: "Side 21",
                        isQuestion: false,
                        isAnswer: true,
                        text: "Answer 1"
                      }
                      ]
                    }) {
                      itemId
                      sides {
                        label
                        isQuestion
                        isAnswer
                        text
                      }
                    }
                  }
                }
                """;


        final UUID setId = sets.get(0).getAssessmentId();

        // Execute the mutation and extract the created flashcard
        final Flashcard createdFlashcard = graphQlTester.document(query)
                .variable("setId", setId)
                .variable("itemId", itemId)
                .execute()
                .path("mutateFlashcardSet._internal_noauth_createFlashcard")
                .entity(Flashcard.class)
                .get();

        // Assert the values of the data returned by the createFlashcard mutation
        assertThat(createdFlashcard.getItemId()).isNotNull();
        assertThat(createdFlashcard.getSides()).containsExactlyInAnyOrder(
                new FlashcardSide("Question 1", "Side 11", true, false),
                new FlashcardSide("Answer 1", "Side 21", false, true)
        );

        // Assert that the flashcard was added to the set in the repository
        assertThat(flashcardSetRepository
                .getReferenceById(setId)
                .getFlashcards()
                .stream()
                .map(FlashcardEntity::getItemId))
                .contains(createdFlashcard.getItemId());

        final FlashcardEntity flashcardFromRepo = flashcardRepository.getReferenceById(createdFlashcard.getItemId());

        assertThat(flashcardFromRepo.getParentSet().getAssessmentId()).isEqualTo(setId);
        assertThat(flashcardFromRepo.getSides().get(0))
                .returns("Side 11", FlashcardSideEntity::getLabel)
                .returns(true, FlashcardSideEntity::isQuestion)
                .returns("Question 1", FlashcardSideEntity::getText);
        assertThat(flashcardFromRepo.getSides().get(1))
                .returns("Side 21", FlashcardSideEntity::getLabel)
                .returns(false, FlashcardSideEntity::isQuestion)
                .returns("Answer 1", FlashcardSideEntity::getText);
    }

    @Test
    void testCreateInvalidFlashcard(final GraphQlTester graphQlTester) {
        final List<FlashcardSetEntity> sets = testUtils.populateFlashcardSetRepository(flashcardSetRepository, courseId);
        final UUID itemId = UUID.randomUUID();
        final String query = """
                mutation ($setId: UUID!,$itemId:UUID!) {
                  mutateFlashcardSet(assessmentId: $setId) {
                     _internal_noauth_createFlashcard(input: {
                      itemId:$itemId
                      sides: [
                      {
                        label: "Side 11",
                        isQuestion: false,
                        isAnswer: true,
                        text: "Question 1"
                      },
                      {
                        label: "Side 21",
                        isQuestion: false,
                        isAnswer: true,
                        text: "Answer 1"
                      }
                      ]
                    }) {
                      itemId
                      sides {
                        label
                        isQuestion
                        isAnswer
                        text
                      }
                    }
                  }
                }
                """;


        final UUID setId = sets.get(0).getAssessmentId();

        // Execute the mutation and check for expected errors
        graphQlTester.document(query)
                .variable("setId", setId)
                .variable("itemId", itemId)
                .execute()
                .errors()
                .expect(responseError -> responseError.getMessage() != null && responseError.getMessage().toLowerCase().contains("flashcards must have at least one question side and one answer side"));



    }

    @Test
    void testCreateInvalidFlashcardSide(final GraphQlTester graphQlTester) {
        final List<FlashcardSetEntity> sets = testUtils.populateFlashcardSetRepository(flashcardSetRepository, courseId);
        final UUID itemId = UUID.randomUUID();
        final String query = """
                mutation ($setId: UUID!) {
                  mutateFlashcardSet(assessmentId: $setId) {
                     _internal_noauth_createFlashcard(input: {
                      sides: [
                      {
                        label: "Side 11",
                        isQuestion: true,
                        isAnswer: false,
                        text: "Question 1"
                      },
                      {
                        label: "Side 21",
                        isQuestion: false,
                        isAnswer: false,
                        text: "Answer 1"
                      }
                      ]
                    }) {
                      itemId
                      sides {
                        label
                        isQuestion
                        isAnswer
                        text
                      }
                    }
                  }
                }
                """;


        final UUID setId = sets.get(0).getAssessmentId();

        // Execute the mutation and check for expected errors
        graphQlTester.document(query)
                .variable("setId", setId)
                .variable("itemId", itemId)
                .execute()
                .errors()
                .expect(responseError -> responseError.getMessage() != null && responseError.getMessage().toLowerCase().contains("flashcard side must must be at least a question or an answer"));



    }
}
