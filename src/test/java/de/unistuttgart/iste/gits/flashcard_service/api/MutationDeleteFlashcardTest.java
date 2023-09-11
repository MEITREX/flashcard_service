package de.unistuttgart.iste.gits.flashcard_service.api;

import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.common.testutil.TablesToDelete;
import de.unistuttgart.iste.gits.flashcard_service.persistence.entity.FlashcardSetEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardRepository;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardSetRepository;
import de.unistuttgart.iste.gits.flashcard_service.test_utils.TestUtils;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.annotation.Commit;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@GraphQlApiTest
@TablesToDelete({"flashcard_side", "flashcard", "flashcard_set"})
class MutationDeleteFlashcardTest {

    @Autowired
    private FlashcardSetRepository flashcardSetRepository;

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Autowired
    private TestUtils testUtils;

    @Test
    @Transactional
    @Commit
    void testDeleteFlashcard(GraphQlTester tester) {
        // Create and save the flashcards to be deleted
        List<FlashcardSetEntity> expectedSets = testUtils.populateFlashcardSetRepository(flashcardSetRepository);

        String query = """
                mutation($assessmentId: UUID!, $flashcardId: UUID!) {
                    mutateFlashcardSet(assessmentId: $assessmentId) {
                        deleteFlashcard(id: $flashcardId)
                    }
                }
                """;

        UUID setToDeleteFrom = expectedSets.get(0).getAssessmentId();
        UUID flashcardToDelete = expectedSets.get(0).getFlashcards().stream().findAny().orElseThrow().getId();

        tester.document(query)
                .variable("assessmentId", setToDeleteFrom)
                .variable("flashcardId", flashcardToDelete)
                .execute()
                .path("mutateFlashcardSet.deleteFlashcard")
                .entity(UUID.class)
                .isEqualTo(flashcardToDelete);

        assertThat(flashcardRepository.count()).isEqualTo(3);

        // assert that the flashcard is missing from the flashcard set and other flashcard is still there
        assertThat(flashcardSetRepository.findById(setToDeleteFrom).orElseThrow().getFlashcards()).hasSize(1);
        assertThat(flashcardSetRepository.findById(setToDeleteFrom)
                .orElseThrow()
                .getFlashcards()
                .stream()
                .filter(x -> x.getId() == flashcardToDelete))
                .isEmpty();

        // assert that the flashcard is missing from the flashcard repository
        assertThat(flashcardRepository.count()).isEqualTo(3);
        assertThat(flashcardRepository.findById(flashcardToDelete)).isNotPresent();
    }

    @Test
    @Transactional
    void testDeleteFlashcardNotExisting(GraphQlTester tester) {
        // fill the repo with some data
        List<FlashcardSetEntity> expectedSets = testUtils.populateFlashcardSetRepository(flashcardSetRepository);

        UUID setToDeleteFrom = expectedSets.get(0).getAssessmentId();
        UUID nonexistantUUID = UUID.randomUUID();

        String query = """
                mutation($assessmentId: UUID!, $flashcardId: UUID!) {
                    mutateFlashcardSet(assessmentId: $assessmentId) {
                        deleteFlashcard(id: $flashcardId)
                    }
                }
                """;

        tester.document(query)
                .variable("assessmentId", setToDeleteFrom)
                .variable("flashcardId", nonexistantUUID)
                .execute()
                .errors()
                .expect(x -> x.getExtensions().get("exception").equals("EntityNotFoundException"));

        assertThat(flashcardSetRepository.count()).isEqualTo(2);
        assertThat(flashcardRepository.count()).isEqualTo(4);
    }
}
