package com.microsoft.azure.acme.askforhelp.webapi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.acme.askforhelp.webapi.common.AzureOpenAIClient;
import com.microsoft.azure.acme.askforhelp.webapi.common.TextSplitter;
import com.microsoft.azure.acme.askforhelp.webapi.common.vectorstore.RecordEntry;
import com.microsoft.azure.acme.askforhelp.webapi.common.vectorstore.SimpleMemoryVectorStore;
import com.microsoft.azure.acme.askforhelp.webapi.common.vectorstore.VectorStore;
import com.microsoft.azure.acme.askforhelp.webapi.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final AzureOpenAIClient client;

    private final VectorStore vectorStore;

    public void buildFromJson(List<String> jsonFiles, String saveToPath) throws IOException {
        if (CollectionUtils.isEmpty(jsonFiles)) {
            throw new IllegalArgumentException("jsonFiles shouldn't be empty.");
        }
        if (saveToPath == null) {
            throw new IllegalArgumentException("saveToPath shouldn't be empty.");
        }

        var objectMapper = new ObjectMapper();
        var splitter = new TextSplitter();
        for (var jsonFile : jsonFiles) {
            var products = objectMapper.readValue(new File(jsonFile), new TypeReference<List<Product>>() {
            });
            for (var product : products) {
                log.info("String to process {}...", product.getName());
                var textChunks = splitter.split(product.getDescription());
                for (var chunk : textChunks) {
                    var response = client.getEmbeddings(List.of(chunk));
                    var embedding = response.getData().get(0).getEmbedding();
                    String key = UUID.randomUUID().toString();
                    vectorStore.saveRecord(RecordEntry.builder()
                            .id(key)
                            .docId(product.getId())
                            .docTitle(product.getName())
                            .embedding(embedding)
                            .text(chunk)
                            .build());

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        // pass
                    }
                }
            }
        }

        ((SimpleMemoryVectorStore) vectorStore).saveToJsonFile(saveToPath);
        log.info("All documents are loaded to the local vector store. The index file saved to: {}", saveToPath);
    }

}
