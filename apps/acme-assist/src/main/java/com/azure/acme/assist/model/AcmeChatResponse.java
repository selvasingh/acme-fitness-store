package com.azure.acme.assist.model;

import java.util.List;

public class AcmeChatResponse {

    /**
     * The candidate answers for the chat. Only one is provided for now.
     */
    private List<String> messages;

    /**
     * Set messages
     * 
     * @param messages
     */
    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    /**
     * Get messages
     * 
     * @return
     */
    public List<String> getMessages() {
        return this.messages;
    }
}
