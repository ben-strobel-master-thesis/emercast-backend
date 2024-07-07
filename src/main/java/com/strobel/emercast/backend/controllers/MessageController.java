package com.strobel.emercast.backend.controllers;

import com.openapi.gen.springboot.api.MessageApi;
import com.openapi.gen.springboot.dto.BroadcastMessageDTO;
import com.openapi.gen.springboot.dto.PostBroadcastMessageRequest;
import com.strobel.emercast.backend.services.BroadcastMessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class MessageController implements MessageApi {

    private final BroadcastMessageService broadcastMessageService;

    public MessageController(@Autowired BroadcastMessageService broadcastMessageService) {
        this.broadcastMessageService = broadcastMessageService;
    }

    public ResponseEntity<BroadcastMessageDTO> postBroadcastMessage(
            @Valid @RequestBody PostBroadcastMessageRequest postBroadcastMessageRequest
    ) {
        var message = this.broadcastMessageService.sendBroadcastMessage(
          postBroadcastMessageRequest.getLatitude(),
          postBroadcastMessageRequest.getLongitude(),
          postBroadcastMessageRequest.getRadius(),
          postBroadcastMessageRequest.getCategory(),
          postBroadcastMessageRequest.getSeverity(),
          postBroadcastMessageRequest.getTitle(),
          postBroadcastMessageRequest.getMessage()
        );
        return ResponseEntity.ok(message.toOpenAPI());
    }
}
