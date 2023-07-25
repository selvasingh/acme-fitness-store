package com.azure.acme.assist.model;

import lombok.Data;

/**
 * Model of request body of Greeting API
 *
 */
@Data
public class GreetingRequest {

	private String page;

	private String userId;

	private String conversationId;
}
