package de.unistuttgart.iste.gits.flashcard_service.api.mutation.query;

import de.unistuttgart.iste.gits.common.resource_markdown.ResourceMarkdownEntity;
import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.common.testutil.TablesToDelete;
import de.unistuttgart.iste.gits.flashcard_service.persistence.dao.FlashcardEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.dao.FlashcardSetEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.dao.FlashcardSideEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardRepository;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardSetRepository;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardSideRepository;
import de.unistuttgart.iste.gits.generated.dto.Flashcard;
import de.unistuttgart.iste.gits.generated.dto.FlashcardSet;
import de.unistuttgart.iste.gits.generated.dto.FlashcardSide;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;




@GraphQlApiTest
@TablesToDelete({"flashcard_side", "flashcard", "flashcard_set"})
public class QueryByAssessmentIdTest {


    @Autowired
    private FlashcardSetRepository flashcardSetRepository;
    @Autowired
    private FlashcardSideRepository flashcardSideRepository;
    @Autowired
    private FlashcardRepository flashcardRepository;


    @Test
    void testFlashcardSetsByAssessmentIds(GraphQlTester graphQlTester) {
        // Create test data


        UUID assessmentId1 = UUID.randomUUID();
        UUID assessmentId2 = UUID.randomUUID();

        FlashcardSetEntity set1 = createFlashcardSet(assessmentId1);
        FlashcardSetEntity set2 = createFlashcardSet(assessmentId2);

        FlashcardEntity flashcard1 = createFlashcard(set1.getAssessmentId());
        FlashcardEntity flashcard2 = createFlashcard(set2.getAssessmentId());

        FlashcardSideEntity side1 = createFlashcardSide(flashcard1, "Side 1", true, "Question 1");
        FlashcardSideEntity side2 = createFlashcardSide(flashcard1, "Side 2", false, "Answer 1");
        FlashcardSideEntity side3 = createFlashcardSide(flashcard2, "Side 1", true, "Question 2");
        FlashcardSideEntity side4 = createFlashcardSide(flashcard2, "Side 2", false, "Answer 2");

        flashcard1.setSides(Arrays.asList(side1, side2));
        flashcard2.setSides(Arrays.asList(side3, side4));

        flashcard1= flashcardRepository.save(flashcard1);
        flashcard2= flashcardRepository.save(flashcard2);

        set1.setFlashcards(Arrays.asList(flashcard1));
        set2.setFlashcards(Arrays.asList(flashcard2));

        set1 = flashcardSetRepository.save(set1);
        set2 = flashcardSetRepository.save(set2);

        // Prepare input assessment IDs
        List<UUID> assessmentIds = Arrays.asList(assessmentId1, assessmentId2);

        // Prepare expected result
        List<FlashcardSetEntity> expectedResult = Arrays.asList(set1, set2);

        // Execute the query
        String query = """
            query($assessmentIds: [UUID!]!) {
              flashcardSetsByAssessmentIds(assessmentIds: $assessmentIds) {
                assessmentId
                flashcards {
                  sides {
                    label
                    isQuestion
                    text {
                        text,
                        referencedMediaRecordIds
                    }
                  }
                }
              }
            }
        """;

        List<FlashcardSet> actualResult = graphQlTester.document(query)
                .variable("assessmentIds", assessmentIds)
                .execute()
                .path("flashcardSetsByAssessmentIds")
                .entityList(FlashcardSet.class)
                .get();

        assertEquals(expectedResult.size(), actualResult.size());
        for (int i = 0; i < expectedResult.size(); i++) {
            FlashcardSetEntity expectedSet = expectedResult.get(i);
            FlashcardSet actualDto = actualResult.get(i);

            assertEquals(expectedSet.getAssessmentId(), actualDto.getAssessmentId());
            assertEquals(expectedSet.getFlashcards().size(), actualDto.getFlashcards().size());

            // Compare flashcards field by field
            for (int j = 0; j < expectedSet.getFlashcards().size(); j++) {
                FlashcardEntity expectedFlashcard = expectedSet.getFlashcards().get(j);
                Flashcard actualFlashcard = actualDto.getFlashcards().get(j);

                // Compare flashcard sides field by field
                assertEquals(expectedFlashcard.getSides().size(), actualFlashcard.getSides().size());
                for (int k = 0; k < expectedFlashcard.getSides().size(); k++) {
                    FlashcardSideEntity expectedSide = expectedFlashcard.getSides().get(k);
                    FlashcardSide actualSide = actualFlashcard.getSides().get(k);

                    assertEquals(expectedSide.getLabel(), actualSide.getLabel());
                    assertEquals(expectedSide.isQuestion(), actualSide.getIsQuestion());
                    assertEquals(expectedSide.getText().getText(), actualSide.getText().getText());
                    assertEquals(expectedSide.getText().getReferencedMediaRecordIds(),
                                 actualSide.getText().getReferencedMediaRecordIds());

                }
            }
        }
    }

    private FlashcardSetEntity createFlashcardSet(UUID assessmentId) {
        FlashcardSetEntity set = new FlashcardSetEntity();
        set.setAssessmentId(assessmentId);
        set = flashcardSetRepository.save(set);

        return set;
    }

    private FlashcardEntity createFlashcard(UUID setId) {
        FlashcardEntity flashcard = new FlashcardEntity();
        flashcard.setSetId(setId);
        flashcard = flashcardRepository.save(flashcard);

        return flashcard;
    }



    private FlashcardSideEntity createFlashcardSide(FlashcardEntity flashcard, String label, boolean isQuestion, String text) {
        FlashcardSideEntity side = new FlashcardSideEntity();
        side.setId(UUID.randomUUID());
        side.setFlashcard(flashcard);
        side.setLabel(label);
        side.setQuestion(isQuestion);
        side.setText(new ResourceMarkdownEntity(text));
        side = flashcardSideRepository.save(side);

        return side;
    }
}
