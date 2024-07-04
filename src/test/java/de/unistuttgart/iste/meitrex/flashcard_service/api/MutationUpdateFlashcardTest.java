package de.unistuttgart.iste.meitrex.flashcard_service.api;


import de.unistuttgart.iste.meitrex.common.testutil.AuthorizationAsserts;
import de.unistuttgart.iste.meitrex.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.meitrex.common.testutil.InjectCurrentUserHeader;
import de.unistuttgart.iste.meitrex.common.testutil.TablesToDelete;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity.FlashcardSetEntity;

import de.unistuttgart.iste.meitrex.flashcard_service.persistence.repository.FlashcardRepository;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.repository.FlashcardSetRepository;
import de.unistuttgart.iste.meitrex.flashcard_service.test_utils.TestUtils;
import de.unistuttgart.iste.meitrex.generated.dto.Flashcard;
import de.unistuttgart.iste.meitrex.generated.dto.FlashcardSide;

import de.unistuttgart.iste.meitrex.flashcard_service.persistence.repository.FlashcardSetRepository;
import de.unistuttgart.iste.meitrex.flashcard_service.test_utils.TestUtils;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.annotation.Commit;

import java.util.List;
import java.util.UUID;

import static de.unistuttgart.iste.meitrex.common.testutil.TestUsers.userWithMembershipInCourseWithId;
import static org.assertj.core.api.Assertions.assertThat;

@GraphQlApiTest
@TablesToDelete({"flashcard_side", "flashcard", "flashcard_set"})
class MutationUpdateFlashcardTest {



    @Autowired
    private FlashcardSetRepository flashcardSetRepository;

    @Autowired
    FlashcardRepository flashcardRepository;
    private final UUID courseId = UUID.randomUUID();

    @InjectCurrentUserHeader
    private final LoggedInUser loggedInUser = userWithMembershipInCourseWithId(courseId, LoggedInUser.UserRoleInCourse.ADMINISTRATOR);

    @Autowired
    private TestUtils testUtils;

    @Test
    @Transactional
    @Commit
    void testUpdateFlashcard(final GraphQlTester tester) {
        final List<FlashcardSetEntity> set = testUtils.populateFlashcardSetRepository(flashcardSetRepository, courseId);

        final UUID setOfFlashcard = set.get(0).getAssessmentId();
        // Perform the update operation
        final UUID flashcardToUpdate = set.get(0).getFlashcards().stream().findAny().orElseThrow().getItemId();

        final String query = """
                  mutation ($assessmentId: UUID!, $flashcardId: UUID!) {
                    mutateFlashcardSet(assessmentId: $assessmentId) {
                      _internal_noauth_updateFlashcard(input: {
                        itemId: $flashcardId,
                        sides: [
                          {
                            label: "New_Side 1",
                            isQuestion: true,
                            isAnswer: false,
                            text: "{text: \\"New_Question 1\\"}"
                          },
                          {
                            label: "New_Side 2",
                            isQuestion: false,
                            isAnswer: true,
                            text: "{text: \\"New_Answer 1\\"}"
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


        // Execute the update mutation query
        final Flashcard updatedFlashcard = tester.document(query)
                .variable("assessmentId", setOfFlashcard)
                .variable("flashcardId", flashcardToUpdate)
                .execute()
                .path("mutateFlashcardSet._internal_noauth_updateFlashcard")
                .entity(Flashcard.class)
                .get();

        // Assert the values of the data returned by the updateFlashcard mutation
        assertThat(updatedFlashcard.getItemId()).isEqualTo(flashcardToUpdate);
        assertThat(updatedFlashcard.getSides()).containsExactlyInAnyOrder(
                new FlashcardSide("{text: \"New_Question 1\"}",
                        "New_Side 1",
                        true, false),
                new FlashcardSide("{text: \"New_Answer 1\"}",
                        "New_Side 2",
                        false, true)
        );

        assertThat(flashcardRepository.count()).isEqualTo(4);
    }
}
