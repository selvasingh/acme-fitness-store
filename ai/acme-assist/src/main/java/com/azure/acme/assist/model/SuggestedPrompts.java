package com.azure.acme.assist.model;

import java.util.List;

import lombok.Data;

@Data
public class SuggestedPrompts {

	private String page;
	
	private String greeting;

	private List<String> prompts;
	
	private boolean isDefault;
}
