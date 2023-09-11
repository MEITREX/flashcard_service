package de.unistuttgart.iste.gits.flashcard_service.controller;

import de.unistuttgart.iste.gits.common.event.ContentChangeEvent;
import de.unistuttgart.iste.gits.flashcard_service.service.FlashcardService;
import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST Controller Class listening to a dapr Topic.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SubscriptionController {

    private final FlashcardService flashcardService;

    @Topic(name = "content-changes", pubsubName = "gits")
    @PostMapping(path = "/flashcard-service/content-changes-pubsub")
    public Mono<Void> updateAssociation(@RequestBody CloudEvent<ContentChangeEvent> cloudEvent) {


        return Mono.fromRunnable(() -> {
            try {
                flashcardService.deleteFlashcardSetIfContentIsDeleted(cloudEvent.getData());
            } catch (Exception e) {
                log.error("Error while processing content change event", e);
            }
        });
    }

}
