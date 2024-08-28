/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.bos.dr.rest.service.http;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ericsson.bos.dr.rest.service.exceptions.ErrorCode;
import com.ericsson.bos.dr.rest.service.exceptions.RestServiceException;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.stereotype.Component;

/**
 * Provides SslContext for secure communication with external connected systems. The SslContext is generated based
 * on the keystore and truststore secret names defined in the http request. The certs must be
 * added prior to getting the SslContext. The same instance of SslContext will be returned for the same
 * combination of truststore and keystore secrets names in the http request.
 */
@Component
public class ExternalSslCtx {

    private record SslContextKey(String trustStoreSecretName, String keystoreSecretName) {}

    private final Map<SslContextKey, SslContext> sslContexts = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, byte[]> trustStorePkcs12Certs = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, byte[]> keyStorePkcs12Certs = Collections.synchronizedMap(new HashMap<>());

    /**
     * Get an SSL Context for keystore and truststore specified in the http request.
     *
     * @return the SSL Context.
     */
    public SslContext get(final HttpRequest httpRequest) {
        final SslContextKey sslContextKey =
                new SslContextKey(httpRequest.getTrustStoreSecretName(), httpRequest.getKeyStoreSecretName());
        if (!sslContexts.containsKey(sslContextKey)) {
            try {
                var sslContextBuilder = SslContextBuilder.forClient();
                if (httpRequest.getTrustStoreSecretName() != null) {
                    sslContextBuilder = sslContextBuilder.trustManager(loadTrustManagerFactory(httpRequest.getTrustStoreSecretName(),
                            httpRequest.getTrustStoreSecretPassword()));
                }
                if (httpRequest.getKeyStoreSecretName() != null) {
                    sslContextBuilder = sslContextBuilder.keyManager(loadKeyManagerFactory(httpRequest.getKeyStoreSecretName(),
                            httpRequest.getKeyStoreSecretPassword()));
                }
                sslContexts.put(sslContextKey, sslContextBuilder.build());
            } catch (SSLException e) {
                throw new RestServiceException(e, ErrorCode.GENERAL_ERROR, e.getMessage());
            }
        }
        return sslContexts.get(sslContextKey);
    }

    /**
     * Creates a new instance of <code>TrustManagerFactory</code> initialized with trust store
     * containing PKCS12 cert identified by its secret name.
     * An entry must exist in the local cache for the secret name, otherwise an exception is thrown.
     *
     * @param secretName name of the secret containing the pkcs12 cert to be loaded to the keystore
     * @param secretPassword secret password required to check integrity of secret data. This is password used during creation of the secret data, and
     *                      can be empty if no password was used.
     * @return TrustManagerFactory the trust manager factory
     */
    private TrustManagerFactory loadTrustManagerFactory(final String secretName, final String secretPassword) {
        final var truststoreCert = Optional.ofNullable(trustStorePkcs12Certs.get(secretName))
                .orElseThrow(() -> new RestServiceException(ErrorCode.CERTIFICATE_HANDLING_FAILED,
                        "X509TrustManager", secretName, "Empty secret data"));
        try {
            final var trustStore = KeyStore.getInstance("PKCS12");
            trustStore.load(new ByteArrayInputStream(truststoreCert), secretPassword.toCharArray());
            final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            return trustManagerFactory;
        } catch (Exception e) {
            throw new RestServiceException(e, ErrorCode.CERTIFICATE_HANDLING_FAILED,
                    "X509TrustManager", secretName, e.getMessage());
        }
    }

    /**
     * Creates a new instance of <code>KeyManagerFactory</code> initialized with keystore
     * containing PKCS12 cert identified by its secret name.
     * An entry must exist in the local cache for the secret name, otherwise an exception is thrown.
     *
     * @param secretName name of the secret containing the pkcs12 cert to be loaded to the keystore
     * @param secretPassword secret password required to check integrity of secret data. This is password used during creation of the secret data, and
     *                      can be empty if no password was used.
     * @return KeyManagerFactory the key manager factory
     */
    private KeyManagerFactory loadKeyManagerFactory(final String secretName, final String secretPassword) {
        final byte[] keystoreCert = Optional.ofNullable(keyStorePkcs12Certs.get(secretName))
                .orElseThrow(() -> new RestServiceException(ErrorCode.CERTIFICATE_HANDLING_FAILED,
                        "X509KeyManager", secretName, "Empty secret data"));
        try {
            final var clientKeyStore = KeyStore.getInstance("PKCS12");
            clientKeyStore.load(new ByteArrayInputStream(keystoreCert), secretPassword.toCharArray());
            final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(clientKeyStore, secretPassword.toCharArray());
            return keyManagerFactory;
        } catch (Exception e) {
            throw new RestServiceException(e, ErrorCode.CERTIFICATE_HANDLING_FAILED, "X509KeyManager", secretName, e.getMessage());
        }
    }

    /**
     * Add PKCS12 cert identified by a secret name.
     * @param secretName name of the secret key that contains the secret data
     * @param pkcs12Cert decoded pkcs12 cert contained in the secret
     */
    public void addKeystoreCert(final String secretName, final byte[] pkcs12Cert) {
        if (keyStorePkcs12Certs.put(secretName, pkcs12Cert) != null) {
            removeSslContextsUsingKeystore(secretName);
        }
    }

    /**
     * Delete PKCS12 cert.
     * @param secretName name of the secret key that contains the secret data
     */
    public void deleteKeystoreCert(final String secretName) {
        keyStorePkcs12Certs.remove(secretName);
        removeSslContextsUsingKeystore(secretName);
    }

    /**
     * Add PKCS12 cert identified by a secret name.
     * @param secretName name of the secret key that contains the secret data
     * @param pkcs12Cert decoded pkcs12 cert contained in the secret
     */
    public void addTruststoreCert(final String secretName, final byte[] pkcs12Cert) {
        if (trustStorePkcs12Certs.put(secretName, pkcs12Cert) != null) {
            removeSslContextsUsingTruststore(secretName);
        }
    }

    /**
     * Delete PKCS12 cert.
     * @param secretName name of the secret key that contains the secret data
     */
    public void deleteTruststoreCert(final String secretName) {
        trustStorePkcs12Certs.remove(secretName);
        removeSslContextsUsingTruststore(secretName);
    }

    private void removeSslContextsUsingKeystore(final String secretName) {
        final List<SslContextKey> keysToRemove = sslContexts.keySet().stream()
                .filter(k -> secretName.equals(k.keystoreSecretName())).toList();
        keysToRemove.forEach(sslContexts::remove);
    }

    private void removeSslContextsUsingTruststore(final String secretName) {
        final List<SslContextKey> keysToRemove = sslContexts.keySet().stream()
                .filter(k -> secretName.equals(k.trustStoreSecretName())).toList();
        keysToRemove.forEach(sslContexts::remove);
    }
}