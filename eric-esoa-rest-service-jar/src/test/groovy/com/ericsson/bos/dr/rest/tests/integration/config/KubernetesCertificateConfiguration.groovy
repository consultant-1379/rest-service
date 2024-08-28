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
package com.ericsson.bos.dr.rest.tests.integration.config

import io.fabric8.kubernetes.client.KubernetesClient
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cloud.kubernetes.commons.config.reload.ConfigReloadProperties
import org.springframework.context.annotation.Bean
import spock.lang.Specification

@TestConfiguration
class KubernetesCertificateConfiguration extends Specification {

    @Bean
    public KubernetesClient getKubernetesClient() {
        return Mock(KubernetesClient)
    }

    @Bean
    public ConfigReloadProperties getConfigReloadProperties() {
        return new ConfigReloadProperties(true, false, true,
                null, null, null, null, false, null)
    }
}
