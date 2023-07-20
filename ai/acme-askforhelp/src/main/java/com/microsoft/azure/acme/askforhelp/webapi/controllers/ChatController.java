package com.microsoft.azure.acme.askforhelp.webapi.controllers;


import com.azure.ai.openai.models.ChatCompletions;
import com.microsoft.azure.acme.askforhelp.webapi.service.ChatService;
import com.microsoft.azure.acme.askforhelp.webapi.model.ChatCompletionsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/completions")
    public ChatCompletions chatCompletion(@RequestBody ChatCompletionsRequest request) {
        final String productId = request.getProductId();
        if (StringUtils.hasText(productId)) {
            return chatService.chatWithProduct(request.getMessages(), request.getProductId());
        } else {
            return chatService.chat(request.getMessages());
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody String handleException(IllegalArgumentException ex) {
        return ex.getMessage();
    }
}
