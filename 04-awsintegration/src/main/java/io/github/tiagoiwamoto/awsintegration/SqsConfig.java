package io.github.tiagoiwamoto.awsintegration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.io.UncheckedIOException;

@Configuration
@Slf4j
public class SqsConfig {

    @Value(value = "${app.aws.region:sa-east-1}")
    private String region;
    @Value(value = "${app.aws.environment:local}")
    private String environment;
    @Value(value = "${app.aws.sqs.retry.attempts:3}")
    private Integer maxRetryAttempts;

    @Bean
    public SqsClient sqsClient() {
        var sqsBuilder = SqsClient.builder()
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .retryPolicy(RetryPolicy.builder()
                                .numRetries(maxRetryAttempts)
                                .build())
                        .build())
                .region(Region.of(region));

        if("local".equals(environment)) {
            sqsBuilder.endpointOverride(URI.create("http://localhost:4566"));
            sqsBuilder.credentialsProvider(AnonymousCredentialsProvider.create());
        }else{
            sqsBuilder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        var sqs = sqsBuilder.build();
        Optional<URI> endpoint = sqs.serviceClientConfiguration().endpointOverride();
        log.info("Cliente Sqs criado com sucesso. Region: {}, Environment: {}, Endpoint: {}",
                region, environment, endpoint.map(URI::toString).orElse("(default)"));

        return sqs;
    }

    // Bean for Jackson ObjectMapper configured to support Java records and Java Time types
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Support parameter names (useful for records), and Java Time module
        mapper.registerModule(new ParameterNamesModule())
                .registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }

    // Simple SQS message converter that (de)serializes message bodies to/from Java records using Jackson
    @Bean
    public SqsMessageConverter sqsMessageConverter(ObjectMapper objectMapper) {
        return new SqsMessageConverter(objectMapper);
    }

    /**
     * Utility class to convert SQS message bodies to/from POJOs / records using Jackson.
     * Keep as a simple helper so consumers can inject and use it in listeners / processors.
     */
    public static class SqsMessageConverter {
        private final ObjectMapper objectMapper;

        public SqsMessageConverter(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        public <T> T fromMessageBody(String body, Class<T> clazz) {
            try {
                return objectMapper.readValue(body, clazz);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to deserialize SQS message body", e);
            }
        }

        public String toMessageBody(Object payload) {
            try {
                return objectMapper.writeValueAsString(payload);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize payload to SQS message body", e);
            }
        }
    }

}
