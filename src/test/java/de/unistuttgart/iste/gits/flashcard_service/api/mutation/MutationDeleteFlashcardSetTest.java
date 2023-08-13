package de.unistuttgart.iste.gits.flashcard_service.api.mutation;

import de.unistuttgart.iste.gits.common.resource_markdown.ResourceMarkdownEntity;
import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.common.testutil.TablesToDelete;

import de.unistuttgart.iste.gits.flashcard_service.persistence.dao.FlashcardEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.dao.FlashcardSetEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.dao.FlashcardSideEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardRepository;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardSetRepository;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardSideRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@GraphQlApiTest
@TablesToDelete({"flashcard_side", "flashcard", "flashcard_set"})
public class MutationDeleteFlashcardSetTest {

    @Autowired
    private FlashcardSetRepository flashcardSetRepository;
    @Autowired
    private FlashcardSideRepository flashcardSideRepository;
    @Autowired
    private FlashcardRepository flashcardRepository;

    @Test
    @Transactional
    @Commit
    void testDeleteFlashcardSet(GraphQlTester graphQlTester) {
        // Create and save the flashcard to be deleted
        FlashcardSetEntity flashcardSet = new FlashcardSetEntity();
        flashcardSet.setAssessmentId(UUID.randomUUID());

        FlashcardEntity flashcard = new FlashcardEntity();
        flashcard.setSetId(UUID.randomUUID());

        List<FlashcardSideEntity> sides = new ArrayList<>();

        // Create the first side and set its properties
        FlashcardSideEntity side1 = new FlashcardSideEntity();
        side1.setLabel("Side 1");
        side1.setQuestion(true);
        side1.setText(new ResourceMarkdownEntity("Question 1"));
        side1.setFlashcard(flashcard);
        side1 = flashcardSideRepository.save(side1);
        sides.add(side1);


        // Create the second side and set its properties
        FlashcardSideEntity side2 = new FlashcardSideEntity();
        side2.setLabel("Side 2");
        side2.setQuestion(false);
        side2.setText(new ResourceMarkdownEntity("Answer 1"));
        side2.setFlashcard(flashcard);
        side2 = flashcardSideRepository.save(side2);
        sides.add(side2);


        // Set the sides of the flashcard directly
        flashcard.setSides(sides);
        flashcard = flashcardRepository.save(flashcard);

        flashcardSet.setFlashcards(new ArrayList<>());
        flashcardSet.getFlashcards().add(flashcard);
        flashcardSet= flashcardSetRepository.save(flashcardSet);

        UUID flashcardSetId = flashcardSet.getAssessmentId();

        // Perform the delete operation
        String query = """
          mutation ($input: UUID!) {
            deleteFlashcardSet(input: $input)
          }
        """;

        // Execute the delete mutation query
        UUID deletedFlashcardSetId = graphQlTester.document(query)
                .variable("input", flashcardSetId)
                .execute()
                .path("deleteFlashcardSet")
                .entity(UUID.class)
                .get();

        // Assert that the flashcard ID matches the deleted ID
        assertThat(deletedFlashcardSetId, is(flashcardSetId));

        // Verify that the flashcard no longer exists in the repository
        Optional<FlashcardSetEntity> deletedFlashcardSet = flashcardSetRepository.findById(flashcardSetId);
        assertThat(deletedFlashcardSet.isPresent(), is(false));

        // Verify that no flashcards exist in the repository
        List<FlashcardEntity> flashcards = flashcardRepository.findAll();
        assertThat(flashcards.isEmpty(), is(true));

    }
}
