package com.strobel.emercast.backend.controllers;

import com.openapi.gen.springboot.api.BroadcastMessageApi;
import com.openapi.gen.springboot.dto.BroadcastMessageDTO;
import com.openapi.gen.springboot.dto.GetBroadcastMessageChainHash200Response;
import com.openapi.gen.springboot.dto.PostBroadcastMessageRequest;
import com.strobel.emercast.backend.services.AuthorityService;
import com.strobel.emercast.backend.services.BroadcastMessageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

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

    @Override
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

    @Override
    public ResponseEntity<GetBroadcastMessageChainHash200Response> getBroadcastMessageChainHash() {
        //TODO
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<List<BroadcastMessageDTO>> getBroadcastMessagesPage(
            @NotNull @Valid @RequestParam(value = "page", required = true) Integer page,
            @NotNull  @Valid @RequestParam(value = "pageSize", required = true) Integer pageSize
    ) {
        //TODO
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }
}
