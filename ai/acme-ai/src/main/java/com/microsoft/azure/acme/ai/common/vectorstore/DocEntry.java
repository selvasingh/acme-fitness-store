package com.microsoft.azure.acme.ai.common.vectorstore;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
public class DocEntry {
    private final String id;

    private final String hash;

    private final String text;

    private final List<Double> embedding;
}
