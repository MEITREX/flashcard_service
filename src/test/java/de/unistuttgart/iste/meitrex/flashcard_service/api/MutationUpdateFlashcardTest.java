package de.unistuttgart.iste.gits.flashcard_service.api;

import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.common.testutil.InjectCurrentUserHeader;
import de.unistuttgart.iste.gits.common.testutil.TablesToDelete;
import de.unistuttgart.iste.gits.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.gits.flashcard_service.persistence.entity.FlashcardSetEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardRepository;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardSetRepository;
import de.unistuttgart.iste.gits.flashcard_service.test_utils.TestUtils;
import de.unistuttgart.iste.gits.generated.dto.Flashcard;
import de.unistuttgart.iste.gits.generated.dto.FlashcardSide;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.annotation.Commit;

import java.util.List;
import java.util.UUID;

import static de.unistuttgart.iste.gits.common.testutil.TestUsers.userWithMembershipInCourseWithId;
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
        final UUID flashcardToUpdate = set.get(0).getFlashcards().stream().findAny().orElseThrow().getId();

        final String query = """
          mutation ($assessmentId: UUID!, $flashcardId: UUID!) {
            mutateFlashcardSet(assessmentId: $assessmentId) {
              updateFlashcard(input: {
                id: $flashcardId,
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
                id
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
                .path("mutateFlashcardSet.updateFlashcard")
                .entity(Flashcard.class)
                .get();

        // Assert the values of the data returned by the updateFlashcard mutation
        assertThat(updatedFlashcard.getId()).isEqualTo(flashcardToUpdate);
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
