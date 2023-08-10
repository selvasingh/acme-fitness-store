package com.azure.acme.assist.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.azure.acme.assist.model.Product;
import com.azure.acme.assist.openai.AcmeAzureOpenAIClient;
import com.azure.acme.assist.openai.RecordEntry;
import com.azure.acme.assist.openai.SimpleMemoryVectorStore;
import com.azure.acme.assist.openai.TextSplitter;
import com.azure.acme.assist.openai.VectorStore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class VectorStoreService {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreService.class);

    @Autowired
    private AcmeAzureOpenAIClient client;

    @Autowired
    private VectorStore vectorStore;

    public void buildFromJson(List<String> files, String saveToPath, List<String> pages, String format)
            throws IOException {
        if (CollectionUtils.isEmpty(files)) {
            throw new IllegalArgumentException("jsonFiles shouldn't be empty.");
        }
        if (saveToPath == null) {
            throw new IllegalArgumentException("saveToPath shouldn't be empty.");
        }

        var objectMapper = new ObjectMapper();
        var splitter = new TextSplitter();
        for (var file : files) {
            List<Product> products = null;
            if (format == null || "json".equals(format)) {
                products = objectMapper.readValue(new File(file), new TypeReference<List<Product>>() {
                });
            } else {
                File sourceFile = new File(file);
                StringBuffer sb = new StringBuffer();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile)))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                }
                Product product = new Product();
                product.setName(sourceFile.getName());
                product.setDescription(sb.toString());
                products = new ArrayList<>();
                products.add(product);
            }

            for (var product : products) {
                log.info("String to process {}...", product.getName());
                var textChunks = splitter.split(product.getDescription());
                for (var chunk : textChunks) {
                    var response = client.getEmbeddings(List.of(chunk));
                    var embedding = response.getData().get(0).getEmbedding();
                    String key = UUID.randomUUID().toString();
                    RecordEntry entry = new RecordEntry();
                    entry.setId(key);
                    entry.setDocId(product.getId());
                    entry.setDocTitle(product.getName());
                    entry.setEmbedding(embedding);
                    entry.setText(chunk);
                    vectorStore.saveRecord(entry);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // pass
                    }
                }
            }
        }
        ((SimpleMemoryVectorStore) vectorStore).saveToJsonFile(new File(saveToPath));

        log.info("All documents are loaded to the local vector store. The index file saved to: {}", saveToPath);
    }

}
