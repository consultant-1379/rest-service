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
package com.ericsson.bos.dr.rest.service.exceptions;

/**
 * Exception used by the Http executor.
 */
public class HttpExecutorException extends RestServiceException {
    private static final ErrorCode errorCode = ErrorCode.HTTP_EXECUTION_ERROR;

    /**
     * Constructs new <code>HttpExecutorException</code> with command and message.
     *
     * @param command command that resulted in the exception being thrown
     * @param message error message
     */
    public HttpExecutorException(final String command, final String message) {
        super(errorCode, command, message);
    }

    /**
     * Constructs new <code>HttpExecutorException</code> with cause, command and message.
     *
     * @param cause the cause
     * @param command command that resulted in the exception being thrown
     * @param message error message
     */
    public HttpExecutorException(final Throwable cause, final String command, final String message) {
        super(cause, errorCode, command, message);
    }
}