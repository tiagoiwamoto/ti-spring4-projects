package br.com.tiagoiwamoto.iggyproducer.config;

import org.apache.iggy.client.blocking.tcp.IggyTcpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IggyConfig {


    @Value( "${iggy.host}")
    private String host;

    @Bean
    public IggyTcpClient iggyClient(){
        return IggyTcpClient.builder()
                .host(host)
                .port(5100)
                .credentials("iggy", "iggy")
                .buildAndLogin();
    }
}
