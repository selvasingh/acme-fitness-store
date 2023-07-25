package com.azure.acme.assist.model;

import java.util.List;

import com.azure.ai.openai.models.ChatRole;

import lombok.Data;

@Data
public class ChatRequest {

	private String page;

	private String productId;

	private List<Message> messages;

	@Data
	public static class Message {

		private ChatRole role;

		private String content;
	}
}
