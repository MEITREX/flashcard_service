package de.unistuttgart.iste.gits.flashcard_service.api;

import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.common.testutil.TablesToDelete;
import de.unistuttgart.iste.gits.generated.dto.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.annotation.Commit;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@GraphQlApiTest
@TablesToDelete({"flashcard_side", "flashcard", "flashcard_set"})
class MutationCreateFlashcardSetTest {

    @Test
    @Transactional
    @Commit
    void testCreateFlashcardSet(final GraphQlTester tester) {
        final UUID assessmentId = UUID.randomUUID();
        final UUID courseId = UUID.randomUUID();
        final String query =
            """    
            mutation ($courseId: UUID!, $assessmentId: UUID!){
                _internal_noauth_createFlashcardSet(courseId: $courseId, assessmentId: $assessmentId, input: {
                    flashcards: [
                        {
                            sides: [
                                {
                                    label: "Side 1",
                                    isQuestion: true,
                                    isAnswer: false,
                                    text: "Question 1"
                                },
                                {
                                    label: "Side 2",
                                    isQuestion: false,
                                    isAnswer: true,
                                    text: "Answer 1"
                                }
                            ]
                        },
                        {
                            sides: [
                                {
                                    label: "Side 1",
                                    isQuestion: true,
                                    isAnswer: false,
                                    text: "Question 2"
                                },
                                {
                                    label: "Side 2",
                                    isQuestion: false,
                                    isAnswer: true,
                                    text: "Answer 2"
                                }
                            ]
                        }
                    ]
                }
            )
            {
               assessmentId
               courseId
                 flashcards {
                     sides {
                     label
                     isQuestion
                     isAnswer
                     text
                 }
               }
            }
        }
        """;

        final FlashcardSet createdFlashcardSet = tester.document(query)
                .variable("assessmentId", assessmentId)
                .variable("courseId", courseId)
                .execute()
                .path("_internal_noauth_createFlashcardSet").entity(FlashcardSet.class).get();

        // check that returned Flashcard is correct
        assertThat(createdFlashcardSet.getAssessmentId(), is(assessmentId));
        assertThat(createdFlashcardSet.getCourseId(), is(courseId));


        final List<Flashcard> flashcards = createdFlashcardSet.getFlashcards();
        assertThat(flashcards, hasSize(2));

        final Flashcard flashcard1 = flashcards.get(0);
        assertThat(flashcard1.getSides(), hasSize(2));

        final FlashcardSide flashcard1Side1 = flashcard1.getSides().get(0);
        assertThat(flashcard1Side1.getLabel(), is("Side 1"));
        assertThat(flashcard1Side1.getIsQuestion(), is(true));
        assertThat(flashcard1Side1.getIsAnswer(), is(false));
        assertThat(flashcard1Side1.getText(), is("Question 1"));

        final FlashcardSide flashcard1Side2 = flashcard1.getSides().get(1);
        assertThat(flashcard1Side2.getLabel(), is("Side 2"));
        assertThat(flashcard1Side2.getIsQuestion(), is(false));
        assertThat(flashcard1Side2.getIsAnswer(), is(true));
        assertThat(flashcard1Side2.getText(), is("Answer 1"));

        final Flashcard flashcard2 = flashcards.get(1);
        assertThat(flashcard2.getSides(), hasSize(2));

        final FlashcardSide flashcard2Side1 = flashcard2.getSides().get(0);
        assertThat(flashcard2Side1.getLabel(), is("Side 1"));
        assertThat(flashcard2Side1.getIsQuestion(), is(true));
        assertThat(flashcard2Side1.getIsAnswer(), is(false));
        assertThat(flashcard2Side1.getText(), is("Question 2"));

        final FlashcardSide flashcard2Side2 = flashcard2.getSides().get(1);
        assertThat(flashcard2Side2.getLabel(), is("Side 2"));
        assertThat(flashcard2Side2.getIsQuestion(), is(false));
        assertThat(flashcard2Side2.getIsAnswer(), is(true));

    }
}
