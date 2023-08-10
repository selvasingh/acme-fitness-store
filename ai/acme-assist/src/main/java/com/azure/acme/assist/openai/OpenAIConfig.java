package com.azure.acme.assist.openai;

import java.io.IOException;
import java.time.Duration;

import com.azure.ai.openai.OpenAIClient;
import org.springframework.ai.core.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import static java.nio.charset.StandardCharsets.UTF_8;

@Configuration
public class OpenAIConfig {

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
    public SystemPromptTemplate productDetailSystemPromptTemplate() {
        Resource resource = new ClassPathResource("/prompts/product-detail.st");
        try {
            return new SystemPromptTemplate(StreamUtils.copyToString(resource.getInputStream(), UTF_8));
        } catch (IOException ex) {
            throw new RuntimeException("Could not create input stream from " + resource, ex);
        }
    }

    @Bean
    public OpenAIClient openAiClient() {
        var innerClient = new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(apiKey))
                .retryOptions(new RetryOptions(new FixedDelayOptions(5, Duration.ofSeconds(1))))
                .buildClient();
        return innerClient;
    }

    @Bean
    public AcmeAzureOpenAIClient AzureOpenAIClient(OpenAIClient openAiClient) {
        return new AcmeAzureOpenAIClient(openAiClient, embeddingDeploymentId, chatDeploymentId);
    }

    @Bean
    public VectorStore vectorStore() throws IOException {
        return SimpleMemoryVectorStore.loadFromJsonFile(new ClassPathResource(vectorJsonFile).getFile());
    }
}
