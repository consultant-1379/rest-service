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

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.ericsson.oss.orchestration.so.common.error.factory.ErrorMessageFactory;
import com.ericsson.oss.orchestration.so.common.error.message.ErrorMessage;

/**
 * Base class for rest-service exceptions based on <code>ErrorCode</code>.
 */
public class RestServiceException extends RuntimeException {

    private final transient ErrorMessage errorMessage;
    private final ErrorCode errorCode;

    /**
     * RestServiceException without cause
     * @param errorCode error code
     * @param errorData error data
     */
    public RestServiceException(final ErrorCode errorCode, String... errorData) {
        this.errorCode = errorCode;
        this.errorMessage = createErrorMessage(this.errorCode.getErrorCode(), Arrays.asList(errorData));
    }

    /**
     * RestServiceException with cause
     * @param cause cause
     * @param errorCode error code
     * @param errorData error data
     */
    public RestServiceException(final Throwable cause, final ErrorCode errorCode, String... errorData) {
        super(cause);
        this.errorCode = errorCode;
        this.errorMessage = createErrorMessage(this.errorCode.getErrorCode(), Arrays.asList(errorData));
    }

    @Override
    public String getMessage() {
        return errorMessage.getErrorCode() + ":" + errorMessage.getUserMessage();
    }

    public ErrorMessage getErrorMessage(){
        return this.errorMessage;
    }

    public HttpStatus getHttpStatus() {
        return errorCode.getHttpStatus();
    }

    private ErrorMessage createErrorMessage(String errorCode, List<String> errorData) {
        return ErrorMessageFactory.buildFrom(errorCode, errorData);
    }
}
