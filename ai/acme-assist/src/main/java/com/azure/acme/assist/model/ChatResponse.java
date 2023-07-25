package com.azure.acme.assist.model;

import java.util.List;

import lombok.Data;

@Data
public class ChatResponse {

	private List<String> messages;
}
