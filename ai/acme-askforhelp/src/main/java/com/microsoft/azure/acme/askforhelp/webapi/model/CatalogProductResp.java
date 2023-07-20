package com.microsoft.azure.acme.askforhelp.webapi.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Builder
@Data
@Jacksonized
public class CatalogProductResp {
    private final Product data;
    private final int status;
}
