package com.microsoft.azure.acme.askforhelp.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Builder
@Data
@Jacksonized
public class Product {
    private final String id;
    private final String imageUrl1;
    private final String imageUrl2;
    private final String imageUrl3;
    private final String name;
    private final String shortDescription;
    private final String description;
    private final Double price;
    private final List<String> tags;
}
