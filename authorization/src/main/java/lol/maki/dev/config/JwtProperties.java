package lol.maki.dev.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.core.io.Resource;
import org.springframework.util.Base64Utils;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@ConfigurationProperties(prefix = "jwt")
@ConstructorBinding
public class JwtProperties {
    private final KeyPair keyPair;

    public JwtProperties(Resource privateKey, Resource publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.keyPair = new KeyPair(resourceToPublicKey(publicKey), resourceToPrivateKey(privateKey));
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    private static PublicKey resourceToPublicKey(Resource resource) throws NoSuchAlgorithmException, InvalidKeySpecException {
        final String key = resourceToString(resource)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .trim()
                .replace("\r\n", "")
                .replace("\n", "");
        final byte[] decode = Base64Utils.decodeFromString(key);
        final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decode);
        final KeyFactory kf = KeyFactory.getInstance("RSA");
        final RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(keySpec);
        return publicKey;
    }

    private static PrivateKey resourceToPrivateKey(Resource resource) throws NoSuchAlgorithmException, InvalidKeySpecException {
        final String key = resourceToString(resource);
        final byte[] decoded = Base64Utils.decodeFromString(key
                .replace("fake", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .trim()
                .replace("\r\n", "")
                .replace("\n", ""));
        final EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        final KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }


    private static String resourceToString(Resource resource) {
        try (InputStream stream = resource.getInputStream()) {
            return StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
