package com.microsoft.azure.acme.askforhelp.webapi.common.prompt;

import com.microsoft.azure.acme.askforhelp.webapi.model.Product;

public class ProductDetailPromptTemplate {

    private static final String template = """
            You are an AI assistant of an online shop that helps people find information.
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
