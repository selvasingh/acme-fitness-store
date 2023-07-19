package com.microsoft.azure.acme.askforhelp.common.vectorstore;

import java.util.List;

public interface VectorStore {
    void saveRecord(RecordEntry record);

    RecordEntry getRecord(String id);

    void removeRecord(String id);

    List<RecordEntry> searchTopKNearest(List<Double> embedding, int k);

    List<RecordEntry> searchTopKNearest(List<Double> embedding, int k, double cutOff);
}
