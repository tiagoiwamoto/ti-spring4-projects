package io.github.tiagoiwamoto.kafkaconsumerproducer;

import io.github.tiagoiwamoto.avro.User;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    @Value("${spring.kafka.properties.security.protocol:}")
    private String securityProtocol;

    @Value("${spring.kafka.properties.ssl.keystore.location:}")
    private String sslKeystoreLocation;

    @Value("${spring.kafka.properties.ssl.keystore.password:}")
    private String sslKeystorePassword;

    @Value("${spring.kafka.properties.ssl.key.password:}")
    private String sslKeyPassword;

    @Value("${spring.kafka.properties.ssl.truststore.location:}")
    private String sslTruststoreLocation;

    @Value("${spring.kafka.properties.ssl.truststore.password:}")
    private String sslTruststorePassword;

    @Bean
    public ProducerFactory<String, User> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // Use Confluent Avro serializer (ensure dependency at runtime)
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer");

        // Schema registry
        props.put("schema.registry.url", schemaRegistryUrl);

        // SSL properties (if provided)
        if (securityProtocol != null && !securityProtocol.isBlank()) {
            props.put("security.protocol", securityProtocol);
        }
        if (sslKeystoreLocation != null && !sslKeystoreLocation.isBlank()) {
            props.put("ssl.keystore.location", sslKeystoreLocation);
        }
        if (sslKeystorePassword != null && !sslKeystorePassword.isBlank()) {
            props.put("ssl.keystore.password", sslKeystorePassword);
        }
        if (sslKeyPassword != null && !sslKeyPassword.isBlank()) {
            props.put("ssl.key.password", sslKeyPassword);
        }
        if (sslTruststoreLocation != null && !sslTruststoreLocation.isBlank()) {
            props.put("ssl.truststore.location", sslTruststoreLocation);
        }
        if (sslTruststorePassword != null && !sslTruststorePassword.isBlank()) {
            props.put("ssl.truststore.password", sslTruststorePassword);
        }

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, User> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, User> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // Confluent Avro deserializer
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer");

        // Schema registry
        props.put("schema.registry.url", schemaRegistryUrl);
        // Avro deserializer specific: return type as SpecificRecord
        props.put("specific.avro.reader", true);

        // SSL
        if (securityProtocol != null && !securityProtocol.isBlank()) {
            props.put("security.protocol", securityProtocol);
        }
        if (sslKeystoreLocation != null && !sslKeystoreLocation.isBlank()) {
            props.put("ssl.keystore.location", sslKeystoreLocation);
        }
        if (sslKeystorePassword != null && !sslKeystorePassword.isBlank()) {
            props.put("ssl.keystore.password", sslKeystorePassword);
        }
        if (sslKeyPassword != null && !sslKeyPassword.isBlank()) {
            props.put("ssl.key.password", sslKeyPassword);
        }
        if (sslTruststoreLocation != null && !sslTruststoreLocation.isBlank()) {
            props.put("ssl.truststore.location", sslTruststoreLocation);
        }
        if (sslTruststorePassword != null && !sslTruststorePassword.isBlank()) {
            props.put("ssl.truststore.password", sslTruststorePassword);
        }

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, User> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, User> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
