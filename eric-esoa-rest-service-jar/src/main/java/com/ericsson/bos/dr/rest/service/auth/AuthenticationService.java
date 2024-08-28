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


import com.ericsson.bos.dr.rest.service.connectivity.Subsystem;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Authenticate towards external system.
 */
@Component
public class AuthenticationService {

    @Autowired
    private AuthHandlerFactory authHandlerFactory;

    /**
     * Authenticate towards external system using the auth properties defined for the subsystem.
     * If the 'auth.subsystemName' property points to an auth subsystem then fetch that subsystem
     * and use its properties to authenticate.
     * @param subsystem original subsystem
     * @return authentication token
     */
    public String authenticate(final Subsystem subsystem) {
        final Subsystem authSubsystem = subsystem.getAuthSubsystem().orElse(subsystem);
        final var authHandler = authHandlerFactory.get(authSubsystem.getConnection().getAuthType());
        final String authKey = authHandler.getAuthKey(authSubsystem);
        if (StringUtils.isNotBlank(authKey)) {
            final String uniqueTokenCacheKey = authSubsystem.getName().concat("_").concat(authKey);
            return authHandler.getAuthToken(authSubsystem, uniqueTokenCacheKey);
        }
        return null;
    }

}