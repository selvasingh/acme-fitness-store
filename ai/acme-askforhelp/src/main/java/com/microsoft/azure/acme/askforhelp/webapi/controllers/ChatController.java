package com.microsoft.azure.acme.askforhelp.webapi.controllers;


import com.azure.ai.openai.models.ChatCompletions;
import com.microsoft.azure.acme.askforhelp.common.ChatTask;
import com.microsoft.azure.acme.askforhelp.webapi.models.ChatCompletionsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatTask planner;

    @PostMapping("/completions")
    public ChatCompletions chatCompletion(@RequestBody ChatCompletionsRequest request) {
        return planner.chat(request.getMessages());
    }
}
