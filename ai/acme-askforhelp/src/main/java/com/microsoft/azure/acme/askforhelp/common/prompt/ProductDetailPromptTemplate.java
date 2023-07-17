package com.microsoft.azure.acme.askforhelp.common.prompt;

import java.util.List;

public class ProductDetailPromptTemplate {

    private static final String template = """
            You are an AI assistant of an online shop that helps people find information.
            Please answer the questions based the following product details:
            ==================================
            %s
            """;

    public static String formatWithContext(String productDesc) {
        return String.format(template, productDesc);
    }
}
