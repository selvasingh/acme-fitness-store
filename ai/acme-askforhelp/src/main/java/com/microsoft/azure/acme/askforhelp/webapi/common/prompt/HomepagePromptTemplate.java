package com.microsoft.azure.acme.askforhelp.webapi.common.prompt;

import com.microsoft.azure.acme.askforhelp.webapi.common.vectorstore.RecordEntry;

import java.util.List;
import java.util.stream.Collectors;

public class HomepagePromptTemplate {

    private static final String template = """
            You are an AI assistant of this website named 'Acme Fitness Store', which sells bikes and accessories online.
            You helps people find information.
            Please answer the questions based the following product context and not prior knowledge:
            ===================================
            %s
            """;

    public static String formatWithContext(List<RecordEntry> recordEntries, String question) {
        String merged = recordEntries.stream()
                .map(entry -> String.format(
                        "Product Name: %s\nText: %s\n",
                        entry.getDocTitle(),
                        entry.getText()))
                .collect(Collectors.joining("\n"));
        return String.format(template, merged, question);
    }
}
