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
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

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
        log.info("Cliente Sqs criado com sucesso. Region: {}, Environment: {}, Endpoint: {}",
                region, environment, sqs.serviceClientConfiguration().endpointOverride().get());

        return sqs;
    }

}
