package com.azure.acme.assist.model;

/**
 * Model of request body of Greeting API
 */
public class GreetingRequest {

	/**
	 * Name of the request page
	 */
	private String page;

	/**
	 * ID of current user
	 */
	private String userId;

	/**
	 * ID of current conversation
	 */
	private String conversationId;

	/**
	 * Set page name
	 * 
	 * @param page
	 */
	public void setPage(String page) {
		this.page = page;
	}

	/**
	 * Set user id
	 * 
	 * @param userId
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * Set conversation id
	 * 
	 * @param conversationId
	 */
	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}

	public String getPage() {
		return this.page;
	}

	public String getUserId() {
		return this.userId;
	}

	public String getConversationId() {
		return this.conversationId;
	}
}
