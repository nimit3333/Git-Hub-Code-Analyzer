package com.code.analyzer.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // ✅ connection timeout
                .responseTimeout(Duration.ofSeconds(6))             // ✅ response timeout
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(6))
                                .addHandlerLast(new WriteTimeoutHandler(6))
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}