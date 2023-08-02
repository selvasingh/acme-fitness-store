package com.azure.acme.assist.prompt;

import com.azure.acme.assist.model.Product;

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
            """;

    public static String formatWithContext(Product product) {
        return String.format(template,
                product.getName(),
                String.join(",", product.getTags()),
                product.getShortDescription(),
                product.getDescription());
    }
}
