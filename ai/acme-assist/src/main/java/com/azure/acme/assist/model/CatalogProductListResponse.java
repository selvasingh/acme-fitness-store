package com.azure.acme.assist.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Builder
@Data
@Jacksonized
public class CatalogProductListResponse {

    private final List<Product> data;

}
