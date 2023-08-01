package com.azure.acme.assist.openai;

import java.util.List;

/**
 * Interface for the vector store.
 */
public interface VectorStore {
    void saveRecord(RecordEntry record);

    RecordEntry getRecord(String id);

    void removeRecord(String id);

    void clear();

    List<RecordEntry> searchTopKNearest(List<Double> embedding, int k);

    List<RecordEntry> searchTopKNearest(List<Double> embedding, int k, double cutOff);
}
