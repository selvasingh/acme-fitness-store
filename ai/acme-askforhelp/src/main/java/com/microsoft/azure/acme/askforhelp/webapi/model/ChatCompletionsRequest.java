package com.microsoft.azure.acme.askforhelp.webapi.model;

import com.azure.ai.openai.models.ChatMessage;
import lombok.Data;

import java.util.List;

@Data
public class ChatCompletionsRequest {
    private List<ChatMessage> messages;
    private String productId;
}
