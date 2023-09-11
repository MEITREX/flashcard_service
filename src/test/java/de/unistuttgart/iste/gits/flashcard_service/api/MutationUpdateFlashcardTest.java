package de.unistuttgart.iste.gits.flashcard_service.api;

import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.common.testutil.TablesToDelete;
import de.unistuttgart.iste.gits.flashcard_service.persistence.entity.FlashcardSetEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardRepository;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardSetRepository;
import de.unistuttgart.iste.gits.flashcard_service.test_utils.TestUtils;
import de.unistuttgart.iste.gits.generated.dto.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.annotation.Commit;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@GraphQlApiTest
@TablesToDelete({"flashcard_side", "flashcard", "flashcard_set"})
class MutationUpdateFlashcardTest {

    @Autowired
    private FlashcardSetRepository flashcardSetRepository;

    @Autowired
    FlashcardRepository flashcardRepository;

    @Autowired
    private TestUtils testUtils;

    @Test
    @Transactional
    @Commit
    void testUpdateFlashcard(GraphQlTester tester) {
        List<FlashcardSetEntity> set = testUtils.populateFlashcardSetRepository(flashcardSetRepository);

        UUID setOfFlashcard = set.get(0).getAssessmentId();
        // Perform the update operation
        UUID flashcardToUpdate = set.get(0).getFlashcards().stream().findAny().orElseThrow().getId();

        String query = """
          mutation ($assessmentId: UUID!, $flashcardId: UUID!) {
            mutateFlashcardSet(assessmentId: $assessmentId) {
              updateFlashcard(input: {
                id: $flashcardId,
                sides: [
                  {
                    label: "New_Side 1",
                    isQuestion: true,
                    isAnswer: false,
                    text: {text: "New_Question 1"}
                  },
                  {
                    label: "New_Side 2",
                    isQuestion: false,
                    isAnswer: true,
                    text: {text: "New_Answer 1 [[media/b4f2e8d1-a1e6-4834-8f5d-ac793f18e854]]"}
                  }
                ]
              }) {
                id
                sides {
                  label
                  isQuestion
                  isAnswer
                  text {text, referencedMediaRecordIds}
                }
              }
            }
          }
        """;

        // Execute the update mutation query
        Flashcard updatedFlashcard = tester.document(query)
                .variable("assessmentId", setOfFlashcard)
                .variable("flashcardId", flashcardToUpdate)
                .execute()
                .path("mutateFlashcardSet.updateFlashcard")
                .entity(Flashcard.class)
                .get();

        // Assert the values of the data returned by the updateFlashcard mutation
        assertThat(updatedFlashcard.getId()).isEqualTo(flashcardToUpdate);
        assertThat(updatedFlashcard.getSides()).containsExactlyInAnyOrder(
                new FlashcardSide(new ResourceMarkdown("New_Question 1", Collections.emptyList()),
                        "New_Side 1",
                        true, false),
                new FlashcardSide(new ResourceMarkdown("New_Answer 1 [[media/b4f2e8d1-a1e6-4834-8f5d-ac793f18e854]]",
                                                       List.of(UUID.fromString("b4f2e8d1-a1e6-4834-8f5d-ac793f18e854"))),
                        "New_Side 2",
                        false, true)
        );

        assertThat(flashcardRepository.count()).isEqualTo(4);
    }
}
