package com.azure.acme.assist.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Builder
@Data
@Jacksonized
public class CatalogProductResponse {

    private final Product data;

    private final int status;
}
