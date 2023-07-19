package com.microsoft.azure.acme.askforhelp.service;

import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatMessage;
import com.azure.ai.openai.models.ChatRole;
import com.microsoft.azure.acme.askforhelp.common.AzureOpenAIClient;
import com.microsoft.azure.acme.askforhelp.common.prompt.HomepagePromptTemplate;
import com.microsoft.azure.acme.askforhelp.common.prompt.ProductDetailPromptTemplate;
import com.microsoft.azure.acme.askforhelp.common.vectorstore.VectorStore;
import com.microsoft.azure.acme.askforhelp.common.vectorstore.RecordEntry;
import com.microsoft.azure.acme.askforhelp.model.CatalogProductResp;
import com.microsoft.azure.acme.askforhelp.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final AzureOpenAIClient client;

    private final VectorStore store;

    public ChatCompletions chatWithProduct(List<ChatMessage> messages, String productId) {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("message shouldn't be empty.");
        }

        if (messages.get(0).getRole() != ChatRole.USER) {
            throw new IllegalArgumentException("The first message should be in user role.");
        }

        var lastUserMessage = messages.get(messages.size() - 1);
        if (lastUserMessage.getRole() != ChatRole.USER) {
            throw new IllegalArgumentException("The last message should be in user role.");
        }

        // step 1. Retrieve the product details.
        Product product = getProduct(productId);
        if (product == null) {
            return null;
        }

        // step 2. Populate the prompt template with the product details.
        var prompt = ProductDetailPromptTemplate.formatWithContext(product);
        var processedMessages = new ArrayList<ChatMessage>();
        processedMessages.add(new ChatMessage(ChatRole.SYSTEM).setContent(prompt));
        processedMessages.addAll(messages);

        // step 3. Call to OpenAI chat completion API
        var answer = client.getChatCompletions(processedMessages);
        return answer;
    }

    public ChatCompletions chat(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("message shouldn't be empty.");
        }

        var lastUserMessage = messages.get(messages.size() - 1);
        if (lastUserMessage.getRole() != ChatRole.USER) {
            throw new IllegalArgumentException("The last message should be in user role.");
        }
        String question = lastUserMessage.getContent();

        // step 1. Convert the user's query text to an embedding
        var response = client.getEmbeddings(List.of(question));
        var embedding = response.getData().get(0).getEmbedding();

        // step 2. Query Top-K nearest text chunks from the vector store
        var candidateDocs = store.searchTopKNearest(embedding, 5, 0.4).stream()
                .map(RecordEntry::getText).toList();

        // step 3. Populate the prompt template with the chunks
        var prompt = HomepagePromptTemplate.formatWithContext(candidateDocs, question);
        var processedMessages = new ArrayList<>(messages);
        processedMessages.set(messages.size() - 1, new ChatMessage(ChatRole.USER).setContent(prompt));

        // step 4. Call to OpenAI chat completion API
        var answer = client.getChatCompletions(processedMessages);
        return answer;
    }

    private Product getProduct(String productId) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            var response = restTemplate.getForEntity("http://catalog-service/products/" + productId, CatalogProductResp.class);
            log.info("Response code from catalog-service: {}", response.getStatusCode());
            return response.getBody().getData();
        } catch (HttpClientErrorException ex) {
            log.warn("Can't get the product detail: {}", ex.getMessage());
            return null;
        }
    }
}
