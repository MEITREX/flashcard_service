package de.unistuttgart.iste.meitrex.flashcard_service.api;

import de.unistuttgart.iste.meitrex.common.testutil.*;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser.UserRoleInCourse;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity.FlashcardEntity;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity.FlashcardSetEntity;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.mapper.FlashcardMapper;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.repository.FlashcardSetRepository;
import de.unistuttgart.iste.meitrex.flashcard_service.test_utils.TestUtils;
import de.unistuttgart.iste.meitrex.generated.dto.Flashcard;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.List;
import java.util.UUID;

import static de.unistuttgart.iste.meitrex.common.testutil.TestUsers.userWithMembershipInCourseWithId;
import static org.assertj.core.api.Assertions.assertThat;

@GraphQlApiTest
@TablesToDelete({"flashcard_side", "flashcard", "flashcard_set"})
class QueryFlashcardTest {

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
    @Transactional
    void testQueryFlashcardsByIds(final GraphQlTester tester) {
        final List<FlashcardSetEntity> expectedSets = testUtils.populateFlashcardSetRepository(flashcardSetRepository, courseId);
        System.out.println( ("testId"+expectedSets.get(0).getFlashcards().get(0).getItemId()));
        System.out.println( ("testId"+expectedSets.get(1).getFlashcards().get(1).getItemId()));
        final List<FlashcardEntity> flashcardsToQuery = List.of(
                expectedSets.get(0).getFlashcards().get(0),
                expectedSets.get(1).getFlashcards().get(1)
        );
        List<UUID>ids=List.of(expectedSets.get(0).getFlashcards().get(0).getItemId(),expectedSets.get(1).getFlashcards().get(1).getItemId());
        System.out.println(ids);
        final String query = """
                query($ids: [UUID!]!) {
                  flashcardsByIds(itemIds: $ids) {
                    itemId
                    sides {
                      label
                      isQuestion
                      isAnswer
                      text
                    }
                  }
                }
                """;

        final List<Flashcard> actualFlashcards = tester.document(query)
                .variable("ids", flashcardsToQuery.stream().map(FlashcardEntity::getItemId))
                .execute()
                .path("flashcardsByIds")
                .entityList(Flashcard.class)
                .get();

        assertThat(actualFlashcards)
                .containsExactlyInAnyOrder(
                        flashcardsToQuery.stream().map(x -> mapper.entityToDto(x)).toArray(Flashcard[]::new));
    }
}
