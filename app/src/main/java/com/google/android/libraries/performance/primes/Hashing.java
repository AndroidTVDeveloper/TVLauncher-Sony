package com.google.android.libraries.performance.primes;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Hashing {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    public static Long hash(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(name.getBytes(UTF_8));
            return Long.valueOf(ByteBuffer.wrap(md.digest()).getLong());
        } catch (NoSuchAlgorithmException impossible) {
            throw new RuntimeException(impossible);
        }
    }
}
