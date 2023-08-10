package com.azure.acme.assist.model;

import java.util.List;

public class CatalogProductListResponse {

	private List<Product> data;

	/**
	 * @return the data
	 */
	public List<Product> getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(List<Product> data) {
		this.data = data;
	}

}
