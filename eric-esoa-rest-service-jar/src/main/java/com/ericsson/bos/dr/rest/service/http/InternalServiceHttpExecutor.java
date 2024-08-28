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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.netty.handler.ssl.SslContext;

/**
 * Executes HTTP requests against services that are internal to the D&R cluster (i.e. Subsystem Manager).
 * <p>
 * If TLS is enabled the HTTP requests are secured using SIP-TLS.
 * </p>
 */
@Component("internal_service")
public class InternalServiceHttpExecutor extends HttpExecutor {

    @Value("${security.tls.enabled}")
    private boolean securityTlsEnabled;

    @Autowired
    private UnsecuredSslCtx unsecuredSslCtx;

    @Autowired
    private InternalSipTlsSslCtx internalSipTlsSslCtx;

    /**
     * Returns the SslContext for communication.
     *
     * @param httpRequest
     *         http request properties
     * @return SslContext
     */
    @Override
    protected SslContext configureSslContext(final HttpRequest httpRequest) {
        if (securityTlsEnabled) {
            return internalSipTlsSslCtx.get();
        } else {
            return unsecuredSslCtx.get();
        }
    }
}
