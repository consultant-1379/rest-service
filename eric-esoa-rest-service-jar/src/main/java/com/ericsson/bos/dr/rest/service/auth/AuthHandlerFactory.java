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
package com.ericsson.bos.dr.rest.service.auth;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.bos.dr.rest.service.exceptions.ErrorCode;
import com.ericsson.bos.dr.rest.service.exceptions.RestServiceException;

/**
 * Factory returning Auth Handler implementation of given type
 */
@Component
public class AuthHandlerFactory {

    @Autowired
    private List<AuthHandler> authHandlers;

    /**
     * Factory method to get auth handler implementation
     * @param authHandlerType Auth type to determine the correct implementation
     * @return AuthHandler implementation
     */
    public AuthHandler get(final String authHandlerType) {
        return authHandlers.stream()
            .filter(a -> authHandlerType.equalsIgnoreCase(a.authType()))
            .findFirst()
            .orElseThrow(
                () -> new RestServiceException(ErrorCode.AUTH_HANDLER_NOT_SUPPORTED, authHandlerType)
            );
    }

}
