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

import java.net.URI;

@Configuration
@Slf4j
public class LambdaConfig {

    @Value(value = "${app.aws.region:sa-east-1}")
    private String region;
    @Value(value = "${app.aws.environment:local}")
    private String environment;
    @Value(value = "${app.aws.retry.attempts:3}")
    private Integer maxRetryAttempts;

    @Bean
    public LambdaClient lambdaClient() {
        var lambdaBuilder = LambdaClient.builder()
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .retryPolicy(RetryPolicy.builder()
                                .numRetries(maxRetryAttempts)
                                .build())
                        .build())
                .region(Region.of(region));

        if("local".equals(environment)) {
            lambdaBuilder.endpointOverride(URI.create("http://localhost:4566"));
            lambdaBuilder.credentialsProvider(AnonymousCredentialsProvider.create());
        }else{
            lambdaBuilder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        var lambda = lambdaBuilder.build();
        log.info("Cliente Lambda criado com sucesso. Region: {}, Environment: {}, Endpoint: {}",
                region, environment, lambda.serviceClientConfiguration().endpointOverride().get());

        return lambda;
    }

}
