package io.github.tiagoiwamoto.awsintegration;

import io.awspring.cloud.sns.core.SnsTemplate;
import io.awspring.cloud.sqs.annotation.SnsNotificationMessage;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.eventstream.MessageBuilder;

import java.util.Map;

import static io.awspring.cloud.sqs.annotation.SqsListenerAcknowledgementMode.ON_SUCCESS;

@Service
@RequiredArgsConstructor
public class SnsEntrypoint {

    private final SnsClient snsClient;
    private final SnsTemplate snsTemplate;

    public void sendMessage() {
        //Usando o spring
        snsTemplate.convertAndSend("arn:aws:sns:sa-east-1:123456789012:my-topic",
            Map.of("event", "GREETING", "text", "Hello, SNS!"),
            Map.of("contentType", "application/json")
        );

        //usando apenas o sdk
        snsClient.publish(builder ->
            builder
                .topicArn("arn:aws:sns:sa-east-1:123456789012:my-topic")
                .message("{\"event\":\"GREETING\",\"text\":\"Hello, SNS!\"}")
                .messageAttributes(Map.of(
                    "contentType",
                    software.amazon.awssdk.services.sns.model.MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue("application/json")
                        .build()
                ))
        );
    }
}
