package br.com.tiagoiwamoto.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para o SqsEventHandler
 */
class SqsEventHandlerTest {

    private SqsEventHandler handler;
    private Context context;

    @BeforeEach
    void setUp() {
        handler = new SqsEventHandler();
        context = createMockContext();
    }

    @Test
    @DisplayName("Deve processar mensagem JSON com sucesso")
    void testProcessJsonMessage() {
        // Arrange
        SQSEvent event = createSQSEvent(
            "{\"type\":\"ORDER\",\"orderId\":\"12345\",\"amount\":100.00}"
        );

        // Act & Assert
        assertDoesNotThrow(() -> handler.handleRequest(event, context));
    }

    @Test
    @DisplayName("Deve processar mensagem de texto com sucesso")
    void testProcessTextMessage() {
        // Arrange
        SQSEvent event = createSQSEvent("Mensagem de texto simples");

        // Act & Assert
        assertDoesNotThrow(() -> handler.handleRequest(event, context));
    }

    @Test
    @DisplayName("Deve processar múltiplas mensagens")
    void testProcessMultipleMessages() {
        // Arrange
        SQSEvent event = new SQSEvent();
        List<SQSMessage> messages = new ArrayList<>();

        messages.add(createSQSMessage(
            "msg-1",
            "{\"type\":\"ORDER\",\"orderId\":\"123\"}"
        ));
        messages.add(createSQSMessage(
            "msg-2",
            "{\"type\":\"NOTIFICATION\",\"message\":\"Test\"}"
        ));
        messages.add(createSQSMessage(
            "msg-3",
            "{\"type\":\"UPDATE\",\"entityId\":\"456\"}"
        ));

        event.setRecords(messages);

        // Act & Assert
        assertDoesNotThrow(() -> handler.handleRequest(event, context));
    }

    @Test
    @DisplayName("Deve processar evento vazio")
    void testProcessEmptyEvent() {
        // Arrange
        SQSEvent event = new SQSEvent();
        event.setRecords(new ArrayList<>());

        // Act & Assert
        assertDoesNotThrow(() -> handler.handleRequest(event, context));
    }

    @Test
    @DisplayName("Deve processar mensagem com atributos")
    void testProcessMessageWithAttributes() {
        // Arrange
        SQSMessage message = createSQSMessage(
            "msg-1",
            "{\"type\":\"ORDER\",\"orderId\":\"123\"}"
        );

        Map<String, String> attributes = new HashMap<>();
        attributes.put("ApproximateReceiveCount", "1");
        attributes.put("SentTimestamp", "1234567890");
        message.setAttributes(attributes);

        SQSEvent event = new SQSEvent();
        List<SQSMessage> messages = new ArrayList<>();
        messages.add(message);
        event.setRecords(messages);

        // Act & Assert
        assertDoesNotThrow(() -> handler.handleRequest(event, context));
    }

    // Helper methods

    private SQSEvent createSQSEvent(String messageBody) {
        SQSEvent event = new SQSEvent();
        List<SQSMessage> messages = new ArrayList<>();
        messages.add(createSQSMessage("test-message-id", messageBody));
        event.setRecords(messages);
        return event;
    }

    private SQSMessage createSQSMessage(String messageId, String body) {
        SQSMessage message = new SQSMessage();
        message.setMessageId(messageId);
        message.setBody(body);
        message.setReceiptHandle("test-receipt-handle");
        message.setAwsRegion("us-east-1");
        message.setEventSource("aws:sqs");
        message.setEventSourceArn("arn:aws:sqs:us-east-1:123456789012:test-queue");
        message.setAttributes(new HashMap<>());
        message.setMessageAttributes(new HashMap<>());
        return message;
    }

    private Context createMockContext() {
        return new Context() {
            @Override
            public String getAwsRequestId() {
                return "test-request-id";
            }

            @Override
            public String getLogGroupName() {
                return "/aws/lambda/test-function";
            }

            @Override
            public String getLogStreamName() {
                return "test-stream";
            }

            @Override
            public String getFunctionName() {
                return "test-function";
            }

            @Override
            public String getFunctionVersion() {
                return "1";
            }

            @Override
            public String getInvokedFunctionArn() {
                return "arn:aws:lambda:us-east-1:123456789012:function:test-function";
            }

            @Override
            public com.amazonaws.services.lambda.runtime.CognitoIdentity getIdentity() {
                return null;
            }

            @Override
            public com.amazonaws.services.lambda.runtime.ClientContext getClientContext() {
                return null;
            }

            @Override
            public int getRemainingTimeInMillis() {
                return 300000;
            }

            @Override
            public int getMemoryLimitInMB() {
                return 512;
            }

            @Override
            public com.amazonaws.services.lambda.runtime.LambdaLogger getLogger() {
                return new com.amazonaws.services.lambda.runtime.LambdaLogger() {
                    @Override
                    public void log(String message) {
                        System.out.println(message);
                    }

                    @Override
                    public void log(byte[] message) {
                        System.out.println(new String(message));
                    }
                };
            }
        };
    }
}

