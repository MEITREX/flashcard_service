package de.unistuttgart.iste.gits.flashcard_service.api;

import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.common.testutil.TablesToDelete;
import de.unistuttgart.iste.gits.flashcard_service.persistence.entity.*;
import de.unistuttgart.iste.gits.flashcard_service.persistence.mapper.FlashcardMapper;
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
class MutationCreateFlashcardTest {

    @Autowired
    private FlashcardSetRepository flashcardSetRepository;

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Autowired
    private FlashcardMapper flashcardMapper;

    @Autowired
    private TestUtils testUtils;

    @Test
    @Transactional
    @Commit
    void testCreateFlashcard(GraphQlTester graphQlTester) {
        List<FlashcardSetEntity> sets = testUtils.populateFlashcardSetRepository(flashcardSetRepository);

        String query = """
          mutation ($setId: UUID!) {
            mutateFlashcardSet(assessmentId: $setId) {
              createFlashcard(input: {
                sides: [
                {
                  label: "Side 11",
                  isQuestion: true,
                  isAnswer: false,
                  text: {text: "Question 1"}
                },
                {
                  label: "Side 21",
                  isQuestion: false,
                  isAnswer: true,
                  text: {text: "Answer 1"}
                }
                ]
              }) {
                id
                sides {
                  label
                  isQuestion
                  isAnswer
                  text {
                    text,
                    referencedMediaRecordIds
                  }
                }
              }
            }
          }
          """;

        UUID setId = sets.get(0).getAssessmentId();

        // Execute the mutation and extract the created flashcard
        Flashcard createdFlashcard = graphQlTester.document(query)
                .variable("setId", setId)
                .execute()
                .path("mutateFlashcardSet.createFlashcard")
                .entity(Flashcard.class)
                .get();

        // Assert the values of the data returned by the createFlashcard mutation
        assertThat(createdFlashcard.getId()).isNotNull();
        assertThat(createdFlashcard.getSides()).containsExactlyInAnyOrder(
                new FlashcardSide(new ResourceMarkdown("Question 1", Collections.emptyList()), "Side 11", true, false),
                new FlashcardSide(new ResourceMarkdown("Answer 1", Collections.emptyList()), "Side 21", false, true)
        );

        // Assert that the flashcard was added to the set in the repository
        assertThat(flashcardSetRepository
                .getReferenceById(setId)
                .getFlashcards()
                .stream()
                .map(FlashcardEntity::getId))
                .contains(createdFlashcard.getId());

        FlashcardEntity flashcardFromRepo = flashcardRepository.getReferenceById(createdFlashcard.getId());

        assertThat(flashcardFromRepo.getParentSet().getAssessmentId()).isEqualTo(setId);
        assertThat(flashcardFromRepo.getSides().get(0))
                .returns("Side 11", FlashcardSideEntity::getLabel)
                .returns(true, FlashcardSideEntity::isQuestion)
                .returns("Question 1", side -> side.getText().getText());
        assertThat(flashcardFromRepo.getSides().get(1))
                .returns("Side 21", FlashcardSideEntity::getLabel)
                .returns(false, FlashcardSideEntity::isQuestion)
                .returns("Answer 1", side -> side.getText().getText());
    }

    @Test
    void testCreateInvalidFlashcard(GraphQlTester graphQlTester) {
        List<FlashcardSetEntity> sets = testUtils.populateFlashcardSetRepository(flashcardSetRepository);

        String query = """
          mutation ($setId: UUID!) {
            mutateFlashcardSet(assessmentId: $setId) {
              createFlashcard(input: {
                sides: [
                {
                  label: "Side 11",
                  isQuestion: false,
                  isAnswer: true,
                  text: {text: "Question 1"}
                },
                {
                  label: "Side 21",
                  isQuestion: false,
                  isAnswer: true,
                  text: {text: "Answer 1"}
                }
                ]
              }) {
                id
                sides {
                  label
                  isQuestion
                  isAnswer
                  text {
                    text,
                    referencedMediaRecordIds
                  }
                }
              }
            }
          }
          """;

        UUID setId = sets.get(0).getAssessmentId();

        // Execute the mutation and check for expected errors
        graphQlTester.document(query)
                .variable("setId", setId)
                .execute()
                .errors()
                .expect(responseError ->  responseError.getMessage() != null && responseError.getMessage().toLowerCase().contains("flashcards must have at least one question side and one answer side"));


    }

    @Test
    void testCreateInvalidFlashcardSide(GraphQlTester graphQlTester) {
        List<FlashcardSetEntity> sets = testUtils.populateFlashcardSetRepository(flashcardSetRepository);

        String query = """
          mutation ($setId: UUID!) {
            mutateFlashcardSet(assessmentId: $setId) {
              createFlashcard(input: {
                sides: [
                {
                  label: "Side 11",
                  isQuestion: true,
                  isAnswer: false,
                  text: {text: "Question 1"}
                },
                {
                  label: "Side 21",
                  isQuestion: false,
                  isAnswer: false,
                  text: {text: "Answer 1"}
                }
                ]
              }) {
                id
                sides {
                  label
                  isQuestion
                  isAnswer
                  text {
                    text,
                    referencedMediaRecordIds
                  }
                }
              }
            }
          }
          """;

        UUID setId = sets.get(0).getAssessmentId();

        // Execute the mutation and check for expected errors
        graphQlTester.document(query)
                .variable("setId", setId)
                .execute()
                .errors()
                .expect(responseError ->  responseError.getMessage() != null && responseError.getMessage().toLowerCase().contains("flashcard side must must be at least a question or an answer"));


    }
}
