package com.microsoft.azure.acme.askforhelp.common;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Builder
@Data
@Jacksonized
public class Product {
    private String id;
    private String imageUrl1;
    private String imageUrl2;
    private String imageUrl3;
    private String name;
    private String shortDescription;
    private String description;
    private Double price;
    private String tags;    
}
