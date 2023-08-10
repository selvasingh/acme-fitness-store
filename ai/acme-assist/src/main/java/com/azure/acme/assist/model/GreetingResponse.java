package com.azure.acme.assist.model;

import java.util.List;

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

    /**
     * Set conversation id
     * 
     * @param conversationId
     */
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    /**
     * Set greeting message
     * 
     * @param greeting
     */
    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    /**
     * Set suggested prompts
     * 
     * @param suggestedPrompts
     */
    public void setSuggestedPrompts(List<String> suggestedPrompts) {
        this.suggestedPrompts = suggestedPrompts;
    }

    /**
     * Get conversation id
     * 
     * @return
     */
    public String getConversationId() {
        return this.conversationId;
    }

    /**
     * Get greeting message
     * 
     * @return
     */
    public String getGreeting() {
        return this.greeting;
    }

    /**
     * Gett Suggested prompts
     * 
     * @return
     */
    public List<String> getSuggestedPrompts() {
        return this.suggestedPrompts;
    }
}
