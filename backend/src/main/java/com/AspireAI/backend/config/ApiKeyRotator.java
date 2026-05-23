package com.AspireAI.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import com.google.genai.Client;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ApiKeyRotator {

    private final ChatModel defaultChatModel;
    private final ChatClient defaultChatClient;

    @Value("${GOOGLE_GENAI_API_KEYS:}")
    private String rawKeys;

    private final List<ChatClient> chatClients = new ArrayList<>();
    private final AtomicInteger index = new AtomicInteger(0);

    @PostConstruct
    public void init() {
        if (rawKeys == null || rawKeys.trim().isEmpty()) {
            log.info("No GOOGLE_GENAI_API_KEYS env variable found. ApiKeyRotator will fall back to the default ChatClient.");
            return;
        }

        String[] keys = rawKeys.split(",");
        for (String key : keys) {
            String trimmedKey = key.trim();
            if (!trimmedKey.isEmpty()) {
                try {
                    Client genAiClient = new Client.Builder()
                            .apiKey(trimmedKey)
                            .build();

                    GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                            .model("gemini-flash-latest")
                            .build();

                    GoogleGenAiChatModel model = GoogleGenAiChatModel.builder()
                            .genAiClient(genAiClient)
                            .defaultOptions(options)
                            .build();

                    ChatClient client = ChatClient.builder(model)
                            .defaultSystem("""
                                You are an expert technical recruiter and career coach
                                specializing in Indian CS placement preparation.
                                Always respond with valid JSON when asked.
                                Be precise, evidence-based, and concise.
                                """)
                            .build();

                    chatClients.add(client);
                    log.info("Successfully configured rotated API key: ...{}", trimmedKey.substring(Math.max(0, trimmedKey.length() - 4)));
                } catch (Exception e) {
                    log.error("Failed to configure rotated API key: {}", e.getMessage());
                }
            }
        }
        log.info("ApiKeyRotator initialized with {} active keys in the rotation pool.", chatClients.size());
    }

    public ChatClient getChatClient() {
        if (chatClients.isEmpty()) {
            return defaultChatClient;
        }
        int nextIndex = index.getAndIncrement() % chatClients.size();
        if (nextIndex < 0) nextIndex = 0; // handle overflow
        log.info("Using rotated ChatClient key index {}", nextIndex);
        return chatClients.get(nextIndex);
    }
}
