package io.github.tiagoiwamoto._3httpcalls;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
public class HttpClientConfig {

    @Value("${swapi.baseurl}")
    private String swapiBaseUrl;
    @Value("${poke.baseurl}")
    private String pokeapiBaseUrl;

    @Value("${api.client.retry.attemps}")
    private Integer retryAttemps;
    @Value("${api.client.retry.jitter}")
    private Integer retryJitter;
    @Value("${api.client.retry.backoff.delay.milis}")
    private Long retryBackoff;
    @Value("${api.client.timout.milis}")
    private Long timoutMilis;


    @Bean
    public WebClient swapiWebClient() {
        return this.webClient(swapiBaseUrl);
    }

    @Bean
    public WebClient pokeWebClient() {
        return this.webClient(pokeapiBaseUrl);
    }

    @Bean
    SwapiPort swapiClient(WebClient swapiWebClient) {
        return httpServiceProxyFactory(swapiWebClient).createClient(SwapiPort.class);
    }

    @Bean
    PokePort pokeClient(WebClient pokeWebClient) {
        return httpServiceProxyFactory(pokeWebClient).createClient(PokePort.class);
    }

    private WebClient webClient(String baseUrl) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(timoutMilis))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(2000, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(2000, TimeUnit.MILLISECONDS)));

        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        return WebClient.builder()
                .baseUrl(baseUrl)
                .filter(errorHandlingFilter())
                .filter(retryFilter())
                .clientConnector(connector)
                .defaultHeader("Authorization", "Bearer ".concat("123456789"))
                .defaultHeader("Accept", "application/json")
                .build();
    }

    private HttpServiceProxyFactory httpServiceProxyFactory(WebClient webClient) {
        return HttpServiceProxyFactory.builder()
                        .exchangeAdapter(WebClientAdapter.create(webClient))
                        .build();
    }

    private ExchangeFilterFunction retryFilter() {
        return (request, next) -> next.exchange(request)
                .retryWhen(Retry.backoff(retryAttemps, Duration.ofMillis(retryBackoff)).jitter(retryJitter));
    }

    private ExchangeFilterFunction errorHandlingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                return clientResponse.bodyToMono(Map.class)
                        .flatMap(errorBody -> {
                            HttpStatusCode status = clientResponse.statusCode();

                            if (status.is4xxClientError()) {
                                return Mono.error(new HttpClientErrorException(
                                        status,
                                        "Client error: " + errorBody
                                ));
                            } else if (status.is5xxServerError()) {
                                return Mono.error(new HttpServerErrorException(
                                        status,
                                        "Server error: " + errorBody
                                ));
                            } else {
                                return Mono.error(new RuntimeException(
                                        "HTTP error: " + status + " - " + errorBody
                                ));
                            }
                        });
            }
            return Mono.just(clientResponse);
        });
    }

}
