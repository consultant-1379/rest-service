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
package com.ericsson.bos.dr.rest.web;

import static com.ericsson.bos.dr.rest.service.exceptions.ErrorCode.GENERAL_ERROR;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ericsson.bos.dr.rest.service.exceptions.RestServiceException;
import com.ericsson.bos.dr.rest.web.v1.api.model.ErrorResponseDto;
import com.ericsson.oss.orchestration.so.common.error.factory.ErrorMessageFactory;

/**
 * Custom exception handlers.
 */
@ControllerAdvice
public class RestServiceControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestServiceControllerAdvice.class);

    /**
     * Rest Service Exception
     * @param ex RestServiceException
     * @return ErrorResponseDto
     */
    @ExceptionHandler(RestServiceException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponseDto> handleRestServiceException(final RestServiceException ex) {
        LOGGER.error("Rest-Service Exception", ex);
        final var errorResponseDto = new ErrorResponseDto().errorCode(ex.getErrorMessage().getErrorCode())
                .errorMessage(ex.getErrorMessage().getUserMessage());
        return new ResponseEntity<>(errorResponseDto, ex.getHttpStatus());
    }

    /**
     * General Exception Handler.
     * @param ex Exception
     * @return ErrorResponseDto
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ErrorResponseDto> handleUnexpectedError(final Exception ex) {
        LOGGER.error("Internal Server Error", ex);
        if (ex.getCause() instanceof RestServiceException) {
            return handleRestServiceException((RestServiceException) ex.getCause());
        }

        final var errorMessage = ErrorMessageFactory.buildFrom(GENERAL_ERROR.getErrorCode(), ExceptionUtils.getRootCauseMessage(ex));
        final var errorResponseDto = new ErrorResponseDto().errorCode(errorMessage.getErrorCode())
                .errorMessage(errorMessage.getUserMessage());
        return new ResponseEntity<>(errorResponseDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
