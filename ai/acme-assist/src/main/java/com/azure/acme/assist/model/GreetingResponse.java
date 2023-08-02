package com.azure.acme.assist.model;

import java.util.List;

import lombok.Data;

@Data
public class GreetingResponse {

    /**
     * ID of current conversation
     */
    private String conversationId;

    /**
     * Greeting message for the page
     */
    private String greeting;

    /**
     * List of suggested prompts for the page
     */
    private List<String> suggestedPrompts;
}
