package de.unistuttgart.iste.gits.flashcard_service.api;
import de.unistuttgart.iste.gits.common.testutil.TablesToDelete;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardRepository;
import de.unistuttgart.iste.gits.generated.dto.Flashcard;
import de.unistuttgart.iste.gits.generated.dto.FlashcardSet;
import de.unistuttgart.iste.gits.generated.dto.FlashcardSide;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.annotation.Commit;
import java.util.UUID;
import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import java.util.List;
@GraphQlApiTest
@TablesToDelete({"flashcard_side", "flashcard", "flashcard_set"})
class MutationCreateFlashcardSetTest {

    @Test
    @Transactional
    @Commit
    void testCreateFlashcardSet(GraphQlTester tester) {
        UUID assessmentId = UUID.randomUUID();
        String query = """
                
            mutation ($assessmentId: UUID!){
              createFlashcardSet(input: {
                 assessmentId: $assessmentId
                 flashcards: [
                    {
                      sides: [
                        {
                          label: "Side 1",
                          isQuestion: true,
                          text: {text: "Question 1"}
                        },
                        {
                          label: "Side 2",
                          isQuestion: false,
                          text: {text: "Answer 1"}
                        }
                      ]
                    },
                    {
                      sides: [
                        {
                          label: "Side 1",
                          isQuestion: true,
                          text: {text: "Question 2"}
                        },
                        {
                          label: "Side 2",
                          isQuestion: false,
                          text: {text: "Answer 2"}
                        }
                      ]
                    }
                 
              ]
            })
            {
               assessmentId
               flashcards
               {
                   sides
                   {
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

        FlashcardSet createdFlashcardSet = tester.document(query)
                .variable("assessmentId", assessmentId)
                .execute()
                .path("createFlashcardSet").entity(FlashcardSet.class).get();

        // check that returned Flashcard is correct
        assertThat(createdFlashcardSet.getAssessmentId(), is(notNullValue()));


        List<Flashcard> flashcards = createdFlashcardSet.getFlashcards();
        assertThat(flashcards, hasSize(2));

        Flashcard flashcard1 = flashcards.get(0);
        assertThat(flashcard1.getSides(), hasSize(2));

        FlashcardSide flashcard1Side1 = flashcard1.getSides().get(0);
        assertThat(flashcard1Side1.getLabel(), is("Side 1"));
        assertThat(flashcard1Side1.getIsQuestion(), is(true));
        assertThat(flashcard1Side1.getText().getText(), is("Question 1"));
        assertThat(flashcard1Side1.getText().getReferencedMediaRecordIds().isEmpty(), is(true));

        FlashcardSide flashcard1Side2 = flashcard1.getSides().get(1);
        assertThat(flashcard1Side2.getLabel(), is("Side 2"));
        assertThat(flashcard1Side2.getIsQuestion(), is(false));
        assertThat(flashcard1Side2.getText().getText(), is("Answer 1"));
        assertThat(flashcard1Side2.getText().getReferencedMediaRecordIds().isEmpty(), is(true));

        Flashcard flashcard2 = flashcards.get(1);
        assertThat(flashcard2.getSides(), hasSize(2));

        FlashcardSide flashcard2Side1 = flashcard2.getSides().get(0);
        assertThat(flashcard2Side1.getLabel(), is("Side 1"));
        assertThat(flashcard2Side1.getIsQuestion(), is(true));
        assertThat(flashcard2Side1.getText().getText(), is("Question 2"));
        assertThat(flashcard2Side1.getText().getReferencedMediaRecordIds().isEmpty(), is(true));

        FlashcardSide flashcard2Side2 = flashcard2.getSides().get(1);
        assertThat(flashcard2Side2.getLabel(), is("Side 2"));
        assertThat(flashcard2Side2.getIsQuestion(), is(false));
        assertThat(flashcard2Side2.getText().getReferencedMediaRecordIds().isEmpty(), is(true));

    }
}
