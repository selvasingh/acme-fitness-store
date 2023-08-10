package com.azure.acme.assist.model;

public class CatalogProductResponse {

	private Product data;

	private int status;

	/**
	 * @return the data
	 */
	public Product getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(Product data) {
		this.data = data;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

}
