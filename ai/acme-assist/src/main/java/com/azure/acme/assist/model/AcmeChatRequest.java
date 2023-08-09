package com.azure.acme.assist.model;

import java.util.List;

import com.azure.ai.openai.models.ChatRole;

import lombok.Data;

@Data
public class AcmeChatRequest {

    /**
     * (Optional) Name of the request page. Used as the context for the conversation.
     */
    private String page;

    /**
     * (Optional) ID of the product. Used as the context for the conversation.
     */
    private String productId;

    /**
     * The chat history of the conversation. The last message must be in the user role.
     */
    private List<Message> messages;

    @Data
    public static class Message {

        private ChatRole role;

        private String content;
    }
}
