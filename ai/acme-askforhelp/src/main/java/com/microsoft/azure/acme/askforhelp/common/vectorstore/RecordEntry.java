package com.microsoft.azure.acme.askforhelp.common.vectorstore;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
public class RecordEntry {
    private final String id;

    private final String docId;

    private final String text;

    private final List<Double> embedding;
}
