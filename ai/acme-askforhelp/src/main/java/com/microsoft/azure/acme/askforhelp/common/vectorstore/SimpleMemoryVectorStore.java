package com.microsoft.azure.acme.askforhelp.common.vectorstore;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleMemoryVectorStore implements VectorStore {

    private final VectorStoreData data;

    public SimpleMemoryVectorStore() {
        this.data = new VectorStoreData();
    }

    SimpleMemoryVectorStore(VectorStoreData data) {
        this.data = data;
    }

    @Override
    public void saveRecord(RecordEntry record) {
        data.store.put(record.getId(), record);
    }

    @Override
    public RecordEntry getRecord(String id) {
        return data.store.getOrDefault(id, null);
    }

    @Override
    public void removeRecord(String id) {
        data.store.remove(id);
    }

    @Override
    public List<RecordEntry> searchTopKNearest(List<Double> embedding, int k) {
        return searchTopKNearest(embedding, k, 0);
    }

    @Override
    public List<RecordEntry> searchTopKNearest(List<Double> embedding, int k, double cutOff) {
        var similarities = data.store.values().stream().map(entry -> new Similarity(
                        entry.getId(),
                        EmbeddingMath.cosineSimilarity(embedding, entry.getEmbedding())))
                .filter(s -> s.similarity >= cutOff)
                .sorted(Comparator.<Similarity>comparingDouble(s -> s.similarity).reversed())
                .limit(k)
                .map(s -> data.store.get(s.key))
                .toList();
        return similarities;
    }

    public void saveToJsonFile(String filePath) {
        var objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try (var fileWriter = new FileWriter(filePath, StandardCharsets.UTF_8)) {
            objectWriter.writeValue(fileWriter, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SimpleMemoryVectorStore loadFromJsonFile(String filePath) {
        var reader = new ObjectMapper().reader();
        try {
            var data = reader.readValue(new File(filePath), VectorStoreData.class);
            return new SimpleMemoryVectorStore(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AllArgsConstructor
    private static class Similarity {
        private String key;
        private double similarity;
    }

    @Setter
    @Getter
    private static class VectorStoreData {
        private Map<String, RecordEntry> store = new ConcurrentHashMap<>();
    }
}