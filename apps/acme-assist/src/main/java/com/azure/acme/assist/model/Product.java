package com.azure.acme.assist.model;

import java.util.List;

/**
 * The product model from "catalog-service"
 */
public class Product {

    private String id;

    private String imageUrl1;

    private String imageUrl2;

    private String imageUrl3;

    private String name;

    private String shortDescription;

    private String description;

    private Double price;

    private List<String> tags;

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
     * @return the imageUrl1
     */
    public String getImageUrl1() {
        return imageUrl1;
    }

    /**
     * @param imageUrl1 the imageUrl1 to set
     */
    public void setImageUrl1(String imageUrl1) {
        this.imageUrl1 = imageUrl1;
    }

    /**
     * @return the imageUrl2
     */
    public String getImageUrl2() {
        return imageUrl2;
    }

    /**
     * @param imageUrl2 the imageUrl2 to set
     */
    public void setImageUrl2(String imageUrl2) {
        this.imageUrl2 = imageUrl2;
    }

    /**
     * @return the imageUrl3
     */
    public String getImageUrl3() {
        return imageUrl3;
    }

    /**
     * @param imageUrl3 the imageUrl3 to set
     */
    public void setImageUrl3(String imageUrl3) {
        this.imageUrl3 = imageUrl3;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the shortDescription
     */
    public String getShortDescription() {
        return shortDescription;
    }

    /**
     * @param shortDescription the shortDescription to set
     */
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the price
     */
    public Double getPrice() {
        return price;
    }

    /**
     * @param price the price to set
     */
    public void setPrice(Double price) {
        this.price = price;
    }

    /**
     * @return the tags
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
