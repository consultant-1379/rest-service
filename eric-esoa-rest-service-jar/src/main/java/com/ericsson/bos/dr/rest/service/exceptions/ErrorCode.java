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

import org.springframework.http.HttpStatus;

/**
 * Error codes defined in error.properties.
 */
public enum ErrorCode {

    RESOURCE_CONFIGURATION_NOT_FOUND("RS-01", HttpStatus.NOT_FOUND),
    RESOURCE_CONFIGURATION_ALREADY_EXISTS("RS-02", HttpStatus.CONFLICT),
    RESOURCE_CONFIGURATION_IO_READ_ERROR("RS-03", HttpStatus.INTERNAL_SERVER_ERROR),
    RESOURCE_NOT_FOUND("RS-04", HttpStatus.NOT_FOUND),
    RESOURCE_METHOD_NOT_FOUND("RS-05", HttpStatus.NOT_FOUND),
    JQ_ERROR("RS-06", HttpStatus.INTERNAL_SERVER_ERROR),
    SUBSTITUTION_FAILED("RS-07", HttpStatus.INTERNAL_SERVER_ERROR),
    HTTP_EXECUTION_ERROR("RS-08", HttpStatus.INTERNAL_SERVER_ERROR),
    CONNECTED_SYSTEM_NOT_FOUND("RS-09", HttpStatus.NOT_FOUND),
    CONTENT_TYPE_IN_HTTPRESPONSE_NOT_SUPPORTED("RS-10", HttpStatus.INTERNAL_SERVER_ERROR),
    SCHEMA_ERROR("RS-11", HttpStatus.BAD_REQUEST),
    AUTH_HANDLER_NOT_SUPPORTED("RS-12", HttpStatus.INTERNAL_SERVER_ERROR),
    AUTH_PROPERTIES_MISSING("RS-13", HttpStatus.NOT_FOUND),
    COOKIE_AUTHENTICATION_FAILED("RS-14", HttpStatus.UNAUTHORIZED),
    METHOD_NAME_REQUIRED_IN_RUNREQUEST("RS-15", HttpStatus.INTERNAL_SERVER_ERROR),
    INCORRECT_SECRET_NAME("RS-16", HttpStatus.INTERNAL_SERVER_ERROR),
    CERTIFICATE_HANDLING_FAILED("RS-17", HttpStatus.INTERNAL_SERVER_ERROR),
    GET_AUTH_TOKEN_ERROR("RS-18", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_AUTH_BODY("RS-19", HttpStatus.BAD_REQUEST),
    GENERAL_ERROR("RS-500", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final HttpStatus httpStatus;

    /**
     * ErrorCode
     * @param errorCode error code
     * @param httpStatus associated http status
     */
    ErrorCode(final String errorCode, final HttpStatus httpStatus) {
        this.code = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
