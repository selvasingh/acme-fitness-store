package com.microsoft.azure.acme.askforhelp.webapi;

import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.microsoft.azure.acme.askforhelp.common.AzureOpenAIClient;
import com.microsoft.azure.acme.askforhelp.service.ChatService;
import com.microsoft.azure.acme.askforhelp.common.vectorstore.SimpleMemoryVectorStore;
import com.microsoft.azure.acme.askforhelp.common.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

//    @Value("${vector-store.file}")
//    private String vectorJsonFile;

    @Bean
    public ChatService chatService(AzureOpenAIClient openAIClient, VectorStore vectorStore) {
        return new ChatService(openAIClient, vectorStore);
    }

    @Bean
    public AzureOpenAIClient AzureOpenAIClient() {
        var innerClient = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildClient();
        return new AzureOpenAIClient(innerClient, embeddingDeploymentId, chatDeploymentId);
    }

    @Bean
    public VectorStore vectorStore() {
        return new SimpleMemoryVectorStore();
    }
}
