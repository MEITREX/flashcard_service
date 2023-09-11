package de.unistuttgart.iste.gits.flashcard_service.test_utils;

import de.unistuttgart.iste.gits.common.resource_markdown.ResourceMarkdownEntity;
import de.unistuttgart.iste.gits.flashcard_service.persistence.entity.*;
import de.unistuttgart.iste.gits.flashcard_service.persistence.repository.FlashcardSetRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class TestUtils {
    /**
     * Helper method which creates some flashcard sets and saves them to the repository.
     * @param repo The repository to save the flashcard sets to.
     * @return Returns the created flashcard sets.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW) // Required so the data is committed to the repo before the
    // rest of the test method which calls this method is executed.
    public List<FlashcardSetEntity> populateFlashcardSetRepository(FlashcardSetRepository repo) {
        FlashcardSetEntity set1 = new FlashcardSetEntity();
        set1.setAssessmentId(UUID.randomUUID());

        FlashcardEntity flashcard1 = new FlashcardEntity();
        flashcard1.setId(UUID.randomUUID());
        flashcard1.setParentSet(set1);
        flashcard1.setSides(List.of(
                new FlashcardSideEntity(UUID.randomUUID(),
                        new ResourceMarkdownEntity("Question 1"),
                        "Question Label",
                        true,
                        false,
                        flashcard1),
                new FlashcardSideEntity(UUID.randomUUID(),
                        new ResourceMarkdownEntity("Answer 1"),
                        "Answer Label",
                        false,
                        true,
                        flashcard1)
        ));

        FlashcardEntity flashcard2 = new FlashcardEntity();
        flashcard2.setId(UUID.randomUUID());
        flashcard2.setParentSet(set1);
        flashcard2.setSides(List.of(
                new FlashcardSideEntity(UUID.randomUUID(),
                        new ResourceMarkdownEntity("Question 2"),
                        "Question Label",
                        true,
                        false,
                        flashcard2),
                new FlashcardSideEntity(UUID.randomUUID(),
                        new ResourceMarkdownEntity("Answer 2"),
                        "Answer Label",
                        false,
                        true,
                        flashcard2)
        ));

        set1.setFlashcards(List.of(flashcard1, flashcard2));

        FlashcardSetEntity set2 = new FlashcardSetEntity();
        set2.setAssessmentId(UUID.randomUUID());

        FlashcardEntity flashcard3 = new FlashcardEntity();
        flashcard3.setId(UUID.randomUUID());
        flashcard3.setParentSet(set2);
        flashcard3.setSides(List.of(
                new FlashcardSideEntity(UUID.randomUUID(),
                        new ResourceMarkdownEntity("Question 3"),
                        "Question Label",
                        true,
                        false,
                        flashcard3),
                new FlashcardSideEntity(UUID.randomUUID(),
                        new ResourceMarkdownEntity("Answer 3"),
                        "Answer Label",
                        false,
                        true,
                        flashcard3)
        ));

        FlashcardEntity flashcard4 = new FlashcardEntity();
        flashcard4.setId(UUID.randomUUID());
        flashcard4.setParentSet(set2);
        flashcard4.setSides(List.of(
                new FlashcardSideEntity(UUID.randomUUID(),
                        new ResourceMarkdownEntity("Question 4"),
                        "Question Label",
                        true,
                        false,
                        flashcard4),
                new FlashcardSideEntity(UUID.randomUUID(),
                        new ResourceMarkdownEntity("Answer 4"),
                        "Answer Label",
                        false,
                        true,
                        flashcard4)
        ));

        set2.setFlashcards(List.of(flashcard3, flashcard4));

        return repo.saveAll(List.of(set1, set2));
    }
}
