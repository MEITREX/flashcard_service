package de.unistuttgart.iste.meitrex.flashcard_service.validation;

import de.unistuttgart.iste.meitrex.generated.dto.*;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FlashcardValidator {

    /**
     * validation function for newly created flashcard inputs
     * @param flashcardInput new flashcard input
     */
    public void validateCreateFlashcardInput(CreateFlashcardInput flashcardInput) {
        validateSides(flashcardInput.getSides());
    }

    /**
     * validation of flashcard set inputs
     * @param flashcardSetInput a Flashcard-Set input
     */
    public void validateCreateFlashcardSetInput(CreateFlashcardSetInput flashcardSetInput) {
        for (CreateFlashcardInput flashcardInput: flashcardSetInput.getFlashcards()) {
            validateCreateFlashcardInput(flashcardInput);
        }
    }


    /**
     * validation function for updated flashcard inputs
     * @param input an updated flashcard input
     */
    public void validateUpdateFlashcardInput(UpdateFlashcardInput input) {
        validateSides(input.getSides());
    }

    /**
     * validates if flashcard has at least one side labeled as question and at least one side labeled as answer
     * @param flashcardSideInputs list of flashcard sides
     * @throws ValidationException if flashcard has no question or no answer or a side that is neither a question nor an answer
     */
    private void validateSides(List<FlashcardSideInput> flashcardSideInputs) {
        boolean hasQuestion = false;
        boolean hasAnswer = false;
        for (FlashcardSideInput flashcardInputSide: flashcardSideInputs) {

            if (flashcardInputSide.getIsQuestion()){
                hasQuestion = true;
            }
            if (flashcardInputSide.getIsAnswer()){
                hasAnswer = true;
            }

            // finds invalid flashcard side
            if ((!flashcardInputSide.getIsQuestion()) && (!flashcardInputSide.getIsAnswer())){
                throw new ValidationException("Flashcard side must must be at least a question or an answer");
            }
        }

        if (!hasQuestion || !hasAnswer) {
            throw new ValidationException("Flashcards must have at least one Question Side and one Answer Side");
        }
    }

}
