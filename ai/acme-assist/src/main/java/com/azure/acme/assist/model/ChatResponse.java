package com.azure.acme.assist.model;

import java.util.List;

import lombok.Data;

@Data
public class ChatResponse {

    /**
     * The candidate answers for the chat. Only one is provided for now.
     */
    private List<String> messages;
}
