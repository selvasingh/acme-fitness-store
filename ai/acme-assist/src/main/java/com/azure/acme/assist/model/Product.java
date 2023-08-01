package com.azure.acme.assist.model;

import java.util.List;

import lombok.Data;

/**
 * The product model from "catalog-service"
 */
@Data
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
}
