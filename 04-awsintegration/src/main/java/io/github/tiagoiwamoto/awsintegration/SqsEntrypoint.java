package io.github.tiagoiwamoto.awsintegration;

import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.Map;

import static io.awspring.cloud.sqs.annotation.SqsListenerAcknowledgementMode.ON_SUCCESS;

@Service
@RequiredArgsConstructor
public class SqsEntrypoint {

    private final SqsClient sqsClient;
    private final SqsTemplate sqsTemplate;

    @SqsListener(value = "my-queue-name", acknowledgementMode = ON_SUCCESS)
    public void receiveMessage(@Payload Message<Map<String, Object>> message) {
        System.out.println("Received message: " + message);
    }

    public void sendMessage() {
        sqsTemplate.send("my-queue-name",
            Map.of("event", "GREETING", "text", "Hello, SQS!"));

        sqsClient.sendMessage(builder ->
            builder
                .queueUrl("http://localhost:4566/000000000000/my-queue-name")
                .messageBody("{\"event\":\"GREETING\",\"text\":\"Hello, SQS!\"}")
        );
    }
}
