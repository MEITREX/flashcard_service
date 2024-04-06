package de.unistuttgart.iste.meitrex.flashcard_service.api;

import de.unistuttgart.iste.meitrex.common.testutil.*;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser.UserRoleInCourse;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity.FlashcardSetEntity;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.mapper.FlashcardMapper;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.repository.FlashcardSetRepository;
import de.unistuttgart.iste.meitrex.flashcard_service.test_utils.TestUtils;
import de.unistuttgart.iste.meitrex.generated.dto.FlashcardSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.List;
import java.util.UUID;

import static de.unistuttgart.iste.meitrex.common.testutil.TestUsers.userWithMembershipInCourseWithId;
import static org.assertj.core.api.Assertions.assertThat;

@GraphQlApiTest
@TablesToDelete({"flashcard_side", "flashcard", "flashcard_set"})
class QueryFlashcardSetTest {


    @Autowired
    private FlashcardSetRepository flashcardSetRepository;

    private final UUID courseId = UUID.randomUUID();

    @InjectCurrentUserHeader
    private final LoggedInUser loggedInUser = userWithMembershipInCourseWithId(courseId, UserRoleInCourse.STUDENT);

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private FlashcardMapper mapper;

    @Test
    void testFlashcardSetsByAssessmentIds(final GraphQlTester tester) {
        final List<FlashcardSetEntity> expectedSets = testUtils.populateFlashcardSetRepository(flashcardSetRepository, courseId);

        final String query = """
                query($ids: [UUID!]!) {
                    findFlashcardSetsByAssessmentIds(assessmentIds: $ids) {
                        assessmentId,
                        courseId,
                        flashcards {
                            itemId,
                            sides {
                              label,
                              isQuestion,
                              isAnswer,
                              text
                            }
                        }
                    }
                }
                """;

        final List<FlashcardSet> actualSets = tester.document(query)
                .variable("ids", expectedSets.stream().map(FlashcardSetEntity::getAssessmentId))
                .execute()
                .path("findFlashcardSetsByAssessmentIds")
                .entityList(FlashcardSet.class)
                .hasSize(2)
                .get();

        assertThat(actualSets)
                .containsExactlyInAnyOrder(expectedSets
                        .stream()
                        .map(x -> mapper.flashcardSetEntityToDto(x))
                        .toArray(FlashcardSet[]::new));
    }

    @Test
    void testQueryFlashcardSetsNonExisting(final GraphQlTester graphQlTester) {
        final String query = """
                query($ids: [UUID!]!) {
                    findFlashcardSetsByAssessmentIds(assessmentIds: $ids) {
                        assessmentId
                    }
                }
                """;

        graphQlTester.document(query)
                .variable("ids", List.of(UUID.randomUUID(), UUID.randomUUID()))
                .execute()
                .path("findFlashcardSetsByAssessmentIds[0]").valueIsNull()
                .path("findFlashcardSetsByAssessmentIds[1]").valueIsNull();
    }
}
