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

import javax.net.ssl.SSLException;

import org.springframework.stereotype.Component;

import com.ericsson.bos.dr.rest.service.exceptions.ErrorCode;
import com.ericsson.bos.dr.rest.service.exceptions.RestServiceException;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

/**
 * Provides SSL Context for unsecure communication.
 */
@Component
public class UnsecuredSslCtx {

    private final SslContext unsecureCtx;

    /**
     * UnsecuredSslCtx.
     */
    public UnsecuredSslCtx() {
        try {
            unsecureCtx = SslContextBuilder
                    .forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
        } catch (final SSLException e) {
            throw new RestServiceException(e, ErrorCode.GENERAL_ERROR, e.getMessage());
        }
    }
    /**
     * Returns an unsecured SSL Context.
     *
     * @return the SSL Context.
     */
    SslContext get() {
        return unsecureCtx;
    }
}