package io.github.tiagoiwamoto.awsintegration;

import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

import static io.awspring.cloud.sqs.annotation.SqsListenerAcknowledgementMode.ON_SUCCESS;

@Service
public class SqsEntrypoint {

    @SqsListener(value = "my-queue-name", acknowledgementMode = ON_SUCCESS)
    public void receiveMessage(@Payload Message<Map<String, Object>> message) {
        System.out.println("Received message: " + message);
    }
}
