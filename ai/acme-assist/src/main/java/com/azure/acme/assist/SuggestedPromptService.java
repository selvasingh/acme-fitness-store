package com.azure.acme.assist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.azure.acme.assist.model.SuggestedPrompts;

@Service
public class SuggestedPromptService {

    @Autowired
    private SuggestedPromptRepository repository;

    /**
     * Returns suggested prompts according to specified page name.
     *
     * @param page
     * @return
     */
    public SuggestedPrompts getSuggestedPrompts(String page) {
        SuggestedPrompts prompts = repository.getSuggestedPrompts(page);
        return prompts == null ? repository.getDefaultSuggestedPrompts() : prompts;
    }
}
