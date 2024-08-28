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
package com.ericsson.bos.dr.rest.service.utils;

import java.net.ConnectException;
import java.net.UnknownHostException;

import org.springframework.web.reactive.function.client.WebClientRequestException;

import io.netty.handler.timeout.TimeoutException;

/**
 * Utility class containing common methods for identifying error conditions from the exceptions thrown when they occur.
 */
public class ExceptionChecks {
    private ExceptionChecks() {
    }

    /**
     * Checks a {@link Throwable} to determine whether it is a {@link WebClientRequestException} caused by
     * some issue establishing a HTTP connection to a remote host.
     *
     * @param requestException the exception to check
     *
     * @return boolean whether the exception is a connection issue
     */
    public static boolean isConnectionIssue(final Throwable failure) {
        return failure instanceof final WebClientRequestException requestException
                && (isReadOrWriteTimeout(requestException)
                        || isUnknownHost(requestException)
                        || isConnectionTimeoutOrRefused(requestException));
    }

    /**
     * Checks a {@link WebClientRequestException} to determine whether it was caused by
     * no data being either read or written within a certain period of time.
     *
     * @param requestException the exception to check
     *
     * @return boolean whether the exception is a read/write timeout
     */
    public static boolean isReadOrWriteTimeout(final WebClientRequestException requestException) {
        return requestException.getCause() instanceof TimeoutException;
    }

    /**
     * Checks a {@link WebClientRequestException} to determine whether it was caused by
     * the request being made to an unknown hostname.
     *
     * @param requestException the exception to check
     *
     * @return boolean whether the exception was caused by an unknown hostname
     */
    public static boolean isUnknownHost(final WebClientRequestException requestException) {
        return requestException.getCause() instanceof UnknownHostException;
    }

    /**
     * Checks a {@link WebClientRequestException} to determine whether it was caused by
     * an issue establishing the HTTP connection - e.g. connection refused or connection timeout.
     *
     * @param requestException the exception to check
     *
     * @return boolean whether the exception was caused by a connection issue
     */
    public static boolean isConnectionTimeoutOrRefused(final WebClientRequestException requestException) {
        return requestException.getRootCause() instanceof ConnectException;
    }
}