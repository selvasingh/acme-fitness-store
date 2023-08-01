package com.azure.acme.assist.model;

import java.util.List;

import lombok.Data;

@Data
public class SuggestedPrompts {

    /**
     * Name of the request page
     */
    private String page;

    /**
     * Greeting message for the page
     */
    private String greeting;

    /**
     * List of suggested prompts for the page
     */
    private List<String> prompts;

    /**
     * If these suggestion prompts used as the default
     */
    private boolean isDefault;
}
