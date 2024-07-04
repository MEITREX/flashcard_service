package de.unistuttgart.iste.meitrex.flashcard_service.api;

import de.unistuttgart.iste.meitrex.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.meitrex.common.testutil.InjectCurrentUserHeader;
import de.unistuttgart.iste.meitrex.common.testutil.TablesToDelete;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity.FlashcardSetEntity;

import de.unistuttgart.iste.meitrex.flashcard_service.persistence.repository.*;

import de.unistuttgart.iste.meitrex.flashcard_service.test_utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static de.unistuttgart.iste.meitrex.common.testutil.TestUsers.userWithMembershipInCourseWithId;
import static org.assertj.core.api.Assertions.assertThat;

@GraphQlApiTest
@TablesToDelete({"flashcard_side", "flashcard", "flashcard_set"})
class MutationDeleteFlashcardSetTest {

    @Autowired
    private FlashcardSetRepository flashcardSetRepository;
    @Autowired
    private FlashcardSideRepository flashcardSideRepository;
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
    void testDeleteFlashcardSet(final GraphQlTester tester) {
        // put some data into the database
        final List<FlashcardSetEntity> sets = testUtils.populateFlashcardSetRepository(flashcardSetRepository, courseId);

        final UUID setToDelete = sets.get(0).getAssessmentId();

        final String query = """
                mutation($assessmentId: UUID!) {
                    deleteFlashcardSet(input: $assessmentId)
                }
                """;

        tester.document(query)
                .variable("assessmentId", setToDelete)
                .execute()
                .path("deleteFlashcardSet")
                .entity(UUID.class)
                .isEqualTo(setToDelete);

        assertThat(flashcardSetRepository.count()).isEqualTo(1);
        assertThat(flashcardSetRepository.findById(setToDelete)).isEmpty();

        assertThat(flashcardRepository.count()).isEqualTo(2);
        assertThat(flashcardSideRepository.count()).isEqualTo(4);
    }

    @Test
    @Transactional
    void testDeleteFlashcardSetNotExisting(final GraphQlTester tester) {
        // put some data into the database
        testUtils.populateFlashcardSetRepository(flashcardSetRepository, courseId);

        final UUID setToDelete = UUID.randomUUID();

        final String query = """
                mutation($assessmentId: UUID!) {
                    deleteFlashcardSet(input: $assessmentId)
                }
                """;

        tester.document(query)
                .variable("assessmentId", setToDelete)
                .execute()
                .errors()
                .expect(x -> x.getExtensions().get("exception").equals("EntityNotFoundException"));
    }
}
