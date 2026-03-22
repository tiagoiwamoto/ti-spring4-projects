package br.com.tiagoiwamoto.iggyproducer.entrypoint;

import lombok.RequiredArgsConstructor;
import org.apache.iggy.client.blocking.tcp.IggyTcpClient;
import org.apache.iggy.consumergroup.Consumer;
import org.apache.iggy.identifier.StreamId;
import org.apache.iggy.identifier.TopicId;
import org.apache.iggy.message.Message;
import org.apache.iggy.message.Partitioning;
import org.apache.iggy.message.PolledMessages;
import org.apache.iggy.message.PollingStrategy;
import org.apache.iggy.topic.CompressionAlgorithm;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;

@RestController
@RequestMapping(path = "/iggy")
@RequiredArgsConstructor
public class IwaRest {

    private final IggyTcpClient iggyTcpClient;

    static final String STREAM_NAME = "iggy-sample";
    static final StreamId STREAM_ID = StreamId.of(STREAM_NAME);
    static final String TOPIC_NAME = "iggy-topic";
    static final TopicId TOPIC_ID = TopicId.of(TOPIC_NAME);

    @PostMapping
    public String create(@RequestBody Object message){

        if(iggyTcpClient.streams().getStream(STREAM_ID).isEmpty()){
            iggyTcpClient.streams().createStream(STREAM_NAME);
        }
        if(iggyTcpClient.topics().getTopic(STREAM_ID, TOPIC_ID).isEmpty()){
            iggyTcpClient.topics().createTopic(
                    STREAM_ID,
                    1L,
                    CompressionAlgorithm.None,
                    BigInteger.ZERO,
                    BigInteger.ZERO,
                    empty(),
                    TOPIC_NAME);
        }

        Partitioning partitioning = Partitioning.partitionId(0L);
        ObjectMapper mapper = new ObjectMapper();
        iggyTcpClient.messages().sendMessages(
                STREAM_ID,
                TOPIC_ID,
                partitioning,
                List.of(Message.of(mapper.writeValueAsString(message))));
        return "OK";
    }

    void consumer(){
        Consumer consumer = Consumer.of(0L);
        BigInteger offset = BigInteger.ZERO;
        while (true) {
            PolledMessages polledMessages = iggyTcpClient.messages().pollMessages(
                    STREAM_ID,
                    TOPIC_ID,
                    Optional.of(0L),
                    consumer,
                    PollingStrategy.offset(offset),
                    10L,
                    false);

            for (Message msg : polledMessages.messages()) {
                String payload = new String(msg.payload(), StandardCharsets.UTF_8);
                System.out.printf("Offset: %d, Payload: %s%n", offset, payload);
            }
            offset = offset.add(BigInteger.valueOf(polledMessages.messages().size()));
        }

    }

    @EventListener(ApplicationReadyEvent.class)
    void init(){
        Map<String, Object> pojoPayload = Map.of("name", "Igwan");
        this.create(pojoPayload);
        this.consumer();
    }

}
