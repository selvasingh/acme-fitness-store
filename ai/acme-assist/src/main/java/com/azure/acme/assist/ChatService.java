package com.azure.acme.assist;

import com.azure.acme.assist.model.AcmeChatRequest;
import com.azure.acme.assist.model.Product;
import com.azure.acme.assist.openai.AcmeAzureOpenAIClient;
import com.azure.acme.assist.openai.VectorStore;
import com.azure.acme.assist.prompt.HomepagePromptTemplate;
import com.azure.acme.assist.prompt.ProductDetailMessageCreator;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatRole;
import org.apache.logging.log4j.util.Strings;
import org.springframework.ai.core.llm.LLMResult;
import org.springframework.ai.core.llm.LlmClient;
import org.springframework.ai.core.prompt.Generation;
import org.springframework.ai.core.prompt.Prompt;
import org.springframework.ai.core.prompt.messages.ChatMessage;
import org.springframework.ai.core.prompt.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private AcmeAzureOpenAIClient client;

    @Autowired
    private VectorStore store;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private LlmClient llmClient;

    @Autowired
    private ProductDetailMessageCreator productDetailMessageCreator;

    /**
     * Chat with the OpenAI API. Use the product details as the context.
     *
     * @param chatRequestMessages the chat messages
     * @return the chat response
     */
    public List<String> chat(List<AcmeChatRequest.Message> chatRequestMessages, String productId) {

        validateMessage(chatRequestMessages);

        // step 1. Retrieve the product details.
        Product product = productRepository.getProductById(productId);
        // If no specific product is found, search the vector store to find something that matches the request.
        if (product == null) {
            return chat(chatRequestMessages);
        }

        List<Message> messages = new ArrayList<>();

        // step 2. Convert the user's query text to an embedding
        String question = chatRequestMessages.get(chatRequestMessages.size() - 1).getContent();
        var response = client.getEmbeddings(List.of(question));
        var embedding = response.getData().get(0).getEmbedding();

        // step 3. Query Top-K nearest text chunks from the vector store
        var candidateRecords = store.searchTopKNearest(embedding, 5, 0.4);

        // step 4. Populate the prompt template with the product details.
        Message productDetailMessage =  productDetailMessageCreator.getMessage(product, candidateRecords);
        messages.add(productDetailMessage);

        // Convert to acme messages types to Spring AI message types
        for (AcmeChatRequest.Message chatRequestMessage : chatRequestMessages) {
            String roletoString = chatRequestMessage.getRole().toString().toUpperCase();
            messages.add(new ChatMessage(roletoString, chatRequestMessage.getContent()));
        }

        // step 5. Call to OpenAI chat completion API
        Prompt prompt = new Prompt(messages);
        LLMResult result = this.llmClient.generate(prompt);
        List<String> ret = new ArrayList<>();
        List<Generation> generations = result.getGenerations().get(0);
        for (Generation generation : generations) {
            ret.add(filterMessage(generation.getText()));
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
    public List<String> chat(List<AcmeChatRequest.Message> messages) {

        validateMessage(messages);

        String question = messages.get(messages.size() - 1).getContent();

        // step 1. Convert the user's query text to an embedding
        var response = client.getEmbeddings(List.of(question));
        var embedding = response.getData().get(0).getEmbedding();

        // step 2. Query Top-K nearest text chunks from the vector store
        var candidateRecords = store.searchTopKNearest(embedding, 5, 0.4);

        // step 3. Populate the prompt template with the chunks
        var prompt = HomepagePromptTemplate.formatWithContext(candidateRecords);
        var processedMessages = new ArrayList<com.azure.ai.openai.models.ChatMessage>();
        processedMessages.add(new com.azure.ai.openai.models.ChatMessage(ChatRole.SYSTEM, prompt));
        List<com.azure.ai.openai.models.ChatMessage> list = new ArrayList<>();
        for (AcmeChatRequest.Message line : messages) {
            list.add(new com.azure.ai.openai.models.ChatMessage(line.getRole(), line.getContent()));
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

    private static void validateMessage(List<AcmeChatRequest.Message> messages) {
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
