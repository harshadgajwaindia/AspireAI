package com.AspireAI.backend.analyzer.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;


@Configuration
@EnableConfigurationProperties(PgVectorStoreConnectionProperties.class)
@ConditionalOnProperty(prefix = "spring.ai.vectorstore.pgvector", name = "jdbc-url")
public class PgVectorStoreConfiguration implements DisposableBean {

    private volatile HikariDataSource pgVectorPool;

    @Bean(name = "pgVectorJdbcTemplate")
    public JdbcTemplate pgVectorJdbcTemplate(PgVectorStoreConnectionProperties props) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(props.getJdbcUrl());
        ds.setUsername(props.getUsername());
        ds.setPassword(props.getPassword());
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setPoolName("pgvector-pool");
        this.pgVectorPool = ds;
        return new JdbcTemplate(ds);
    }

    @Bean
    public VectorStore vectorStore(
            @Qualifier("pgVectorJdbcTemplate") JdbcTemplate jdbcTemplate,
            EmbeddingModel embeddingModel,
            PgVectorStoreConnectionProperties props) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(props.getDimensions() != null ? props.getDimensions() : 768)
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .initializeSchema(Boolean.TRUE.equals(props.getInitializeSchema()))
                .build();
    }

    @Override
    public void destroy() {
        if (pgVectorPool != null) {
            pgVectorPool.close();
        }
    }
}
