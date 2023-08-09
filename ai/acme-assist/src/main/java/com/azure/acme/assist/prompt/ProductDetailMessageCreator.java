package com.azure.acme.assist.prompt;


import com.azure.acme.assist.model.Product;
import com.azure.acme.assist.openai.RecordEntry;
import org.springframework.ai.core.prompt.SystemPromptTemplate;
import org.springframework.ai.core.prompt.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@Component
public class ProductDetailMessageCreator {

    @Autowired
    @Qualifier("productDetailSystemPromptTemplate")
    private SystemPromptTemplate systemPromptTemplate;

    public Message getMessage(Product product, List<RecordEntry> recordEntries) {
        String additionalContext = recordEntries.stream()
                .map(entry -> String.format(
                        "Product Name: %s\nText: %s\n",
                        entry.getDocTitle(),
                        entry.getText()))
                .collect(Collectors.joining("\n"));

        Map map = Map.of(
                "name", product.getName(),
                "tags", String.join(",", product.getTags()),
                "shortDescription", product.getShortDescription(),
                "fullDescription", product.getDescription(),
                "additionalContext", additionalContext);
        // This ensures that all variables in the template were replaced, otherwise an exception is thrown
        return systemPromptTemplate.create(map).getMessages().get(0);
    }

}
