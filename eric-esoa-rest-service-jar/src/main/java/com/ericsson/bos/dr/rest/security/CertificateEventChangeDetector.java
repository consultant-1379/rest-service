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

package com.ericsson.bos.dr.rest.security;

import com.ericsson.bos.dr.rest.service.http.ExternalSslCtx;
import com.google.common.collect.Maps;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher.Action;

import java.util.Base64;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.kubernetes.commons.config.reload.ConfigReloadProperties;
import org.springframework.core.env.AbstractEnvironment;

import static io.fabric8.kubernetes.client.Watcher.Action.ADDED;
import static io.fabric8.kubernetes.client.Watcher.Action.DELETED;
import static io.fabric8.kubernetes.client.Watcher.Action.MODIFIED;

/**
 * Detect certificate changes and dynamically updates keyManager and trustManager to load the new certificates
 */
public class CertificateEventChangeDetector extends ChangeDetector {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateEventChangeDetector.class);

    @Autowired
    private ExternalSslCtx externalSslCtx;

    @Autowired
    private CertificateConfig certConfig;

    /**
     * Constructor
     * @param environment AbstractEnvironment
     * @param properties ConfigReloadProperties
     * @param kubernetesClient kubernetesClient
     * @param externalSslCtx externalSslCtx
     */
    public CertificateEventChangeDetector(AbstractEnvironment environment,
                                           KubernetesClient kubernetesClient,
                                           ConfigReloadProperties properties,
                                           ExternalSslCtx externalSslCtx) {
        super(environment, kubernetesClient, properties);
        this.externalSslCtx = externalSslCtx;
    }

    @Override
    public void subscribe() {
        if (properties.monitoringSecrets()) {
            LOGGER.info("Certificate event detector is ENABLED");
            final var detectorName = this.toString();
            if (kubernetesClient.secrets() != null) {
                kubernetesClient.secrets().watch(new Subscriber<>(detectorName) {
                    @Override
                    public void eventReceived(final Action action, final Secret secret) {
                        onSecret(action, secret);
                    }
                });
            }
        }
    }

    private void onSecret(final Action action, final Secret secret) {
        certConfig.getSecrets().forEach(secretConfig -> handleSecret(secretConfig, action, secret));
    }

    private void handleSecret(CertificateConfig.SecretConfig secretConfig, Action action, Secret secret){
        final String secretName = secret.getMetadata().getName();
        if (secretIsSupportedAndActionIsAddOrModify(secretConfig.getNamePrefix(), secretName, action)) {
            LOGGER.info("Detected {} change in secrets {}", action, secretName);
            final String secretData = Optional.ofNullable(secret.getData()).orElseGet(Maps::newHashMap).get(secretConfig.getDataFieldKey());
            if (secretData != null) {
                addCertificate(secretName, secretData, secretConfig);
                LOGGER.info("SecretCredentials {} has been updated in store by action {}", secretName, action);
            } else {
                LOGGER.warn("No secret data was found in secret {}", secretName);
            }
        } else if (secretIsSupportedAndActionIsDelete(secretConfig.getNamePrefix(), secretName, action)) {
            LOGGER.info("Detected {} change in secrets {}", action, secretName);
            removeCertificate(secretName, secretConfig);
            LOGGER.info("SecretCredentials {} has been deleted from store by action {}", secretName, action);
        }
    }

    private void addCertificate(String secretName, String secretData, CertificateConfig.SecretConfig secretConfig) {
        final byte[] decodedCert = Base64.getDecoder().decode(secretData);
        if (secretConfig.isTrustStoreSecret()) {
            externalSslCtx.addTruststoreCert(secretName, decodedCert);
        } else {
            externalSslCtx.addKeystoreCert(secretName, decodedCert);
        }
    }

    private void removeCertificate(String secretName, CertificateConfig.SecretConfig secretConfig) {
        if (secretConfig.isTrustStoreSecret()) {
            externalSslCtx.deleteTruststoreCert(secretName);
        } else {
            externalSslCtx.deleteKeystoreCert(secretName);
        }
    }

    private boolean secretIsSupportedAndActionIsAddOrModify(String namePrefix, String secretName, Action action) {
        return secretName.startsWith(namePrefix) && (ADDED.equals(action) || MODIFIED.equals(action));
    }

    private boolean secretIsSupportedAndActionIsDelete(String namePrefix, String secretName, Action action) {
        return secretName.startsWith(namePrefix) && DELETED.equals(action);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}