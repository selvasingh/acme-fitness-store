package com.azure.acme.assist.model;

import java.util.List;

import lombok.Data;

@Data
public class GreetingResponse {

	private String conversationId;
	
	private String greeting;
	
	private List<String> suggestedPrompts;
}
