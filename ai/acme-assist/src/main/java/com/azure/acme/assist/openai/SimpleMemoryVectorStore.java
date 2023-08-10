package com.azure.acme.assist.openai;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Simple in-memory implementation of the vector store. Support saving to and
 * loading from a json file. Used for demo purposes only.
 */
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
    public void clear() {
        data.store.clear();
    }

    @Override
    public List<RecordEntry> searchTopKNearest(List<Double> embedding, int k) {
        return searchTopKNearest(embedding, k, 0);
    }

    @Override
    public List<RecordEntry> searchTopKNearest(List<Double> embedding, int k, double cutOff) {
        var similarities = data.store.values().stream()
                .map(entry -> new Similarity(entry.getId(),
                        EmbeddingMath.cosineSimilarity(embedding, entry.getEmbedding())))
                .filter(s -> s.similarity >= cutOff)
                .sorted(Comparator.<Similarity>comparingDouble(s -> s.similarity).reversed()).limit(k)
                .map(s -> data.store.get(s.key)).toList();
        return similarities;
    }

    public void saveToJsonFile(File filePath) {
        var objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try (var fileWriter = new FileWriter(filePath, StandardCharsets.UTF_8)) {
            objectWriter.writeValue(fileWriter, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SimpleMemoryVectorStore loadFromJsonFile(File jsonFile) {
        var reader = new ObjectMapper().reader();
        try {
            var data = reader.readValue(jsonFile, VectorStoreData.class);
            return new SimpleMemoryVectorStore(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Similarity {
        private String key;
        private double similarity;

        public Similarity(String key, double similarity) {
            this.key = key;
            this.similarity = similarity;
        }
    }

    public static class VectorStoreData {
        private Map<String, RecordEntry> store = new ConcurrentHashMap<>();

        /**
         * @return the store
         */
        public Map<String, RecordEntry> getStore() {
            return store;
        }

        /**
         * @param store the store to set
         */
        public void setStore(Map<String, RecordEntry> store) {
            this.store = store;
        }
    }
}
