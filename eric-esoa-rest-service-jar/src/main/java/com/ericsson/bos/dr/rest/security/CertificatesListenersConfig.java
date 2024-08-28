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
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.cloud.kubernetes.commons.ConditionalOnKubernetesConfigEnabled;
import org.springframework.cloud.kubernetes.commons.config.reload.ConfigReloadProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.AbstractEnvironment;

/**
 * Certificate Listener config
 */
@Configuration
@ConditionalOnKubernetesConfigEnabled
public class CertificatesListenersConfig {

    /**
     * Construct new CertificateEventChangeDetector
     * @param environment AbstractEnvironment
     * @param kubernetesClient kubernetesClient
     * @param properties ConfigReloadProperties
     * @param externalSslCtx externalSslCtx
     * @return CertificateEventChangeDetector
     */
    @Bean
    public ChangeDetector certificateSecretChangeDetector(AbstractEnvironment environment,
                                                          KubernetesClient kubernetesClient,
                                                          ConfigReloadProperties properties,
                                                          ExternalSslCtx externalSslCtx) {
        return new CertificateEventChangeDetector(environment, kubernetesClient, properties, externalSslCtx);
    }
}
