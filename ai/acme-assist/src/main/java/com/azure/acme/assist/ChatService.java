package com.azure.acme.assist;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.azure.acme.assist.model.ChatRequest;
import com.azure.acme.assist.model.Product;
import com.azure.acme.assist.openai.AzureOpenAIClient;
import com.azure.acme.assist.openai.VectorStore;
import com.azure.acme.assist.prompt.HomepagePromptTemplate;
import com.azure.acme.assist.prompt.ProductDetailPromptTemplate;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatMessage;
import com.azure.ai.openai.models.ChatRole;

@Service
public class ChatService {

    @Autowired
    private AzureOpenAIClient client;

    @Autowired
    private VectorStore store;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Chat with the OpenAI API. Use the product details as the context.
     *
     * @param messages the chat messages
     * @return the chat response
     */
    public List<String> chat(List<ChatRequest.Message> messages, String productId) {

        validateMessage(messages);

        // step 1. Retrieve the product details.
        Product product = productRepository.getProductById(productId);
        if (product == null) {
            return chat(messages);
        }

        // step 2. Convert the user's query text to an embedding
        String question = messages.get(messages.size() - 1).getContent();
        var response = client.getEmbeddings(List.of(question));
        var embedding = response.getData().get(0).getEmbedding();

        // step 3. Query Top-K nearest text chunks from the vector store
        var candidateRecords = store.searchTopKNearest(embedding, 5, 0.4);

        // step 4. Populate the prompt template with the product details.
        var prompt = ProductDetailPromptTemplate.formatWithContext(product, candidateRecords);
        var processedMessages = new ArrayList<ChatMessage>();
        processedMessages.add(new ChatMessage(ChatRole.SYSTEM, prompt));

        List<ChatMessage> list = new ArrayList<>();
        for (ChatRequest.Message line : messages) {
            list.add(new ChatMessage(line.getRole(), line.getContent()));
        }
        processedMessages.addAll(list);

        // step 5. Call to OpenAI chat completion API
        var answer = client.getChatCompletions(processedMessages);
        List<String> ret = new ArrayList<>();
        for (ChatChoice choice : answer.getChoices()) {
            if (choice.getMessage() != null && choice.getMessage().getContent() != null) {
                ret.add(filterMessage(choice.getMessage().getContent()));
            }
        }
        return ret;
    }

    /**
     * Chat with the OpenAI API. Search the vector store for the top 5 nearest text chunks
     * to the questions and use them as the context.
     *
     * @param messages the chat messages
     * @return the chat response
     */
    public List<String> chat(List<ChatRequest.Message> messages) {

        validateMessage(messages);

        String question = messages.get(messages.size() - 1).getContent();

        // step 1. Convert the user's query text to an embedding
        var response = client.getEmbeddings(List.of(question));
        var embedding = response.getData().get(0).getEmbedding();

        // step 2. Query Top-K nearest text chunks from the vector store
        var candidateRecords = store.searchTopKNearest(embedding, 5, 0.4);

        // step 3. Populate the prompt template with the chunks
        var prompt = HomepagePromptTemplate.formatWithContext(candidateRecords);
        var processedMessages = new ArrayList<ChatMessage>();
        processedMessages.add(new ChatMessage(ChatRole.SYSTEM, prompt));
        List<ChatMessage> list = new ArrayList<>();
        for (ChatRequest.Message line : messages) {
            list.add(new ChatMessage(line.getRole(), line.getContent()));
        }
        processedMessages.addAll(list);

        // step 4. Call to OpenAI chat completion API
        var answer = client.getChatCompletions(processedMessages);

        List<String> ret = new ArrayList<>();
        for (ChatChoice choice : answer.getChoices()) {
            if (choice.getMessage() != null && choice.getMessage().getContent() != null) {
                ret.add(filterMessage(choice.getMessage().getContent()));
            }
        }
        return ret;
    }

    private static void validateMessage(List<ChatRequest.Message> messages) {
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
    }

    private String filterMessage(String content) {
        if (Strings.isEmpty(content)) {
            return "";
        }
        List<Product> products = productRepository.getProductList();
        for (Product product : products) {
            content = content.replace(product.getName(), "{{" + product.getName() + "|" + product.getId() + "}}");
        }
        return content;
    }
}
