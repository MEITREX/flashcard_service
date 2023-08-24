package de.unistuttgart.iste.gits.flashcard_service.dapr;

import de.unistuttgart.iste.gits.common.event.*;
import de.unistuttgart.iste.gits.flashcard_service.persistence.dao.FlashcardEntity;
import io.dapr.client.DaprClient;
import lombok.extern.slf4j.Slf4j;

/**
 * Component that takes care of publishing messages to a dapr Topic
 */
@Slf4j
public class TopicPublisher {

    private static final String PUBSUB_NAME = "gits";
    private static final String RESOURCE_UPDATE_TOPIC = "resource-update";

    private static final String USER_PROGRESS_LOG_TOPIC = "content-progressed";

    private final DaprClient client;

    public TopicPublisher(DaprClient client) {
        this.client = client;
    }

    /**
     * method used to publish dapr messages to a topic
     *
     * @param dto message
     */
    private void publishChanges(ResourceUpdateEvent dto) {
        log.info("publishing ResourceUpdateEvent: {}", dto);
        client.publishEvent(
                PUBSUB_NAME,
                RESOURCE_UPDATE_TOPIC,
                dto).block();
    }

    /**
     * method to take changes done to an entity and to transmit them to the dapr topic
     *
     * @param flashcardEntity changed entity
     * @param operation         type of CRUD operation performed on entity
     */
    public void notifyResourceChange(FlashcardEntity flashcardEntity, CrudOperation operation) {
        ResourceUpdateEvent dto = ResourceUpdateEvent.builder()
                .entityId(flashcardEntity.getId())
                .operation(operation).build();
        publishChanges(dto);
    }

    /**
     * Publishes a message to the dapr topic that a user has learned a flashcard
     *
     * @param userProgressLogEvent event to publish
     */
    public void notifyFlashcardSetLearned(UserProgressLogEvent userProgressLogEvent) {
        log.info("Publish UserProgressLogEvent: {}", userProgressLogEvent);
        client.publishEvent(PUBSUB_NAME, USER_PROGRESS_LOG_TOPIC, userProgressLogEvent).subscribe();
    }

}

