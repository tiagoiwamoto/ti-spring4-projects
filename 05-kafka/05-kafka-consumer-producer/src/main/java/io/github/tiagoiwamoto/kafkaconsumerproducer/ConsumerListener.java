package io.github.tiagoiwamoto.kafkaconsumerproducer;

import io.github.tiagoiwamoto.avro.User;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ConsumerListener {

    @KafkaListener(topics = "${kafka.topic.user:users}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ConsumerRecord<String, User> record) {
        User user = record.value();
        System.out.println("Consumed user: " + user);
    }
}

