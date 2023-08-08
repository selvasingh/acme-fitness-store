package com.azure.acme.assist.openai;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatMessage;
import com.azure.ai.openai.models.Embeddings;
import com.azure.ai.openai.models.EmbeddingsOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class AzureOpenAIClient {

    private static final double TEMPERATURE = 0.7;

    private final OpenAIClient client;

    private final String embeddingDeploymentId;

    private final String chatDeploymentId;

    public Embeddings getEmbeddings(List<String> texts) {
        long startTime = System.currentTimeMillis();
        var response = client.getEmbeddings(embeddingDeploymentId,
                new EmbeddingsOptions(texts));
        long endTime = System.currentTimeMillis();
        log.info("Finished an embedding call with {} tokens in {} milliseconds.",
                response.getUsage().getTotalTokens(),
                endTime - startTime);
        return response;
    }

    public ChatCompletions getChatCompletions(List<ChatMessage> messages) {
        long startTime = System.currentTimeMillis();
        var chatCompletionsOptions = new ChatCompletionsOptions(messages)
                .setTemperature(TEMPERATURE);
        var response = client.getChatCompletions(chatDeploymentId, chatCompletionsOptions);
        long endTime = System.currentTimeMillis();
        log.info("Finished a chat completion call with {} tokens in {} milliseconds.",
                response.getUsage().getTotalTokens(),
                endTime - startTime);
        return response;
    }
}
