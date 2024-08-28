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

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Certificate Config
 */
@Configuration
@ConfigurationProperties(prefix = "certificates")
public class CertificateConfig {

    private List<SecretConfig> secrets;

    public List<SecretConfig> getSecrets() {
        return secrets;
    }

    public void setSecrets(List<SecretConfig> secrets) {
        this.secrets = secrets;
    }

    /**
     * Secret configuration.
     */
    @Getter
    @Setter
    public static class SecretConfig {
        private String type;
        private String namePrefix;
        private String dataFieldKey;

        /**
         * Check if truststore secret.
         * @return true if truststore secret
         */
        public boolean isTrustStoreSecret() {
            return "truststore".equalsIgnoreCase(type);
        }
    }
}