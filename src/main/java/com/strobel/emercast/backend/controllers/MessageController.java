package com.strobel.emercast.backend.controllers;

import com.openapi.gen.springboot.api.MessageApi;
import com.openapi.gen.springboot.dto.BroadcastMessageDTO;
import com.openapi.gen.springboot.dto.PostBroadcastMessageRequest;
import com.strobel.emercast.backend.services.AuthorityService;
import com.strobel.emercast.backend.services.BroadcastMessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class MessageController implements MessageApi {

    private final BroadcastMessageService broadcastMessageService;

    private final AuthorityService authorityService;

    public MessageController(
            @Autowired BroadcastMessageService broadcastMessageService,
            @Autowired AuthorityService authorityService
    ) {
        this.broadcastMessageService = broadcastMessageService;
        this.authorityService = authorityService;
    }

    public ResponseEntity<BroadcastMessageDTO> postBroadcastMessage(
            @Valid @RequestBody PostBroadcastMessageRequest postBroadcastMessageRequest
    ) {
        var message = this.broadcastMessageService.sendPayloadBroadcastMessage(
          authorityService,
          null, // TODO Get authority of currently logged in authority
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
