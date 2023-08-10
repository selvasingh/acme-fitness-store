package com.azure.acme.assist.model;

import java.util.List;

public class SuggestedPrompts {

	/**
	 * Name of the request page
	 */
	private String page;

	/**
	 * Greeting message for the page
	 */
	private String greeting;

	/**
	 * List of suggested prompts for the page
	 */
	private List<String> prompts;

	/**
	 * If these suggestion prompts used as the default
	 */
	private boolean isDefault;

	/**
	 * @return the page
	 */
	public String getPage() {
		return page;
	}

	/**
	 * @param page the page to set
	 */
	public void setPage(String page) {
		this.page = page;
	}

	/**
	 * @return the greeting
	 */
	public String getGreeting() {
		return greeting;
	}

	/**
	 * @param greeting the greeting to set
	 */
	public void setGreeting(String greeting) {
		this.greeting = greeting;
	}

	/**
	 * @return the prompts
	 */
	public List<String> getPrompts() {
		return prompts;
	}

	/**
	 * @param prompts the prompts to set
	 */
	public void setPrompts(List<String> prompts) {
		this.prompts = prompts;
	}

	/**
	 * @return the isDefault
	 */
	public boolean isDefault() {
		return isDefault;
	}

	/**
	 * @param isDefault the isDefault to set
	 */
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}
}
