package com.strobel.emercast.backend.lib;

import java.util.Base64;

public class SerializationUtils {
    public static String toBase64String(com.google.protobuf.GeneratedMessageV3 object) {
        return Base64.getEncoder().encodeToString(object.toByteArray());
    }
}
