package de.unistuttgart.iste.meitrex.flashcard_service.api;

import de.unistuttgart.iste.meitrex.common.testutil.AuthorizationAsserts;
import de.unistuttgart.iste.meitrex.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.meitrex.common.testutil.InjectCurrentUserHeader;
import de.unistuttgart.iste.meitrex.common.testutil.TablesToDelete;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity.FlashcardSetEntity;
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
    void testDeleteFlashcardOnlyForAdmins(final GraphQlTester tester) {
       final List<FlashcardSetEntity> expectedSets = testUtils.populateFlashcardSetRepository(flashcardSetRepository, courseId);

        final String query = """
                mutation($assessmentId: UUID!, $flashcardId: UUID!) {
                    mutateFlashcardSet(assessmentId: $assessmentId) {
                        deleteFlashcard(id: $flashcardId)
                    }
                }
                """;

        final UUID setToDeleteFrom = expectedSets.get(0).getAssessmentId();
        final UUID flashcardToDelete = expectedSets.get(0).getFlashcards().stream().findAny().orElseThrow().getItemId();

        tester.document(query)
                .variable("assessmentId", setToDeleteFrom)
                .variable("flashcardId", flashcardToDelete)
                .execute()
                .errors()
                .satisfy(AuthorizationAsserts::assertIsMissingUserRoleError);

    }


}

