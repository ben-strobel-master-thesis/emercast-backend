package com.strobel.emercast.backend.controllers;

import com.openapi.gen.springboot.api.AuthorityApi;
import com.openapi.gen.springboot.api.LoginApi;
import com.openapi.gen.springboot.dto.*;
import com.strobel.emercast.backend.db.models.authority.Authority;
import com.strobel.emercast.backend.db.models.authority.JurisdictionMarker;
import com.strobel.emercast.backend.services.AuthorityService;
import com.strobel.emercast.backend.services.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class AuthorityController implements AuthorityApi, LoginApi {

    private final AuthorityService authorityService;

    private final JwtService jwtService;

    public AuthorityController(@Autowired AuthorityService authorityService, @Autowired JwtService jwtService) {
        this.authorityService = authorityService;
        this.jwtService = jwtService;
    }

    @Override
    public ResponseEntity<Login200Response> login(@Valid @RequestBody LoginRequest loginRequest) {
        var authority = authorityService.checkCredentials(loginRequest.getLoginName(), loginRequest.getPassword());
        if(authority.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        var token = jwtService.createTokenForAuthority(authority.get());
        return ResponseEntity.ok(new Login200Response(authority.get().toOpenAPI().getId(), token));
    }

    @Override
    public ResponseEntity<GetBroadcastMessageChainHash200Response> getAuthorityChainHash() {
        var hash = authorityService.getAuthorityChainHash();
        return ResponseEntity.ok(new GetBroadcastMessageChainHash200Response(hash));
    }

    @Override
    public ResponseEntity<AuthorityDTO> createNewAuthority( @Valid @RequestBody CreateNewAuthorityRequest createNewAuthorityRequest ){
        var callingAuthority = authorityService.getCallingAuthority();

        var authority = authorityService.createAuthority(
                createNewAuthorityRequest.getLoginName(),
                createNewAuthorityRequest.getPassword(),
                callingAuthority.getId(),
                createNewAuthorityRequest.getPublicName(),
                createNewAuthorityRequest.getJurisdictionDescription(),
                createNewAuthorityRequest.getJurisdictionMarkers().stream().map(JurisdictionMarker::fromOpenAPI).collect(Collectors.toList())
        );

        return ResponseEntity.ok(authority.toOpenAPI());
    }

    @Override
    public ResponseEntity<List<AuthorityDTO>> getAuthorityPage(
            @NotNull @Valid @RequestParam(value = "page", required = true) Integer page,
            @NotNull  @Valid @RequestParam(value = "pageSize", required = true) Integer pageSize
    ) {
        var list = authorityService.getAuthorityListPage(page, pageSize);
        return ResponseEntity.ok(list.stream().map(Authority::toOpenAPI).collect(Collectors.toList()));
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return AuthorityApi.super.getRequest();
    }
}
