package com.azure.acme.assist.openai;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatMessage;
import com.azure.ai.openai.models.Embeddings;
import com.azure.ai.openai.models.EmbeddingsOptions;

public class AcmeAzureOpenAIClient {

    private static final Logger log = LoggerFactory.getLogger(AcmeAzureOpenAIClient.class);

    private static final double TEMPERATURE = 0.7;

    public AcmeAzureOpenAIClient(OpenAIClient client, String embeddingDeploymentId, String chatDeploymentId) {
        this.client = client;
        this.embeddingDeploymentId = embeddingDeploymentId;
        this.chatDeploymentId = chatDeploymentId;
    }

    private final OpenAIClient client;

    private final String embeddingDeploymentId;

    private final String chatDeploymentId;

    public Embeddings getEmbeddings(List<String> texts) {
        long startTime = System.currentTimeMillis();
        var response = client.getEmbeddings(embeddingDeploymentId, new EmbeddingsOptions(texts));
        long endTime = System.currentTimeMillis();
        log.info("Finished an embedding call with {} tokens in {} milliseconds.", response.getUsage().getTotalTokens(),
                endTime - startTime);
        return response;
    }

    public ChatCompletions getChatCompletions(List<ChatMessage> messages) {
        long startTime = System.currentTimeMillis();
        var chatCompletionsOptions = new ChatCompletionsOptions(messages).setTemperature(TEMPERATURE);
        var response = client.getChatCompletions(chatDeploymentId, chatCompletionsOptions);
        long endTime = System.currentTimeMillis();
        log.info("Finished a chat completion call with {} tokens in {} milliseconds.",
                response.getUsage().getTotalTokens(), endTime - startTime);
        return response;
    }
}
