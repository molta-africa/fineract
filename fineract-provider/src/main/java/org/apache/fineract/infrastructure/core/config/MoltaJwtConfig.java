/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.core.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * @author Ikechi Ucheagwu
 * @createdOn May-26(Mon)-2025
 */

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty("fineract.security.oauth.custom.enabled")
public class MoltaJwtConfig {

    @Value("${molta.securityPEMStorePath}")
    private String securityPEMStorePath;

    @Bean
    public RSAPrivateKey jwtSigningKey() {
        Path path;
        try {
            String moltaPrivateKeyPEMPath = securityPEMStorePath.concat("/").concat( "molta_private_key.pem");
            path = Paths.get(moltaPrivateKeyPEMPath);
        } catch (Exception e) {
            log.error("Could get RSA private key from PEM path:: {}", securityPEMStorePath, e);
            throw new IllegalArgumentException("Could get RSA private key from PEM path: " + securityPEMStorePath);
        }

        try (InputStream inputStream = new FileInputStream(path.toFile())) {
            String privateKeyPEM = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Could not load RSA private key from PEM: {}", securityPEMStorePath, e);
        }

        throw new IllegalArgumentException("Could not load RSA private key from PEM");
    }

    @Bean
    public RSAPublicKey jwtValidationKey() {
        Path path;
        try {
            String moltaPublicKeyPEMPath = securityPEMStorePath.concat("/").concat( "molta_public_key.pem");
            path = Paths.get(moltaPublicKeyPEMPath);
        } catch (Exception e) {
            log.error("Could get RSA public key from PEM path: {}", securityPEMStorePath, e);
            throw new IllegalArgumentException("Could get RSA public key from PEM path: " + securityPEMStorePath);
        }

        try (InputStream inputStream = new FileInputStream(path.toFile())) {
            String publicKeyPEM = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] decoded = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(keySpec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Could not load RSA public key from PEM: {}", securityPEMStorePath, e);
        }

        throw new IllegalArgumentException("Could not load RSA public key from PEM");
    }

}
