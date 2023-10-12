package de.unistuttgart.iste.gits.flashcard_service.api;

import de.unistuttgart.iste.gits.common.testutil.AuthorizationAsserts;
import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.common.testutil.InjectCurrentUserHeader;
import de.unistuttgart.iste.gits.common.testutil.TablesToDelete;
import de.unistuttgart.iste.gits.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.gits.flashcard_service.persistence.entity.FlashcardSetEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardSetRepository;
import de.unistuttgart.iste.gits.flashcard_service.test_utils.TestUtils;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.annotation.Commit;

import java.util.List;
import java.util.UUID;

import static de.unistuttgart.iste.gits.common.testutil.TestUsers.userWithMembershipInCourseWithId;

@GraphQlApiTest
@TablesToDelete({"flashcard_side", "flashcard", "flashcard_set"})
public class TestAuthorization {

    @Autowired
    private FlashcardSetRepository flashcardSetRepository;

    private final UUID courseId = UUID.randomUUID();

    @InjectCurrentUserHeader
    private final LoggedInUser loggedInUser = userWithMembershipInCourseWithId(courseId, LoggedInUser.UserRoleInCourse.STUDENT);

    @Autowired
    private TestUtils testUtils;

    @Test
    @Transactional
    @Commit
    void testUpdateFlashcardOnlyForAdmins(final GraphQlTester tester) {
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
        tester.document(query)
                .variable("assessmentId", setOfFlashcard)
                .variable("flashcardId", flashcardToUpdate)
                .execute()
                .errors()
                .satisfy(AuthorizationAsserts::assertIsMissingUserRoleError);

    }


}

