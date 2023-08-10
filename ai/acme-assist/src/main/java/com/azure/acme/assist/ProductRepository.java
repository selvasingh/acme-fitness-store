package com.azure.acme.assist;

import java.util.List;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.azure.acme.assist.model.CatalogProductListResponse;
import com.azure.acme.assist.model.CatalogProductResponse;
import com.azure.acme.assist.model.Product;

import jakarta.annotation.PostConstruct;

@Repository
@Configuration
public class ProductRepository {

	private static final Logger log = LoggerFactory.getLogger(ProductRepository.class);

	@Value("${catalogService:http://catalog-service}")
	private String catalogService;

	public Product getProductById(String id) {
		if (Strings.isEmpty(id)) {
			return null;
		}
		try {
			RestTemplate restTemplate = new RestTemplate();
			var response = restTemplate.getForEntity(catalogService + "/products/" + id, CatalogProductResponse.class);
			log.info("Response code from catalog-service: {}", response.getStatusCode());
			return response.getBody().getData();
		} catch (HttpClientErrorException ex) {
			log.warn("Can't get the product detail: {}", ex.getMessage());
			return null;
		}
	}

	private static List<Product> products;

	@PostConstruct
	public List<Product> getProductList() {
		if (products == null) {
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<CatalogProductListResponse> response = restTemplate
					.getForEntity(catalogService + "/products", CatalogProductListResponse.class);
			products = response.getBody().getData();
		}
		return products;
	}
}
