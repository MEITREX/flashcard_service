package de.unistuttgart.iste.gits.flashcard_service.api.mutation;

import de.unistuttgart.iste.gits.common.resource_markdown.ResourceMarkdownEntity;
import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.common.testutil.TablesToDelete;
import de.unistuttgart.iste.gits.flashcard_service.persistence.dao.FlashcardEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.dao.FlashcardSideEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardRepository;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardSideRepository;
import de.unistuttgart.iste.gits.generated.dto.Flashcard;
import de.unistuttgart.iste.gits.generated.dto.FlashcardSide;
import de.unistuttgart.iste.gits.generated.dto.ResourceMarkdown;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.annotation.Commit;
import java.util.ArrayList;
import java.util.UUID;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@GraphQlApiTest
@TablesToDelete({"flashcard_side", "flashcard", "flashcard_set"})
class MutationUpdateFlashcardTest {
    @Autowired
    private FlashcardRepository flashcardRepository;

    @Autowired
    private FlashcardSideRepository flashcardSideRepository;

    @Test
    @Transactional
    @Commit
    void testUpdateFlashcard(GraphQlTester graphQlTester) {
        // Create and save the initial flashcard with sides
        FlashcardEntity flashcard = new FlashcardEntity();
        flashcard.setSetId(UUID.randomUUID());

        List<FlashcardSideEntity> sides = new ArrayList<>();

        // Create the first side and set its properties
        FlashcardSideEntity side1 = new FlashcardSideEntity();
        side1.setLabel("Side 1");
        side1.setQuestion(true);
        side1.setText(new ResourceMarkdownEntity("Question 1"));
        side1.setFlashcard(flashcard);
        flashcardSideRepository.save(side1); // Save the side using the side repository
        sides.add(side1);

        // Create the second side and set its properties
        FlashcardSideEntity side2 = new FlashcardSideEntity();
        side2.setLabel("Side 2");
        side2.setQuestion(false);
        side2.setText(new ResourceMarkdownEntity("Answer 1"));
        side2.setFlashcard(flashcard);
        flashcardSideRepository.save(side2); // Save the side using the side repository
        sides.add(side2);

        // Set the sides of the flashcard directly
        flashcard.setSides(sides);

        flashcard= flashcardRepository.save(flashcard);

        // Perform the update operation
        UUID flashcardId = flashcard.getId();
        String query = """
          mutation ($id: UUID!) {
            updateFlashcard(input: {
              id: $id
              sides: [
                {
                  label: "New_Side 1",
                  isQuestion: true,
                  text: {text: "New_Question 1"}
                },
                {
                  label: "New_Side 2",
                  isQuestion: false,
                  text: {text: "New_Answer 1 [[media/b4f2e8d1-a1e6-4834-8f5d-ac793f18e854]]"}
                }
              ]
            }) {
              id
              sides {
                label
                isQuestion
                text {text, referencedMediaRecordIds}
              }
            }
          }
        """;

        // Execute the update mutation query
        Flashcard updatedFlashcard = graphQlTester.document(query)
                .variable("id", flashcardId)
                .execute()
                .path("updateFlashcard")
                .entity(Flashcard.class)
                .get();

        // Assert the updated flashcard properties
        assertThat(updatedFlashcard.getId(), is(flashcardId));

        List<FlashcardSide> updatedSides = updatedFlashcard.getSides();
        assertThat(updatedSides, hasSize(2));

        FlashcardSide updatedSide1 = updatedSides.get(0);
        assertThat(updatedSide1.getLabel(), is("New_Side 1"));
        assertThat(updatedSide1.getIsQuestion(), is(true));
        assertThat(updatedSide1.getText().getText(), is("New_Question 1"));
        assertThat(updatedSide1.getText().getReferencedMediaRecordIds().isEmpty(), is(true));

        FlashcardSide updatedSide2 = updatedSides.get(1);
        assertThat(updatedSide2.getLabel(), is("New_Side 2"));
        assertThat(updatedSide2.getIsQuestion(), is(false));
        assertThat(updatedSide2.getText().getText(),
                is("New_Answer 1 [[media/b4f2e8d1-a1e6-4834-8f5d-ac793f18e854]]"));
        assertThat(updatedSide2.getText().getReferencedMediaRecordIds(),
                contains(equalTo(UUID.fromString("b4f2e8d1-a1e6-4834-8f5d-ac793f18e854"))));
    }
}
