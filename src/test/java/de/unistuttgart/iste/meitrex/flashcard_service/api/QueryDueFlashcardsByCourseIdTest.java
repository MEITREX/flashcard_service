package de.unistuttgart.iste.meitrex.flashcard_service.api;

import de.unistuttgart.iste.meitrex.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.meitrex.common.testutil.InjectCurrentUserHeader;
import de.unistuttgart.iste.meitrex.common.testutil.TablesToDelete;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser.UserRoleInCourse;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity.FlashcardEntity;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity.FlashcardProgressDataEntity;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity.FlashcardSetEntity;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.repository.FlashcardProgressDataRepository;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.repository.FlashcardSetRepository;
import de.unistuttgart.iste.meitrex.flashcard_service.test_utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static de.unistuttgart.iste.meitrex.common.testutil.TestUsers.userWithMembershipInCourseWithId;

@GraphQlApiTest
@TablesToDelete({"flashcard_side", "flashcard", "flashcard_set"})
class QueryDueFlashcardsByCourseIdTest {

    @Autowired
    private FlashcardSetRepository flashcardSetRepository;

    @Autowired
    private FlashcardProgressDataRepository flashcardProgressDataRepository;

    private final UUID courseId = UUID.randomUUID();

    @InjectCurrentUserHeader
    private final LoggedInUser loggedInUser = userWithMembershipInCourseWithId(courseId, UserRoleInCourse.STUDENT);

    @Autowired
    private TestUtils testUtils;

    /**
     * Tests that the query dueFlashcardsByCourseId
     * returns all due flashcards for a course
     */
    @Test
    void testDueFlashcardsByCourseId(final GraphQlTester tester) {
        final List<FlashcardSetEntity> sets = testUtils.populateFlashcardSetRepository(flashcardSetRepository, courseId);
        final List<FlashcardEntity> flashCardsSet1 = sets.get(0).getFlashcards();
        final List<FlashcardEntity> flashCardsSet2 = sets.get(1).getFlashcards();

        // create user progress data
        flashcardProgressDataRepository.saveAll(List.of(
                // due
                FlashcardProgressDataEntity.builder()
                        .primaryKey(new FlashcardProgressDataEntity.PrimaryKey(flashCardsSet1.get(0).getId(), loggedInUser.getId()))
                        .nextLearn(OffsetDateTime.now().minusDays(2))
                        .build(),
                // not due
                FlashcardProgressDataEntity.builder()
                        .primaryKey(new FlashcardProgressDataEntity.PrimaryKey(flashCardsSet1.get(1).getId(), loggedInUser.getId()))
                        .nextLearn(null)
                        .build(),
                // due
                FlashcardProgressDataEntity.builder()
                        .primaryKey(new FlashcardProgressDataEntity.PrimaryKey(flashCardsSet2.get(0).getId(), loggedInUser.getId()))
                        .nextLearn(OffsetDateTime.now().minusDays(1))
                        .build(),
                // not due
                FlashcardProgressDataEntity.builder()
                        .primaryKey(new FlashcardProgressDataEntity.PrimaryKey(flashCardsSet2.get(1).getId(), loggedInUser.getId()))
                        .nextLearn(OffsetDateTime.now().plusDays(1))
                        .build()
        ));

        final String query = """
                query($courseId: UUID!) {
                  dueFlashcardsByCourseId(courseId: $courseId) {
                    id
                  }
                }
                """;

        tester.document(query)
                .variable("courseId", courseId)
                .execute()
                .path("dueFlashcardsByCourseId[*].id")
                .entityList(UUID.class)
                .hasSize(2)
                .contains(flashCardsSet1.get(0).getId(), flashCardsSet2.get(0).getId());
    }
}
