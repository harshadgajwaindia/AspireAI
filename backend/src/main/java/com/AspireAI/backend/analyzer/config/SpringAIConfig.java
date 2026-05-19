package com.AspireAI.backend.analyzer.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import java.time.Duration;

@Configuration
@EnableCaching 
public class SpringAIConfig {

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return builder -> {
            JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
            factory.setReadTimeout(Duration.ofMinutes(5));
            builder.requestFactory(factory);
        };
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem("""
                    You are an expert technical recruiter and career coach
                    specializing in Indian CS placement preparation.
                    Always respond with valid JSON when asked.
                    Be precise, evidence-based, and concise.
                    """)
                .build();
    }


    @Bean
    public RestClient githubRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeaders(headers -> {
                    headers.set(HttpHeaders.ACCEPT, "application/vnd.github+json");
                    headers.set("X-GitHub-Api-Version", "2022-11-28");
                })
                .build();
    }
}