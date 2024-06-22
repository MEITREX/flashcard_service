package de.unistuttgart.iste.meitrex.flashcard_service.service;

import de.unistuttgart.iste.meitrex.common.dapr.TopicPublisher;
import de.unistuttgart.iste.meitrex.common.event.ContentChangeEvent;
import de.unistuttgart.iste.meitrex.common.event.CrudOperation;
import de.unistuttgart.iste.meitrex.common.exception.IncompleteEventMessageException;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.entity.FlashcardSetEntity;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.mapper.FlashcardMapper;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.repository.FlashcardRepository;
import de.unistuttgart.iste.meitrex.flashcard_service.persistence.repository.FlashcardSetRepository;
import de.unistuttgart.iste.meitrex.flashcard_service.validation.FlashcardValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FlashcardServiceTest {
    private final FlashcardRepository flashcardRepository = Mockito.mock(FlashcardRepository.class);
    private final FlashcardSetRepository flashcardSetRepository = Mockito.mock(FlashcardSetRepository.class);
    private final FlashcardMapper flashcardMapper = new FlashcardMapper(new ModelMapper());
    private final FlashcardValidator flashcardValidator = new FlashcardValidator();

    private final TopicPublisher topicPublisher = Mockito.mock(TopicPublisher.class);
    private final FlashcardService flashcardService = new FlashcardService(flashcardRepository, flashcardSetRepository, flashcardMapper, flashcardValidator, topicPublisher);

    @Test
    void removeContentIds() {
        //init
        UUID assessmentId = UUID.randomUUID();
        OffsetDateTime lastLearned = OffsetDateTime.now();
        FlashcardSetEntity flashcardSetEntity = FlashcardSetEntity.builder()
                .assessmentId(assessmentId)
                .flashcards(new ArrayList<>())
                .lastLearned(lastLearned)
                .build();

        ContentChangeEvent contentChangeEvent = ContentChangeEvent.builder()
                .contentIds(List.of(assessmentId))
                .operation(CrudOperation.DELETE)
                .build();

        //mock repository
        when(flashcardSetRepository.findAllById(contentChangeEvent.getContentIds())).thenReturn(List.of(flashcardSetEntity));

        // invoke method under test
        assertDoesNotThrow(() -> flashcardService.deleteFlashcardSetIfContentIsDeleted(contentChangeEvent));
        verify(flashcardSetRepository, times(1)).deleteAllById(any());
    }

    @Test
    void removeContentIdsWithNoIdsToBeRemovedTest() {
        //init
        UUID assessmentId = UUID.randomUUID();

        ContentChangeEvent contentChangeEvent = ContentChangeEvent.builder()
                .contentIds(List.of(assessmentId))
                .operation(CrudOperation.DELETE)
                .build();

        //mock repository
        when(flashcardSetRepository.findAllById(contentChangeEvent.getContentIds())).thenReturn(new ArrayList<FlashcardSetEntity>());

        // invoke method under test
        assertDoesNotThrow(() -> flashcardService.deleteFlashcardSetIfContentIsDeleted(contentChangeEvent));

        verify(flashcardSetRepository, times(1)).deleteAllById(any());
    }

    @Test
    void removeContentIdsInvalidInputTest() {
        //init
        UUID assessmentId = UUID.randomUUID();

        ContentChangeEvent emptyListDto = ContentChangeEvent.builder()
                .contentIds(List.of())
                .operation(CrudOperation.DELETE)
                .build();

        ContentChangeEvent nullListDto = ContentChangeEvent.builder()
                .contentIds(null)
                .operation(CrudOperation.DELETE)
                .build();

        ContentChangeEvent nullOperationDto = ContentChangeEvent.builder()
                .contentIds(List.of(assessmentId))
                .operation(null)
                .build();

        ContentChangeEvent creationEvent = ContentChangeEvent.builder()
                .contentIds(List.of(assessmentId))
                .operation(CrudOperation.CREATE)
                .build();

        ContentChangeEvent updateEvent = ContentChangeEvent.builder()
                .contentIds(List.of(assessmentId))
                .operation(CrudOperation.UPDATE)
                .build();

        List<ContentChangeEvent> events = List.of(emptyListDto, creationEvent, updateEvent);
        List<ContentChangeEvent> errorEvents = List.of(nullListDto, nullOperationDto);

        for (ContentChangeEvent event : events) {
            //invoke method under test
            assertDoesNotThrow(() -> flashcardService.deleteFlashcardSetIfContentIsDeleted(event));
            verify(flashcardSetRepository, never()).findAllById(any());
            verify(flashcardSetRepository, never()).deleteAllById(any());
        }

        for (ContentChangeEvent errorEvent : errorEvents) {
            //invoke method under test
            assertThrows(IncompleteEventMessageException.class, () -> flashcardService.deleteFlashcardSetIfContentIsDeleted(errorEvent));
            verify(flashcardSetRepository, never()).findAllById(any());
            verify(flashcardSetRepository, never()).deleteAllById(any());
        }

    }
}