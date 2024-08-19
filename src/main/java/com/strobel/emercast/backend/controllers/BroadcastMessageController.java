package com.strobel.emercast.backend.controllers;

import com.openapi.gen.springboot.api.BroadcastMessageApi;
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
public class BroadcastMessageController implements BroadcastMessageApi {

    private final BroadcastMessageService broadcastMessageService;

    private final AuthorityService authorityService;

    public BroadcastMessageController(
            @Autowired BroadcastMessageService broadcastMessageService,
            @Autowired AuthorityService authorityService
    ) {
        this.broadcastMessageService = broadcastMessageService;
        this.authorityService = authorityService;
    }

    public ResponseEntity<BroadcastMessageDTO> postBroadcastMessage(
            @Valid @RequestBody PostBroadcastMessageRequest postBroadcastMessageRequest
    ) {
        var callingAuthority = authorityService.getCallingAuthority();

        var message = this.broadcastMessageService.sendPayloadBroadcastMessage(
          authorityService,
          callingAuthority,
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
