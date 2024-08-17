package com.strobel.emercast.backend.services;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.file.Path;

@Service
public class AuthorityService {

    private static final Path authorityKeysPath = Path.of(URI.create("/etc/emercast/keys"));
    private static final Path authorityPrivateKeysPath = authorityKeysPath.resolve("private");
    private static final Path authorityPublicKeysPath = authorityKeysPath.resolve("public");

    private void generateKeyPair() {

    }

    private static void runKeyTool(String[] args) {
        
    }
}
