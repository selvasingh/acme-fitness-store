package com.azure.acme.assist.prompt;

import java.util.List;
import java.util.stream.Collectors;

import com.azure.acme.assist.model.Product;
import com.azure.acme.assist.openai.RecordEntry;

public class ProductDetailPromptTemplate {

    private static final String template = """
            You are an AI assistant of this website named 'Acme Fitness Store', which sells bikes and accessories online.
            You helps people find information.
            Please answer the questions based the following product details:
            ==================================
            Name: %s
            Tags: %s
            Short description:
            %s
            Full description:
            %s
            ==================================
            Then try to improve your answer the following additional information:
            %s
            """;

    public static String formatWithContext(Product product, List<RecordEntry> recordEntries) {
        String additionalContext = recordEntries.stream()
                .map(entry -> String.format(
                        "Product Name: %s\nText: %s\n",
                        entry.getDocTitle(),
                        entry.getText()))
                .collect(Collectors.joining("\n"));
        return String.format(template,
                product.getName(),
                String.join(",", product.getTags()),
                product.getShortDescription(),
                product.getDescription(),
                additionalContext);
    }
}
