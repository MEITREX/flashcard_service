package de.unistuttgart.iste.gits.flashcard_service.api.mutation;

import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.common.testutil.TablesToDelete;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardRepository;
import de.unistuttgart.iste.gits.generated.dto.Flashcard;
import de.unistuttgart.iste.gits.generated.dto.FlashcardSide;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.annotation.Commit;
import java.util.List;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@GraphQlApiTest
@TablesToDelete({"flashcard_side", "flashcard", "flashcard_set"})
class MutationCreateFlashcardTest {

    @Test
    @Transactional
    @Commit
    void testCreateFlashcard(GraphQlTester graphQlTester) {
        UUID setId = UUID.randomUUID();
        String query = """
          mutation ($setId: UUID!) {
            createFlashcard(input: {
              setId: $setId
              sides: [
              {
                  label: "Side 11",
                  isQuestion: true,
                  text: {text: "Question 1"}
              },
              {
                 label: "Side 21",
                 isQuestion: false,
                 text: {text: "Answer 1"}
              }
              ]
            }) {
               id
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
    """;

       // Execute the mutation and extract the created flashcard
        Flashcard createdFlashcard = graphQlTester.document(query)
                .variable("setId", setId)
                .execute()
                .path("createFlashcard")
                .entity(Flashcard.class)
                .get();

        // Assert the created flashcard's ID is not null
        assertThat(createdFlashcard.getId(), is(notNullValue()));

        // Assert the sides of the created flashcard
        List<FlashcardSide> sides = createdFlashcard.getSides();
        assertThat(sides, hasSize(2));

        FlashcardSide side1 = sides.get(0);
        assertThat(side1.getLabel(), is("Side 11"));
        assertThat(side1.getIsQuestion(), is(true));
        assertThat(side1.getText().getText(), is("Question 1"));
        assertThat(side1.getText().getReferencedMediaRecordIds().isEmpty(), is(true));

        FlashcardSide side2 = sides.get(1);
        assertThat(side2.getLabel(), is("Side 21"));
        assertThat(side2.getIsQuestion(), is(false));
        assertThat(side2.getText().getText(), is("Answer 1"));
        assertThat(side2.getText().getReferencedMediaRecordIds().isEmpty(), is(true));

    }
}
