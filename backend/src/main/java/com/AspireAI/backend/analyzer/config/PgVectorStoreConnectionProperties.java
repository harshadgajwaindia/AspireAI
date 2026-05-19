package com.AspireAI.backend.analyzer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.ai.vectorstore.pgvector")
public class PgVectorStoreConnectionProperties {

    private String jdbcUrl;
    private String username;
    private String password;
    private Integer dimensions = 768;
    private Boolean initializeSchema = true;
}
