package com.azure.acme.assist.openai;

import java.util.List;

public class RecordEntry {
    private String id;

    private String docId;

    private String docTitle;

    private String text;

    private List<Double> embedding;

    public RecordEntry() {

    }

    public RecordEntry(String id, String docId, String docTitle, String text, List<Double> embedding) {
        this.id = id;
        this.docId = docId;
        this.docTitle = docTitle;
        this.text = text;
        this.embedding = embedding;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the docId
     */
    public String getDocId() {
        return docId;
    }

    /**
     * @param docId the docId to set
     */
    public void setDocId(String docId) {
        this.docId = docId;
    }

    /**
     * @return the docTitle
     */
    public String getDocTitle() {
        return docTitle;
    }

    /**
     * @param docTitle the docTitle to set
     */
    public void setDocTitle(String docTitle) {
        this.docTitle = docTitle;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return the embedding
     */
    public List<Double> getEmbedding() {
        return embedding;
    }

    /**
     * @param embedding the embedding to set
     */
    public void setEmbedding(List<Double> embedding) {
        this.embedding = embedding;
    }

}
