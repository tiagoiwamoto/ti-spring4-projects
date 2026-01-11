package br.com.tiagoiwamoto.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Lambda handler para processar mensagens do AWS SQS
 * Configurado para compilação nativa com GraalVM
 */
public class SqsEventHandler implements RequestHandler<SQSEvent, Void> {

    private static final Logger logger = LoggerFactory.getLogger(SqsEventHandler.class);
    private final ObjectMapper objectMapper;
    private final MessageProcessor messageProcessor;

    public SqsEventHandler() {
        this.objectMapper = new ObjectMapper();
        this.messageProcessor = new MessageProcessor();
    }

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        logger.info("Recebendo evento SQS com {} mensagens", event.getRecords().size());

        List<String> processedMessages = new ArrayList<>();
        List<String> failedMessages = new ArrayList<>();

        for (SQSMessage message : event.getRecords()) {
            try {
                processMessage(message, context);
                processedMessages.add(message.getMessageId());
                logger.info("Mensagem processada com sucesso: {}", message.getMessageId());
            } catch (Exception e) {
                failedMessages.add(message.getMessageId());
                logger.error("Erro ao processar mensagem: {}", message.getMessageId(), e);
                // Em produção, considere enviar para uma DLQ (Dead Letter Queue)
            }
        }

        logger.info("Processamento concluído - Sucesso: {}, Falhas: {}",
                    processedMessages.size(), failedMessages.size());

        return null;
    }

    private void processMessage(SQSMessage message, Context context) {
        String messageBody = message.getBody();
        String messageId = message.getMessageId();

        logger.info("Processando mensagem ID: {}", messageId);
        logger.debug("Corpo da mensagem: {}", messageBody);

        // Obter atributos da mensagem
        message.getAttributes().forEach((key, value) ->
            logger.debug("Atributo {}: {}", key, value)
        );

        // Processar a mensagem
        messageProcessor.process(messageBody, messageId, context);
    }

    /**
     * Main method para testes locais (não usado no Lambda)
     */
    public static void main(String[] args) {
        logger.info("SQS Lambda Handler inicializado");
        logger.info("Esta função deve ser executada no AWS Lambda");
    }
}

