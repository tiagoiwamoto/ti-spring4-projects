package io.github.tiagoiwamoto.kafkaconsumerproducer;

import io.github.tiagoiwamoto.avro.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProducerService {

    private final KafkaTemplate<String, User> kafkaTemplate;
    private final String topic;

    public ProducerService(KafkaTemplate<String, User> kafkaTemplate, @Value("${kafka.topic.user:users}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void sendUser(User user) {
        kafkaTemplate.send(topic, user.getId().toString(), user);
    }
}
