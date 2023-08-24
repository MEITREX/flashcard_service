package de.unistuttgart.iste.gits.flashcard_service.test_config;

import de.unistuttgart.iste.gits.flashcard_service.dapr.TopicPublisher;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.ArgumentMatchers.any;

@TestConfiguration
public class MockTopicPublisherConfiguration {

    @Primary
    @Bean
    public TopicPublisher getTestTopicPublisher() {
        TopicPublisher mockPublisher = Mockito.mock(TopicPublisher.class);
        Mockito.doNothing().when(mockPublisher).notifyFlashcardSetLearned(any());
        Mockito.doNothing().when(mockPublisher).notifyResourceChange(any(), any());
        return mockPublisher;
    }
}
