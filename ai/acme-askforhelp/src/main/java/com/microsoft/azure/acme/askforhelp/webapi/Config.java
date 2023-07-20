package com.microsoft.azure.acme.askforhelp.webapi;

import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import com.microsoft.azure.acme.askforhelp.webapi.common.AzureOpenAIClient;
import com.microsoft.azure.acme.askforhelp.webapi.service.ChatService;
import com.microsoft.azure.acme.askforhelp.webapi.common.vectorstore.SimpleMemoryVectorStore;
import com.microsoft.azure.acme.askforhelp.webapi.common.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.time.Duration;

@Configuration
public class Config {

    @Value("${azure.openai.embedding-deployment-id}")
    private String embeddingDeploymentId;

    @Value("${azure.openai.chat-deployment-id}")
    private String chatDeploymentId;

    @Value("${azure.openai.endpoint}")
    private String endpoint;

    @Value("${azure.openai.api-key}")
    private String apiKey;

    @Value("vector_store.json")
    private String vectorJsonFile;

    @Bean
    public AzureOpenAIClient AzureOpenAIClient() {
        var innerClient = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .retryOptions(new RetryOptions(new FixedDelayOptions(5, Duration.ofSeconds(1))))
            .buildClient();
        return new AzureOpenAIClient(innerClient, embeddingDeploymentId, chatDeploymentId);
    }

    @Bean
    public VectorStore vectorStore() throws IOException {
        return SimpleMemoryVectorStore.loadFromJsonFile(new ClassPathResource(vectorJsonFile).getFile());
    }
}
